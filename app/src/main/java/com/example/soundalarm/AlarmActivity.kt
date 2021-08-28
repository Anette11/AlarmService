package com.example.soundalarm

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.soundalarm.databinding.ActivityMainBinding

class AlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object ConstantsForIntent {
        private const val SCHEME = "package"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initialize()
        checkIfPermissionAboutContextCanDrawOnTopOfOtherAppsIsGranted()
    }

    private fun initialize() {
        val isServiceExists: Boolean = AlarmSharedPreferences.checkServiceIfExistsStatus()
        changeButtonBackgroundAccordingToIfServiceIsAlreadyExists(isServiceExists)
        buttonAddAlarmServiceSetOnClickListener()
    }

    private fun changeButtonBackgroundAccordingToIfServiceIsAlreadyExists(isServiceExists: Boolean) =
        when {
            isServiceExists -> {
                setButtonBackgroundAsDeleteService()
            }
            else -> {
                setButtonBackgroundAsCreateService()
            }
        }

    private fun buttonAddAlarmServiceSetOnClickListener() {
        binding.buttonAddAlarmService.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                checkIfPermissionAboutContextCanDrawOnTopOfOtherAppsIsGranted()
            } else {
                var isServiceExists: Boolean = AlarmSharedPreferences.checkServiceIfExistsStatus()
                when {
                    isServiceExists -> {
                        stopAlarmService()
                        isServiceExists = !isServiceExists
                        AlarmSharedPreferences.saveServiceIsExistsStatus(isServiceExists)
                        changeButtonBackgroundAccordingToIfServiceIsAlreadyExists(isServiceExists)
                    }
                    else -> {
                        startAlarmService()
                        isServiceExists = !isServiceExists
                        AlarmSharedPreferences.saveServiceIsExistsStatus(isServiceExists)
                        changeButtonBackgroundAccordingToIfServiceIsAlreadyExists(isServiceExists)
                    }
                }
            }
        }
    }

    private fun startAlarmService() {
        Intent(this, AlarmService::class.java).also { intent -> startService(intent) }
    }

    private fun stopAlarmService() {
        Intent(this, AlarmService::class.java).also { intent -> stopService(intent) }
    }

    private fun setButtonBackgroundAsDeleteService() {
        val buttonColor = R.color.gray
        val buttonText = resources.getString(R.string.delete_alarm_service)
        setButtonBackgroundColorAndText(buttonColor, buttonText)
        AlarmSharedPreferences.saveButtonBackgroundText(buttonText)
    }

    private fun setButtonBackgroundAsCreateService() {
        val buttonColor = R.color.orange
        val buttonText = resources.getString(R.string.create_alarm_service)
        setButtonBackgroundColorAndText(buttonColor, buttonText)
        AlarmSharedPreferences.saveButtonBackgroundText(buttonText)
    }

    private fun setButtonBackgroundColorAndText(buttonColor: Int, buttonText: String) {
        binding.buttonAddAlarmService.backgroundTintList =
            ColorStateList.valueOf(ResourcesCompat.getColor(resources, buttonColor, null))
        binding.buttonAddAlarmService.text = buttonText
    }

    private var activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            checkIfPermissionAboutContextCanDrawOnTopOfOtherAppsIsGranted()
        }
    }

    private fun checkIfPermissionAboutContextCanDrawOnTopOfOtherAppsIsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            val intent = Intent().apply {
                action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                data = Uri.fromParts(SCHEME, packageName, null)
            }
            activityResultLauncher.launch(intent)
        }
    }
}