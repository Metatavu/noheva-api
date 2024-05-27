package fi.metatavu.noheva.api.test.functional.builder.impl

import fi.metatavu.noheva.api.client.apis.DeviceDataApi
import fi.metatavu.noheva.api.client.infrastructure.ApiClient
import fi.metatavu.noheva.api.client.infrastructure.ClientException
import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import fi.metatavu.noheva.api.test.functional.settings.ApiTestSettings
import org.junit.Assert
import java.util.*

/**
 * Test builder device datas for handling devices
 */
class DeviceDatasTestBuilderResource(
    testBuilder: TestBuilder,
    private val deviceKey: String?,
    apiClient: ApiClient
): ApiTestBuilderResource<Device, ApiClient?>(testBuilder, apiClient) {

    override fun clean(t: Device?) {
    }

    override fun getApi(): DeviceDataApi {
        if (deviceKey != null) {
            ApiClient.apiKey["X-DEVICE-KEY"] = deviceKey
        }

        return DeviceDataApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Lists device data pages from API
     *
     * @param deviceId device id
     * @return device data pages
     */
    fun listDeviceDataPages(
        deviceId: UUID
    ): Array<DevicePage> {
        return api.listDeviceDataPages(
            deviceId = deviceId
        )
    }

    /**
     * Lists device data layouts from API
     *
     * @param deviceId device id
     * @return device data layouts
     */
    fun listDeviceDataLayouts(
        deviceId: UUID
    ): Array<DeviceLayout> {
        return api.listDeviceDataLayouts(
            deviceId = deviceId
        )
    }

    /**
     * Lists device data settings from API
     *
     * @param deviceId device id
     * @return device data settings
     */
    fun listDeviceDataSettings(
        deviceId: UUID
    ): Array<DeviceSetting> {
        return api.listDeviceDataSettings(
            deviceId = deviceId
        )
    }

    /**
     * Asserts that listing device data pages fails with given status code
     *
     * @param expectedStatus expectedStatusCode
     * @param deviceId device id
     */
    fun assertListDeviceDataPages(expectedStatus: Int, deviceId: UUID) {
        try {
            api.listDeviceDataPages(
                deviceId = deviceId
            )
            Assert.fail(String.format("Expected list to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts that listing device data layouts fails with given status code
     *
     * @param expectedStatus expected status code
     * @param deviceId device id
     */
    fun assertListDeviceDataLayouts(expectedStatus: Int, deviceId: UUID) {
        try {
            api.listDeviceDataLayouts(
                deviceId = deviceId
            )
            Assert.fail(String.format("Expected list to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts that listing device data settings fails with given status code
     *
     * @param expectedStatus expected status code
     * @param deviceId device id
     */
    fun assertListDeviceDataSettings(expectedStatus: Int, deviceId: UUID) {
        try {
            api.listDeviceDataSettings(
                deviceId = deviceId
            )
            Assert.fail(String.format("Expected list to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

}