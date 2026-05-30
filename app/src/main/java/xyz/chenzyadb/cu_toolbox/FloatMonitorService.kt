package xyz.chenzyadb.cu_toolbox

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.topjohnwu.superuser.Shell
import xyz.chenzyadb.cu_toolbox.utils.CopyAssetsFile
import xyz.chenzyadb.cu_toolbox.utils.CreateDir
import java.io.File
import java.util.*


class FloatMonitorService : Service() {
    companion object {
        private var serviceCreated = false
        fun isServiceCreated(): Boolean {
            return serviceCreated
        }
    }

    private var floatWindow: TextView? = null
    private var timer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, START_FLAG_REDELIVERY, startId)
    }

    override fun onCreate() {
        super.onCreate()
        serviceCreated = true
        startMonitorProcess()
        createWindow()
        startTimer()
    }

    override fun onDestroy() {
        stopMonitorProcess()
        stopTimer()
        destroyWindow()
        serviceCreated = false
        super.onDestroy()
    }

    private fun startTimer() {
        if (timer != null) {
            return
        }
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                updateMonitorText()
            }
        }, 0, 1000)
    }

    private fun stopTimer() {
        if (timer == null) {
            return
        }
        timer!!.cancel()
        timer = null
    }

    @SuppressLint("SetTextI18n")
    private fun createWindow() {
        if (floatWindow != null) {
            return
        }
        floatWindow = TextView(this)
        floatWindow!!.setBackgroundColor(Color(0x88000000).toArgb())
        floatWindow!!.setPadding(20, 10, 20, 10)
        floatWindow!!.setTextColor(Color(0xFFFFFFFF).toArgb())
        floatWindow!!.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8f)
        val typeface = ResourcesCompat.getFont(applicationContext, R.font.jetbrainsmono)
        floatWindow!!.typeface = typeface
        floatWindow!!.text = ""

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        layoutParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE + WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.TOP or Gravity.END
        layoutParams.alpha = 0.6f
        windowManager.addView(floatWindow, layoutParams)
    }

    private fun destroyWindow() {
        if (floatWindow == null) {
            return
        }
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.removeViewImmediate(floatWindow)
        floatWindow = null
    }

    private fun updateMonitorText() {
        val filesPath = applicationContext.filesDir.absolutePath
        val monitorFile = File("${filesPath}/monitor/monitor.txt")
        if (!monitorFile.exists()) {
            return
        }
        val monitorText = monitorFile.readText(Charsets.UTF_8)
        if (monitorText.isEmpty()) {
            return
        }
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            if (floatWindow == null) {
                return@post
            }
            floatWindow!!.text = monitorText.substringBeforeLast('\n')
        }
    }

    private fun startMonitorProcess() {
        val filesPath = applicationContext.filesDir.absolutePath
        CreateDir("${filesPath}/monitor")
        CopyAssetsFile(applicationContext, "binaries/ct_monitor", "${filesPath}/monitor/ct_monitor")
        Shell.cmd("chmod 0777 ${filesPath}/monitor/ct_monitor").exec()
        Shell.cmd("${filesPath}/monitor/ct_monitor").exec()
    }

    private fun stopMonitorProcess() {
        Shell.cmd("kill $(pgrep -f ct_monitor)").exec()
    }
}

