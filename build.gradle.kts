import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.*

val targetJavaVersion: Int = 17

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

allprojects {
    apply {
        plugin("eclipse")
        plugin("java")
        plugin("java-library")
        plugin("org.openjfx.javafxplugin")
        plugin("org.jetbrains.kotlin.jvm")
    }

    version = "3.5.0"
    ext {
        // App Metadata
        set("appName", "ArkPets")
        set("appAuthor", "Harry Huang")
        set("appYearBegin", "2022")
        set("appYearCurrent", SimpleDateFormat("yyyy").format(Date()))
        set("appCopyright", "Copyright (c) ${get("appYearBegin")}-${get("appYearCurrent")} ${get("appAuthor")}")
        // Prefabs
        set("gdxVersion", "1.11.0")
        set("jnaVersion", "5.12.1")
        set("javaFXVersion", "17.0.8")
    }

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
    }
}

val gdxVersion = "1.11.0"
val jnaVersion = "5.12.1"
val javaFXVersion = "17.0.8"

project(":core") {
    dependencies {
        // Spine Runtime
        api("com.esotericsoftware.spine:spine-libgdx:3.8.99.1")
        // libGDX
        api("com.badlogicgames.gdx:gdx:$gdxVersion")
        api("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
        // JNA
        api("net.java.dev.jna:jna:$jnaVersion")
        api("net.java.dev.jna:jna-platform:$jnaVersion")
        // JavaFX
        api("org.openjfx:javafx-base:$javaFXVersion:win")
        api("org.openjfx:javafx-controls:$javaFXVersion:win")
        api("org.openjfx:javafx-graphics:$javaFXVersion:win")
        api("org.openjfx:javafx-fxml:$javaFXVersion:win")
        // JFoenix
        api("com.jfoenix:jfoenix:9.0.1")
        // FastJson
        api("com.alibaba:fastjson:2.0.39")
        // Log4j
        api("apache-log4j:log4j:1.2.15")
    }
}

project(":desktop") {
    dependencies {
        implementation(project(":core"))
        // libGDX Desktop
        api("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
        api("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    }
}
