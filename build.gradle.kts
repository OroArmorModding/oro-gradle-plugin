plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    id("dev.yumi.gradle.licenser") version "4.0.+"
}

group = "com.oroarmor"
version = "1.4.0" + (if (System.getenv("GITHUB_RUN_NUMBER") != null) "" else "-local")

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
    implementation("dev.yumi:yumi-gradle-licenser:4.0.0")
    implementation("org.kohsuke:github-api:1.330")
    implementation("net.dumbcode.gradlehook:GradleHook:2.0.2")

    implementation("io.github.gradle-nexus:publish-plugin:2.0.0")

    implementation("com.modrinth.minotaur:Minotaur:2.10.0")
    implementation("net.darkhax.curseforgegradle:net.darkhax.curseforgegradle.gradle.plugin:1.3.33")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

license {
    rule(project.file("LICENSE"))
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