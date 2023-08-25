package fi.metatavu.noheva.api.test.functional

import fi.metatavu.noheva.api.client.models.DeviceApprovalStatus
import fi.metatavu.noheva.api.client.models.DeviceRequest
import fi.metatavu.noheva.api.client.models.DeviceStatus
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
            assertEquals(createdDevice.status, DeviceStatus.ONLINE)
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
            assertEquals(foundDevice.status, DeviceStatus.ONLINE)
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
            assertEquals(updatedDevice.status, DeviceStatus.ONLINE)
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
            val exhibitionDevice = createDefaultDevice(testBuilder, exhibition, exhibitionDeviceGroup)
            val updatedExhibitionDevice = testBuilder.admin.exhibitionDevices.updateExhibitionDevice(exhibition.id!!, exhibitionDevice.copy(deviceId = createdDevice.id!!))

            assertEquals(updatedExhibitionDevice.deviceId, createdDevice.id)

            testBuilder.admin.devices.assertDeleteFail(400, createdDevice.id)
        }
    }
}