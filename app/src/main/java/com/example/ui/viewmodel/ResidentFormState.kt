package com.example.ui.viewmodel

import com.example.data.model.Resident

data class ResidentFormState(
  val id: Long = 0,
  val nik: String = "",
  val noKk: String = "",
  val namaLengkap: String = "",
  val jenisKelamin: String = "LAKI-LAKI",
  val tempatLahir: String = "",
  val tanggalLahir: String = "1995-01-01",
  val rt: String = "RT 01",
  val rw: String = "RW 01",
  val dusun: String = "Dusun Benda",
  val alamatLengkap: String = "",
  val agama: String = "Islam",
  val statusKeluarga: String = "Kepala Keluarga",
  val statusKawin: String = "Kawin",
  val pekerjaan: String = "Wiraswasta / Dagang",
  val pendidikan: String = "SMA / SMK / Sederajat",
  val golonganDarah: String = "-",
  val statusTinggal: String = "Warga Tetap",
  val noHp: String = "",
  val bantuanSosial: Boolean = false,
  val disabilitas: Boolean = false,
  val catatan: String = "",
  val fotoPenduduk: String = "",
  val fotoKtp: String = "",
  val fotoKk: String = "",
  val errors: Map<String, String> = emptyMap(),
  val isSaving: Boolean = false
) {
  fun toResident(): Resident {
    return Resident(
      id = id,
      nik = nik.trim(),
      noKk = noKk.trim(),
      namaLengkap = namaLengkap.trim(),
      jenisKelamin = jenisKelamin,
      tempatLahir = tempatLahir.trim(),
      tanggalLahir = tanggalLahir.trim(),
      rt = rt.trim(),
      rw = rw.trim(),
      dusun = dusun.trim(),
      alamatLengkap = alamatLengkap.trim(),
      agama = agama,
      statusKeluarga = statusKeluarga,
      statusKawin = statusKawin,
      pekerjaan = pekerjaan,
      pendidikan = pendidikan,
      golonganDarah = golonganDarah,
      statusTinggal = statusTinggal,
      noHp = noHp.trim(),
      bantuanSosial = bantuanSosial,
      disabilitas = disabilitas,
      catatan = catatan.trim(),
      fotoPenduduk = fotoPenduduk,
      fotoKtp = fotoKtp,
      fotoKk = fotoKk
    )
  }

  companion object {
    fun fromResident(resident: Resident): ResidentFormState {
      return ResidentFormState(
        id = resident.id,
        nik = resident.nik,
        noKk = resident.noKk,
        namaLengkap = resident.namaLengkap,
        jenisKelamin = resident.jenisKelamin,
        tempatLahir = resident.tempatLahir,
        tanggalLahir = resident.tanggalLahir,
        rt = resident.rt,
        rw = resident.rw,
        dusun = resident.dusun,
        alamatLengkap = resident.alamatLengkap,
        agama = resident.agama,
        statusKeluarga = resident.statusKeluarga,
        statusKawin = resident.statusKawin,
        pekerjaan = resident.pekerjaan,
        pendidikan = resident.pendidikan,
        golonganDarah = resident.golonganDarah,
        statusTinggal = resident.statusTinggal,
        noHp = resident.noHp,
        bantuanSosial = resident.bantuanSosial,
        disabilitas = resident.disabilitas,
        catatan = resident.catatan,
        fotoPenduduk = resident.fotoPenduduk,
        fotoKtp = resident.fotoKtp,
        fotoKk = resident.fotoKk
      )
    }
  }
}
