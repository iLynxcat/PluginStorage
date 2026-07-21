plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

val gitVersionProvider = providers.exec {
    commandLine("git", "describe", "--tags", "--always", "--dirty=-dev")
}.standardOutput.asText.map { it.trim() }.orElse("unknown")

group = "me.ilynxcat"
version = gitVersionProvider.get()

dependencies {
    compileOnly(libs.paper.api)
    implementation(libs.sqlitejdbc)
    implementation(libs.hikaricp)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

tasks.shadowJar {
    relocate("com.zaxxer.hikari", "me.ilynxcat.pluginstorage.com.zaxxer.hikari")
    relocate("org.sqlite", "me.ilynxcat.pluginstorage.org.sqlite")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = libs.versions.java.get().toInt()
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}
