package fi.metatavu.etra.test.functional

import fi.metatavu.muisti.api.test.functional.ContentVersionTestsIT
import fi.metatavu.muisti.api.test.functional.DeviceModelTestsIT
import fi.metatavu.muisti.api.test.functional.ExhibitionDeviceGroupTestsIT
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MqttResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest

@QuarkusIntegrationTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class NativeExhibitionDeviceGroupTestsIT : ExhibitionDeviceGroupTestsIT() {
}