package fi.metatavu.noheva.api.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.noheva.api.client.apis.ExhibitionDevicesApi
import fi.metatavu.noheva.api.client.infrastructure.ApiClient
import fi.metatavu.noheva.api.client.infrastructure.ClientException
import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import fi.metatavu.noheva.api.test.functional.settings.ApiTestSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import java.util.*

/**
 * Test builder resource for handling exhibitionDevices
 */
class ExhibitionDeviceTestBuilderResource(
    testBuilder: TestBuilder,
    val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<ExhibitionDevice, ApiClient?>(testBuilder, apiClient) {

    /**
     * Creates new exhibition device using default values
     *
     * @param exhibitionId exhibition id
     * @param groupId group id
     * @return created exhibition device
     */
    fun create(exhibitionId: UUID, groupId: UUID): ExhibitionDevice {
        val result: ExhibitionDevice = api.createExhibitionDevice(
            exhibitionId, ExhibitionDevice(
                groupId = groupId,
                name = "Default",
                screenOrientation = ScreenOrientation.PORTRAIT,
                imageLoadStrategy = DeviceImageLoadStrategy.MEMORY
            )
        )

        addClosable(result)
        return result
    }

    /**
     * Creates new exhibition device using default values
     *
     * @param exhibition exhibition
     * @param group group
     * @return created exhibition device
     */
    fun create(exhibition: Exhibition, group: ExhibitionDeviceGroup): ExhibitionDevice {
        return create(
            exhibitionId = exhibition.id!!,
            groupId = group.id!!
        )
    }

    /**
     * Creates new exhibition Device
     *
     * @param exhibitionId exhibition id
     * @param payload payload
     * @return created exhibition Device
     */
    fun create(exhibitionId: UUID, payload: ExhibitionDevice): ExhibitionDevice {
        val result: ExhibitionDevice = api.createExhibitionDevice(exhibitionId, payload)
        addClosable(result)
        return result
    }

    /**
     * Finds exhibition Device
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceId exhibition Device id
     * @return exhibition Device
     */
    fun findExhibitionDevice(exhibitionId: UUID, exhibitionDeviceId: UUID): ExhibitionDevice {
        return api.findExhibitionDevice(exhibitionId, exhibitionDeviceId)
    }

    /**
     * Lists exhibition Devices
     *
     * @param exhibitionId exhibition id
     * @param exhibitionGroupId exhibition group id
     * @param deviceModelId device model ID
     * @return exhibition Devices
     */
    fun listExhibitionDevices(
        exhibitionId: UUID,
        exhibitionGroupId: UUID?,
        deviceModelId: UUID?
    ): Array<ExhibitionDevice> {
        return api.listExhibitionDevices(
            exhibitionId = exhibitionId,
            exhibitionGroupId = exhibitionGroupId,
            deviceModelId = deviceModelId
        )
    }

    /**
     * Updates exhibition Device
     *
     * @param exhibitionId exhibition id
     * @param payload update payload
     * @return updated exhibition Device
     */
    fun updateExhibitionDevice(exhibitionId: UUID, payload: ExhibitionDevice): ExhibitionDevice {
        return api.updateExhibitionDevice(exhibitionId, payload.id!!, payload)
    }

    /**
     * Deletes a exhibitionDevice from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDevice exhibitionDevice to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionDevice: ExhibitionDevice) {
        delete(exhibitionId, exhibitionDevice.id!!)
    }

    /**
     * Deletes a exhibitionDevice from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceId exhibitionDevice id to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionDeviceId: UUID) {
        api.deleteExhibitionDevice(exhibitionId, exhibitionDeviceId)
        removeCloseable { closable: Any ->
            if (closable !is ExhibitionDevice) {
                return@removeCloseable false
            }

            val closeableExhibitionDevice: ExhibitionDevice = closable
            closeableExhibitionDevice.id!! == exhibitionDeviceId
        }
    }

    /**
     * Asserts exhibitionDevice count within the system
     *
     * @param expected expected count
     * @param exhibitionId exhibition id
     * @param exhibitionGroupId exhibition group id
     * @param deviceModelId device model id
     */
    fun assertCount(expected: Int, exhibitionId: UUID, exhibitionGroupId: UUID?, deviceModelId: UUID?) {
        assertEquals(
            expected, api.listExhibitionDevices(
                exhibitionId = exhibitionId,
                exhibitionGroupId = exhibitionGroupId,
                deviceModelId = deviceModelId
            ).size
        )
    }

    /**
     * Asserts find fails with given status
     *
     * @param expectedStatus expected status code
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceId exhibitionDevice id
     */
    fun assertFindFailStatus(expectedStatus: Int, exhibitionId: UUID, exhibitionDeviceId: UUID) {
        assertFindFailStatus(expectedStatus, exhibitionId, exhibitionDeviceId)
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceId exhibitionDevice id
     */
    fun assertFindFail(expectedStatus: Int, exhibitionId: UUID, exhibitionDeviceId: UUID) {
        try {
            api.findExhibitionDevice(exhibitionId, exhibitionDeviceId)
            fail(String.format("Expected find to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts list status fails with given status code
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param exhibitionGroupId exhibition group id
     * @param deviceModelId device model id
     */
    fun assertListFail(expectedStatus: Int, exhibitionId: UUID, exhibitionGroupId: UUID?, deviceModelId: UUID?) {
        try {
            api.listExhibitionDevices(
                exhibitionId = exhibitionId,
                exhibitionGroupId = exhibitionGroupId,
                deviceModelId = deviceModelId
            )
            fail(String.format("Expected list to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts create fails with given status
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param payload payload
     */
    fun assertCreateFail(expectedStatus: Int, exhibitionId: UUID, payload: ExhibitionDevice) {
        try {
            create(exhibitionId, payload)
            fail(String.format("Expected create to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts update fails with given status
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param payload payload
     */
    fun assertUpdateFail(expectedStatus: Int, exhibitionId: UUID, payload: ExhibitionDevice) {
        try {
            updateExhibitionDevice(exhibitionId, payload)
            fail(String.format("Expected update to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts delete fails with given status
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param id id
     */
    fun assertDeleteFail(expectedStatus: Int, exhibitionId: UUID, id: UUID) {
        try {
            delete(exhibitionId, id)
            fail(String.format("Expected delete to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    override fun clean(exhibitionDevice: ExhibitionDevice) {
        val exhibitionId = exhibitionDevice.exhibitionId!!
        val deviceId = exhibitionDevice.id!!
        api.deleteExhibitionDevice(exhibitionId = exhibitionId, deviceId = deviceId)
    }

    override fun getApi(): ExhibitionDevicesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ExhibitionDevicesApi(ApiTestSettings.apiBasePath)
    }

}