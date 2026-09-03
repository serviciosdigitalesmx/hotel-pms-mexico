plugins {
    java
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.danilopianini.gradle-java-qa") version "1.165.0"
    // Opt-in Native Image build; config-service/Dockerfile remains the JVM fallback.
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "com.hotelpms"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

springBoot {
    mainClass.set("com.hotelpms.config.ConfigServiceApplication")
}

graalvmNative {
    binaries {
        named("main") {
            // Quick reachability gate is selected only by the CI quick job.
            if (providers.gradleProperty("nativeQuickBuild").orNull == "true") {
                buildArgs.add("-Ob")
            } else {
                buildArgs.add("-O2")
            }
            buildArgs.add("-J-Xmx12g")
            buildArgs.add("--parallelism=2")
            // ConfigServerRuntimeHints retains JGit SSH classes even though
            // the native profile uses the classpath repository. Graal's JDK
            // ServiceLoader otherwise places optional SSH/SFTP objects in the
            // image heap with runtime initialization.
            buildArgs.add("--initialize-at-build-time=org.apache.sshd.common.file.root.RootedFileSystemProvider")
            buildArgs.add("--initialize-at-build-time=org.apache.sshd.sftp.client.fs.SftpFileSystemProvider")
            buildArgs.add("--initialize-at-build-time=org.apache.sshd.sftp.client.SftpErrorDataHandler")
            buildArgs.add("--initialize-at-build-time=org.apache.sshd.sftp.client.fs.SftpFileSystemClientSessionInitializer")
            // The anonymous initializer is materialized by the JDK ServiceLoader;
            // initialize the SFTP provider package together so its generated
            // implementation classes cannot remain in the runtime-init heap.
            buildArgs.add("--initialize-at-build-time=org.apache.sshd.sftp.client.fs")
            buildArgs.add("--initialize-at-build-time=org.apache.sshd.sftp.client.SftpVersionSelector.${'$'}NamedVersionSelector")
            buildArgs.add("--initialize-at-build-time=org.apache.sshd.client.SshClient")
            buildArgs.add("--initialize-at-build-time=org.apache.sshd.common.Factory")
            buildArgs.add("--initialize-at-build-time=org.apache.sshd.common")
            buildArgs.add("--initialize-at-build-time=org.apache.sshd")
            buildArgs.add("--initialize-at-build-time=java.util.function.Supplier")
            buildArgs.add("--initialize-at-build-time=ch.qos.logback.classic.Logger")
            buildArgs.add("--initialize-at-build-time=ch.qos.logback")
            buildArgs.add("--initialize-at-build-time=net.i2p.crypto.eddsa")
        }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

ext {
    set("springCloudVersion", "2025.0.0")
    // CVE-2026-42583/42584/42579/42587: fixed in 4.1.133.Final; CVE-2026-47691/45674/45416/44249: fixed in 4.1.135.Final; CVE-2026-56745/55833/55831/59901: fixed in 4.1.136.Final — override Spring Boot BOM pin.
    set("netty.version", "4.1.136.Final")
    // CVE-2026-43512/43513/43515/41284/41293/42498: fixed in Tomcat 10.1.55 (2026-05-05).
    set("tomcat.version", "10.1.55")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
    dependencies {
        // CVE-2025-48976 (commons-fileupload 1.5→1.6.0) + CVE-2024-47554 (commons-io 2.11.0→2.14.0)
        // commons-fileupload is not managed by Spring Boot 3.5.x BOM (removed with CommonsMultipartResolver
        // in Spring 6.1); dependencyManagement.dependencies forces the version regardless of BOM properties.
        dependency("commons-fileupload:commons-fileupload:1.6.0")
        dependency("commons-io:commons-io:2.14.0")
        // CVE-2026-5598: fixed in BouncyCastle 1.84.
        dependency("org.bouncycastle:bcprov-jdk18on:1.84")
        // CVE-2026-40981 (GCP Secrets Manager backend info disclosure, not used here, defense
        // in depth) + CVE-2026-40982 CRITICAL (path traversal serving arbitrary files via crafted
        // URL): Spring Cloud 2025.0.0 BOM pins spring-cloud-config-server 4.3.0, vulnerable through
        // 4.3.2. Fixed in 4.3.3 — override regardless of BOM.
        dependency("org.springframework.cloud:spring-cloud-config-server:4.3.3")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// AOT tests are generated framework code. Keep the cheap JVM regression suite
// and QA focused on handwritten sources; nativeCompile still runs processAot.
tasks.matching {
    it.name in setOf(
        "processTestAot",
        "compileAotTestJava",
        "processAotTestResources",
        "aotTestClasses",
        "checkstyleAot",
        "checkstyleAotTest",
        "pmdAot",
        "pmdAotTest",
        "spotbugsAot",
        "spotbugsAotTest"
    )
}.configureEach {
    enabled = false
}
