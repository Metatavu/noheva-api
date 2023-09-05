package fi.metatavu.noheva.api.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.noheva.api.client.apis.DevicesApi
import fi.metatavu.noheva.api.client.infrastructure.ApiClient
import fi.metatavu.noheva.api.client.infrastructure.ClientException
import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import fi.metatavu.noheva.api.test.functional.settings.ApiTestSettings
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for handling devices
 */
class DevicesTestBuilderResource(
    testBuilder: TestBuilder,
    val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<Device, ApiClient?>(testBuilder, apiClient) {

    private val createdDeviceIds = mutableListOf<UUID>()

    override fun clean(t: Device?) {
        api.deleteDevice(t?.id!!)
    }

    override fun getApi(): DevicesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken

        return DevicesApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates device
     *
     * @param serialNumber serial number
     * @param name name
     * @param description description
     * @param version version
     * @return created device
     */
    fun create(
        serialNumber: String = "123",
        name: String? = null,
        description: String? = null,
        version: String = "1.0.0"
    ): Device {
        val result: Device = api.createDevice(
            DeviceRequest(
                serialNumber = serialNumber,
                name = name,
                description = description,
                version = version
            )
        )
        if (!createdDeviceIds.contains(result.id)) {
            addClosable(result)
            createdDeviceIds.add(result.id!!)
        }

        return result
    }

    /**
     * Lists devices
     *
     * @param status status
     * @param approvalStatus approval status
     * @return found devices
     */
    fun list(
        status: DeviceStatus? = null,
        approvalStatus: DeviceApprovalStatus? = null
    ): Array<Device> {
        return api.listDevices(status = status, approvalStatus = approvalStatus)
    }

    /**
     * Updates device
     *
     * @param deviceId device id
     * @param device device
     * @return updated device
     */
    fun update(deviceId: UUID, device: Device): Device {
        return api.updateDevice(deviceId, device)
    }

    /**
     * Deletes device
     *
     * @param device device
     */
    fun delete(device: Device) {
        api.deleteDevice(device.id!!)
        removeCloseable { closable: Any ->
            if (closable !is Device) {
                return@removeCloseable false
            }
            val closeableDevice: Device = closable
            closeableDevice.id == device.id
        }
        createdDeviceIds.remove(device.id)
    }

    /**
     * Finds device
     *
     * @param deviceId device id
     * @return found device
     */
    fun find(deviceId: UUID): Device {
        return api.findDevice(deviceId)
    }

    /**
     * Gets device key for
     *
     * @param deviceId device id
     * @return device key
     */
    fun getDeviceKey(deviceId: UUID): DeviceKey {
        return api.getDeviceKey(deviceId)
    }

    /**
     * Asserts that creating Device fails with given status code
     *
     * @param expectedStatus expected status code
     * @param deviceRequest payload
     */
    fun assertCreateFail(expectedStatus: Int, deviceRequest: DeviceRequest) {
        try {
            api.createDevice(deviceRequest)
            Assert.fail(String.format("Expected create to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts that finding Device fails with given status code
     *
     * @param expectedStatus expected status code
     * @param deviceId deviceId
     */
    fun assertFindFail(expectedStatus: Int, deviceId: UUID) {
        try {
            api.findDevice(deviceId)
            Assert.fail(String.format("Expected find to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts that updating Device fails with given status code
     *
     * @param expectedStatus expected status code
     * @param deviceId deviceId
     * @param device device payload
     */
    fun assertUpdateFail(expectedStatus: Int, deviceId: UUID, device: Device) {
        try {
            api.updateDevice(deviceId, device)
            Assert.fail(String.format("Expected update to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts that deleting Device fails with given status code
     *
     * @param expectedStatus expectedStatusCode
     * @param deviceId deviceId
     */
    fun assertDeleteFail(expectedStatus: Int, deviceId: UUID) {
        try {
            api.deleteDevice(deviceId)
            Assert.fail(String.format("Expected delete to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }
}