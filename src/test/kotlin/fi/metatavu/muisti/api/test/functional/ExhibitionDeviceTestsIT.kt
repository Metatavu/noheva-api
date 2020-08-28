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
    fun testCreateDevice() {
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
            val createdDevice = it.admin().exhibitionDevices().create(exhibitionId, ExhibitionDevice( groupId = group.id!!, modelId = model.id!!, name = "name", screenOrientation = screenOrientation, pageOrder = emptyArray()))
            assertNotNull(createdDevice)
            assertJsonsEqual(listOf(MqttDeviceCreate(exhibitionId = exhibitionId, id = createdDevice.id!!)), mqttSubscription.getMessages(1))

            it.admin().exhibitionDevices().assertCreateFail(400, exhibitionId, ExhibitionDevice( groupId = UUID.randomUUID(), modelId = model.id!!, name = "name", screenOrientation = screenOrientation, pageOrder = emptyArray()))
            it.admin().exhibitionDevices().assertCreateFail(400, exhibitionId, ExhibitionDevice( groupId = group.id!!, modelId = UUID.randomUUID(), name = "name", screenOrientation = screenOrientation, pageOrder = emptyArray()))
        }
   }

    @Test
    fun testDeviceIndexPage() {
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
                indexPageId = page.id!!,
                pageOrder = emptyArray()
            ))

            val deviceWithoutIndex = it.admin().exhibitionDevices().create(exhibitionId = exhibition.id!!, payload = ExhibitionDevice(
                name = "with index",
                screenOrientation = ScreenOrientation.landscape,
                groupId = group.id!!,
                modelId = model.id!!,
                pageOrder = emptyArray()
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
    fun testFindDevice() {
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
    fun testListDevices() {
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
    fun testUpdateDevice() {
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

            val createdExhibitionDevice = it.admin().exhibitionDevices().create(exhibitionId, ExhibitionDevice( groupId = group1.id!!, modelId = model1.id!!, name = "created name", location = Point(-123.0, 234.0), screenOrientation = screenOrientation, pageOrder = emptyArray()))
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
                location = Point(123.2, -234.4),
                pageOrder = emptyArray()
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

            it.admin().exhibitionDevices().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionDevice(groupId = group1.id!!, modelId = model2.id!!, name = "name", screenOrientation = screenOrientation, id = createdExhibitionDeviceId, pageOrder = emptyArray()))
            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId, ExhibitionDevice(groupId = nonExistingGroupId, modelId = model2.id!!, name = "updated name", screenOrientation = screenOrientation, id = createdExhibitionDeviceId, pageOrder = emptyArray()))
            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId, ExhibitionDevice(groupId = group1.id!!, modelId = nonExistingModelId, name = "updated name", screenOrientation = screenOrientation, id = createdExhibitionDeviceId, pageOrder = emptyArray()))
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

    @Test
    fun testDevicePageOrder() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            var device = createDefaultDevice(it, exhibition)
            val foreignDevice = createDefaultDevice(it, exhibition)

            val exhibitionId = exhibition.id!!
            val deviceId = device.id!!

            assertEquals(0, device.pageOrder.size)

            val layout = it.admin().pageLayouts().create(it.admin().deviceModels().create())
            val contentVersion = it.admin().contentVersions().create(exhibition)
            val pages = (0..2).map {
                _ -> it.admin().exhibitionPages().create(
                    exhibition = exhibition,
                    layout = layout,
                    contentVersion = contentVersion,
                    device = device
                )
            }

            val pageIds = pages.map { page -> page.id!! }

            val foreignPages  = (0..2).map {
                _ -> it.admin().exhibitionPages().create(
                    exhibition = exhibition,
                    layout = layout,
                    contentVersion = contentVersion,
                    device = foreignDevice
                )
            }

            device = it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId = exhibitionId, exhibitionDeviceId = deviceId)!!

            assertEquals(pageIds.size, device.pageOrder.size)
            assertJsonsEqual(pageIds, device.pageOrder)

            val updatedPageIds = arrayOf<UUID>(pageIds[1], pageIds[0], pageIds[2])

            device = it.admin().exhibitionDevices().updateExhibitionDevice(exhibitionId, device.copy(pageOrder = updatedPageIds))!!

            assertEquals(updatedPageIds.size, device.pageOrder.size)
            assertJsonsEqual(updatedPageIds, device.pageOrder)

            val foreignPageIds = foreignPages.map { page -> page.id!! }.toTypedArray()
            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId, device.copy(pageOrder = foreignPageIds))

            val partialPageIds = arrayOf<UUID>(pageIds[1], pageIds[0])
            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId, device.copy(pageOrder = partialPageIds))

            val duplicatePageIds = arrayOf<UUID>(pageIds[1], pageIds[0], pageIds[1])
            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId, device.copy(pageOrder = duplicatePageIds))
        }
    }
}
