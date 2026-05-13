plugins {
    kotlin("jvm") version "1.9.24"
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.spring") version "1.9.24"

}

group = "com.dekogen.granular-deployment"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_21

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
//    implementation("org.springframework.cloud:spring-cloud-kubernetes-istio:1.1.9.RELEASE")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

//    implementation("io.kubernetes:client-java:12.0.0")
    implementation("io.fabric8:kubernetes-client:5.4.1")
    implementation("me.snowdrop:istio-common:1.7.7.1")
    implementation("me.snowdrop:istio-client:1.7.7.1")
    implementation("me.snowdrop:istio-model:1.7.7.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17")
    implementation("io.github.classgraph:classgraph:4.8.149")

    implementation("org.openapitools.openapidiff:openapi-diff-core:2.0.0-beta.10")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    imageName = "10.0.10.1:5000/granular-deployment-orchestrator:${project.version}"
//    publish.set(true)
//    docker {
//        publishRegistry {
//            username = "techpriest"
//            password = "techpriest"
//            url = "http://10.0.10.1:5000"
//        }
//    }
}
