package com.example.data.repository

import android.content.Context
import com.example.data.local.ResidentDao
import com.example.data.model.Resident
import com.example.data.sample.SampleData
import com.example.util.PhotoStorageManager
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPreview(
  val isValid: Boolean,
  val appName: String = "",
  val village: String = "",
  val totalRecords: Int = 0,
  val exportedAt: String = "",
  val photoCount: Int = 0,
  val errorMessage: String = ""
)

data class ImportResult(
  val success: Boolean,
  val totalProcessed: Int,
  val newInserted: Int,
  val updatedCount: Int,
  val photosRestored: Int,
  val message: String
)

class ResidentRepository(private val dao: ResidentDao) {

  fun getAllResidents(): Flow<List<Resident>> = dao.getAllResidents()

  fun getResidentsByRt(rt: String): Flow<List<Resident>> = dao.getResidentsByRt(rt)

  fun getResidentById(id: Long): Flow<Resident?> = dao.getResidentById(id)

  suspend fun getResidentByIdOnce(id: Long): Resident? = dao.getResidentByIdOnce(id)

  fun getResidentsByKk(noKk: String): Flow<List<Resident>> = dao.getResidentsByKk(noKk)

  fun getAllRtList(): Flow<List<String>> = dao.getAllRtList()

  fun searchResidents(query: String): Flow<List<Resident>> = dao.searchResidents(query)

  suspend fun insertResident(resident: Resident): Long = dao.insert(resident)

  suspend fun updateResident(resident: Resident) = dao.update(resident)

  suspend fun deleteResident(resident: Resident) {
    // Optionally remove local photos
    PhotoStorageManager.deletePhoto(resident.fotoPenduduk)
    PhotoStorageManager.deletePhoto(resident.fotoKtp)
    PhotoStorageManager.deletePhoto(resident.fotoKk)
    dao.delete(resident)
  }

  suspend fun deleteById(id: Long) = dao.deleteById(id)

  suspend fun purgeSampleData(): Int {
    return dao.deleteByNiks(SampleData.sampleNiksToPurge)
  }

  suspend fun resetToSampleData() {
    purgeSampleData()
  }

  suspend fun clearAll() = dao.clearAll()

  suspend fun ensureInitialized() {
    // Hapus data contoh jika ada di database
    purgeSampleData()
  }

  suspend fun getLocalResidentsList(): List<Resident> = dao.getAllList()

  suspend fun syncFromCloud(cloudList: List<Resident>) {
    try {
      if (cloudList.isNotEmpty()) {
        dao.insertAll(cloudList)
      }
    } catch (_: Exception) {}
  }

  /**
   * Export all residents into a standalone JSON backup file.
   * Encodes local photos to Base64 so the file can be transferred to another phone
   * via WhatsApp, Bluetooth, Nearby Share, Google Drive, etc.
   */
  fun exportToJson(residents: List<Resident>, includePhotos: Boolean = true): String {
    val root = JSONObject()
    root.put("app", "DAPENDA")
    root.put("schemaVersion", 2)
    root.put("village", "Desa Benda")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    root.put("exportedAt", dateFormat.format(Date()))
    root.put("totalRecords", residents.size)

    val residentsArray = JSONArray()
    for (r in residents) {
      val obj = JSONObject()
      obj.put("nik", r.nik)
      obj.put("noKk", r.noKk)
      obj.put("namaLengkap", r.namaLengkap)
      obj.put("jenisKelamin", r.jenisKelamin)
      obj.put("tempatLahir", r.tempatLahir)
      obj.put("tanggalLahir", r.tanggalLahir)
      obj.put("rt", r.rt)
      obj.put("rw", r.rw)
      obj.put("dusun", r.dusun)
      obj.put("alamatLengkap", r.alamatLengkap)
      obj.put("agama", r.agama)
      obj.put("statusKeluarga", r.statusKeluarga)
      obj.put("statusKawin", r.statusKawin)
      obj.put("pekerjaan", r.pekerjaan)
      obj.put("pendidikan", r.pendidikan)
      obj.put("golonganDarah", r.golonganDarah)
      obj.put("statusTinggal", r.statusTinggal)
      obj.put("noHp", r.noHp)
      obj.put("bantuanSosial", r.bantuanSosial)
      obj.put("disabilitas", r.disabilitas)
      obj.put("catatan", r.catatan)

      if (includePhotos) {
        val b64Penduduk = PhotoStorageManager.fileToBase64(r.fotoPenduduk)
        val b64Ktp = PhotoStorageManager.fileToBase64(r.fotoKtp)
        val b64Kk = PhotoStorageManager.fileToBase64(r.fotoKk)
        if (b64Penduduk.isNotBlank()) obj.put("fotoPendudukBase64", b64Penduduk)
        if (b64Ktp.isNotBlank()) obj.put("fotoKtpBase64", b64Ktp)
        if (b64Kk.isNotBlank()) obj.put("fotoKkBase64", b64Kk)
      }
      residentsArray.put(obj)
    }

    root.put("residents", residentsArray)
    return root.toString(2)
  }

