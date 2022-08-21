buildscript {
    repositories {
        gradlePluginPortal()
        google()
        maven(url = "https://jitpack.io")
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.42.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
