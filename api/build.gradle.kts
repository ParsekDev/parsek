val pf4jVersion: String by project

plugins {
    id("java")
    kotlin("jvm") version "1.9.20"
}

group = "co.statu.parsek.api"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.pf4j:pf4j:${pf4jVersion}")
}
