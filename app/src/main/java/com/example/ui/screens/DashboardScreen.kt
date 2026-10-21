package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.BorderStroke
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.data.firebase.UserProfile
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Resident
import com.example.data.sample.SampleData
import com.example.ui.components.DapendaTopAppBar
import com.example.ui.components.DemographicBarRow
import com.example.ui.components.ResidentItemCard
import com.example.ui.components.RtSelectorChips
import com.example.ui.components.RwSelectorChips
import com.example.ui.components.StatMetricCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberAccentContainer
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.FemalePink
import com.example.ui.theme.MaleBlue
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryContainer
import com.example.ui.viewmodel.DapendaViewModel

@Composable
fun DashboardScreen(
  viewModel: DapendaViewModel,
  modifier: Modifier = Modifier
) {
  val allResidents by viewModel.allResidents.collectAsState()
  val selectedRw by viewModel.selectedRw.collectAsState()
  val selectedRt by viewModel.selectedRt.collectAsState()
  val availableRts by viewModel.availableRtList.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val userProfile by viewModel.userProfile.collectAsState()
  val syncInfo by viewModel.syncInfo.collectAsState()
  val operators by viewModel.operators.collectAsState()
  val isAdmin = userProfile?.isAdmin == true
  val isWebServerRunning by viewModel.isWebServerRunning.collectAsState()
  val webServerAddress by viewModel.webServerAddress.collectAsState()
  val context = LocalContext.current

  var showAdminChangePasswordDialog by remember { mutableStateOf(false) }
  var showOperatorDialog by remember { mutableStateOf(false) }
  var editingOperator by remember { mutableStateOf<UserProfile?>(null) }
  var operatorFilterRw by remember { mutableStateOf("Semua RW") }

  val activeList = allResidents.filter { resident ->
    val matchRw = if (selectedRw == "Semua RW" || selectedRw.isBlank()) true else resident.rw.equals(selectedRw, ignoreCase = true)
    val matchRt = if (selectedRt == "Semua RT" || selectedRt.isBlank()) true else resident.rt.equals(selectedRt, ignoreCase = true)
    val isNotMovedOut = !resident.statusTinggal.equals("Pindah Keluar", ignoreCase = true)
    matchRw && matchRt && isNotMovedOut
  }

  // Pre-calculate counts per RT for chip badges (excluding Pindah Keluar)
  val countMap = allResidents.filter {
    val matchesRw = if (selectedRw == "Semua RW" || selectedRw.isBlank()) true else it.rw.equals(selectedRw, ignoreCase = true)
    val isNotMovedOut = !it.statusTinggal.equals("Pindah Keluar", ignoreCase = true)
    matchesRw && isNotMovedOut
  }.groupBy { it.rt }.mapValues { it.value.size }

  val totalJiwa = activeList.size
  val totalKk = activeList.map { it.noKk }.distinct().size
  val totalLaki = activeList.count { it.jenisKelamin.equals("LAKI-LAKI", ignoreCase = true) }
  val totalPerempuan = activeList.count { it.jenisKelamin.equals("PEREMPUAN", ignoreCase = true) }
  val totalBalita = activeList.count { it.usia <= 5 }
  val totalLansia = activeList.count { it.usia >= 60 }
  val totalBansos = activeList.count { it.bantuanSosial }
  var selectedBerandaPhoto by remember { mutableStateOf<Triple<String, String, Int>?>(null) }

  if (showAdminChangePasswordDialog) {
    AdminChangePasswordDialog(
      onDismiss = { showAdminChangePasswordDialog = false },
      onConfirm = { oldPass, newPass ->
        viewModel.changeAdminPassword(oldPass, newPass) {
          showAdminChangePasswordDialog = false
        }
      }
    )
  }

  if (showOperatorDialog) {
    OperatorEditDialog(
      operator = editingOperator,
      onDismiss = {
        showOperatorDialog = false
        editingOperator = null
      },
      onSave = { op ->
        viewModel.saveOperator(op) {
          showOperatorDialog = false
          editingOperator = null
        }
      }
    )
  }

  selectedBerandaPhoto?.let { (title, desc, imgRes) ->
    Dialog(onDismissRequest = { selectedBerandaPhoto = null }) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { selectedBerandaPhoto = null }) {
              Icon(Icons.Default.Close, contentDescription = "Tutup")
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black.copy(alpha = 0.04f))
          ) {
            Image(
              painter = painterResource(id = imgRes),
              contentDescription = title,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Fit
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(14.dp))
          Button(onClick = { selectedBerandaPhoto = null }, modifier = Modifier.fillMaxWidth()) {
            Text("Tutup Tampilan")
          }
        }
      }
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // Background Image: Serene Village Canal and Rice Fields (Desa Benda)
    Image(
      painter = painterResource(id = R.drawable.img_beranda_bg),
      contentDescription = "Background Beranda Desa Benda",
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop
    )
    // Subtle gradient tint for optimal contrast and text readability
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
              MaterialTheme.colorScheme.background.copy(alpha = 0.88f),
              MaterialTheme.colorScheme.background.copy(alpha = 0.94f)
            )
          )
        )
    )

    Scaffold(
      containerColor = Color.Transparent,
      topBar = {
        DapendaTopAppBar(
          title = "SIDAPENDA",
          subtitle = "Data Penduduk Desa Benda",
          containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
          actions = {
            IconButton(
              onClick = { viewModel.navigateTo(Screen.ResidentList) },
              modifier = Modifier.testTag("dashboard_search_button")
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari Warga",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
            IconButton(
              onClick = { viewModel.navigateTo(Screen.ExportBackup) },
              modifier = Modifier.testTag("dashboard_export_button")
            ) {
              Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Ekspor Data",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
            IconButton(
              onClick = { viewModel.navigateTo(Screen.AuditLog) },
              modifier = Modifier.testTag("dashboard_audit_button")
            ) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Catatan Aktivitas",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
            IconButton(
              onClick = { viewModel.signOut() },
              modifier = Modifier.testTag("dashboard_logout_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Keluar Akun",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        )
      },
      floatingActionButton = {
        FloatingActionButton(
          onClick = { viewModel.initFormForAdd(selectedRt, selectedRw) },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = Color.White,
          modifier = Modifier.testTag("fab_add_resident")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Input Data Warga")
            Text("Input Warga", fontWeight = FontWeight.Bold)
          }
        }
      }
    ) { paddingValues ->
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
      // Akun Petugas & Status Sinkronisasi Cloud
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              val isAdmin = userProfile?.isAdmin == true
              Surface(
                shape = CircleShape,
                color = if (isAdmin) TealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
              ) {
                Icon(
                  imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.AccountCircle,
                  contentDescription = null,
                  tint = if (isAdmin) TealPrimary else MaterialTheme.colorScheme.secondary,
                  modifier = Modifier
                    .padding(6.dp)
                    .size(20.dp)
                )
              }

              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = if (isAdmin) "Administrator Desa" else "${userProfile?.role?.replaceFirstChar { it.uppercase() } ?: "Operator"} ${userProfile?.rt ?: ""}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (syncInfo.isOffline) AmberAccent.copy(alpha = 0.15f) else EmeraldSecondary.copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = if (syncInfo.isOffline) "OFFLINE CACHE" else "CLOUD LIVE",
                      color = if (syncInfo.isOffline) AmberAccent else EmeraldSecondary,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.ExtraBold,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Text(
                  text = userProfile?.let { "${it.displayName} • ${it.rt}/${it.rw}" }
                    ?: currentUser?.displayName
                    ?: currentUser?.email
                    ?: "Petugas Resmi Desa",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.clickable { viewModel.refreshSync() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = if (syncInfo.isOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                  contentDescription = null,
                  tint = if (syncInfo.isOffline) AmberAccent else EmeraldSecondary,
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = if (syncInfo.isOffline) "Tersimpan Lokal" else "Sinkron Otomatis",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            if (isAdmin) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = TealPrimary.copy(alpha = 0.12f),
                modifier = Modifier
                  .clickable { showAdminChangePasswordDialog = true }
                  .testTag("btn_header_ubah_sandi")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(13.dp)
                  )
                  Text(
                    text = "Ubah Sandi",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                  )
                }
              }
            }
          }
        }
      }

      // Hero Header Card with village art
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            Image(
              painter = painterResource(id = R.drawable.img_hero_banner),
              contentDescription = "Desa Benda",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
            // Gradient overlay
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(
                      Color.Transparent,
                      Color.Black.copy(alpha = 0.75f)
                    )
                  )
                )
            )
            Column(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
            ) {
              Text(
                text = if (selectedRw == "Semua RW" && selectedRt == "Semua RT") "Pusat Data Warga Desa" else if (selectedRt == "Semua RT") "Administrasi $selectedRw" else "$selectedRt / $selectedRw",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "5 RW • 19 RT • Wilayah Desa Benda",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
              )
            }
          }
        }
      }

      // RW & RT Filter Carousels
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          // RW selector
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Pilih Rukun Warga (RW):",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "5 RW (19 RT)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
          RwSelectorChips(
            selectedRw = selectedRw,
            onRwSelected = { viewModel.setSelectedRw(it) }
          )

          // RT selector
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Pilih Rukun Tetangga (RT)${if (selectedRw != "Semua RW") " di $selectedRw" else ""}:",
              style = MaterialTheme.typography.labelLarge,
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
          RtSelectorChips(
            rtList = availableRts,
            selectedRt = selectedRt,
            onRtSelected = { viewModel.setSelectedRt(it) },
            residentCounts = countMap
          )
        }
      }

      // 4-Card Metric Grid
      item {
        Column(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StatMetricCard(
              title = "Total Penduduk",
              value = "$totalJiwa",
              subtext = "Jiwa di $selectedRt",
              icon = Icons.Default.Group,
              accentColor = TealPrimary,
              backgroundColor = TealPrimaryContainer,
              modifier = Modifier.weight(1f),
              onClick = { viewModel.navigateTo(Screen.ResidentList) }
            )
            StatMetricCard(
              title = "Kartu Keluarga",
              value = "$totalKk",
              subtext = "Kepala Keluarga",
              icon = Icons.Default.Home,
              accentColor = AmberAccent,
              backgroundColor = AmberAccentContainer,
              modifier = Modifier.weight(1f),
              onClick = { viewModel.navigateTo(Screen.FamilyGroup()) }
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StatMetricCard(
              title = "Laki-Laki",
              value = "$totalLaki",
              subtext = if (totalJiwa > 0) "${(totalLaki * 100) / totalJiwa}%" else "0%",
              icon = Icons.Default.Male,
              accentColor = MaleBlue,
              backgroundColor = Color(0xFFEFF6FF),
              modifier = Modifier.weight(1f)
            )
            StatMetricCard(
              title = "Perempuan",
              value = "$totalPerempuan",
              subtext = if (totalJiwa > 0) "${(totalPerempuan * 100) / totalJiwa}%" else "0%",
              icon = Icons.Default.Female,
              accentColor = FemalePink,
              backgroundColor = Color(0xFFFDF2F8),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // Quick Nav Buttons
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            QuickActionButton(
              icon = Icons.Default.Group,
              label = "Daftar Warga",
              onClick = { viewModel.navigateTo(Screen.ResidentList) }
            )
            QuickActionButton(
              icon = Icons.Default.HomeWork,
              label = "Daftar KK",
              onClick = { viewModel.navigateTo(Screen.FamilyGroup()) }
            )
            QuickActionButton(
              icon = Icons.Default.BarChart,
              label = "Statistik RT",
              onClick = { viewModel.navigateTo(Screen.Statistics) }
            )
            QuickActionButton(
              icon = Icons.Default.SyncAlt,
              label = "Ekspor / Impor",
              onClick = { viewModel.navigateTo(Screen.ExportBackup) }
            )
          }
        }
      }

      // Mutasi Penduduk Quick Banner Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Surface(
                  shape = CircleShape,
                  color = TealPrimary.copy(alpha = 0.12f)
                ) {
                  Icon(
                    imageVector = Icons.Default.SyncAlt,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier
                      .padding(8.dp)
                      .size(22.dp)
                  )
                }
                Column {
                  Text(
                    text = "Layanan Mutasi & Peristiwa Warga",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Pindah Masuk/Keluar • Kelahiran (+ Jiwa) • Kematian (- Jiwa)",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              TextButton(onClick = { viewModel.navigateTo(Screen.PopulationMutation(initialTab = 4)) }) {
                Text("Riwayat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 1: Pindah Masuk & Pindah Keluar
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { viewModel.navigateTo(Screen.PopulationMutation(initialTab = 0)) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_menu_pindah_masuk"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp)
              ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pindah Masuk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = { viewModel.navigateTo(Screen.PopulationMutation(initialTab = 1)) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_menu_pindah_keluar"),
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pindah Keluar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Kelahiran (+ Jiwa) & Kematian (- Jiwa)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { viewModel.navigateTo(Screen.PopulationMutation(initialTab = 2)) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_menu_kelahiran"),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp)
              ) {
                Icon(Icons.Default.ChildCare, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kelahiran (+)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = { viewModel.navigateTo(Screen.PopulationMutation(initialTab = 3)) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_menu_kematian"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp)
              ) {
                Icon(Icons.Default.Elderly, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kematian (-)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // ================= SECTION DATA OPERATOR & WHATSAPP (KHUSUS ADMIN) =================
      if (isAdmin) {
        item {
          AdminOperatorsSection(
            operators = operators,
            selectedRw = operatorFilterRw,
            onRwSelected = { operatorFilterRw = it },
            onChangePasswordClick = { showAdminChangePasswordDialog = true },
            onAddOperatorClick = {
              editingOperator = null
              showOperatorDialog = true
            },
            onEditOperatorClick = { op ->
              editingOperator = op
              showOperatorDialog = true
            },
            onDeleteOperatorClick = { op ->
              viewModel.deleteOperator(op.uid) {}
            },
            onChatWhatsApp = { op ->
              openWhatsAppChat(context, op.phone, op.displayName)
            },
            onCallPhone = { op ->
              dialPhoneNumber(context, op.phone)
            }
          )
        }
      }

      // ================= SECTION SIDAPENDA WEB PORTAL ONLINE =================
      item {
        WebPortalCard(
          isServerRunning = isWebServerRunning,
          serverAddress = webServerAddress,
          onToggleServer = { viewModel.toggleWebServer() },
          onOpenBrowser = {
            val url = if (webServerAddress.isNotBlank()) webServerAddress else "http://localhost:8080"
            try {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
              context.startActivity(intent)
            } catch (e: Exception) {
              viewModel.showToast("Gagal membuka browser: ${e.localizedMessage}")
            }
          },
          onCopyUrl = {
            val url = if (webServerAddress.isNotBlank()) webServerAddress else "http://localhost:8080"
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("URL Web SIDAPENDA", url))
            viewModel.showToast("Tautan web berhasil disalin: $url")
          },
          onShareHtml = {
            val file = viewModel.exportWebPortalHtml()
            if (file != null) {
              val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
              val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "SIDAPENDA Web Portal - Data Kependudukan Desa Benda")
                putExtra(Intent.EXTRA_TEXT, "Buka file HTML ini di komputer/laptop untuk membuka Portal Web SIDAPENDA Online.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
              }
              context.startActivity(Intent.createChooser(shareIntent, "Bagikan Web Portal SIDAPENDA"))
            } else {
              viewModel.showToast("Gagal menyiapkan file web portal")
            }
          }
        )
      }

      // Galeri Foto & Dokumentasi Desa Benda
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                  imageVector = Icons.Default.Collections,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(22.dp)
                )
                Column {
                  Text(
                    text = "Identitas Wilayah Resmi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Pemerintah Kabupaten Tangerang • Desa Benda",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
              ) {
                Text(
                  text = "Dokumen Resmi",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lambang Daerah Resmi Kabupaten Tangerang Card
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                  selectedBerandaPhoto = Triple(
                    "Lambang Resmi Kabupaten Tangerang",
                    "Lambang daerah Kabupaten Tangerang dengan semboyan 'Satya Karya Kerta Raharja' (Setia dan Berkarya Menuju Kemakmuran). Wilayah administrasi induk yang menaungi Kecamatan Sukamulya dan Desa Benda.",
                    R.drawable.foto_logo_tangerang
                  )
                },
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
              Column {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                ) {
                  Image(
                    painter = painterResource(id = R.drawable.foto_logo_tangerang),
                    contentDescription = "Lambang Kabupaten Tangerang",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                  Surface(
                    modifier = Modifier
                      .align(Alignment.TopStart)
                      .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                  ) {
                    Text(
                      text = "Satya Karya Kerta Raharja",
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                      color = Color.White,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  Surface(
                    modifier = Modifier
                      .align(Alignment.BottomEnd)
                      .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                      Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                      Text("Perbesar", color = Color.White, fontSize = 10.sp)
                    }
                  }
                }
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "Pemerintah Daerah Kabupaten Tangerang",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "Lambang resmi daerah Satya Karya Kerta Raharja yang menaungi wilayah administrasi pemerintahan Desa Benda, Kecamatan Sukamulya.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }
        }
      }

      // Demographics Age Breakdown
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Komposisi Usia Penduduk",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Detail Statistik >",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { viewModel.navigateTo(Screen.Statistics) }
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val balitaPct = if (totalJiwa > 0) (totalBalita.toFloat() / totalJiwa) else 0f
            val lansiaPct = if (totalJiwa > 0) (totalLansia.toFloat() / totalJiwa) else 0f
            val bansosPct = if (totalJiwa > 0) (totalBansos.toFloat() / totalJiwa) else 0f

            DemographicBarRow(
              label = "Balita (0 - 5 tahun)",
              count = totalBalita,
              fraction = balitaPct,
              color = EmeraldSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            DemographicBarRow(
              label = "Lansia (60+ tahun)",
              count = totalLansia,
              fraction = lansiaPct,
              color = AmberAccent
            )
            Spacer(modifier = Modifier.height(10.dp))
            DemographicBarRow(
              label = "Penerima Bantuan Sosial",
              count = totalBansos,
              fraction = bansosPct,
              color = TealPrimary
            )
          }
        }
      }

      // Recent 5 Residents Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Data Warga Terbaru ($selectedRt)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Lihat Semua (${activeList.size})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { viewModel.navigateTo(Screen.ResidentList) }
          )
        }
      }

      // Recent Residents List
      val recentResidents = activeList.take(5)
      if (recentResidents.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Belum ada data penduduk di $selectedRt",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = { viewModel.initFormForAdd(selectedRt) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Input Warga Sekarang")
              }
            }
          }
        }
      } else {
        items(recentResidents) { resident ->
          Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            ResidentItemCard(
              resident = resident,
              onClick = { viewModel.navigateTo(Screen.ResidentDetail(resident.id)) }
            )
          }
        }
      }
    }
  }
}
}

