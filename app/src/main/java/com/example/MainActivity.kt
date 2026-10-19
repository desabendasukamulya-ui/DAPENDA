package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.Screen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExportBackupScreen
import com.example.ui.screens.FamilyGroupScreen
import com.example.ui.screens.ResidentDetailScreen
import com.example.ui.screens.ResidentFormScreen
import com.example.ui.screens.ResidentListScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.DapendaTheme
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryContainer
import com.example.ui.viewmodel.DapendaViewModel

import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.AuthScreen

class MainActivity : ComponentActivity() {

  private val viewModel: DapendaViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      DapendaTheme {
        val currentUser by viewModel.currentUser.collectAsState()
        val userProfile by viewModel.userProfile.collectAsState()
        if (currentUser == null && userProfile == null) {
          AuthScreen(viewModel = viewModel, activity = this@MainActivity)
        } else {
          DapendaApp(viewModel = viewModel, activity = this@MainActivity)
        }
      }
    }
  }
}

@Composable
fun DapendaApp(viewModel: DapendaViewModel, activity: ComponentActivity? = null) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val toastMessage by viewModel.toastMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current

  LaunchedEffect(toastMessage) {
    toastMessage?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearToastMessage()
    }
  }

  val isTopLevel = currentScreen is Screen.Dashboard ||
    currentScreen is Screen.ResidentList ||
    currentScreen is Screen.FamilyGroup ||
    currentScreen is Screen.Statistics

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (isTopLevel) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 6.dp
        ) {
          NavigationBarItem(
            selected = currentScreen is Screen.Dashboard,
            onClick = { viewModel.navigateTo(Screen.Dashboard) },
            icon = {
              Icon(
                if (currentScreen is Screen.Dashboard) Icons.Filled.Home else Icons.Outlined.Home,
                contentDescription = "Beranda"
              )
            },
            label = { Text("Beranda", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = TealPrimary,
              selectedTextColor = TealPrimary,
              indicatorColor = TealPrimaryContainer
            ),
            modifier = Modifier.testTag("nav_bottom_beranda")
          )

          NavigationBarItem(
            selected = currentScreen is Screen.ResidentList,
            onClick = { viewModel.navigateTo(Screen.ResidentList) },
            icon = {
              Icon(
                if (currentScreen is Screen.ResidentList) Icons.Filled.Group else Icons.Outlined.Group,
                contentDescription = "Data Warga"
              )
            },
            label = { Text("Warga", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = TealPrimary,
              selectedTextColor = TealPrimary,
              indicatorColor = TealPrimaryContainer
            ),
            modifier = Modifier.testTag("nav_bottom_warga")
          )

          NavigationBarItem(
            selected = currentScreen is Screen.FamilyGroup,
            onClick = { viewModel.navigateTo(Screen.FamilyGroup()) },
            icon = {
              Icon(
                if (currentScreen is Screen.FamilyGroup) Icons.Filled.HomeWork else Icons.Outlined.HomeWork,
                contentDescription = "Data KK"
              )
            },
            label = { Text("Data KK", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = TealPrimary,
              selectedTextColor = TealPrimary,
              indicatorColor = TealPrimaryContainer
            ),
            modifier = Modifier.testTag("nav_bottom_kk")
          )

          NavigationBarItem(
            selected = currentScreen is Screen.Statistics,
            onClick = { viewModel.navigateTo(Screen.Statistics) },
            icon = {
              Icon(
                if (currentScreen is Screen.Statistics) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                contentDescription = "Statistik"
              )
            },
            label = { Text("Statistik", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = TealPrimary,
              selectedTextColor = TealPrimary,
              indicatorColor = TealPrimaryContainer
            ),
            modifier = Modifier.testTag("nav_bottom_statistik")
          )
        }
      }
    }
  ) { innerPadding ->
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      color = MaterialTheme.colorScheme.background
    ) {
      AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
      ) { screen ->
        when (screen) {
          is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
          is Screen.ResidentList -> ResidentListScreen(viewModel = viewModel)
          is Screen.ResidentForm -> ResidentFormScreen(viewModel = viewModel, residentId = screen.residentId)
          is Screen.ResidentDetail -> ResidentDetailScreen(viewModel = viewModel, residentId = screen.residentId)
          is Screen.FamilyGroup -> FamilyGroupScreen(viewModel = viewModel, initialKk = screen.initialKk)
          is Screen.Statistics -> StatisticsScreen(viewModel = viewModel)
          is Screen.PopulationMutation -> com.example.ui.screens.PopulationMutationScreen(viewModel = viewModel, initialTab = screen.initialTab)
          is Screen.ExportBackup -> ExportBackupScreen(viewModel = viewModel)
          is Screen.AuditLog -> AuditLogScreen(viewModel = viewModel)
          is Screen.Auth -> AuthScreen(viewModel = viewModel, activity = activity)
        }
      }
    }
  }
}
