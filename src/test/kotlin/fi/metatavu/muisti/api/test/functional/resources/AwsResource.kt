package fi.metatavu.muisti.api.test.functional.resources

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName


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

        val s3Client = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(
                AwsClientBuilder.EndpointConfiguration(
                    endpoint.toString(),
                    s3.region
                )
            )
            .withCredentials(
                AWSStaticCredentialsProvider(
                    BasicAWSCredentials(s3.accessKey, s3.secretKey)
                )
            )
            .build()
        s3Client.createBucket(bucketName)
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