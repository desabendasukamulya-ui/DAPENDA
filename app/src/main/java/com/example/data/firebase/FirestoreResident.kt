package com.example.data.firebase

import android.content.Context
import com.example.data.model.Resident
import com.example.util.PhotoStorageManager
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.File

@IgnoreExtraProperties
data class FirestoreResident(
  @DocumentId val id: String = "",
  val nik: String = "",
  val noKk: String = "",
  val namaLengkap: String = "",
  val jenisKelamin: String = "",
  val tempatLahir: String = "",
  val tanggalLahir: String = "",
  val rt: String = "",
  val rw: String = "",
  val dusun: String = "",
  val alamatLengkap: String = "",
  val agama: String = "",
  val statusKeluarga: String = "",
  val statusKawin: String = "",
  val pekerjaan: String = "",
  val pendidikan: String = "",
  val golonganDarah: String = "",
  val statusTinggal: String = "",
  val noHp: String = "",
  val bantuanSosial: Boolean = false,
  val disabilitas: Boolean = false,
  val catatan: String = "",
  val fotoPenduduk: String = "",
  val fotoKtp: String = "",
  val fotoKk: String = "",
  val fotoPendudukBase64: String = "",
  val fotoKtpBase64: String = "",
  val fotoKkBase64: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val updatedBy: String = ""
) {
  fun toResident(context: Context? = null): Resident {
    val resolvedPenduduk = resolvePhoto(context, fotoPendudukBase64, fotoPenduduk, "penduduk", nik)
    val resolvedKtp = resolvePhoto(context, fotoKtpBase64, fotoKtp, "ktp", nik)
    val resolvedKk = resolvePhoto(context, fotoKkBase64, fotoKk, "kk", if (noKk.isNotBlank()) noKk else nik)

    return Resident(
      id = nik.toLongOrNull() ?: Math.abs(id.hashCode().toLong()),
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
      fotoPenduduk = resolvedPenduduk,
      fotoKtp = resolvedKtp,
      fotoKk = resolvedKk,
      createdAt = createdAt
    )
  }

  private fun resolvePhoto(
    context: Context?,
    b64: String,
    rawPathOrB64: String,
    prefix: String,
    idKey: String
  ): String {
    val cleanB64 = PhotoStorageManager.cleanBase64(b64)
    val actualBase64 = when {
      cleanB64.isNotBlank() -> cleanB64
      PhotoStorageManager.isBase64String(rawPathOrB64) -> PhotoStorageManager.cleanBase64(rawPathOrB64)
      else -> ""
    }

    // 1. Jika ada Base64 string dan context, buat / pastikan file lokal di perangkat ini (misal di HP Admin)
    if (context != null && actualBase64.isNotBlank()) {
      val localPath = PhotoStorageManager.ensureLocalFileFromBase64(context, actualBase64, prefix, idKey)
      if (localPath.isNotBlank() && File(localPath).exists() && File(localPath).length() > 0) {
        return localPath
      }
    }

    // 2. Cek apakah file lokal yang tercatat ada di perangkat ini (misal di HP operator yang mengambil foto)
    if (rawPathOrB64.isNotBlank() && File(rawPathOrB64).exists() && File(rawPathOrB64).length() > 0) {
      return rawPathOrB64
    }

    // 3. Cek file default berdasarkan idKey di folder resident_photos
    if (context != null) {
      val cleanKey = idKey.replace(Regex("[^a-zA-Z0-9]"), "_")
      val defaultFile = File(PhotoStorageManager.getPhotoDirectory(context), "${prefix}_${cleanKey}.jpg")
      if (defaultFile.exists() && defaultFile.length() > 0) {
        return defaultFile.absolutePath
      }
    }

    // 4. Jika tetap belum ada file lokal tapi ada Base64, kembalikan Base64 agar dapat di-decode langsung oleh Coil / BitmapFactory
    return actualBase64.ifBlank { rawPathOrB64 }
  }

  companion object {
    fun fromResident(r: Resident, userEmail: String = ""): FirestoreResident {
      val b64Penduduk = PhotoStorageManager.fileToCompressedBase64(r.fotoPenduduk, maxDim = 480, quality = 65)
      val b64Ktp = PhotoStorageManager.fileToCompressedBase64(r.fotoKtp, maxDim = 720, quality = 60)
      val b64Kk = PhotoStorageManager.fileToCompressedBase64(r.fotoKk, maxDim = 720, quality = 60)

      return FirestoreResident(
        id = r.nik,
        nik = r.nik,
        noKk = r.noKk,
        namaLengkap = r.namaLengkap,
        jenisKelamin = r.jenisKelamin,
        tempatLahir = r.tempatLahir,
        tanggalLahir = r.tanggalLahir,
        rt = r.rt,
        rw = r.rw,
        dusun = r.dusun,
        alamatLengkap = r.alamatLengkap,
        agama = r.agama,
        statusKeluarga = r.statusKeluarga,
        statusKawin = r.statusKawin,
        pekerjaan = r.pekerjaan,
        pendidikan = r.pendidikan,
        golonganDarah = r.golonganDarah,
        statusTinggal = r.statusTinggal,
        noHp = r.noHp,
        bantuanSosial = r.bantuanSosial,
        disabilitas = r.disabilitas,
        catatan = r.catatan,
        fotoPenduduk = r.fotoPenduduk,
        fotoKtp = r.fotoKtp,
        fotoKk = r.fotoKk,
        fotoPendudukBase64 = b64Penduduk,
        fotoKtpBase64 = b64Ktp,
        fotoKkBase64 = b64Kk,
        createdAt = if (r.createdAt > 0) r.createdAt else System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        updatedBy = userEmail
      )
    }
  }
}
