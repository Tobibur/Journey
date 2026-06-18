import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
    jacoco
}

android {
    namespace = "com.tobibur.journey"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tobibur.journey"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Export Room schema JSON (app/schemas/<version>.json) so future migrations
    // can be written and tested against a known baseline. Commit this folder.
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    // Release signing is configured via keystore.properties (gitignored, kept out
    // of the repo). When the file is absent — e.g. on CI or a fresh clone — the
    // signing config is simply not created, so debug builds keep working.
    val keystorePropsFile = rootProject.file("keystore.properties")
    signingConfigs {
        if (keystorePropsFile.exists()) {
            val keystoreProps = Properties().apply {
                keystorePropsFile.inputStream().use { load(it) }
            }
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
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

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

}

// Tell Room (via KSP) where to write exported schema JSON files.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// JaCoCo code-coverage report for unit tests.
// Run with: ./gradlew jacocoTestReport
// HTML report: app/build/reports/jacoco/jacocoTestReport/html/index.html
jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Generates JaCoCo coverage report for the debug unit tests."

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }

    // Exclude generated code and UI/framework classes that aren't unit-testable.
    val excludes = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*",
        // Hilt / generated
        "**/*_Factory*.*", "**/*_HiltModules*.*", "**/*_Impl*.*",
        "**/Hilt_*.*", "**/*_GeneratedInjector*.*", "**/*_MembersInjector*.*",
        "**/di/**", "dagger/**", "hilt_aggregated_deps/**",
        // Pure-UI Compose code (not unit tested)
        "**/ui/theme/**", "**/presentation/components/**",
        "**/presentation/navigation/**", "**/*Screen*.*",
        "**/ComposableSingletons*.*"
    )

    val kotlinClasses = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        setExcludes(excludes)
    }
    classDirectories.setFrom(kotlinClasses)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) { setIncludes(listOf("**/testDebugUnitTest.exec")) }
    )
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.lottie)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}