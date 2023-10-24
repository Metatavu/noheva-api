package fi.metatavu.noheva.api.test.functional

import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import fi.metatavu.noheva.api.test.functional.resources.KeycloakResource
import fi.metatavu.noheva.api.test.functional.resources.MqttResource
import fi.metatavu.noheva.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID


/**
 * Test class for testing Device API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class DeviceTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateDevice() {
        createTestBuilder().use { testBuilder ->
            val createdDevice = testBuilder.admin.devices.create(serialNumber = "123abc", version = "1.0.0")
            val createdDevice2 = testBuilder.admin.devices.create(
                serialNumber = "123abc2",
                name = "Test device",
                description = "Test device description",
                version = "1.0.0"
            )

            assertEquals(createdDevice.serialNumber, "123abc")
            assertEquals(createdDevice.version, "1.0.0")
            assertEquals(createdDevice.approvalStatus, DeviceApprovalStatus.PENDING)
            assertEquals(createdDevice.status, DeviceStatus.OFFLINE)
            assertEquals(createdDevice.lastSeen, createdDevice.createdAt)
            assertNull(createdDevice.name)
            assertNull(createdDevice.description)
            assertNull(createdDevice.lastModifierId)
            assertNotNull(createdDevice.createdAt)
            assertNotNull(createdDevice.modifiedAt)

            assertEquals(createdDevice2.serialNumber, "123abc2")
            assertEquals(createdDevice2.name, "Test device")
            assertEquals(createdDevice2.description, "Test device description")
        }
    }

    @Test
    fun testCreateDeviceFail() {
        createTestBuilder().use { testBuilder ->
            testBuilder.admin.devices.create(serialNumber = "123abc", version = "1.0.0")
            testBuilder.admin.devices.assertCreateFail(
                expectedStatus = 409,
                deviceRequest = DeviceRequest(
                    serialNumber = "123abc",
                    version = "1.0.0"
                )
            )
        }
    }

    @Test
    fun testListDevices() {
        createTestBuilder().use { testBuilder ->
            for (i in 0 until 10) {
                testBuilder.admin.devices.create(serialNumber = "123abc$i", version = "1.0.0")
            }

            val foundDevices = testBuilder.admin.devices.list()

            assertEquals(foundDevices.size, 10)

            foundDevices
                .filterIndexed { i, _ -> i % 2 == 0 }
                .forEach {
                    testBuilder.admin.devices.update(
                        deviceId = it.id!!,
                        device = it.copy(approvalStatus = DeviceApprovalStatus.APPROVED)
                    )
                }

            val foundDevices2 = testBuilder.admin.devices.list(approvalStatus = DeviceApprovalStatus.APPROVED)
            val foundDevices3 = testBuilder.admin.devices.list(approvalStatus = DeviceApprovalStatus.PENDING)

            assertEquals(foundDevices2.size, 5)
            assertEquals(foundDevices3.size, 5)
        }
    }

    @Test
    fun testDeviceApproval() {
        createTestBuilder().use { testBuilder ->
            val createdDevice = testBuilder.admin.devices.create(serialNumber = "123abc", version = "1.0.0")
            assertEquals(createdDevice.approvalStatus, DeviceApprovalStatus.PENDING)

            val updatedDevice = testBuilder.admin.devices.update(
                deviceId = createdDevice.id!!,
                device = createdDevice.copy(approvalStatus = DeviceApprovalStatus.APPROVED)
            )
            assertEquals(updatedDevice.approvalStatus, DeviceApprovalStatus.APPROVED)

            // Assert that one cannot create device with same serial number if it exists and approval status isn't PENDING_REAPPROVAL
            testBuilder.admin.devices.assertCreateFail(
                expectedStatus = 409,
                deviceRequest = DeviceRequest(
                    serialNumber = "123abc",
                    version = "1.0.0"
                )
            )
            testBuilder.admin.devices.getDeviceKey(createdDevice.id)
            val readyDevice = testBuilder.admin.devices.find(createdDevice.id)
            assertEquals(readyDevice.approvalStatus, DeviceApprovalStatus.READY)

            val updatedDevice2 = testBuilder.admin.devices.update(
                deviceId = createdDevice.id,
                device = createdDevice.copy(approvalStatus = DeviceApprovalStatus.PENDING_REAPPROVAL)
            )
            assertEquals(updatedDevice2.approvalStatus, DeviceApprovalStatus.PENDING_REAPPROVAL)

            // Assert that one can create device with same serial number if it exists and approval status is PENDING_REAPPROVAL
            val reCreatedDevice = testBuilder.admin.devices.create(serialNumber = "123abc", version = "1.0.0")
            assertEquals(reCreatedDevice.approvalStatus, DeviceApprovalStatus.PENDING)
            val reUpdatedDevice = testBuilder.admin.devices.update(
                deviceId = createdDevice.id,
                device = createdDevice.copy(approvalStatus = DeviceApprovalStatus.APPROVED)
            )
            assertEquals(reUpdatedDevice.approvalStatus, DeviceApprovalStatus.APPROVED)

            testBuilder.admin.devices.getDeviceKey(createdDevice.id)
            val readyDevice2 = testBuilder.admin.devices.find(createdDevice.id)
            assertEquals(readyDevice2.approvalStatus, DeviceApprovalStatus.READY)
        }
    }

    @Test
    fun testFindDevice() {
        createTestBuilder().use { testBuilder ->
            val createdDevice = testBuilder.admin.devices.create(serialNumber = "123abc", version = "1.0.0")
            val foundDevice = testBuilder.admin.devices.find(createdDevice.id!!)
            assertEquals(foundDevice.serialNumber, "123abc")
            assertEquals(foundDevice.version, "1.0.0")
            assertEquals(foundDevice.approvalStatus, DeviceApprovalStatus.PENDING)
            assertEquals(foundDevice.status, DeviceStatus.OFFLINE)
            assertEquals(foundDevice.lastSeen, foundDevice.createdAt)
            assertNull(foundDevice.name)
            assertNull(foundDevice.description)
            assertNull(foundDevice.lastModifierId)
            assertNotNull(foundDevice.createdAt)
            assertNotNull(foundDevice.modifiedAt)

            // Asserts that one cannot find device with invalid id
            testBuilder.admin.devices.assertFindFail(
                expectedStatus = 404,
                deviceId = UUID.randomUUID()
            )
        }
    }

    @Test
    fun testUpdateDevice() {
        createTestBuilder().use { testBuilder ->
            val deviceModel = testBuilder.admin.deviceModels.create()
            val createdDevice = testBuilder.admin.devices.create(serialNumber = "123abc", version = "1.0.0")
            val updatedDevice = testBuilder.admin.devices.update(
                deviceId = createdDevice.id!!,
                device = createdDevice.copy(
                    name = "Test device",
                    description = "Test device description",
                    deviceModelId = deviceModel.id!!,
                    approvalStatus = DeviceApprovalStatus.APPROVED,
                )
            )
            assertEquals(updatedDevice.name, "Test device")
            assertEquals(updatedDevice.description, "Test device description")
            assertEquals(updatedDevice.approvalStatus, DeviceApprovalStatus.APPROVED)
            assertEquals(updatedDevice.status, DeviceStatus.OFFLINE)
            assertEquals(updatedDevice.lastSeen, updatedDevice.createdAt)
            assertEquals(updatedDevice.deviceModelId, deviceModel.id)
            assertNotNull(updatedDevice.lastModifierId)
            assertNotNull(updatedDevice.createdAt)
            assertNotNull(updatedDevice.modifiedAt)

            // Asserts that one cannot update device with invalid id
            testBuilder.admin.devices.assertUpdateFail(
                expectedStatus = 404,
                deviceId = UUID.randomUUID(),
                device = updatedDevice
            )

            // Asserts that one cannot update device with invalid device model id
            testBuilder.admin.devices.assertUpdateFail(
                expectedStatus = 400,
                deviceId = updatedDevice.id!!,
                device = updatedDevice.copy(deviceModelId = UUID.randomUUID())
            )
        }
    }

    @Test
    fun testDeleteDevice() {
        createTestBuilder().use { testBuilder ->
            val createdDevice = testBuilder.admin.devices.create(serialNumber = "123abc", version = "1.0.0")
            val foundDevice = testBuilder.admin.devices.find(createdDevice.id!!)

            assertEquals(foundDevice, createdDevice)

            testBuilder.admin.devices.delete(createdDevice)
            testBuilder.admin.devices.assertFindFail(
                expectedStatus = 404,
                deviceId = createdDevice.id
            )
            // Asserts that one cannot delete device with invalid id
            testBuilder.admin.devices.assertDeleteFail(
                expectedStatus = 404,
                deviceId = UUID.randomUUID()
            )
        }
    }

    @Test
    fun testDeleteDeviceFail() {
        createTestBuilder().use { testBuilder ->
            val createdDevice = testBuilder.admin.devices.create(serialNumber = "123abc", version = "1.0.0")
            val exhibition = testBuilder.admin.exhibitions.create()
            val exhibitionDeviceGroup = createDefaultDeviceGroup(testBuilder, exhibition)
            val exhibitionDevice = createDefaultExhibitionDevice(testBuilder, exhibition, exhibitionDeviceGroup)
            val updatedExhibitionDevice = testBuilder.admin.exhibitionDevices.updateExhibitionDevice(exhibition.id!!, exhibitionDevice.copy(deviceId = createdDevice.id!!))

            assertEquals(updatedExhibitionDevice.deviceId, createdDevice.id)

            testBuilder.admin.devices.assertDeleteFail(400, createdDevice.id)
        }
    }

    @Test
    fun testListDeviceDatas() = createTestBuilder().use { testBuilder ->
        val exhibition = testBuilder.admin.exhibitions.create()
        val exhibitionId = exhibition.id!!

        val devices = (1..3).map {
            testBuilder.admin.devices.create(serialNumber = "device-$it", version = "1.0.0")
        }

        val deviceGroups = (1..3).map {
            createDefaultDeviceGroup(testBuilder, exhibition)
        }

        val floorId = testBuilder.admin.exhibitionFloors.create(exhibitionId).id!!
        val roomId = testBuilder.admin.exhibitionRooms.create(exhibitionId, floorId).id!!

        val contentVersions = List(deviceGroups.size) { index ->
            testBuilder.admin.contentVersions.create(
                exhibitionId = exhibitionId,
                payload = ContentVersion(name = "created name $index", language = "FI", rooms = arrayOf(roomId))
            )
        }

        val pageLayouts = (1..4).map {
            testBuilder.admin.pageLayouts.create(
                payload = PageLayout(
                    name = "created name",
                    data = PageLayoutViewHtml("<html><body><h1>Test</h1></body></html>"),
                    thumbnailUrl = "http://example.com/thumbnail.png",
                    screenOrientation = ScreenOrientation.PORTRAIT,
                    layoutType = LayoutType.HTML,
                    modelId = testBuilder.admin.deviceModels.create().id!!
                )
            )
        }

        val deviceKeys = devices.map { device ->
            testBuilder.admin.devices.update(
                deviceId = device.id!!,
                device = device .copy(approvalStatus = DeviceApprovalStatus.APPROVED)
            )

            testBuilder.admin.devices.getDeviceKey(device.id).key
        }

        val exhibitionDevices = devices.mapIndexed { index, device ->
            testBuilder.admin.exhibitionDevices.create(
                exhibitionId = exhibitionId,
                payload = ExhibitionDevice(
                    deviceId = device.id!!,
                    exhibitionId = exhibitionId,
                    groupId = deviceGroups[index].id!!,
                    location = Point(0.0, 0.0),
                    name = "Exhibition device $index",
                    screenOrientation = ScreenOrientation.LANDSCAPE,
                    imageLoadStrategy = DeviceImageLoadStrategy.DISK
                )
            )
        }

        val pages = (1..4).map {
            testBuilder.admin.exhibitionPages.create(
                exhibitionId = exhibitionId,
                payload = ExhibitionPage(
                    name = "created name",
                    deviceId = exhibitionDevices[(it - 1) / 2].id!!,
                    layoutId = pageLayouts[it - 1].id!!,
                    contentVersionId = contentVersions[(it - 1) / 2].id!!,
                    exhibitionId = exhibitionId,
                    enterTransitions = arrayOf(),
                    eventTriggers = arrayOf(),
                    exitTransitions = arrayOf(),
                    resources = arrayOf(),
                    orderNumber = it,
                )
            )
        }

        val deviceLayouts = exhibitionDevices.mapIndexed { index, exhibitionDevice ->
            testBuilder.getDevice(deviceKeys[index]).deviceDatas.listDeviceDataLayouts(
                exhibitionDevice.id!!
            )
        }

        val devicePages = exhibitionDevices.mapIndexed { index, exhibitionDevice ->
            testBuilder.getDevice(deviceKeys[index]).deviceDatas.listDeviceDataPages(
                exhibitionDevice.id!!
            )
        }

        // Device 1 should have layouts 1, 2
        assertEquals(deviceLayouts[0].size, 2)
        assertTrue(deviceLayouts[0].map(DeviceLayout::id).contains(pageLayouts[0].id))
        assertTrue(deviceLayouts[0].map(DeviceLayout::id).contains(pageLayouts[1].id))

        // Device 2 should have layouts 3, 4
        assertEquals(deviceLayouts[1].size, 2)
        assertTrue(deviceLayouts[1].map(DeviceLayout::id).contains(pageLayouts[2].id))
        assertTrue(deviceLayouts[1].map(DeviceLayout::id).contains(pageLayouts[3].id))

        // Device 3 should not have layouts
        assertEquals(deviceLayouts[2].size, 0)

        // Device 1 should have pages 1, 2
        assertEquals(devicePages[0].size, 2)
        assertTrue(devicePages[0].map(DevicePage::id).contains(pages[0].id))
        assertTrue(devicePages[0].map(DevicePage::id).contains(pages[1].id))

        // Device 2 should have pages 3, 4
        assertEquals(devicePages[1].size, 2)
        assertTrue(devicePages[1].map(DevicePage::id).contains(pages[2].id))
        assertTrue(devicePages[1].map(DevicePage::id).contains(pages[3].id))

        // Device 3 should have not pages
        assertEquals(devicePages[2].size, 0)
    }

    @Test
    fun testListDeviceDataUnauthorized() = createTestBuilder().use { testBuilder ->
        val exhibition = testBuilder.admin.exhibitions.create()
        val exhibitionId = exhibition.id!!
        val device = testBuilder.admin.devices.create(serialNumber = "device", version = "1.0.0")
        val deviceGroup = createDefaultDeviceGroup(testBuilder, exhibition)
        val exhibitionDevice = testBuilder.admin.exhibitionDevices.create(
            exhibitionId = exhibitionId,
            payload = ExhibitionDevice(
                deviceId = device.id!!,
                exhibitionId = exhibitionId,
                groupId = deviceGroup.id!!,
                location = Point(0.0, 0.0),
                name = "Exhibition device",
                screenOrientation = ScreenOrientation.LANDSCAPE,
                imageLoadStrategy = DeviceImageLoadStrategy.DISK
            )
        )

        // No device key
        testBuilder.getDevice(null).deviceDatas.assertListDeviceDataLayouts(403, exhibitionDevice.id!!)
        testBuilder.getDevice(null).deviceDatas.assertListDeviceDataLayouts(403, exhibitionDevice.id)

        // Invalid key
        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataLayouts(403, exhibitionDevice.id)
        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataPages(403, exhibitionDevice.id)

        testBuilder.admin.devices.update(
            deviceId = device.id,
            device = device .copy(approvalStatus = DeviceApprovalStatus.APPROVED)
        )

        val key = testBuilder.admin.devices.getDeviceKey(device.id).key

        // Assert it works with the key
        assertEquals(testBuilder.getDevice(key).deviceDatas.listDeviceDataLayouts(exhibitionDevice.id).size, 0)
        assertEquals(testBuilder.getDevice(key).deviceDatas.listDeviceDataPages(exhibitionDevice.id).size, 0)

        testBuilder.admin.devices.update(
            deviceId = device.id,
            device = device .copy(approvalStatus = DeviceApprovalStatus.PENDING_REAPPROVAL)
        )

        // Unapproved device
        testBuilder.getDevice(key).deviceDatas.assertListDeviceDataLayouts(404, device.id)
        testBuilder.getDevice(key).deviceDatas.assertListDeviceDataPages(404, device.id)
    }

    @Test
    fun testListDeviceDataInvalid() = createTestBuilder().use { testBuilder ->
        // Invalid device id
        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataLayouts(404, UUID.randomUUID())
        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataPages(404, UUID.randomUUID())
    }

    @Test
    fun testHandleDeviceStatusMessages() = createTestBuilder().use { testBuilder ->
        val device = setupApprovedDevice(testBuilder)
        val statusMessageSubscription = testBuilder.mqtt.subscribe(MqttDeviceAttachedToExhibition::class.java, "${device.id}/status")
        testBuilder.mqtt.publish(
            message = MqttDeviceStatus(
                deviceId = device.id!!,
                status = DeviceStatus.ONLINE,
                version = "1.0.0"
            ),
            subTopic = "${device.id}/status"
        )
        val statusMessages = statusMessageSubscription.getMessages(1)
        assertEquals(statusMessages.size, 1)

        val foundDevice = testBuilder.admin.devices.find(device.id)
        assertEquals(foundDevice.status, DeviceStatus.ONLINE)
        assertEquals(foundDevice.version, "1.0.0")

        Thread.sleep(60000)

        testBuilder.mqtt.publish(
            message = MqttDeviceStatus(
                deviceId = device.id,
                status = DeviceStatus.OFFLINE,
                version = "1.0.0"
            ),
            subTopic = "${device.id}/status"
        )
        val statusMessages2 = statusMessageSubscription.getMessages(2)
        assertEquals(statusMessages2.size, 2)

        val foundDevice2 = testBuilder.admin.devices.find(device.id)
        val usageHours = foundDevice2.usageHours ?: 0.0
        // Assert usage hours is greater than one minute (0.01)
        assertTrue(usageHours > 0.01)
        assertEquals(foundDevice2.status, DeviceStatus.OFFLINE)
    }

    /**
     * Setups approved device
     *
     * @return approved device
     */
    private fun setupApprovedDevice(testBuilder: TestBuilder): Device {
        val createdDevice = testBuilder.admin.devices.create(serialNumber = "123abc", version = "0.9.0")
        assertEquals(createdDevice.approvalStatus, DeviceApprovalStatus.PENDING)
        assertEquals(createdDevice.version, "0.9.0")

        return testBuilder.admin.devices.update(
            deviceId = createdDevice.id!!,
            device = createdDevice.copy(approvalStatus = DeviceApprovalStatus.APPROVED)
        )
    }
}