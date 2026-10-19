package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "residents")
data class Resident(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val nik: String, // 16 digit
  val noKk: String, // 16 digit
  val namaLengkap: String,
  val jenisKelamin: String, // "LAKI-LAKI" / "PEREMPUAN"
  val tempatLahir: String,
  val tanggalLahir: String, // format YYYY-MM-DD
  val rt: String, // e.g. "RT 01"
  val rw: String, // e.g. "RW 01"
  val dusun: String, // e.g. "Dusun Benda"
  val alamatLengkap: String,
  val agama: String, // "Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Konghucu", "Lainnya"
  val statusKeluarga: String, // "Kepala Keluarga", "Istri", "Anak", "Orang Tua", "Famili Lain"
  val statusKawin: String, // "Belum Kawin", "Kawin", "Cerai Hidup", "Cerai Mati"
  val pekerjaan: String,
  val pendidikan: String,
  val golonganDarah: String, // "A", "B", "AB", "O", "-"
  val statusTinggal: String, // "Warga Tetap", "Warga Kontrak / Sementara", "Pindah Keluar", "Meninggal"
  val noHp: String = "",
  val bantuanSosial: Boolean = false,
  val disabilitas: Boolean = false,
  val catatan: String = "",
  val fotoPenduduk: String = "", // Path to local resident face photo
  val fotoKtp: String = "", // Path to local KTP photo
  val fotoKk: String = "", // Path to local Kartu Keluarga photo
  val createdAt: Long = System.currentTimeMillis()
) {
  // Calculate approximate age from tanggalLahir (YYYY-MM-DD)
  val usia: Int
    get() {
      return try {
        val parts = tanggalLahir.split("-")
        if (parts.size >= 1) {
          val birthYear = parts[0].toIntOrNull() ?: 2000
          val currentYear = 2026
          (currentYear - birthYear).coerceAtLeast(0)
        } else {
          0
        }
      } catch (e: Exception) {
        0
      }
    }

  val kelompokUsia: String
    get() {
      val age = usia
      return when {
        age <= 5 -> "Balita (0-5 th)"
        age in 6..12 -> "Anak (6-12 th)"
        age in 13..17 -> "Remaja (13-17 th)"
        age in 18..59 -> "Produktif (18-59 th)"
        else -> "Lansia (60+ th)"
      }
    }
}
