package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition deviceGroups API
 *
 * @author Antti Leppä
 */
class ExhibitionDeviceGroupTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionDeviceGroup() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttDeviceGroupCreate::class.java, "devicegroups/create")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibition.id,
                sourceDeviceGroupId = null,
                payload = ExhibitionDeviceGroup(
                    name = "name",
                    roomId = roomId,
                    allowVisitorSessionCreation = false,
                    visitorSessionEndTimeout = 5000,
                    visitorSessionStartStrategy = DeviceGroupVisitorSessionStartStrategy.OTHERSBLOCK,
                    indexPageTimeout = 2000
                )
            )

            assertJsonsEqual(
                listOf(
                    MqttDeviceGroupCreate(
                        exhibitionId = exhibitionId,
                        id = createdExhibitionDeviceGroup.id!!
                    )
                ), mqttSubscription.getMessages(1)
            )

            assertNotNull(createdExhibitionDeviceGroup)
            assertEquals(5000, createdExhibitionDeviceGroup.visitorSessionEndTimeout)
            assertEquals(2000L, createdExhibitionDeviceGroup.indexPageTimeout)
        }
    }

    @Test
    fun testFindExhibitionDeviceGroup() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionDeviceGroupId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!

            it.admin().exhibitionDeviceGroups().assertFindFail(404, exhibitionId, nonExistingExhibitionDeviceGroupId)
            it.admin().exhibitionDeviceGroups().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionDeviceGroupId)
            it.admin().exhibitionDeviceGroups().assertFindFail(404, nonExistingExhibitionId, createdExhibitionDeviceGroupId)
            assertNotNull(it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId))
        }
    }

    @Test
    fun testListExhibitionDeviceGroups() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room1 = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId1 = room1.id!!
            val room2 = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId2 = room2.id!!

            it.admin().exhibitionDeviceGroups().assertListFail(expectedStatus = 404, exhibitionId = nonExistingExhibitionId, roomId = roomId1)
            assertEquals(0, it.admin().exhibitionDeviceGroups().listExhibitionDeviceGroups(exhibitionId = exhibitionId, roomId = roomId1).size)

            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibitionId,
                roomId = roomId1,
                name = "Group 1"
            )

            it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibitionId,
                roomId = roomId2,
                name = "Group 2"
            )

            it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibitionId,
                roomId = roomId2,
                name = "Group 3"
            )

            it.admin().exhibitionDeviceGroups().assertCount(1, exhibitionId = exhibitionId, roomId = roomId1)
            it.admin().exhibitionDeviceGroups().assertCount(2, exhibitionId = exhibitionId, roomId = roomId2)
            it.admin().exhibitionDeviceGroups().assertCount(3, exhibitionId = exhibitionId, roomId = null)

            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!
            val exhibitionDeviceGroups = it.admin().exhibitionDeviceGroups().listExhibitionDeviceGroups( exhibitionId = exhibitionId, roomId = roomId1)
            assertEquals(1, exhibitionDeviceGroups.size)
            assertEquals(createdExhibitionDeviceGroupId, exhibitionDeviceGroups[0].id)
            it.admin().exhibitionDeviceGroups().delete(exhibitionId, createdExhibitionDeviceGroupId)
            assertEquals(0, it.admin().exhibitionDeviceGroups().listExhibitionDeviceGroups( exhibitionId = exhibitionId, roomId = roomId1).size)
        }
    }

    @Test
    fun testUpdateExhibitionDeviceGroup() {
      ApiTestBuilder().use {
        val mqttSubscription= it.mqtt().subscribe(MqttDeviceGroupUpdate::class.java,"devicegroups/update")
        val exhibition = it.admin().exhibitions().create()
        val exhibitionId = exhibition.id!!
        val nonExistingExhibitionId = UUID.randomUUID()
        val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
        val floorId = floor.id!!
        val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
        val roomId = room.id!!

        val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(
          exhibitionId = exhibitionId,
          sourceDeviceGroupId = null,
          payload = ExhibitionDeviceGroup(
            name = "created name",
            roomId = roomId,
            allowVisitorSessionCreation = false,
            visitorSessionEndTimeout = 5000,
            visitorSessionStartStrategy = DeviceGroupVisitorSessionStartStrategy.OTHERSBLOCK
          )
        )

        val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id!!

        val foundCreatedExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId)
        assertEquals(createdExhibitionDeviceGroup.id, foundCreatedExhibitionDeviceGroup.id)
        assertEquals("created name", createdExhibitionDeviceGroup.name)
        assertEquals(false, createdExhibitionDeviceGroup.allowVisitorSessionCreation)
        assertEquals(5000, createdExhibitionDeviceGroup.visitorSessionEndTimeout)
        assertEquals(DeviceGroupVisitorSessionStartStrategy.OTHERSBLOCK, createdExhibitionDeviceGroup.visitorSessionStartStrategy)
        assertNull(createdExhibitionDeviceGroup.indexPageTimeout)

        val updatedExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().updateExhibitionDeviceGroup(exhibitionId, ExhibitionDeviceGroup(
            name = "updated name",
            roomId = roomId,
            id = createdExhibitionDeviceGroupId,
            allowVisitorSessionCreation = true,
            visitorSessionEndTimeout = 6000,
            indexPageTimeout = 4000,
            visitorSessionStartStrategy = DeviceGroupVisitorSessionStartStrategy.ENDOTHERS
        ))

        assertJsonsEqual(listOf(MqttDeviceGroupUpdate(exhibitionId = exhibitionId, id = createdExhibitionDeviceGroup.id)), mqttSubscription.getMessages(1))

        val foundUpdatedExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId)

        assertEquals(updatedExhibitionDeviceGroup.id, foundUpdatedExhibitionDeviceGroup.id)
        assertEquals("updated name", updatedExhibitionDeviceGroup.name)
        assertEquals(true, updatedExhibitionDeviceGroup.allowVisitorSessionCreation)
        assertEquals(6000, updatedExhibitionDeviceGroup.visitorSessionEndTimeout)
        assertEquals(DeviceGroupVisitorSessionStartStrategy.ENDOTHERS, updatedExhibitionDeviceGroup.visitorSessionStartStrategy)
        assertEquals(4000L, updatedExhibitionDeviceGroup.indexPageTimeout)

        it.admin().exhibitionDeviceGroups().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionDeviceGroup(
          name = "name",
          id = createdExhibitionDeviceGroupId,
          allowVisitorSessionCreation = false,
          visitorSessionEndTimeout = 5000,
          visitorSessionStartStrategy = DeviceGroupVisitorSessionStartStrategy.ENDOTHERS
        ))
      }
    }

    @Test
    fun testDeleteExhibitionDeviceGroup() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttDeviceGroupDelete::class.java,"devicegroups/delete")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val createdContentVersion = it.admin().contentVersions().create(exhibitionId)
            val createdExhibitionDeviceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val groupContentVersionToCreate = GroupContentVersion(
                name = "Group name",
                status = GroupContentVersionStatus.NOTSTARTED,
                deviceGroupId = createdExhibitionDeviceGroup.id!!,
                contentVersionId = createdContentVersion.id!!
            )

            val createdGroupContentVersion = it.admin().groupContentVersions().create(exhibitionId = exhibitionId, payload = groupContentVersionToCreate)
            val createdExhibitionDeviceGroupId = createdExhibitionDeviceGroup.id

            assertNotNull(it.admin().exhibitionDeviceGroups().findExhibitionDeviceGroup(exhibitionId, createdExhibitionDeviceGroupId))
            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionDeviceGroupId)
            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionDeviceGroups().assertDeleteFail(400, exhibitionId = exhibitionId, id = createdExhibitionDeviceGroupId)

            it.admin().groupContentVersions().delete(exhibitionId = exhibitionId, groupContentVersion = createdGroupContentVersion)

            it.admin().exhibitionDeviceGroups().delete(exhibitionId, createdExhibitionDeviceGroup)
            assertJsonsEqual(listOf(MqttDeviceGroupDelete(exhibitionId = exhibitionId, id = createdExhibitionDeviceGroup.id)), mqttSubscription.getMessages(1))

            it.admin().exhibitionDeviceGroups().assertDeleteFail(404, exhibitionId, createdExhibitionDeviceGroupId)
        }
    }

    @Test
    fun testCopyDeviceGroupWithDevices() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val deviceModel = it.admin().deviceModels().create()

            val sourceDeviceGroup = createDefaultDeviceGroup(testBuilder = it, exhibition = exhibition)

            val exhibitionId = exhibition.id!!
            val sourceGroupId = sourceDeviceGroup.id!!
            val deviceModelId = deviceModel.id!!

            val sourceDevices = (1..3).map { i ->
                it.admin().exhibitionDevices().create(exhibitionId = exhibitionId, payload = ExhibitionDevice(
                    name = "Device $i",
                    modelId = deviceModelId,
                    groupId = sourceGroupId,
                    screenOrientation = ScreenOrientation.LANDSCAPE,
                    imageLoadStrategy = DeviceImageLoadStrategy.MEMORY,
                    location = Point(55.0, 33.0)
                ))
            }

            assertEquals(3, sourceDevices.size)

            val targetDeviceGroup = it.admin().exhibitionDeviceGroups().copy(
                exhibitionId = exhibitionId,
                sourceDeviceGroupId = sourceGroupId
            )

            assertNotNull(targetDeviceGroup)
            assertNotNull(targetDeviceGroup.id)
            assertNotEquals(sourceDeviceGroup.id, targetDeviceGroup.id)
            assertEquals(sourceDeviceGroup.allowVisitorSessionCreation, targetDeviceGroup.allowVisitorSessionCreation)
            assertEquals("${sourceDeviceGroup.name} 2", targetDeviceGroup.name)
            assertEquals(sourceDeviceGroup.visitorSessionEndTimeout, targetDeviceGroup.visitorSessionEndTimeout)
            assertEquals(sourceDeviceGroup.visitorSessionStartStrategy, targetDeviceGroup.visitorSessionStartStrategy)
            assertEquals(sourceDeviceGroup.exhibitionId, targetDeviceGroup.exhibitionId)
            assertEquals(sourceDeviceGroup.roomId, targetDeviceGroup.roomId)

            val targetDeviceGroupId = targetDeviceGroup.id!!

            val targetDevices = it.admin().exhibitionDevices().listExhibitionDevices(
                exhibitionId = exhibitionId,
                exhibitionGroupId = targetDeviceGroupId,
                deviceModelId = null
            )

            assertEquals(3, targetDevices.size)

            val sourceDeviceIds = sourceDevices.mapNotNull(ExhibitionDevice::id)
            val targetDeviceIds = targetDevices.mapNotNull(ExhibitionDevice::id)
            assertTrue(targetDeviceIds.intersect(sourceDeviceIds.toSet()).isEmpty())

            val sampleSourceDevice = sourceDevices.find { targetDevice -> targetDevice.name == "Device 1" }
            val sampleTargetDevice = targetDevices.find { targetDevice -> targetDevice.name == "Device 1" }

            assertNotNull(sampleSourceDevice)
            assertNotNull(sampleTargetDevice)

            assertNotEquals(sampleSourceDevice?.id, sampleTargetDevice?.id)
            assertEquals(sampleSourceDevice?.modelId, sampleTargetDevice?.modelId)
            assertEquals(sampleSourceDevice?.name, sampleTargetDevice?.name)
            assertEquals(sampleSourceDevice?.screenOrientation, sampleTargetDevice?.screenOrientation)
            assertEquals(sampleSourceDevice?.imageLoadStrategy, sampleTargetDevice?.imageLoadStrategy)
            assertEquals(sampleSourceDevice?.exhibitionId, sampleTargetDevice?.exhibitionId)
            assertEquals(sampleSourceDevice?.location?.x, sampleTargetDevice?.location?.x)
            assertEquals(sampleSourceDevice?.location?.y, sampleTargetDevice?.location?.y)

            cleanupCopiedResources(
                apiTestBuilder = it,
                exhibitionId = exhibitionId,
                targetDeviceGroupId = targetDeviceGroupId
            )

        }

    }

    @Test
    fun testCopyDeviceGroupWithAntennas() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val sourceDeviceGroup = createDefaultDeviceGroup(testBuilder = it, exhibition = exhibition)
            val exhibitionId = exhibition.id!!
            val sourceGroupId = sourceDeviceGroup.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val sourceAntennas = (1..3).map { i ->
                it.admin().rfidAntennas().create(
                    exhibitionId = exhibitionId,
                    payload = RfidAntenna(
                        name = "Antenna $i",
                        groupId = sourceGroupId,
                        roomId = roomId,
                        location = Point(-123.0, 234.0),
                        readerId = "id $i",
                        antennaNumber = 15,
                        visitorSessionStartThreshold = 80,
                        visitorSessionEndThreshold = 10
                    ))
            }

            assertEquals(3, sourceAntennas.size)

            val targetDeviceGroup = it.admin().exhibitionDeviceGroups().copy(
                exhibitionId = exhibitionId,
                sourceDeviceGroupId = sourceGroupId
            )

            assertNotNull(targetDeviceGroup)
            assertNotNull(targetDeviceGroup.id)
            val targetDeviceGroupId = targetDeviceGroup.id!!

            val targetAntennas = it.admin().rfidAntennas().listRfidAntennas(
                exhibitionId = exhibitionId,
                deviceGroupId = targetDeviceGroupId,
                roomId = null
            )

            assertEquals(3, targetAntennas.size)

            val sourceAntennaIds = sourceAntennas.mapNotNull(RfidAntenna::id)
            val targetAntennaIds = targetAntennas.mapNotNull(RfidAntenna::id)
            assertTrue(targetAntennaIds.intersect(sourceAntennaIds.toSet()).isEmpty())

            val sampleSourceAntenna = sourceAntennas.find { targetAntenna -> targetAntenna.name == "Antenna 1" }
            val sampleTargetAntenna = targetAntennas.find { targetDevice -> targetDevice.name == "Antenna 1" }

            assertNotNull(sampleSourceAntenna)
            assertNotNull(sampleTargetAntenna)

            assertNotEquals(sampleSourceAntenna?.id, sampleTargetAntenna?.id)
            assertNotEquals(sampleSourceAntenna?.groupId, sampleTargetAntenna?.groupId)
            assertEquals(sampleSourceAntenna?.readerId, sampleTargetAntenna?.readerId)
            assertEquals(sampleSourceAntenna?.antennaNumber, sampleTargetAntenna?.antennaNumber)
            assertEquals(sampleSourceAntenna?.visitorSessionStartThreshold, sampleTargetAntenna?.visitorSessionStartThreshold)
            assertEquals(sampleSourceAntenna?.visitorSessionEndThreshold, sampleTargetAntenna?.visitorSessionEndThreshold)
            assertEquals(sampleSourceAntenna?.name, sampleTargetAntenna?.name)
            assertEquals(sampleSourceAntenna?.roomId, sampleTargetAntenna?.roomId)
            assertEquals(sampleSourceAntenna?.exhibitionId, sampleTargetAntenna?.exhibitionId)
            assertEquals(sampleSourceAntenna?.location?.x, sampleTargetAntenna?.location?.x)
            assertEquals(sampleSourceAntenna?.location?.y, sampleTargetAntenna?.location?.y)

            cleanupCopiedResources(
                apiTestBuilder = it,
                exhibitionId = exhibitionId,
                targetDeviceGroupId = targetDeviceGroupId
            )

        }

    }

    @Test
    fun testCopyDeviceGroupWithContentVersions() {
        ApiTestBuilder().use {
            val languages = listOf("FI", "SV", "EN")

            val exhibition = it.admin().exhibitions().create()
            val room = createDefaultRoom(testBuilder = it, exhibition = exhibition)

            val sourceDeviceGroup = createDefaultDeviceGroup(testBuilder = it, exhibition = exhibition)

            val exhibitionId = exhibition.id!!
            val sourceGroupId = sourceDeviceGroup.id!!
            val roomId = room.id!!

            val sourceContentVersions = (1..3).map { i ->
                languages.map { language ->
                    it.admin().contentVersions().create(exhibitionId, ContentVersion(
                        name = "$i at $language",
                        language = language,
                        activeCondition = ContentVersionActiveCondition(
                            userVariable = "var $i",
                            equals = "val $i"
                        ),
                        rooms = arrayOf(roomId))
                    )
                }
            }

            assertEquals(9, sourceContentVersions.flatten().size)

            val sourceGroupContentVersions = sourceContentVersions.flatMap { contentVersions ->
                contentVersions.map { contentVersion ->
                    it.admin().groupContentVersions().create(
                        exhibitionId = exhibitionId,
                        payload = GroupContentVersion(
                            name = contentVersion.name,
                            status = GroupContentVersionStatus.NOTSTARTED,
                            deviceGroupId = sourceGroupId,
                            contentVersionId = contentVersion.id!!
                        )
                    )
                }
            }

            assertEquals(9, sourceGroupContentVersions.size)

            val targetDeviceGroup = it.admin().exhibitionDeviceGroups().copy(
                exhibitionId = exhibitionId,
                sourceDeviceGroupId = sourceGroupId
            )

            assertNotNull(targetDeviceGroup)

            val targetDeviceGroupId = targetDeviceGroup.id!!

            // Check copied group content versions

            val targetGroupContentVersions = it.admin().groupContentVersions().listGroupContentVersions(
                exhibitionId = exhibitionId,
                contentVersionId = null,
                deviceGroupId = targetDeviceGroupId
            )

            assertEquals(sourceGroupContentVersions.size, targetGroupContentVersions.size)

            val sourceGroupContentVersionIds = sourceGroupContentVersions.mapNotNull(GroupContentVersion::id)
            val targetGroupContentVersionIds = targetGroupContentVersions.mapNotNull(GroupContentVersion::id)
            assertTrue(targetGroupContentVersionIds.intersect(sourceGroupContentVersionIds.toSet()).isEmpty())

            val sampleSourceGroupContentVersion = sourceGroupContentVersions.find { groupContentVersion -> groupContentVersion.name == "1 at FI" }
            val sampleTargetGroupContentVersion = targetGroupContentVersions.find { groupContentVersion -> groupContentVersion.name == "1 at FI" }

            assertNotEquals(sampleSourceGroupContentVersion?.id, sampleTargetGroupContentVersion?.id)
            assertEquals(sampleSourceGroupContentVersion?.name, sampleTargetGroupContentVersion?.name)
            assertEquals(sampleSourceGroupContentVersion?.status, sampleTargetGroupContentVersion?.status)
            assertNotEquals(sampleSourceGroupContentVersion?.contentVersionId, sampleTargetGroupContentVersion?.contentVersionId)
            assertNotEquals(sampleSourceGroupContentVersion?.deviceGroupId, sampleTargetGroupContentVersion?.deviceGroupId)
            assertEquals(sampleSourceGroupContentVersion?.exhibitionId, sampleTargetGroupContentVersion?.exhibitionId)

            // Check copied content versions

            val targetContentVersions = getCopiedContentVersions(it, sourceContentVersions.flatten(), exhibitionId)
            assertEquals(sourceContentVersions.flatten().size, targetContentVersions.size)

            val sampleSourceContentVersion = sourceContentVersions.flatten().find { contentVersion -> contentVersion.name == "1 at FI" }
            assertNotNull(sampleSourceContentVersion)
            val sampleTargetContentVersion = targetContentVersions.find { contentVersion -> contentVersion.name == "${sampleSourceContentVersion?.name} 2" }
            assertNotNull(sampleTargetContentVersion)

            assertNotEquals(sampleSourceContentVersion?.id, sampleTargetContentVersion?.id)
            assertEquals("${sampleSourceContentVersion?.name} 2", sampleTargetContentVersion?.name)
            assertEquals(sampleSourceContentVersion?.language, sampleTargetContentVersion?.language)
            assertArrayEquals(sampleSourceContentVersion?.rooms, sampleTargetContentVersion?.rooms)
            assertEquals(sampleSourceContentVersion?.exhibitionId, sampleTargetContentVersion?.exhibitionId)
            assertJsonsEqual(sampleSourceContentVersion?.activeCondition, sampleTargetContentVersion?.activeCondition)

            cleanupCopiedResources(
                apiTestBuilder = it,
                exhibitionId = exhibitionId,
                targetDeviceGroupId = targetDeviceGroupId
            )

        }

    }

    @Test
    fun testCopyDeviceGroupWithPages() {
        ApiTestBuilder().use {
            val languages = listOf("FI", "SV")

            val exhibition = it.admin().exhibitions().create()
            val deviceModel = it.admin().deviceModels().create()
            val room = createDefaultRoom(testBuilder = it, exhibition = exhibition)
            val pageLayout = it.admin().pageLayouts().create(deviceModel)

            val sourceDeviceGroup = createDefaultDeviceGroup(testBuilder = it, exhibition = exhibition)

            val exhibitionId = exhibition.id!!
            val sourceGroupId = sourceDeviceGroup.id!!
            val roomId = room.id!!
            val deviceModelId = deviceModel.id!!
            val pageLayoutId = pageLayout.id!!

            val sourceDevices = (1..2).map { i ->
                it.admin().exhibitionDevices().create(exhibitionId = exhibitionId, payload = ExhibitionDevice(
                    name = "Device $i",
                    modelId = deviceModelId,
                    groupId = sourceGroupId,
                    screenOrientation = ScreenOrientation.LANDSCAPE,
                    location = Point(55.0, 33.0),
                    imageLoadStrategy = DeviceImageLoadStrategy.MEMORY
                ))
            }

            val sourceContentVersions = languages.map { language ->
                it.admin().contentVersions().create(exhibitionId, ContentVersion(name = language, language = language, rooms = arrayOf(roomId)))
            }

            sourceContentVersions.map { contentVersion ->
                it.admin().groupContentVersions().create(
                    exhibitionId = exhibitionId,
                    payload = GroupContentVersion(
                        name = contentVersion.name,
                        status = GroupContentVersionStatus.NOTSTARTED,
                        deviceGroupId = sourceGroupId,
                        contentVersionId = contentVersion.id!!
                    )
                )
            }

            val sourcePages = languages.flatMap { language ->
                val contentVersion = sourceContentVersions.find { contentVersion -> contentVersion.language == language }
                sourceDevices.flatMap { sourceDevice ->
                    (0..1).map { orderNumber ->
                        it.admin().exhibitionPages().create(
                            exhibitionId = exhibitionId,
                            payload = ExhibitionPage(
                                layoutId = pageLayoutId,
                                deviceId = sourceDevice.id!!,
                                name = "create page",
                                orderNumber = orderNumber,
                                resources = arrayOf(ExhibitionPageResource(
                                    id = "createresid",
                                    data = "https://example.com/IMAGE.png",
                                    type = ExhibitionPageResourceType.IMAGE
                                )),
                                eventTriggers = arrayOf(),
                                contentVersionId = contentVersion?.id!!,
                                enterTransitions = arrayOf(ExhibitionPageTransition(
                                    transition = Transition(
                                        duration = 300,
                                        animation = Animation.FADE,
                                        timeInterpolation = AnimationTimeInterpolation.ACCELERATE
                                    )
                                )),
                                exitTransitions = arrayOf(ExhibitionPageTransition(
                                    transition = Transition(
                                        duration = 500,
                                        animation = Animation.MORPH,
                                        timeInterpolation = AnimationTimeInterpolation.DECELERATE
                                    )
                                ))
                            )
                        )
                   }
                }
            }

            assertEquals(8, sourcePages.size)

            val targetDeviceGroup = it.admin().exhibitionDeviceGroups().copy(
                exhibitionId = exhibitionId,
                sourceDeviceGroupId = sourceGroupId
            )

            assertNotNull(targetDeviceGroup)

            val targetDeviceGroupId = targetDeviceGroup.id!!
            val targetContentVersions = getCopiedContentVersions(it, sourceContentVersions, exhibitionId)
            assertEquals(2, targetContentVersions.size)

            val targetDevices = it.admin().exhibitionDevices().listExhibitionDevices(
                exhibitionId = exhibitionId,
                exhibitionGroupId = targetDeviceGroupId,
                deviceModelId = null
            )

            assertEquals(sourceDevices.size, targetDevices.size)

            targetDevices.forEach { targetDevice ->
                val sourceDevice = sourceDevices.find { sourceDevice -> sourceDevice.name == targetDevice.name }

                it.admin().exhibitionPages().assertCount(
                    expected = 4,
                    exhibitionId = exhibitionId,
                    exhibitionDeviceId = targetDevice.id!!,
                    contentVersionId = null,
                    pageLayoutId = null
                )

                targetContentVersions.forEach { targetContentVersion ->
                    val sourceContentVersion = sourceContentVersions.find { sourceContentVersion -> sourceContentVersion.language == targetContentVersion.language }

                    val sourceContentVersionPages = it.admin().exhibitionPages().listExhibitionPages(
                        exhibitionId = exhibitionId,
                        exhibitionDeviceId = sourceDevice?.id!!,
                        contentVersionId = sourceContentVersion?.id!!,
                        pageLayoutId = null
                    )

                    assertEquals(2, sourceContentVersionPages.size)

                    val targetContentVersionPages = it.admin().exhibitionPages().listExhibitionPages(
                        exhibitionId = exhibitionId,
                        exhibitionDeviceId = targetDevice.id,
                        contentVersionId = targetContentVersion.id!!,
                        pageLayoutId = null
                    )

                    assertEquals(sourceContentVersionPages.size, targetContentVersionPages.size)

                    sourceContentVersionPages.sortBy(ExhibitionPage::orderNumber)
                    targetContentVersionPages.sortBy(ExhibitionPage::orderNumber)

                    targetContentVersionPages.indices.forEach { i ->
                        val sourceContentVersionPage = sourceContentVersionPages[i]
                        val targetContentVersionPage = targetContentVersionPages[i]

                        assertNotEquals(sourceContentVersionPage.id, targetContentVersionPage.id)
                        assertEquals(sourceContentVersionPage.name, targetContentVersionPage.name)
                        assertEquals(targetDevice.id, targetContentVersionPage.deviceId)
                        assertEquals(sourceContentVersionPage.layoutId, targetContentVersionPage.layoutId)
                        assertNotEquals(sourceContentVersionPage.contentVersionId, targetContentVersionPage.contentVersionId)
                        assertJsonsEqual(sourceContentVersionPage.resources, targetContentVersionPage.resources)
                        assertJsonsEqual(sourceContentVersionPage.eventTriggers, targetContentVersionPage.eventTriggers)
                        assertJsonsEqual(sourceContentVersionPage.enterTransitions, targetContentVersionPage.enterTransitions)
                        assertJsonsEqual(sourceContentVersionPage.exitTransitions, targetContentVersionPage.exitTransitions)
                        assertEquals(sourceContentVersionPage.orderNumber, targetContentVersionPage.orderNumber)
                        assertEquals(sourceContentVersionPage.exhibitionId, targetContentVersionPage.exhibitionId)
                    }

                }
            }

            cleanupCopiedResources(
                apiTestBuilder = it,
                exhibitionId = exhibitionId,
                targetDeviceGroupId = targetDeviceGroupId
            )
        }

    }

    @Test
    fun testCopyDeviceGroupWithIdlePage() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val deviceModel = it.admin().deviceModels().create()
            val room = createDefaultRoom(testBuilder = it, exhibition = exhibition)
            val pageLayout = it.admin().pageLayouts().create(deviceModel)

            val sourceDeviceGroup = createDefaultDeviceGroup(testBuilder = it, exhibition = exhibition)

            val exhibitionId = exhibition.id!!
            val sourceGroupId = sourceDeviceGroup.id!!
            val roomId = room.id!!
            val deviceModelId = deviceModel.id!!
            val pageLayoutId = pageLayout.id!!

            var sourceDevice = it.admin().exhibitionDevices().create(exhibitionId = exhibitionId, payload = ExhibitionDevice(
                name = "Device",
                modelId = deviceModelId,
                groupId = sourceGroupId,
                screenOrientation = ScreenOrientation.LANDSCAPE,
                imageLoadStrategy = DeviceImageLoadStrategy.MEMORY,
                location = Point(55.0, 33.0)
            ))

            val sourceContentVersion = it.admin().contentVersions().create(exhibitionId, ContentVersion(name = "FI", language = "FI", rooms = arrayOf(roomId)))

            it.admin().groupContentVersions().create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = sourceContentVersion.name,
                    status = GroupContentVersionStatus.NOTSTARTED,
                    deviceGroupId = sourceGroupId,
                    contentVersionId = sourceContentVersion.id!!
                )
            )

            val sourceIdlePage = it.admin().exhibitionPages().create(
                exhibitionId = exhibitionId,
                payload = ExhibitionPage(
                    layoutId = pageLayoutId,
                    deviceId = sourceDevice.id!!,
                    name = "idle page",
                    orderNumber = 0,
                    resources = arrayOf(),
                    eventTriggers = arrayOf(),
                    contentVersionId = sourceContentVersion.id,
                    enterTransitions = arrayOf(),
                    exitTransitions = arrayOf()
                )
            )

            sourceDevice = it.admin().exhibitionDevices().updateExhibitionDevice(
                exhibitionId = exhibitionId,
                payload = sourceDevice.copy(
                    idlePageId = sourceIdlePage.id!!
                )
            )

            val targetDeviceGroup = it.admin().exhibitionDeviceGroups().copy(
                exhibitionId = exhibitionId,
                sourceDeviceGroupId = sourceGroupId
            )

            assertNotNull(targetDeviceGroup)

            val targetDeviceGroupId = targetDeviceGroup.id!!

            val targetDevices = it.admin().exhibitionDevices().listExhibitionDevices(
                exhibitionId = exhibitionId,
                exhibitionGroupId = targetDeviceGroupId,
                deviceModelId = null
            )

            assertEquals(1, targetDevices.size)

            val targetDevice = targetDevices[0]

            assertNotNull(targetDevice.idlePageId)
            assertNotNull(sourceDevice.idlePageId)
            assertNotEquals(sourceDevice.idlePageId, targetDevice.idlePageId)

            it.admin().exhibitionDevices().updateExhibitionDevice(
                exhibitionId = exhibitionId,
                payload = sourceDevice.copy(idlePageId = null)
            )

            cleanupCopiedResources(
                apiTestBuilder = it,
                exhibitionId = exhibitionId,
                targetDeviceGroupId = targetDeviceGroupId
            )
        }

    }

    @Test
    fun testCopyDeviceGroupWithNAVIGATEAction() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val deviceModel = it.admin().deviceModels().create()
            val room = createDefaultRoom(testBuilder = it, exhibition = exhibition)
            val pageLayout = it.admin().pageLayouts().create(deviceModel)

            val sourceDeviceGroup = createDefaultDeviceGroup(testBuilder = it, exhibition = exhibition)

            val exhibitionId = exhibition.id!!
            val sourceGroupId = sourceDeviceGroup.id!!
            val roomId = room.id!!
            val deviceModelId = deviceModel.id!!
            val pageLayoutId = pageLayout.id!!

            val sourceDevice = it.admin().exhibitionDevices().create(exhibitionId = exhibitionId, payload = ExhibitionDevice(
                name = "Device",
                modelId = deviceModelId,
                groupId = sourceGroupId,
                screenOrientation = ScreenOrientation.LANDSCAPE,
                imageLoadStrategy = DeviceImageLoadStrategy.MEMORY,
                location = Point(55.0, 33.0)
            ))

            val sourceContentVersion = it.admin().contentVersions().create(exhibitionId, ContentVersion(name = "FI", language = "FI", rooms = arrayOf(roomId)))

            it.admin().groupContentVersions().create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = sourceContentVersion.name,
                    status = GroupContentVersionStatus.NOTSTARTED,
                    deviceGroupId = sourceGroupId,
                    contentVersionId = sourceContentVersion.id!!
                )
            )

            val sourceToPage = it.admin().exhibitionPages().create(
                exhibitionId = exhibitionId,
                payload = ExhibitionPage(
                    layoutId = pageLayoutId,
                    deviceId = sourceDevice.id!!,
                    name = "to page",
                    orderNumber = 1,
                    resources = arrayOf(),
                    eventTriggers = arrayOf(),
                    contentVersionId = sourceContentVersion.id,
                    enterTransitions = arrayOf(),
                    exitTransitions = arrayOf()
                )
            )

            val sourceFromPage = it.admin().exhibitionPages().create(
                exhibitionId = exhibitionId,
                payload = ExhibitionPage(
                    layoutId = pageLayoutId,
                    deviceId = sourceDevice.id,
                    name = "from page",
                    orderNumber = 0,
                    resources = arrayOf(),
                    eventTriggers = arrayOf(ExhibitionPageEventTrigger(
                        events = arrayOf(ExhibitionPageEvent(
                            action = ExhibitionPageEventActionType.NAVIGATE,
                            properties = arrayOf(
                                ExhibitionPageEventProperty(
                                    name = "pageId",
                                    type = ExhibitionPageEventPropertyType.STRING,
                                    value = sourceToPage.id.toString()
                                )
                            )
                        )),
                        clickViewId =  "createviewid",
                        delay = 0,
                        next = arrayOf(),
                        id = UUID.randomUUID(),
                        name = "Create View"
                    )),
                    contentVersionId = sourceContentVersion.id,
                    enterTransitions = arrayOf(),
                    exitTransitions = arrayOf()
                )
            )

            val targetDeviceGroup = it.admin().exhibitionDeviceGroups().copy(
                exhibitionId = exhibitionId,
                sourceDeviceGroupId = sourceGroupId
            )

            assertNotNull(targetDeviceGroup)

            val targetDeviceGroupId = targetDeviceGroup.id!!

            val targetDevices = it.admin().exhibitionDevices().listExhibitionDevices(
                exhibitionId = exhibitionId,
                exhibitionGroupId = targetDeviceGroupId,
                deviceModelId = null
            )

            assertEquals(1, targetDevices.size)

            val targetDeviceId = targetDevices[0].id!!

            val targetPages = it.admin().exhibitionPages().listExhibitionPages(
                exhibitionId = exhibitionId,
                exhibitionDeviceId = targetDeviceId,
                contentVersionId = null,
                pageLayoutId = null
            )

            assertEquals(2, targetPages.size)

            targetPages.sortBy(ExhibitionPage::orderNumber)

            assertNotEquals(sourceFromPage.id, targetPages[0].id)
            assertEquals(sourceFromPage.name, targetPages[0].name)
            assertNotEquals(sourceToPage.id, targetPages[1].id)
            assertEquals(sourceToPage.name, targetPages[1].name)

            assertEquals(1, targetPages[0].eventTriggers.size)
            assertEquals(1, targetPages[0].eventTriggers[0].events?.size)
            assertEquals(ExhibitionPageEventActionType.NAVIGATE, targetPages[0].eventTriggers[0].events?.get(0)?.action)
            assertEquals(1, targetPages[0].eventTriggers[0].events?.get(0)?.properties?.size)
            assertEquals("pageId", targetPages[0].eventTriggers[0].events?.get(0)?.properties?.get(0)?.name)
            assertEquals(ExhibitionPageEventPropertyType.STRING, targetPages[0].eventTriggers[0].events?.get(0)?.properties?.get(0)?.type)
            assertEquals(targetPages[1].id.toString(), targetPages[0].eventTriggers[0].events?.get(0)?.properties?.get(0)?.value)

            cleanupCopiedResources(
                apiTestBuilder = it,
                exhibitionId = exhibitionId,
                targetDeviceGroupId = targetDeviceGroupId
            )
        }

    }

    @Test
    fun testCopyDeviceGroupWithoutIndexPageTimeout() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val sourceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibition.id,
                sourceDeviceGroupId = null,
                payload = ExhibitionDeviceGroup(
                    name = "name",
                    roomId = roomId,
                    allowVisitorSessionCreation = false,
                    visitorSessionEndTimeout = 5000,
                    visitorSessionStartStrategy = DeviceGroupVisitorSessionStartStrategy.OTHERSBLOCK,
                    indexPageTimeout = null
                )
            )

            val targetDeviceGroup = it.admin().exhibitionDeviceGroups().copy(
                exhibitionId = exhibitionId,
                sourceDeviceGroupId = sourceGroup.id!!
            )

            assertEquals(sourceGroup.indexPageTimeout, targetDeviceGroup.indexPageTimeout)

            cleanupCopiedResources(
                apiTestBuilder = it,
                exhibitionId = exhibitionId,
                targetDeviceGroupId = targetDeviceGroup.id!!
            )
        }
    }

    @Test
    fun testCopyDeviceGroupWithIndexPageTimeout() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val sourceGroup = it.admin().exhibitionDeviceGroups().create(
                exhibitionId = exhibition.id,
                sourceDeviceGroupId = null,
                payload = ExhibitionDeviceGroup(
                    name = "name",
                    roomId = roomId,
                    allowVisitorSessionCreation = false,
                    visitorSessionEndTimeout = 5000,
                    visitorSessionStartStrategy = DeviceGroupVisitorSessionStartStrategy.OTHERSBLOCK,
                    indexPageTimeout = 3000
                )
            )

            val targetDeviceGroup = it.admin().exhibitionDeviceGroups().copy(
                exhibitionId = exhibitionId,
                sourceDeviceGroupId = sourceGroup.id!!
            )

            assertEquals(sourceGroup.indexPageTimeout, targetDeviceGroup.indexPageTimeout)

            cleanupCopiedResources(
                apiTestBuilder = it,
                exhibitionId = exhibitionId,
                targetDeviceGroupId = targetDeviceGroup.id!!
            )
        }
    }

    /**
     * Cleans up after copy test
     *
     * @param apiTestBuilder API test builder
     * @param exhibitionId exhibition id
     * @param targetDeviceGroupId copy target device group id
     */
    private fun cleanupCopiedResources(apiTestBuilder: ApiTestBuilder, exhibitionId: UUID, targetDeviceGroupId: UUID) {
        val targetDevices = apiTestBuilder.admin().exhibitionDevices().listExhibitionDevices(
            exhibitionId = exhibitionId,
            exhibitionGroupId = targetDeviceGroupId,
            deviceModelId = null
        )

        targetDevices
            .filter { it.idlePageId != null }
            .forEach { targetDevice ->
                apiTestBuilder.admin().exhibitionDevices().updateExhibitionDevice(
                    exhibitionId = exhibitionId,
                    payload = targetDevice.copy(idlePageId = null)
                )
            }

        val targetAntennas = apiTestBuilder.admin().rfidAntennas().listRfidAntennas(
            exhibitionId = exhibitionId,
            deviceGroupId = targetDeviceGroupId,
            roomId = null
        )

        targetAntennas.forEach { targetAntenna ->
            apiTestBuilder.admin().rfidAntennas().delete(
                exhibitionId = exhibitionId,
                rfidAntennaId = targetAntenna.id!!
            )
        }

        targetDevices.forEach { targetDevice ->
            apiTestBuilder.admin().exhibitionPages().listExhibitionPages(
                exhibitionId = exhibitionId,
                contentVersionId = null,
                exhibitionDeviceId = targetDevice.id!!,
                pageLayoutId = null
            ).forEach { page ->
                apiTestBuilder.admin().exhibitionPages().delete(exhibitionId = exhibitionId, exhibitionPage = page)
            }

            apiTestBuilder.admin().exhibitionDevices().delete(
                exhibitionId = exhibitionId,
                exhibitionDevice = targetDevice
            )
        }


        val targetGroupContentVersions = apiTestBuilder.admin().groupContentVersions().listGroupContentVersions(
            exhibitionId = exhibitionId,
            contentVersionId = null,
            deviceGroupId = targetDeviceGroupId
        )

        val targetContentVersionIds = targetGroupContentVersions.map { it.contentVersionId }.distinct()

        targetGroupContentVersions.forEach { groupContentVersion ->
            apiTestBuilder.admin().groupContentVersions().delete(
                exhibitionId = exhibitionId,
                groupContentVersion = groupContentVersion
            )
        }

        targetContentVersionIds.forEach { targetContentVersionId ->
            apiTestBuilder.admin().contentVersions().delete(
                exhibitionId = exhibitionId,
                contentVersionId = targetContentVersionId
            )
        }
    }

    /**
     * Returns copied content versions
     *
     * @param apiTestBuilder API test builder
     * @param sourceContentVersions source content versions
     * @param exhibitionId exhibition id
     * @return copied content versions
     */
    private fun getCopiedContentVersions(
        apiTestBuilder: ApiTestBuilder,
        sourceContentVersions: List<ContentVersion>,
        exhibitionId: UUID
    ): List<ContentVersion> {
        val sourceContentVersionIds = sourceContentVersions.mapNotNull(ContentVersion::id)
        return apiTestBuilder.admin().contentVersions().listContentVersions(
            exhibitionId = exhibitionId,
            roomId = null
        ).filter { contentVersion -> !sourceContentVersionIds.contains(contentVersion.id) }
    }

}