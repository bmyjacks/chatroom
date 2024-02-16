plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
    id("jacoco")
}

group = "io.bmyjacks.app.chatroom"
version = "1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.googlecode.lanterna:lanterna:3.1.1")
    implementation("info.picocli:picocli:4.7.5")
    implementation("com.google.guava:guava:33.0.0-jre")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Test> {
    finalizedBy("jacocoTestReport")
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

tasks.getByName<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("reports/jacoco.xml"))
    }
}