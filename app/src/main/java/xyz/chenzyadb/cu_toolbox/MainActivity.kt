package xyz.chenzyadb.cu_toolbox

import android.app.ActivityManager
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topjohnwu.superuser.Shell
import xyz.chenzyadb.cu_toolbox.ui.theme.CuToolboxTheme
import xyz.chenzyadb.cu_toolbox.ui.theme.reimuDarkRed
import xyz.chenzyadb.cu_toolbox.ui.theme.reimuGray
import xyz.chenzyadb.cu_toolbox.ui.theme.reimuRed
import xyz.chenzyadb.cu_toolbox.utils.SchedulerController
import java.util.*


class MainActivity : ComponentActivity() {
    private var timer: Timer? = null
    private var schedulerStatus: Int by mutableIntStateOf(SchedulerController.SCHEDULER_NOT_WORKING)
    private var schedulerDefaultMode: String by mutableStateOf("balance")
    private var customConfigImported: Boolean by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkSuperUserEnvironment()
        SchedulerController.init(applicationContext)
        updateSchedulerStats()

        setContent {
            CuToolboxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            Row(
                                modifier = Modifier
                                    .padding(top = 10.dp, start = 30.dp)
                                    .height(50.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.mipmap.ic_launcher_round),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(40.dp)
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(start = 20.dp),
                                    text = "CuToolbox",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = it.calculateTopPadding() + 20.dp,
                                    start = 20.dp,
                                    end = 20.dp
                                )
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SchedulerStatusBar()
                            DefaultModeBar()
                            CustomConfigBar()
                            DynamicModeListEditorButton()
                            LogReaderButton()
                            AdditionalFunctionButton()
                            SettingButton()
                            AboutButton()
                        }
                    }
                }
            }
        }
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        val configContext = createConfigurationContext(resources.configuration)
        return configContext.resources.apply {
            configuration.fontScale = 1.0f
        }
    }

    override fun onStart() {
        super.onStart()
        startTimer()
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
                val tasks = activityManager.appTasks.filterNotNull()
                tasks.forEach {
                    it.setExcludeFromRecents(true)
                }
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
        return super.onKeyUp(keyCode, event)
    }

    @Composable
    private fun SchedulerStatusBar() {
        val schedulerStatusColorMap = hashMapOf(
            SchedulerController.SCHEDULER_WORKING to reimuDarkRed,
            SchedulerController.SCHEDULER_INITIALIZING to reimuRed,
            SchedulerController.SCHEDULER_NOT_WORKING to reimuGray
        )
        val schedulerStatusIconIdMap = hashMapOf(
            SchedulerController.SCHEDULER_WORKING to R.drawable.working,
            SchedulerController.SCHEDULER_INITIALIZING to R.drawable.initializing,
            SchedulerController.SCHEDULER_NOT_WORKING to R.drawable.not_working
        )
        val schedulerStatusTitleMap = hashMapOf(
            SchedulerController.SCHEDULER_WORKING to "调度工作中",
            SchedulerController.SCHEDULER_INITIALIZING to "调度启动中",
            SchedulerController.SCHEDULER_NOT_WORKING to "调度未工作"
        )
        val schedulerStatusHintMap = hashMapOf(
            SchedulerController.SCHEDULER_WORKING to "没有可执行的操作",
            SchedulerController.SCHEDULER_INITIALIZING to "启动过程中请勿操作",
            SchedulerController.SCHEDULER_NOT_WORKING to "点击此处启动调度"
        )
        TextButton(
            onClick = {
                SchedulerController.runScheduler(applicationContext)
            },
            modifier = Modifier
                .height(70.dp)
                .fillMaxWidth()
                .background(
                    shape = RoundedCornerShape(10.dp),
                    color = schedulerStatusColorMap[schedulerStatus]!!
                ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = schedulerStatusIconIdMap[schedulerStatus]!!),
                    contentDescription = null,
                    modifier = Modifier
                        .height(36.dp)
                        .width(36.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .fillMaxHeight()
                        .width(180.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = schedulerStatusTitleMap[schedulerStatus]!!,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = schedulerStatusHintMap[schedulerStatus]!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    AnimatedVisibility(visible = (schedulerStatus == SchedulerController.SCHEDULER_WORKING)) {
                        Image(
                            painter = painterResource(id = R.drawable.reimu_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .height(60.dp)
                                .width(60.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DefaultModeBar() {
        var showModeSwitcher by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(
                onClick = { showModeSwitcher = !showModeSwitcher },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.tune),
                        contentDescription = null,
                        modifier = Modifier
                            .height(28.dp)
                            .width(28.dp)
                    )
                    Text(
                        modifier = Modifier.padding(start = 20.dp),
                        text = "默认模式",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = schedulerDefaultMode,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = reimuRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            AnimatedVisibility(visible = showModeSwitcher) {
                Row(
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .fillMaxWidth()
                        .height(50.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .width(250.dp)
                            .height(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val schedulerModes = listOf("powersave", "balance", "performance", "fast")
                        val modeNameMap =
                            hashMapOf(
                                "powersave" to "省电",
                                "balance" to "均衡",
                                "performance" to "性能",
                                "fast" to "极速"
                            )
                        schedulerModes.forEach {
                            val buttonColor = when (it) {
                                schedulerDefaultMode -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                            val textColor = when (it) {
                                schedulerDefaultMode -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                            TextButton(
                                onClick = {
                                    if (it != schedulerDefaultMode) {
                                        SchedulerController.setDefaultMode(it)
                                        SchedulerController.runDynamicMode(
                                            applicationContext, packageName
                                        )
                                        SchedulerController.sync()
                                        schedulerDefaultMode = it
                                    }
                                },
                                modifier = Modifier
                                    .background(
                                        color = buttonColor,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .height(35.dp)
                                    .width(60.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = modeNameMap[it]!!,
                                        color = textColor,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CustomConfigBar() {
        var showCustomConfigManager by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(
                onClick = { showCustomConfigManager = !showCustomConfigManager },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.custom_config),
                        contentDescription = null,
                        modifier = Modifier
                            .height(28.dp)
                            .width(28.dp)
                    )
                    Text(
                        modifier = Modifier.padding(start = 20.dp),
                        text = "配置文件",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val configStatus = if (customConfigImported) "自定义" else "默认"
                        Text(
                            text = configStatus,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = reimuRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            AnimatedVisibility(visible = showCustomConfigManager) {
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (customConfigImported) {
                        Text(
                            modifier = Modifier.width(250.dp),
                            text = "已导入自定义配置",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    SchedulerController.deleteCustomConfig(applicationContext)
                                    SchedulerController.runScheduler(applicationContext)
                                },
                                modifier = Modifier
                                    .width(25.dp)
                                    .height(25.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.delete_config),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(24.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "尚未导入自定义配置",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    importCustomConfig()
                                },
                                modifier = Modifier
                                    .width(25.dp)
                                    .height(25.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.import_config),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DynamicModeListEditorButton() {
        TextButton(
            onClick = {
                val intent = Intent(applicationContext, DynamicModeListEditorActivity::class.java)
                startActivity(intent)
            },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(top = 10.dp)
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.list),
                    contentDescription = null,
                    modifier = Modifier
                        .height(28.dp)
                        .width(28.dp)
                )
                Text(
                    modifier = Modifier.padding(start = 20.dp),
                    text = "动态模式清单编辑",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_forward),
                        contentDescription = null,
                        modifier = Modifier
                            .height(16.dp)
                            .width(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun LogReaderButton() {
        TextButton(
            onClick = {
                val intent = Intent(applicationContext, LogReaderActivity::class.java)
                startActivity(intent)
            },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(top = 5.dp)
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.log),
                    contentDescription = null,
                    modifier = Modifier
                        .height(28.dp)
                        .width(28.dp)
                )
                Text(
                    modifier = Modifier.padding(start = 20.dp),
                    text = "日志查看",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_forward),
                        contentDescription = null,
                        modifier = Modifier
                            .height(16.dp)
                            .width(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun AdditionalFunctionButton() {
        TextButton(
            onClick = {
                val intent = Intent(applicationContext, AdditionalFunctionActivity::class.java)
                startActivity(intent)
            },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(top = 5.dp)
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.apps),
                    contentDescription = null,
                    modifier = Modifier
                        .height(28.dp)
                        .width(28.dp)
                )
                Text(
                    modifier = Modifier.padding(start = 20.dp),
                    text = "扩展功能",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_forward),
                        contentDescription = null,
                        modifier = Modifier
                            .height(16.dp)
                            .width(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingButton() {
        TextButton(
            onClick = {
                val intent = Intent(applicationContext, SettingActivity::class.java)
                startActivity(intent)
            },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(top = 5.dp)
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = null,
                    modifier = Modifier
                        .height(28.dp)
                        .width(28.dp)
                )
                Text(
                    modifier = Modifier.padding(start = 20.dp),
                    text = "设置",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_forward),
                        contentDescription = null,
                        modifier = Modifier
                            .height(16.dp)
                            .width(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun AboutButton() {
        TextButton(
            onClick = {
                val intent = Intent(applicationContext, AboutActivity::class.java)
                startActivity(intent)
            },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(top = 5.dp)
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.about),
                    contentDescription = null,
                    modifier = Modifier
                        .height(28.dp)
                        .width(28.dp)
                )
                Text(
                    modifier = Modifier.padding(start = 20.dp),
                    text = "关于",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_forward),
                        contentDescription = null,
                        modifier = Modifier
                            .height(16.dp)
                            .width(16.dp)
                    )
                }
            }
        }
    }

    private fun startTimer() {
        if (timer != null) {
            return
        }
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                updateSchedulerStats()
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

    private val openFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }
            contentResolver.openInputStream(uri).use { inputStream ->
                if (inputStream == null) {
                    return@use
                }
                SchedulerController.importCustomConfig(applicationContext, inputStream)
            }
            SchedulerController.runScheduler(applicationContext)
            Toast.makeText(applicationContext, "导入自定义配置成功", Toast.LENGTH_LONG).show()
        }

    private fun importCustomConfig() {
        try {
            val openType = arrayOf("application/zip")
            openFile.launch(openType)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "导入自定义配置失败", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun updateSchedulerStats() {
        schedulerStatus = SchedulerController.getSchedulerStatus()
        schedulerDefaultMode = SchedulerController.getDefaultMode()
        customConfigImported = SchedulerController.isCustomConfigImported(applicationContext)
    }

    private fun checkSuperUserEnvironment() {
        Shell.cmd("su -c true").exec()
        val grantedRoot = Shell.isAppGrantedRoot()
        if (grantedRoot == null || !grantedRoot) {
            Toast.makeText(applicationContext, "未能获得ROOT权限", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

