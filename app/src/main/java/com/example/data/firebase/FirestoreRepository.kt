package com.example.data.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.R
import com.example.data.model.PopulationMutation
import com.example.data.model.Resident
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class SyncInfo(
  val statusText: String = "Menghubungkan ke Cloud...",
  val isOffline: Boolean = false,
  val hasPendingWrites: Boolean = false
)

class FirestoreRepository(
  private val context: Context,
  private val db: FirebaseFirestore
) {

  constructor(context: Context) : this(
    context.applicationContext,
    createFirestore(context)
  )

  companion object {
    private fun createFirestore(context: Context): FirebaseFirestore {
      return try {
        val dbId = context.applicationContext.getString(R.string.firestore_database_id).trim()
        if (dbId.isNotBlank() && dbId != "(default)") {
          FirebaseFirestore.getInstance(dbId)
        } else {
          FirebaseFirestore.getInstance()
        }
      } catch (e: Exception) {
        android.util.Log.w("FirestoreRepo", "Fallback ke database default: ${e.message}")
        FirebaseFirestore.getInstance()
      }.apply {
        try {
          firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
        } catch (_: Exception) {
          // Abaikan jika sudah diatur
        }
      }
    }
  }

  private val residentsCollection = db.collection("residents")
  private val usersCollection = db.collection("users")
  private val auditCollection = db.collection("audit_logs")
  private val mutationsCollection = db.collection("mutations")

  private val _syncInfo = MutableStateFlow(SyncInfo("Menghubungkan ke Cloud...", false, false))
  val syncInfo: StateFlow<SyncInfo> = _syncInfo.asStateFlow()

  /**
   * Cek ketersediaan koneksi internet aktif di perangkat.
   */
  fun isNetworkConnected(): Boolean {
    return try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
      val network = cm.activeNetwork ?: return false
      val capabilities = cm.getNetworkCapabilities(network) ?: return false
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (_: Exception) {
      true
    }
  }

  /**
   * Mengamati pembaruan data kependudukan secara real-time dari Cloud Firestore.
   * Menggunakan MetadataChanges.INCLUDE agar mendeteksi pembaruan server secara instan.
   */
  fun observeResidents(): Flow<List<Resident>> = callbackFlow {
    val listener = residentsCollection
      .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
        if (error != null) {
          android.util.Log.w("FirestoreRepo", "Peringatan listener Firestore: ${error.localizedMessage}")
          val isOnline = isNetworkConnected()
          _syncInfo.value = SyncInfo(
            statusText = if (isOnline) "Online (Sinkronisasi Cloud)" else "Offline (Cache Lokal)",
            isOffline = !isOnline,
            hasPendingWrites = false
          )
          return@addSnapshotListener
        }
        if (snapshot != null) {
          val isFromCache = snapshot.metadata.isFromCache
          val pendingWrites = snapshot.metadata.hasPendingWrites()
          val isOnline = isNetworkConnected()

          // Jika perangkat memiliki internet aktif, status tetap Online meskipun snapshot pertama memuat cache
          val isOffline = !isOnline && isFromCache
          val status = when {
            pendingWrites -> "Sedang menyinkronkan data..."
            !isFromCache -> "Online (Cloud Firestore Live)"
            isOnline -> "Online (Sinkronisasi Cloud)"
            else -> "Offline (Menggunakan Cache Lokal)"
          }
          _syncInfo.value = SyncInfo(
            statusText = status,
            isOffline = isOffline,
            hasPendingWrites = pendingWrites
          )

          val list = snapshot.documents.mapNotNull { doc ->
            doc.toObject(FirestoreResident::class.java)?.toResident(context)
          }.sortedBy { it.namaLengkap }

          trySend(list)
        }
      }
    awaitClose { listener.remove() }
  }

  fun refreshSync() {
    val isOnline = isNetworkConnected()
    _syncInfo.value = SyncInfo(
      statusText = if (isOnline) "Online (Cloud Firestore Live)" else "Offline (Cache Lokal)",
      isOffline = !isOnline,
      hasPendingWrites = false
    )
  }

  /**
   * Menyimpan atau memperbarui data warga di Cloud Firestore.
   * Menggunakan NIK sebagai Document ID untuk mencegah duplikasi data.
   */
  suspend fun saveResident(resident: Resident, userEmail: String): Result<Unit> {
    return try {
      require(resident.nik.length == 16) { "NIK harus terdiri dari 16 digit!" }
      val docRef = residentsCollection.document(resident.nik)
      val existing = docRef.get().await()
      val isNew = !existing.exists()

      var firestoreDoc = FirestoreResident.fromResident(resident, userEmail)
      if (!isNew) {
        val oldDoc = existing.toObject(FirestoreResident::class.java)
        if (oldDoc != null) {
          firestoreDoc = firestoreDoc.copy(
            fotoPendudukBase64 = if (firestoreDoc.fotoPendudukBase64.isNotBlank()) firestoreDoc.fotoPendudukBase64 else oldDoc.fotoPendudukBase64,
            fotoKtpBase64 = if (firestoreDoc.fotoKtpBase64.isNotBlank()) firestoreDoc.fotoKtpBase64 else oldDoc.fotoKtpBase64,
            fotoKkBase64 = if (firestoreDoc.fotoKkBase64.isNotBlank()) firestoreDoc.fotoKkBase64 else oldDoc.fotoKkBase64
          )
        }
      }

      docRef.set(firestoreDoc).await()

      // Catat log aktivitas
      logActivity(
        action = if (isNew) "TAMBAH" else "UBAH",
        entityId = resident.nik,
        performedBy = userEmail,
        details = "${if (isNew) "Menambahkan" else "Memperbarui"} data warga: ${resident.namaLengkap} (NIK: ${resident.nik}, RT: ${resident.rt}/RW: ${resident.rw})"
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Mengunggah foto lokal yang belum tersinkronisasi ke Cloud Firestore agar tampil di akun Admin.
   */
  suspend fun uploadLocalPhotosIfMissing(localList: List<Resident>, userEmail: String) {
    try {
      for (r in localList) {
        val hasLocalPhotos = (r.fotoPenduduk.isNotBlank() && java.io.File(r.fotoPenduduk).exists()) ||
                             (r.fotoKtp.isNotBlank() && java.io.File(r.fotoKtp).exists()) ||
                             (r.fotoKk.isNotBlank() && java.io.File(r.fotoKk).exists())
        if (hasLocalPhotos) {
          val docRef = residentsCollection.document(r.nik)
          val snap = docRef.get().await()
          if (snap.exists()) {
            val cloud = snap.toObject(FirestoreResident::class.java)
            if (cloud != null && (cloud.fotoKtpBase64.isBlank() || cloud.fotoPendudukBase64.isBlank() || cloud.fotoKkBase64.isBlank())) {
              val b64Penduduk = if (cloud.fotoPendudukBase64.isBlank()) com.example.util.PhotoStorageManager.fileToCompressedBase64(r.fotoPenduduk, 500, 70) else cloud.fotoPendudukBase64
              val b64Ktp = if (cloud.fotoKtpBase64.isBlank()) com.example.util.PhotoStorageManager.fileToCompressedBase64(r.fotoKtp, 800, 65) else cloud.fotoKtpBase64
              val b64Kk = if (cloud.fotoKkBase64.isBlank()) com.example.util.PhotoStorageManager.fileToCompressedBase64(r.fotoKk, 800, 65) else cloud.fotoKkBase64

              docRef.update(
                mapOf(
                  "fotoPendudukBase64" to b64Penduduk,
                  "fotoKtpBase64" to b64Ktp,
                  "fotoKkBase64" to b64Kk,
                  "updatedAt" to System.currentTimeMillis()
                )
              ).await()
            }
          }
        }
      }
    } catch (e: Exception) {
      android.util.Log.w("FirestoreRepo", "Gagal sinkronisasi foto lokal ke cloud", e)
    }
  }

  /**
   * Menghapus data penduduk di Cloud Firestore (Hak Akses Administrator).
   */
  suspend fun deleteResident(nik: String, userEmail: String): Result<Unit> {
    return try {
      residentsCollection.document(nik).delete().await()
      logActivity(
        action = "HAPUS",
        entityId = nik,
        performedBy = userEmail,
        details = "Menghapus data warga dengan NIK: $nik"
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Migrasi seluruh data warga dari basis data lokal (Room SQLite) ke Cloud Firestore.
   */
  suspend fun migrateFromRoom(
    localResidents: List<Resident>,
    userEmail: String,
    onProgress: (Int, Int) -> Unit
  ): Result<Int> {
    return try {
      var count = 0
      val total = localResidents.size
      for (chunk in localResidents.chunked(200)) {
        val batch = db.batch()
        for (r in chunk) {
          val docRef = residentsCollection.document(r.nik)
          val firestoreDoc = FirestoreResident.fromResident(r, userEmail)
          batch.set(docRef, firestoreDoc)
          count++
        }
        batch.commit().await()
        onProgress(count, total)
      }

      logActivity(
        action = "MIGRASI_LOKAL",
        entityId = "Semua RT/RW",
        performedBy = userEmail,
        details = "Migrasi $count data penduduk dari memori lokal HP ke Cloud Firestore"
      )
      Result.success(count)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Mengamati profil dan peran pengguna saat ini (Admin atau Operator).
   */
  fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
    val listener = usersCollection.document(uid).addSnapshotListener { snapshot, _ ->
      val profile = snapshot?.toObject(UserProfile::class.java)
      trySend(profile)
    }
    awaitClose { listener.remove() }
  }

  /**
   * Mengamati semua data akun operator yang terdaftar secara real-time.
   */
  fun observeAllUsers(): Flow<List<UserProfile>> = callbackFlow {
    val listener = usersCollection.addSnapshotListener { snapshot, error ->
      if (error != null) {
        android.util.Log.w("FirestoreRepo", "Error observeAllUsers: ${error.message}")
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val list = snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
        trySend(list)
      }
    }
    awaitClose { listener.remove() }
  }

  suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
    return try {
      usersCollection.document(profile.uid).set(profile).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteUserProfile(uid: String): Result<Unit> {
    return try {
      usersCollection.document(uid).delete().await()
      logActivity(
        action = "Hapus Operator",
        entityId = uid,
        performedBy = "admin",
        details = "Menghapus akun operator dengan ID: $uid"
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateUserPassword(uid: String, newPasswordHash: String): Result<Unit> {
    return try {
      usersCollection.document(uid).update("passwordHash", newPasswordHash).await()
      logActivity(
        action = "Ubah Password",
        entityId = uid,
        performedBy = uid,
        details = "Memperbarui kata sandi akun"
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun getAdminProfile(): UserProfile? {
    return try {
      val doc = usersCollection.document("admin_benda_official").get().await()
      if (doc.exists()) doc.toObject(UserProfile::class.java) else null
    } catch (_: Exception) {
      null
    }
  }

  suspend fun seedDefaultOperatorsIfEmpty(): Result<Unit> {
    return try {
      val snapshot = usersCollection.get().await()
      // Jika belum ada operator yang terdaftar selain admin, daftarkan operator bawaan
      val existingOperators = snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
        .filter { !it.isAdmin }

      if (existingOperators.isEmpty()) {
        val defaults = com.example.data.sample.SampleData.getDefaultRegisteredOperators()
        for (op in defaults) {
          usersCollection.document(op.uid).set(op).await()
        }
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun findUserByIdentifier(query: String): UserProfile? {
    return try {
      val trimmed = query.trim().lowercase()
      val snapshot = usersCollection.get().await()
      for (doc in snapshot.documents) {
        val user = doc.toObject(UserProfile::class.java)
        if (user != null) {
          if (user.email.equals(trimmed, ignoreCase = true) ||
              user.phone == query.trim() ||
              user.displayName.equals(query.trim(), ignoreCase = true) ||
              user.uid.equals(trimmed, ignoreCase = true)) {
            return user
          }
        }
      }
      null
    } catch (e: Exception) {
      android.util.Log.w("FirestoreRepo", "Gagal mencari user di cloud: ${e.message}")
      null
    }
  }

  /**
   * Mengamati catatan riwayat audit perubahan data.
   */
  fun observeAuditLogs(): Flow<List<AuditLog>> = callbackFlow {
    val listener = auditCollection
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .limit(60)
      .addSnapshotListener { snapshot, _ ->
        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { it.toObject(AuditLog::class.java) }
          trySend(list)
        }
      }
    awaitClose { listener.remove() }
  }

  /**
   * Mengamati riwayat data mutasi penduduk (Pindah Masuk & Pindah Keluar) secara real-time.
   */
  fun observeMutations(): Flow<List<PopulationMutation>> = callbackFlow {
    val listener = mutationsCollection
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .limit(100)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          android.util.Log.w("FirestoreRepo", "Error observeMutations: ${error.message}")
          return@addSnapshotListener
        }
        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { it.toObject(PopulationMutation::class.java) }
          trySend(list)
        }
      }
    awaitClose { listener.remove() }
  }

  /**
   * Mencatat mutasi PINDAH MASUK dan secara OTOMATIS MENAMBAH data penduduk ke Cloud Firestore.
   */
  suspend fun recordPindahMasuk(mutation: PopulationMutation, userEmail: String): Result<Unit> {
    return try {
      require(mutation.nik.length == 16) { "NIK harus tepat 16 digit!" }
      require(mutation.namaLengkap.isNotBlank()) { "Nama lengkap warga wajib diisi!" }

      // 1. Simpan dokumen mutasi
      val mutDocRef = if (mutation.id.isNotBlank()) mutationsCollection.document(mutation.id) else mutationsCollection.document()
      val finalMutation = mutation.copy(
        id = mutDocRef.id,
        type = PopulationMutation.TYPE_PINDAH_MASUK,
        petugas = userEmail,
        createdAt = System.currentTimeMillis()
      )
      mutDocRef.set(finalMutation).await()

      // 2. OTOMATIS MENAMBAH data penduduk ke data penduduk aktif (residents)
      val newResident = Resident(
        id = mutation.nik.toLongOrNull() ?: System.currentTimeMillis(),
        nik = mutation.nik.trim(),
        noKk = mutation.noKk.trim(),
        namaLengkap = mutation.namaLengkap.trim(),
        jenisKelamin = mutation.jenisKelamin,
        tempatLahir = mutation.tempatLahir.trim(),
        tanggalLahir = mutation.tanggalLahir.trim(),
        rt = mutation.rt.trim(),
        rw = mutation.rw.trim(),
        dusun = mutation.dusun.trim(),
        alamatLengkap = mutation.alamatLokal.trim().ifBlank { "Desa Benda, ${mutation.rt}/${mutation.rw}" },
        agama = "Islam",
        statusKeluarga = mutation.statusKeluarga,
        statusKawin = "Kawin",
        pekerjaan = "Wiraswasta / Dagang",
        pendidikan = "SMA / SMK / Sederajat",
        golonganDarah = "-",
        statusTinggal = "Warga Tetap",
        noHp = mutation.noHp.trim(),
        catatan = "Pindah Masuk dari: ${mutation.alamatLuar.trim()} (Tgl: ${mutation.tanggalMutasi}). Alasan: ${mutation.alasanPindah}. ${mutation.catatan}".trim(),
        createdAt = System.currentTimeMillis()
      )
      saveResident(newResident, userEmail)

      // 3. Catat audit log
      logActivity(
        action = "MUTASI_PINDAH_MASUK",
        entityId = mutation.nik,
        performedBy = userEmail,
        details = "Input Warga Pindah Masuk: ${mutation.namaLengkap} (NIK: ${mutation.nik}) asal ${mutation.alamatLuar} ke ${mutation.rt}/${mutation.rw}. Otomatis menambah 1 warga."
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Mencatat mutasi PINDAH KELUAR dan secara OTOMATIS MENGURANGI data penduduk dari data penduduk aktif.
   */
  suspend fun recordPindahKeluar(
    mutation: PopulationMutation,
    removePermanently: Boolean,
    userEmail: String
  ): Result<Unit> {
    return try {
      require(mutation.nik.length == 16) { "NIK harus tepat 16 digit!" }

      // 1. Simpan dokumen mutasi
      val mutDocRef = if (mutation.id.isNotBlank()) mutationsCollection.document(mutation.id) else mutationsCollection.document()
      val finalMutation = mutation.copy(
        id = mutDocRef.id,
        type = PopulationMutation.TYPE_PINDAH_KELUAR,
        petugas = userEmail,
        createdAt = System.currentTimeMillis()
      )
      mutDocRef.set(finalMutation).await()

      // 2. OTOMATIS MENGURANGI dari data kependudukan aktif
      val resDocRef = residentsCollection.document(mutation.nik)
      if (removePermanently) {
        // Hapus langsung dari collection residents
        resDocRef.delete().await()
      } else {
        // Tandai statusTinggal = "Pindah Keluar" sehingga otomatis terkurangi dari hitungan warga aktif
        resDocRef.update(
          mapOf(
            "statusTinggal" to "Pindah Keluar",
            "catatan" to "Pindah Keluar ke: ${mutation.alamatLuar.trim()} (Tgl: ${mutation.tanggalMutasi}). Alasan: ${mutation.alasanPindah}. ${mutation.catatan}".trim(),
            "updatedAt" to System.currentTimeMillis(),
            "updatedBy" to userEmail
          )
        ).await()
      }

      // 3. Catat audit log
      logActivity(
        action = "MUTASI_PINDAH_KELUAR",
        entityId = mutation.nik,
        performedBy = userEmail,
        details = "Input Warga Pindah Keluar: ${mutation.namaLengkap} (NIK: ${mutation.nik}) tujuan ${mutation.alamatLuar}. Otomatis mengurangi 1 warga dari ${mutation.rt}."
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Menghapus catatan riwayat mutasi
   */
  suspend fun deleteMutation(mutationId: String, userEmail: String): Result<Unit> {
    return try {
      mutationsCollection.document(mutationId).delete().await()
      logActivity(
        action = "HAPUS_MUTASI",
        entityId = mutationId,
        performedBy = userEmail,
        details = "Menghapus catatan mutasi kependudukan ID: $mutationId"
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private suspend fun logActivity(
    action: String,
    entityId: String,
    performedBy: String,
    details: String
  ) {
    try {
      val log = AuditLog(
        action = action,
        entity = "Resident",
        entityId = entityId,
        performedBy = performedBy,
        timestamp = System.currentTimeMillis(),
        details = details
      )
      auditCollection.add(log).await()
    } catch (_: Exception) {
      // Abaikan kegagalan log jika sedang offline
    }
  }
}
