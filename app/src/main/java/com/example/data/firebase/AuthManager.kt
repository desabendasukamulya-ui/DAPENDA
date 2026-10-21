package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.util.SessionManager
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthManager(
  private val context: Context,
  private val firestoreRepository: FirestoreRepository,
  private val scope: CoroutineScope
) {
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
  private val sessionManager = SessionManager(context)

  private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  private val _userProfile = MutableStateFlow<UserProfile?>(sessionManager.getSavedSession())
  val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

  private val _isAuthenticating = MutableStateFlow(false)
  val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

  private val _authError = MutableStateFlow<String?>(null)
  val authError: StateFlow<String?> = _authError.asStateFlow()

  val isUserLoggedIn: Boolean
    get() = _currentUser.value != null || _userProfile.value != null

  init {
    auth.addAuthStateListener { firebaseAuth ->
      val user = firebaseAuth.currentUser
      _currentUser.value = user
      if (user != null) {
        scope.launch {
          checkAndRegisterUserProfile(user)
          firestoreRepository.observeUserProfile(user.uid).collect { profile ->
            if (profile != null) {
              _userProfile.value = profile
              sessionManager.saveSession(profile, true)
            }
          }
        }
      }
    }
  }

  /**
   * Helper untuk mengekstrak Activity murni dari Context hierarki apapun.
   * Menjamin tidak ada lagi error "Failed to launch the selector UI. Hint: ensure the context parameter is an Activity-based context".
   */
  private fun extractActivity(context: Context): Activity {
    var current: Context = context
    while (current is ContextWrapper) {
      if (current is Activity) return current
      current = current.baseContext
    }
    return (current as? Activity)
      ?: throw IllegalArgumentException("Parameter context wajib berupa Activity-based context. Diterima: ${context::class.java.name}")
  }

  /**
   * Masuk menggunakan Akun Google dengan Activity-based context yang tervalidasi.
   */
  suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
    _isAuthenticating.value = true
    _authError.value = null

    return try {
      val activity = extractActivity(activityContext)
      val credentialManager = CredentialManager.create(activity)
      val defaultClientId = activity.getString(R.string.default_web_client_id)

      val googleIdOption = GetSignInWithGoogleOption.Builder(defaultClientId).build()
      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(context = activity, request = request)
      val credential = result.credential

      if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        val authResult = auth.signInWithCredential(authCredential).await()
        val user = checkNotNull(authResult.user) { "Pengguna tidak ditemukan setelah autentikasi" }

        checkAndRegisterUserProfile(user)
        _isAuthenticating.value = false
        Result.success(user)
      } else {
        _isAuthenticating.value = false
        val errorMsg = "Tipe kredensial tidak valid"
        _authError.value = errorMsg
        Result.failure(IllegalStateException(errorMsg))
      }
    } catch (e: GetCredentialCancellationException) {
      _isAuthenticating.value = false
      Result.failure(e)
    } catch (e: Exception) {
      Log.e("AuthManager", "Gagal login Google", e)
      _isAuthenticating.value = false
      val raw = e.localizedMessage ?: "Gagal masuk dengan Google"
      val friendlyMsg = if (raw.contains("Activity-based context", ignoreCase = true) || raw.contains("selector UI", ignoreCase = true)) {
        "Google Sign-In tidak dapat diluncurkan di perangkat ini. Silakan gunakan formulir Masuk / Daftar Operator RT/RW di bawah untuk masuk langsung."
      } else if (raw.contains("No credential available", ignoreCase = true)) {
        "Tidak ada akun Google yang terdeteksi di perangkat ini. Silakan gunakan formulir Masuk / Daftar Operator RT/RW di bawah."
      } else {
        raw
      }
      _authError.value = friendlyMsg
      Result.failure(Exception(friendlyMsg, e))
    }
  }

  /**
   * Masuk sebagai Operator RT/RW menggunakan Email/Username/No HP dan Kata Sandi/PIN.
   * Mendukung penyimpanan sesi (remember me) agar dapat langsung buka di kemudian hari.
   */
  suspend fun loginOperator(
    identifier: String,
    password: String,
    rememberMe: Boolean = true
  ): Result<UserProfile> {
    _isAuthenticating.value = true
    _authError.value = null

    return try {
      val trimmed = identifier.trim()
      if (trimmed.isBlank() || password.isBlank()) {
        val msg = "Email/Username/No. HP dan kata sandi wajib diisi!"
        _authError.value = msg
        _isAuthenticating.value = false
        return Result.failure(IllegalArgumentException(msg))
      }

      val hash = SessionManager.hashPassword(password)

      // 1. Penanganan Khusus Akun Administrator Desa Benda
      if (trimmed.equals("admin", ignoreCase = true) ||
          trimmed.equals("desabendasukamulya@gmail.com", ignoreCase = true)) {

        // Cari profil admin dari Firestore atau local preference
        val cloudAdmin = firestoreRepository.getAdminProfile()
        val localAdminHash = sessionManager.getAdminPasswordHash()
        val expectedHash = when {
          cloudAdmin != null && cloudAdmin.passwordHash.isNotBlank() -> cloudAdmin.passwordHash
          localAdminHash != null && localAdminHash.isNotBlank() -> localAdminHash
          else -> SessionManager.getDefaultAdminPasswordHash()
        }

        if (hash != expectedHash) {
          val msg = "Kata sandi Admin salah. Gunakan kata sandi 'Benda15631' (atau kata sandi baru jika telah Anda rubah)."
          _authError.value = msg
          _isAuthenticating.value = false
          return Result.failure(IllegalArgumentException(msg))
        }

        val adminProfile = cloudAdmin?.copy(passwordHash = hash) ?: UserProfile(
          uid = "admin_benda_official",
          email = "desabendasukamulya@gmail.com",
          displayName = "Administrator Desa Benda",
          role = "admin",
          rt = "Semua RT",
          rw = "Semua RW",
          phone = "081234567890",
          passwordHash = hash
        )

        firestoreRepository.saveUserProfile(adminProfile)
        sessionManager.saveAdminPasswordHash(hash)
        _userProfile.value = adminProfile
        sessionManager.saveSession(adminProfile, rememberMe)
        _isAuthenticating.value = false
        return Result.success(adminProfile)
      }

      // 2. Pemeriksaan Akun Operator yang Terdaftar di Cloud Firestore
      var matchedUser = firestoreRepository.findUserByIdentifier(trimmed)

      // 3. Fallback pemeriksaan sesi lokal yang telah tersimpan
      if (matchedUser == null) {
        val localUser = sessionManager.getSavedSession()
        if (localUser != null && (localUser.email.equals(trimmed, ignoreCase = true) ||
                                  localUser.displayName.equals(trimmed, ignoreCase = true) ||
                                  localUser.phone == trimmed)) {
          matchedUser = localUser
        }
      }

      // 4. Syarat mutlak: IJINKAN HANYA YANG AKUNNYA SUDAH TERDAFTAR SAJA
      if (matchedUser == null) {
        val msg = "Akun '$trimmed' belum terdaftar di sistem. Aplikasi tidak dapat dibuka. Silakan hubungi Administrator Desa Benda untuk mendaftarkan akun Operator Anda."
        _authError.value = msg
        _isAuthenticating.value = false
        return Result.failure(IllegalArgumentException(msg))
      }

      // 5. Validasi Kata Sandi Akun Terdaftar
      if (matchedUser.passwordHash.isNotBlank() && matchedUser.passwordHash != hash) {
        val msg = "Kata sandi salah untuk akun '${matchedUser.displayName}'. Silakan periksa kembali."
        _authError.value = msg
        _isAuthenticating.value = false
        return Result.failure(IllegalArgumentException(msg))
      }

      _userProfile.value = matchedUser
      sessionManager.saveSession(matchedUser, rememberMe)
      _isAuthenticating.value = false
      return Result.success(matchedUser)
    } catch (e: Exception) {
      _isAuthenticating.value = false
      val msg = e.localizedMessage ?: "Gagal masuk operator"
      _authError.value = msg
      Result.failure(e)
    }
  }

  /**
   * Mengubah kata sandi akun pengguna (khususnya akun Admin).
   */
  suspend fun changePassword(uid: String, oldPass: String, newPass: String): Result<Unit> {
    if (newPass.length < 5) {
      return Result.failure(IllegalArgumentException("Kata sandi baru minimal 5 karakter!"))
    }

    val oldHash = SessionManager.hashPassword(oldPass)
    val newHash = SessionManager.hashPassword(newPass)

    val current = _userProfile.value
    if (current == null) {
      return Result.failure(IllegalStateException("Pengguna belum masuk"))
    }

    // Jika admin
    if (current.isAdmin || uid == "admin_benda_official") {
      val expectedHash = sessionManager.getAdminPasswordHash() ?: SessionManager.getDefaultAdminPasswordHash()
      if (oldHash != expectedHash && current.passwordHash.isNotBlank() && oldHash != current.passwordHash) {
        return Result.failure(IllegalArgumentException("Kata sandi lama salah!"))
      }
      sessionManager.saveAdminPasswordHash(newHash)
      val updatedAdmin = current.copy(passwordHash = newHash)
      firestoreRepository.saveUserProfile(updatedAdmin)
      _userProfile.value = updatedAdmin
      sessionManager.saveSession(updatedAdmin, true)
      return Result.success(Unit)
    } else {
      // Operator biasa
      if (current.passwordHash.isNotBlank() && oldHash != current.passwordHash) {
        return Result.failure(IllegalArgumentException("Kata sandi lama salah!"))
      }
      val updated = current.copy(passwordHash = newHash)
      firestoreRepository.saveUserProfile(updated)
      _userProfile.value = updated
      sessionManager.saveSession(updated, true)
      return Result.success(Unit)
    }
  }

  /**
   * Pendaftaran akun baru untuk Operator RT / RW.
   * Menyimpan profil di Cloud Firestore dan menyimpan sesi lokal di perangkat secara permanen.
   */
  suspend fun registerOperator(
    name: String,
    emailOrUser: String,
    phone: String,
    rt: String,
    rw: String,
    role: String,
    password: String,
    rememberMe: Boolean = true
  ): Result<UserProfile> {
    _isAuthenticating.value = true
    _authError.value = null

    return try {
      if (name.isBlank() || emailOrUser.isBlank() || password.length < 6) {
        val msg = "Nama, kontak, dan kata sandi minimal 6 karakter wajib diisi!"
        _authError.value = msg
        _isAuthenticating.value = false
        return Result.failure(IllegalArgumentException(msg))
      }

      val hash = SessionManager.hashPassword(password)
      val generatedUid = "op_${System.currentTimeMillis()}_${(1000..9999).random()}"

      val newProfile = UserProfile(
        uid = generatedUid,
        email = emailOrUser.trim().lowercase(),
        displayName = name.trim(),
        role = if (role.contains("Admin", ignoreCase = true)) "admin" else "operator",
        rt = rt,
        rw = rw,
        phone = phone.trim(),
        passwordHash = hash,
        createdAt = System.currentTimeMillis()
      )

      firestoreRepository.saveUserProfile(newProfile)
      _userProfile.value = newProfile
      sessionManager.saveSession(newProfile, rememberMe)
      _isAuthenticating.value = false
      Result.success(newProfile)
    } catch (e: Exception) {
      _isAuthenticating.value = false
      val msg = e.localizedMessage ?: "Pendaftaran gagal"
      _authError.value = msg
      Result.failure(e)
    }
  }

  fun signOut() {
    try {
      auth.signOut()
    } catch (_: Exception) {}
    sessionManager.clearSession()
    _currentUser.value = null
    _userProfile.value = null
  }

  private suspend fun checkAndRegisterUserProfile(user: FirebaseUser) {
    try {
      val email = user.email ?: ""
      val isOfficialAdmin = email.equals("desabendasukamulya@gmail.com", ignoreCase = true)
      val defaultRole = if (isOfficialAdmin) "admin" else "operator"

      val profile = UserProfile(
        uid = user.uid,
        email = email,
        displayName = user.displayName ?: "Petugas Desa",
        role = defaultRole,
        createdAt = System.currentTimeMillis()
      )
      firestoreRepository.saveUserProfile(profile)
      _userProfile.value = profile
      sessionManager.saveSession(profile, true)
    } catch (e: Exception) {
      Log.w("AuthManager", "Gagal menyimpan profil pengguna", e)
    }
  }
}
