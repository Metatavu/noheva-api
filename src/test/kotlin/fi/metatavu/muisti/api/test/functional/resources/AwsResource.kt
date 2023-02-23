package fi.metatavu.muisti.api.test.functional.resources

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.endpoints.internal.DefaultS3EndpointProvider
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import java.net.URI


var localstackImage: DockerImageName = DockerImageName.parse("localstack/localstack:0.11.3")

/**
 * Test resource for S3 storage
 */
class AwsResource : QuarkusTestResourceLifecycleManager {
    override fun start(): Map<String, String> {
        s3.start()
        val config: MutableMap<String, String> = HashMap()

        val bucketName = "testbucket"
        val endpoint = s3.getEndpointOverride(LocalStackContainer.Service.S3)
        config["s3.file.storage.region"] = s3.region
        config["s3.file.storage.bucket"] = bucketName
        config["s3.file.storage.prefix"] = "http://${endpoint.authority}/${bucketName}"
        config["s3.file.storage.keyid"] = s3.accessKey
        config["s3.file.storage.secret"] = s3.secretKey
        config["s3.file.storage.endpoint"] = endpoint.toString()
        config["file.storage.provider"] = "S3"
        config["quarkus.profile"] = "test"

            val s3Client = S3Client.builder()
            .region(Region.US_WEST_2)
            .endpointOverride(endpoint)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        s3.accessKey,
                        s3.secretKey
                    )
                )
            ).build()
        s3Client.createBucket(
            CreateBucketRequest.builder()
                .bucket(bucketName)
                .build()
        )
        return config
    }

    override fun stop() {
        s3.stop()
    }

    companion object {
        var s3: LocalStackContainer = LocalStackContainer(localstackImage)
            .withServices(LocalStackContainer.Service.S3)
            .withExposedPorts(4572)
    }
}