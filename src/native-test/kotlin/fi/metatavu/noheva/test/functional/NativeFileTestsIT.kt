package fi.metatavu.noheva.test.functional

import fi.metatavu.noheva.api.test.functional.FileTestsIT
import fi.metatavu.noheva.api.test.functional.resources.AwsResource
import fi.metatavu.noheva.api.test.functional.resources.KeycloakResource
import fi.metatavu.noheva.api.test.functional.resources.MqttResource
import fi.metatavu.noheva.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest

@QuarkusIntegrationTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class),
    QuarkusTestResource(AwsResource::class)
)
class NativeFileTestsIT : FileTestsIT()