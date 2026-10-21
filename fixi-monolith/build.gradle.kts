plugins {
    java
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native")
}

group = "com.hotelpms"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

springBoot {
    mainClass.set("com.hotelpms.monolith.FixiMonolithApplication")
}

graalvmNative {
    binaries {
        named("main") {
            if (providers.gradleProperty("nativeQuickBuild").orNull == "true") {
                buildArgs.add("-Ob")
            } else {
                buildArgs.add("-O2")
            }
            buildArgs.add("-J-Xmx16g")
            buildArgs.add("--parallelism=${System.getenv("NATIVE_IMAGE_PARALLELISM") ?: "2"}")
            buildArgs.add("-H:DeadlockWatchdogInterval=60")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Reuse the existing bounded-context implementations; gateway and Config Server are
    // intentionally absent from this runtime target.
    implementation(project(":auth-service"))
    implementation(project(":frontdesk-service"))
    implementation(project(":guest-service"))
    implementation(project(":billing-service"))
    implementation(project(":fb-service"))
    implementation(project(":notification-service"))
    implementation(project(":pdf-template-engine"))
    implementation(project(":internal-auth-lib"))
    implementation(project(":common-web-lib"))

    // The monolith consumes the shared libraries directly, so it must import
    // the same Spring Cloud BOM that provides the managed Feign version those
    // libraries use. The microservices get this transitively from their own
    // Spring Cloud starters; the standalone monolith does not.
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2025.0.0"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    // The monolith serves the service-grouped OpenAPI contract formerly aggregated by Gateway.
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val migrationModules = listOf(
    "auth-service",
    "frontdesk-service",
    "guest-service",
    "billing-service",
    "fb-service",
    "notification-service",
)
val generatedMigrations = layout.buildDirectory.dir("generated/resources/monolith-migrations")

val collectModuleMigrations by tasks.registering(Copy::class) {
    migrationModules.forEach { module ->
        from(rootProject.file("$module/src/main/resources/db/migration")) {
            into("db/monolith-migrations/$module")
        }
    }
    into(generatedMigrations)
    doLast {
        val migrationIndex = generatedMigrations.get().file("db/monolith-migrations/index.txt").asFile
        migrationIndex.parentFile.mkdirs()
        migrationIndex.writeText(
            migrationModules
                .flatMap { module ->
                    rootProject.file("$module/src/main/resources/db/migration")
                        .walkTopDown()
                        .filter { it.isFile && it.extension.equals("sql", ignoreCase = true) }
                        .map { file -> "db/monolith-migrations/$module/${file.relativeTo(rootProject.file("$module/src/main/resources/db/migration")).invariantSeparatorsPath}" }
                        .toList()
                }
                .sorted()
                .joinToString(separator = "\n", postfix = "\n"),
        )
    }
}

sourceSets.main {
    resources.srcDir(generatedMigrations)
}

tasks.named("processResources") {
    dependsOn(collectModuleMigrations)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
