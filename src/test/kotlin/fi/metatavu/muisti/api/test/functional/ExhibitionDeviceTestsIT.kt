package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition devices API
 *
 * @author Antti Leppä
 */
class ExhibitionDeviceTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionDevice() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttDeviceCreate::class.java,"devices/create")
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!

            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!
            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)

            val model = it.admin().deviceModels().create()
            val screenOrientation = ScreenOrientation.portrait
            val createdDevice = it.admin().exhibitionDevices().create(exhibitionId, ExhibitionDevice( groupId = group.id!!, modelId = model.id!!, name = "name", screenOrientation = screenOrientation))
            assertNotNull(createdDevice)
            assertJsonsEqual(listOf(MqttDeviceCreate(exhibitionId = exhibitionId, id = createdDevice.id!!)), mqttSubscription.getMessages(1))

            it.admin().exhibitionDevices().assertCreateFail(400, exhibitionId, ExhibitionDevice( groupId = UUID.randomUUID(), modelId = model.id!!, name = "name", screenOrientation = screenOrientation ))
            it.admin().exhibitionDevices().assertCreateFail(400, exhibitionId, ExhibitionDevice( groupId = group.id!!, modelId = UUID.randomUUID(), name = "name", screenOrientation = screenOrientation ))
        }
   }

    @Test
    fun testExhibitionDeviceIndexPage() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val group = createDefaultDeviceGroup(it, exhibition)
            val model = it.admin().deviceModels().create()
            val page = createDefaultPage(it, exhibition)

            val deviceWithIndex = it.admin().exhibitionDevices().create(exhibitionId = exhibition.id!!, payload = ExhibitionDevice(
                name = "with index",
                screenOrientation = ScreenOrientation.landscape,
                groupId = group.id!!,
                modelId = model.id!!,
                indexPageId = page.id!!
            ))

            val deviceWithoutIndex = it.admin().exhibitionDevices().create(exhibitionId = exhibition.id!!, payload = ExhibitionDevice(
                name = "with index",
                screenOrientation = ScreenOrientation.landscape,
                groupId = group.id!!,
                modelId = model.id!!
            ))

            assertEquals(page.id, deviceWithIndex.indexPageId)
            assertNull(deviceWithoutIndex.indexPageId)

            val updatedDevice = it.admin().exhibitionDevices().updateExhibitionDevice(exhibitionId = exhibition.id!!, payload = deviceWithoutIndex.copy(indexPageId = page.id!!))
            assertEquals(updatedDevice?.indexPageId, page.id)
            assertEquals(it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId = exhibition.id!!, exhibitionDeviceId = updatedDevice?.id!!)?.indexPageId, page.id)

            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId = exhibition.id!!, payload = deviceWithoutIndex.copy(indexPageId = UUID.randomUUID()))
        }
    }

    @Test
    fun testFindExhibitionDevice() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!
            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val model = it.admin().deviceModels().create()
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionDeviceId = UUID.randomUUID()
            val createdExhibitionDevice = it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!)
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            it.admin().exhibitionDevices().assertFindFail(404, exhibitionId, nonExistingExhibitionDeviceId)
            it.admin().exhibitionDevices().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionDeviceId)
            it.admin().exhibitionDevices().assertFindFail(404, nonExistingExhibitionId, createdExhibitionDeviceId)
            assertNotNull(it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId, createdExhibitionDeviceId))
        }
    }

    @Test
    fun testListExhibitionDevices() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group1 = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val group2 = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val model = it.admin().deviceModels().create()
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().exhibitionDevices().assertListFail(404, nonExistingExhibitionId, null)
            assertEquals(0, it.admin().exhibitionDevices().listExhibitionDevices(exhibitionId, null).size)

            val createdExhibitionDevice = it.admin().exhibitionDevices().create(exhibitionId, group1.id!!, model.id!!)
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            it.admin().exhibitionDevices().assertCount(1, exhibitionId, null)
            it.admin().exhibitionDevices().assertCount(1, exhibitionId, group1.id!!)
            it.admin().exhibitionDevices().assertCount(0, exhibitionId, group2.id!!)

            val exhibitionDevices = it.admin().exhibitionDevices().listExhibitionDevices(exhibitionId, null)
            assertEquals(1, exhibitionDevices.size)
            assertEquals(createdExhibitionDeviceId, exhibitionDevices[0].id)
            it.admin().exhibitionDevices().delete(exhibitionId, createdExhibitionDeviceId)
            assertEquals(0, it.admin().exhibitionDevices().listExhibitionDevices(exhibitionId, null).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttDeviceUpdate::class.java,"devices/update")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group1 = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val group2 = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)

            val model1 = it.admin().deviceModels().create()
            val model2 = it.admin().deviceModels().create()
            val nonExistingGroupId = UUID.randomUUID()
            val nonExistingModelId = UUID.randomUUID()
            var screenOrientation = ScreenOrientation.portrait

            val createdExhibitionDevice = it.admin().exhibitionDevices().create(exhibitionId, ExhibitionDevice( groupId = group1.id!!, modelId = model1.id!!, name = "created name", location = Point(-123.0, 234.0), screenOrientation = screenOrientation))
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            val foundCreatedExhibitionDevice = it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId, createdExhibitionDeviceId)
            assertEquals(createdExhibitionDevice.id, foundCreatedExhibitionDevice?.id)
            assertEquals("created name", createdExhibitionDevice.name)
            assertEquals(-123.0, createdExhibitionDevice.location?.x)
            assertEquals(234.0, createdExhibitionDevice.location?.y)
            assertEquals(ScreenOrientation.portrait, createdExhibitionDevice.screenOrientation)
            assertEquals(model1.id, createdExhibitionDevice.modelId)
            screenOrientation = ScreenOrientation.landscape

            val updatedExhibitionDevice = it.admin().exhibitionDevices().updateExhibitionDevice(exhibitionId, ExhibitionDevice(
                groupId = group2.id!!,
                modelId = model2.id!!,
                name = "updated name",
                screenOrientation = screenOrientation,
                id = createdExhibitionDeviceId,
                exhibitionId = exhibitionId,
                location = Point(123.2, -234.4)
            ))

            it.admin().exhibitionDevices().updateExhibitionDevice(exhibitionId, updatedExhibitionDevice!!)

            assertJsonsEqual(
                listOf(
                    MqttDeviceUpdate(exhibitionId = exhibitionId, id = createdExhibitionDeviceId, groupChanged = true),
                    MqttDeviceUpdate(exhibitionId = exhibitionId, id = createdExhibitionDeviceId, groupChanged = false)
                ),
                mqttSubscription.getMessages(1)
            )

            val foundUpdatedExhibitionDevice = it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId, createdExhibitionDeviceId)

            assertEquals(updatedExhibitionDevice.id, foundUpdatedExhibitionDevice?.id)
            assertEquals("updated name", updatedExhibitionDevice.name)
            assertEquals(123.2, updatedExhibitionDevice.location?.x)
            assertEquals(-234.4, updatedExhibitionDevice.location?.y)
            assertEquals(model2.id, updatedExhibitionDevice.modelId)
            assertEquals(group2.id, updatedExhibitionDevice.groupId)
            assertEquals(ScreenOrientation.landscape, updatedExhibitionDevice.screenOrientation)

            it.admin().exhibitionDevices().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionDevice(group1.id!!, model2.id!!,"name", screenOrientation, createdExhibitionDeviceId))
            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId, ExhibitionDevice(nonExistingGroupId, model2.id!!,"updated name", screenOrientation, createdExhibitionDeviceId))
            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId, ExhibitionDevice(group1.id!!, nonExistingModelId,"updated name", screenOrientation, createdExhibitionDeviceId))
        }
    }

    @Test
    fun testDeleteDevice() {
        ApiTestBuilder().use {
            val mqttSubscription = it.mqtt().subscribe(MqttDeviceDelete::class.java,"devices/delete")

            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val floor = it.admin().exhibitionFloors().create(exhibitionId = exhibitionId)
            val floorId = floor.id!!
            val room = it.admin().exhibitionRooms().create(exhibitionId = exhibitionId, floorId = floorId)
            val roomId = room.id!!

            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId = exhibitionId, roomId = roomId)
            val model = it.admin().deviceModels().create()
            val createdExhibitionDevice = it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!)
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            assertNotNull(it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId, createdExhibitionDeviceId))
            it.admin().exhibitionDevices().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionDevices().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionDeviceId)
            it.admin().exhibitionDevices().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionDevices().delete(exhibitionId, createdExhibitionDevice)
            assertJsonsEqual(listOf(MqttDeviceDelete(exhibitionId = exhibitionId, id = createdExhibitionDeviceId)), mqttSubscription.getMessages(1))

            it.admin().exhibitionDevices().assertDeleteFail(404, exhibitionId, createdExhibitionDeviceId)
        }
    }

    @Test
    fun testDeleteDeviceWithPages() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val layout = it.admin().pageLayouts().create(it.admin().deviceModels().create())
            val contentVersion = it.admin().contentVersions().create(exhibition)
            val device = createDefaultDevice(it, exhibition)
            val deviceId = device.id!!

            val page = it.admin().exhibitionPages().create(
                exhibition = exhibition,
                layout = layout,
                contentVersion = contentVersion,
                device = device
            )

            it.admin().exhibitionDevices().assertDeleteFail(400, exhibitionId, deviceId)
            it.admin().exhibitionPages().delete(exhibitionId = exhibitionId, exhibitionPage = page)
            it.admin().exhibitionDevices().delete(exhibitionId, device)
        }
    }
}
