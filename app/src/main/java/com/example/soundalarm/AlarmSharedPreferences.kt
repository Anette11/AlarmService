package com.example.soundalarm

import android.content.Context
import android.content.SharedPreferences

object AlarmSharedPreferences {
    @Volatile
    private lateinit var sharedPreferences: SharedPreferences

    private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"
    private const val SHARED_PREFERENCES_KEY_ICON_TYPE = "SHARED_PREFERENCES_KEY_ICON_TYPE"
    private const val SHARED_PREFERENCES_BUTTON_TEXT = "SHARED_PREFERENCES_BUTTON_TEXT"
    private const val SHARED_PREFERENCES_IS_SERVICE_EXISTS = "SHARED_PREFERENCES_IS_SERVICE_EXISTS"
    private const val SHARED_PREFERENCES_ICON_PLAY = "SHARED_PREFERENCES_ICON_PLAY"
    private const val SHARED_PREFERENCES_ICON_STOP = "SHARED_PREFERENCES_ICON_STOP"

    val SHARED_PREFERENCES_ICON_PLAY_ get() = SHARED_PREFERENCES_ICON_PLAY
    val SHARED_PREFERENCES_ICON_STOP_ get() = SHARED_PREFERENCES_ICON_STOP

    fun initializeSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun saveButtonBackgroundIconType(buttonIconType: String) {
        val sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_KEY_ICON_TYPE, buttonIconType)
        sharedPreferencesEditor.apply()
    }

    fun getSavedButtonBackgroundIconType(): String? =
        sharedPreferences.getString(SHARED_PREFERENCES_KEY_ICON_TYPE, SHARED_PREFERENCES_ICON_PLAY)

    fun saveButtonBackgroundText(buttonText: String) {
        val sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_BUTTON_TEXT, buttonText)
        sharedPreferencesEditor.apply()
    }

    fun saveServiceIsExistsStatus(isServiceExists: Boolean) {
        val sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()
        sharedPreferencesEditor.putBoolean(SHARED_PREFERENCES_IS_SERVICE_EXISTS, isServiceExists)
        sharedPreferencesEditor.apply()
    }

    fun checkServiceIfExistsStatus(): Boolean =
        sharedPreferences.getBoolean(SHARED_PREFERENCES_IS_SERVICE_EXISTS, false)
}