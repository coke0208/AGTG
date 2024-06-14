plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.test"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.test"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    viewBinding{
        enable = true
    }
    dependencies {
        implementation("androidx.viewpager2:viewpager2:1.0.0")

        implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
        implementation("com.google.android.gms:play-services-auth:21.1.0")
        implementation("com.google.firebase:firebase-bom:32.8.1")
        implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
        implementation("androidx.multidex:multidex:2.0.1")
        implementation("com.google.firebase:firebase-analytics")
        implementation("com.google.gms:google-services:4.3.10")
        implementation ("com.google.zxing:core:3.4.1")
        implementation("com.journeyapps:zxing-android-embedded:4.2.0")

        implementation ("androidx.room:room-runtime:2.6.1")
        annotationProcessor ("androidx.room:room-compiler:2.6.1")
        implementation ("androidx.recyclerview:recyclerview:1.1.0")
        implementation("com.google.firebase:firebase-firestore")
        implementation ("com.google.firebase:firebase-database-ktx:20.0.4")
        implementation ("androidx.work:work-runtime-ktx:2.8.1")
            //ss
        implementation("com.github.bumptech.glide:glide:4.16.0")
        implementation ("com.google.zxing:core:3.4.1")
        implementation ("com.google.code.gson:gson:2.8.6")
        implementation ("com.squareup.picasso:picasso:2.8")
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
//푸시알림
buildscript {
    dependencies {
        classpath ("com.google.gms:google-services:4.3.15") // Add this line
    }
}