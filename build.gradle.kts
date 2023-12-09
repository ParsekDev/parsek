val vertxVersion: String by project
val gsonVersion: String by project
val handlebarsVersion: String by project
val log4jVersion = "2.21.1"
val appMainClass = "co.statu.parsek.Main"
val pf4jVersion: String by project
val pluginsDir: File by rootProject.extra

plugins {
    java
    kotlin("jvm") version "1.9.20"
    kotlin("kapt") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "co.statu.parsek"
version = "1.0.0"

val buildType = "alpha"
val timeStamp: String by project
val fullVersion = if (project.hasProperty("timeStamp")) "$version-$buildType-$timeStamp" else "$version-$buildType"
val buildDir by extra { file("${rootProject.layout.buildDirectory.get()}/libs") }
val defaultJarEnabled: String? by project

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/iovertx-3720/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("io.vertx:vertx-unit:$vertxVersion")

    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-web-client:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-config-hocon:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-web-validation:$vertxVersion")
    implementation("io.vertx:vertx-json-schema:$vertxVersion")

    implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = log4jVersion)
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = log4jVersion)
    implementation(group = "org.apache.logging.log4j", name = "log4j-slf4j2-impl", version = log4jVersion)

    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation(group = "commons-codec", name = "commons-codec", version = "1.16.0")

    // https://mvnrepository.com/artifact/org.springframework/spring-context
    implementation("org.springframework:spring-context:5.3.30")

    implementation("org.pf4j:pf4j:${pf4jVersion}")

    implementation("org.apache.commons:commons-lang3:3.13.0")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-text
    implementation("org.apache.commons:commons-text:1.11.0")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:$gsonVersion")

    implementation("org.ow2.asm:asm:9.6")

    // https://mvnrepository.com/artifact/com.github.jknack/handlebars
    implementation("com.github.jknack:handlebars:$handlebarsVersion")
}

tasks {
    register("copyJar") {
        dependsOn(shadowJar)

        doLast {
            if (shadowJar.get().archiveFile.get().asFile.parentFile.absolutePath != buildDir.absolutePath) {
                copy {
                    from(shadowJar.get().archiveFile.get().asFile.absolutePath)
                    into(buildDir)
                }
            }
        }
    }

    build {
        dependsOn("copyJar")
    }

    register("buildDev") {
        dependsOn("build")
    }

    shadowJar {
        manifest {
            val attrMap = mutableMapOf<String, String>()

            if (project.gradle.startParameter.taskNames.contains("buildDev"))
                attrMap["MODE"] = "DEVELOPMENT"

            attrMap["VERSION"] = fullVersion
            attrMap["BUILD_TYPE"] = buildType

            attributes(attrMap)
        }

        if (project.hasProperty("timeStamp")) {
            archiveFileName.set("${rootProject.name}-${timeStamp}.jar")
        } else {
            archiveFileName.set("${rootProject.name}.jar")
        }
    }
}

tasks.named<JavaExec>("run") {
    environment("EnvironmentType", "DEVELOPMENT")
    environment("ParsekVersion", fullVersion)
    environment("ParsekBuildType", buildType)
    systemProperty("pf4j.pluginsDir", pluginsDir.absolutePath)
}

application {
    mainClass.set(appMainClass)
}

tasks.named("jar").configure {
    enabled = defaultJarEnabled.toBoolean()
}