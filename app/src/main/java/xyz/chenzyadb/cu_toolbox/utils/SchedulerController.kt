package xyz.chenzyadb.cu_toolbox.utils

import android.content.Context
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.InputStream

object SchedulerController {
    const val SCHEDULER_WORKING = 0
    const val SCHEDULER_INITIALIZING = 1
    const val SCHEDULER_NOT_WORKING = 2

    private val mutex = Mutex()
    private var dynamicModeFile: File? = null
    private var defaultMode: String = "balance"
    private val dynamicModeMap: HashMap<String, String> = hashMapOf()

    fun init(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val filesPath = context.filesDir.absolutePath
            CreateDir("${filesPath}/scheduler")
            CreateDir("${filesPath}/scheduler_settings")

            mutex.withLock {
                if (dynamicModeFile != null) {
                    saveData()
                    return@withLock
                }
                dynamicModeFile = File("${filesPath}/scheduler_settings/dynamic_mode.json")
                if (!dynamicModeFile!!.exists()) {
                    dynamicModeFile!!.createNewFile()
                    val dynamicModeJSON = JSONObject()
                    dynamicModeJSON["defaultMode"] = "balance"
                    dynamicModeJSON["dynamicModeList"] = JSONArray()
                    dynamicModeFile!!.writeText(dynamicModeJSON.toJSONString(), Charsets.UTF_8)
                } else {
                    readData()
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sync() {
        GlobalScope.launch {
            mutex.withLock {
                saveData()
            }
        }
    }

    fun runScheduler(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val filesPath = context.filesDir.absolutePath
            DeleteDir("${filesPath}/scheduler")
            if (!IsPathExists("${filesPath}/custom_config/scheduler.zip")) {
                CopyAssetsFile(context, "scheduler/release.zip", "${filesPath}/scheduler.zip")
                UnzipFile("${filesPath}/scheduler.zip", "${filesPath}/scheduler")
                DeleteFile("${filesPath}/scheduler.zip")
            } else {
                UnzipFile("${filesPath}/custom_config/scheduler.zip", "${filesPath}/scheduler")
            }

            mutex.withLock {
                WriteFile("${filesPath}/scheduler/mode.txt", defaultMode)
            }

            if (IsPathExists("${filesPath}/scheduler/CuDaemon") &&
                IsPathExists("${filesPath}/scheduler/start_daemon.sh")
            ) {
                Shell.cmd("chmod -R 0777 ${filesPath}/scheduler").exec()
                Shell.cmd("sh ${filesPath}/scheduler/start_daemon.sh").exec()
            }
        }
    }

    fun runDynamicMode(context: Context, pkgName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val filesPath = context.filesDir.absolutePath
                if (dynamicModeMap.containsKey(pkgName)) {
                    WriteFile("${filesPath}/scheduler/mode.txt", dynamicModeMap[pkgName]!!)
                } else {
                    WriteFile("${filesPath}/scheduler/mode.txt", defaultMode)
                }
            }
        }
    }

    fun getSchedulerStatus(): Int {
        val scriptPid = ArrayList<String>()
        val daemonPid = ArrayList<String>()
        Shell.cmd("pgrep -f \"start_daemon.sh\"").to(scriptPid).exec()
        if (scriptPid.isEmpty()) {
            Shell.cmd("pgrep -f \"CuDaemon\"").to(daemonPid).exec()
        }
        return when {
            scriptPid.isNotEmpty() -> SCHEDULER_INITIALIZING
            daemonPid.isNotEmpty() -> SCHEDULER_WORKING
            else -> SCHEDULER_NOT_WORKING
        }
    }

    fun getSchedulerMode(context: Context): String {
        val filesPath = context.filesDir.absolutePath
        return ReadFile("${filesPath}/scheduler/mode.txt")
    }

    fun getDefaultMode(): String {
        var mode: String
        runBlocking {
            mutex.withLock {
                mode = defaultMode
            }
        }
        return mode
    }

    fun getDynamicModeMap(): HashMap<String, String> {
        var map: HashMap<String, String>
        runBlocking {
            mutex.withLock {
                map = HashMap(dynamicModeMap)
            }
        }
        return map
    }

    fun setDefaultMode(mode: String) {
        runBlocking {
            mutex.withLock {
                defaultMode = mode
            }
        }
    }

    fun setDynamicMode(context: Context, pkgName: String, mode: String) {
        runBlocking {
            mutex.withLock {
                if (pkgName == context.packageName) {
                    return@withLock
                }
                dynamicModeMap[pkgName] = mode
            }
        }
    }

    fun removeDynamicMode(pkgName: String) {
        runBlocking {
            mutex.withLock {
                if (dynamicModeMap.containsKey(pkgName)) {
                    dynamicModeMap.remove(pkgName)
                }
            }
        }
    }

    fun isDynamicMode(pkgName: String): Boolean {
        var dynamicMode: Boolean
        runBlocking {
            mutex.withLock {
                dynamicMode = dynamicModeMap.containsKey(pkgName)
            }
        }
        return dynamicMode
    }

    fun importCustomConfig(context: Context, inputStream: InputStream) {
        val filesPath = context.filesDir.absolutePath
        CreateDir("${filesPath}/custom_config")
        val customConfigFile = File("${filesPath}/custom_config/scheduler.zip")
        customConfigFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    fun deleteCustomConfig(context: Context) {
        val filesPath = context.filesDir.absolutePath
        DeleteDir("${filesPath}/custom_config")
    }

    fun isCustomConfigImported(context: Context): Boolean {
        val filesPath = context.filesDir.absolutePath
        return IsPathExists("${filesPath}/custom_config/scheduler.zip")
    }

    private fun readData() {
        if (dynamicModeFile == null) {
            return
        }
        try {
            val dynamicModeJSON = JSON.parseObject(dynamicModeFile!!.readText(Charsets.UTF_8))
            defaultMode = dynamicModeJSON.getString("defaultMode")
            val dynamicModeList = dynamicModeJSON.getJSONArray("dynamicModeList")
            for (idx in 0 until dynamicModeList.size) {
                val dynamicModeItem = dynamicModeList.getJSONObject(idx)
                val pkgName = dynamicModeItem.getString("pkgName")
                dynamicModeMap[pkgName] = dynamicModeItem.getString("mode")
            }
        } catch (e: Exception) {
            dynamicModeFile!!.delete()
            e.printStackTrace()
        }
    }

    private fun saveData() {
        if (dynamicModeFile == null) {
            return
        }
        try {
            val dynamicModeJSON = JSON.parseObject(dynamicModeFile!!.readText(Charsets.UTF_8))
            dynamicModeJSON["defaultMode"] = defaultMode
            val dynamicModeList = JSONArray()
            for ((pkgName, mode) in dynamicModeMap.entries) {
                val dynamicModeItem = JSONObject()
                dynamicModeItem["pkgName"] = pkgName
                dynamicModeItem["mode"] = mode
                dynamicModeList.add(dynamicModeItem)
            }
            dynamicModeJSON["dynamicModeList"] = dynamicModeList
            dynamicModeFile!!.writeText(dynamicModeJSON.toJSONString(), Charsets.UTF_8)
        } catch (e: Exception) {
            dynamicModeFile!!.delete()
            e.printStackTrace()
        }
    }
}