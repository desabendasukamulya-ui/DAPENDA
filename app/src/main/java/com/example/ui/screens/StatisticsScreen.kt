package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DapendaTopAppBar
import com.example.ui.components.DemographicBarRow
import com.example.ui.components.RtSelectorChips
import com.example.ui.components.StatMetricCard
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
fun StatisticsScreen(
  viewModel: DapendaViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val context = LocalContext.current
  val allResidents by viewModel.allResidents.collectAsState()
  val selectedRt by viewModel.selectedRt.collectAsState()
  val availableRts by viewModel.availableRtList.collectAsState()

  val activeList = if (selectedRt == "Semua RT" || selectedRt.isBlank()) {
    allResidents.filter { !it.statusTinggal.equals("Pindah Keluar", ignoreCase = true) }
  } else {
    allResidents.filter { it.rt == selectedRt && !it.statusTinggal.equals("Pindah Keluar", ignoreCase = true) }
  }

  val countMap = allResidents.filter { !it.statusTinggal.equals("Pindah Keluar", ignoreCase = true) }
    .groupBy { it.rt }.mapValues { it.value.size }
  val totalJiwa = activeList.size
  val totalKk = activeList.map { it.noKk }.distinct().size
  val lakiLaki = activeList.count { it.jenisKelamin.equals("LAKI-LAKI", ignoreCase = true) }
  val perempuan = activeList.count { it.jenisKelamin.equals("PEREMPUAN", ignoreCase = true) }

  // Age buckets
  val balita = activeList.count { it.usia <= 5 }
  val anak = activeList.count { it.usia in 6..12 }
  val remaja = activeList.count { it.usia in 13..17 }
  val produktif = activeList.count { it.usia in 18..59 }
  val lansia = activeList.count { it.usia >= 60 }

  // Jobs
  val jobDistribution = activeList.groupBy { it.pekerjaan }
    .mapValues { it.value.size }
    .toList()
    .sortedByDescending { it.second }
    .take(5)

  // Education
  val eduDistribution = activeList.groupBy { it.pendidikan }
    .mapValues { it.value.size }
    .toList()
    .sortedByDescending { it.second }

  // Social assistance
  val bansosCount = activeList.count { it.bantuanSosial }
  val disabilitasCount = activeList.count { it.disabilitas }

  Scaffold(
    topBar = {
      DapendaTopAppBar(
        title = "Statistik & Rekapitulasi RT",
        subtitle = "Grafik Demografi & Komposisi Penduduk",
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
            .padding(16.dp)
        ) {
          Button(
            onClick = {
              val reportText = viewModel.generateReportText(selectedRt)
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, reportText)
                type = "text/plain"
              }
              context.startActivity(Intent.createChooser(sendIntent, "Bagikan Rekapitulasi RT"))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bagikan Laporan RT ke WhatsApp", fontWeight = FontWeight.Bold)
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
      // RT selector
      item {
        Column {
          Text(
            text = "Filter Wilayah:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          RtSelectorChips(
            rtList = availableRts,
            selectedRt = selectedRt,
            onRtSelected = { viewModel.setSelectedRt(it) },
            residentCounts = countMap
          )
        }
      }

      // Quick summary numbers
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatMetricCard(
            title = "Total Jiwa",
            value = "$totalJiwa",
            subtext = selectedRt,
            icon = Icons.Default.Group,
            accentColor = TealPrimary,
            backgroundColor = TealPrimaryContainer,
            modifier = Modifier.weight(1f)
          )
          StatMetricCard(
            title = "Total KK",
            value = "$totalKk",
            subtext = "Kepala Keluarga",
            icon = Icons.Default.Home,
            accentColor = AmberAccent,
            backgroundColor = AmberAccentContainer,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Gender Breakdown Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Perbandingan Gender (L / P)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val malePct = if (totalJiwa > 0) (lakiLaki.toFloat() / totalJiwa) else 0f
            val femalePct = if (totalJiwa > 0) (perempuan.toFloat() / totalJiwa) else 0f

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Male, contentDescription = null, tint = MaleBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Laki-laki: $lakiLaki (${(malePct * 100).toInt()}%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Female, contentDescription = null, tint = FemalePink, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Perempuan: $perempuan (${(femalePct * 100).toInt()}%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dual Progress bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
            ) {
              Box(
                modifier = Modifier
                  .weight(if (malePct > 0f) malePct else 0.001f)
                  .background(MaleBlue)
              )
              Box(
                modifier = Modifier
                  .weight(if (femalePct > 0f) femalePct else 0.001f)
                  .background(FemalePink)
              )
            }
          }
        }
      }

      // Age Distribution Pyramid
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Piramida Kelompok Usia",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            DemographicBarRow(
              label = "Balita (0 - 5 tahun)",
              count = balita,
              fraction = if (totalJiwa > 0) (balita.toFloat() / totalJiwa) else 0f,
              color = EmeraldSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            DemographicBarRow(
              label = "Anak-anak (6 - 12 tahun)",
              count = anak,
              fraction = if (totalJiwa > 0) (anak.toFloat() / totalJiwa) else 0f,
              color = Color(0xFF0284C7)
            )
            Spacer(modifier = Modifier.height(10.dp))

            DemographicBarRow(
              label = "Remaja (13 - 17 tahun)",
              count = remaja,
              fraction = if (totalJiwa > 0) (remaja.toFloat() / totalJiwa) else 0f,
              color = Color(0xFF7C3AED)
            )
            Spacer(modifier = Modifier.height(10.dp))

            DemographicBarRow(
              label = "Usia Produktif (18 - 59 tahun)",
              count = produktif,
              fraction = if (totalJiwa > 0) (produktif.toFloat() / totalJiwa) else 0f,
              color = TealPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            DemographicBarRow(
              label = "Lansia (60+ tahun)",
              count = lansia,
              fraction = if (totalJiwa > 0) (lansia.toFloat() / totalJiwa) else 0f,
              color = AmberAccent
            )
          }
        }
      }

      // Top Jobs in RT
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "5 Pekerjaan Terbanyak di $selectedRt",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (jobDistribution.isEmpty()) {
              Text("Belum ada data pekerjaan.")
            } else {
              jobDistribution.forEach { (jobName, count) ->
                val frac = if (totalJiwa > 0) (count.toFloat() / totalJiwa) else 0f
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(text = jobName, style = MaterialTheme.typography.bodySmall)
                    Text(text = "$count Orang", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                  }
                  Spacer(modifier = Modifier.height(3.dp))
                  LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = TealPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      // Social Assistance summary
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Data Kesejahteraan & Bantuan Sosial",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              StatMetricCard(
                title = "Penerima Bansos",
                value = "$bansosCount",
                subtext = "PKH / BPNT / BLT",
                icon = Icons.Default.VolunteerActivism,
                accentColor = EmeraldSecondary,
                backgroundColor = EmeraldSecondaryContainer,
                modifier = Modifier.weight(1f)
              )
              StatMetricCard(
                title = "Disabilitas / Rentan",
                value = "$disabilitasCount",
                subtext = "Butuh Perhatian",
                icon = Icons.Default.Group,
                accentColor = AmberAccent,
                backgroundColor = AmberAccentContainer,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      // Bottom padding
      item {
        Spacer(modifier = Modifier.height(70.dp))
      }
    }
  }
}
