package fi.metatavu.noheva.api.test.functional

import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import fi.metatavu.noheva.api.test.functional.resources.KeycloakResource
import fi.metatavu.noheva.api.test.functional.resources.MqttResource
import fi.metatavu.noheva.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing exhibitions API
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class ExhibitionTestsIT : AbstractFunctionalTest() {

    @Test
    fun testCreateExhibition() {
        createTestBuilder().use {
            assertNotNull(it.admin.exhibitions.create())
            it.admin.exhibitions.assertCreateFail(expectedStatus = 400, payload = Exhibition(name = ""))
        }
    }

    @Test
    fun testCopyExhibitionUniqueName() {
        createTestBuilder().use {
            val sourceExhibition = it.admin.exhibitions.create(payload = Exhibition(name = "copy test"))
            val copiedExhibition = it.admin.exhibitions.copy(sourceExhibitionId = sourceExhibition.id!!)
            assertNotEquals(sourceExhibition.id, copiedExhibition.id)
            assertNotNull(copiedExhibition)
            assertEquals("copy test 2", copiedExhibition.name)

            cleanCopiedExhibition(
                apiTestBuilder = it,
                copiedExhibition = copiedExhibition
            )
        }
    }

    @Test
    fun testCopyExhibitionVisitorVariables() {
        createTestBuilder().use {
            val sourceExhibition = it.admin.exhibitions.create(payload = Exhibition(name = "copy test"))
            val sourceExhibitionId = sourceExhibition.id!!

            val sourceVisitorVariable1 = it.admin.visitorVariables.create(
                exhibitionId = sourceExhibitionId,
                payload = VisitorVariable(name = "var1", type = VisitorVariableType.NUMBER, editableFromUI = false)
            )

            val sourceVisitorVariable2 = it.admin.visitorVariables.create(
                exhibitionId = sourceExhibitionId,
                payload = VisitorVariable(name = "var2", type = VisitorVariableType.TEXT, editableFromUI = true)
            )

            val copiedExhibition = it.admin.exhibitions.copy(sourceExhibitionId = sourceExhibitionId)
            val copiedVisitorVariables = it.admin.visitorVariables.listVisitorVariables(exhibitionId = copiedExhibition.id!!, name = null)
            assertEquals(2, copiedVisitorVariables.size)

            val copiedVisitorVariable1 = copiedVisitorVariables.first { visitorVariable -> visitorVariable.name == sourceVisitorVariable1.name }
            assertNotNull(copiedVisitorVariable1)
            assertEquals(sourceVisitorVariable1.name, copiedVisitorVariable1.name)
            assertEquals(VisitorVariableType.NUMBER, copiedVisitorVariable1.type)
            assertFalse(copiedVisitorVariable1.editableFromUI)
            assertNotEquals(sourceVisitorVariable1.id, copiedVisitorVariable1.id)

            val copiedVisitorVariable2 = copiedVisitorVariables.first { visitorVariable -> visitorVariable.name == sourceVisitorVariable2.name }
            assertNotNull(copiedVisitorVariable2)
            assertEquals(sourceVisitorVariable2.name, copiedVisitorVariable2.name)
            assertEquals(VisitorVariableType.TEXT, copiedVisitorVariable2.type)
            assertTrue(copiedVisitorVariable2.editableFromUI)
            assertNotEquals(sourceVisitorVariable2.id, copiedVisitorVariable2.id)

            cleanCopiedExhibition(
                apiTestBuilder = it,
                copiedExhibition = copiedExhibition
            )
        }
    }

    @Test
    fun testCopyExhibitionFloors() {
        createTestBuilder().use {
            val sourceExhibition = it.admin.exhibitions.create(payload = Exhibition(name = "copy test"))
            val sourceExhibitionId = sourceExhibition.id!!

            val sourceFloor1 = it.admin.exhibitionFloors.create(
                exhibitionId = sourceExhibitionId,
                payload = ExhibitionFloor(
                    name = "floor1",
                    floorPlanUrl = "http://example.com/floor1.png",
                    floorPlanBounds = Bounds(
                        northEastCorner = Coordinates(latitude = 18.22, longitude = 28.5),
                        southWestCorner = Coordinates(latitude = 13.22, longitude = 22.5)
                    )
                )
            )

            val sourceFloor2 = it.admin.exhibitionFloors.create(
                exhibitionId = sourceExhibitionId,
                payload = ExhibitionFloor(
                    name = "floor2"
                )
            )

            val copiedExhibition = it.admin.exhibitions.copy(sourceExhibitionId = sourceExhibitionId)
            val copiedFloors = it.admin.exhibitionFloors.listExhibitionFloors(exhibitionId = copiedExhibition.id!!)
            assertEquals(2, copiedFloors.size)

            val copiedFloor1 = copiedFloors.first { floor -> floor.name == sourceFloor1.name }
            assertNotNull(copiedFloor1)
            assertEquals(sourceFloor1.name, copiedFloor1.name)
            assertEquals(sourceFloor1.floorPlanUrl, copiedFloor1.floorPlanUrl)
            assertJsonsEqual(sourceFloor1.floorPlanBounds, copiedFloor1.floorPlanBounds)
            assertNotEquals(sourceFloor1.id, copiedFloor1.id)

            val copiedFloor2 = copiedFloors.first { floor -> floor.name == sourceFloor2.name }
            assertNotNull(copiedFloor2)
            assertEquals(sourceFloor2.name, copiedFloor2.name)
            assertEquals(sourceFloor2.floorPlanUrl, copiedFloor2.floorPlanUrl)
            assertJsonsEqual(sourceFloor2.floorPlanBounds, copiedFloor2.floorPlanBounds)
            assertNotEquals(sourceFloor2.id, copiedFloor2.id)

            cleanCopiedExhibition(
                apiTestBuilder = it,
                copiedExhibition = copiedExhibition
            )
        }
    }

    @Test
    fun testCopyExhibitionRooms() {
        createTestBuilder().use {
            val sourceExhibition = it.admin.exhibitions.create(payload = Exhibition(name = "copy test"))
            val sourceExhibitionId = sourceExhibition.id!!
            val sourceFloors =  (1..3).map { i ->
                it.admin.exhibitionFloors.create(
                    exhibitionId = sourceExhibitionId,
                    payload = ExhibitionFloor(
                        name = "floor$i"
                    )
                )
            }

            val sourceRooms = (1..3).map { i ->
                val floorIndex = if (i == 3) 1 else 0
                it.admin.exhibitionRooms.create(
                    exhibitionId = sourceExhibitionId,
                    payload = ExhibitionRoom(
                        name = "room$i",
                        floorId = sourceFloors[floorIndex].id!!
                    )
                )
            }

            val copiedExhibition = it.admin.exhibitions.copy(sourceExhibitionId = sourceExhibitionId)
            val copiedExhibitionId = copiedExhibition.id!!
            val copiedFloors = it.admin.exhibitionFloors.listExhibitionFloors(exhibitionId = copiedExhibitionId)
            val copiedRooms = it.admin.exhibitionRooms.listExhibitionRooms(exhibitionId = copiedExhibitionId, floorId = null)
            assertEquals(3, copiedRooms.size)

            val copiedFloor1 = copiedFloors.first { floor -> floor.name == sourceFloors[0].name }
            assertNotNull(copiedFloor1)
            val copiedFloor2 = copiedFloors.first { floor -> floor.name == sourceFloors[1].name }
            assertNotNull(copiedFloor2)

            val copiedRoom1 = copiedRooms.first { room -> room.name == sourceRooms[0].name }
            assertNotNull(copiedRoom1)
            assertEquals(sourceRooms[0].name, copiedRoom1.name)
            assertEquals(copiedFloor1.id, copiedRoom1.floorId)
            assertNotEquals(sourceRooms[0].id, copiedRoom1.id)

            val copiedRoom2 = copiedRooms.first { room -> room.name == sourceRooms[1].name }
            assertNotNull(copiedRoom2)
            assertEquals(sourceRooms[1].name, copiedRoom2.name)
            assertEquals(copiedFloor1.id, copiedRoom2.floorId)
            assertNotEquals(sourceRooms[1].id, copiedRoom2.id)

            val copiedRoom3 = copiedRooms.first { room -> room.name == sourceRooms[2].name }
            assertNotNull(copiedRoom3)
            assertEquals(sourceRooms[2].name, copiedRoom3.name)
            assertEquals(copiedFloor2.id, copiedRoom3.floorId)
            assertNotEquals(sourceRooms[2].id, copiedRoom3.id)

            cleanCopiedExhibition(
                apiTestBuilder = it,
                copiedExhibition = copiedExhibition
            )
        }
    }

    @Test
    fun testCopyExhibitionGroups() {
        createTestBuilder().use {
            val sourceExhibition = it.admin.exhibitions.create(payload = Exhibition(name = "copy test"))
            val sourceExhibitionId = sourceExhibition.id!!
            val sourceFloors =  (1..3).map { i ->
                it.admin.exhibitionFloors.create(
                    exhibitionId = sourceExhibitionId,
                    payload = ExhibitionFloor(
                        name = "floor$i"
                    )
                )
            }

            val sourceRooms = (1..3).map { i ->
                val floorIndex = if (i == 3) 1 else 0
                it.admin.exhibitionRooms.create(
                    exhibitionId = sourceExhibitionId,
                    payload = ExhibitionRoom(
                        name = "room$i",
                        floorId = sourceFloors[floorIndex].id!!
                    )
                )
            }

            val sourceGroups = (1..3).map { i ->
                val roomIndex = if (i == 3) 1 else 0
                it.admin.exhibitionDeviceGroups.create(
                    exhibitionId = sourceExhibitionId,
                    sourceDeviceGroupId = null,
                    payload = ExhibitionDeviceGroup(
                        name = "group$i",
                        roomId = sourceRooms[roomIndex].id!!,
                        allowVisitorSessionCreation = i == 1,
                        visitorSessionEndTimeout = 10L * i,
                        visitorSessionStartStrategy = DeviceGroupVisitorSessionStartStrategy.ENDOTHERS
                    )
                )
            }

            val copiedExhibition = it.admin.exhibitions.copy(sourceExhibitionId = sourceExhibitionId)
            val copiedExhibitionId = copiedExhibition.id!!
            val copiedGroups = it.admin.exhibitionDeviceGroups.listExhibitionDeviceGroups(exhibitionId = copiedExhibitionId, roomId = null)
            assertEquals(3, copiedGroups.size)

            val copiedRooms = it.admin.exhibitionRooms.listExhibitionRooms(exhibitionId = copiedExhibitionId, floorId = null)

            val copiedRoom1 = copiedRooms.first { floor -> floor.name == sourceRooms[0].name }
            assertNotNull(copiedRoom1)
            val copiedRoom2 = copiedRooms.first { floor -> floor.name == sourceRooms[1].name }
            assertNotNull(copiedRoom2)

            val copiedGroup1 = copiedGroups.first { group -> group.name == sourceGroups[0].name }
            assertNotNull(copiedGroup1)
            assertEquals(sourceGroups[0].name, copiedGroup1.name)
            assertEquals(copiedRoom1.id, copiedGroup1.roomId)
            assertNotEquals(sourceGroups[0].id, copiedGroup1.id)
            assertEquals(sourceGroups[0].allowVisitorSessionCreation, copiedGroup1.allowVisitorSessionCreation)
            assertEquals(sourceGroups[0].visitorSessionEndTimeout, copiedGroup1.visitorSessionEndTimeout)
            assertEquals(sourceGroups[0].visitorSessionStartStrategy, copiedGroup1.visitorSessionStartStrategy)

            val copiedGroup2 = copiedGroups.first { group -> group.name == sourceGroups[1].name }
            assertNotNull(copiedGroup2)
            assertEquals(sourceGroups[1].name, copiedGroup2.name)
            assertEquals(copiedRoom1.id, copiedGroup2.roomId)
            assertNotEquals(sourceGroups[1].id, copiedGroup2.id)
            assertEquals(sourceGroups[1].allowVisitorSessionCreation, copiedGroup2.allowVisitorSessionCreation)
            assertEquals(sourceGroups[1].visitorSessionEndTimeout, copiedGroup2.visitorSessionEndTimeout)
            assertEquals(sourceGroups[1].visitorSessionStartStrategy, copiedGroup2.visitorSessionStartStrategy)

            val copiedGroup3 = copiedGroups.first { group -> group.name == sourceGroups[2].name }
            assertNotNull(copiedGroup3)
            assertEquals(sourceGroups[2].name, copiedGroup3.name)
            assertEquals(copiedRoom2.id, copiedGroup3.roomId)
            assertNotEquals(sourceGroups[2].id, copiedGroup3.id)
            assertEquals(sourceGroups[2].allowVisitorSessionCreation, copiedGroup3.allowVisitorSessionCreation)
            assertEquals(sourceGroups[2].visitorSessionEndTimeout, copiedGroup3.visitorSessionEndTimeout)
            assertEquals(sourceGroups[2].visitorSessionStartStrategy, copiedGroup3.visitorSessionStartStrategy)

            cleanCopiedExhibition(
                apiTestBuilder = it,
                copiedExhibition = copiedExhibition
            )
        }
    }

    @Test
    fun testCopyExhibitionWithContents() {
        createTestBuilder().use { testBuilder ->
            val sourceExhibition = testBuilder.admin.exhibitions.create(payload = Exhibition(name = "copy test"))
            val sourceExhibitionId = sourceExhibition.id!!
            val sourceFloor =  testBuilder.admin.exhibitionFloors.create(
                exhibitionId = sourceExhibitionId,
                payload = ExhibitionFloor(name = "copy test floor")
            )

            val sourceRoom = testBuilder.admin.exhibitionRooms.create(
                exhibitionId = sourceExhibitionId,
                payload = ExhibitionRoom(
                    name = "copy test room",
                    floorId = sourceFloor.id!!
                )
            )

            val sourceGroup = testBuilder.admin.exhibitionDeviceGroups.create(
                exhibitionId = sourceExhibitionId,
                sourceDeviceGroupId = null,
                payload = ExhibitionDeviceGroup(
                    name = "copy test group",
                    roomId = sourceRoom.id!!,
                    allowVisitorSessionCreation = false,
                    visitorSessionEndTimeout = 10L,
                    visitorSessionStartStrategy = DeviceGroupVisitorSessionStartStrategy.ENDOTHERS
                )
            )

            val sourceContentVersion = testBuilder.admin.contentVersions.create(
                exhibitionId = sourceExhibitionId,
                payload = ContentVersion(
                    language = "FI",
                    name = "copy test content version",
                    rooms = arrayOf(sourceRoom.id),
                    activeCondition = ContentVersionActiveCondition(userVariable = "test-var", equals = "test-val")
                )
            )

            val sourceGroupContentVersion = testBuilder.admin.groupContentVersions.create(
                exhibitionId = sourceExhibitionId,
                payload = GroupContentVersion(
                    contentVersionId = sourceContentVersion.id!!,
                    deviceGroupId = sourceGroup.id!!,
                    name = "copy test group content version",
                    status = GroupContentVersionStatus.READY
                )
            )

            val model = testBuilder.admin.deviceModels.create()

            var sourceDevice = testBuilder.admin.exhibitionDevices.create(
                exhibitionId = sourceExhibition.id,
                payload = ExhibitionDevice(
                    groupId = sourceGroup.id,
                    modelId = model.id!!,
                    name = "copy test device",
                    screenOrientation = ScreenOrientation.PORTRAIT,
                    imageLoadStrategy = DeviceImageLoadStrategy.MEMORY
                )
            )

            val deviceModel = testBuilder.admin.deviceModels.create()
            val defaultPageLayout = testBuilder.admin.pageLayouts.create(deviceModel)
            val sourceLayout = testBuilder.admin.pageLayouts.create(defaultPageLayout)

            val sourceDevicePage = testBuilder.admin.exhibitionPages.create(
                exhibitionId = sourceExhibitionId,
                payload = ExhibitionPage(
                    name = "copy device page",
                    contentVersionId = sourceContentVersion.id,
                    deviceId = sourceDevice.id!!,
                    orderNumber = 0,
                    resources = arrayOf(),
                    eventTriggers = arrayOf(),
                    enterTransitions = arrayOf(),
                    exitTransitions = arrayOf(),
                    layoutId = sourceLayout.id!!
                )
            )

            val sourceIdlePage = testBuilder.admin.exhibitionPages.create(
                exhibitionId = sourceExhibitionId,
                payload = ExhibitionPage(
                    deviceId = sourceDevice.id!!,
                    name = "copy idle page",
                    orderNumber = 0,
                    resources = arrayOf(),
                    eventTriggers = arrayOf(),
                    contentVersionId = sourceContentVersion.id,
                    enterTransitions = arrayOf(),
                    exitTransitions = arrayOf(),
                    layoutId = sourceLayout.id
                )
            )

            sourceDevice = testBuilder.admin.exhibitionDevices.updateExhibitionDevice(
                exhibitionId = sourceExhibitionId,
                payload = sourceDevice.copy(
                    idlePageId = sourceIdlePage.id!!
                )
            )

            val sourceAntenna = testBuilder.admin.rfidAntennas.create(
                exhibitionId = sourceExhibitionId,
                payload = RfidAntenna(
                    groupId = sourceGroup.id,
                    roomId = sourceRoom.id,
                    name = "copy test antenna",
                    antennaNumber = 5,
                    readerId = "readid",
                    location = Point(x = 123.0, y = 234.0),
                    visitorSessionStartThreshold = 80,
                    visitorSessionEndThreshold = 10
                )
            )

            // Copy exhibition
            val copiedExhibition = testBuilder.admin.exhibitions.copy(sourceExhibitionId = sourceExhibitionId)
            val copiedExhibitionId = copiedExhibition.id!!

            // Gather copied data
            val copiedContentVersions = testBuilder.admin.contentVersions.listContentVersions(exhibitionId = copiedExhibitionId, roomId = null)
            assertEquals(1, copiedContentVersions.size)
            val copiedContentVersion = copiedContentVersions.first()

            val copiedGroupContentVersions = testBuilder.admin.groupContentVersions.listGroupContentVersions(exhibitionId = copiedExhibitionId, contentVersionId = null, deviceGroupId = null)
            assertEquals(1, copiedGroupContentVersions.size)
            val copiedGroupContentVersion = copiedGroupContentVersions.first()

            val copiedDevices = testBuilder.admin.exhibitionDevices.listExhibitionDevices(exhibitionId = copiedExhibitionId, exhibitionGroupId = null, deviceModelId = null)
            assertEquals(1, copiedDevices.size)
            val copiedDevice = copiedDevices.first()

            val copiedRooms = testBuilder.admin.exhibitionRooms.listExhibitionRooms(exhibitionId = copiedExhibitionId,floorId = null)
            assertEquals(1, copiedRooms.size)
            val copiedRoom = copiedRooms.first()

            val copiedGroups = testBuilder.admin.exhibitionDeviceGroups.listExhibitionDeviceGroups(exhibitionId = copiedExhibitionId, roomId = null)
            assertEquals(1, copiedGroups.size)
            val copiedGroup = copiedGroups.first()

            val copiedFloors = testBuilder.admin.exhibitionFloors.listExhibitionFloors(exhibitionId = copiedExhibitionId)
            assertEquals(1, copiedFloors.size)
            val copiedFloor = copiedFloors.first()

            val copiedAntennas = testBuilder.admin.rfidAntennas.listRfidAntennas(exhibitionId = copiedExhibitionId, deviceGroupId = null, roomId = null)
            assertEquals(1, copiedAntennas.size)
            val copiedAntenna = copiedAntennas.first()

            val copiedPages = testBuilder.admin.exhibitionPages.listExhibitionPages(exhibitionId = copiedExhibitionId, exhibitionDeviceId = null, contentVersionId = null, pageLayoutId = null)
            assertEquals(2, copiedPages.size)
            val copiedDevicePage = copiedPages.first { it.name == "copy device page" }
            val copiedIdlePage = copiedPages.first { it.name == "copy idle page" }

            // Assert copied floor
            assertNotEquals(copiedFloor.id, sourceFloor.id)
            assertEquals(sourceFloor.name, copiedFloor.name)
            assertEquals(sourceFloor.floorPlanUrl, copiedFloor.floorPlanUrl)
            assertJsonsEqual(sourceFloor.floorPlanBounds, copiedFloor.floorPlanBounds)
            assertEquals(copiedExhibitionId, copiedFloor.exhibitionId)

            // Assert copied room
            assertNotEquals(sourceRoom.id, copiedRoom.id)
            assertEquals(copiedFloor.id, copiedRoom.floorId)
            assertEquals(sourceRoom.name, copiedRoom.name)
            assertEquals(sourceRoom.color, copiedRoom.color)
            assertEquals(sourceRoom.geoShape, copiedRoom.geoShape)
            assertEquals(copiedExhibitionId, copiedRoom.exhibitionId)

            // Assert copied device
            assertNotEquals(sourceDevice.id, copiedDevice.id)
            assertEquals(sourceDevice.name, copiedDevice.name)
            assertEquals(copiedExhibitionId, copiedDevice.exhibitionId)
            assertEquals(copiedGroup.id, copiedDevice.groupId)
            assertEquals(sourceDevice.modelId, copiedDevice.modelId)
            assertEquals(sourceDevice.screenOrientation, copiedDevice.screenOrientation)
            assertEquals(sourceDevice.imageLoadStrategy, copiedDevice.imageLoadStrategy)
            assertEquals(sourceDevice.location, copiedDevice.location)
            assertEquals(copiedIdlePage.id, copiedDevice.idlePageId)

            // Assert copied antenna
            assertNotEquals(sourceAntenna.id, copiedAntenna.id)
            assertEquals(sourceAntenna.name, copiedAntenna.name)
            assertEquals(copiedExhibitionId, copiedAntenna.exhibitionId)
            assertEquals(copiedGroup.id, copiedAntenna.groupId)
            assertEquals(sourceAntenna.antennaNumber, copiedAntenna.antennaNumber)
            assertEquals(sourceAntenna.readerId, copiedAntenna.readerId)
            assertEquals(copiedRoom.id, copiedAntenna.roomId)
            assertJsonsEqual(sourceAntenna.location, copiedAntenna.location)
            assertEquals(sourceAntenna.visitorSessionEndThreshold, copiedAntenna.visitorSessionEndThreshold)
            assertEquals(sourceAntenna.visitorSessionStartThreshold, copiedAntenna.visitorSessionStartThreshold)

            // Assert copied page
            assertNotEquals(sourceDevicePage.id, copiedDevicePage.id)
            assertEquals(sourceDevicePage.name, copiedDevicePage.name)
            assertEquals(sourceDevicePage.layoutId, copiedDevicePage.layoutId)
            assertEquals(copiedExhibitionId, copiedDevicePage.exhibitionId)
            assertEquals(sourceDevicePage.orderNumber, copiedDevicePage.orderNumber)
            assertJsonsEqual(sourceDevicePage.exitTransitions, copiedDevicePage.exitTransitions)
            assertJsonsEqual(sourceDevicePage.enterTransitions, copiedDevicePage.enterTransitions)
            assertJsonsEqual(sourceDevicePage.resources, copiedDevicePage.resources)
            assertJsonsEqual(sourceDevicePage.eventTriggers, copiedDevicePage.eventTriggers)
            assertEquals(copiedContentVersion.id, copiedDevicePage.contentVersionId)
            assertEquals(copiedDevice.id, copiedDevicePage.deviceId)
            assertNotEquals(sourceIdlePage.id, copiedIdlePage.id)

            // Assert copied content version
            assertNotEquals(sourceContentVersion.id, copiedContentVersion.id)
            assertEquals(sourceContentVersion.name, copiedContentVersion.name)
            assertEquals(sourceContentVersion.language, copiedContentVersion.language)
            assertArrayEquals(arrayOf(copiedRoom.id), copiedContentVersion.rooms)
            assertEquals(copiedExhibitionId, copiedContentVersion.exhibitionId)
            assertJsonsEqual(sourceContentVersion.activeCondition, copiedContentVersion.activeCondition)

            // Assert copied group content version
            assertNotEquals(sourceGroupContentVersion.id, copiedGroupContentVersion.id)
            assertEquals(sourceGroupContentVersion.name, copiedGroupContentVersion.name)
            assertEquals(sourceGroupContentVersion.status, copiedGroupContentVersion.status)
            assertEquals(copiedContentVersion.id, copiedGroupContentVersion.contentVersionId)
            assertEquals(copiedGroup.id, copiedGroupContentVersion.deviceGroupId)
            assertEquals(copiedExhibitionId, copiedGroupContentVersion.exhibitionId)

            testBuilder.admin.exhibitionDevices.updateExhibitionDevice(
                exhibitionId = sourceExhibitionId,
                payload = sourceDevice.copy(
                    idlePageId = null
                )
            )

            cleanCopiedExhibition(
                apiTestBuilder = testBuilder,
                copiedExhibition = copiedExhibition
            )
        }
    }

    @Test
    fun testFindExhibition() {
        createTestBuilder().use {
            val nonExistingExhibitionId = UUID.randomUUID()
            it.admin.exhibitions.assertFindFail(404, nonExistingExhibitionId)
            val createdExhibition = it.admin.exhibitions.create()
            val createdExhibitionId = createdExhibition.id!!
            it.admin.exhibitions.assertFindFail(404, nonExistingExhibitionId)
            assertNotNull(it.admin.exhibitions.findExhibition(createdExhibitionId))
        }
    }

    @Test
    fun testListExhibitions() {
        createTestBuilder().use {
            assertEquals(0, it.admin.exhibitions.listExhibitions().size)
            val createdExhibition = it.admin.exhibitions.create()
            val createdExhibitionId = createdExhibition.id!!
            val exhibitions = it.admin.exhibitions.listExhibitions()
            assertEquals(1, exhibitions.size)
            assertEquals(createdExhibitionId, exhibitions[0].id)
            it.admin.exhibitions.delete(createdExhibition)
            assertEquals(0, it.admin.exhibitions.listExhibitions().size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        createTestBuilder().use {
            val createdExhibition = it.admin.exhibitions.create()
            val createdExhibitionId = createdExhibition.id!!

            val foundCreatedExhibition = it.admin.exhibitions.findExhibition(createdExhibitionId)
            assertEquals(createdExhibition.id, foundCreatedExhibition?.id)
            assertEquals(createdExhibition.name, foundCreatedExhibition?.name)

            val updateBody = Exhibition(
                "new name",
                createdExhibition.id,
                createdExhibition.creatorId,
                createdExhibition.lastModifierId,
                createdExhibition.createdAt,
                createdExhibition.modifiedAt
            )

            val updatedExhibition = it.admin.exhibitions.updateExhibition(updateBody)
            assertEquals(updateBody.id, updatedExhibition.id)
            assertEquals(updateBody.name, updatedExhibition.name)

            val foundUpdatedExhibition = it.admin.exhibitions.findExhibition(createdExhibitionId)
            assertEquals(updateBody.id, foundUpdatedExhibition?.id)
            assertEquals(updateBody.name, foundUpdatedExhibition?.name)

            it.admin.exhibitions.assertUpdateFail(
                404,
                Exhibition(
                    "fail name",
                    UUID.randomUUID(),
                    createdExhibition.creatorId,
                    createdExhibition.lastModifierId,
                    createdExhibition.createdAt,
                    createdExhibition.modifiedAt
                )
            )
            it.admin.exhibitions.assertUpdateFail(
                400,
                Exhibition(
                    "",
                    UUID.randomUUID(),
                    createdExhibition.creatorId,
                    createdExhibition.lastModifierId,
                    createdExhibition.createdAt,
                    createdExhibition.modifiedAt
                )
            )
        }
    }

    @Test
    fun testDeleteExhibition() {
        createTestBuilder().use {
            val createdExhibition = it.admin.exhibitions.create()
            val createdExhibitionId = createdExhibition.id!!
            assertNotNull(it.admin.exhibitions.findExhibition(createdExhibitionId))
            it.admin.exhibitions.delete(createdExhibition)
            it.admin.exhibitions.assertFindFail(404, createdExhibitionId)
            it.admin.exhibitions.assertDeleteFail(404, UUID.randomUUID())
        }
    }

    /**
     * Cleans up the test data after copy tests
     *
     * @param apiTestBuilder the TestBuilder
     * @param copiedExhibition the copied exhibition
     */
    private fun cleanCopiedExhibition(apiTestBuilder: TestBuilder, copiedExhibition: Exhibition) {
        val copiedExhibitionId = copiedExhibition.id!!

        val copiedDevices = apiTestBuilder.admin.exhibitionDevices.listExhibitionDevices(exhibitionId = copiedExhibitionId, exhibitionGroupId = null, deviceModelId = null)

        copiedDevices
            .filter { it.idlePageId != null }
            .forEach { targetDevice ->
                apiTestBuilder.admin.exhibitionDevices.updateExhibitionDevice(
                    exhibitionId = targetDevice.exhibitionId!!,
                    payload = targetDevice.copy(idlePageId = null)
                )
            }

        apiTestBuilder.admin.exhibitionPages.listExhibitionPages(
            exhibitionId = copiedExhibitionId,
            exhibitionDeviceId = null,
            contentVersionId = null,
            pageLayoutId = null
        ).forEach { page ->
            apiTestBuilder.admin.exhibitionPages.delete(
                exhibitionId = copiedExhibitionId,
                exhibitionPageId = page.id!!
            )
        }

        val targetGroupContentVersions = apiTestBuilder.admin.groupContentVersions.listGroupContentVersions(
            exhibitionId = copiedExhibitionId,
            contentVersionId = null,
            deviceGroupId = null
        )

        val targetContentVersionIds = targetGroupContentVersions.map { it.contentVersionId }.distinct()

        targetGroupContentVersions.forEach { groupContentVersion ->
            apiTestBuilder.admin.groupContentVersions.delete(
                exhibitionId = copiedExhibitionId,
                groupContentVersion = groupContentVersion
            )
        }

        targetContentVersionIds.forEach { targetContentVersionId ->
            apiTestBuilder.admin.contentVersions.delete(
                exhibitionId = copiedExhibitionId,
                contentVersionId = targetContentVersionId
            )
        }

        apiTestBuilder.admin.rfidAntennas.listRfidAntennas(exhibitionId = copiedExhibitionId, deviceGroupId = null, roomId = null)
            .mapNotNull(RfidAntenna::id)
            .forEach { apiTestBuilder.admin.rfidAntennas.delete(exhibitionId = copiedExhibitionId, rfidAntennaId = it ) }

        copiedDevices
            .mapNotNull(ExhibitionDevice::id)
            .forEach { apiTestBuilder.admin.exhibitionDevices.delete(exhibitionId = copiedExhibitionId, exhibitionDeviceId = it ) }

        apiTestBuilder.admin.visitorVariables.listVisitorVariables(exhibitionId = copiedExhibitionId, name = null)
            .mapNotNull(VisitorVariable::id)
            .forEach { apiTestBuilder.admin.visitorVariables.delete(exhibitionId = copiedExhibitionId, visitorVariableId = it) }

        apiTestBuilder.admin.exhibitionDeviceGroups.listExhibitionDeviceGroups(exhibitionId = copiedExhibitionId, roomId = null)
            .mapNotNull(ExhibitionDeviceGroup::id)
            .forEach { apiTestBuilder.admin.exhibitionDeviceGroups.delete(exhibitionId = copiedExhibitionId, exhibitionDeviceGroupId = it) }

        apiTestBuilder.admin.exhibitionRooms.listExhibitionRooms(exhibitionId = copiedExhibitionId, floorId = null)
            .mapNotNull(ExhibitionRoom::id)
            .forEach { apiTestBuilder.admin.exhibitionRooms.delete(exhibitionId = copiedExhibitionId, exhibitionRoomId = it) }

        apiTestBuilder.admin.exhibitionFloors.listExhibitionFloors(exhibitionId = copiedExhibitionId)
            .mapNotNull(ExhibitionFloor::id)
            .forEach { apiTestBuilder.admin.exhibitionFloors.delete(exhibitionId = copiedExhibitionId, exhibitionFloorId = it) }

        apiTestBuilder.admin.exhibitions.delete(copiedExhibition.id)
    }

}