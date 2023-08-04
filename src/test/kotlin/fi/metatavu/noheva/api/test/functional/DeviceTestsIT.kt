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
}