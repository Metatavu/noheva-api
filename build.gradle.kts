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
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val jaxrs_functional_test_builder_version: String by project
val testContainersVersion: String by project
val testContainersKeycloakVersion: String by project
val awssdk_version: String by project
val jackson_version: String by project
val paho_version: String by project
val jts_core_version: String by project
val hibernate_spatial_version: String by project
val awaitility_version: String by project
val liquibase_version: String by project
val moshiVersion: String by project

dependencies {
    kapt("org.hibernate:hibernate-jpamodelgen:5.4.11.Final")

    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-liquibase")
    implementation("io.quarkus:quarkus-jdbc-mysql")

    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-keycloak-admin-client")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")




    implementation(platform ("com.amazonaws:aws-java-sdk-bom:$awssdk_version"))
    implementation("commons-io:commons-io")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.infinispan:infinispan-core")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
    implementation("com.amazonaws:aws-java-sdk-s3")
    implementation("io.quarkus:quarkus-liquibase")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:$paho_version")
    implementation("io.quarkus:quarkus-undertow")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("io.quarkus:quarkus-jdbc-mysql")
    /**
     * Spatial dependencies
     */
    implementation("com.vividsolutions:jts-core:$jts_core_version")
    implementation("org.hibernate:hibernate-spatial:$hibernate_spatial_version")

    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:hivemq")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:localstack")
    testImplementation("com.github.dasniko:testcontainers-keycloak:2.4.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.4.2")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("fi.metatavu.jaxrs.testbuilder:jaxrs-functional-test-builder:$jaxrs_functional_test_builder_version")
    testImplementation("org.awaitility:awaitility:$awaitility_version")

    //jacocoAgent("org.jacoco:org.jacoco.agent:0.8.5")
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
    setProperty("inputSpec", "$rootDir/muisti-api-spec/swagger.yaml")
    setProperty("outputDir", "$buildDir/generated/api-spec")
    setProperty("apiPackage", "fi.metatavu.muisti.api.spec")
    setProperty("invokerPackage", "fi.metatavu.muisti.api.spec.invoker")
    setProperty("modelPackage", "fi.metatavu.muisti.api.spec.model")

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
    setProperty("inputSpec", "$rootDir/muisti-api-spec/swagger.yaml")
    setProperty("outputDir", "$buildDir/generated/api-client")
    setProperty("packageName", "fi.metatavu.muisti.api.client")
    this.configOptions.put("dateLibrary", "string")
    this.configOptions.put("collectionType", "array")
    this.configOptions.put("enumPropertyNaming", "UPPERCASE")
    this.configOptions.put("serializationLibrary", "jackson")
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