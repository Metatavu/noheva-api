package fi.metatavu.noheva.api.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.noheva.api.client.apis.DevicesApi
import fi.metatavu.noheva.api.client.infrastructure.ApiClient
import fi.metatavu.noheva.api.client.infrastructure.ClientException
import fi.metatavu.noheva.api.client.models.Device
import fi.metatavu.noheva.api.client.models.DeviceApprovalStatus
import fi.metatavu.noheva.api.client.models.DeviceRequest
import fi.metatavu.noheva.api.client.models.DeviceStatus
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
        serialNumber: String,
        name: String? = null,
        description: String? = null,
        version: String
    ): Device {
        val result: Device = api.createDevice(
            DeviceRequest(
                serialNumber = serialNumber,
                name = name,
                description = description,
                version = version
            )
        )
        addClosable(result)

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
}