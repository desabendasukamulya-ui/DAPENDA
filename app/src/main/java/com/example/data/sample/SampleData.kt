package com.example.data.sample

import com.example.data.model.Resident

object SampleData {

  val rwOptions = listOf(
    "RW 01",
    "RW 02",
    "RW 03",
    "RW 04",
    "RW 05"
  )

  // Konfigurasi Hierarki RT per RW sesuai ketentuan desa:
  // RW 01: 5 RT (RT 01 s/d RT 05)
  // RW 02: 4 RT (RT 01 s/d RT 04)
  // RW 03: 2 RT (RT 01 s/d RT 02)
  // RW 04: 3 RT (RT 01 s/d RT 03)
  // RW 05: 5 RT (RT 01 s/d RT 05)
  val rwRtHierarchy: Map<String, List<String>> = mapOf(
    "RW 01" to listOf("RT 01", "RT 02", "RT 03", "RT 04", "RT 05"),
    "RW 02" to listOf("RT 01", "RT 02", "RT 03", "RT 04"),
    "RW 03" to listOf("RT 01", "RT 02"),
    "RW 04" to listOf("RT 01", "RT 02", "RT 03"),
    "RW 05" to listOf("RT 01", "RT 02", "RT 03", "RT 04", "RT 05")
  )

  fun getRtListForRw(rw: String): List<String> {
    return rwRtHierarchy[rw] ?: listOf("RT 01", "RT 02", "RT 03", "RT 04", "RT 05")
  }

  fun getRtCountDescription(rw: String): String {
    val count = rwRtHierarchy[rw]?.size ?: 0
    return "$rw ($count RT)"
  }

  val defaultRtList = listOf(
    "RT 01",
    "RT 02",
    "RT 03",
    "RT 04",
    "RT 05"
  )

  // Data contoh penduduk telah dikosongkan/dihapus sesuai permintaan pengguna.
  // Database kini bersih dan siap untuk input data warga asli atau impor file cadangan.
  val initialResidents: List<Resident> = emptyList()

  // Daftar NIK data contoh bawaan terdahulu untuk pembersihan database
  val sampleNiksToPurge: List<String> = listOf(
    "3201011503750001",
    "3201015208790002",
    "3201011005050003",
    "3201010407680004",
    "3201014101700005",
    "3201012512800006",
    "3201016003850007",
    "3201011409210008",
    "3201010101920009",
    "3201011802550010",
    "3201015606900011",
    "3201011210880012",
    "3201014511910013",
    "3201010303820014",
    "3201014404860015"
  )

  val agamaOptions = listOf(
    "Islam",
    "Kristen",
    "Katolik",
    "Hindu",
    "Buddha",
    "Konghucu",
    "Lainnya"
  )

  val statusKeluargaOptions = listOf(
    "Kepala Keluarga",
    "Istri",
    "Anak",
    "Orang Tua",
    "Mertua",
    "Famili Lain"
  )

  val statusKawinOptions = listOf(
    "Belum Kawin",
    "Kawin",
    "Cerai Hidup",
    "Cerai Mati"
  )

  val pekerjaanOptions = listOf(
    "Wiraswasta / Dagang",
    "Karyawan Swasta",
    "Petani / Pekebun / Peternak",
    "PNS / TNI / POLRI",
    "Buruh Harian Lepas",
    "Guru / Tenaga Pengajar",
    "Pelajar / Mahasiswa",
    "Mengurus Rumah Tangga",
    "Pensiunan",
    "Belum / Tidak Bekerja",
    "Lainnya"
  )

  val pendidikanOptions = listOf(
    "Tidak/Belum Sekolah",
    "SD / Sederajat",
    "SMP / Sederajat",
    "SMA / SMK / Sederajat",
    "Diploma (D1 - D3)",
    "Sarjana (S1 / D4)",
    "Magister (S2)",
    "Doktor (S3)"
  )

  val golonganDarahOptions = listOf("A", "B", "AB", "O", "-")

  val statusTinggalOptions = listOf(
    "Warga Tetap",
    "Warga Kontrak / Sementara",
    "Pindah Keluar",
    "Meninggal"
  )

  /**
   * Sesuai instruksi: Data operator dikosongkan. Tidak ada contoh data otomatis.
   * Data operator hanya akan terisi ketika ada petugas RT/RW yang mendaftarkan akun.
   */
  fun getDefaultRegisteredOperators(): List<com.example.data.firebase.UserProfile> {
    return emptyList()
  }

  // Set ID operator contoh bawaan sebelumnya untuk keperluan pembersihan otomatis dari database
  val dummyOperatorUids = setOf(
    "op_rt01_rw01", "op_rt02_rw01", "op_rt03_rw01", "op_rt04_rw01", "op_rt05_rw01",
    "op_rt01_rw02", "op_rt02_rw02", "op_rt03_rw02", "op_rt04_rw02",
    "op_rt01_rw03", "op_rt02_rw03",
    "op_rt01_rw04", "op_rt02_rw04", "op_rt03_rw04",
    "op_rt01_rw05", "op_rt02_rw05", "op_rt03_rw05", "op_rt04_rw05", "op_rt05_rw05"
  )
}
