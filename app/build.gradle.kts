plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val releaseKeystorePath: String? = System.getenv("KITSUNE_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }

android {
    namespace = "com.kitsune.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kitsune.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "1.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64"))
        }

        manifestPlaceholders["appName"] = "Kitsune"
    }

    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = System.getenv("KITSUNE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KITSUNE_KEY_ALIAS")
                keyPassword = System.getenv("KITSUNE_KEY_PASSWORD")
            }
        }
    }

    flavorDimensions += "distribution"

    productFlavors {
        create("github") {
            dimension = "distribution"
            isDefault = true
            buildConfigField("boolean", "UPDATER_ENABLED", "true")
        }
        create("fdroid") {
            dimension = "distribution"
            buildConfigField("boolean", "UPDATER_ENABLED", "false")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (releaseKeystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            manifestPlaceholders["appName"] = "Kitsune"
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-nightly"
            manifestPlaceholders["appName"] = "Kitsune Nightly"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        localeFilters += listOf("en", "pt-rBR")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
}

val abiVersionCodes = mapOf(
    "armeabi-v7a" to 1,
    "arm64-v8a" to 2,
    "x86" to 3,
    "x86_64" to 4
)

android.applicationVariants.all {
    val variant = this
    val baseCode = variant.versionCode
    val versionName = variant.versionName ?: "1.0.0"
    val buildType = variant.buildType.name
    val distributionSuffix = if (variant.flavorName == "fdroid") "-fdroid" else ""

    outputs.all {
        val output = this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl
        val abiName = output?.getFilter(com.android.build.OutputFile.ABI)

        if (abiName != null) {
            val abiPriority = abiVersionCodes[abiName] ?: 0
            output?.versionCodeOverride = baseCode * 10 + abiPriority
        } else {
            output?.versionCodeOverride = baseCode * 10 + 0
        }

        val architecture = abiName ?: "universal"
        output?.outputFileName = "Kitsune-v${versionName}${distributionSuffix}-${architecture}-${buildType}.apk"
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_reports")
    metricsDestination = layout.buildDirectory.dir("compose_metrics")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.youtubedl.library)
    implementation(libs.youtubedl.ffmpeg)

    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    implementation(libs.lottie.compose)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
