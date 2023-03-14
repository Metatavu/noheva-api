package fi.metatavu.noheva.api.test.functional

import fi.metatavu.noheva.api.client.models.Bounds
import fi.metatavu.noheva.api.client.models.Coordinates
import fi.metatavu.noheva.api.client.models.ExhibitionFloor
import fi.metatavu.noheva.api.test.functional.resources.KeycloakResource
import fi.metatavu.noheva.api.test.functional.resources.MqttResource
import fi.metatavu.noheva.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing exhibition floors API
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class ExhibitionFloorTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionFloor() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val createdExhibitionFloor = it.admin.exhibitionFloors.create(exhibition.id!!, ExhibitionFloor(name = "name"))
            assertNotNull(createdExhibitionFloor)
        }
   }

    @Test
    fun testFindExhibitionFloor() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionFloorId = UUID.randomUUID()
            val createdExhibitionFloor = it.admin.exhibitionFloors.create(exhibitionId)
            val createdExhibitionFloorId = createdExhibitionFloor.id!!

            it.admin.exhibitionFloors.assertFindFail(404, exhibitionId, nonExistingExhibitionFloorId)
            it.admin.exhibitionFloors.assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionFloorId)
            it.admin.exhibitionFloors.assertFindFail(404, nonExistingExhibitionId, createdExhibitionFloorId)
            assertNotNull(it.admin.exhibitionFloors.findExhibitionFloor(exhibitionId, createdExhibitionFloorId))
        }
    }

    @Test
    fun testListExhibitionFloors() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin.exhibitionFloors.assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin.exhibitionFloors.listExhibitionFloors(exhibitionId).size)

            val createdExhibitionFloor = it.admin.exhibitionFloors.create(exhibitionId)
            val createdExhibitionFloorId = createdExhibitionFloor.id!!
            val exhibitionFloors = it.admin.exhibitionFloors.listExhibitionFloors(exhibitionId)
            assertEquals(1, exhibitionFloors.size)
            assertEquals(createdExhibitionFloorId, exhibitionFloors[0].id)
            it.admin.exhibitionFloors.delete(exhibitionId, createdExhibitionFloorId)
            assertEquals(0, it.admin.exhibitionFloors.listExhibitionFloors(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val createdExhibitionFloor = it.admin.exhibitionFloors.create(exhibitionId, ExhibitionFloor(name = "created name"))
            val createdExhibitionFloorId = createdExhibitionFloor.id!!

            val foundCreatedExhibitionFloor = it.admin.exhibitionFloors.findExhibitionFloor(exhibitionId, createdExhibitionFloorId)
            assertEquals(createdExhibitionFloor.id, foundCreatedExhibitionFloor.id)
            assertEquals("created name", createdExhibitionFloor.name)
            val bounds = Bounds(
                northEastCorner = Coordinates(latitude = 18.22, longitude = 28.5),
                southWestCorner = Coordinates(latitude = 13.22, longitude = 22.5)
            )
            val exhibitionFloorToUpdate = ExhibitionFloor(
                    name = "updated name",
                    id = createdExhibitionFloorId,
                    floorPlanUrl = "http://example.com/floorPlan.png",
                    floorPlanBounds = bounds
            )

            val updatedExhibitionFloor = it.admin.exhibitionFloors.updateExhibitionFloor(exhibitionId, exhibitionFloorToUpdate)
            val foundUpdatedExhibitionFloor = it.admin.exhibitionFloors.findExhibitionFloor(exhibitionId, createdExhibitionFloorId)
            assertEquals(updatedExhibitionFloor.id, foundUpdatedExhibitionFloor.id)
            assertEquals("updated name", foundUpdatedExhibitionFloor.name)
            assertEquals("http://example.com/floorPlan.png", foundUpdatedExhibitionFloor.floorPlanUrl)
            assertEquals(18.22, foundUpdatedExhibitionFloor.floorPlanBounds?.northEastCorner?.latitude)
            assertEquals(28.5, foundUpdatedExhibitionFloor.floorPlanBounds?.northEastCorner?.longitude)
            assertEquals(13.22, foundUpdatedExhibitionFloor.floorPlanBounds?.southWestCorner?.latitude)
            assertEquals(22.5, foundUpdatedExhibitionFloor.floorPlanBounds?.southWestCorner?.longitude)

            it.admin.exhibitionFloors.assertUpdateFail(404, nonExistingExhibitionId, ExhibitionFloor("name", createdExhibitionFloorId))
        }
    }

    @Test
    fun testDeleteExhibition() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdExhibitionFloor = it.admin.exhibitionFloors.create(exhibitionId)
            val createdExhibitionFloorId = createdExhibitionFloor.id!!

            assertNotNull(it.admin.exhibitionFloors.findExhibitionFloor(exhibitionId, createdExhibitionFloorId))
            it.admin.exhibitionFloors.assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin.exhibitionFloors.assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionFloorId)
            it.admin.exhibitionFloors.assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin.exhibitionFloors.delete(exhibitionId, createdExhibitionFloor)

            it.admin.exhibitionFloors.assertDeleteFail(404, exhibitionId, createdExhibitionFloorId)
        }
    }

}
