package com.example.soundalarm

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class AlarmService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var linearLayout: LinearLayout
    private lateinit var viewInflated: View
    private lateinit var alarmButton: ImageView
    private lateinit var job: Job
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun initialize() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val linearLayoutParams = getLinearLayoutParams()
        val layoutInflater = LayoutInflater.from(this)
        viewInflated = layoutInflater.inflate(R.layout.alarm_button_layout, null, false)
        linearLayout = LinearLayout(this).apply {
            layoutParams = linearLayoutParams
            addView(viewInflated)
        }
        val windowManagerLayoutParams = getWindowManagerLayoutParams()
        alarmButton = linearLayout.findViewById(R.id.alarm_button)
        setAlarmButtonIconType(
            R.drawable.ic_play,
            AlarmSharedPreferences.SHARED_PREFERENCES_ICON_PLAY_
        )
        setOnTouchListenerForAlarmButton()
        windowManager.addView(linearLayout, windowManagerLayoutParams)
    }

    private fun setAlarmButtonIconType(drawable: Int, iconType: String) {
        alarmButton.background = ContextCompat.getDrawable(this, drawable)
        AlarmSharedPreferences.saveButtonBackgroundIconType(iconType)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListenerForAlarmButton() {
        val windowManagerLayoutParamsUpdated = getWindowManagerLayoutParams()
        var positionForWindowX = 0
        var positionForWindowY = 0
        var coordinateOfEMotionEventX = 0f
        var coordinateOfEMotionEventY = 0f
        var impactPointX = 0f
        var impactPointY = 0f

        alarmButton.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    positionForWindowX = windowManagerLayoutParamsUpdated.x
                    positionForWindowY = windowManagerLayoutParamsUpdated.y
                    coordinateOfEMotionEventX = motionEvent.rawX
                    coordinateOfEMotionEventY = motionEvent.rawY
                    impactPointX = motionEvent.x
                    impactPointY = motionEvent.y
                }
                MotionEvent.ACTION_MOVE -> {
                    handleMotionEventActionMove(
                        windowManagerLayoutParamsUpdated,
                        positionForWindowX,
                        positionForWindowY,
                        coordinateOfEMotionEventX,
                        coordinateOfEMotionEventY,
                        motionEvent
                    )
                }
                MotionEvent.ACTION_UP -> {
                    handleMotionEventActionUp(
                        impactPointX,
                        impactPointY,
                        motionEvent
                    )
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun handleMotionEventActionMove(
        windowManagerLayoutParamsUpdated: WindowManager.LayoutParams,
        positionForWindowX: Int,
        positionForWindowY: Int,
        coordinateOfEMotionEventX: Float,
        coordinateOfEMotionEventY: Float,
        motionEvent: MotionEvent
    ) {
        windowManagerLayoutParamsUpdated.x =
            positionForWindowX + (motionEvent.rawX - coordinateOfEMotionEventX).toInt()
        windowManagerLayoutParamsUpdated.y =
            positionForWindowY + (motionEvent.rawY - coordinateOfEMotionEventY).toInt()
        windowManager.updateViewLayout(linearLayout, windowManagerLayoutParamsUpdated)
    }

    private fun handleMotionEventActionUp(
        impactPointX: Float,
        impactPointY: Float,
        motionEvent: MotionEvent
    ) {
        val distanceX = impactPointX - motionEvent.x
        val distanceY = impactPointY - motionEvent.y
        if (distanceX == 0f && distanceY == 0f) {
            onClickForCreateAlarmServiceButton()
        }
    }

    private fun onClickForCreateAlarmServiceButton() {
        when (AlarmSharedPreferences.getSavedButtonBackgroundIconType()) {
            AlarmSharedPreferences.SHARED_PREFERENCES_ICON_PLAY_ -> {
                setAlarmButtonIconType(
                    R.drawable.ic_stop,
                    AlarmSharedPreferences.SHARED_PREFERENCES_ICON_STOP_
                )
                startPlaySound()
            }
            else -> {
                setAlarmButtonIconType(
                    R.drawable.ic_play,
                    AlarmSharedPreferences.SHARED_PREFERENCES_ICON_PLAY_
                )
                stopPlaySound()
            }
        }
    }

    private fun startPlaySound() {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        job = scope.launch {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this@AlarmService, uri)
            }
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
    }

    private fun stopPlaySound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        job.cancel()
    }

    private fun getLinearLayoutParams(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

    private fun getWindowManagerLayoutParams(): WindowManager.LayoutParams =
        WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = getWindowManagerLayoutParamsType()
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
        }

    private fun getWindowManagerLayoutParamsType() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            stopPlaySound()
        }
        windowManager.removeView(linearLayout)
    }
}