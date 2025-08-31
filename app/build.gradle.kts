plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.thellex.payments"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file("/Users/thelle/Desktop/Keystore/Thellex-Consumer-Kotlin-Keystore.jks")
            storePassword = properties["RELEASE_STORE_PASSWORD"]?.toString() ?: error("RELEASE_STORE_PASSWORD not set in gradle.properties")
            keyAlias = properties["RELEASE_KEY_ALIAS"]?.toString() ?: error("RELEASE_KEY_ALIAS not set in gradle.properties")
            keyPassword = properties["RELEASE_KEY_PASSWORD"]?.toString() ?: error("RELEASE_KEY_PASSWORD not set in gradle.properties")
        }
    }

    defaultConfig {
        applicationId = "com.thellex.payments"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.1" // ✅ Match your Kotlin version
//    }

    buildTypes {
        release {
            // isMinifyEnabled = true
            // isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx.v261)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v261)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.ui.text.android)

    // QR / Barcode
    implementation(libs.journeyapps.zxing.android.embedded)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    // OTP & Image Picker
    implementation(libs.otp.view)
    implementation(libs.imagepicker)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.crashlytics.ndk)

    // Glide
    implementation(libs.glide)
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // DateTime Picker
    implementation(libs.materialdatetimepicker)

    // Splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // -------------------- Jetpack Compose --------------------
    val composeBom = platform("androidx.compose:compose-bom:2024.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI, Material3 & Activity integration
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // -------------------- Testing --------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
