plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.10"
}

android {
    namespace = "com.balex.logged_user"
    compileSdk = 35

    defaultConfig {
        minSdk = 30
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    ksp(libs.dagger.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material)
    implementation(libs.icons)

    implementation(libs.decompose.core)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.coroutines)

    runtimeOnly(libs.billing)

    implementation(libs.kotlinx.serialization.core)

    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.graphics)

    implementation(libs.androidx.ui.geometry)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.ui.unit)

    api(project(":common"))
    api(libs.dagger.core)
    api(libs.androidx.foundation.layout)
    api(libs.androidx.runtime)
    api(libs.back.handler)
    api(libs.instance.keeper)
    api(libs.lifecycle)
    api(libs.state.keeper)
    api(libs.javax.inject)
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.monitor)
    androidTestImplementation(libs.junit)


}