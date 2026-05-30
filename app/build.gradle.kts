plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

// noinspection GradleDependency
android {
    compileSdk = 34

    // noinspection OldTargetApi
    defaultConfig {
        applicationId = "xyz.chenzyadb.cu_toolbox"
        minSdk = 28
        targetSdk = 34
        versionCode = 8031301
        versionName = "8.3.13_release"

        ndk {
            abiFilters.clear()
            abiFilters.add("arm64-v8a")
        }

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes.add("META-INF/**")
            excludes.add("okhttp3/**")
            excludes.add("schema/**")
            excludes.add("assets/dexopt/**")
            excludes.add("DebugProbesKt.bin")
            excludes.add("kotlin-tooling-metadata.json")
            excludes.add("**/*.kotlin_builtins")
            excludes.add("**/*.kotlin_module")
            excludes.add("**/*.properties")
            excludes.add("**/*.txt")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    ndkVersion = "29.0.14206865"
    namespace = "xyz.chenzyadb.cu_toolbox"
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(libs.libsu.core)
    implementation(libs.compose.markdown)
    implementation(libs.fastjson2)
    implementation(libs.coil)
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material)
    implementation(libs.ui.tooling.preview)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.material)
}
