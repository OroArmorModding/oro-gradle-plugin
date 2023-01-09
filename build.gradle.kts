plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    id("org.cadixdev.licenser") version "0.6.1"
}

group = "com.oroarmor"
version = "1.1.0"

repositories {
    mavenCentral()

    maven {
        name = "Gradle Plugin Portal"
        url = uri("https://plugins.gradle.org/m2")
    }
    maven {
        name = "Quilt Maven"
        url = uri("https://maven.quiltmc.org/repository/release")
    }
}

gradlePlugin {
    plugins {
        create("minecraft") {
            id = "com.oroarmor.minecraft-gradle-plugin"
            implementationClass = "com.oroarmor.orogradleplugin.minecraft.MinecraftPlugin"
        }
        create("general") {
            id = "com.oroarmor.general-gradle-plugin"
            implementationClass = "com.oroarmor.orogradleplugin.GenericPlugin"
        }
        create("maven-central") {
            id = "com.oroarmor.maven-central-publish-plugin"
            implementationClass = "com.oroarmor.orogradleplugin.maven.MavenCentralPlugin"
        }
    }
}

dependencies {
    implementation("org.quiltmc:quilt-gradle-licenser:1.1.2" )
    implementation("org.kohsuke:github-api:1.301")
    implementation("net.dumbcode.gradlehook:GradleHook:1.3.1")

    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")

    implementation("com.modrinth.minotaur:Minotaur:2.6.0")
    implementation("gradle.plugin.com.matthewprenger:CurseGradle:1.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

license {
    setHeader(project.file("LICENSE"))
}

publishing {
    repositories {
        if (System.getenv("MAVEN_URL") != null) {
            maven {
                name = "OroArmor"
                url = uri(System.getenv("MAVEN_URL"))
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }
    }
}