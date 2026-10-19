package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Resident
import com.example.ui.components.DapendaTopAppBar
import com.example.ui.components.EmptyView
import com.example.ui.components.RtSelectorChips
import com.example.ui.components.RwSelectorChips
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberAccentContainer
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryContainer
import com.example.ui.viewmodel.DapendaViewModel

@Composable
fun FamilyGroupScreen(
  viewModel: DapendaViewModel,
  initialKk: String? = null,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val allResidents by viewModel.allResidents.collectAsState()
  val selectedRt by viewModel.selectedRt.collectAsState()
  val availableRts by viewModel.availableRtList.collectAsState()

  val activeList = if (selectedRt == "Semua RT" || selectedRt.isBlank()) {
    allResidents
  } else {
    allResidents.filter { it.rt == selectedRt }
  }

  val familiesMap = activeList.groupBy { it.noKk }
  val countMap = allResidents.groupBy { it.rt }.mapValues { it.value.size }
  val expandedState = remember { mutableStateMapOf<String, Boolean>() }
  var residentToDelete by remember { mutableStateOf<Resident?>(null) }

  val target = residentToDelete
  if (target != null) {
    AlertDialog(
      onDismissRequest = { residentToDelete = null },
      icon = {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )
      },
      title = { Text("Hapus Anggota Keluarga?", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Apakah Anda yakin ingin menghapus data anggota keluarga ini?")
          Text("• Nama: ${target.namaLengkap}", fontWeight = FontWeight.Bold)
          Text("• NIK: ${target.nik}", style = MaterialTheme.typography.bodySmall)
          Text("• Hubungan: ${target.statusKeluarga}", style = MaterialTheme.typography.bodySmall)
          Text(
            "Data akan dihapus permanen dari basis data.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteResident(target) {}
            residentToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Hapus Data")
        }
      },
      dismissButton = {
        TextButton(onClick = { residentToDelete = null }) {
          Text("Batal")
        }
      }
    )
  }

  Scaffold(
    topBar = {
      DapendaTopAppBar(
        title = "Daftar Kartu Keluarga (KK)",
        subtitle = "Pengelompokan Warga Berdasarkan No. KK",
        showBackButton = true,
        onBackClick = { viewModel.navigateBack() }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.initFormForAdd(selectedRt) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null)
          Text("Input KK Baru", fontWeight = FontWeight.Bold)
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      RtSelectorChips(
        rtList = availableRts,
        selectedRt = selectedRt,
        onRtSelected = { viewModel.setSelectedRt(it) },
        residentCounts = countMap,
        modifier = Modifier.padding(vertical = 6.dp)
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Total ${familiesMap.size} Kartu Keluarga",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${activeList.size} Jiwa Terdaftar",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )
      }

      if (familiesMap.isEmpty()) {
        EmptyView(
          message = "Belum ada Kartu Keluarga terdaftar di $selectedRt",
          subMessage = "Klik tombol di bawah untuk menambah data keluarga baru."
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(familiesMap.entries.toList(), key = { it.key }) { entry ->
            val noKk = entry.key
            val members = entry.value
            val isExpanded = expandedState[noKk] ?: (noKk == initialKk)

            val kepalaKeluarga = members.find {
              it.statusKeluarga.equals("Kepala Keluarga", ignoreCase = true)
            } ?: members.firstOrNull()

            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row (Click to Expand)
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedState[noKk] = !isExpanded }
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(46.dp)
                      .clip(CircleShape)
                      .background(AmberAccentContainer),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Home,
                      contentDescription = null,
                      tint = AmberAccent,
                      modifier = Modifier.size(24.dp)
                    )
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Text(
                        text = kepalaKeluarga?.namaLengkap ?: "Keluarga",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(MaterialTheme.colorScheme.surfaceVariant)
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = "${members.size} Jiwa",
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary
                        )
                      }
                    }

                    Text(
                      text = "No. KK: $noKk",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                      text = "${kepalaKeluarga?.rt ?: ""} • ${kepalaKeluarga?.alamatLengkap ?: ""}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                // Expandable Members Section
                AnimatedVisibility(visible = isExpanded) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                      .padding(14.dp)
                  ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                      text = "Anggota Keluarga Terdaftar:",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    members.forEach { member ->
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clip(RoundedCornerShape(8.dp))
                          .background(MaterialTheme.colorScheme.surface)
                          .clickable { viewModel.navigateTo(Screen.ResidentDetail(member.id)) }
                          .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Column {
                          Text(
                            text = member.namaLengkap,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                          )
                          Text(
                            text = "${member.statusKeluarga} • ${member.usia} th • NIK: ${member.nik}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                          Text(
                            text = "Detail >",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                          )
                          IconButton(
                            onClick = { residentToDelete = member },
                            modifier = Modifier.size(32.dp)
                          ) {
                            Icon(
                              imageVector = Icons.Default.DeleteOutline,
                              contentDescription = "Hapus Anggota",
                              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                              modifier = Modifier.size(18.dp)
                            )
                          }
                        }
                      }
                      Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                      onClick = {
                        kepalaKeluarga?.let { kk ->
                          viewModel.initFormForAddFamilyMember(
                            parentKk = noKk,
                            rt = kk.rt,
                            rw = kk.rw,
                            dusun = kk.dusun,
                            alamat = kk.alamatLengkap
                          )
                        }
                      },
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Icon(Icons.Default.PersonAdd, contentDescription = null)
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("+ Tambah Anggota di KK Ini")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
