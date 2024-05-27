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
                    version = "1.0.0",
                    deviceType = DeviceType.NOHEVA_ANDROID
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
                    version = "1.0.0",
                    deviceType = DeviceType.NOHEVA_ANDROID
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
        val exhibition = testBuilder.admin.exhibitions.create(Exhibition(
            name = "default exhibition",
            active = true
        ))

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
                device = device.copy(approvalStatus = DeviceApprovalStatus.APPROVED)
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
                devices[index].id!!
            )
        }

        val devicePages = exhibitionDevices.mapIndexed { index, exhibitionDevice ->
            testBuilder.getDevice(deviceKeys[index]).deviceDatas.listDeviceDataPages(
                devices[index].id!!
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
        val exhibition = testBuilder.admin.exhibitions.create(
            Exhibition(
                name = "default exhibition",
                active = true
            )
        )

        val exhibitionId = exhibition.id!!
        val device = testBuilder.admin.devices.create(serialNumber = "device", version = "1.0.0")
        val deviceGroup = createDefaultDeviceGroup(testBuilder, exhibition)

        testBuilder.admin.exhibitionDevices.create(
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
        testBuilder.getDevice(null).deviceDatas.assertListDeviceDataLayouts(
            expectedStatus = 403,
            deviceId = device.id
        )

        testBuilder.getDevice(null).deviceDatas.assertListDeviceDataLayouts(
            expectedStatus = 403,
            deviceId = device.id
        )

        // Invalid key
        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataLayouts(
            expectedStatus =  403,
            deviceId = device.id
        )

        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataPages(
            expectedStatus =  403,
            deviceId = device.id
        )

        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataSettings(
            expectedStatus =  403,
            deviceId = device.id
        )

        testBuilder.admin.devices.update(
            deviceId = device.id,
            device = device .copy(approvalStatus = DeviceApprovalStatus.APPROVED)
        )

        val key = testBuilder.admin.devices.getDeviceKey(device.id).key

        // Assert it works with the key
        assertEquals(testBuilder.getDevice(key).deviceDatas.listDeviceDataLayouts(deviceId = device.id).size, 0)
        assertEquals(testBuilder.getDevice(key).deviceDatas.listDeviceDataPages(deviceId = device.id).size, 0)

        testBuilder.admin.devices.update(
            deviceId = device.id,
            device = device.copy(approvalStatus = DeviceApprovalStatus.PENDING_REAPPROVAL)
        )

        // Unapproved device
        testBuilder.getDevice(key).deviceDatas.assertListDeviceDataLayouts(
            expectedStatus = 403,
            deviceId = device.id
        )

        testBuilder.getDevice(key).deviceDatas.assertListDeviceDataPages(
            expectedStatus = 403,
            deviceId = device.id
        )
    }

    @Test
    fun testListDeviceDataInvalid() = createTestBuilder().use { testBuilder ->
        // Invalid device id
        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataLayouts(404, UUID.randomUUID())
        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataPages(404, UUID.randomUUID())
        testBuilder.getDevice("fake-key").deviceDatas.assertListDeviceDataSettings(404, UUID.randomUUID())
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
        assertNotEquals(foundDevice.lastSeen, foundDevice2.lastSeen)
    }

    @Test
    fun testDeviceSettingsEmpty(): Unit = createTestBuilder().use { testBuilder ->
        val device = setupApprovedDevice(testBuilder)
        val deviceId = device.id!!
        val deviceKey = testBuilder.admin.devices.getDeviceKey(deviceId).key
        val deviceModel = testBuilder.admin.deviceModels.create()
        val readyDevice = testBuilder.admin.devices.find(deviceId = deviceId)

        testBuilder.admin.devices.update(
            deviceId = deviceId,
            device = readyDevice.copy(deviceModelId = deviceModel.id!!)
        )

        val deviceSettings = testBuilder.getDevice(deviceKey).deviceDatas.listDeviceDataSettings(deviceId = deviceId)
        assertEquals(deviceSettings.size, 0)

        testBuilder.admin.devices.update(
            deviceId = deviceId,
            device = readyDevice.copy(deviceModelId = null)
        )
    }

    @Test
    fun testDeviceSettingsDensity(): Unit = createTestBuilder().use { testBuilder ->
        val device = setupApprovedDevice(testBuilder)
        val deviceId = device.id!!
        val deviceKey = testBuilder.admin.devices.getDeviceKey(deviceId).key
        val deviceModel = testBuilder.admin.deviceModels.create(DeviceModel(
            manufacturer = "Manufacturer with Density setting",
            model = "Model with Density setting",
            dimensions = DeviceModelDimensions(),
            displayMetrics = DeviceModelDisplayMetrics(
                density = 77.0
            ),
            capabilities = DeviceModelCapabilities(touch = true),
            screenOrientation = ScreenOrientation.PORTRAIT
        ))
        val readyDevice = testBuilder.admin.devices.find(deviceId = deviceId)

        testBuilder.admin.devices.update(
            deviceId = deviceId,
            device = readyDevice.copy(deviceModelId = deviceModel.id!!)
        )

        val deviceSettings = testBuilder.getDevice(deviceKey).deviceDatas.listDeviceDataSettings(deviceId = deviceId)
        assertEquals(deviceSettings.size, 1)
        assertEquals(deviceSettings[0].key, DeviceSettingKey.SCREEN_DENSITY)
        assertEquals(deviceSettings[0].value, "77.0")
        assertEquals(deviceSettings[0].modifiedAt, deviceModel.modifiedAt)

        testBuilder.admin.devices.update(
            deviceId = deviceId,
            device = readyDevice.copy(deviceModelId = null)
        )
    }

    @Test
    fun testDeviceSettingsDensityNull(): Unit = createTestBuilder().use { testBuilder ->
        val device = setupApprovedDevice(testBuilder)
        val deviceId = device.id!!
        val deviceKey = testBuilder.admin.devices.getDeviceKey(deviceId).key
        val deviceModel = testBuilder.admin.deviceModels.create(DeviceModel(
            manufacturer = "Manufacturer with Density setting",
            model = "Model with Density setting",
            dimensions = DeviceModelDimensions(),
            displayMetrics = DeviceModelDisplayMetrics(
                density = null
            ),
            capabilities = DeviceModelCapabilities(touch = true),
            screenOrientation = ScreenOrientation.PORTRAIT
        ))
        val readyDevice = testBuilder.admin.devices.find(deviceId = deviceId)

        testBuilder.admin.devices.update(
            deviceId = deviceId,
            device = readyDevice.copy(deviceModelId = deviceModel.id!!)
        )

        val deviceSettings = testBuilder.getDevice(deviceKey).deviceDatas.listDeviceDataSettings(deviceId = deviceId)
        assertEquals(deviceSettings.size, 0)

        testBuilder.admin.devices.update(
            deviceId = deviceId,
            device = readyDevice.copy(deviceModelId = null)
        )
    }

    @Test
    fun testDeviceDataResources() = createTestBuilder().use { testBuilder ->
        val exhibition = testBuilder.admin.exhibitions.create(Exhibition(
            name = "Test exhibition",
            active = true
        ))

        val exhibitionId = exhibition.id!!
        val deviceGroup = createDefaultDeviceGroup(testBuilder, exhibition)
        val device = setupApprovedDevice(testBuilder)
        val deviceId = device.id!!
        val deviceKey = testBuilder.admin.devices.getDeviceKey(deviceId).key

        val deviceModel = testBuilder.admin.deviceModels.create()

        val exhibitionDeviceId = testBuilder.admin.exhibitionDevices.create(
            exhibitionId = exhibitionId,
            payload = ExhibitionDevice(
                deviceId = deviceId,
                exhibitionId = exhibitionId,
                groupId = deviceGroup.id!!,
                location = Point(0.0, 0.0),
                name = "Exhibition device",
                screenOrientation = ScreenOrientation.LANDSCAPE,
                imageLoadStrategy = DeviceImageLoadStrategy.DISK
            )
        ).id!!

        val testLayoutHtml = javaClass.getResource("/test-html-layout.html")?.readText()
        assertNotNull(testLayoutHtml)

        val layoutId = testBuilder.admin.pageLayouts.create(
            payload = PageLayout(
                name = "Test layout",
                modelId = deviceModel.id!!,
                layoutType = LayoutType.HTML,
                data = PageLayoutViewHtml(
                    html = testLayoutHtml!!
                ),
                screenOrientation = ScreenOrientation.LANDSCAPE
            )
        ).id!!

        val floorId = testBuilder.admin.exhibitionFloors.create(exhibitionId).id!!
        val roomId = testBuilder.admin.exhibitionRooms.create(exhibitionId, floorId).id!!

        val contentVersionId = testBuilder.admin.contentVersions.create(
            exhibitionId = exhibitionId,
            payload = ContentVersion(name = "content version", language = "FI", rooms = arrayOf(roomId))
        ).id!!

        // Page with all resources defined
        val page1 = testBuilder.admin.exhibitionPages.create(
            exhibitionId = exhibitionId,
            payload = ExhibitionPage(
                deviceId = exhibitionDeviceId,
                layoutId = layoutId,
                contentVersionId = contentVersionId,
                exhibitionId = exhibitionId,
                name = "page 1",
                enterTransitions = arrayOf(),
                eventTriggers = arrayOf(),
                exitTransitions = arrayOf(),
                orderNumber = 0,
                resources = arrayOf(
                    ExhibitionPageResource(
                        id = "62206AFF-74A1-44A6-8EFC-C8FDB1CE2890",
                        data = "https://www.example.com/image1.png",
                        type = ExhibitionPageResourceType.IMAGE,
                        mode = PageResourceMode.STATIC
                    ),
                    ExhibitionPageResource(
                        id = "9056A552-4E7D-4988-9DAF-F381F3EA7131",
                        data = "Text content",
                        type = ExhibitionPageResourceType.TEXT,
                        mode = PageResourceMode.STATIC
                    ),
                    ExhibitionPageResource(
                        id = "c9c97b46-25c9-465d-a79f-032ff3cfa271",
                        data = "https://www.example.com/image2.png",
                        type = ExhibitionPageResourceType.IMAGE,
                        mode = PageResourceMode.STATIC
                    ),
                    ExhibitionPageResource(
                        id = "27ff5a5f-3be5-4eae-9196-7d83dc47831d",
                        data = "https://www.example.com/video.mov",
                        type = ExhibitionPageResourceType.VIDEO,
                        mode = PageResourceMode.STATIC
                    ),
                    ExhibitionPageResource(
                        id = "95e4d27f-43da-4349-9e86-431c31de2ffe",
                        data = "https://www.example.com/video-under-control.mp4",
                        type = ExhibitionPageResourceType.VIDEO,
                        mode = PageResourceMode.STATIC
                    ),
                    ExhibitionPageResource(
                        id = "e363a01e-31b1-4766-931d-8cf54ff1f811",
                        data = "https://www.example.com/play.png",
                        type = ExhibitionPageResourceType.IMAGE,
                        mode = PageResourceMode.STATIC
                    ),
                    ExhibitionPageResource(
                        id = "8b2ef0ea-c5fa-4add-8240-26f759a8bd2a",
                        data = "https://www.example.com/close.png",
                        type = ExhibitionPageResourceType.IMAGE,
                        mode = PageResourceMode.STATIC
                    ),
                ),
            )
        )

        // Page with no resources defined
        val page2 = testBuilder.admin.exhibitionPages.create(
            exhibitionId = exhibitionId,
            payload = ExhibitionPage(
                deviceId = exhibitionDeviceId,
                layoutId = layoutId,
                contentVersionId = contentVersionId,
                exhibitionId = exhibitionId,
                name = "page 2",
                enterTransitions = arrayOf(),
                eventTriggers = arrayOf(),
                exitTransitions = arrayOf(),
                orderNumber = 0,
                resources = arrayOf(),
            )
        )

        // Page with invalid resource id defined
        val page3 = testBuilder.admin.exhibitionPages.create(
            exhibitionId = exhibitionId,
            payload = ExhibitionPage(
                deviceId = exhibitionDeviceId,
                layoutId = layoutId,
                contentVersionId = contentVersionId,
                exhibitionId = exhibitionId,
                name = "page 3",
                enterTransitions = arrayOf(),
                eventTriggers = arrayOf(),
                exitTransitions = arrayOf(),
                orderNumber = 0,
                resources = arrayOf(
                    ExhibitionPageResource(
                        id = "A3B33F4A-A80A-4CE1-9949-8DAC9F8E7B71",
                        data = "https://www.example.com/image3.png",
                        type = ExhibitionPageResourceType.IMAGE,
                        mode = PageResourceMode.STATIC
                    )
                ),
            )
        )

        val devicePages = testBuilder.getDevice(deviceKey).deviceDatas.listDeviceDataPages(
            deviceId = deviceId
        )

        devicePages.sortBy { it.name }

        assertEquals(devicePages.size, 3)
        assertEquals(page1.id, devicePages[0].id)
        assertEquals(7, devicePages[0].resources.size)
        assertEquals("62206AFF-74A1-44A6-8EFC-C8FDB1CE2890", devicePages[0].resources[0].id)
        assertEquals("https://www.example.com/image1.png", devicePages[0].resources[0].data)
        assertEquals(ExhibitionPageResourceType.IMAGE, devicePages[0].resources[0].type)
        assertEquals(PageResourceMode.STATIC, devicePages[0].resources[0].mode)
        assertEquals("Page background", devicePages[0].resources[0].component)
        assertEquals("@style:background-image", devicePages[0].resources[0].property)

        assertEquals("9056A552-4E7D-4988-9DAF-F381F3EA7131", devicePages[0].resources[1].id)
        assertEquals("Text content", devicePages[0].resources[1].data)
        assertEquals(ExhibitionPageResourceType.TEXT, devicePages[0].resources[1].type)
        assertEquals(PageResourceMode.STATIC, devicePages[0].resources[1].mode)
        assertEquals("Text contents", devicePages[0].resources[1].component)
        assertEquals("#text", devicePages[0].resources[1].property)

        assertEquals("c9c97b46-25c9-465d-a79f-032ff3cfa271", devicePages[0].resources[2].id)
        assertEquals("https://www.example.com/image2.png", devicePages[0].resources[2].data)
        assertEquals(ExhibitionPageResourceType.IMAGE, devicePages[0].resources[2].type)
        assertEquals(PageResourceMode.STATIC, devicePages[0].resources[2].mode)
        assertEquals("Image", devicePages[0].resources[2].component)
        assertEquals("@src", devicePages[0].resources[2].property)

        assertEquals("27ff5a5f-3be5-4eae-9196-7d83dc47831d", devicePages[0].resources[3].id)
        assertEquals("https://www.example.com/video.mov", devicePages[0].resources[3].data)
        assertEquals(ExhibitionPageResourceType.VIDEO, devicePages[0].resources[3].type)
        assertEquals(PageResourceMode.STATIC, devicePages[0].resources[3].mode)
        assertEquals("Video", devicePages[0].resources[3].component)
        assertEquals("@src", devicePages[0].resources[3].property)

        assertEquals("95e4d27f-43da-4349-9e86-431c31de2ffe", devicePages[0].resources[4].id)
        assertEquals("https://www.example.com/video-under-control.mp4", devicePages[0].resources[4].data)
        assertEquals(ExhibitionPageResourceType.VIDEO, devicePages[0].resources[4].type)
        assertEquals(PageResourceMode.STATIC, devicePages[0].resources[4].mode)
        assertEquals("Video component with controls", devicePages[0].resources[4].component)
        assertEquals("@src", devicePages[0].resources[4].property)

        assertEquals("e363a01e-31b1-4766-931d-8cf54ff1f811", devicePages[0].resources[5].id)
        assertEquals("https://www.example.com/play.png", devicePages[0].resources[5].data)
        assertEquals(ExhibitionPageResourceType.IMAGE, devicePages[0].resources[5].type)
        assertEquals(PageResourceMode.STATIC, devicePages[0].resources[5].mode)
        assertEquals("Play button", devicePages[0].resources[5].component)
        assertEquals("@src", devicePages[0].resources[5].property)

        assertEquals("8b2ef0ea-c5fa-4add-8240-26f759a8bd2a", devicePages[0].resources[6].id)
        assertEquals("https://www.example.com/close.png", devicePages[0].resources[6].data)
        assertEquals(ExhibitionPageResourceType.IMAGE, devicePages[0].resources[6].type)
        assertEquals(PageResourceMode.STATIC, devicePages[0].resources[6].mode)
        assertEquals("Close button", devicePages[0].resources[6].component)
        assertEquals("@src", devicePages[0].resources[6].property)

        assertEquals(page2.id, devicePages[1].id)
        assertEquals(0, devicePages[1].resources.size)

        assertEquals(page3.id, devicePages[2].id)
        assertEquals(1, devicePages[2].resources.size)
        assertEquals("A3B33F4A-A80A-4CE1-9949-8DAC9F8E7B71", devicePages[2].resources[0].id)
        assertEquals("https://www.example.com/image3.png", devicePages[2].resources[0].data)
        assertEquals(ExhibitionPageResourceType.IMAGE, devicePages[2].resources[0].type)
        assertEquals(PageResourceMode.STATIC, devicePages[2].resources[0].mode)
        assertNull(devicePages[2].resources[0].component)
        assertNull(devicePages[2].resources[0].property)
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