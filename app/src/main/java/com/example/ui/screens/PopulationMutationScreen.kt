package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PopulationMutation
import com.example.data.model.Resident
import com.example.data.sample.SampleData
import com.example.ui.components.DapendaTopAppBar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberAccentContainer
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.FemalePink
import com.example.ui.theme.MaleBlue
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryContainer
import com.example.ui.viewmodel.DapendaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopulationMutationScreen(
  viewModel: DapendaViewModel,
  initialTab: Int = 0,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  var selectedTab by remember { mutableIntStateOf(initialTab) }
  val allResidents by viewModel.allResidents.collectAsState()
  val mutations by viewModel.mutations.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()
  val isAdmin = userProfile?.isAdmin == true

  val todayStr = remember {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  }

  Scaffold(
    topBar = {
      DapendaTopAppBar(
        title = "Mutasi Kependudukan",
        subtitle = "Pindah Masuk & Pindah Keluar RT/RW",
        showBackButton = true,
        onBackClick = { viewModel.navigateBack() }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Tab Bar
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Pindah Masuk", fontWeight = FontWeight.Bold) },
          icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp)) },
          modifier = Modifier.testTag("tab_pindah_masuk")
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Pindah Keluar", fontWeight = FontWeight.Bold) },
          icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp)) },
          modifier = Modifier.testTag("tab_pindah_keluar")
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Riwayat (${mutations.size})", fontWeight = FontWeight.Bold) },
          icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp)) },
          modifier = Modifier.testTag("tab_riwayat_mutasi")
        )
      }

      when (selectedTab) {
        0 -> FormPindahMasukTab(
          defaultDate = todayStr,
          defaultRt = userProfile?.rt?.ifBlank { "RT 01" } ?: "RT 01",
          onSave = { mutation ->
            viewModel.recordPindahMasuk(mutation) {
              selectedTab = 2 // Pindah ke tab riwayat setelah berhasil
            }
          }
        )
        1 -> FormPindahKeluarTab(
          allResidents = allResidents,
          defaultDate = todayStr,
          onSave = { mutation, removePermanently ->
            viewModel.recordPindahKeluar(mutation, removePermanently) {
              selectedTab = 2
            }
          }
        )
        2 -> RiwayatMutasiTab(
          mutations = mutations,
          isAdmin = isAdmin,
          onDeleteMutation = { id -> viewModel.deleteMutation(id) }
        )
      }
    }
  }
}

