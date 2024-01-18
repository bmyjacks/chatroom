plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}

group = "io.bmyjacks.app.chatroom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.googlecode.lanterna:lanterna:3.1.1")
    implementation("info.picocli:picocli:4.7.5")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Javadoc> {
    setDestinationDir(file("doc"))
}

tasks.getByName<JavaCompile>("compileJava") {
    options.compilerArgs.plusAssign(listOf("-Aproject=${project.group}/${project.name}"))
}

tasks.getByName<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "io.bmyjacks.app.chatroom.Main"
    }
}

tasks.getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    manifest {
        attributes["Main-Class"] = "io.bmyjacks.app.chatroom.Main"
    }
    minimize()
}