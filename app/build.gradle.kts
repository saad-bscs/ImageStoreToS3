plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.imagestoretos3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.imagestoretos3"
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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // AWS SDK for Java
    implementation("com.amazonaws:aws-android-sdk-core:2.22.6")
    implementation("com.amazonaws:aws-android-sdk-s3:2.22.6")

    //Volley library for network
    implementation("com.android.volley:volley:1.2.1")

    //Easy Permissions wrapper
    implementation("pub.devrel:easypermissions:3.0.0")

    //Image Library
    implementation("com.github.bumptech.glide:glide:4.13.0")
}