// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.safe.args) apply false
    alias(libs.plugins.org.jetbrains.kotlin.kapt) apply false
   // id("com.chaquo.python") version "16.1.0" apply false
}