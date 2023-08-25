package fi.metatavu.noheva.api.test.functional

import fi.metatavu.noheva.api.client.models.*
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
 * Test class for testing exhibition devices API
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class ExhibitionDeviceTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateDevice() {
        createTestBuilder().use {
            val mqttSubscription = it.mqtt.subscribe(MqttDeviceCreate::class.java,"devices/create")
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!

            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!
            val group = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val device = it.admin.devices.create()
            val screenOrientation = ScreenOrientation.PORTRAIT
            val createdDevice = it.admin.exhibitionDevices.create(exhibitionId, ExhibitionDevice(
                groupId = group.id!!,
                deviceId = device.id!!,
                name = "name",
                screenOrientation = screenOrientation,
                imageLoadStrategy = DeviceImageLoadStrategy.DISK
            ))

            assertNotNull(createdDevice)
            assertJsonsEqual(listOf(MqttDeviceCreate(exhibitionId = exhibitionId, id = createdDevice.id!!)), mqttSubscription.getMessages(1))

            it.admin.exhibitionDevices.assertCreateFail(400, exhibitionId, ExhibitionDevice(groupId = UUID.randomUUID(), deviceId = device.id, name = "name", screenOrientation = screenOrientation, imageLoadStrategy = DeviceImageLoadStrategy.MEMORY))
            it.admin.exhibitionDevices.assertCreateFail(400, exhibitionId, ExhibitionDevice(groupId = group.id, deviceId = UUID.randomUUID(), name = "name", screenOrientation = screenOrientation, imageLoadStrategy = DeviceImageLoadStrategy.MEMORY))
        }
   }

    @Test
    fun testFindDevice() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!
            val group = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionDeviceId = UUID.randomUUID()
            val createdExhibitionDevice = it.admin.exhibitionDevices.create(exhibitionId, group.id!!)
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            it.admin.exhibitionDevices.assertFindFail(404, exhibitionId, nonExistingExhibitionDeviceId)
            it.admin.exhibitionDevices.assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionDeviceId)
            it.admin.exhibitionDevices.assertFindFail(404, nonExistingExhibitionId, createdExhibitionDeviceId)
            assertNotNull(it.admin.exhibitionDevices.findExhibitionDevice(exhibitionId, createdExhibitionDeviceId))
        }
    }

    @Test
    fun testListDevices() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val model = it.admin.deviceModels.create()
            val createdDevice = it.admin.devices.create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group1 = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val group2 = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 2"
            )
            it.admin.devices.update(createdDevice.id!!, createdDevice.copy(deviceModelId = model.id!!))
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin.exhibitionDevices.assertListFail(404, nonExistingExhibitionId, null, null)
            assertEquals(0, it.admin.exhibitionDevices.listExhibitionDevices(exhibitionId, null, null).size)

            val createdExhibitionDevice = it.admin.exhibitionDevices.create(exhibitionId, group1.id!!)
            it.admin.exhibitionDevices.updateExhibitionDevice(exhibitionId, createdExhibitionDevice.copy(deviceId = createdDevice.id))
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            it.admin.exhibitionDevices.assertCount(1, exhibitionId, null, null)
            it.admin.exhibitionDevices.assertCount(1, exhibitionId, group1.id, null)
            it.admin.exhibitionDevices.assertCount(0, exhibitionId, group2.id!!, null)

            val exhibitionDevices = it.admin.exhibitionDevices.listExhibitionDevices(exhibitionId, null, null)
            assertEquals(1, exhibitionDevices.size)
            assertEquals(createdExhibitionDeviceId, exhibitionDevices[0].id)

            val listFilteredByDeviceModel = it.admin.exhibitionDevices.listExhibitionDevices(exhibitionId, null,
                model.id
            )
            assertEquals(1, listFilteredByDeviceModel.size)

            it.admin.exhibitionDevices.delete(exhibitionId, createdExhibitionDeviceId)
            assertEquals(0, it.admin.exhibitionDevices.listExhibitionDevices(exhibitionId, null, null).size)

        }
    }

    @Test
    fun testUpdateDevice() {
        createTestBuilder().use {
            val mqttSubscription = it.mqtt.subscribe(MqttDeviceUpdate::class.java,"devices/update")

            val exhibition = it.admin.exhibitions.create()
            val device = it.admin.devices.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!
            val layout = it.admin.pageLayouts.create(it.admin.deviceModels.create())
            val layoutId = layout.id!!

            val contentVersion1 = it.admin.contentVersions.create(exhibitionId)
            val contentVersion2 = it.admin.contentVersions.create(exhibitionId)
            val group1 = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val group2 = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 2"
            )

            it.admin.groupContentVersions.create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = "default",
                    contentVersionId = contentVersion1.id!!,
                    deviceGroupId = group1.id!!,
                    status = GroupContentVersionStatus.INPROGRESS
                )
            )

            it.admin.groupContentVersions.create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = "default",
                    contentVersionId = contentVersion2.id!!,
                    deviceGroupId = group2.id!!,
                    status = GroupContentVersionStatus.INPROGRESS
                )
            )

            val nonExistingGroupId = UUID.randomUUID()
            var screenOrientation = ScreenOrientation.PORTRAIT
            var createdExhibitionDevice = it.admin.exhibitionDevices.create(
                exhibitionId = exhibitionId, payload = ExhibitionDevice(
                    groupId = group1.id,
                    name = "created name",
                    location = Point(-123.0, 234.0),
                    screenOrientation = screenOrientation,
                    idlePageId = null,
                    imageLoadStrategy = DeviceImageLoadStrategy.MEMORY
                )
            )

            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            val idlePage1 = it.admin.exhibitionPages.create(
                exhibitionId = exhibitionId,
                layoutId = layoutId,
                deviceId = createdExhibitionDeviceId,
                contentVersionId = contentVersion1.id
            )

            createdExhibitionDevice = it.admin.exhibitionDevices.updateExhibitionDevice(
                exhibitionId = exhibitionId,
                payload = createdExhibitionDevice.copy(idlePageId = idlePage1.id)
            )

            val idlePage2 = it.admin.exhibitionPages.create(
                exhibitionId = exhibitionId,
                layoutId = layoutId,
                deviceId = createdExhibitionDeviceId,
                contentVersionId = contentVersion1.id
            )
            val foundCreatedExhibitionDevice = it.admin.exhibitionDevices.findExhibitionDevice(exhibitionId, createdExhibitionDeviceId)
            assertEquals(createdExhibitionDevice.id, foundCreatedExhibitionDevice.id)
            assertEquals("created name", createdExhibitionDevice.name)
            assertEquals(-123.0, createdExhibitionDevice.location?.x)
            assertEquals(234.0, createdExhibitionDevice.location?.y)
            assertEquals(ScreenOrientation.PORTRAIT, createdExhibitionDevice.screenOrientation)
            assertEquals(DeviceImageLoadStrategy.MEMORY, createdExhibitionDevice.imageLoadStrategy)
            assertEquals(idlePage1.id, createdExhibitionDevice.idlePageId)
            screenOrientation = ScreenOrientation.LANDSCAPE

            val updatedExhibitionDevice = it.admin.exhibitionDevices.updateExhibitionDevice(exhibitionId, ExhibitionDevice(
                groupId = group2.id,
                deviceId = device.id!!,
                name = "updated name",
                screenOrientation = screenOrientation,
                id = createdExhibitionDeviceId,
                exhibitionId = exhibitionId,
                location = Point(123.2, -234.4),
                idlePageId = idlePage2.id,
                imageLoadStrategy = DeviceImageLoadStrategy.DISK
            ))

            it.admin.exhibitionDevices.updateExhibitionDevice(exhibitionId, updatedExhibitionDevice)

            assertJsonsEqual(
                listOf(
                    MqttDeviceUpdate(exhibitionId = exhibitionId, id = createdExhibitionDeviceId, groupChanged = true),
                    MqttDeviceUpdate(exhibitionId = exhibitionId, id = createdExhibitionDeviceId, groupChanged = false)
                ),
                mqttSubscription.getMessages(1)
            )

            val foundUpdatedExhibitionDevice = it.admin.exhibitionDevices.findExhibitionDevice(exhibitionId, createdExhibitionDeviceId)

            assertEquals(updatedExhibitionDevice.id, foundUpdatedExhibitionDevice.id)
            assertEquals("updated name", updatedExhibitionDevice.name)
            assertEquals(123.2, updatedExhibitionDevice.location?.x)
            assertEquals(-234.4, updatedExhibitionDevice.location?.y)
            assertEquals(device.id, updatedExhibitionDevice.deviceId)
            assertEquals(group2.id, updatedExhibitionDevice.groupId)
            assertEquals(ScreenOrientation.LANDSCAPE, updatedExhibitionDevice.screenOrientation)
            assertEquals(DeviceImageLoadStrategy.DISK, updatedExhibitionDevice.imageLoadStrategy)
            assertEquals(idlePage2.id, updatedExhibitionDevice.idlePageId)

            it.admin.exhibitionDevices.assertUpdateFail(404, nonExistingExhibitionId, ExhibitionDevice(groupId = group1.id, deviceId = device.id, name = "name", screenOrientation = screenOrientation, id = createdExhibitionDeviceId, imageLoadStrategy = DeviceImageLoadStrategy.MEMORY))
            it.admin.exhibitionDevices.assertUpdateFail(400, exhibitionId, ExhibitionDevice(groupId = nonExistingGroupId, deviceId = device.id, name = "updated name", screenOrientation = screenOrientation, id = createdExhibitionDeviceId, imageLoadStrategy = DeviceImageLoadStrategy.MEMORY))
            it.admin.exhibitionDevices.assertUpdateFail(400, exhibitionId, ExhibitionDevice(groupId = group1.id, deviceId = UUID.randomUUID(), name = "updated name", screenOrientation = screenOrientation, id = createdExhibitionDeviceId, imageLoadStrategy = DeviceImageLoadStrategy.MEMORY))
            it.admin.exhibitionDevices.assertUpdateFail(400, exhibitionId, ExhibitionDevice(groupId = group1.id, deviceId = device.id, name = "updated name", screenOrientation = screenOrientation, id = createdExhibitionDeviceId, idlePageId = UUID.randomUUID(), imageLoadStrategy = DeviceImageLoadStrategy.MEMORY))

            it.admin.exhibitionDevices.updateExhibitionDevice(
                exhibitionId = exhibitionId,
                payload = updatedExhibitionDevice.copy(idlePageId = null)
            )
        }
    }

    @Test
    fun testDeleteDevice() {
        createTestBuilder().use {
            val mqttSubscription = it.mqtt.subscribe(MqttDeviceDelete::class.java,"devices/delete")

            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val createdExhibitionDevice = it.admin.exhibitionDevices.create(exhibitionId, group.id!!)
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            assertNotNull(it.admin.exhibitionDevices.findExhibitionDevice(exhibitionId, createdExhibitionDeviceId))
            it.admin.exhibitionDevices.assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin.exhibitionDevices.assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionDeviceId)
            it.admin.exhibitionDevices.assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin.exhibitionDevices.delete(exhibitionId, createdExhibitionDevice)
            assertJsonsEqual(listOf(MqttDeviceDelete(exhibitionId = exhibitionId, id = createdExhibitionDeviceId)), mqttSubscription.getMessages(1))

            it.admin.exhibitionDevices.assertDeleteFail(404, exhibitionId, createdExhibitionDeviceId)
        }
    }

    @Test
    fun testDeleteDeviceWithPages() {
        createTestBuilder().use {
            val exhibition = it.admin.exhibitions.create()
            val exhibitionId = exhibition.id!!
            val layout = it.admin.pageLayouts.create(it.admin.deviceModels.create())
            val contentVersion = it.admin.contentVersions.create(exhibition)
            val contentVersionId = contentVersion.id!!

            val floor = it.admin.exhibitionFloors.create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin.exhibitionRooms.create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!
            val deviceGroup = it.admin.exhibitionDeviceGroups.create(
                exhibitionId = exhibitionId,
                roomId = roomId,
                name = "Group 1"
            )

            val deviceGroupId = deviceGroup.id!!
            val device = createDefaultDevice(it, exhibition, deviceGroup)
            val deviceId = device.id!!

            it.admin.groupContentVersions.create(
                exhibitionId = exhibitionId,
                payload = GroupContentVersion(
                    name = "default",
                    contentVersionId = contentVersionId,
                    deviceGroupId = deviceGroupId,
                    status = GroupContentVersionStatus.INPROGRESS
                )
            )

            val page = it.admin.exhibitionPages.create(
                exhibition = exhibition,
                layout = layout,
                contentVersion = contentVersion,
                device = device
            )

            it.admin.exhibitionDevices.assertDeleteFail(400, exhibitionId, deviceId)
            it.admin.exhibitionPages.delete(exhibitionId = exhibitionId, exhibitionPage = page)
            it.admin.exhibitionDevices.delete(exhibitionId, device)
        }
    }
}