@Composable
fun QuickActionButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(22.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun VillagePhotoCard(
  imageRes: Int,
  title: String,
  tag: String,
  description: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .width(260.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(135.dp)
      ) {
        Image(
          painter = painterResource(id = imageRes),
          contentDescription = title,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
        Surface(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp),
          shape = RoundedCornerShape(6.dp),
          color = Color.Black.copy(alpha = 0.65f)
        ) {
          Text(
            text = tag,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Surface(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp),
          shape = RoundedCornerShape(6.dp),
          color = Color.Black.copy(alpha = 0.65f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text("Perbesar", color = Color.White, fontSize = 10.sp)
          }
        }
      }
      Column(modifier = Modifier.padding(10.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp,
          maxLines = 2,
          lineHeight = 15.sp
        )
      }
    }
  }
}

/**
 * Helper untuk membuka WhatsApp secara langsung ke nomor operator.
 */
fun openWhatsAppChat(context: Context, phoneNumber: String, operatorName: String) {
  try {
    val clean = phoneNumber.replace(Regex("[^0-9]"), "")
    val intl = if (clean.startsWith("0")) "62" + clean.substring(1) else clean
    val message = "Halo Petugas $operatorName (Desa Benda), salam dari Administrator Desa."
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$intl&text=${Uri.encode(message)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
  } catch (e: Exception) {
    Toast.makeText(context, "Tidak dapat membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
  }
}

/**
 * Helper untuk melakukan panggilan telepon langsung ke nomor operator.
 */
fun dialPhoneNumber(context: Context, phoneNumber: String) {
  try {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
    context.startActivity(intent)
  } catch (e: Exception) {
    Toast.makeText(context, "Tidak dapat melakukan panggilan: ${e.message}", Toast.LENGTH_SHORT).show()
  }
}

/**
 * Komponen Khusus Akun Admin:
 * Menampilkan direktori seluruh operator RT & RW se-Desa Benda beserta kontak WhatsApp masing-masing,
 * serta tombol ubah password admin dan tambah operator baru.
 */
@Composable
fun AdminOperatorsSection(
  operators: List<UserProfile>,
  selectedRw: String,
  onRwSelected: (String) -> Unit,
  onChangePasswordClick: () -> Unit,
  onAddOperatorClick: () -> Unit,
  onEditOperatorClick: (UserProfile) -> Unit,
  onDeleteOperatorClick: (UserProfile) -> Unit,
  onChatWhatsApp: (UserProfile) -> Unit,
  onCallPhone: (UserProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  val rwOptions = listOf("Semua RW", "RW 01", "RW 02", "RW 03", "RW 04", "RW 05")
  val filteredList = operators.filter {
    if (selectedRw == "Semua RW" || selectedRw.isBlank()) true else it.rw.equals(selectedRw, ignoreCase = true)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary.copy(alpha = 0.4f))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header Section
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0xFF25D366).copy(alpha = 0.15f)
          ) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = null,
              tint = Color(0xFF1EBE5D),
              modifier = Modifier
                .padding(8.dp)
                .size(22.dp)
            )
          }
          Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Data Operator & WhatsApp",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = TealPrimary.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "ADMIN ONLY",
                  color = TealPrimary,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.ExtraBold,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "Data Petugas RT/RW & Nomor WhatsApp Resmi",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Buttons (Ubah Password Admin & Tambah Operator)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onChangePasswordClick,
          modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .testTag("admin_change_password_button"),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
          Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp), tint = TealPrimary)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Ubah Sandi Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
        }

        Button(
          onClick = onAddOperatorClick,
          modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .testTag("admin_add_operator_button"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Tambah Operator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // RW Filter Tabs
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(rwOptions) { rw ->
          val isSelected = selectedRw == rw
          val count = if (rw == "Semua RW") operators.size else operators.count { it.rw.equals(rw, ignoreCase = true) }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.clickable { onRwSelected(rw) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = rw,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
              Surface(
                shape = CircleShape,
                color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
              ) {
                Text(
                  text = "$count",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Daftar Operator
      if (operators.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = TealPrimary.copy(alpha = 0.12f),
              modifier = Modifier.size(48.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.PersonAdd,
                  contentDescription = null,
                  tint = TealPrimary,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
            Text(
              text = "Belum Ada Operator Terdaftar",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Daftar operator kosong. Data operator baru akan otomatis terisi dan tampil di sini ketika ada petugas RT/RW yang mendaftarkan akun di sistem atau ditambahkan oleh Administrator.",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else if (filteredList.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
              text = "Belum ada operator terdaftar di $selectedRw",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          filteredList.forEach { op ->
            OperatorItemCard(
              operator = op,
              onChatWhatsApp = { onChatWhatsApp(op) },
              onCallPhone = { onCallPhone(op) },
              onEdit = { onEditOperatorClick(op) },
              onDelete = { onDeleteOperatorClick(op) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun OperatorItemCard(
  operator: UserProfile,
  onChatWhatsApp: () -> Unit,
  onCallPhone: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = TealPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(38.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = operator.displayName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
              )
            }
          }
          Column {
            Text(
              text = operator.displayName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = TealPrimary.copy(alpha = 0.12f)
              ) {
                Text(
                  text = "${operator.rt} / ${operator.rw}",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = TealPrimary,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
              Text(
                text = "• ${operator.role.ifBlank { "Operator RT" }}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Row {
          IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Operator", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Hapus Operator", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Box WhatsApp
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF25D366).copy(alpha = 0.10f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = "WhatsApp",
              tint = Color(0xFF1EBE5D),
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "WhatsApp: ${operator.phone}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F5132),
              fontSize = 12.sp
            )
          }

          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF25D366).copy(alpha = 0.2f)
          ) {
            Text(
              text = "Terdaftar",
              color = Color(0xFF0F5132),
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Direct Action Buttons: Chat WA & Panggil
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onChatWhatsApp,
          modifier = Modifier
            .weight(1.2f)
            .height(36.dp),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
          contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
          Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Chat WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        OutlinedButton(
          onClick = onCallPhone,
          modifier = Modifier
            .weight(0.8f)
            .height(36.dp),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
          Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Telepon", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

/**
 * Dialog Ubah Password Akun Admin.
 * Memenuhi user request: "gunakan untuk akun admin User : admin Password : Benda15631 dan password bisa dirubah."
 */
@Composable
fun AdminChangePasswordDialog(
  onDismiss: () -> Unit,
  onConfirm: (oldPass: String, newPass: String) -> Unit
) {
  var oldPass by remember { mutableStateOf("") }
  var newPass by remember { mutableStateOf("") }
  var confirmPass by remember { mutableStateOf("") }
  var showPass by remember { mutableStateOf(false) }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary)
        Text("Ubah Password Akun Admin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "User Admin : admin\nPassword bawaan awal adalah Benda15631. Anda dapat merubahnya menjadi kata sandi baru yang aman.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (errorText != null) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = errorText ?: "",
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              modifier = Modifier.padding(8.dp)
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedTextField(
          value = oldPass,
          onValueChange = { oldPass = it; errorText = null },
          label = { Text("Password Lama (Awal: Benda15631)") },
          placeholder = { Text("Masukkan password lama") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = newPass,
          onValueChange = { newPass = it; errorText = null },
          label = { Text("Password Baru") },
          placeholder = { Text("Minimal 5 karakter") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = confirmPass,
          onValueChange = { confirmPass = it; errorText = null },
          label = { Text("Konfirmasi Password Baru") },
          placeholder = { Text("Ulangi password baru") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { showPass = !showPass }
        ) {
          Icon(
            imageVector = if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (showPass) "Sembunyikan password" else "Tampilkan password",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (oldPass.isBlank()) {
            errorText = "Password lama wajib diisi! (Bawaan: Benda15631)"
            return@Button
          }
          if (newPass.length < 5) {
            errorText = "Password baru minimal 5 karakter!"
            return@Button
          }
          if (newPass != confirmPass) {
            errorText = "Konfirmasi password baru tidak cocok!"
            return@Button
          }
          onConfirm(oldPass, newPass)
        },
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Simpan Password Baru", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

/**
 * Dialog Tambah / Edit Operator RT & RW berikut Nomor WhatsApp.
 */
@Composable
fun OperatorEditDialog(
  operator: UserProfile?,
  onDismiss: () -> Unit,
  onSave: (UserProfile) -> Unit
) {
  var name by remember { mutableStateOf(operator?.displayName ?: "") }
  var emailOrUser by remember { mutableStateOf(operator?.email ?: "") }
  var phone by remember { mutableStateOf(operator?.phone ?: "") }
  var selectedRw by remember { mutableStateOf(operator?.rw?.ifBlank { "RW 01" } ?: "RW 01") }
  var selectedRt by remember { mutableStateOf(operator?.rt?.ifBlank { "RT 01" } ?: "RT 01") }
  var role by remember { mutableStateOf(operator?.role?.ifBlank { "Operator RT" } ?: "Operator RT") }
  var password by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  val rwOptions = listOf("RW 01", "RW 02", "RW 03", "RW 04", "RW 05")
  val rtOptions = remember(selectedRw) { SampleData.getRtListForRw(selectedRw) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (operator == null) "Daftarkan Operator Baru" else "Edit Data Operator",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Data operator yang didaftarkan akan dapat login dan membuka aplikasi.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )

        if (errorText != null) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = errorText ?: "",
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.padding(8.dp)
            )
          }
        }

        OutlinedTextField(
          value = name,
          onValueChange = { name = it; errorText = null },
          label = { Text("Nama Lengkap Petugas *") },
          placeholder = { Text("Contoh: Ahmad Fauzi") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it; errorText = null },
          label = { Text("Nomor WhatsApp *") },
          placeholder = { Text("Contoh: 081287654321") },
          singleLine = true,
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF25D366)) },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = emailOrUser,
          onValueChange = { emailOrUser = it; errorText = null },
          label = { Text("Username / Email Login *") },
          placeholder = { Text("Contoh: rt01rw01 atau email") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Pilihan RW
        Column {
          Text("Wilayah Rukun Warga (RW):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(rwOptions) { rw ->
              val isSel = selectedRw == rw
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSel) TealPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clickable {
                  selectedRw = rw
                  val rts = SampleData.getRtListForRw(rw)
                  if (!rts.contains(selectedRt)) {
                    selectedRt = rts.firstOrNull() ?: "RT 01"
                  }
                }
              ) {
                Text(
                  text = rw,
                  color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                  fontSize = 11.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        // Pilihan RT
        Column {
          Text("Wilayah Rukun Tetangga (RT) di $selectedRw:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(rtOptions) { rt ->
              val isSel = selectedRt == rt
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSel) TealPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clickable { selectedRt = rt }
              ) {
                Text(
                  text = rt,
                  color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                  fontSize = 11.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        // Jabatan
        OutlinedTextField(
          value = role,
          onValueChange = { role = it },
          label = { Text("Jabatan / Peran") },
          placeholder = { Text("Contoh: Operator RT, Ketua RT, dsb") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Kata Sandi (jika baru atau ingin ubah)
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text(if (operator == null) "Kata Sandi Akun *" else "Kata Sandi Baru (Kosongkan jika tidak diganti)") },
          placeholder = { Text("Minimal 5 karakter (Bawaan: Benda15631)") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank()) {
            errorText = "Nama petugas wajib diisi!"
            return@Button
          }
          if (phone.isBlank()) {
            errorText = "Nomor WhatsApp wajib diisi!"
            return@Button
          }
          if (emailOrUser.isBlank()) {
            errorText = "Username / Email wajib diisi!"
            return@Button
          }
          if (operator == null && password.length < 5) {
            errorText = "Kata sandi akun minimal 5 karakter!"
            return@Button
          }

          val passHash = if (password.isNotBlank()) {
            com.example.util.SessionManager.hashPassword(password)
          } else {
            operator?.passwordHash?.ifBlank { com.example.util.SessionManager.hashPassword("Benda15631") }
              ?: com.example.util.SessionManager.hashPassword("Benda15631")
          }

          val uid = operator?.uid?.ifBlank { "op_${System.currentTimeMillis()}" } ?: "op_${System.currentTimeMillis()}"

          val savedOp = UserProfile(
            uid = uid,
            email = emailOrUser.trim().lowercase(),
            displayName = name.trim(),
            role = role.ifBlank { "Operator RT" },
            rt = selectedRt,
            rw = selectedRw,
            phone = phone.trim(),
            passwordHash = passHash,
            createdAt = operator?.createdAt ?: System.currentTimeMillis()
          )

          onSave(savedOp)
        },
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Simpan Operator", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

/**
 * Komponen Akses Web Portal Online SIDAPENDA:
 * Menampilkan kontrol server web embedded lokal, tautan URL live, tombol buka browser,
 * salin link, bagikan file HTML mandiri, dan petunjuk akses komputer/laptop.
 */
@Composable
fun WebPortalCard(
  isServerRunning: Boolean,
  serverAddress: String,
  onToggleServer: () -> Unit,
  onOpenBrowser: () -> Unit,
  onCopyUrl: () -> Unit,
  onShareHtml: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showHelpDialog by remember { mutableStateOf(false) }

  if (showHelpDialog) {
    AlertDialog(
      onDismissRequest = { showHelpDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text("Petunjuk SIDAPENDA Web Online", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "Aplikasi SIDAPENDA kini dapat diakses melalui browser komputer, laptop, dan tablet baik secara online maupun offline lokal.",
            style = MaterialTheme.typography.bodySmall
          )
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("1. Melalui Jaringan Wi-Fi / Hotspot:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
              Text("• Hubungkan HP dan Komputer ke Wi-Fi / Hotspot yang sama.", style = MaterialTheme.typography.bodySmall)
              Text("• Tekan tombol 'Nyalakan Server' pada kartu di bawah.", style = MaterialTheme.typography.bodySmall)
              Text("• Buka browser di laptop (Chrome/Edge/Firefox) lalu ketik alamat IP yang muncul (contoh: http://192.168.1.15:8080).", style = MaterialTheme.typography.bodySmall)
            }
          }
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("2. Menggunakan File Web Mandiri (HTML):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
              Text("• Tekan tombol 'Bagikan File Web (HTML)'.", style = MaterialTheme.typography.bodySmall)
              Text("• Kirim via WhatsApp Web / Email / Flashdisk ke komputer kantor desa.", style = MaterialTheme.typography.bodySmall)
              Text("• Klik dua kali file 'dapenda_web_portal.html' di komputer untuk membuka sistem tanpa perlu instalasi!", style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      },
      confirmButton = {
        Button(onClick = { showHelpDialog = false }) {
          Text("Saya Mengerti")
        }
      }
    )
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isServerRunning) androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = null,
              tint = if (isServerRunning) androidx.compose.ui.graphics.Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(24.dp)
            )
          }
          Column {
            Text(
              text = "SIDAPENDA Web Online",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Akses Browser Komputer & Laptop",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = { showHelpDialog = true }) {
          Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = "Petunjuk Akses Web",
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Status Bar
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isServerRunning) androidx.compose.ui.graphics.Color(0xFFECFDF5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
          1.dp,
          if (isServerRunning) androidx.compose.ui.graphics.Color(0xFFA7F3D0) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isServerRunning) androidx.compose.ui.graphics.Color(0xFF10B981) else androidx.compose.ui.graphics.Color.Gray)
            )
            Column {
              Text(
                text = if (isServerRunning) "Server Web Aktif (Live)" else "Server Web Offline",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isServerRunning) androidx.compose.ui.graphics.Color(0xFF065F46) else MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (isServerRunning && serverAddress.isNotBlank()) {
                Text(
                  text = serverAddress,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = androidx.compose.ui.graphics.Color(0xFF047857)
                )
              } else {
                Text(
                  text = "Nyalakan server untuk koneksi komputer",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Button(
            onClick = onToggleServer,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isServerRunning) androidx.compose.ui.graphics.Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PowerSettingsNew,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isServerRunning) "Matikan" else "Nyalakan",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onOpenBrowser,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(vertical = 10.dp)
        ) {
          Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Buka Browser", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
          onClick = onCopyUrl,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(vertical = 10.dp)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Salin Tautan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
          onClick = onShareHtml,
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(vertical = 10.dp)
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Bagikan Web", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}



