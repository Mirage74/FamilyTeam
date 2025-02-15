plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.10"
}

android {
    namespace = "com.balex.common"
    compileSdk = 35

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val key = property("licencekey")?.toString() ?: error(
            "You should add licencekey into gradle.properties"
        )
        buildConfigField("String", "LICENCE_KEY", "\"$key\"")
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {

    ksp(libs.dagger.compiler)
    ksp(libs.room.compiler)

    api(libs.dagger.core)
    api(libs.javax.inject)
    api(libs.androidx.runtime)
    api(libs.androidx.room.runtime)
    api(libs.kotlinx.coroutines.core)
    api(libs.billing)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.runtime.saveable)
    implementation(libs.androidx.annotation)

    implementation(libs.androidx.security.crypto)

    implementation(libs.kotlinx.serialization.core)

    implementation(libs.decompose.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.logging)
    implementation(libs.lifecycle)

    implementation(libs.billing.ktx)

    implementation(libs.room.core)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.sqlite)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.crashlytics)

    implementation(libs.gson)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.ui.unit)

    implementation(libs.play.services.ads)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.monitor)
    androidTestImplementation(libs.junit)

}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
