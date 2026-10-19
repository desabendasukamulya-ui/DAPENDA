package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.firebase.UserProfile
import java.security.MessageDigest

class SessionManager(context: Context) {

  private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

  companion object {
    private const val PREF_NAME = "dapenda_user_session"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_UID = "user_uid"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_NAME = "user_name"
    private const val KEY_ROLE = "user_role"
    private const val KEY_RT = "user_rt"
    private const val KEY_RW = "user_rw"
    private const val KEY_PHONE = "user_phone"
    private const val KEY_REMEMBER_ME = "remember_me"
    private const val KEY_ADMIN_PASSWORD_HASH = "admin_password_hash"

    const val DEFAULT_ADMIN_USERNAME = "admin"
    const val DEFAULT_ADMIN_PASSWORD = "Benda15631"

    fun hashPassword(password: String): String {
      val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
      return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getDefaultAdminPasswordHash(): String = hashPassword(DEFAULT_ADMIN_PASSWORD)
  }

  fun saveAdminPasswordHash(hash: String) {
    prefs.edit().putString(KEY_ADMIN_PASSWORD_HASH, hash).apply()
  }

  fun getAdminPasswordHash(): String? {
    return prefs.getString(KEY_ADMIN_PASSWORD_HASH, null)
  }

  fun saveSession(user: UserProfile, rememberMe: Boolean = true) {
    prefs.edit()
      .putBoolean(KEY_IS_LOGGED_IN, true)
      .putBoolean(KEY_REMEMBER_ME, rememberMe)
      .putString(KEY_UID, user.uid)
      .putString(KEY_EMAIL, user.email)
      .putString(KEY_NAME, user.displayName)
      .putString(KEY_ROLE, user.role)
      .putString(KEY_RT, user.rt)
      .putString(KEY_RW, user.rw)
      .putString(KEY_PHONE, user.phone)
      .apply()
  }

  fun getSavedSession(): UserProfile? {
    val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    if (!isLoggedIn) return null

    val uid = prefs.getString(KEY_UID, "") ?: ""
    val email = prefs.getString(KEY_EMAIL, "") ?: ""
    val name = prefs.getString(KEY_NAME, "") ?: ""
    val role = prefs.getString(KEY_ROLE, "operator") ?: "operator"
    val rt = prefs.getString(KEY_RT, "") ?: ""
    val rw = prefs.getString(KEY_RW, "") ?: ""
    val phone = prefs.getString(KEY_PHONE, "") ?: ""

    if (uid.isBlank() && email.isBlank()) return null

    return UserProfile(
      uid = uid,
      email = email,
      displayName = name,
      role = role,
      rt = rt,
      rw = rw,
      phone = phone
    )
  }

  fun clearSession() {
    prefs.edit().clear().apply()
  }

  fun isLoggedIn(): Boolean {
    return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
  }
}
