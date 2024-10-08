plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/") // Paper
}

dependencies {
    compileOnly(project(":core"))
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    pluginRemapper("net.fabricmc:tiny-remapper:0.10.3:fat")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}