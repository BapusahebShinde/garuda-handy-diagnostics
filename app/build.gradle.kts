plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.itek.rftaar"
    compileSdk = 35/*
    compileSdk {
        version = release(36)
    }*/

    defaultConfig {
        applicationId = "com.itek.rftaar"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging{
      jniLibs {
        pickFirsts.add("lib/arm64-v8a/libModuleAPIJni.so")
        pickFirsts.add("lib/armeabi-v7a/libModuleAPIJni.so")
        pickFirsts.add("lib/armeabi-v7a/libSerialPort.so")
      }
      resources.excludes.add("resources.arsc")
      resources.excludes.add("AndroidManifest.xml")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar","*.aar"))))

    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    // Hilt (Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")

    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.material:material:1.5.1")
    implementation("io.coil-kt:coil-compose:2.5.0")

    //Dat Store
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    //MQTT
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    //implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")
    //implementation("com.github.hannesa2:paho.mqtt.android:3.3.5")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    //Play store auto-update
    implementation("com.google.android.play:app-update-ktx:2.0.1")

    // Koin Core features
    implementation("io.insert-koin:koin-core:3.5.3")

// Koin for Android
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    //GSON
    implementation("com.google.code.gson:gson:2.8.9")

    // Core Compose libraries
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    // Lifecycle and ViewModel support
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.compose.runtime:runtime-livedata")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Android Core KTX
    implementation("androidx.core:core-ktx:1.12.0")

    // Kotlin Standard Library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    //Splash Screen
    implementation("androidx.core:core-splashscreen:1.1.0-rc01")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt ("com.github.bumptech.glide:compiler:4.16.0")

    implementation("io.coil-kt:coil-svg:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    //ACRA
    //implementation 'ch.acra:acra:4.9.2'
    //implementation 'ch.acra:acra-core:5.13.1'
    // Use the latest version from the ACRA [GitHub repository](https://github.com/ACRA/acra)
    implementation("ch.acra:acra-core:5.11.3") //5.13.1'
    implementation("ch.acra:acra-mail:5.11.3")//5.13.1'

    //OpenCsv
    implementation("com.opencsv:opencsv:5.9")
}