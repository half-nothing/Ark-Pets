import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
}

buildscript {
    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("org.openjfx:javafx-plugin:0.0.13")
    }
}

val appName: String by project

allprojects {
    val appAuthor: String by project
    val appYearBegin: String by project
    val appVersion: String by project
    val mavenGroup: String by project
    val appCopyright = "Copyright (c) $appYearBegin-${SimpleDateFormat("yyyy").format(Date())} $appAuthor"
    val targetJavaVersion = 17

    apply {
        plugin("java")
        plugin("java-library")
        plugin("org.openjfx.javafxplugin")
        plugin("org.jetbrains.kotlin.jvm")
    }

    version = "$appVersion${getVersionMetadata()}"
    group = mavenGroup

    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        mavenLocal()
        mavenCentral()
    }

    kotlin {
        jvmToolchain(targetJavaVersion)
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    }

    tasks {
        withType<JavaCompile>().configureEach() {
            options.encoding = "UTF-8"
            options.release.set(targetJavaVersion)
        }

        withType<KotlinCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
        }

        jar {
            from("LICENSE")
            manifest {
                attributes(
                    "Build-By" to System.getProperty("user.name"),
                    "Build-TimeStamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()),
                    "Build-Version" to version,
                    "Created-By" to "Gradle ${gradle.gradleVersion}",
                    "Build-Jdk" to "${System.getProperty("java.version")} " +
                            "(${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})",
                    "Build-OS" to "${System.getProperty("os.name")} " +
                            "${System.getProperty("os.arch")} ${System.getProperty("os.version")}",
                    "App-Name" to appName,
                    "App-Version" to appVersion,
                    "App-Author" to appAuthor,
                    "Copyright" to appCopyright
                )
            }
        }
    }
}

fun getVersionMetadata(): String {
    val buildId = System.getenv("GITHUB_RUN_NUMBER")
    val workflow = System.getenv("GITHUB_WORKFLOW")
    val release = System.getenv("RELEASE")

    if (workflow == "Release" || release != null) {
        return ""
    }

    // CI builds only
    if (buildId != null) {
        return "+build.$buildId"
    }

    // No tracking information could be found about the build
    return "+nightly"
}
