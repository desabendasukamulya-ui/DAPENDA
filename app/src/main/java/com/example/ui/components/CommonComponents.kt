package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.util.PhotoStorageManager
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Resident
import com.example.data.sample.SampleData
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberAccentContainer
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.FemalePink
import com.example.ui.theme.MaleBlue
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DapendaTopAppBar(
  title: String,
  subtitle: String? = null,
  showBackButton: Boolean = false,
  onBackClick: (() -> Unit)? = null,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  actions: @Composable () -> Unit = {}
) {
  CenterAlignedTopAppBar(
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrBlank()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
          )
        }
      }
    },
    navigationIcon = {
      if (showBackButton && onBackClick != null) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("top_bar_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Kembali",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    },
    actions = { actions() },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
      containerColor = containerColor
    )
  )
}

@Composable
fun RwSelectorChips(
  selectedRw: String,
  onRwSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val allRwOptions = listOf("Semua RW") + SampleData.rwOptions

  LazyRow(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(allRwOptions) { rw ->
      val isSelected = (rw == selectedRw)
      val countDesc = if (rw == "Semua RW") "Semua" else "${SampleData.rwRtHierarchy[rw]?.size ?: 0} RT"

      FilterChip(
        selected = isSelected,
        onClick = { onRwSelected(rw) },
        label = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = rw,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = countDesc,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        leadingIcon = if (isSelected) {
          {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
          }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = TealPrimaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.testTag("rw_chip_${rw.replace(" ", "_")}")
      )
    }
  }
}

@Composable
fun RtSelectorChips(
  rtList: List<String>,
  selectedRt: String,
  onRtSelected: (String) -> Unit,
  residentCounts: Map<String, Int> = emptyMap(),
  modifier: Modifier = Modifier
) {
  val allOptions = listOf("Semua RT") + rtList.filter { it != "Semua RT" }

  LazyRow(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(allOptions) { rt ->
      val isSelected = (rt == selectedRt)
      val count = if (rt == "Semua RT") {
        residentCounts.values.sum()
      } else {
        residentCounts[rt] ?: 0
      }

      FilterChip(
        selected = isSelected,
        onClick = { onRtSelected(rt) },
        label = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = rt,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (count > 0) {
              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                  )
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "$count",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        },
        leadingIcon = if (isSelected) {
          {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
          }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = TealPrimaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.testTag("rt_chip_${rt.replace(" ", "_")}")
      )
    }
  }
}

@Composable
fun StatMetricCard(
  title: String,
  value: String,
  subtext: String? = null,
  icon: ImageVector,
  accentColor: Color,
  backgroundColor: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Card(
    modifier = modifier
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(backgroundColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = accentColor,
          modifier = Modifier.size(24.dp)
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (!subtext.isNullOrBlank()) {
          Text(
            text = subtext,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}

@Composable
fun ResidentItemCard(
  resident: Resident,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onDeleteClick: (() -> Unit)? = null
) {
  val isMale = resident.jenisKelamin.equals("LAKI-LAKI", ignoreCase = true)
  val genderColor = if (isMale) MaleBlue else FemalePink
  val isKepalaKeluarga = resident.statusKeluarga.equals("Kepala Keluarga", ignoreCase = true)
  val context = LocalContext.current
  val hasPhoto = PhotoStorageManager.hasPhoto(resident.fotoPenduduk)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("resident_card_${resident.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      if (hasPhoto) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        ) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(PhotoStorageManager.getPhotoModel(context, resident.fotoPenduduk))
              .crossfade(true)
              .build(),
            contentDescription = resident.namaLengkap,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
          )
        }
      } else {
        // Avatar initials circle with gender ring
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isMale) Color(0xFFEFF6FF) else Color(0xFFFDF2F8)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = resident.namaLengkap.take(2).uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = genderColor
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = resident.namaLengkap,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
          if (isKepalaKeluarga) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AmberAccentContainer)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "KK",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AmberAccent
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "NIK: ${resident.nik}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // RT Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${resident.rt}/${resident.rw}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Age & Gender
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isMale) Icons.Default.Male else Icons.Default.Female,
              contentDescription = resident.jenisKelamin,
              tint = genderColor,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "${resident.usia} th (${resident.statusKeluarga})",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (resident.bantuanSosial) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(EmeraldSecondaryContainer)
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = "Bansos",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldSecondary
              )
            }
          }

          if (resident.fotoKtp.isNotBlank()) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(TealPrimaryContainer)
                .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Text(
                text = "KTP",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
              )
            }
          }

          if (resident.fotoKk.isNotBlank()) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(AmberAccentContainer)
                .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Text(
                text = "KK",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = AmberAccent
              )
            }
          }
        }
      }

      if (onDeleteClick != null) {
        IconButton(
          onClick = onDeleteClick,
          modifier = Modifier
            .size(36.dp)
            .testTag("delete_resident_button_${resident.id}")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Hapus Data Warga",
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun EmptyView(
  message: String,
  subMessage: String? = null,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Outlined.Inbox,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(36.dp)
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = message,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
    if (!subMessage.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = subMessage,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 18.sp
      )
    }
  }
}

@Composable
fun DemographicBarRow(
  label: String,
  count: Int,
  fraction: Float,
  color: Color
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "$count Orang (${(fraction * 100).toInt()}%)",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { fraction.coerceIn(0f, 1f) },
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp)),
      color = color,
      trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
  }
}

