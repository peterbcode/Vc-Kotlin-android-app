package com.example.vc_client_android_app.data

import android.content.Context
import kotlin.random.Random

data class AccountProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val contactPreference: String = "WhatsApp",
    val serviceInterest: String = "Fibre",
    val notificationsEnabled: Boolean = true
)

data class RecentRequest(
    val type: String = "No requests yet",
    val summary: String = "Create an ISP, repair, network or support request to see activity here.",
    val timestamp: String = "Waiting for your first request"
)

object AppPreferences {
    private const val PREFS_NAME = "vc_client_prefs"
    private const val KEY_NAME = "account_name"
    private const val KEY_EMAIL = "account_email"
    private const val KEY_PHONE = "account_phone"
    private const val KEY_ADDRESS = "account_address"
    private const val KEY_CONTACT_PREF = "contact_preference"
    private const val KEY_SERVICE_INTEREST = "service_interest"
    private const val KEY_NOTIFICATIONS = "notifications_enabled"
    private const val KEY_LAST_REQUEST_TYPE = "last_request_type"
    private const val KEY_LAST_REQUEST_SUMMARY = "last_request_summary"
    private const val KEY_LAST_REQUEST_TIME = "last_request_time"
    private const val KEY_EASTER_EGG_ELIGIBLE = "easter_egg_eligible"

    fun getProfile(context: Context): AccountProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AccountProfile(
            name = prefs.getString(KEY_NAME, "").orEmpty(),
            email = prefs.getString(KEY_EMAIL, "").orEmpty(),
            phone = prefs.getString(KEY_PHONE, "").orEmpty(),
            address = prefs.getString(KEY_ADDRESS, "").orEmpty(),
            contactPreference = prefs.getString(KEY_CONTACT_PREF, "WhatsApp").orEmpty(),
            serviceInterest = prefs.getString(KEY_SERVICE_INTEREST, "Fibre").orEmpty(),
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        )
    }

    fun saveProfile(context: Context, profile: AccountProfile) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_PHONE, profile.phone)
            .putString(KEY_ADDRESS, profile.address)
            .putString(KEY_CONTACT_PREF, profile.contactPreference)
            .putString(KEY_SERVICE_INTEREST, profile.serviceInterest)
            .putBoolean(KEY_NOTIFICATIONS, profile.notificationsEnabled)
            .apply()
    }

    fun getRecentRequest(context: Context): RecentRequest {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return RecentRequest(
            type = prefs.getString(KEY_LAST_REQUEST_TYPE, "No requests yet").orEmpty(),
            summary = prefs.getString(
                KEY_LAST_REQUEST_SUMMARY,
                "Create an ISP, repair, network or support request to see activity here."
            ).orEmpty(),
            timestamp = prefs.getString(KEY_LAST_REQUEST_TIME, "Waiting for your first request").orEmpty()
        )
    }

    fun saveRecentRequest(context: Context, type: String, summary: String, timestamp: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LAST_REQUEST_TYPE, type)
            .putString(KEY_LAST_REQUEST_SUMMARY, summary)
            .putString(KEY_LAST_REQUEST_TIME, timestamp)
            .apply()
    }

    fun isEasterEggEligible(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_EASTER_EGG_ELIGIBLE)) {
            val eligible = Random.nextInt(100) < 70
            prefs.edit().putBoolean(KEY_EASTER_EGG_ELIGIBLE, eligible).apply()
        }
        return prefs.getBoolean(KEY_EASTER_EGG_ELIGIBLE, false)
    }
}
