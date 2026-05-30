package xyz.chenzyadb.cu_toolbox

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import dev.jeziellago.compose.markdowntext.MarkdownText
import xyz.chenzyadb.cu_toolbox.ui.theme.CuToolboxTheme
import xyz.chenzyadb.cu_toolbox.ui.theme.reimuBlue
import xyz.chenzyadb.cu_toolbox.ui.theme.reimuRed
import xyz.chenzyadb.cu_toolbox.utils.ReadAssetsFile


class AboutActivity : ComponentActivity() {
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
                                    text = "关于",
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
                            ApplicationAboutContent()
                            ProjectRepoButton()
                            OpenSourceLicensesText()
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

    @Suppress("deprecation")
    @Composable
    private fun ApplicationAboutContent() {
        val applicationIcon =
            packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
        val applicationLabel = packageManager.getApplicationLabel(applicationInfo).toString()
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.versionCode
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                .fillMaxWidth()
                .height(180.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                modifier = Modifier
                    .height(80.dp)
                    .width(80.dp),
                bitmap = applicationIcon,
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(top = 5.dp),
                text = applicationLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = reimuRed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.padding(top = 5.dp),
                text = "V$versionName ($versionCode)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "chenzyadb@github.com",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun ProjectRepoButton() {
        TextButton(
            onClick = {
                try {
                    val intent = Intent()
                    intent.action = "android.intent.action.VIEW"
                    val uri = "https://github.com/chenzyadb/CuprumTurbo-Scheduler".toUri()
                    intent.data = uri
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "启动系统浏览器失败", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            },
            modifier = Modifier
                .padding(top = 5.dp)
                .height(50.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "访问项目仓库",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
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
    private fun OpenSourceLicensesText() {
        var showText by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TextButton(
                onClick = {
                    showText = !showText
                },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "开放源代码许可",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
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
            AnimatedVisibility(visible = showText) {
                val markdownText = ReadAssetsFile(applicationContext, "docs/opensource_licenses.md")
                MarkdownText(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 5.dp)
                        .fillMaxWidth(),
                    markdown = markdownText,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Left
                    ),
                    linkColor = reimuBlue
                )
            }
        }
    }
}