/**
 * Tab 1: Form Khusus Warga Pindah Masuk (Otomatis Menambah Jumlah Warga RT)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormPindahMasukTab(
  defaultDate: String,
  defaultRt: String,
  onSave: (PopulationMutation) -> Unit
) {
  var tanggalMutasi by remember { mutableStateOf(defaultDate) }
  var alamatAsal by remember { mutableStateOf("") }
  var alasanPindah by remember { mutableStateOf("Pekerjaan / Mutasi Kerja") }
  var customAlasan by remember { mutableStateOf("") }

  // Tujuan Wilayah
  var selectedRw by remember { mutableStateOf("RW 01") }
  var selectedRt by remember { mutableStateOf(defaultRt) }
  var alamatLokal by remember { mutableStateOf("") }

  // Data Identitas Warga Baru
  var nik by remember { mutableStateOf("") }
  var noKk by remember { mutableStateOf("") }
  var namaLengkap by remember { mutableStateOf("") }
  var jenisKelamin by remember { mutableStateOf("LAKI-LAKI") }
  var tempatLahir by remember { mutableStateOf("") }
  var tanggalLahir by remember { mutableStateOf("1995-01-01") }
  var statusKeluarga by remember { mutableStateOf("Kepala Keluarga") }
  var noHp by remember { mutableStateOf("") }
  var catatan by remember { mutableStateOf("") }

  var expandedAlasan by remember { mutableStateOf(false) }
  var expandedRw by remember { mutableStateOf(false) }
  var expandedRt by remember { mutableStateOf(false) }
  var expandedStatusKeluarga by remember { mutableStateOf(false) }

  val rtOptions = remember(selectedRw) { SampleData.getRtListForRw(selectedRw) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Penjelasan Otomasi
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldSecondaryContainer.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSecondary.copy(alpha = 0.4f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(28.dp))
          Column {
            Text("Pencatatan Warga Pindah Masuk", fontWeight = FontWeight.Bold, color = EmeraldSecondary, style = MaterialTheme.typography.titleSmall)
            Text(
              "Saat data disimpan, sistem DAPENDA otomatis MENAMBAH warga baru ini ke basis data kependudukan RT tujuan dan menambah jumlah jiwa desa.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    // Informasi Pindah
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("1. Informasi Mutasi Pindah Masuk", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

          OutlinedTextField(
            value = tanggalMutasi,
            onValueChange = { tanggalMutasi = it },
            label = { Text("Tanggal Pindah Masuk (YYYY-MM-DD)") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = alamatAsal,
            onValueChange = { alamatAsal = it },
            label = { Text("Alamat Daerah Asal (Desa / Kec / Kota) *") },
            placeholder = { Text("Contoh: Desa Pasir Nangka, Kec. Tigaraksa, Kab. Tangerang") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          // Alasan Pindah
          ExposedDropdownMenuBox(
            expanded = expandedAlasan,
            onExpandedChange = { expandedAlasan = it }
          ) {
            OutlinedTextField(
              value = alasanPindah,
              onValueChange = {},
              readOnly = true,
              label = { Text("Alasan Pindah Masuk") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlasan) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
              expanded = expandedAlasan,
              onDismissRequest = { expandedAlasan = false }
            ) {
              PopulationMutation.defaultAlasanList.forEach { item ->
                DropdownMenuItem(
                  text = { Text(item) },
                  onClick = {
                    alasanPindah = item
                    expandedAlasan = false
                  }
                )
              }
            }
          }

          if (alasanPindah == "Lainnya") {
            OutlinedTextField(
              value = customAlasan,
              onValueChange = { customAlasan = it },
              label = { Text("Keterangan Alasan Lainnya") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          }
        }
      }
    }

    // Wilayah Tujuan di Desa Benda
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("2. Wilayah Domisili Baru di Desa Benda", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // RW Dropdown
            ExposedDropdownMenuBox(
              expanded = expandedRw,
              onExpandedChange = { expandedRw = it },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = selectedRw,
                onValueChange = {},
                readOnly = true,
                label = { Text("RW Tujuan") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRw) },
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor(MenuAnchorType.PrimaryNotEditable)
              )
              ExposedDropdownMenu(
                expanded = expandedRw,
                onDismissRequest = { expandedRw = false }
              ) {
                SampleData.rwOptions.forEach { rw ->
                  DropdownMenuItem(
                    text = { Text(rw) },
                    onClick = {
                      selectedRw = rw
                      selectedRt = SampleData.getRtListForRw(rw).firstOrNull() ?: "RT 01"
                      expandedRw = false
                    }
                  )
                }
              }
            }

            // RT Dropdown
            ExposedDropdownMenuBox(
              expanded = expandedRt,
              onExpandedChange = { expandedRt = it },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = selectedRt,
                onValueChange = {},
                readOnly = true,
                label = { Text("RT Tujuan") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRt) },
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor(MenuAnchorType.PrimaryNotEditable)
              )
              ExposedDropdownMenu(
                expanded = expandedRt,
                onDismissRequest = { expandedRt = false }
              ) {
                rtOptions.forEach { rt ->
                  DropdownMenuItem(
                    text = { Text(rt) },
                    onClick = {
                      selectedRt = rt
                      expandedRt = false
                    }
                  )
                }
              }
            }
          }

          OutlinedTextField(
            value = alamatLokal,
            onValueChange = { alamatLokal = it },
            label = { Text("Alamat Rumah di RT Tujuan") },
            placeholder = { Text("Contoh: Jl. Sukamulya No. 12, RT 01/RW 01") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Biodata Warga Pindah Masuk
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("3. Biodata Kependudukan Warga", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

          OutlinedTextField(
            value = nik,
            onValueChange = { if (it.length <= 16 && it.all { c -> c.isDigit() }) nik = it },
            label = { Text("NIK (Nomor Induk Kependudukan 16 Digit) *") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            supportingText = { Text("${nik.length} / 16 digit") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pindah_masuk_nik"),
            singleLine = true
          )

          OutlinedTextField(
            value = noKk,
            onValueChange = { if (it.length <= 16 && it.all { c -> c.isDigit() }) noKk = it },
            label = { Text("Nomor Kartu Keluarga (KK 16 Digit) *") },
            supportingText = { Text("${noKk.length} / 16 digit") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pindah_masuk_nokk"),
            singleLine = true
          )

          OutlinedTextField(
            value = namaLengkap,
            onValueChange = { namaLengkap = it },
            label = { Text("Nama Lengkap Sesuai KTP *") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pindah_masuk_nama"),
            singleLine = true
          )

          // Jenis Kelamin
          Text("Jenis Kelamin", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { jenisKelamin = "LAKI-LAKI" }
            ) {
              RadioButton(selected = jenisKelamin == "LAKI-LAKI", onClick = { jenisKelamin = "LAKI-LAKI" })
              Text("Laki-Laki", color = MaleBlue, fontWeight = FontWeight.Bold)
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { jenisKelamin = "PEREMPUAN" }
            ) {
              RadioButton(selected = jenisKelamin == "PEREMPUAN", onClick = { jenisKelamin = "PEREMPUAN" })
              Text("Perempuan", color = FemalePink, fontWeight = FontWeight.Bold)
            }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = tempatLahir,
              onValueChange = { tempatLahir = it },
              label = { Text("Tempat Lahir") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            OutlinedTextField(
              value = tanggalLahir,
              onValueChange = { tanggalLahir = it },
              label = { Text("Tgl Lahir (YYYY-MM-DD)") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }

          // Status dalam Keluarga
          ExposedDropdownMenuBox(
            expanded = expandedStatusKeluarga,
            onExpandedChange = { expandedStatusKeluarga = it }
          ) {
            OutlinedTextField(
              value = statusKeluarga,
              onValueChange = {},
              readOnly = true,
              label = { Text("Status Hubungan dalam KK") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatusKeluarga) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
              expanded = expandedStatusKeluarga,
              onDismissRequest = { expandedStatusKeluarga = false }
            ) {
              SampleData.statusKeluargaOptions.forEach { item ->
                DropdownMenuItem(
                  text = { Text(item) },
                  onClick = {
                    statusKeluarga = item
                    expandedStatusKeluarga = false
                  }
                )
              }
            }
          }

          OutlinedTextField(
            value = noHp,
            onValueChange = { noHp = it },
            label = { Text("No. HP / WhatsApp (opsional)") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = catatan,
            onValueChange = { catatan = it },
            label = { Text("Catatan Petugas (opsional)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Tombol Simpan
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Button(
        onClick = {
          val finalAlasan = if (alasanPindah == "Lainnya" && customAlasan.isNotBlank()) customAlasan else alasanPindah
          val mutation = PopulationMutation(
            type = PopulationMutation.TYPE_PINDAH_MASUK,
            tanggalMutasi = tanggalMutasi,
            nik = nik,
            noKk = noKk,
            namaLengkap = namaLengkap,
            jenisKelamin = jenisKelamin,
            tempatLahir = tempatLahir,
            tanggalLahir = tanggalLahir,
            rt = selectedRt,
            rw = selectedRw,
            dusun = "Dusun Benda",
            alamatLokal = alamatLokal,
            alamatLuar = alamatAsal,
            alasanPindah = finalAlasan,
            statusKeluarga = statusKeluarga,
            noHp = noHp,
            catatan = catatan
          )
          onSave(mutation)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_pindah_masuk_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Simpan & Otomatis Tambah ke Warga RT", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

/**
 * Tab 2: Form Khusus Warga Pindah Keluar (Otomatis Mengurangi Jumlah Warga RT)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormPindahKeluarTab(
  allResidents: List<Resident>,
  defaultDate: String,
  onSave: (PopulationMutation, Boolean) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedResident by remember { mutableStateOf<Resident?>(null) }
  var tanggalPindah by remember { mutableStateOf(defaultDate) }
  var alamatTujuan by remember { mutableStateOf("") }
  var alasanPindah by remember { mutableStateOf("Pekerjaan / Mutasi Kerja") }
  var customAlasan by remember { mutableStateOf("") }
  var removePermanently by remember { mutableStateOf(false) } // false: update status to "Pindah Keluar", true: delete completely
  var catatan by remember { mutableStateOf("") }

  var expandedAlasan by remember { mutableStateOf(false) }

  // Filter residents matching search
  val searchResults = remember(searchQuery, allResidents) {
    if (searchQuery.isBlank()) {
      emptyList()
    } else {
      allResidents.filter {
        !it.statusTinggal.equals("Pindah Keluar", ignoreCase = true) &&
        (it.namaLengkap.contains(searchQuery, ignoreCase = true) ||
         it.nik.contains(searchQuery) ||
         it.noKk.contains(searchQuery))
      }.take(8)
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Info banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AmberAccentContainer.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(28.dp))
          Column {
            Text("Pencatatan Warga Pindah Keluar", fontWeight = FontWeight.Bold, color = AmberAccent, style = MaterialTheme.typography.titleSmall)
            Text(
              "Saat data diproses, sistem DAPENDA otomatis MENGURANGI data warga ini dari jumlah penduduk aktif RT bersangkutan.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    // Pilih Warga
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("1. Pilih Warga yang Pindah Keluar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

          if (selectedResident == null) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              label = { Text("Cari Berdasarkan Nama atau NIK Warga") },
              placeholder = { Text("Ketik nama atau 16 digit NIK...") },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Hapus")
                  }
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("search_warga_pindah_keluar"),
              singleLine = true
            )

            if (searchResults.isNotEmpty()) {
              Text("Hasil Pencarian (${searchResults.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                searchResults.forEach { r ->
                  Surface(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(10.dp))
                      .clickable {
                        selectedResident = r
                        searchQuery = ""
                      },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(r.namaLengkap, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("NIK: ${r.nik} • ${r.rt}/${r.rw}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                      Button(
                        onClick = { selectedResident = r },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                      ) {
                        Text("Pilih", fontSize = 12.sp)
                      }
                    }
                  }
                }
              }
            } else if (searchQuery.isNotBlank()) {
              Text("Tidak ada data warga ditemukan dengan kata kunci '$searchQuery'.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          } else {
            // Selected Resident Card
            val r = selectedResident!!
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(r.namaLengkap.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                  }
                  TextButton(onClick = { selectedResident = null }) {
                    Text("Ganti Warga", fontSize = 12.sp)
                  }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("NIK: ${r.nik} • No KK: ${r.noKk}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text("Asal RT/RW: ${r.rt} / ${r.rw}, ${r.dusun}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Alamat: ${r.alamatLengkap}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }
    }

    // Detail Pindah Keluar
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("2. Detail Tujuan & Alasan Pindah", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

          OutlinedTextField(
            value = tanggalPindah,
            onValueChange = { tanggalPindah = it },
            label = { Text("Tanggal Pindah Keluar (YYYY-MM-DD)") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          OutlinedTextField(
            value = alamatTujuan,
            onValueChange = { alamatTujuan = it },
            label = { Text("Alamat Daerah Tujuan Pindah *") },
            placeholder = { Text("Contoh: Kel. Kebon Jeruk, Jakarta Barat atau Kab. Serang") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pindah_keluar_tujuan"),
            singleLine = true
          )

          ExposedDropdownMenuBox(
            expanded = expandedAlasan,
            onExpandedChange = { expandedAlasan = it }
          ) {
            OutlinedTextField(
              value = alasanPindah,
              onValueChange = {},
              readOnly = true,
              label = { Text("Alasan Pindah Keluar") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlasan) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
              expanded = expandedAlasan,
              onDismissRequest = { expandedAlasan = false }
            ) {
              PopulationMutation.defaultAlasanList.forEach { item ->
                DropdownMenuItem(
                  text = { Text(item) },
                  onClick = {
                    alasanPindah = item
                    expandedAlasan = false
                  }
                )
              }
            }
          }

          if (alasanPindah == "Lainnya") {
            OutlinedTextField(
              value = customAlasan,
              onValueChange = { customAlasan = it },
              label = { Text("Keterangan Alasan Lainnya") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          }

          OutlinedTextField(
            value = catatan,
            onValueChange = { catatan = it },
            label = { Text("Catatan Mutasi (opsional)") },
            modifier = Modifier.fillMaxWidth()
          )

          // Pilihan Cara Pengurangan Data
          Text("Tindakan pada Data Kependudukan:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { removePermanently = false },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = !removePermanently, onClick = { removePermanently = false })
            Column {
              Text("Tandai Status Pindah Keluar (Direkomendasikan)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("Otomatis mengurangi jumlah warga aktif, data tetap tercatat di arsip mutasi.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { removePermanently = true },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = removePermanently, onClick = { removePermanently = true })
            Column {
              Text("Hapus Permanen dari Daftar Penduduk", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
              Text("Menghapus data kependudukan sepenuhnya dari database penduduk.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }

    // Submit button
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Button(
        onClick = {
          val r = selectedResident
          if (r == null) {
            return@Button
          }
          val finalAlasan = if (alasanPindah == "Lainnya" && customAlasan.isNotBlank()) customAlasan else alasanPindah
          val mutation = PopulationMutation(
            type = PopulationMutation.TYPE_PINDAH_KELUAR,
            tanggalMutasi = tanggalPindah,
            nik = r.nik,
            noKk = r.noKk,
            namaLengkap = r.namaLengkap,
            jenisKelamin = r.jenisKelamin,
            tempatLahir = r.tempatLahir,
            tanggalLahir = r.tanggalLahir,
            rt = r.rt,
            rw = r.rw,
            dusun = r.dusun,
            alamatLokal = r.alamatLengkap,
            alamatLuar = alamatTujuan,
            alasanPindah = finalAlasan,
            statusKeluarga = r.statusKeluarga,
            noHp = r.noHp,
            catatan = catatan
          )
          onSave(mutation, removePermanently)
        },
        enabled = selectedResident != null,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("process_pindah_keluar_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Proses Pindah Keluar & Kurangi Warga", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

/**
 * Tab 3: Riwayat Mutasi Warga (Pindah Masuk & Pindah Keluar)
 */
