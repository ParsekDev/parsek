
import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

val vertxVersion: String by project
val gsonVersion: String by project
val springContextVersion: String by project
val handlebarsVersion: String by project
val log4jVersion = "2.24.2"
val appMainClass = "co.statu.parsek.Main"
val pf4jVersion: String by project
val pluginsDir: File? by rootProject.extra

val defaultVersion = "0.0.0-local-build"

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("kapt") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jreleaser") version "1.14.0"
    `maven-publish`
    signing
    application
}

group = "dev.parsek"
version = project.findProperty("version") ?: defaultVersion

val buildType = project.findProperty("buildType") as String? ?: "alpha"
val timeStamp: String by project
// Custom directory for distribution JARs (separate from build/libs which is used for publishing)
val distDir by extra { file("${rootProject.layout.buildDirectory.get()}/dist") }
val defaultJarEnabled: String? by project

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/iovertx-3720/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
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
    implementation(group = "commons-codec", name = "commons-codec", version = "1.17.1")

    // https://mvnrepository.com/artifact/org.springframework/spring-context
    implementation("org.springframework:spring-context:$springContextVersion")

    implementation("org.pf4j:pf4j:${pf4jVersion}")

    implementation("org.apache.commons:commons-lang3:3.17.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-text
    implementation("org.apache.commons:commons-text:1.12.0")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:$gsonVersion")

    // https://mvnrepository.com/artifact/org.ow2.asm/asm
    implementation("org.ow2.asm:asm:9.7.1")

    // https://mvnrepository.com/artifact/com.github.jknack/handlebars
    implementation("com.github.jknack:handlebars:$handlebarsVersion")

    // https://mvnrepository.com/artifact/commons-validator/commons-validator
    implementation("commons-validator:commons-validator:1.9.0")

    // https://mvnrepository.com/artifact/com.jcabi/jcabi-manifests
    implementation("com.jcabi:jcabi-manifests:2.1.0")
}

tasks {
    register("copyJar") {
        dependsOn(shadowJar)

        doLast {
            // Create distribution directory
            distDir.mkdirs()
            
            // Copy only shadow JAR to distribution directory
            copy {
                from(shadowJar.get().archiveFile.get().asFile.absolutePath)
                into(distDir)
            }
            
            println("Distribution JAR created at: ${distDir}/${shadowJar.get().archiveFileName.get()}")
        }
    }

    build {
        dependsOn("copyJar")
        // Ensure standard jar is built for Maven publishing (stays in build/libs)
        dependsOn(jar)
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

        if (version != "unspecified") {
            archiveFileName.set("${rootProject.name}-v${version}.jar")
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
    environment("ParsekVersion", if (version == "unspecified") defaultVersion else version)
    environment("ParsekBuildType", buildType)
    pluginsDir?.let { systemProperty("pf4j.pluginsDir", it.absolutePath) }
}

application {
    mainClass.set(appMainClass)
}

tasks.named<Jar>("jar") {
    // Keep jar enabled for Maven publishing
    enabled = true
    // Add custom naming: v before version and -api before .jar
    if (version != "unspecified") {
        archiveFileName.set("${rootProject.name}-v${version}-api.jar")
    } else {
        archiveFileName.set("${rootProject.name}-api.jar")
    }
}

java {
    // Use Java 21 for compilation
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    // Automatically create sources and javadoc JARs
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(21) // Ensure Kotlin uses the Java 21 toolchain
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.parsek"
            artifactId = "core"
            version = project.version.toString()
            
            // Use the standard jar task output
            artifact(tasks.named("jar"))
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
            
            pom {
                name.set("Parsek")
                description.set("Open-source modular backend in Kotlin")
                url.set("https://github.com/ParsekDev/parsek")
                inceptionYear.set("2025")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("Statu")
                        name.set("Statu")
                        email.set("info@statu.co")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/ParsekDev/parsek.git")
                    developerConnection.set("scm:git:ssh://github.com/ParsekDev/parsek.git")
                    url.set("https://github.com/ParsekDev/parsek")
                }
            }
        }
    }
    
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

// Signing configuration
signing {
    val signingKey = System.getenv("GPG_PRIVATE_KEY")
    val signingPassword = System.getenv("GPG_PASSPHRASE")
    
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}

// JReleaser configuration
jreleaser {
    project {
        description.set("Open-source modular backend in Kotlin")
        authors.add("Statu")
        license.set("MIT")
        links {
            homepage.set("https://github.com/ParsekDev/parsek")
        }
        inceptionYear.set("2025")
    }
    
    // Configure GitHub release provider (required by JReleaser even for deploy-only)
    release {
        github {
            overwrite.set(false)
            skipTag.set(true)
            skipRelease.set(true)
            changelog {
                enabled.set(false)
            }
        }
    }
    
    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
    }
    
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}