plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

val sdkVersion = project.findProperty("wepinAndroidSdkVersion") ?: "LOCAL-SNAPSHOT"
rootProject.extra["wepinAndroidSdkVersion"] = sdkVersion

android {
    namespace = "com.wepin.android.widgetlib"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        buildConfigField(
            "String",
            "LIBRARY_VERSION",
            "\"${rootProject.extra["wepinAndroidSdkVersion"]}\""
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true // buildConfig 기능을 활성화합니다.
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
    libraryVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = when (name) {
                "release" -> "wepin-widget-v${project.extra["wepinAndroidSdkVersion"]}.aar"
                "debug" -> "debug-wepin-widget-v${project.extra["wepinAndroidSdkVersion"]}.aar"
                else -> throw IllegalArgumentException("Unsupported build variant: $name")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

//    implementation(project(":libs:common:commonLib"))
//    api(project(":libs:common:commonLib"))
//    implementation(project(":libs:modal:modalLib"))
//    implementation(project(":libs:network:networkLib"))
//    implementation(project(":libs:storage:storageLib"))
//    implementation(project(":libs:session:sessionLib"))
//    implementation(project(":libs:login:loginLib"))

    api("io.wepin:wepin-android-sdk-common-v1:${sdkVersion}")
    implementation("io.wepin:wepin-android-sdk-network-v1:${sdkVersion}")
    implementation("io.wepin:wepin-android-sdk-modal-v1:${sdkVersion}")
    implementation("io.wepin:wepin-android-sdk-storage-v1:${sdkVersion}")
    implementation("io.wepin:wepin-android-sdk-session-v1:${sdkVersion}")
    implementation("com.github.WepinWallet:wepin-android-sdk-login-v1:v1.1.0-test2")

//    implementation("com.github.WepinWallet:wepin-android-sdk-login-v1:v1.0.0")

//    // Jackson Databind for JSON serialization/deserialization
//    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
//
//    // Jackson Annotations
//    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
//
//    // Jackson Kotlin module
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.WepinWallet"
                artifactId = "wepin-android-sdk-widget-v1"
            }
        }
    }
}