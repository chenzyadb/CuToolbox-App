package xyz.chenzyadb.cu_toolbox.utils

import android.annotation.SuppressLint
import android.content.Context
import com.topjohnwu.superuser.Shell
import java.io.File
import java.util.zip.ZipInputStream

@Suppress("Unused")
fun CopyAssetsFile(context: Context, assetsPath: String, targetPath: String) {
    try {
        val file = File(targetPath)
        if (file.exists()) {
            if (file.isDirectory) {
                return
            }
            file.delete()
        }
        context.assets.open(assetsPath).use { inputStream ->
            file.outputStream().use {
                inputStream.copyTo(it)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Suppress("Unused")
fun ReadAssetsFile(context: Context, assetsPath: String): String {
    var text = ""
    try {
        context.assets.open(assetsPath).use {
            val bufferedReader = it.bufferedReader(Charsets.UTF_8)
            text = bufferedReader.readText()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return text
}

@Suppress("Unused")
fun WriteFile(filePath: String, text: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(text, Charsets.UTF_8)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Suppress("Unused")
fun ReadFile(filePath: String): String {
    try {
        val file = File(filePath)
        if (file.exists()) {
            return file.readText(Charsets.UTF_8)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

@Suppress("Unused")
fun DeleteFile(filePath: String) {
    try {
        val file = File(filePath)
        if (file.exists() && file.isFile) {
            file.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Suppress("Unused")
fun CreateDir(dirPath: String) {
    try {
        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdir()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Suppress("Unused")
fun DeleteDir(dirPath: String) {
    try {
        val dir = File(dirPath)
        if (dir.exists() && dir.isDirectory) {
            dir.deleteRecursively()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Suppress("Unused")
fun IsPathExists(path: String): Boolean {
    return File(path).exists()
}

@Suppress("Unused")
@SuppressLint("PrivateApi")
fun GetProperty(key: String): String {
    var value = ""
    try {
        val property = Class
            .forName("android.os.SystemProperties")
            .getMethod("get", String::class.java, String::class.java)
            .invoke(null, key, value)
        value = (property as String)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return value
}

@Suppress("Unused")
fun ReadFileViaSu(filePath: String): String {
    val output = mutableListOf<String>()
    Shell.cmd("cat \"${filePath}\"").to(output).exec()
    var content = ""
    output.forEach {
        content += it
    }
    return content
}

@Suppress("Unused")
fun UnzipFile(inputPath: String, outputPath: String) {
    try {
        val inputFile = File(inputPath)
        if (!inputFile.exists()) {
            return
        }

        inputFile.inputStream().use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var zipEntry = zipInputStream.nextEntry
                while (zipEntry != null) {
                    if (!zipEntry.isDirectory) {
                        val outputFile = File(outputPath + '/' + zipEntry.name)
                        outputFile.parentFile?.mkdirs()
                        outputFile.outputStream().use { outputStream ->
                            zipInputStream.copyTo(outputStream)
                        }
                    }
                    zipInputStream.closeEntry()
                    zipEntry = zipInputStream.nextEntry
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}