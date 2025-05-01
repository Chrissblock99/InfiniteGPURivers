plugins {
    kotlin("jvm") version "2.1.10"
}

group = "me.chriss99"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.6"
val lwjglNatives = "natives-linux"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("io.github.kotlin-graphics:glm:0.9.9.1-12")
    implementation("io.github.kotlin-graphics:kool:0.9.79")

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "me.chriss99.Main"
            attributes["Implementation-Version"] = version
        }
    }


    test {
        useJUnitPlatform()
    }
}