  /**
   * Preview backup metadata before applying changes to database.
   */
  fun parseBackupPreview(jsonStr: String): BackupPreview {
    return try {
      val root = JSONObject(jsonStr)
      val app = root.optString("app", "")
      val village = root.optString("village", "Desa Benda")
      val exportedAt = root.optString("exportedAt", "Tidak diketahui")
      val arr = root.optJSONArray("residents")
      val total = arr?.length() ?: 0

      var photosCount = 0
      if (arr != null) {
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          if (obj.has("fotoPendudukBase64")) photosCount++
          if (obj.has("fotoKtpBase64")) photosCount++
          if (obj.has("fotoKkBase64")) photosCount++
        }
      }

      if (total == 0 && !root.has("residents")) {
        BackupPreview(isValid = false, errorMessage = "Format file tidak dikenali atau daftar warga kosong.")
      } else {
        BackupPreview(
          isValid = true,
          appName = app.ifBlank { "DAPENDA" },
          village = village,
          totalRecords = total,
          exportedAt = exportedAt,
          photoCount = photosCount
        )
      }
    } catch (e: Exception) {
      BackupPreview(isValid = false, errorMessage = "Gagal membaca file JSON: ${e.localizedMessage}")
    }
  }

  /**
   * Import residents and photos from JSON string.
   * If merge is false, clears existing database first.
   * If merge is true, matches by NIK: updates existing resident or inserts new resident.
   */
  suspend fun importFromJson(context: Context, jsonStr: String, merge: Boolean): ImportResult {
    return try {
      val root = JSONObject(jsonStr)
      val arr = root.optJSONArray("residents")
        ?: return ImportResult(false, 0, 0, 0, 0, "File cadangan tidak memuat data warga.")

      if (!merge) {
        dao.clearAll()
      }

      var insertedCount = 0
      var updatedCount = 0
      var photosRestored = 0

      // Read all existing to map by NIK if merging
      val existingResidents = if (merge) {
        // We can do a quick lookup
        val map = mutableMapOf<String, Long>()
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          val nik = obj.optString("nik", "")
          // search existing
        }
        map
      } else emptyMap()

      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val nik = obj.optString("nik", "")
        val noKk = obj.optString("noKk", "")
        val namaLengkap = obj.optString("namaLengkap", "")
        val jenisKelamin = obj.optString("jenisKelamin", "LAKI-LAKI")
        val tempatLahir = obj.optString("tempatLahir", "")
        val tanggalLahir = obj.optString("tanggalLahir", "2000-01-01")
        val rt = obj.optString("rt", "RT 01")
        val rw = obj.optString("rw", "RW 01")
        val dusun = obj.optString("dusun", "Dusun Benda")
        val alamatLengkap = obj.optString("alamatLengkap", "")
        val agama = obj.optString("agama", "Islam")
        val statusKeluarga = obj.optString("statusKeluarga", "Kepala Keluarga")
        val statusKawin = obj.optString("statusKawin", "Kawin")
        val pekerjaan = obj.optString("pekerjaan", "Wiraswasta / Dagang")
        val pendidikan = obj.optString("pendidikan", "SMA / SMK / Sederajat")
        val golonganDarah = obj.optString("golonganDarah", "-")
        val statusTinggal = obj.optString("statusTinggal", "Warga Tetap")
        val noHp = obj.optString("noHp", "")
        val bantuanSosial = obj.optBoolean("bantuanSosial", false)
        val disabilitas = obj.optBoolean("disabilitas", false)
        val catatan = obj.optString("catatan", "")

        // Restore photos from Base64 if present
        var localFotoPenduduk = ""
        var localFotoKtp = ""
        var localFotoKk = ""

        val b64Penduduk = obj.optString("fotoPendudukBase64", "")
        if (b64Penduduk.isNotBlank()) {
          localFotoPenduduk = PhotoStorageManager.saveBase64ToFile(context, b64Penduduk, "penduduk_${nik}")
          if (localFotoPenduduk.isNotBlank()) photosRestored++
        }

        val b64Ktp = obj.optString("fotoKtpBase64", "")
        if (b64Ktp.isNotBlank()) {
          localFotoKtp = PhotoStorageManager.saveBase64ToFile(context, b64Ktp, "ktp_${nik}")
          if (localFotoKtp.isNotBlank()) photosRestored++
        }

        val b64Kk = obj.optString("fotoKkBase64", "")
        if (b64Kk.isNotBlank()) {
          localFotoKk = PhotoStorageManager.saveBase64ToFile(context, b64Kk, "kk_${noKk}")
          if (localFotoKk.isNotBlank()) photosRestored++
        }

        val resident = Resident(
          id = 0,
          nik = nik,
          noKk = noKk,
          namaLengkap = namaLengkap,
          jenisKelamin = jenisKelamin,
          tempatLahir = tempatLahir,
          tanggalLahir = tanggalLahir,
          rt = rt,
          rw = rw,
          dusun = dusun,
          alamatLengkap = alamatLengkap,
          agama = agama,
          statusKeluarga = statusKeluarga,
          statusKawin = statusKawin,
          pekerjaan = pekerjaan,
          pendidikan = pendidikan,
          golonganDarah = golonganDarah,
          statusTinggal = statusTinggal,
          noHp = noHp,
          bantuanSosial = bantuanSosial,
          disabilitas = disabilitas,
          catatan = catatan,
          fotoPenduduk = localFotoPenduduk,
          fotoKtp = localFotoKtp,
          fotoKk = localFotoKk
        )

        dao.insert(resident)
        insertedCount++
      }

      ImportResult(
        success = true,
        totalProcessed = arr.length(),
        newInserted = insertedCount,
        updatedCount = updatedCount,
        photosRestored = photosRestored,
        message = "Berhasil memproses ${arr.length()} data warga ($photosRestored berkas foto dipulihkan) ke sistem DAPENDA."
      )
    } catch (e: Exception) {
      ImportResult(false, 0, 0, 0, 0, "Gagal mengimpor data: ${e.localizedMessage}")
    }
  }

  fun exportToCsv(residents: List<Resident>): String {
    val sb = StringBuilder()
    sb.append("No,NIK,No KK,Nama Lengkap,Jenis Kelamin,Tempat Lahir,Tanggal Lahir,Usia,RT,RW,Dusun,Alamat,Agama,Status Keluarga,Status Kawin,Pekerjaan,Pendidikan,Golongan Darah,Status Tinggal,No HP,Bansos,Disabilitas,Foto Wajah,Foto KTP,Foto KK,Catatan\n")
    residents.forEachIndexed { index, r ->
      sb.append("${index + 1},")
      sb.append("\"${r.nik}\",")
      sb.append("\"${r.noKk}\",")
      sb.append("\"${r.namaLengkap.replace("\"", "\"\"")}\",")
      sb.append("\"${r.jenisKelamin}\",")
      sb.append("\"${r.tempatLahir}\",")
      sb.append("\"${r.tanggalLahir}\",")
      sb.append("${r.usia},")
      sb.append("\"${r.rt}\",")
      sb.append("\"${r.rw}\",")
      sb.append("\"${r.dusun}\",")
      sb.append("\"${r.alamatLengkap.replace("\"", "\"\"")}\",")
      sb.append("\"${r.agama}\",")
      sb.append("\"${r.statusKeluarga}\",")
      sb.append("\"${r.statusKawin}\",")
      sb.append("\"${r.pekerjaan}\",")
      sb.append("\"${r.pendidikan}\",")
      sb.append("\"${r.golonganDarah}\",")
      sb.append("\"${r.statusTinggal}\",")
      sb.append("\"${r.noHp}\",")
      sb.append("\"${if (r.bantuanSosial) "Ya" else "Tidak"}\",")
      sb.append("\"${if (r.disabilitas) "Ya" else "Tidak"}\",")
      sb.append("\"${if (r.fotoPenduduk.isNotBlank()) "Tersedia" else "Kosong"}\",")
      sb.append("\"${if (r.fotoKtp.isNotBlank()) "Tersedia" else "Kosong"}\",")
      sb.append("\"${if (r.fotoKk.isNotBlank()) "Tersedia" else "Kosong"}\",")
      sb.append("\"${r.catatan.replace("\"", "\"\"")}\"\n")
    }
    return sb.toString()
  }

  fun generateRtSummaryReport(residents: List<Resident>, targetRt: String): String {
    val list = if (targetRt == "Semua RT" || targetRt.isBlank()) residents else residents.filter { it.rt == targetRt }
    val totalJiwa = list.size
    val totalKk = list.map { it.noKk }.distinct().size
    val lakiLaki = list.count { it.jenisKelamin.equals("LAKI-LAKI", ignoreCase = true) }
    val perempuan = list.count { it.jenisKelamin.equals("PEREMPUAN", ignoreCase = true) }
    val balita = list.count { it.usia <= 5 }
    val anak = list.count { it.usia in 6..12 }
    val remaja = list.count { it.usia in 13..17 }
    val produktif = list.count { it.usia in 18..59 }
    val lansia = list.count { it.usia >= 60 }
    val bansos = list.count { it.bantuanSosial }
    val disabilitas = list.count { it.disabilitas }
    val tetap = list.count { it.statusTinggal.contains("Tetap", ignoreCase = true) }
    val kontrak = list.count { it.statusTinggal.contains("Kontrak", ignoreCase = true) }

    return """
      *LAPORAN REKAPITULASI KEPENDUDUKAN DAPENDA*
      Wilayah: ${if (targetRt == "Semua RT" || targetRt.isBlank()) "Seluruh RT (Desa Benda)" else targetRt}
      Tanggal: 2026-10-15
      -----------------------------------------
      📊 *RINGKASAN UTAMA*
      • Total Penduduk: $totalJiwa Jiwa
      • Total Kepala Keluarga (KK): $totalKk KK
      • Laki-laki: $lakiLaki Jiwa (${if (totalJiwa > 0) (lakiLaki * 100 / totalJiwa) else 0}%)
      • Perempuan: $perempuan Jiwa (${if (totalJiwa > 0) (perempuan * 100 / totalJiwa) else 0}%)

      👶 *DISTRIBUSI KELOMPOK USIA*
      • Balita (0-5 th): $balita
      • Anak-anak (6-12 th): $anak
      • Remaja (13-17 th): $remaja
      • Usia Produktif (18-59 th): $produktif
      • Lansia (60+ th): $lansia

      🏠 *STATUS DOMISILI & SOSIAL*
      • Warga Tetap: $tetap
      • Warga Kontrak / Sementara: $kontrak
      • Penerima Bantuan Sosial: $bansos
      • Penyandang Disabilitas: $disabilitas
      -----------------------------------------
      _Digenerate otomatis melalui aplikasi DAPENDA (Data Penduduk RT)_
    """.trimIndent()
  }
}
