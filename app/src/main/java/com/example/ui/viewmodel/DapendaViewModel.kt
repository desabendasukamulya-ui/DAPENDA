package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AuditLog
import com.example.data.firebase.AuthManager
import com.example.data.firebase.FirestoreRepository
import com.example.data.firebase.SyncInfo
import com.example.data.firebase.UserProfile
import com.example.data.local.AppDatabase
import com.example.data.model.Resident
import com.example.data.repository.ResidentRepository
import com.example.data.sample.SampleData
import com.example.ui.navigation.Screen
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DapendaViewModel(application: Application) : AndroidViewModel(application) {

  private val localRepo: ResidentRepository
  private val firestoreRepo: FirestoreRepository = FirestoreRepository(application)
  private val authManager: AuthManager

  private val screenHistory = mutableListOf<Screen>(Screen.Dashboard)
  private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  // Autentikasi Pengguna
  val currentUser: StateFlow<FirebaseUser?>
  val userProfile: StateFlow<UserProfile?>
  val isAuthenticating: StateFlow<Boolean>
  val authError: StateFlow<String?>

  // Status Sinkronisasi Cloud Firestore
  val syncInfo: StateFlow<SyncInfo> = firestoreRepo.syncInfo

  // Data Penduduk Online (Cloud Firestore dengan Offline Cache)
  private val _allResidents = MutableStateFlow<List<Resident>>(emptyList())
  val allResidents: StateFlow<List<Resident>> = _allResidents.asStateFlow()

  // Jumlah data lokal Room (untuk informasi migrasi ke Cloud)
  private val _localResidentList = MutableStateFlow<List<Resident>>(emptyList())
  val localResidentList: StateFlow<List<Resident>> = _localResidentList.asStateFlow()

  // Catatan Aktivitas Audit Log
  private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
  val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

  // Data Riwayat Mutasi Penduduk (Pindah Masuk & Pindah Keluar)
  private val _mutations = MutableStateFlow<List<com.example.data.model.PopulationMutation>>(emptyList())
  val mutations: StateFlow<List<com.example.data.model.PopulationMutation>> = _mutations.asStateFlow()

  // Data Operator RT/RW Terdaftar beserta Nomor WhatsApp
  private val _operators = MutableStateFlow<List<UserProfile>>(emptyList())
  val operators: StateFlow<List<UserProfile>> = _operators.asStateFlow()

  // Filter & Pencarian
  private val _selectedRw = MutableStateFlow("Semua RW")
  val selectedRw: StateFlow<String> = _selectedRw.asStateFlow()

  private val _selectedRt = MutableStateFlow("Semua RT")
  val selectedRt: StateFlow<String> = _selectedRt.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _genderFilter = MutableStateFlow<String?>(null)
  val genderFilter: StateFlow<String?> = _genderFilter.asStateFlow()

  private val _statusTinggalFilter = MutableStateFlow<String?>(null)
  val statusTinggalFilter: StateFlow<String?> = _statusTinggalFilter.asStateFlow()

  private val _bansosOnlyFilter = MutableStateFlow(false)
  val bansosOnlyFilter: StateFlow<String> = MutableStateFlow("Semua")

  // Form State
  private val _formState = MutableStateFlow(ResidentFormState())
  val formState: StateFlow<ResidentFormState> = _formState.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  init {
    val db = AppDatabase.getDatabase(application, viewModelScope)
    localRepo = ResidentRepository(db.residentDao())
    authManager = AuthManager(application, firestoreRepo, viewModelScope)

    currentUser = authManager.currentUser
    userProfile = authManager.userProfile
    isAuthenticating = authManager.isAuthenticating
    authError = authManager.authError

    // Bersihkan data contoh lokal
    viewModelScope.launch {
      localRepo.ensureInitialized()
      _localResidentList.value = localRepo.getLocalResidentsList()
    }

    // Amati data cloud saat pengguna berhasil terautentikasi (via Google Sign-In maupun Operator RT/RW)
    viewModelScope.launch {
      combine(currentUser, userProfile) { user, profile ->
        user != null || profile != null
      }.collect { isLoggedIn ->
        if (isLoggedIn) {
          // Amati data penduduk dari Cloud Firestore
          launch {
            firestoreRepo.observeResidents().collect { list ->
              _allResidents.value = list
              // Simpan salinan ke basis data lokal Room secara background
              localRepo.syncFromCloud(list)
            }
          }
          // Amati riwayat log aktivitas
          launch {
            firestoreRepo.observeAuditLogs().collect { logs ->
              _auditLogs.value = logs
            }
          }
          // Amati riwayat mutasi penduduk (Pindah Masuk & Keluar)
          launch {
            firestoreRepo.observeMutations().collect { muts ->
              _mutations.value = muts
            }
          }
          // Amati data operator RT/RW terdaftar (hanya operator yang mendaftar atau didaftarkan admin)
          launch {
            firestoreRepo.cleanupDummyOperators()
            firestoreRepo.observeAllUsers().collect { list ->
              _operators.value = list.filter {
                !it.isAdmin &&
                !com.example.data.sample.SampleData.dummyOperatorUids.contains(it.uid) &&
                !it.uid.startsWith("op_rt") &&
                !it.email.endsWith("@desabenda.id")
              }
            }
          }
          // Sinkronisasikan foto lokal yang ada di HP operator ke cloud jika belum terunggah
          launch {
            try {
              val localList = localRepo.getLocalResidentsList()
              if (localList.isNotEmpty()) {
                firestoreRepo.uploadLocalPhotosIfMissing(localList, getActiveOperatorIdentity())
              }
            } catch (_: Exception) {}
          }
        } else {
          _allResidents.value = emptyList()
          _auditLogs.value = emptyList()
          _mutations.value = emptyList()
          _operators.value = emptyList()
        }
      }
    }
  }

  // Filtered residents list
  val filteredResidents: StateFlow<List<Resident>> = combine(
    allResidents,
    _selectedRw,
    _selectedRt,
    _searchQuery,
    combine(_genderFilter, _statusTinggalFilter) { g, s -> Pair(g, s) }
  ) { list, rw, rt, query, (gender, statusTinggal) ->
    list.filter { resident ->
      val matchesRw = if (rw == "Semua RW" || rw.isBlank()) true else resident.rw.equals(rw, ignoreCase = true)
      val matchesRt = if (rt == "Semua RT" || rt.isBlank()) true else resident.rt.equals(rt, ignoreCase = true)
      val matchesQuery = if (query.isBlank()) {
        true
      } else {
        resident.namaLengkap.contains(query, ignoreCase = true) ||
          resident.nik.contains(query) ||
          resident.noKk.contains(query) ||
          resident.alamatLengkap.contains(query, ignoreCase = true) ||
          resident.pekerjaan.contains(query, ignoreCase = true)
      }
      val matchesGender = if (gender.isNullOrBlank()) true else resident.jenisKelamin.equals(gender, ignoreCase = true)
      val matchesStatus = if (statusTinggal.isNullOrBlank()) {
        !resident.statusTinggal.equals("Pindah Keluar", ignoreCase = true)
      } else {
        resident.statusTinggal.equals(statusTinggal, ignoreCase = true)
      }

      matchesRw && matchesRt && matchesQuery && matchesGender && matchesStatus
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Distinct available RTs depending on selected RW
  val availableRtList: StateFlow<List<String>> = _selectedRw.combine(allResidents) { rw, _ ->
    if (rw == "Semua RW" || rw.isBlank()) {
      SampleData.defaultRtList
    } else {
      SampleData.getRtListForRw(rw)
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SampleData.defaultRtList
  )

  suspend fun signInWithGoogle(activityContext: android.content.Context): Result<FirebaseUser> {
    return authManager.signInWithGoogle(activityContext)
  }

  fun loginOperator(
    identifier: String,
    password: String,
    rememberMe: Boolean = true,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      val res = authManager.loginOperator(identifier, password, rememberMe)
      if (res.isSuccess) {
        showToast("Selamat datang, ${res.getOrNull()?.displayName}!")
        onSuccess()
      } else {
        showToast(res.exceptionOrNull()?.localizedMessage ?: "Gagal masuk")
      }
    }
  }

  fun registerOperator(
    name: String,
    emailOrUser: String,
    phone: String,
    rt: String,
    rw: String,
    role: String,
    password: String,
    rememberMe: Boolean = true,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      val res = authManager.registerOperator(name, emailOrUser, phone, rt, rw, role, password, rememberMe)
      if (res.isSuccess) {
        showToast("Pendaftaran berhasil! Akun operator telah tersimpan.")
        onSuccess()
      } else {
        showToast(res.exceptionOrNull()?.localizedMessage ?: "Pendaftaran gagal")
      }
    }
  }

  fun refreshSync() {
    firestoreRepo.refreshSync()
  }

  private fun getActiveOperatorIdentity(): String {
    return currentUser.value?.email
      ?: userProfile.value?.let { "${it.displayName} (${it.role} ${it.rt}/${it.rw})" }
      ?: "Petugas Desa"
  }

  fun signOut() {
    authManager.signOut()
    _currentScreen.value = Screen.Dashboard
  }

  // Migrasi data Room lokal ke Cloud Firestore
  fun migrateLocalDataToFirestore(
    onProgress: (Int, Int) -> Unit,
    onResult: (Result<Int>) -> Unit
  ) {
    val userEmail = getActiveOperatorIdentity()
    viewModelScope.launch {
      val localList = localRepo.getLocalResidentsList()
      if (localList.isEmpty()) {
        onResult(Result.failure(IllegalStateException("Tidak ada data warga di penyimpanan lokal untuk dimigrasikan.")))
        return@launch
      }
      val result = firestoreRepo.migrateFromRoom(localList, userEmail, onProgress)
      if (result.isSuccess) {
        showToast("Sukses migrasi ${result.getOrNull()} data warga ke Cloud Firestore!")
      } else {
        showToast("Migrasi gagal: ${result.exceptionOrNull()?.localizedMessage}")
      }
      onResult(result)
    }
  }

  // Navigation handlers
  fun navigateTo(screen: Screen) {
    if (_currentScreen.value != screen) {
      screenHistory.add(screen)
      _currentScreen.value = screen
    }
  }

  fun navigateBack(): Boolean {
    if (screenHistory.size > 1) {
      screenHistory.removeAt(screenHistory.size - 1)
      _currentScreen.value = screenHistory.last()
      return true
    }
    return false
  }

  fun setSelectedRw(rw: String) {
    _selectedRw.value = rw
    _selectedRt.value = "Semua RT"
  }

  fun setSelectedRt(rt: String) {
    _selectedRt.value = rt
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setGenderFilter(gender: String?) {
    _genderFilter.value = gender
  }

  fun setStatusTinggalFilter(status: String?) {
    _statusTinggalFilter.value = status
  }

  fun clearFilters() {
    _searchQuery.value = ""
    _genderFilter.value = null
    _statusTinggalFilter.value = null
    _selectedRw.value = "Semua RW"
    _selectedRt.value = "Semua RT"
  }

  fun clearToastMessage() {
    _toastMessage.value = null
  }

  fun showToast(msg: String) {
    _toastMessage.value = msg
  }

  // Form handling
  fun initFormForAdd(targetRt: String? = null, targetRw: String? = null) {
    val rw = if (!targetRw.isNullOrBlank() && targetRw != "Semua RW") {
      targetRw
    } else if (_selectedRw.value != "Semua RW") {
      _selectedRw.value
    } else {
      "RW 01"
    }

    val allowedRts = SampleData.getRtListForRw(rw)
    val defaultRt = if (!targetRt.isNullOrBlank() && targetRt != "Semua RT" && allowedRts.contains(targetRt)) {
      targetRt
    } else if (_selectedRt.value != "Semua RT" && allowedRts.contains(_selectedRt.value)) {
      _selectedRt.value
    } else {
      allowedRts.firstOrNull() ?: "RT 01"
    }

    _formState.value = ResidentFormState(
      id = 0,
      rt = defaultRt,
      rw = rw,
      dusun = "Dusun Benda"
    )
    navigateTo(Screen.ResidentForm(null, defaultRt))
  }

  fun initFormForEdit(resident: Resident) {
    _formState.value = ResidentFormState.fromResident(resident)
    navigateTo(Screen.ResidentForm(resident.id, resident.rt))
  }

  fun initFormForAddFamilyMember(parentKk: String, rt: String, rw: String, dusun: String, alamat: String) {
    _formState.value = ResidentFormState(
      id = 0,
      noKk = parentKk,
      rt = rt,
      rw = rw,
      dusun = dusun,
      alamatLengkap = alamat,
      statusKeluarga = "Anak"
    )
    navigateTo(Screen.ResidentForm(null, rt))
  }

  fun updateFormField(transform: (ResidentFormState) -> ResidentFormState) {
    _formState.value = transform(_formState.value)
  }

  fun validateForm(): Boolean {
    val state = _formState.value
    val newErrors = mutableMapOf<String, String>()

    if (state.namaLengkap.isBlank()) {
      newErrors["namaLengkap"] = "Nama lengkap wajib diisi"
    }
    if (state.nik.isBlank()) {
      newErrors["nik"] = "NIK 16 digit wajib diisi"
    } else if (state.nik.length != 16 || !state.nik.all { it.isDigit() }) {
      newErrors["nik"] = "NIK harus tepat 16 angka (saat ini ${state.nik.length} digit)"
    }

    if (state.noKk.isBlank()) {
      newErrors["noKk"] = "No. KK 16 digit wajib diisi"
    } else if (state.noKk.length != 16 || !state.noKk.all { it.isDigit() }) {
      newErrors["noKk"] = "No. Kartu Keluarga harus 16 angka (saat ini ${state.noKk.length} digit)"
    }

    if (state.tempatLahir.isBlank()) {
      newErrors["tempatLahir"] = "Tempat lahir wajib diisi"
    }

    if (state.alamatLengkap.isBlank()) {
      newErrors["alamatLengkap"] = "Alamat rumah / jalan wajib diisi"
    }

    _formState.value = state.copy(errors = newErrors)
    return newErrors.isEmpty()
  }

  fun saveResident(onSuccess: (Long) -> Unit) {
    if (!validateForm()) {
      showToast("Mohon periksa data input yang belum lengkap!")
      return
    }

    val state = _formState.value
    val isNew = state.id == 0L

    // Cek duplikasi NIK jika menambahkan data baru
    if (isNew && allResidents.value.any { it.nik == state.nik }) {
      showToast("NIK ${state.nik} sudah terdaftar dalam sistem!")
      return
    }

    val userEmail = getActiveOperatorIdentity()

    viewModelScope.launch {
      _formState.value = _formState.value.copy(isSaving = true)
      try {
        val resident = _formState.value.toResident()

        // 1. Simpan ke Cloud Firestore (Database Utama Online)
        val cloudResult = firestoreRepo.saveResident(resident, userEmail)
        if (cloudResult.isFailure) {
          throw cloudResult.exceptionOrNull() ?: Exception("Gagal menyimpan ke Cloud Firestore")
        }

        // 2. Simpan juga ke cache lokal Room
        val resultId = if (isNew) {
          localRepo.insertResident(resident)
        } else {
          localRepo.updateResident(resident)
          resident.id
        }

        showToast(if (isNew) "Data warga ${resident.namaLengkap} berhasil disimpan ke Cloud!" else "Data warga berhasil diperbarui di Cloud!")
        _formState.value = _formState.value.copy(isSaving = false)
        onSuccess(resultId)
      } catch (e: Exception) {
        _formState.value = _formState.value.copy(isSaving = false)
        showToast("Gagal menyimpan data: ${e.localizedMessage}")
      }
    }
  }

  fun deleteResident(resident: Resident, onDeleted: () -> Unit) {
    val isAdmin = userProfile.value?.isAdmin == true
    if (!isAdmin) {
      showToast("Akses Ditolak: Hanya Administrator Desa yang berhak menghapus data.")
      return
    }

    val userEmail = getActiveOperatorIdentity()

    viewModelScope.launch {
      try {
        // 1. Hapus dari Cloud Firestore
        val cloudResult = firestoreRepo.deleteResident(resident.nik, userEmail)
        if (cloudResult.isFailure) {
          throw cloudResult.exceptionOrNull() ?: Exception("Gagal menghapus dari Cloud Firestore")
        }

        // 2. Hapus dari cache lokal
        localRepo.deleteResident(resident)
        showToast("Data ${resident.namaLengkap} berhasil dihapus dari Cloud.")
        onDeleted()
      } catch (e: Exception) {
        showToast("Gagal menghapus: ${e.localizedMessage}")
      }
    }
  }

  fun purgeSampleData() {
    viewModelScope.launch {
      val count = localRepo.purgeSampleData()
      showToast(if (count > 0) "$count data penduduk contoh berhasil dibersihkan!" else "Data contoh sudah bersih.")
    }
  }

  fun resetToSampleData() {
    purgeSampleData()
  }

  fun clearAllData() {
    val isAdmin = userProfile.value?.isAdmin == true
    if (!isAdmin) {
      showToast("Akses Ditolak: Hanya Administrator Desa yang berhak mengosongkan data.")
      return
    }
    viewModelScope.launch {
      localRepo.clearAll()
      showToast("Penyimpanan lokal telah dikosongkan.")
    }
  }

  fun exportCsvContent(): String {
    return localRepo.exportToCsv(filteredResidents.value)
  }

  fun exportJsonContent(includePhotos: Boolean = true): String {
    return localRepo.exportToJson(allResidents.value, includePhotos)
  }

  fun parseBackupPreview(jsonStr: String): com.example.data.repository.BackupPreview {
    return localRepo.parseBackupPreview(jsonStr)
  }

  fun importBackup(
    context: android.content.Context,
    jsonStr: String,
    merge: Boolean,
    onResult: (com.example.data.repository.ImportResult) -> Unit
  ) {
    viewModelScope.launch {
      val res = localRepo.importFromJson(context, jsonStr, merge)
      if (res.success) {
        showToast("Impor berhasil: ${res.newInserted} data warga diproses!")
      } else {
        showToast(res.message)
      }
      onResult(res)
    }
  }

  fun updateFormPhotoPenduduk(path: String) {
    updateFormField { it.copy(fotoPenduduk = path) }
  }

  fun updateFormPhotoKtp(path: String) {
    updateFormField { it.copy(fotoKtp = path) }
  }

  fun updateFormPhotoKk(path: String) {
    updateFormField { it.copy(fotoKk = path) }
  }

  fun generateReportText(rt: String): String {
    return localRepo.generateRtSummaryReport(allResidents.value, rt)
  }

  fun recordPindahMasuk(mutation: com.example.data.model.PopulationMutation, onSuccess: () -> Unit) {
    if (mutation.nik.length != 16 || !mutation.nik.all { it.isDigit() }) {
      showToast("NIK harus tepat 16 digit angka!")
      return
    }
    if (mutation.namaLengkap.isBlank()) {
      showToast("Nama lengkap warga wajib diisi!")
      return
    }
    if (mutation.alamatLuar.isBlank()) {
      showToast("Alamat daerah asal warga wajib diisi!")
      return
    }

    viewModelScope.launch {
      try {
        val userEmail = getActiveOperatorIdentity()
        val result = firestoreRepo.recordPindahMasuk(mutation, userEmail)
        if (result.isSuccess) {
          showToast("Berhasil: Warga pindah masuk ${mutation.namaLengkap} otomatis ditambahkan ke ${mutation.rt}!")
          onSuccess()
        } else {
          showToast("Gagal: ${result.exceptionOrNull()?.localizedMessage}")
        }
      } catch (e: Exception) {
        showToast("Terjadi kesalahan: ${e.localizedMessage}")
      }
    }
  }

  fun recordPindahKeluar(
    mutation: com.example.data.model.PopulationMutation,
    removePermanently: Boolean,
    onSuccess: () -> Unit
  ) {
    if (mutation.nik.length != 16) {
      showToast("Pilih data warga yang valid (NIK 16 digit)!")
      return
    }
    if (mutation.alamatLuar.isBlank()) {
      showToast("Alamat tujuan pindah warga wajib diisi!")
      return
    }

    viewModelScope.launch {
      try {
        val userEmail = getActiveOperatorIdentity()
        val result = firestoreRepo.recordPindahKeluar(mutation, removePermanently, userEmail)
        if (result.isSuccess) {
          if (removePermanently) {
            localRepo.deleteById(mutation.nik.toLongOrNull() ?: 0L)
          }
          showToast("Berhasil: Data warga ${mutation.namaLengkap} otomatis dikurangi dari data penduduk aktif!")
          onSuccess()
        } else {
          showToast("Gagal: ${result.exceptionOrNull()?.localizedMessage}")
        }
      } catch (e: Exception) {
        showToast("Terjadi kesalahan: ${e.localizedMessage}")
      }
    }
  }

  fun recordKelahiran(
    event: com.example.data.model.PopulationMutation,
    onSuccess: () -> Unit
  ) {
    if (event.namaLengkap.isBlank()) {
      showToast("Nama lengkap bayi wajib diisi!")
      return
    }
    if (event.noKk.isBlank()) {
      showToast("Nomor KK keluarga bayi wajib diisi!")
      return
    }

    viewModelScope.launch {
      try {
        val userEmail = getActiveOperatorIdentity()
        val result = firestoreRepo.recordKelahiran(event, userEmail)
        if (result.isSuccess) {
          showToast("Berhasil: Data kelahiran ${event.namaLengkap} otomatis menambahkan 1 data warga baru!")
          onSuccess()
        } else {
          showToast("Gagal: ${result.exceptionOrNull()?.localizedMessage}")
        }
      } catch (e: Exception) {
        showToast("Terjadi kesalahan: ${e.localizedMessage}")
      }
    }
  }

  fun recordKematian(
    event: com.example.data.model.PopulationMutation,
    removePermanently: Boolean,
    onSuccess: () -> Unit
  ) {
    if (event.nik.length != 16) {
      showToast("Pilih data warga yang valid (NIK 16 digit)!")
      return
    }

    viewModelScope.launch {
      try {
        val userEmail = getActiveOperatorIdentity()
        val result = firestoreRepo.recordKematian(event, removePermanently, userEmail)
        if (result.isSuccess) {
          if (removePermanently) {
            localRepo.deleteById(event.nik.toLongOrNull() ?: 0L)
          }
          showToast("Berhasil: Data kematian ${event.namaLengkap} otomatis mengurangi data warga aktif!")
          onSuccess()
        } else {
          showToast("Gagal: ${result.exceptionOrNull()?.localizedMessage}")
        }
      } catch (e: Exception) {
        showToast("Terjadi kesalahan: ${e.localizedMessage}")
      }
    }
  }

  fun deleteMutation(mutationId: String) {
    viewModelScope.launch {
      try {
        val userEmail = getActiveOperatorIdentity()
        firestoreRepo.deleteMutation(mutationId, userEmail)
        showToast("Catatan mutasi berhasil dihapus.")
      } catch (e: Exception) {
        showToast("Gagal menghapus mutasi: ${e.localizedMessage}")
      }
    }
  }

  /**
   * Mengubah kata sandi akun Admin. Password bawaan awal adalah Benda15631 dan dapat dirubah kapan saja.
   */
  fun changeAdminPassword(oldPass: String, newPass: String, onSuccess: () -> Unit) {
    if (newPass.length < 5) {
      showToast("Kata sandi baru minimal 5 karakter!")
      return
    }
    viewModelScope.launch {
      val uid = userProfile.value?.uid ?: "admin_benda_official"
      val result = authManager.changePassword(uid, oldPass, newPass)
      if (result.isSuccess) {
        showToast("Kata sandi Admin berhasil diubah!")
        onSuccess()
      } else {
        showToast(result.exceptionOrNull()?.message ?: "Gagal mengubah kata sandi")
      }
    }
  }

  /**
   * Menyimpan / memperbarui data operator RT/RW termasuk nomor WhatsApp.
   */
  fun saveOperator(operator: UserProfile, onSuccess: () -> Unit) {
    if (operator.displayName.isBlank() || operator.phone.isBlank()) {
      showToast("Nama operator dan nomor WhatsApp wajib diisi!")
      return
    }
    viewModelScope.launch {
      val result = firestoreRepo.saveUserProfile(operator)
      if (result.isSuccess) {
        showToast("Operator ${operator.displayName} (${operator.rt}/${operator.rw}) berhasil disimpan!")
        onSuccess()
      } else {
        showToast("Gagal menyimpan operator: ${result.exceptionOrNull()?.message}")
      }
    }
  }

  /**
   * Menghapus akun operator yang terdaftar.
   */
  fun deleteOperator(uid: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
      val result = firestoreRepo.deleteUserProfile(uid)
      if (result.isSuccess) {
        showToast("Operator berhasil dihapus.")
        onSuccess()
      } else {
        showToast("Gagal menghapus operator: ${result.exceptionOrNull()?.message}")
      }
    }
  }

  // ================= WEB PORTAL SERVER & ONLINE INTEGRATION =================
  val isWebServerRunning: StateFlow<Boolean> = com.example.util.WebPortalServer.isServerRunning
  val webServerAddress: StateFlow<String> = com.example.util.WebPortalServer.serverAddress

  /**
   * Mengaktifkan server web lokal agar komputer/laptop dapat mengakses SIDAPENDA Web Online.
   */
  fun startWebServer() {
    val app = getApplication<Application>()
    com.example.util.WebPortalServer.residentDataProvider = { allResidents.value }
    com.example.util.WebPortalServer.startServer(app)
    val addr = com.example.util.WebPortalServer.serverAddress.value
    showToast(if (addr.isNotBlank()) "Web Server aktif di $addr" else "Menyalakan server web...")
  }

  /**
   * Menonaktifkan server web lokal.
   */
  fun stopWebServer() {
    com.example.util.WebPortalServer.stopServer()
    showToast("Server Web Portal dinonaktifkan")
  }

  /**
   * Mengalihkan status aktif/nonaktif server web lokal.
   */
  fun toggleWebServer() {
    if (com.example.util.WebPortalServer.isServerRunning.value) {
      stopWebServer()
    } else {
      startWebServer()
    }
  }

  /**
   * Menyiapkan file HTML mandiri SIDAPENDA Web Portal untuk dibagikan/disimpan.
   */
  fun exportWebPortalHtml(): java.io.File? {
    return try {
      val app = getApplication<Application>()
      val html = com.example.util.WebPortalServer.loadWebPortalHtml(app)
      val outFile = java.io.File(app.cacheDir, "dapenda_web_portal.html")
      outFile.writeText(html, Charsets.UTF_8)
      outFile
    } catch (e: Exception) {
      null
    }
  }
}

