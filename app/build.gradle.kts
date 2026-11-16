plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.campushub"
    compileSdk {
        version = release(37)
    }

    defaultConfig {        applicationId = "com.example.campushub"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiHost = providers.gradleProperty("campusHubApiHost")
            .orElse("10.79.76.92")
            .get()
        buildConfigField("String", "API_HOST", "\"$apiHost\"")

    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
