import org.gradle.internal.impldep.bsh.commands.dir

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.safe.args)
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.chaquo.python")
}

android {
    namespace = "com.findmyphone.clapping.clapfinder.soundalert"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.findmyphone.clapping.clapfinder.soundalert"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "Find_My_phone-v$versionCode($versionName)")
        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

//    repositories {
//        flatDir {
//            dirs("libs")
//        }
//    }
}

chaquopy {
    defaultConfig {
        version = "3.8"
        pip {
//            install("numpy")
//            install("python_speech_features")
//            install("scipy")
//            install("fastdtw")
        }
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.junit.junit)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.multidex)
    implementation(libs.hilt.android)
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)
    implementation(libs.glide)
    implementation(libs.dexter)
    implementation(libs.gson)
    implementation(libs.eventbus)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.hilt.compiler)
    implementation(libs.lottie)
    implementation(libs.androidx.datastore.preferences)
    kapt(libs.androidx.room.compiler.v250)

    implementation(files("libs/TarsosDSPKit-release.aar"))

//
//    implementation("be.tarsos.dsp:core:2.5")
    // implementation("be.tarsos.dsp:jvm:2.5")
    //  implementation(files("libs/musicg-1.4.2.0.jar"))
}