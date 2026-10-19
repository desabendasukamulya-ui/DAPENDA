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
   * Data Operator RT & RW Resmi Terdaftar di Desa Benda berikut Nomor WhatsApp masing-masing.
   */
  fun getDefaultRegisteredOperators(): List<com.example.data.firebase.UserProfile> {
    val defaultPasswordHash = com.example.util.SessionManager.hashPassword("Benda15631")
    return listOf(
      com.example.data.firebase.UserProfile(
        uid = "op_rt01_rw01",
        email = "rt01rw01@desabenda.id",
        displayName = "Ahmad Fauzi",
        role = "Operator RT",
        rt = "RT 01",
        rw = "RW 01",
        phone = "081287654321",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt02_rw01",
        email = "rt02rw01@desabenda.id",
        displayName = "Siti Rahmawati",
        role = "Operator RT",
        rt = "RT 02",
        rw = "RW 01",
        phone = "081398765432",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt03_rw01",
        email = "rt03rw01@desabenda.id",
        displayName = "Hendra Kurniawan",
        role = "Operator RT",
        rt = "RT 03",
        rw = "RW 01",
        phone = "081987654321",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt04_rw01",
        email = "rt04rw01@desabenda.id",
        displayName = "Bambang Sutrisno",
        role = "Operator RT",
        rt = "RT 04",
        rw = "RW 01",
        phone = "085212345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt05_rw01",
        email = "rt05rw01@desabenda.id",
        displayName = "Dedi Supriyadi",
        role = "Operator RT",
        rt = "RT 05",
        rw = "RW 01",
        phone = "085712345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt01_rw02",
        email = "rt01rw02@desabenda.id",
        displayName = "Agus Setiawan",
        role = "Operator RT",
        rt = "RT 01",
        rw = "RW 02",
        phone = "082112345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt02_rw02",
        email = "rt02rw02@desabenda.id",
        displayName = "Rina Marlina",
        role = "Operator RT",
        rt = "RT 02",
        rw = "RW 02",
        phone = "087812345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt03_rw02",
        email = "rt03rw02@desabenda.id",
        displayName = "Wahyu Hidayat",
        role = "Operator RT",
        rt = "RT 03",
        rw = "RW 02",
        phone = "089512345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt04_rw02",
        email = "rt04rw02@desabenda.id",
        displayName = "Eko Prasetyo",
        role = "Operator RT",
        rt = "RT 04",
        rw = "RW 02",
        phone = "081298765430",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt01_rw03",
        email = "rt01rw03@desabenda.id",
        displayName = "Saepulloh",
        role = "Operator RT",
        rt = "RT 01",
        rw = "RW 03",
        phone = "089612345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt02_rw03",
        email = "rt02rw03@desabenda.id",
        displayName = "Nurhasanah",
        role = "Operator RT",
        rt = "RT 02",
        rw = "RW 03",
        phone = "081387654320",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt01_rw04",
        email = "rt01rw04@desabenda.id",
        displayName = "Mulyadi",
        role = "Operator RT",
        rt = "RT 01",
        rw = "RW 04",
        phone = "081298761234",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt02_rw04",
        email = "rt02rw04@desabenda.id",
        displayName = "Fajar Sidik",
        role = "Operator RT",
        rt = "RT 02",
        rw = "RW 04",
        phone = "082212345670",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt03_rw04",
        email = "rt03rw04@desabenda.id",
        displayName = "Supardi",
        role = "Operator RT",
        rt = "RT 03",
        rw = "RW 04",
        phone = "085312345670",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt01_rw05",
        email = "rt01rw05@desabenda.id",
        displayName = "Ujang Rohman",
        role = "Operator RT",
        rt = "RT 01",
        rw = "RW 05",
        phone = "081312345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt02_rw05",
        email = "rt02rw05@desabenda.id",
        displayName = "Asep Sunandar",
        role = "Operator RT",
        rt = "RT 02",
        rw = "RW 05",
        phone = "081712345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt03_rw05",
        email = "rt03rw05@desabenda.id",
        displayName = "Imas Masitoh",
        role = "Operator RT",
        rt = "RT 03",
        rw = "RW 05",
        phone = "081812345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt04_rw05",
        email = "rt04rw05@desabenda.id",
        displayName = "Dadang Hermawan",
        role = "Operator RT",
        rt = "RT 04",
        rw = "RW 05",
        phone = "081912345678",
        passwordHash = defaultPasswordHash
      ),
      com.example.data.firebase.UserProfile(
        uid = "op_rt05_rw05",
        email = "rt05rw05@desabenda.id",
        displayName = "Iwan Setiawan",
        role = "Operator RT",
        rt = "RT 05",
        rw = "RW 05",
        phone = "081512345678",
        passwordHash = defaultPasswordHash
      )
    )
  }
}
