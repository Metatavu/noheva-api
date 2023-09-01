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
     * @param exhibitionDeviceId exhibition device id
     * @return device data pages
     */
    fun listDeviceDataPages(
        exhibitionDeviceId: UUID
    ): Array<DevicePage> {
        return api.listDeviceDataPages(
            exhibitionDeviceId = exhibitionDeviceId
        )
    }

    /**
     * Lists device data layouts from API
     *
     * @param exhibitionDeviceId exhibition device id
     * @return device data layouts
     */
    fun listDeviceDataLayouts(
        exhibitionDeviceId: UUID
    ): Array<DeviceLayout> {
        return api.listDeviceDataLayouts(
            exhibitionDeviceId = exhibitionDeviceId
        )
    }

    /**
     * Asserts that listing device data pages fails with given status code
     *
     * @param expectedStatus expectedStatusCode
     * @param exhibitionDeviceId exhibitionDeviceId
     */
    fun assertListDeviceDataPages(expectedStatus: Int, exhibitionDeviceId: UUID) {
        try {
            api.listDeviceDataPages(
                exhibitionDeviceId = exhibitionDeviceId
            )
            Assert.fail(String.format("Expected list to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts that listing device data layouts fails with given status code
     *
     * @param expectedStatus expectedStatusCode
     * @param exhibitionDeviceId exhibitionDeviceId
     */
    fun assertListDeviceDataLayouts(expectedStatus: Int, exhibitionDeviceId: UUID) {
        try {
            api.listDeviceDataLayouts(
                exhibitionDeviceId = exhibitionDeviceId
            )
            Assert.fail(String.format("Expected list to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

}