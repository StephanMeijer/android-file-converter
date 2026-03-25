import java.net.URI
import java.util.Base64
import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.stephanmeijer.fileconverter"
    compileSdk = 35

    // Compute versionCode from versionName using formula: major*10000 + minor*100 + patch
    val versionNameStr = "1.0.1"
    val versionParts = versionNameStr.split(".").map { it.toInt() }
    val computedVersionCode = versionParts[0] * 10000 + versionParts[1] * 100 + versionParts[2]

    defaultConfig {
        applicationId = "com.stephanmeijer.fileconverter"
        minSdk = 26
        targetSdk = 35
        versionCode = computedVersionCode
        versionName = versionNameStr

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // ---- Release signing (reads from environment variables for CI/CD) ----
    // Generate upload keystore locally:
    //   keytool -genkeypair -v -storetype PKCS12 -keystore upload.keystore -alias upload -keyalg RSA -keysize 2048 -validity 10000
    // Base64 encode: base64 -w 0 upload.keystore > keystore.b64
    // Add to GitHub Secrets: KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
    signingConfigs {
        create("release") {
            val keystoreBase64 = System.getenv("KEYSTORE_BASE64")
            if (keystoreBase64 != null) {
                val keystoreFile = layout.buildDirectory.get().asFile.resolve("release.keystore")
                keystoreFile.parentFile.mkdirs()
                keystoreFile.writeBytes(Base64.getDecoder().decode(keystoreBase64))
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        release {
            val keystoreBase64 = System.getenv("KEYSTORE_BASE64")
            if (keystoreBase64 != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// ---------------------------------------------------------------------------
// Auto-download pandoc.wasm from GitHub releases if not already present.
// Version is read from app/src/main/assets/PANDOC_VERSION.txt so bumping
// the version file is all that's needed to pull a new binary on next build.
// ---------------------------------------------------------------------------
val pandocVersionFile = file("src/main/assets/PANDOC_VERSION.txt")
val pandocWasmFile    = file("src/main/assets/pandoc.wasm")

tasks.register("downloadPandocWasm") {
    description = "Downloads pandoc.wasm from jgm/pandoc GitHub releases if absent"
    group       = "build setup"

    inputs.file(pandocVersionFile)
    outputs.file(pandocWasmFile)

    doLast {
        if (pandocWasmFile.exists()) {
            println("pandoc.wasm already present — skipping download")
            return@doLast
        }

        val version = pandocVersionFile.readText().trim()
        val url     = "https://github.com/jgm/pandoc/releases/download/$version/pandoc.wasm.zip"
        println("Downloading pandoc.wasm $version …")

        val tmpZip = File(temporaryDir, "pandoc.wasm.zip")
        URI(url).toURL().openStream().use { it.copyTo(tmpZip.outputStream()) }

        ZipFile(tmpZip).use { zip ->
            val entry = zip.getEntry("pandoc.wasm")
                ?: error("pandoc.wasm not found inside $url")
            pandocWasmFile.parentFile.mkdirs()
            zip.getInputStream(entry).use { it.copyTo(pandocWasmFile.outputStream()) }
        }
        tmpZip.delete()

        println("pandoc.wasm ready  (${pandocWasmFile.length() / 1_048_576} MB)")
    }
}

tasks.named("preBuild") {
    dependsOn("downloadPandocWasm")
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.documentfile)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // WebView engine for WASM execution
    implementation(libs.androidx.webkit)

    // Debug
    debugImplementation(libs.compose.ui.tooling)
}
