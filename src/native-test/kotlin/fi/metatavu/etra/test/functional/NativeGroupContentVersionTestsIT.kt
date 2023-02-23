package fi.metatavu.etra.test.functional

import fi.metatavu.muisti.api.test.functional.DefaultTestProfile
import fi.metatavu.muisti.api.test.functional.GroupContentVersionTestsIT
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

@QuarkusIntegrationTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(DefaultTestProfile::class)
class NativeGroupContentVersionTestsIT : GroupContentVersionTestsIT()