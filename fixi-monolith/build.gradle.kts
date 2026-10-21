plugins {
    java
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
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

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
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
