plugins {
    java
    kotlin("jvm") version "1.3.11"
    idea
    id("jacoco")
}

group = "oooast-tools"
version = "1.0-SNAPSHOT"

idea {
    module {
        sourceDirs = setOf(
            file("$projectDir/src/main/kotlin"),
            file("$projectDir/src/main/java"),
            file("$projectDir/config/.")
        )
        outputDir = file("build/classes/kotlin/main")
        testOutputDir = file("build/classes/kotlin/test")
    }
}

kotlin {

}
sourceSets {
    main {
        java.srcDir(file("$projectDir/config/."))
    }
//    create("config") {
//        java.srcDir(file("$projectDir/config"))
//        compileClasspath += sourceSets.main.get().output
//        runtimeClasspath += sourceSets.main.get().output
//    }
}

//val configImplementation: Configuration by configurations.getting {
////    extendsFrom(configurations.implementation.get())
//}

tasks.test {
    @Suppress("UnstableApiUsage")
    useJUnitPlatform()
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
    kotlinOptions {
        jvmTarget = "1.8"
        noReflect = false
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "net.pototskiy.apps.magemediation.MainKt")
    }
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

repositories {
    jcenter()
}



dependencies {
    //    configImplementation(project(":mage-mediation-api"))
    implementation(project(":mage-mediation-api"))
    implementation(project(":mage-mediation-category"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(group = "com.beust", name = "jcommander", version = "1.71")
    // Database
    implementation(group = "org.jetbrains.exposed", name = "exposed", version = "0.11.2") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.slf4j")
    }
    // Excel
    implementation(group = "org.apache.poi", name = "poi", version = "4.0.1")
    implementation(group = "org.apache.poi", name = "poi-ooxml", version = "4.0.1")
    // CSV
    implementation(group = "org.apache.commons", name = "commons-csv", version = "1.6")
    // MySql
    implementation(group = "mysql", name = "mysql-connector-java", version = "8.0.13")
    // Logger
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.8.0-beta2")
    implementation(group = "org.slf4j", name = "slf4j-log4j12", version = "1.8.0-beta2")
    // Kotlin script
    implementation(kotlin("script-runtime"))
    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("script-util"))
    // Test
    // testCompile(group = "junit", name = "junit", version = "4.12")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.0-rc.1") {
        exclude("org.jetbarins.kotlin")
    }
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.0-rc.1") {
        exclude(group = "org.jetbrains.kotlin")
    }
    testImplementation(group = "org.amshove.kluent", name = "kluent", version = "1.45")
}

//compileKotlin {
//    kotlinOptions.jvmTarget = "1.8"
//}
//compileTestKotlin {
//    kotlinOptions.jvmTarget = "1.8"
//}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}

sonarqube {
    val coverageFiles = fileTree("$projectDir") {
        include("build/jacoco/*.exec")
    }
    val javaBinaries = listOf(
        "$projectDir/build/classes/kotlin/main",
        "$projectDir/build/classes/java/main"
    )
    val testBinaries = listOf(
        "$projectDir/build/classes/kotlin/test",
        "$projectDir/build/classes/java/test"
    )
    properties {
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.java.source", "1.8")
//        property("sonar.java.binaries", javaBinaries.joinToString(","))
//        property("sonar.java.test.binaries", testBinaries.joinToString(","))
//        property("sonar.jacoco.reportPaths", coverageFiles.joinToString(","))
    }
}
