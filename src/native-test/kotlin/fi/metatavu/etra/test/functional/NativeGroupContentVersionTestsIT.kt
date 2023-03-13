package fi.metatavu.etra.test.functional

import fi.metatavu.muisti.api.test.functional.GroupContentVersionTestsIT
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest

@QuarkusIntegrationTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class)
)
class NativeGroupContentVersionTestsIT : GroupContentVersionTestsIT()