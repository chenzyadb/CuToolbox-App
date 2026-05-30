package xyz.chenzyadb.cu_toolbox

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.chenzyadb.cu_toolbox.ui.theme.CuToolboxTheme
import xyz.chenzyadb.cu_toolbox.utils.SchedulerController


class SchedulerModeQuickSwitchActivity : ComponentActivity() {
    private var appPkgName: String by mutableStateOf("")
    private var schedulerMode: String by mutableStateOf("balance")
    private var dynamicMode: Boolean by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPkgName = intent.getStringExtra("pkgName").toString()
        schedulerMode = SchedulerController.getSchedulerMode(applicationContext)
        dynamicMode = SchedulerController.isDynamicMode(appPkgName)
        setContent {
            CuToolboxTheme {
                Column(
                    modifier = Modifier
                        .height(140.dp)
                        .width(300.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 5.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .width(280.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier
                                .height(40.dp)
                                .width(250.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            val appInfo = packageManager.getApplicationInfo(
                                appPkgName,
                                PackageManager.GET_META_DATA
                            )
                            val appLabel = packageManager.getApplicationLabel(appInfo).toString()
                            Text(
                                modifier = Modifier.height(20.dp),
                                text = appLabel,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                modifier = Modifier.height(20.dp),
                                text = appPkgName,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        TextButton(
                            onClick = { finish() },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .height(25.dp)
                                .width(25.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp)
                            )
                        }
                    }
                    SchedulerModeSwitcher()
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (FloatMonitorService.isServiceCreated()) {
                                    val intent =
                                        Intent(applicationContext, FloatMonitorService::class.java)
                                    stopService(intent)
                                } else {
                                    if (!Settings.canDrawOverlays(applicationContext)) {
                                        Toast.makeText(
                                            applicationContext,
                                            "请授权悬浮窗权限",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        val intent = Intent()
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        intent.action =
                                            "android.settings.APPLICATION_DETAILS_SETTINGS"
                                        intent.data = Uri.fromParts("package", packageName, null)
                                        startActivity(intent)
                                    } else {
                                        val intent = Intent(
                                            applicationContext,
                                            FloatMonitorService::class.java
                                        )
                                        startService(intent)
                                    }
                                }
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .height(25.dp)
                                .width(25.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.monitor),
                                contentDescription = "FloatMonitor",
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp)
                            )
                        }
                        TextButton(
                            onClick = {
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                startActivity(intent)
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .height(25.dp)
                                .width(25.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.home),
                                contentDescription = "GoToAppMainActivity",
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DynamicModeSwitcher()
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

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onDestroy() {
        SchedulerController.sync()
        super.onDestroy()
    }

    @Composable
    private fun SchedulerModeSwitcher() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.Center,
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
                val schedulerModes = arrayOf("powersave", "balance", "performance", "fast")
                val modeNameMap = hashMapOf(
                    "powersave" to "省电",
                    "balance" to "均衡",
                    "performance" to "性能",
                    "fast" to "极速"
                )
                schedulerModes.forEach {
                    val buttonColor = when (it) {
                        schedulerMode -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                    val textColor = when (it) {
                        schedulerMode -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                    TextButton(
                        onClick = {
                            if (schedulerMode != it) {
                                if (dynamicMode) {
                                    SchedulerController.setDynamicMode(
                                        applicationContext,
                                        appPkgName,
                                        it
                                    )
                                    SchedulerController.runDynamicMode(
                                        applicationContext,
                                        appPkgName
                                    )
                                } else {
                                    SchedulerController.setDefaultMode(it)
                                    SchedulerController.runDynamicMode(
                                        applicationContext,
                                        appPkgName
                                    )
                                }
                                schedulerMode = it
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

    @Composable
    private fun DynamicModeSwitcher() {
        TextButton(
            onClick = {
                dynamicMode = !dynamicMode
                if (dynamicMode) {
                    SchedulerController.setDynamicMode(
                        applicationContext,
                        appPkgName,
                        schedulerMode
                    )
                } else {
                    SchedulerController.removeDynamicMode(appPkgName)
                    SchedulerController.setDefaultMode(schedulerMode)
                }
            },
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .height(25.dp)
                .width(85.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.width(55.dp),
                    text = "动态模式",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                val toggleId = when {
                    dynamicMode -> R.drawable.toggle_on
                    else -> R.drawable.toggle_off
                }
                Image(
                    painter = painterResource(id = toggleId),
                    contentDescription = null,
                    modifier = Modifier
                        .height(24.dp)
                        .width(24.dp)
                )
            }
        }
    }
}