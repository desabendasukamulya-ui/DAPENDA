package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.sample.SampleData
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.DapendaViewModel
import kotlinx.coroutines.launch

/**
 * Helper untuk menemukan Activity context dari hierarki Context Compose.
 */
fun Context.findActivity(): Activity? {
  var currentContext = this
  while (currentContext is ContextWrapper) {
    if (currentContext is Activity) return currentContext
    currentContext = currentContext.baseContext
  }
  return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
  viewModel: DapendaViewModel,
  activity: ComponentActivity? = null
) {
  val context = LocalContext.current
  val actualActivity: Activity? = activity
    ?: (context as? Activity)
    ?: context.findActivity()

  val isAuthenticating by viewModel.isAuthenticating.collectAsState()
  val authError by viewModel.authError.collectAsState()
  val scope = rememberCoroutineScope()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Masuk, 1: Daftar

  // Form Login Operator
  var loginIdentifier by remember { mutableStateOf("") }
  var loginPassword by remember { mutableStateOf("") }
  var loginShowPassword by remember { mutableStateOf(false) }
  var loginRememberMe by remember { mutableStateOf(true) }

  // Form Daftar Operator Baru
  var regName by remember { mutableStateOf("") }
  var regEmailOrUser by remember { mutableStateOf("") }
  var regPhone by remember { mutableStateOf("") }
  var regRw by remember { mutableStateOf("RW 01") }
  var regRt by remember { mutableStateOf("RT 01") }
  var regRole by remember { mutableStateOf("Operator RT") }
  var regPassword by remember { mutableStateOf("") }
  var regConfirmPassword by remember { mutableStateOf("") }
  var regShowPassword by remember { mutableStateOf(false) }
  var regRememberMe by remember { mutableStateOf(true) }

  var rwDropdownExpanded by remember { mutableStateOf(false) }
  var rtDropdownExpanded by remember { mutableStateOf(false) }
  var roleDropdownExpanded by remember { mutableStateOf(false) }

  val availableRts = remember(regRw) { SampleData.getRtListForRw(regRw) }
  val roleOptions = listOf("Operator RT", "Operator RW", "Ketua RT", "Ketua RW", "Administrator Desa", "Staf Pelayanan Desa")

  Box(modifier = Modifier.fillMaxSize()) {
    // Background Image
    Image(
      painter = painterResource(id = R.drawable.img_beranda_bg),
      contentDescription = null,
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
              MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
              MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
              MaterialTheme.colorScheme.background
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Logo Kabupaten Tangerang
      Surface(
        modifier = Modifier
          .size(76.dp)
          .clip(CircleShape),
        color = Color.White,
        shadowElevation = 4.dp
      ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
          Image(
            painter = painterResource(id = R.drawable.foto_logo_tangerang),
            contentDescription = "Lambang Kabupaten Tangerang",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "DAPENDA ONLINE",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = "Sistem Data Kependudukan Desa Benda\nKecamatan Sukamulya, Kabupaten Tangerang",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Kotak Info Awal Install
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = TealPrimary.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Info, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
          Text(
            text = "Saat awal install, silakan Masuk atau Daftar akun Operator RT/RW. Akun akan tersimpan otomatis agar langsung masuk di kemudian hari.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            lineHeight = 15.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Card Container
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Tab Switcher (Masuk vs Daftar)
          TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
          ) {
            Tab(
              selected = selectedTab == 0,
              onClick = { selectedTab = 0 },
              text = {
                Text(
                  "Masuk Operator",
                  fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                )
              },
              icon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
              selected = selectedTab == 1,
              onClick = { selectedTab = 1 },
              text = {
                Text(
                  "Daftar Akun Baru",
                  fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                )
              },
              icon = { Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Error Message Box
          if (authError != null) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.errorContainer
            ) {
              Text(
                text = authError ?: "",
                modifier = Modifier.padding(10.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
          }

          if (selectedTab == 0) {
            // ================= TAB MASUK (LOGIN) =================
            Text(
              text = "Masuk Petugas RT / RW",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Gunakan email, username, no. HP atau akun yang telah Anda daftarkan.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = loginIdentifier,
              onValueChange = { loginIdentifier = it },
              label = { Text("Email / Username / No. HP") },
              placeholder = { Text("Contoh: admin, rt01, atau email") },
              leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = loginPassword,
              onValueChange = { loginPassword = it },
              label = { Text("Kata Sandi / PIN") },
              placeholder = { Text("Masukkan kata sandi") },
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
              trailingIcon = {
                IconButton(onClick = { loginShowPassword = !loginShowPassword }) {
                  Icon(
                    imageVector = if (loginShowPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                  )
                }
              },
              visualTransformation = if (loginShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ingat Saya Checkbox (Simpan Akun)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { loginRememberMe = !loginRememberMe }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = loginRememberMe,
                onCheckedChange = { loginRememberMe = it },
                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
              )
              Text(
                text = "Simpan akun & buka langsung di kemudian hari",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Masuk Petugas
            Button(
              onClick = {
                viewModel.loginOperator(loginIdentifier, loginPassword, loginRememberMe)
              },
              enabled = !isAuthenticating,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("login_operator_button"),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
              shape = RoundedCornerShape(10.dp)
            ) {
              if (isAuthenticating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Memverifikasi...")
              } else {
                Text("Masuk sebagai Operator RT/RW", fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Petunjuk akun resmi dan pembatasan akses
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(Icons.Default.Security, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                  Text(
                    text = "Akses Khusus Akun Terdaftar:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Aplikasi hanya dapat dibuka oleh akun yang sudah terdaftar. Jika belum terdaftar, silakan daftarkan akun baru atau hubungi Admin.",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = TealPrimary.copy(alpha = 0.1f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                      text = "Akun Administrator Desa:",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "User : admin\nPassword : Benda15631 (dapat dirubah)",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 12.sp,
                      color = TealPrimary
                    )
                  }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = {
                    loginIdentifier = "admin"
                    loginPassword = "Benda15631"
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                  modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                  Text(
                    text = "Gunakan Akun Admin (admin / Benda15631)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              HorizontalDivider(modifier = Modifier.weight(1f))
              Text(
                text = "  atau login Google  ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Google Sign-In dengan Activity Context Terjamin
            OutlinedButton(
              onClick = {
                scope.launch {
                  val act = actualActivity ?: (context as? Activity) ?: context.findActivity()
                  if (act != null) {
                    viewModel.signInWithGoogle(act)
                  } else {
                    viewModel.showToast("Gagal membuka Google Sign-In. Silakan masuk menggunakan formulir Operator RT/RW di atas.")
                  }
                }
              },
              enabled = !isAuthenticating,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("google_signin_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Masuk dengan Akun Google", fontWeight = FontWeight.SemiBold)
            }

          } else {
            // ================= TAB DAFTAR OPERATOR BARU =================
            Text(
              text = "Pendaftaran Operator RT / RW",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Daftarkan akun petugas Anda sekali untuk mengelola data warga desa.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = regName,
              onValueChange = { regName = it },
              label = { Text("Nama Lengkap Petugas") },
              placeholder = { Text("Contoh: Suryadi, S.Pd") },
              leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Jabatan / Peran Dropdown
            ExposedDropdownMenuBox(
              expanded = roleDropdownExpanded,
              onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded },
              modifier = Modifier.fillMaxWidth()
            ) {
              OutlinedTextField(
                value = regRole,
                onValueChange = {},
                readOnly = true,
                label = { Text("Jabatan / Peran") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                shape = RoundedCornerShape(10.dp)
              )
              ExposedDropdownMenu(
                expanded = roleDropdownExpanded,
                onDismissRequest = { roleDropdownExpanded = false }
              ) {
                roleOptions.forEach { opt ->
                  DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                      regRole = opt
                      roleDropdownExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Baris Pilihan RW & RT
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // RW Dropdown
              ExposedDropdownMenuBox(
                expanded = rwDropdownExpanded,
                onExpandedChange = { rwDropdownExpanded = !rwDropdownExpanded },
                modifier = Modifier.weight(1f)
              ) {
                OutlinedTextField(
                  value = regRw,
                  onValueChange = {},
                  readOnly = true,
                  label = { Text("RW") },
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rwDropdownExpanded) },
                  modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                  shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                  expanded = rwDropdownExpanded,
                  onDismissRequest = { rwDropdownExpanded = false }
                ) {
                  SampleData.rwOptions.forEach { rwOption ->
                    DropdownMenuItem(
                      text = { Text(rwOption) },
                      onClick = {
                        regRw = rwOption
                        val rts = SampleData.getRtListForRw(rwOption)
                        regRt = rts.firstOrNull() ?: "RT 01"
                        rwDropdownExpanded = false
                      }
                    )
                  }
                }
              }

              // RT Dropdown
              ExposedDropdownMenuBox(
                expanded = rtDropdownExpanded,
                onExpandedChange = { rtDropdownExpanded = !rtDropdownExpanded },
                modifier = Modifier.weight(1f)
              ) {
                OutlinedTextField(
                  value = regRt,
                  onValueChange = {},
                  readOnly = true,
                  label = { Text("RT") },
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rtDropdownExpanded) },
                  modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                  shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                  expanded = rtDropdownExpanded,
                  onDismissRequest = { rtDropdownExpanded = false }
                ) {
                  availableRts.forEach { rtOption ->
                    DropdownMenuItem(
                      text = { Text(rtOption) },
                      onClick = {
                        regRt = rtOption
                        rtDropdownExpanded = false
                      }
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = regPhone,
              onValueChange = { regPhone = it },
              label = { Text("No. WhatsApp / HP") },
              placeholder = { Text("Contoh: 081234567890") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = regEmailOrUser,
              onValueChange = { regEmailOrUser = it },
              label = { Text("Email / Username Petugas") },
              placeholder = { Text("Contoh: suryadi_rt01 atau email") },
              leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = regPassword,
              onValueChange = { regPassword = it },
              label = { Text("Kata Sandi / PIN (Min. 6 Karakter)") },
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
              trailingIcon = {
                IconButton(onClick = { regShowPassword = !regShowPassword }) {
                  Icon(
                    imageVector = if (regShowPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                  )
                }
              },
              visualTransformation = if (regShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = regConfirmPassword,
              onValueChange = { regConfirmPassword = it },
              label = { Text("Konfirmasi Kata Sandi") },
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
              visualTransformation = if (regShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ingat Saya Checkbox (Simpan Akun)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { regRememberMe = !regRememberMe }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = regRememberMe,
                onCheckedChange = { regRememberMe = it },
                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
              )
              Text(
                text = "Simpan akun & masuk otomatis di HP ini",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                if (regPassword != regConfirmPassword) {
                  viewModel.showToast("Kata sandi dan konfirmasi sandi tidak cocok!")
                  return@Button
                }
                viewModel.registerOperator(
                  name = regName,
                  emailOrUser = regEmailOrUser,
                  phone = regPhone,
                  rt = regRt,
                  rw = regRw,
                  role = regRole,
                  password = regPassword,
                  rememberMe = regRememberMe
                )
              },
              enabled = !isAuthenticating,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("register_operator_button"),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
              shape = RoundedCornerShape(10.dp)
            ) {
              if (isAuthenticating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mendaftarkan...")
              } else {
                Text("Daftar & Masuk Otomatis", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Aplikasi DAPENDA • Terkoneksi Real-time Cloud Firestore\nDesa Benda, Kec. Sukamulya, Kab. Tangerang",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
      )
    }
  }
}
