plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.touchstoneinstitute.learningcompanion"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.touchstoneinstitute.learningcompanion"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Learning backend API
        buildConfigField("String", "API_BASE_URL", "\"https://learn-be.tsin.ca/api/\"")
        // External auth service
        buildConfigField("String", "AUTH_BASE_URL", "\"https://zxkbbj3pcy.us-east-1.awsapprunner.com/api/\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
            buildConfigField("String", "API_BASE_URL", "\"https://learndev-be.tsin.ca/api/\"")
            buildConfigField("String", "AUTH_BASE_URL", "\"https://zxkbbj3pcy.us-east-1.awsapprunner.com/api/\"")
        }
        create("staging") {
            initWith(getByName("debug"))
            isDebuggable = true
            // Points to the ECS Fargate ALB -- update after CDK deploy with actual DNS
            buildConfigField("String", "API_BASE_URL", "\"http://tsin-learning-staging-alb.ca-central-1.elb.amazonaws.com/api/\"")
            buildConfigField("String", "AUTH_BASE_URL", "\"https://zxkbbj3pcy.us-east-1.awsapprunner.com/api/\"")
            matchingFallbacks += listOf("debug")
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Local storage
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)

    // Background work
    implementation(libs.work.runtime.ktx)

    // Coroutines
    implementation(libs.coroutines.android)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Utilities
    implementation(libs.timber)
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}