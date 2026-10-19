package com.example.ui.navigation

sealed class Screen {
  object Dashboard : Screen()
  object ResidentList : Screen()
  data class ResidentDetail(val residentId: Long) : Screen()
  data class ResidentForm(val residentId: Long? = null, val initialRt: String? = null) : Screen()
  data class FamilyGroup(val initialKk: String? = null) : Screen()
  data class PopulationMutation(val initialTab: Int = 0) : Screen()
  object Statistics : Screen()
  object ExportBackup : Screen()
  object AuditLog : Screen()
  object Auth : Screen()
}
