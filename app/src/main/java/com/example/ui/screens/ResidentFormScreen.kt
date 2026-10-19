package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import com.example.ui.components.PhotoPickerCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sample.SampleData
import com.example.ui.components.DapendaTopAppBar
import com.example.ui.navigation.Screen
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryContainer
import com.example.ui.viewmodel.DapendaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentFormScreen(
  viewModel: DapendaViewModel,
  residentId: Long?,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val formState by viewModel.formState.collectAsState()
  val availableRts by viewModel.availableRtList.collectAsState()
  val isEdit = (formState.id != 0L)

  Scaffold(
    topBar = {
      DapendaTopAppBar(
        title = if (isEdit) "Edit Data Penduduk" else "Input Data Penduduk Baru",
        subtitle = "Pencatatan Resmi Tingkat RT",
        showBackButton = true,
        onBackClick = { viewModel.navigateBack() }
      )
    },
    bottomBar = {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = { viewModel.navigateBack() },
            modifier = Modifier.weight(1f)
          ) {
            Text("Batal")
          }
          Button(
            onClick = {
              viewModel.saveResident { savedId ->
                viewModel.navigateBack()
              }
            },
            enabled = !formState.isSaving,
            modifier = Modifier
              .weight(2f)
              .testTag("save_resident_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            if (formState.isSaving) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Menyimpan...")
            } else {
              Icon(Icons.Default.Save, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(if (isEdit) "Simpan Perubahan" else "Simpan Data Warga", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // Section 1: Identitas Pokok
      item {
        FormSectionCard(
          title = "1. Data Identitas Pokok",
          icon = Icons.Default.Badge
        ) {
          // NIK
          OutlinedTextField(
            value = formState.nik,
            onValueChange = { input ->
              if (input.length <= 16 && input.all { it.isDigit() }) {
                viewModel.updateFormField { it.copy(nik = input) }
              }
            },
            label = { Text("Nomor Induk Kependudukan (NIK) *") },
            placeholder = { Text("16 Digit Angka") },
            supportingText = {
              Text("${formState.nik.length}/16 digit")
            },
            isError = formState.errors.containsKey("nik"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("form_nik_input")
          )
          formState.errors["nik"]?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // No KK
          OutlinedTextField(
            value = formState.noKk,
            onValueChange = { input ->
              if (input.length <= 16 && input.all { it.isDigit() }) {
                viewModel.updateFormField { it.copy(noKk = input) }
              }
            },
            label = { Text("Nomor Kartu Keluarga (No. KK) *") },
            placeholder = { Text("16 Digit Angka Sesuai KK") },
            supportingText = {
              Text("${formState.noKk.length}/16 digit")
            },
            isError = formState.errors.containsKey("noKk"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("form_nokk_input")
          )
          formState.errors["noKk"]?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Nama Lengkap
          OutlinedTextField(
            value = formState.namaLengkap,
            onValueChange = { input ->
              viewModel.updateFormField { it.copy(namaLengkap = input) }
            },
            label = { Text("Nama Lengkap Sesuai KTP *") },
            placeholder = { Text("Nama Lengkap Warga") },
            isError = formState.errors.containsKey("namaLengkap"),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("form_nama_input")
          )
          formState.errors["namaLengkap"]?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Jenis Kelamin Segmented
          Text(
            text = "Jenis Kelamin *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val isMale = formState.jenisKelamin.equals("LAKI-LAKI", ignoreCase = true)
            FilterChip(
              selected = isMale,
              onClick = { viewModel.updateFormField { it.copy(jenisKelamin = "LAKI-LAKI") } },
              label = { Text("Laki-Laki") },
              modifier = Modifier.weight(1f),
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimaryContainer)
            )
            FilterChip(
              selected = !isMale,
              onClick = { viewModel.updateFormField { it.copy(jenisKelamin = "PEREMPUAN") } },
              label = { Text("Perempuan") },
              modifier = Modifier.weight(1f),
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimaryContainer)
            )
          }
        }
      }

      // Section 2: Tempat & Tanggal Lahir
      item {
        FormSectionCard(
          title = "2. Kelahiran & Usia",
          icon = Icons.Default.Cake
        ) {
          OutlinedTextField(
            value = formState.tempatLahir,
            onValueChange = { input -> viewModel.updateFormField { it.copy(tempatLahir = input) } },
            label = { Text("Tempat Lahir (Kota/Kabupaten) *") },
            placeholder = { Text("Contoh: Bogor") },
            singleLine = true,
            isError = formState.errors.containsKey("tempatLahir"),
            modifier = Modifier.fillMaxWidth()
          )
          formState.errors["tempatLahir"]?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Tanggal Lahir (YYYY-MM-DD)
          OutlinedTextField(
            value = formState.tanggalLahir,
            onValueChange = { input -> viewModel.updateFormField { it.copy(tanggalLahir = input) } },
            label = { Text("Tanggal Lahir (Format: TTTT-BB-HH)") },
            placeholder = { Text("1995-08-17") },
            supportingText = {
              val age = try {
                val year = formState.tanggalLahir.split("-").getOrNull(0)?.toIntOrNull() ?: 2000
                (2026 - year).coerceAtLeast(0)
              } catch (e: Exception) { 0 }
              Text("Estimasi Usia: $age Tahun")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // Section 3: Wilayah RT & Alamat
      item {
        FormSectionCard(
          title = "3. Wilayah RW, RT & Alamat Tinggal",
          icon = Icons.Default.LocationOn
        ) {
          // 1. RW Selection
          Text(
            text = "1. Pilih Rukun Warga (RW) *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(SampleData.rwOptions) { rwOption ->
              val isSelected = (formState.rw == rwOption)
              val rtCount = SampleData.rwRtHierarchy[rwOption]?.size ?: 0
              FilterChip(
                selected = isSelected,
                onClick = {
                  val allowedRts = SampleData.getRtListForRw(rwOption)
                  val newRt = if (allowedRts.contains(formState.rt)) formState.rt else allowedRts.firstOrNull() ?: "RT 01"
                  viewModel.updateFormField { it.copy(rw = rwOption, rt = newRt) }
                },
                label = {
                  Text(
                    text = "$rwOption ($rtCount RT)",
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = TealPrimaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.primary
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // 2. RT Selection dynamically corresponding to selected RW
          val currentRwRts = SampleData.getRtListForRw(formState.rw)
          Text(
            text = "2. Pilih Rukun Tetangga (RT) di ${formState.rw} (${currentRwRts.size} RT Tersedia) *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(currentRwRts) { rtOption ->
              val isSelected = (formState.rt == rtOption)
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.updateFormField { it.copy(rt = rtOption) } },
                label = {
                  Text(
                    text = rtOption,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = TealPrimaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.primary
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Badge Wilayah Terpilih
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Wilayah Terpilih: ${formState.rt} / ${formState.rw} (Total ${currentRwRts.size} RT di ${formState.rw})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = formState.dusun,
            onValueChange = { input -> viewModel.updateFormField { it.copy(dusun = input) } },
            label = { Text("Dusun / Lingkungan") },
            placeholder = { Text("Contoh: Dusun Benda") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = formState.alamatLengkap,
            onValueChange = { input -> viewModel.updateFormField { it.copy(alamatLengkap = input) } },
            label = { Text("Alamat Rumah Lengkap (Jalan/Gang/No. Rumah) *") },
            placeholder = { Text("Contoh: Jl. Melati No. 12 Blok A") },
            isError = formState.errors.containsKey("alamatLengkap"),
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
          )
          formState.errors["alamatLengkap"]?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
        }
      }

      // Section 4: Status Keluarga & Hubungan Sipil
      item {
        FormSectionCard(
          title = "4. Hubungan Keluarga & Status Sipil",
          icon = Icons.Default.FamilyRestroom
        ) {
          FormDropdown(
            label = "Status Hubungan Dalam Keluarga",
            options = SampleData.statusKeluargaOptions,
            selectedValue = formState.statusKeluarga,
            onValueSelected = { selected -> viewModel.updateFormField { it.copy(statusKeluarga = selected) } }
          )

          Spacer(modifier = Modifier.height(10.dp))

          FormDropdown(
            label = "Status Perkawinan",
            options = SampleData.statusKawinOptions,
            selectedValue = formState.statusKawin,
            onValueSelected = { selected -> viewModel.updateFormField { it.copy(statusKawin = selected) } }
          )

          Spacer(modifier = Modifier.height(10.dp))

          FormDropdown(
            label = "Agama",
            options = SampleData.agamaOptions,
            selectedValue = formState.agama,
            onValueSelected = { selected -> viewModel.updateFormField { it.copy(agama = selected) } }
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Golongan Darah",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SampleData.golonganDarahOptions.forEach { goldar ->
              FilterChip(
                selected = (formState.golonganDarah == goldar),
                onClick = { viewModel.updateFormField { it.copy(golonganDarah = goldar) } },
                label = { Text(goldar) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimaryContainer)
              )
            }
          }
        }
      }

      // Section 5: Pendidikan, Profesi & Kesejahteraan
      item {
        FormSectionCard(
          title = "5. Pendidikan, Pekerjaan & Sosial",
          icon = Icons.Default.Work
        ) {
          FormDropdown(
            label = "Pendidikan Terakhir",
            options = SampleData.pendidikanOptions,
            selectedValue = formState.pendidikan,
            onValueSelected = { selected -> viewModel.updateFormField { it.copy(pendidikan = selected) } }
          )

          Spacer(modifier = Modifier.height(10.dp))

          FormDropdown(
            label = "Pekerjaan Utama",
            options = SampleData.pekerjaanOptions,
            selectedValue = formState.pekerjaan,
            onValueSelected = { selected -> viewModel.updateFormField { it.copy(pekerjaan = selected) } }
          )

          Spacer(modifier = Modifier.height(10.dp))

          FormDropdown(
            label = "Status Keberadaan / Domisili",
            options = SampleData.statusTinggalOptions,
            selectedValue = formState.statusTinggal,
            onValueSelected = { selected -> viewModel.updateFormField { it.copy(statusTinggal = selected) } }
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = formState.noHp,
            onValueChange = { input -> viewModel.updateFormField { it.copy(noHp = input) } },
            label = { Text("No. WhatsApp / HP Aktif") },
            placeholder = { Text("08xxxxxxxxxx") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Bantuan Sosial switch
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Penerima Bantuan Sosial",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "PKH, BPNT, BLT Dana Desa, dll.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = formState.bantuanSosial,
              onCheckedChange = { checked -> viewModel.updateFormField { it.copy(bantuanSosial = checked) } },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Disabilitas switch
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Penyandang Disabilitas",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Warga berkebutuhan khusus / lansia rentan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = formState.disabilitas,
              onCheckedChange = { checked -> viewModel.updateFormField { it.copy(disabilitas = checked) } },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = formState.catatan,
            onValueChange = { input -> viewModel.updateFormField { it.copy(catatan = input) } },
            label = { Text("Catatan RT / Keterangan Lain") },
            placeholder = { Text("Contoh: Pengurus RT / Kader Posyandu / dll.") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // Section 6: Berkas & Foto Dokumen Kependudukan
      item {
        FormSectionCard(
          title = "6. Pengambilan Foto Berkas & Dokumen",
          icon = Icons.Default.CameraAlt
        ) {
          Text(
            text = "Ambil foto langsung melalui kamera HP atau pilih dari galeri untuk melengkapi berkas administrasi kependudukan warga RT.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          // 1. Foto Wajah Penduduk
          PhotoPickerCard(
            title = "Foto Wajah / Pas Foto Penduduk",
            subtitle = "Foto profil wajah warga bersangkutan",
            icon = Icons.Default.Person,
            photoPath = formState.fotoPenduduk,
            prefix = "penduduk_${formState.nik.ifBlank { "draft" }}",
            aspectRatio = 1f,
            onPhotoUpdated = { path -> viewModel.updateFormPhotoPenduduk(path) },
            onPhotoRemoved = { viewModel.updateFormPhotoPenduduk("") }
          )

          Spacer(modifier = Modifier.height(14.dp))

          // 2. Foto KTP
          PhotoPickerCard(
            title = "Foto KTP (Kartu Tanda Penduduk)",
            subtitle = "Foto fisik e-KTP tampak depan jelas",
            icon = Icons.Default.Badge,
            photoPath = formState.fotoKtp,
            prefix = "ktp_${formState.nik.ifBlank { "draft" }}",
            aspectRatio = 16f / 10f,
            onPhotoUpdated = { path -> viewModel.updateFormPhotoKtp(path) },
            onPhotoRemoved = { viewModel.updateFormPhotoKtp("") }
          )

          Spacer(modifier = Modifier.height(14.dp))

          // 3. Foto Kartu Keluarga (KK)
          PhotoPickerCard(
            title = "Foto Kartu Keluarga (KK)",
            subtitle = "Foto lembar resmi Kartu Keluarga",
            icon = Icons.Default.Description,
            photoPath = formState.fotoKk,
            prefix = "kk_${formState.noKk.ifBlank { "draft" }}",
            aspectRatio = 14f / 10f,
            onPhotoUpdated = { path -> viewModel.updateFormPhotoKk(path) },
            onPhotoRemoved = { viewModel.updateFormPhotoKk("") }
          )
        }
      }

      // Bottom Spacer for FAB / Bottom Bar
      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
  }
}

@Composable
fun FormSectionCard(
  title: String,
  icon: ImageVector,
  content: @Composable () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      content()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdown(
  label: String,
  options: List<String>,
  selectedValue: String,
  onValueSelected: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = it },
    modifier = Modifier.fillMaxWidth()
  ) {
    OutlinedTextField(
      value = selectedValue,
      onValueChange = {},
      readOnly = true,
      label = { Text(label) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      modifier = Modifier
        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        .fillMaxWidth()
    )
    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option) },
          onClick = {
            onValueSelected(option)
            expanded = false
          }
        )
      }
    }
  }
}
