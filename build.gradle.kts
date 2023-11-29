import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.allopen") version "1.6.10"
    id("io.quarkus")
    id("org.openapi.generator") version "6.2.1"
    id("org.jetbrains.kotlin.kapt") version "1.6.10"

}

repositories {
    mavenLocal()
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val jaxrsFunctionalTestBuilderVersion: String by project
val testContainersKeycloakVersion: String by project
val awssdkVersion: String by project
val jacksonVersion: String by project
val pahoVersion: String by project
val jtsCoreVersion: String by project
val hibernateSpatialVersion: String by project
val awaitilityVersion: String by project
val moshiVersion: String by project
val testContainersVersion: String by project
val camelPahoVersion: String by project
val registerReflectionVersion: String by project
val jsoupVersion: String by project

dependencies {
    kapt("org.hibernate:hibernate-jpamodelgen:5.4.11.Final")

    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-liquibase")
    implementation("io.quarkus:quarkus-jdbc-mysql")
    implementation("io.quarkus:quarkus-undertow")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-keycloak-admin-client")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-awt")
    implementation("org.apache.camel.quarkus:camel-quarkus-paho:$camelPahoVersion")

    implementation("commons-io:commons-io")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("software.amazon.awssdk:s3:$awssdkVersion")
    implementation("software.amazon.awssdk:apache-client:$awssdkVersion")
    implementation("com.github.metatavu.quarkus-register-reflection:quarkus-register-reflection:$registerReflectionVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")

    /**
     * Spatial dependencies
     */
    implementation("org.hibernate:hibernate-spatial:$hibernateSpatialVersion")

    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:hivemq")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:localstack")
    testImplementation("com.github.dasniko:testcontainers-keycloak:2.4.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.4.2")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("fi.metatavu.jaxrs.testbuilder:jaxrs-functional-test-builder:$jaxrsFunctionalTestBuilderVersion")
    testImplementation("org.awaitility:awaitility:$awaitilityVersion")
    testImplementation("com.amazonaws:aws-java-sdk-s3:1.12.393")
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("javax.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

sourceSets["main"].java {
    srcDir("build/generated/api-spec/src/main/kotlin")
    srcDir("build/generated/keycloak-client/src/main/kotlin")
}

sourceSets["test"].java {
    srcDir("build/generated/api-client/src/main/kotlin")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlinOptions.javaParameters = true
}

val generateApiSpec = tasks.register("generateApiSpec", GenerateTask::class) {
    setProperty("generatorName", "kotlin-server")
    setProperty("inputSpec", "$rootDir/noheva-api-spec/swagger.yaml")
    setProperty("outputDir", "$buildDir/generated/api-spec")
    setProperty("apiPackage", "fi.metatavu.noheva.api.spec")
    setProperty("invokerPackage", "fi.metatavu.noheva.api.spec.invoker")
    setProperty("modelPackage", "fi.metatavu.noheva.api.spec.model")

    this.configOptions.put("library", "jaxrs-spec")
    this.configOptions.put("dateLibrary", "java8")
    this.configOptions.put("interfaceOnly", "true")
    this.configOptions.put("useCoroutines", "false")
    this.configOptions.put("enumPropertyNaming", "UPPERCASE")
    this.configOptions.put("returnResponse", "true")
    this.configOptions.put("useSwaggerAnnotations", "false")
    this.configOptions.put("additionalModelTypeAnnotations", "@io.quarkus.runtime.annotations.RegisterForReflection")
}

val generateApiClient = tasks.register("generateApiClient", GenerateTask::class) {
    setProperty("generatorName", "kotlin")
    setProperty("library", "jvm-okhttp3")
    setProperty("inputSpec", "$rootDir/noheva-api-spec/swagger.yaml")
    setProperty("outputDir", "$buildDir/generated/api-client")
    setProperty("packageName", "fi.metatavu.noheva.api.client")
    this.configOptions.put("dateLibrary", "string")
    this.configOptions.put("collectionType", "array")
    this.configOptions.put("enumPropertyNaming", "UPPERCASE")
    this.configOptions.put("serializationLibrary", "jackson")
}

tasks.register("generateClients") {
    dependsOn(generateApiSpec)
    dependsOn(generateApiClient)
}

tasks.named("compileKotlin") {
    dependsOn(generateApiSpec)
}

tasks.named("compileTestKotlin") {
    dependsOn(generateApiClient)
}

tasks.named("clean") {
    this.doFirst {
        file("$rootDir/src/gen").deleteRecursively()
    }
}

tasks.named<Test>("testNative") {
  testLogging.showStandardStreams = true
}
