package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class PopulationMutation(
  @DocumentId val id: String = "",
  val type: String = TYPE_PINDAH_MASUK, // "PINDAH_MASUK", "PINDAH_KELUAR", "KELAHIRAN", "KEMATIAN"
  val tanggalMutasi: String = "", // YYYY-MM-DD
  val nik: String = "",
  val noKk: String = "",
  val namaLengkap: String = "",
  val jenisKelamin: String = "LAKI-LAKI",
  val tempatLahir: String = "",
  val tanggalLahir: String = "",
  val rt: String = "RT 01",
  val rw: String = "RW 01",
  val dusun: String = "Dusun Benda",
  val alamatLokal: String = "", // Alamat rumah di Desa Benda
  val alamatLuar: String = "", // Alamat asal (Pindah Masuk) atau Alamat tujuan (Pindah Keluar)
  val alasanPindah: String = "Pekerjaan",
  val statusKeluarga: String = "Kepala Keluarga",
  val noHp: String = "",
  // Data Khusus Kelahiran
  val namaAyah: String = "",
  val namaIbu: String = "",
  val beratLahir: String = "",
  val panjangLahir: String = "",
  // Data Khusus Kematian
  val penyebabKematian: String = "",
  val tempatKematian: String = "",
  val tempatPemakaman: String = "",
  val namaPelapor: String = "",
  val catatan: String = "",
  val petugas: String = "",
  val createdAt: Long = System.currentTimeMillis()
) {
  val isPindahMasuk: Boolean
    get() = type.equals(TYPE_PINDAH_MASUK, ignoreCase = true)

  val isPindahKeluar: Boolean
    get() = type.equals(TYPE_PINDAH_KELUAR, ignoreCase = true)

  val isKelahiran: Boolean
    get() = type.equals(TYPE_KELAHIRAN, ignoreCase = true)

  val isKematian: Boolean
    get() = type.equals(TYPE_KEMATIAN, ignoreCase = true)

  val isAddingPopulation: Boolean
    get() = isPindahMasuk || isKelahiran

  val typeLabel: String
    get() = when (type) {
      TYPE_PINDAH_MASUK -> "Pindah Masuk (+ Jiwa)"
      TYPE_PINDAH_KELUAR -> "Pindah Keluar (- Jiwa)"
      TYPE_KELAHIRAN -> "Kelahiran (+ Jiwa)"
      TYPE_KEMATIAN -> "Kematian (- Jiwa)"
      else -> type
    }

  companion object {
    const val TYPE_PINDAH_MASUK = "PINDAH_MASUK"
    const val TYPE_PINDAH_KELUAR = "PINDAH_KELUAR"
    const val TYPE_KELAHIRAN = "KELAHIRAN"
    const val TYPE_KEMATIAN = "KEMATIAN"

    val defaultAlasanList = listOf(
      "Pekerjaan / Mutasi Kerja",
      "Pernikahan / Membangun Rumah Tangga",
      "Ikut Keluarga / Orang Tua",
      "Pendidikan / Sekolah",
      "Pindah Rumah / Beli Tempat Tinggal Baru",
      "Lingkungan & Keluarga",
      "Lainnya"
    )

    val defaultPenyebabKematianList = listOf(
      "Sakit / Medis",
      "Usia Tua / Alami",
      "Kecelakaan Lalu Lintas",
      "Kecelakaan Kerja",
      "Penyakit Kronis / Bawaan",
      "Lainnya"
    )
  }
}
