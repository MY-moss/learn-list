import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.Sync
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.mymoss.learnlist"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mymoss.learnlist"
        minSdk = 26
        targetSdk = 36
        versionCode = 22
        versionName = "0.3.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        val releaseKeystorePath = providers.environmentVariable("RELEASE_KEYSTORE_PATH").orNull
        val releaseStorePassword = providers.environmentVariable("RELEASE_STORE_PASSWORD").orNull
        val releaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
        val releaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull

        if (releaseKeystorePath != null) {
            signingConfigs {
                create("release") {
                    storeFile = file(releaseKeystorePath)
                    storePassword = requireNotNull(releaseStorePassword) { "RELEASE_STORE_PASSWORD is required" }
                    keyAlias = requireNotNull(releaseKeyAlias) { "RELEASE_KEY_ALIAS is required" }
                    keyPassword = requireNotNull(releaseKeyPassword) { "RELEASE_KEY_PASSWORD is required" }
                }
            }
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (releaseKeystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    buildFeatures {
        buildConfig = true
    }
}

// Keep a short, audible fallback inside every APK. Some phones/emulators have
// no notification sound selected, so relying only on RingtoneManager can make
// an otherwise enabled feedback action completely silent.
val generatedFeedbackResDir = layout.buildDirectory.dir("generated/res/feedback/main")
val generateFeedbackSound = tasks.register("generateFeedbackSound") {
    val output = generatedFeedbackResDir.map { it.file("raw/feedback_complete.wav") }
    outputs.file(output)
    doLast {
        val outputFile = output.get().asFile
        outputFile.parentFile.mkdirs()
        val sampleRate = 44_100
        val sampleCount = (sampleRate * 0.52).toInt()
        val pcm = ByteArray(sampleCount * 2)
        for (index in 0 until sampleCount) {
            val position = index.toDouble() / sampleCount
            val frequency = if (position < 0.55) {
                740.0 + 180.0 * (position / 0.55)
            } else {
                920.0 - 120.0 * ((position - 0.55) / 0.45)
            }
            val envelope = when {
                position < 0.04 -> position / 0.04
                position > 0.78 -> ((1.0 - position) / 0.22).coerceAtLeast(0.0)
                else -> 1.0
            }
            val time = index.toDouble() / sampleRate
            val tone = sin(2.0 * PI * frequency * time)
            val harmonic = sin(2.0 * PI * frequency * 2.0 * time) * 0.22
            val value = ((tone + harmonic) * envelope * 0.28 * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            pcm[index * 2] = (value and 0xff).toByte()
            pcm[index * 2 + 1] = ((value ushr 8) and 0xff).toByte()
        }
        val header = ByteArray(44)
        fun ascii(offset: Int, value: String) {
            value.toByteArray(Charsets.US_ASCII).copyInto(header, offset)
        }
        fun intLe(offset: Int, value: Int) {
            header[offset] = (value and 0xff).toByte()
            header[offset + 1] = ((value ushr 8) and 0xff).toByte()
            header[offset + 2] = ((value ushr 16) and 0xff).toByte()
            header[offset + 3] = ((value ushr 24) and 0xff).toByte()
        }
        fun shortLe(offset: Int, value: Int) {
            header[offset] = (value and 0xff).toByte()
            header[offset + 1] = ((value ushr 8) and 0xff).toByte()
        }
        ascii(0, "RIFF")
        intLe(4, 36 + pcm.size)
        ascii(8, "WAVE")
        ascii(12, "fmt ")
        intLe(16, 16)
        shortLe(20, 1)
        shortLe(22, 1)
        intLe(24, sampleRate)
        intLe(28, sampleRate * 2)
        shortLe(32, 2)
        shortLe(34, 16)
        ascii(36, "data")
        intLe(40, pcm.size)
        outputFile.writeBytes(header + pcm)
    }
}

android.sourceSets.getByName("main").res.srcDir(generatedFeedbackResDir.get().asFile)
tasks.named("preBuild").configure { dependsOn(generateFeedbackSound) }

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Gradle's test worker can fail to resolve direct class directories in non-ASCII
// Windows workspaces. Stage only the test inputs in the OS temp directory.
val builtInKotlinTestClasses = layout.buildDirectory.dir("intermediates/built_in_kotlinc/debugUnitTest/compileDebugUnitTestKotlin/classes")
val javaTestClasses = layout.buildDirectory.dir("intermediates/javac/debugUnitTest/compileDebugUnitTestJavaWithJavac/classes")
val asciiTestClasses = File(System.getProperty("java.io.tmpdir"), "learn-list-unit-test-classes")
val runtimeClassesJar = layout.buildDirectory.dir("intermediates/runtime_app_classes_jar/debug/bundleDebugClassesToRuntimeJar")
val asciiRuntimeClasses = File(System.getProperty("java.io.tmpdir"), "learn-list-runtime-classes")
val stageUnitTestClasses = tasks.register<Sync>("stageUnitTestClasses") {
    dependsOn("compileDebugUnitTestKotlin")
    dependsOn("compileDebugUnitTestJavaWithJavac")
    from(builtInKotlinTestClasses)
    from(javaTestClasses)
    into(asciiTestClasses)
}
val stageUnitTestRuntimeClasses = tasks.register<Sync>("stageUnitTestRuntimeClasses") {
    dependsOn("bundleDebugClassesToRuntimeJar")
    from(runtimeClassesJar)
    into(asciiRuntimeClasses)
}
tasks.configureEach {
    if (name != "testDebugUnitTest") return@configureEach
    dependsOn(stageUnitTestClasses)
    dependsOn(stageUnitTestRuntimeClasses)
    doFirst {
        val test = this as org.gradle.api.tasks.testing.Test
        val originalTestDirs = test.testClassesDirs.files
        val originalRuntimeJars = test.classpath.files.filter { it.absolutePath.contains("runtime_app_classes_jar") }
        test.testClassesDirs = files(asciiTestClasses)
        test.classpath = files(test.classpath.filterNot { it in originalTestDirs || it in originalRuntimeJars }, asciiTestClasses, File(asciiRuntimeClasses, "classes.jar"))
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
    implementation("androidx.navigation:navigation-compose:2.9.8")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    add("ksp", "androidx.room:room-compiler:2.8.4")
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}



