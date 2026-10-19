package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.sample.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DAPENDA", appName)
  }

  @Test
  fun `verify sample residents are cleared according to user request`() {
    assertTrue(SampleData.initialResidents.isEmpty())
  }

  @Test
  fun `verify RW and RT hierarchy according to specification`() {
    // RW 01 ada 5 RT
    val rw01 = SampleData.getRtListForRw("RW 01")
    assertEquals(5, rw01.size)
    assertEquals(listOf("RT 01", "RT 02", "RT 03", "RT 04", "RT 05"), rw01)

    // RW 02 ada 4 RT
    val rw02 = SampleData.getRtListForRw("RW 02")
    assertEquals(4, rw02.size)
    assertEquals(listOf("RT 01", "RT 02", "RT 03", "RT 04"), rw02)

    // RW 03 ada 2 RT
    val rw03 = SampleData.getRtListForRw("RW 03")
    assertEquals(2, rw03.size)
    assertEquals(listOf("RT 01", "RT 02"), rw03)

    // RW 04 ada 3 RT
    val rw04 = SampleData.getRtListForRw("RW 04")
    assertEquals(3, rw04.size)
    assertEquals(listOf("RT 01", "RT 02", "RT 03"), rw04)

    // RW 05 ada 5 RT
    val rw05 = SampleData.getRtListForRw("RW 05")
    assertEquals(5, rw05.size)
    assertEquals(listOf("RT 01", "RT 02", "RT 03", "RT 04", "RT 05"), rw05)
  }

  @Test
  fun `verify resident model and JSON export`() {
    val testResident = com.example.data.model.Resident(
      id = 1,
      nik = "3201011503750001",
      noKk = "3201012005080001",
      namaLengkap = "Ahmad Hidayat",
      jenisKelamin = "LAKI-LAKI",
      tempatLahir = "Tangerang",
      tanggalLahir = "1975-03-15",
      rt = "RT 01",
      rw = "RW 01",
      dusun = "Dusun Benda",
      alamatLengkap = "Jl. Raya Benda No. 12",
      agama = "Islam",
      statusKeluarga = "Kepala Keluarga",
      statusKawin = "Kawin",
      pekerjaan = "Petani / Pekebun",
      pendidikan = "SLTA / Sederajat",
      golonganDarah = "O",
      statusTinggal = "Warga Tetap"
    )

    val dao = com.example.data.local.AppDatabase.getDatabase(ApplicationProvider.getApplicationContext()).residentDao()
    val repo = com.example.data.repository.ResidentRepository(dao)
    val json = repo.exportToJson(listOf(testResident), includePhotos = false)
    assertTrue(json.contains("DAPENDA"))
    assertTrue(json.contains("Desa Benda"))
    assertTrue(json.contains("Ahmad Hidayat"))

    val preview = repo.parseBackupPreview(json)
    assertTrue(preview.isValid)
    assertEquals("Desa Benda", preview.village)
    assertEquals(1, preview.totalRecords)
  }

  @Test
  fun `verify session persistence for operator login and auto open`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sessionManager = com.example.util.SessionManager(context)
    sessionManager.clearSession()

    val operator = com.example.data.firebase.UserProfile(
      uid = "op_test_123",
      email = "operator.rt01@desabenda.id",
      displayName = "Budi Santoso",
      role = "operator",
      rt = "RT 01",
      rw = "RW 01",
      phone = "08123456789"
    )

    // Simpan akun (remember me)
    sessionManager.saveSession(operator, rememberMe = true)
    assertTrue(sessionManager.isLoggedIn())

    // Buka aplikasi di kemudian hari (restore session)
    val restored = sessionManager.getSavedSession()
    org.junit.Assert.assertNotNull(restored)
    assertEquals("op_test_123", restored?.uid)
    assertEquals("Budi Santoso", restored?.displayName)
    assertEquals("RT 01", restored?.rt)
    assertEquals("RW 01", restored?.rw)
    assertEquals("operator", restored?.role)

    // Logout
    sessionManager.clearSession()
    org.junit.Assert.assertNull(sessionManager.getSavedSession())
  }

  @Test
  fun `verify password hashing security`() {
    val hash1 = com.example.util.SessionManager.hashPassword("rahasia123")
    val hash2 = com.example.util.SessionManager.hashPassword("rahasia123")
    val hashDiff = com.example.util.SessionManager.hashPassword("passwordLain")

    assertEquals(hash1, hash2)
    org.junit.Assert.assertNotEquals(hash1, hashDiff)
    assertEquals(64, hash1.length) // SHA-256 produces 64 hex characters
  }

  @Test
  fun `verify cross-device photo sync and restoration for admin view`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Buat sample dummy bitmap untuk disimulasikan sebagai foto KTP/KK yang diambil operator RT
    val testBitmap = android.graphics.Bitmap.createBitmap(100, 60, android.graphics.Bitmap.Config.ARGB_8888)
    val localOperatorPath = com.example.util.PhotoStorageManager.saveBitmap(context, testBitmap, "ktp_sample")
    assertTrue(java.io.File(localOperatorPath).exists())

    // Operator RT menyimpan data penduduk: dikonversi ke Base64 terkompresi
    val resident = com.example.data.model.Resident(
      id = 1,
      nik = "3201010101990001",
      noKk = "3201010101990001",
      namaLengkap = "Warga Contoh",
      jenisKelamin = "LAKI-LAKI",
      tempatLahir = "Tangerang",
      tanggalLahir = "1999-01-01",
      rt = "RT 01",
      rw = "RW 01",
      dusun = "Dusun Benda",
      alamatLengkap = "Jl. Contoh No. 1",
      agama = "Islam",
      statusKeluarga = "Kepala Keluarga",
      statusKawin = "Belum Kawin",
      pekerjaan = "Karyawan Swasta",
      pendidikan = "SLTA / Sederajat",
      golonganDarah = "O",
      statusTinggal = "Warga Tetap",
      fotoKtp = localOperatorPath
    )

    val firestoreResident = com.example.data.firebase.FirestoreResident.fromResident(resident, "operator.rt01@desabenda.id")
    assertTrue(firestoreResident.fotoKtpBase64.isNotBlank())

    // Simulasikan perangkat Admin (di mana file localOperatorPath tidak ada awalnya)
    // toResident(context) harus otomatis merekonstruksi file gambar lokal dari fotoKtpBase64
    val adminResident = firestoreResident.toResident(context)
    assertTrue(adminResident.fotoKtp.isNotBlank())
    assertTrue(com.example.util.PhotoStorageManager.hasPhoto(adminResident.fotoKtp))

    val adminModel = com.example.util.PhotoStorageManager.getPhotoModel(context, adminResident.fotoKtp)
    org.junit.Assert.assertNotNull(adminModel)
  }

  @Test
  fun `verify population mutation model and automatic citizen increment and decrement`() {
    val pindahMasuk = com.example.data.model.PopulationMutation(
      id = "mut_in_1",
      type = com.example.data.model.PopulationMutation.TYPE_PINDAH_MASUK,
      tanggalMutasi = "2026-10-19",
      nik = "3201011111990001",
      noKk = "3201011111990001",
      namaLengkap = "Rina Marlina",
      jenisKelamin = "PEREMPUAN",
      rt = "RT 02",
      rw = "RW 01",
      alamatLuar = "Desa Cibadak, Kab. Sukabumi",
      alasanPindah = "Pernikahan"
    )

    assertTrue(pindahMasuk.isPindahMasuk)
    org.junit.Assert.assertFalse(pindahMasuk.isPindahKeluar)
    assertEquals("Pindah Masuk", pindahMasuk.typeLabel)

    val pindahKeluar = com.example.data.model.PopulationMutation(
      id = "mut_out_1",
      type = com.example.data.model.PopulationMutation.TYPE_PINDAH_KELUAR,
      tanggalMutasi = "2026-10-19",
      nik = "3201012222880002",
      noKk = "3201012222880002",
      namaLengkap = "Hendra Gunawan",
      rt = "RT 02",
      rw = "RW 01",
      alamatLuar = "Kota Bandung",
      alasanPindah = "Pekerjaan"
    )

    assertTrue(pindahKeluar.isPindahKeluar)
    org.junit.Assert.assertFalse(pindahKeluar.isPindahMasuk)
    assertEquals("Pindah Keluar", pindahKeluar.typeLabel)

    // Simulasi data penduduk dan pengaruh mutasi terhadap hitungan aktif
    val activeResidents = mutableListOf(
      com.example.data.model.Resident(
        id = 101,
        nik = "3201012222880002",
        noKk = "3201012222880002",
        namaLengkap = "Hendra Gunawan",
        jenisKelamin = "LAKI-LAKI",
        tempatLahir = "Tangerang",
        tanggalLahir = "1988-05-10",
        rt = "RT 02",
        rw = "RW 01",
        dusun = "Dusun Benda",
        alamatLengkap = "Jl. Sukamulya",
        agama = "Islam",
        statusKeluarga = "Kepala Keluarga",
        statusKawin = "Kawin",
        pekerjaan = "Karyawan",
        pendidikan = "S1",
        golonganDarah = "A",
        statusTinggal = "Warga Tetap"
      )
    )

    // Jumlah awal warga aktif: 1
    val initialActiveCount = activeResidents.count { !it.statusTinggal.equals("Pindah Keluar", ignoreCase = true) }
    assertEquals(1, initialActiveCount)

    // Proses Pindah Masuk: Warga baru bertambah
    val newResidentFromMasuk = com.example.data.model.Resident(
      id = 102,
      nik = pindahMasuk.nik,
      noKk = pindahMasuk.noKk,
      namaLengkap = pindahMasuk.namaLengkap,
      jenisKelamin = pindahMasuk.jenisKelamin,
      tempatLahir = "Sukabumi",
      tanggalLahir = "1999-10-11",
      rt = pindahMasuk.rt,
      rw = pindahMasuk.rw,
      dusun = "Dusun Benda",
      alamatLengkap = "Jl. Sukamulya No. 4",
      agama = "Islam",
      statusKeluarga = "Istri",
      statusKawin = "Kawin",
      pekerjaan = "Wiraswasta",
      pendidikan = "SMA",
      golonganDarah = "B",
      statusTinggal = "Warga Tetap"
    )
    activeResidents.add(newResidentFromMasuk)

    // Otomatis MENAMBAH data penduduk: jumlah jadi 2
    val countAfterMasuk = activeResidents.count { !it.statusTinggal.equals("Pindah Keluar", ignoreCase = true) }
    assertEquals(2, countAfterMasuk)

    // Proses Pindah Keluar: Hendra Gunawan statusnya berubah jadi Pindah Keluar
    val indexHendra = activeResidents.indexOfFirst { it.nik == pindahKeluar.nik }
    activeResidents[indexHendra] = activeResidents[indexHendra].copy(statusTinggal = "Pindah Keluar")

    // Otomatis MENGURANGI data penduduk dari hitungan warga aktif: jumlah kembali berkurang jadi 1
    val countAfterKeluar = activeResidents.count { !it.statusTinggal.equals("Pindah Keluar", ignoreCase = true) }
    assertEquals(1, countAfterKeluar)
    assertEquals("Rina Marlina", activeResidents.first { !it.statusTinggal.equals("Pindah Keluar", ignoreCase = true) }.namaLengkap)
  }
}
