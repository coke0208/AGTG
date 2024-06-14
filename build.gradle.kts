buildscript {
    dependencies {
        classpath ("com.android.tools.build:gradle:4.1.0")
        classpath ("com.google.gms:google-services:4.3.3")
        classpath(libs.google.services)
    }

    repositories {
        google()
        mavenCentral()
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}