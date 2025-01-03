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
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation (libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retorfit.gsonConverter)


    implementation(libs.decompose.core)
    implementation(libs.decompose.jetpack)

    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.coroutines)
    implementation(libs.mvikotlin.logging)

    implementation(libs.dagger.core)
    implementation(libs.billing.ktx)
    ksp(libs.dagger.compiler)

    implementation(libs.room.core)
    ksp(libs.room.compiler)



    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
