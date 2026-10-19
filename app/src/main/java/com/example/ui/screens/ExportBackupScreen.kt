package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.repository.BackupPreview
import com.example.data.repository.ImportResult
import com.example.ui.components.DapendaTopAppBar
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryContainer
import com.example.ui.viewmodel.DapendaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ExportBackupScreen(
  viewModel: DapendaViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val allResidents by viewModel.allResidents.collectAsState()

  var showResetDialog by remember { mutableStateOf(false) }
  var showClearDialog by remember { mutableStateOf(false) }

  // Export settings
  var includePhotosInExport by remember { mutableStateOf(true) }
  var isExporting by remember { mutableStateOf(false) }

  // Import state & dialogs
  var pendingImportJson by remember { mutableStateOf<String?>(null) }
  var backupPreview by remember { mutableStateOf<BackupPreview?>(null) }
  var importMergeStrategy by remember { mutableStateOf(true) } // true: Merge, false: Overwrite
  var isImporting by remember { mutableStateOf(false) }
  var importResultDialog by remember { mutableStateOf<ImportResult?>(null) }

  // Document Creator for saving JSON backup file locally
  val createDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri ->
    if (uri != null) {
      scope.launch(Dispatchers.IO) {
        try {
          val json = viewModel.exportJsonContent(includePhotosInExport)
          context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(json.toByteArray(Charsets.UTF_8))
            outputStream.flush()
          }
          withContext(Dispatchers.Main) {
            viewModel.showToast("File cadangan berhasil disimpan ke memori HP!")
          }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            viewModel.showToast("Gagal menyimpan file: ${e.localizedMessage}")
          }
        }
      }
    }
  }

  // Document Picker for importing backup file from another phone
  val openDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    if (uri != null) {
      scope.launch(Dispatchers.IO) {
        try {
          val inputStream = context.contentResolver.openInputStream(uri)
          val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
          if (content.isNotBlank()) {
            val preview = viewModel.parseBackupPreview(content)
            withContext(Dispatchers.Main) {
              pendingImportJson = content
              backupPreview = preview
            }
          } else {
            withContext(Dispatchers.Main) {
              viewModel.showToast("File yang dipilih kosong.")
            }
          }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            viewModel.showToast("Gagal membuka file: ${e.localizedMessage}")
          }
        }
      }
    }
  }

  // Import Preview & Confirmation Dialog
  backupPreview?.let { preview ->
    AlertDialog(
      onDismissRequest = {
        if (!isImporting) {
          backupPreview = null
          pendingImportJson = null
        }
      },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text("Konfirmasi Impor Data", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (!preview.isValid) {
            Text(
              text = "File cadangan tidak valid atau rusak:\n${preview.errorMessage}",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodyMedium
            )
          } else {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "File Cadangan dari HP Pengirim:",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "• Wilayah: ${preview.village}",
                  fontWeight = FontWeight.SemiBold,
                  style = MaterialTheme.typography.bodyMedium
                )
                Text(
                  text = "• Jumlah Penduduk: ${preview.totalRecords} Jiwa",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "• Berkas Foto Tersedia: ${preview.photoCount} Dokumen",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "• Waktu Ekspor: ${preview.exportedAt}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Pilih Metode Penggabungan:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )

            // Option 1: Merge
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { importMergeStrategy = true }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = importMergeStrategy,
                onClick = { importMergeStrategy = true }
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text("Gabungkan Data (Direkomendasikan)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                  "Menambahkan warga baru & memperbarui data tanpa menghapus warga yang sudah ada di HP ini.",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Option 2: Overwrite
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { importMergeStrategy = false }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = !importMergeStrategy,
                onClick = { importMergeStrategy = false }
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text("Gantikan Semua (Overwrite)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                Text(
                  "Menghapus seluruh database di HP ini dan menggantikannya dengan isi file cadangan.",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            if (isImporting) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sedang memproses & memulihkan foto...", fontSize = 12.sp)
              }
            }
          }
        }
      },
      confirmButton = {
        if (preview.isValid) {
          Button(
            enabled = !isImporting,
            onClick = {
              pendingImportJson?.let { json ->
                isImporting = true
                viewModel.importBackup(context, json, importMergeStrategy) { result ->
                  isImporting = false
                  backupPreview = null
                  pendingImportJson = null
                  importResultDialog = result
                }
              }
            }
          ) {
            Text("Mulai Impor Data")
          }
        }
      },
      dismissButton = {
        TextButton(
          enabled = !isImporting,
          onClick = {
            backupPreview = null
            pendingImportJson = null
          }
        ) {
          Text("Batal")
        }
      }
    )
  }

  // Import Result Summary Dialog
  importResultDialog?.let { result ->
    AlertDialog(
      onDismissRequest = { importResultDialog = null },
      icon = {
        Icon(
          imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Info,
          contentDescription = null,
          tint = if (result.success) EmeraldSecondary else MaterialTheme.colorScheme.error,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text(if (result.success) "Impor Data Sukses!" else "Gagal Mengimpor Data", fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(result.message)
          if (result.success) {
            Text(
              text = "• Data Penduduk Diproses: ${result.totalProcessed} Jiwa\n• Foto Berkas Dipulihkan: ${result.photosRestored} Foto",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      },
      confirmButton = {
        Button(onClick = { importResultDialog = null }) {
          Text("Selesai")
        }
      }
    )
  }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text("Hapus Data Penduduk Contoh?") },
      text = {
        Text("Tindakan ini akan menghapus seluruh data kependudukan percontohan/dummy dari basis data. Lanjutkan?")
      },
      confirmButton = {
        Button(
          onClick = {
            showResetDialog = false
            viewModel.purgeSampleData()
          }
        ) {
          Text("Ya, Hapus Contoh")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Batal")
        }
      }
    )
  }

  if (showClearDialog) {
    AlertDialog(
      onDismissRequest = { showClearDialog = false },
      title = { Text("Kosongkan Seluruh Data?") },
      text = {
        Text("PERINGATAN: Semua ${allResidents.size} data warga akan dihapus permanen dari memori HP. Pastikan Anda sudah membagikan file cadangan terlebih dahulu!")
      },
      confirmButton = {
        Button(
          onClick = {
            showClearDialog = false
            viewModel.clearAllData()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Hapus Semua")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearDialog = false }) {
          Text("Batal")
        }
      }
    )
  }

  Scaffold(
    topBar = {
      DapendaTopAppBar(
        title = "Ekspor & Impor Data",
        subtitle = "Sinkronisasi & Transfer Antar-HP",
        showBackButton = true,
        onBackClick = { viewModel.navigateBack() }
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Feature 1: Export Card for Multi-Device Transfer
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudUpload,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary
                )
              }
              Column {
                Text(
                  text = "1. Ekspor Data (Kirim ke HP Lain)",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Buat file cadangan untuk dikirim via WhatsApp / Bluetooth",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Data yang akan dicadangkan: ${allResidents.size} data warga Desa Benda.",
              style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Include Photos Toggle
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Column {
                  Text("Sertakan Berkas Foto", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                  Text("Foto Wajah, KTP, dan Kartu Keluarga (KK)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Switch(
                checked = includePhotosInExport,
                onCheckedChange = { includePhotosInExport = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Share File Button
              Button(
                onClick = {
                  scope.launch(Dispatchers.IO) {
                    try {
                      isExporting = true
                      val json = viewModel.exportJsonContent(includePhotosInExport)
                      val file = File(context.cacheDir, "dapenda_backup_desa_benda.json")
                      FileOutputStream(file).use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                        out.flush()
                      }
                      val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                      )
                      val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_SUBJECT, "Data Cadangan DAPENDA Desa Benda.json")
                        type = "application/json"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                      }
                      withContext(Dispatchers.Main) {
                        isExporting = false
                        context.startActivity(Intent.createChooser(sendIntent, "Kirim File Cadangan ke HP Lain"))
                      }
                    } catch (e: Exception) {
                      withContext(Dispatchers.Main) {
                        isExporting = false
                        viewModel.showToast("Gagal menyiapkan ekspor: ${e.localizedMessage}")
                      }
                    }
                  }
                },
                modifier = Modifier
                  .weight(1.3f)
                  .testTag("share_backup_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kirim ke HP Lain", fontWeight = FontWeight.Bold)
              }

              // Save to Device Button
              OutlinedButton(
                onClick = {
                  createDocumentLauncher.launch("dapenda_backup_desa_benda.json")
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("save_backup_button")
              ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Simpan File")
              }
            }
          }
        }
      }

      // Feature 2: Import Card from Received File
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(EmeraldSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudDownload,
                  contentDescription = null,
                  tint = EmeraldSecondary
                )
              }
              Column {
                Text(
                  text = "2. Impor Data (Terima dari HP Lain)",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Buka file .json yang diterima dari WhatsApp / Bluetooth",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Fitur ini memungkinkan Anda memasukkan data penduduk yang dikirim oleh pengurus RT/RW lain, lengkap dengan foto dokumen KTP & KK.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                openDocumentLauncher.launch(arrayOf("*/*", "application/json", "text/plain"))
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("open_import_file_button"),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary)
            ) {
              Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Pilih File Cadangan untuk Diimpor", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Feature 3: Export CSV (Excel)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.TableChart,
                  contentDescription = null,
                  tint = Color(0xFF0284C7)
                )
              }
              Column {
                Text(
                  text = "3. Ekspor Tabel CSV (Excel / Sheets)",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Format tabel untuk dibuka di komputer atau dicetak",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = {
                  val csv = viewModel.exportCsvContent()
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("DAPENDA CSV", csv)
                  clipboard.setPrimaryClip(clip)
                  viewModel.showToast("CSV berhasil disalin ke clipboard!")
                },
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Salin Teks")
              }

              Button(
                onClick = {
                  scope.launch(Dispatchers.IO) {
                    try {
                      val csv = viewModel.exportCsvContent()
                      val file = File(context.cacheDir, "dapenda_data_penduduk_benda.csv")
                      FileOutputStream(file).use { out ->
                        out.write(csv.toByteArray(Charsets.UTF_8))
                        out.flush()
                      }
                      val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                      )
                      val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_SUBJECT, "Data Kependudukan DAPENDA Desa Benda.csv")
                        type = "text/csv"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                      }
                      withContext(Dispatchers.Main) {
                        context.startActivity(Intent.createChooser(sendIntent, "Ekspor CSV Data Penduduk"))
                      }
                    } catch (e: Exception) {
                      withContext(Dispatchers.Main) {
                        viewModel.showToast("Gagal menyiapkan ekspor CSV: ${e.localizedMessage}")
                      }
                    }
                  }
                },
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Bagikan CSV")
              }
            }
          }
        }
      }

      // Feature: Migrasi Data Lokal (Room) ke Cloud Firestore
      item {
        val localList by viewModel.localResidentList.collectAsState()
        var isMigrating by remember { mutableStateOf(false) }
        var progressText by remember { mutableStateOf("") }

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(TealPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudSync,
                  contentDescription = null,
                  tint = TealPrimary
                )
              }
              Column {
                Text(
                  text = "4. Migrasi Data Lokal ke Cloud",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Unggah basis data Room SQLite lokal ke Cloud Firestore",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Data tersimpan di memori HP lokal: ${localList.size} Jiwa. Seluruh data warga lokal akan diunggah ke Cloud Firestore tanpa menghapus data lokal.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )

            if (isMigrating) {
              Spacer(modifier = Modifier.height(12.dp))
              LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = progressText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                if (localList.isEmpty()) {
                  viewModel.showToast("Tidak ada data warga lokal untuk dimigrasikan.")
                  return@Button
                }
                isMigrating = true
                progressText = "Memulai pengunggahan data..."
                viewModel.migrateLocalDataToFirestore(
                  onProgress = { current, total ->
                    progressText = "Mengunggah: $current dari $total warga..."
                  },
                  onResult = { result ->
                    isMigrating = false
                    progressText = ""
                  }
                )
              },
              enabled = !isMigrating && localList.isNotEmpty(),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("migrate_cloud_button"),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Migrasikan Data ke Cloud Firestore", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Feature 4: Maintenance / Reset
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Pemeliharaan Basis Data",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
              onClick = { showResetDialog = true },
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.DeleteForever, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Hapus Data Penduduk Contoh")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
              onClick = { showClearDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Kosongkan Seluruh Basis Data", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }

      // App Info Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Panduan Transfer Antar-HP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "1. Di HP Pertama: Buka menu ini, pilih 'Kirim ke HP Lain' lalu bagikan melalui WhatsApp, Bluetooth, atau Google Drive.\n" +
                "2. Di HP Kedua: Simpan file yang diterima ke memori HP.\n" +
                "3. Buka aplikasi DAPENDA di HP kedua, klik 'Pilih File Cadangan untuk Diimpor', lalu pilih 'Gabungkan Data'.\n" +
                "Semua data penduduk beserta berkas foto KTP & KK akan langsung tersinkronisasi!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 20.sp
            )
          }
        }
      }
    }
  }
}
