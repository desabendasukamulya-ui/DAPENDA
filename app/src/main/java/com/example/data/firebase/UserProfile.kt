package com.example.data.firebase

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserProfile(
  val uid: String = "",
  val email: String = "",
  val displayName: String = "",
  val role: String = "operator", // "admin" or "operator"
  val rt: String = "",
  val rw: String = "",
  val phone: String = "",
  val passwordHash: String = "",
  val createdAt: Long = System.currentTimeMillis()
) {
  val isAdmin: Boolean
    get() = role.equals("admin", ignoreCase = true) ||
      email.equals("desabendasukamulya@gmail.com", ignoreCase = true) ||
      displayName.contains("Admin", ignoreCase = true)
}