@Composable
private fun RiwayatMutasiTab(
  mutations: List<PopulationMutation>,
  isAdmin: Boolean,
  onDeleteMutation: (String) -> Unit
) {
  var filterType by remember { mutableStateOf("Semua") }
  var mutationToDelete by remember { mutableStateOf<PopulationMutation?>(null) }

  mutationToDelete?.let { mut ->
    AlertDialog(
      onDismissRequest = { mutationToDelete = null },
      icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
      title = { Text("Hapus Catatan Mutasi?") },
      text = { Text("Apakah Anda yakin ingin menghapus catatan mutasi warga ${mut.namaLengkap} (NIK: ${mut.nik})?") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteMutation(mut.id)
            mutationToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Hapus")
        }
      },
      dismissButton = {
        TextButton(onClick = { mutationToDelete = null }) {
          Text("Batal")
        }
      }
    )
  }

  val filteredMutations = remember(filterType, mutations) {
    when (filterType) {
      "Pindah Masuk" -> mutations.filter { it.isPindahMasuk }
      "Pindah Keluar" -> mutations.filter { it.isPindahKeluar }
      else -> mutations
    }
  }

  val totalMasuk = mutations.count { it.isPindahMasuk }
  val totalKeluar = mutations.count { it.isPindahKeluar }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Summary Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = EmeraldSecondaryContainer)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Pindah Masuk", style = MaterialTheme.typography.labelSmall, color = EmeraldSecondary, fontWeight = FontWeight.Bold)
            Text("+$totalMasuk Jiwa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EmeraldSecondary)
            Text("Warga baru bertambah", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
          }
        }
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = AmberAccentContainer)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Pindah Keluar", style = MaterialTheme.typography.labelSmall, color = AmberAccent, fontWeight = FontWeight.Bold)
            Text("-$totalKeluar Jiwa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AmberAccent)
            Text("Warga keluar berkurang", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
          }
        }
      }
    }

    // Filter Chips
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Semua", "Pindah Masuk", "Pindah Keluar").forEach { t ->
          FilterChip(
            selected = filterType == t,
            onClick = { filterType = t },
            label = { Text(t) }
          )
        }
      }
    }

    if (filteredMutations.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.SyncAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
            Text("Belum ada catatan mutasi $filterType.", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    } else {
      items(filteredMutations, key = { it.id.ifBlank { "${it.nik}_${it.createdAt}" } }) { mut ->
        MutationItemCard(
          mutation = mut,
          isAdmin = isAdmin,
          onDeleteClick = { mutationToDelete = mut }
        )
      }
    }
  }
}

@Composable
private fun MutationItemCard(
  mutation: PopulationMutation,
  isAdmin: Boolean,
  onDeleteClick: () -> Unit
) {
  val isMasuk = mutation.isPindahMasuk
  val badgeColor = if (isMasuk) EmeraldSecondary else AmberAccent
  val badgeBg = if (isMasuk) EmeraldSecondaryContainer else AmberAccentContainer
  val badgeText = if (isMasuk) "+1 PINDAH MASUK" else "-1 PINDAH KELUAR"

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = badgeBg
        ) {
          Text(
            text = badgeText,
            color = badgeColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(mutation.tanggalMutasi, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          if (isAdmin) {
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      Text(
        text = mutation.namaLengkap.uppercase(),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "NIK: ${mutation.nik} • ${mutation.rt}/${mutation.rw}, ${mutation.dusun}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

      if (isMasuk) {
        Text(
          text = "Asal Daerah: ${mutation.alamatLuar}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        Text(
          text = "Tujuan Pindah: ${mutation.alamatLuar}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Text(
        text = "Alasan: ${mutation.alasanPindah}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (mutation.petugas.isNotBlank()) {
        Text(
          text = "Dicatat oleh: ${mutation.petugas}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}
