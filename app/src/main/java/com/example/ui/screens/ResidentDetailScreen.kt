package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.FullScreenPhotoViewerDialog
import com.example.util.PhotoStorageManager
import java.io.File
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Resident
import com.example.ui.components.DapendaTopAppBar
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
fun ResidentDetailScreen(
  viewModel: DapendaViewModel,
  residentId: Long,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val context = LocalContext.current
  val allResidents by viewModel.allResidents.collectAsState()
  val resident = allResidents.find { it.id == residentId }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var selectedPhotoToView by remember { mutableStateOf<Pair<String, String>?>(null) }

  selectedPhotoToView?.let { (title, path) ->
    FullScreenPhotoViewerDialog(
      title = title,
      photoPath = path,
      onDismiss = { selectedPhotoToView = null }
    )
  }

  if (resident == null) {
    Scaffold(
      topBar = {
        DapendaTopAppBar(
          title = "Detail Penduduk",
          showBackButton = true,
          onBackClick = { viewModel.navigateBack() }
        )
      }
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center
      ) {
        Text("Data penduduk tidak ditemukan.")
      }
    }
    return
  }

  // Family members with the same KK number
  val familyMembers = allResidents.filter { it.noKk == resident.noKk && it.id != resident.id }
  val isMale = resident.jenisKelamin.equals("LAKI-LAKI", ignoreCase = true)
  val genderColor = if (isMale) MaleBlue else FemalePink

  val userProfile by viewModel.userProfile.collectAsState()
  val isAdmin = userProfile?.isAdmin == true

  if (showDeleteDialog) {
    if (!isAdmin) {
      AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        icon = {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
          )
        },
        title = { Text("Hak Akses Dibatasi", fontWeight = FontWeight.Bold) },
        text = {
          Text(
            text = "Hanya Administrator Desa yang memiliki hak akses untuk menghapus data kependudukan dari Cloud Firestore. Operator RT/RW hanya memiliki wewenang untuk melihat, menambah, dan mengubah data warga.",
            style = MaterialTheme.typography.bodyMedium
          )
        },
        confirmButton = {
          Button(onClick = { showDeleteDialog = false }) {
            Text("Mengerti")
          }
        }
      )
    } else {
      AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        icon = {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
          )
        },
        title = { Text("Hapus Data Penduduk?", fontWeight = FontWeight.Bold) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Apakah Anda yakin ingin menghapus data kependudukan ini?")
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(10.dp)
            ) {
              Text(
                text = "• Nama: ${resident.namaLengkap}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "• NIK: ${resident.nik}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "• Wilayah: ${resident.rt}/${resident.rw}, ${resident.dusun}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = "Data yang dihapus akan hilang dari basis data Cloud Firestore dan seluruh HP petugas secara permanen.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              showDeleteDialog = false
              viewModel.deleteResident(resident) {
                viewModel.navigateBack()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.testTag("detail_confirm_delete_button")
          ) {
            Text("Ya, Hapus Permanen", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(
            onClick = { showDeleteDialog = false },
            modifier = Modifier.testTag("detail_cancel_delete_button")
          ) {
            Text("Batal")
          }
        }
      )
    }
  }

  Scaffold(
    topBar = {
      DapendaTopAppBar(
        title = "Biodata Warga RT",
        subtitle = "${resident.rt} • ${resident.dusun}",
        showBackButton = true,
        onBackClick = { viewModel.navigateBack() },
        actions = {
          IconButton(
            onClick = { viewModel.initFormForEdit(resident) },
            modifier = Modifier.testTag("detail_edit_button")
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Data", tint = MaterialTheme.colorScheme.primary)
          }
          IconButton(
            onClick = {
              val shareText = """
                *BIODATA PENDUDUK RT (DAPENDA)*
                Nama: ${resident.namaLengkap}
                NIK: ${resident.nik}
                No KK: ${resident.noKk}
                Jenis Kelamin: ${resident.jenisKelamin}
                TTL: ${resident.tempatLahir}, ${resident.tanggalLahir} (${resident.usia} th)
                Wilayah: ${resident.rt} / ${resident.rw}, ${resident.dusun}
                Alamat: ${resident.alamatLengkap}
                Status Keluarga: ${resident.statusKeluarga}
                Status Kawin: ${resident.statusKawin}
                Pekerjaan: ${resident.pekerjaan}
                Agama: ${resident.agama}
                Status Tinggal: ${resident.statusTinggal}
                No HP/WA: ${if (resident.noHp.isNotBlank()) resident.noHp else "-"}
                Bantuan Sosial: ${if (resident.bantuanSosial) "Penerima Bantuan" else "Bukan Penerima"}
              """.trimIndent()
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
              }
              context.startActivity(Intent.createChooser(sendIntent, "Bagikan Data Warga"))
            }
          ) {
            Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = MaterialTheme.colorScheme.onSurface)
          }
          IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.testTag("detail_delete_button")
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
          }
        }
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
      // Modern Digital KTP Card Preview
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            // Card Top Banner
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(TealPrimary, Color(0xFF0D9488))
                  )
                )
                .padding(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "REPUBLIK INDONESIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                  )
                  Text(
                    text = "KARTU TANDA PENDUDUK RT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = resident.rt,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                  )
                }
              }
            }

            // Card Body
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "NIK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = resident.nik,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                // Avatar initials or face photo
                val hasFacePhoto = PhotoStorageManager.hasPhoto(resident.fotoPenduduk)
                if (hasFacePhoto) {
                  Box(
                    modifier = Modifier
                      .size(60.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                      .clickable {
                        selectedPhotoToView = Pair("Foto Wajah: ${resident.namaLengkap}", resident.fotoPenduduk)
                      }
                  ) {
                    AsyncImage(
                      model = ImageRequest.Builder(context)
                        .data(PhotoStorageManager.getPhotoModel(context, resident.fotoPenduduk))
                        .crossfade(true)
                        .build(),
                      contentDescription = "Foto Wajah ${resident.namaLengkap}",
                      modifier = Modifier.fillMaxSize(),
                      contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                  }
                } else {
                  Box(
                    modifier = Modifier
                      .size(54.dp)
                      .clip(CircleShape)
                      .background(if (isMale) Color(0xFFEFF6FF) else Color(0xFFFDF2F8))
                      .border(2.dp, genderColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = resident.namaLengkap.take(2).uppercase(),
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold,
                      color = genderColor
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = "Nama Lengkap",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = resident.namaLengkap.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              Spacer(modifier = Modifier.height(12.dp))
              HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
              Spacer(modifier = Modifier.height(12.dp))

              // 2-Column Info Grid
              DetailRow(label = "Tempat/Tgl Lahir", value = "${resident.tempatLahir}, ${resident.tanggalLahir} (${resident.usia} th)")
              DetailRow(label = "Jenis Kelamin", value = resident.jenisKelamin)
              DetailRow(label = "Alamat", value = "${resident.alamatLengkap}, ${resident.rt}/${resident.rw}, ${resident.dusun}")
              DetailRow(label = "Agama", value = resident.agama)
              DetailRow(label = "Status Perkawinan", value = resident.statusKawin)
              DetailRow(label = "Pekerjaan", value = resident.pekerjaan)
              DetailRow(label = "Pendidikan", value = resident.pendidikan)
              DetailRow(label = "Golongan Darah", value = resident.golonganDarah)
              DetailRow(label = "Nomor KK", value = resident.noKk)
              DetailRow(label = "Status dalam KK", value = resident.statusKeluarga)
              DetailRow(label = "Status Domisili", value = resident.statusTinggal)

              if (resident.noHp.isNotBlank()) {
                DetailRow(label = "No. Telepon / WA", value = resident.noHp)
              }

              if (resident.catatan.isNotBlank()) {
                DetailRow(label = "Catatan RT", value = resident.catatan)
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Badges row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                if (resident.bantuanSosial) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(EmeraldSecondaryContainer)
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = "✓ Penerima Bansos",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldSecondary
                    )
                  }
                }
                if (resident.disabilitas) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(AmberAccentContainer)
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = "Penyandang Disabilitas",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = AmberAccent
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Documentation Photos (KTP & Kartu Keluarga) Section
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "Dokumentasi Berkas Warga",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              TextButton(
                onClick = { viewModel.initFormForEdit(resident) }
              ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kelola Foto", fontSize = 12.sp)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Foto KTP
            val hasKtpPhoto = PhotoStorageManager.hasPhoto(resident.fotoKtp)
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("Foto e-KTP Warga", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                  }
                  if (hasKtpPhoto) {
                    Text("✓ Terlampir", color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  } else {
                    Text("Belum ada", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (hasKtpPhoto) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(170.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { selectedPhotoToView = Pair("Foto e-KTP: ${resident.namaLengkap}", resident.fotoKtp) }
                  ) {
                    AsyncImage(
                      model = ImageRequest.Builder(context)
                        .data(PhotoStorageManager.getPhotoModel(context, resident.fotoKtp))
                        .crossfade(true)
                        .build(),
                      contentDescription = "Foto KTP",
                      modifier = Modifier.fillMaxSize(),
                      contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Surface(
                      modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
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
                } else {
                  OutlinedButton(
                    onClick = { viewModel.initFormForEdit(resident) },
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ambil Foto KTP Sekarang", fontSize = 12.sp)
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Foto Kartu Keluarga (KK)
            val hasKkPhoto = PhotoStorageManager.hasPhoto(resident.fotoKk)
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("Foto Kartu Keluarga (KK)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                  }
                  if (hasKkPhoto) {
                    Text("✓ Terlampir", color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  } else {
                    Text("Belum ada", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (hasKkPhoto) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(170.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { selectedPhotoToView = Pair("Foto Kartu Keluarga: No. ${resident.noKk}", resident.fotoKk) }
                  ) {
                    AsyncImage(
                      model = ImageRequest.Builder(context)
                        .data(PhotoStorageManager.getPhotoModel(context, resident.fotoKk))
                        .crossfade(true)
                        .build(),
                      contentDescription = "Foto Kartu Keluarga",
                      modifier = Modifier.fillMaxSize(),
                      contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Surface(
                      modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
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
                } else {
                  OutlinedButton(
                    onClick = { viewModel.initFormForEdit(resident) },
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ambil Foto KK Sekarang", fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }

      // WhatsApp Contact Action
      if (resident.noHp.isNotBlank()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Kontak Warga",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = resident.noHp,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Button(
                onClick = {
                  val sanitized = resident.noHp.replace(Regex("[^0-9]"), "")
                  val phoneNum = if (sanitized.startsWith("0")) "62" + sanitized.substring(1) else sanitized
                  val url = "https://wa.me/$phoneNum"
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                  try {
                    context.startActivity(intent)
                  } catch (e: Exception) {
                    // Fallback to dialer
                    val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${resident.noHp}"))
                    context.startActivity(dial)
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
              ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kirim WA", color = Color.White, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Family Members in Same Kartu Keluarga
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
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
              Column {
                Text(
                  text = "Anggota Keluarga (Satu KK)",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "No. KK: ${resident.noKk}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              IconButton(
                onClick = {
                  viewModel.initFormForAddFamilyMember(
                    parentKk = resident.noKk,
                    rt = resident.rt,
                    rw = resident.rw,
                    dusun = resident.dusun,
                    alamat = resident.alamatLengkap
                  )
                }
              ) {
                Icon(
                  Icons.Default.PersonAdd,
                  contentDescription = "Tambah Anggota KK",
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (familyMembers.isEmpty()) {
              Text(
                text = "Belum ada anggota keluarga lain terdaftar di No. KK ini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedButton(
                onClick = {
                  viewModel.initFormForAddFamilyMember(
                    parentKk = resident.noKk,
                    rt = resident.rt,
                    rw = resident.rw,
                    dusun = resident.dusun,
                    alamat = resident.alamatLengkap
                  )
                },
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Tambah Anggota Keluarga")
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                familyMembers.forEach { member ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                      .clickable { viewModel.navigateTo(Screen.ResidentDetail(member.id)) }
                      .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                    Text(
                      text = "Lihat >",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Tindakan Pengelolaan Data Warga Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Tindakan & Pengelolaan Data",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Perbarui informasi warga jika terdapat perubahan data kependudukan atau hapus jika data salah.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = { viewModel.initFormForEdit(resident) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("detail_bottom_edit_button")
              ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Data")
              }

              Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                  .weight(1.2f)
                  .testTag("detail_bottom_delete_button")
              ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hapus Warga", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Bottom Spacer
      item {
        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

@Composable
fun DetailRow(
  label: String,
  value: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(1f)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1.4f)
    )
  }
}
