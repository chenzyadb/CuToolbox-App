package xyz.chenzyadb.cu_toolbox

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.topjohnwu.superuser.Shell
import xyz.chenzyadb.cu_toolbox.ui.theme.CuToolboxTheme
import xyz.chenzyadb.cu_toolbox.ui.theme.reimuDarkRed
import xyz.chenzyadb.cu_toolbox.utils.CopyAssetsFile
import xyz.chenzyadb.cu_toolbox.utils.CreateDir


class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                                    .height(50.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { finish() },
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(50.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.arrow_back),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .height(32.dp)
                                            .width(32.dp)
                                    )
                                }
                                Text(
                                    modifier = Modifier
                                        .padding(start = 10.dp),
                                    text = "设置",
                                    fontSize = 18.sp,
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
                                .padding(
                                    top = it.calculateTopPadding() + 10.dp,
                                    start = 20.dp,
                                    end = 20.dp
                                )
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BackgroundServiceSwitch()
                            RequireIgnoreBatteryOptimizationButton()
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

    @Composable
    private fun BackgroundServiceSwitch() {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 5.dp)
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "后台服务(无障碍)",
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
                var serviceCreated by remember { mutableStateOf(BackgroundAccessibilityService.isServiceCreated()) }
                Switch(
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = reimuDarkRed,
                        checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                        checkedBorderColor = MaterialTheme.colorScheme.outline,
                        uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    checked = serviceCreated,
                    onCheckedChange = {
                        serviceCreated = it
                        if (serviceCreated) {
                            startBackgroundService()
                        } else {
                            stopBackgroundService()
                        }
                    }
                )
            }
        }
    }

    @SuppressLint("BatteryLife")
    @Composable
    private fun RequireIgnoreBatteryOptimizationButton() {
        TextButton(
            onClick = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = ("package:" + this.packageName).toUri()
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
                Text(
                    text = "请求忽略电池优化",
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

    private fun startBackgroundService() {
        val filesPath = applicationContext.filesDir.absolutePath
        CreateDir("${filesPath}/scripts")
        CopyAssetsFile(
            applicationContext,
            "scripts/bg_service_manager.sh",
            "${filesPath}/scripts/bg_service_manager.sh"
        )
        Shell.cmd("chmod 0777 ${filesPath}/scripts/bg_service_manager.sh").exec()
        Shell.cmd("nohup sh ${filesPath}/scripts/bg_service_manager.sh \"enable\" >/dev/null 2>&1 &")
            .exec()
    }

    private fun stopBackgroundService() {
        val filesPath = applicationContext.filesDir.absolutePath
        CreateDir("${filesPath}/scripts")
        CopyAssetsFile(
            applicationContext,
            "scripts/bg_service_manager.sh",
            "${filesPath}/scripts/bg_service_manager.sh"
        )
        Shell.cmd("chmod 0777 ${filesPath}/scripts/bg_service_manager.sh").exec()
        Shell.cmd("nohup sh ${filesPath}/scripts/bg_service_manager.sh \"disable\" >/dev/null 2>&1 &")
            .exec()
    }
}