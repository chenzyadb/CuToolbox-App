package xyz.chenzyadb.cu_toolbox

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.chenzyadb.cu_toolbox.ui.theme.CuToolboxTheme
import xyz.chenzyadb.cu_toolbox.ui.theme.reimuRed
import xyz.chenzyadb.cu_toolbox.utils.SchedulerController


class DynamicModeListEditorActivity : ComponentActivity() {
    private var dynamicModeStateMap = mutableStateMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadDynamicModeMap()
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
                                    text = "动态模式清单编辑",
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
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SearchBar()
                            if (dynamicModeStateMap.size > 0) {
                                DynamicModeList()
                            } else {
                                DynamicModeListEmptyHint()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        saveDynamicModeMap()
        super.onDestroy()
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        val configContext = createConfigurationContext(resources.configuration)
        return configContext.resources.apply {
            configuration.fontScale = 1.0f
        }
    }

    @Suppress("deprecation")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SearchBar() {
        var inputText by remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        OutlinedTextField(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth(),
            value = inputText,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            isError = false,
            singleLine = true,
            maxLines = 1,
            label = {
                Text(
                    text = "搜索",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            placeholder = {
                Text(
                    text = "应用名称或包名",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            onValueChange = {
                inputText = it
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    val symbol = inputText
                    inputText = ""
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    addDynamicModeItem(symbol)
                }
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                focusedLabelColor = MaterialTheme.colorScheme.surface,
                unfocusedLabelColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(5.dp)
        )
    }

    @Composable
    private fun DynamicModeListEmptyHint() {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.list_add),
                contentDescription = "CloseIcon",
                modifier = Modifier
                    .height(28.dp)
                    .width(28.dp)
            )
            Text(
                text = "搜索以添加项",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun DynamicModeList() {
        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for ((pkgName, mode) in dynamicModeStateMap.entries) {
                DynamicModeListItem(pkgName, mode)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Composable
    private fun DynamicModeListItem(pkgName: String, mode: String) {
        AnimatedVisibility(visible = (mode != "deleted")) {
            var showModeSwitcher by remember { mutableStateOf(false) }
            var appIcon =
                resources.getDrawable(R.drawable.icon_default, null).toBitmap().asImageBitmap()
            var appLabel = "unknown app"
            try {
                val appInfo =
                    packageManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
                appIcon = packageManager.getApplicationIcon(appInfo).toBitmap().asImageBitmap()
                appLabel = packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Column(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                TextButton(
                    onClick = { showModeSwitcher = !showModeSwitcher },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp)
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = appIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .height(50.dp)
                                .width(50.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                modifier = Modifier
                                    .height(20.dp)
                                    .fillMaxWidth(),
                                text = appLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                modifier = Modifier
                                    .height(20.dp)
                                    .fillMaxWidth(),
                                text = pkgName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier
                                    .height(20.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "动态模式: ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = mode,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = reimuRed,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
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
                                .padding(end = 10.dp)
                                .width(250.dp)
                                .height(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val schedulerModes =
                                listOf("powersave", "balance", "performance", "fast")
                            val modeNameMap =
                                hashMapOf(
                                    "powersave" to "省电",
                                    "balance" to "均衡",
                                    "performance" to "性能",
                                    "fast" to "极速"
                                )
                            schedulerModes.forEach {
                                val buttonColor = when (it) {
                                    mode -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                                val textColor = when (it) {
                                    mode -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                TextButton(
                                    onClick = {
                                        if (it != mode) {
                                            dynamicModeStateMap[pkgName] = it
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
                        TextButton(
                            onClick = {
                                dynamicModeStateMap[pkgName] = "deleted"
                            },
                            modifier = Modifier
                                .width(25.dp)
                                .height(25.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.delete),
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

    private fun addDynamicModeItem(symbol: String) {
        val applications =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA).filterNotNull()
        val newItemList = mutableListOf<String>()
        for (application in applications) {
            val label = packageManager.getApplicationLabel(application).toString()
            val pkgName = application.packageName
            if (label.contains(symbol) || pkgName.contains(symbol)) {
                newItemList.add(pkgName)
            }
            if (newItemList.size >= 5) {
                Toast.makeText(
                    applicationContext,
                    "搜索到的项目过多, 仅保留五项",
                    Toast.LENGTH_LONG
                ).show()
                break
            }
        }
        if (newItemList.size == 0) {
            Toast.makeText(applicationContext, "未能找到搜索项", Toast.LENGTH_LONG).show()
            return
        }
        newItemList.forEach {
            dynamicModeStateMap[it] = "balance"
        }
    }

    private fun loadDynamicModeMap() {
        CoroutineScope(Dispatchers.IO).launch {
            val dynamicModeMap = SchedulerController.getDynamicModeMap()
            for ((pkgName, mode) in dynamicModeMap.entries) {
                dynamicModeStateMap[pkgName] = mode
            }
        }
    }

    private fun saveDynamicModeMap() {
        CoroutineScope(Dispatchers.IO).launch {
            for ((pkgName, mode) in dynamicModeStateMap.entries) {
                if (mode != "deleted") {
                    SchedulerController.setDynamicMode(applicationContext, pkgName, mode)
                } else {
                    SchedulerController.removeDynamicMode(pkgName)
                }
            }
        }
        SchedulerController.sync()
    }
}
