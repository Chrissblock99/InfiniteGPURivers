plugins {
    id("java")
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

    // https://mvnrepository.com/artifact/org.joml/joml
    implementation("org.joml:joml:1.10.8")

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