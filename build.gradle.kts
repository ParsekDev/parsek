val vertxVersion: String by project
val gsonVersion: String by project
val springContextVersion: String by project
val handlebarsVersion: String by project
val log4jVersion = "2.21.1"
val appMainClass = "co.statu.parsek.Main"
val pf4jVersion: String by project
val pluginsDir: File? by rootProject.extra

plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("kapt") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
    `maven-publish`
}

group = "co.statu"
version = project.findProperty("version") ?: "0.0.1"

val buildType = project.findProperty("buildType") as String? ?: "alpha"
val timeStamp: String by project
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
    implementation("org.springframework:spring-context:$springContextVersion")

    implementation("org.pf4j:pf4j:${pf4jVersion}")

    implementation("org.apache.commons:commons-lang3:3.13.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-text
    implementation("org.apache.commons:commons-text:1.11.0")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:$gsonVersion")

    // https://mvnrepository.com/artifact/org.ow2.asm/asm
    implementation("org.ow2.asm:asm:9.6")

    // https://mvnrepository.com/artifact/com.github.jknack/handlebars
    implementation("com.github.jknack:handlebars:$handlebarsVersion")

    // https://mvnrepository.com/artifact/commons-validator/commons-validator
    implementation("commons-validator:commons-validator:1.8.0")
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
        dependsOn(distTar, distZip)

        manifest {
            val attrMap = mutableMapOf<String, String>()

            if (project.gradle.startParameter.taskNames.contains("buildDev"))
                attrMap["MODE"] = "DEVELOPMENT"

            attrMap["VERSION"] = version.toString()
            attrMap["BUILD_TYPE"] = buildType

            attributes(attrMap)
        }

        if (project.hasProperty("timeStamp")) {
            archiveFileName.set("${rootProject.name}-${timeStamp}.jar")
        } else {
            archiveFileName.set("${rootProject.name}.jar")
        }

        if (project.gradle.startParameter.taskNames.contains("publish")) {
            archiveFileName.set(archiveFileName.get().lowercase())
        }
    }
}

tasks.named<JavaExec>("run") {
    environment("EnvironmentType", "DEVELOPMENT")
    environment("ParsekVersion", version)
    environment("ParsekBuildType", buildType)
    pluginsDir?.let { systemProperty("pf4j.pluginsDir", it.absolutePath) }
}

application {
    mainClass.set(appMainClass)
}

tasks.named("jar").configure {
    enabled = defaultJarEnabled.toBoolean()
}

publishing {
    repositories {
        maven {
            name = "Parsek"
            url = uri("https://maven.pkg.github.com/StatuParsek/Parsek")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME_GITHUB")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN_GITHUB")
            }
        }
    }

    publications {
        create<MavenPublication>("shadow") {
            project.extensions.configure<com.github.jengelman.gradle.plugins.shadow.ShadowExtension> {
                artifactId = "core"
                component(this@create)
            }
        }
    }
}
