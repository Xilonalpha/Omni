import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.chemscanner.omniscient.marrow"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // --- MAPARE CORECTĂ A CHEILOR DIN local.properties ---
        val geminiKey1 = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        val geminiKey2 = localProperties.getProperty("GEMINI_API_KEY_SECONDARY") ?: ""
        val geminiKey3 = localProperties.getProperty("GEMINI_API_KEY_TERTIARY") ?: ""

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey1\"")
        buildConfigField("String", "GEMINI_API_KEY_2", "\"$geminiKey2\"")
        buildConfigField("String", "GEMINI_API_KEY_3", "\"$geminiKey3\"")

        buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties.getProperty("OPENAI_API_KEY") ?: ""}\"")
        buildConfigField("String", "NVIDIA_API_KEY", "\"${localProperties.getProperty("NVIDIA_API_KEY") ?: ""}\"")
        buildConfigField("String", "GROK_API_KEY", "\"${localProperties.getProperty("GROK_API_KEY") ?: ""}\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"${localProperties.getProperty("DEEPSEEK_API_KEY") ?: ""}\"")
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"${localProperties.getProperty("ANTHROPIC_API_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        buildConfig = true
        resValues = true
    }
}

dependencies {
    // BIOMETRIC
    api("androidx.biometric:biometric:1.1.0")

    // ROOM
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // RETROFIT
    val retrofit_version = "2.11.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")

    // AR CORE
    implementation("com.google.ar:core:1.47.0")

    // FIREBASE & VERTEX AI (BYPASS ENGINE)
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-vertexai")

    // Google AI Client for Gemini
    api("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Ktor Client
    val ktor_version = "2.3.12"
    api("io.ktor:ktor-client-core:$ktor_version")
    api("io.ktor:ktor-client-okhttp:$ktor_version")
    api("io.ktor:ktor-client-content-negotiation:$ktor_version")
    api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    api("io.ktor:ktor-client-logging:$ktor_version")

    // ML KIT
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("com.google.mlkit:image-labeling:17.0.9")
    implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")

    // LOCATION
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // MQTT
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    // CAMERA X
    val camerax_version = "1.4.0"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")

    // TENSORFLOW & ML
    implementation("com.google.mediapipe:tasks-genai:0.10.20")
    implementation("org.tensorflow:tensorflow-lite:2.16.1")

    // CORE
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // HILT
    implementation("com.google.dagger:hilt-android:2.54")
    ksp("com.google.dagger:hilt-compiler:2.54")

    // Compose UI Graphics
    implementation("androidx.compose.ui:ui-graphics:1.7.0")
}

ksp {
    arg("dagger.hilt.internal.useAggregatingRootProcessor", "false")
}
