package com.example.data.firebase

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AuditLog(
  @DocumentId val id: String = "",
  val action: String = "", // CREATE, UPDATE, DELETE, MIGRATE
  val entity: String = "Resident",
  val entityId: String = "",
  val performedBy: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val details: String = ""
)
