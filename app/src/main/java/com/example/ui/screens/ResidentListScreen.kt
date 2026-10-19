package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Resident
import com.example.ui.components.DapendaTopAppBar
import com.example.ui.components.EmptyView
import com.example.ui.components.ResidentItemCard
import com.example.ui.components.RtSelectorChips
import com.example.ui.components.RwSelectorChips
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccentContainer
import com.example.ui.theme.TealPrimaryContainer
import com.example.ui.viewmodel.DapendaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentListScreen(
  viewModel: DapendaViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val allResidents by viewModel.allResidents.collectAsState()
  val filteredResidents by viewModel.filteredResidents.collectAsState()
  val selectedRw by viewModel.selectedRw.collectAsState()
  val selectedRt by viewModel.selectedRt.collectAsState()
  val availableRts by viewModel.availableRtList.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val genderFilter by viewModel.genderFilter.collectAsState()
  val statusTinggalFilter by viewModel.statusTinggalFilter.collectAsState()

  val countMap = allResidents.filter {
    if (selectedRw == "Semua RW" || selectedRw.isBlank()) true else it.rw.equals(selectedRw, ignoreCase = true)
  }.groupBy { it.rt }.mapValues { it.value.size }
  var residentToDelete by remember { mutableStateOf<Resident?>(null) }

  // Konfirmasi Hapus Data Warga Dialog
  val target = residentToDelete
  if (target != null) {
    AlertDialog(
      onDismissRequest = { residentToDelete = null },
      icon = {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(bottom = 4.dp)
        )
      },
      title = {
        Text("Hapus Data Warga?", fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Apakah Anda yakin ingin menghapus data kependudukan berikut dari basis data RT?")
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Text(
              text = "• Nama: ${target.namaLengkap}",
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "• NIK: ${target.nik}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "• Wilayah: ${target.rt}/${target.rw}, ${target.dusun}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "• Status: ${target.statusKeluarga} (${target.statusTinggal})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Text(
            text = "Data yang dihapus tidak dapat dipulihkan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteResident(target) {}
            residentToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_button")
        ) {
          Text("Ya, Hapus Data", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { residentToDelete = null },
          modifier = Modifier.testTag("cancel_delete_button")
        ) {
          Text("Batal")
        }
      }
    )
  }

  Scaffold(
    topBar = {
      DapendaTopAppBar(
        title = "Daftar Penduduk RT",
        subtitle = "Pencarian & Data Warga Terverifikasi",
        showBackButton = true,
        onBackClick = { viewModel.navigateBack() }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.initFormForAdd(selectedRt, selectedRw) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_list_add_resident")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Warga")
          Text("Tambah Warga", fontWeight = FontWeight.Bold)
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("search_resident_input"),
        placeholder = { Text("Cari NIK, KK, Nama, Pekerjaan, Alamat...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setSearchQuery("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian")
            }
          }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
      )

      // RW selector carousel
      RwSelectorChips(
        selectedRw = selectedRw,
        onRwSelected = { viewModel.setSelectedRw(it) },
        modifier = Modifier.padding(vertical = 2.dp)
      )

      // RT selector carousel
      RtSelectorChips(
        rtList = availableRts,
        selectedRt = selectedRt,
        onRtSelected = { viewModel.setSelectedRt(it) },
        residentCounts = countMap,
        modifier = Modifier.padding(vertical = 2.dp)
      )

      // Secondary Filters Row (Gender & Status Tinggal)
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        item {
          FilterChip(
            selected = genderFilter == "LAKI-LAKI",
            onClick = {
              viewModel.setGenderFilter(if (genderFilter == "LAKI-LAKI") null else "LAKI-LAKI")
            },
            label = { Text("Laki-laki") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimaryContainer)
          )
        }
        item {
          FilterChip(
            selected = genderFilter == "PEREMPUAN",
            onClick = {
              viewModel.setGenderFilter(if (genderFilter == "PEREMPUAN") null else "PEREMPUAN")
            },
            label = { Text("Perempuan") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimaryContainer)
          )
        }
        item {
          FilterChip(
            selected = statusTinggalFilter == "Warga Tetap",
            onClick = {
              viewModel.setStatusTinggalFilter(if (statusTinggalFilter == "Warga Tetap") null else "Warga Tetap")
            },
            label = { Text("Warga Tetap") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimaryContainer)
          )
        }
        item {
          FilterChip(
            selected = statusTinggalFilter == "Warga Kontrak / Sementara",
            onClick = {
              viewModel.setStatusTinggalFilter(if (statusTinggalFilter == "Warga Kontrak / Sementara") null else "Warga Kontrak / Sementara")
            },
            label = { Text("Kontrak / Musiman") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimaryContainer)
          )
        }
        item {
          FilterChip(
            selected = statusTinggalFilter == "Pindah Keluar",
            onClick = {
              viewModel.setStatusTinggalFilter(if (statusTinggalFilter == "Pindah Keluar") null else "Pindah Keluar")
            },
            label = { Text("Pindah Keluar") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AmberAccentContainer)
          )
        }
        if (genderFilter != null || statusTinggalFilter != null || searchQuery.isNotEmpty()) {
          item {
            FilterChip(
              selected = false,
              onClick = { viewModel.clearFilters() },
              label = { Text("Reset Filter") },
              leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.height(14.dp)) }
            )
          }
        }
      }

      // Count label
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Ditemukan ${filteredResidents.size} data warga",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (selectedRt != "Semua RT") {
          Text(
            text = "Khusus $selectedRt",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // List of Residents
      if (filteredResidents.isEmpty()) {
        EmptyView(
          message = "Tidak ada data penduduk yang cocok",
          subMessage = "Coba ubah kata kunci pencarian atau ganti filter RT.",
          modifier = Modifier.weight(1f)
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredResidents, key = { it.id }) { resident ->
            ResidentItemCard(
              resident = resident,
              onClick = { viewModel.navigateTo(Screen.ResidentDetail(resident.id)) },
              onDeleteClick = { residentToDelete = resident }
            )
          }
        }
      }
    }
  }
}
