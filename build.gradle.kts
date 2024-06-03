// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}

buildscript {
    val objectboxVersion by extra("4.0.0")

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("io.objectbox:objectbox-gradle-plugin:$objectboxVersion")
        classpath("commons-httpclient:commons-httpclient:3.1@jar")
    }
}