package xyz.chenzyadb.cu_toolbox

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import xyz.chenzyadb.cu_toolbox.utils.SchedulerController
import java.util.*

class BackgroundAccessibilityService : AccessibilityService() {
    companion object {
        private var serviceCreated: Boolean = false
        fun isServiceCreated(): Boolean {
            return serviceCreated
        }
    }

    private val notificationId: Int = 0x0d000721
    private var timer: Timer? = null
    private var deviceScreenSize: Long = 0
    private var foregroundPkgName: String = "unknown"
    private var notificationManager: NotificationManager? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        deviceScreenSize = getDeviceScreenSize()

        SchedulerController.init(applicationContext)
        SchedulerController.runScheduler(applicationContext)

        initNotification()
        updateNotification()
        startTimer()
        registerBroadcastReceiver()
    }

    override fun onInterrupt() {
        SchedulerController.sync()
    }

    override fun onCreate() {
        super.onCreate()
        serviceCreated = true
    }

    override fun onDestroy() {
        SchedulerController.sync()
        unregisterBroadcastReceiver()
        stopTimer()
        cancelNotification()
        serviceCreated = false
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        updateForegroundPkgName()
    }

    private fun updateForegroundPkgName() {
        val isInBlackList: (pkgName: String) -> Boolean = { pkgName ->
            val blackListSymbols = listOf(
                "systemui",
                "screenshot",
                "com.miui.securitycenter"
            )
            var inBlackList = (pkgName == "android")
            for (symbol in blackListSymbols) {
                if (pkgName.contains(symbol)) {
                    inBlackList = true
                    break
                }
            }
            inBlackList
        }

        var topWindowPkgName = foregroundPkgName
        try {
            var maxWindowSize = 0L
            windows.forEach { window ->
                val pkgName = window.root?.packageName?.toString()
                if (pkgName != null && !isInBlackList(pkgName)) {
                    val windowRect = Rect()
                    window.getBoundsInScreen(windowRect)
                    val windowSize = windowRect.width().toLong() * windowRect.height()
                    if (windowSize > (deviceScreenSize / 2) && windowSize > maxWindowSize) {
                        topWindowPkgName = pkgName
                        maxWindowSize = windowSize
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (topWindowPkgName != foregroundPkgName) {
            foregroundPkgName = topWindowPkgName
            SchedulerController.runDynamicMode(applicationContext, topWindowPkgName)
            updateNotification()
        }
    }

    private fun initNotification() {
        if (notificationManager == null) {
            notificationManager = getSystemService(NotificationManager::class.java)
            val notificationChannel = NotificationChannel(
                "CuToolbox",
                "BackgroundService",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setSound(null, null)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

    private fun updateNotification() {
        if (notificationManager != null) {
            val intent = Intent(applicationContext, SchedulerModeQuickSwitchActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("pkgName", foregroundPkgName)
            val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_MUTABLE
            val pendingIntent =
                PendingIntent.getActivity(applicationContext, 0, intent, pendingIntentFlags)

            val notificationTitle = "当前应用: ${foregroundPkgName}"
            val dynamicModeState = when {
                SchedulerController.isDynamicMode(foregroundPkgName) -> "[动态模式]"
                else -> "[默认模式]"
            }
            val schedulerCurMode = SchedulerController.getSchedulerMode(applicationContext)
            val notificationText = "调度模式: ${schedulerCurMode}  ${dynamicModeState}"

            val notification =
                NotificationCompat.Builder(applicationContext, "CuToolbox")
                    .setSmallIcon(R.drawable.noti_icon)
                    .setTicker("CuToolbox")
                    .setContentIntent(pendingIntent)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setContentIntent(pendingIntent)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setSound(null)
                    .setVibrate(null)
                    .build()
            notification.flags = Notification.FLAG_ONGOING_EVENT or Notification.FLAG_NO_CLEAR
            notificationManager!!.notify(notificationId, notification)
        }
    }

    private fun cancelNotification() {
        notificationManager?.cancel(notificationId)
    }

    private fun startTimer() {
        if (timer != null) {
            return
        }
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                updateForegroundPkgName()
                updateNotification()
            }
        }, 0, 2000)
    }

    private fun stopTimer() {
        if (timer == null) {
            return
        }
        timer!!.cancel()
        timer = null
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    SchedulerController.sync()
                    stopTimer()
                }

                Intent.ACTION_SCREEN_ON -> {
                    startTimer()
                }
            }
        }
    }

    private fun registerBroadcastReceiver() {
        val broadcastFilter = IntentFilter()
        broadcastFilter.addAction(Intent.ACTION_SCREEN_ON)
        broadcastFilter.addAction(Intent.ACTION_SCREEN_OFF)
        broadcastFilter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
        registerReceiver(broadcastReceiver, broadcastFilter)
    }

    private fun unregisterBroadcastReceiver() {
        unregisterReceiver(broadcastReceiver)
    }

    private fun getDeviceScreenSize(): Long {
        val metrics = resources.displayMetrics
        return metrics.widthPixels.toLong() * metrics.heightPixels
    }
}