package fi.metatavu.muisti.api.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.muisti.api.client.apis.ExhibitionDevicesApi
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.client.infrastructure.ClientException
import fi.metatavu.muisti.api.client.models.ExhibitionDevice
import fi.metatavu.muisti.api.client.models.Point
import fi.metatavu.muisti.api.client.models.ScreenOrientation
import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Test builder resource for handling exhibitionDevices
 */
class ExhibitionDeviceTestBuilderResource(testBuilder: AbstractTestBuilder<ApiClient?>?, val accessTokenProvider: AccessTokenProvider?, apiClient: ApiClient) : ApiTestBuilderResource<ExhibitionDevice, ApiClient?>(testBuilder, apiClient) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates new exhibition Device with default values
     *
     * @param exhibitionId exhibition id
     * @param groupId group id
     * @param screenOrientation screen orientation
     * @return created exhibition Device
     */
    fun create(exhibitionId: UUID, groupId: UUID, modelId: UUID, screenOrientation: ScreenOrientation): ExhibitionDevice {
        return create(exhibitionId, groupId, modelId, "default device", null, screenOrientation)
    }

    /**
     * Creates new exhibition Device
     *
     * @param exhibitionId exhibition id
     * @param groupId group id
     * @param name name
     * @param location location
     * @param screenOrientation screen orientation
     * @return created exhibition Device
     */
    fun create(exhibitionId: UUID, groupId: UUID, modelId: UUID, name: String, location: Point?, screenOrientation: ScreenOrientation): ExhibitionDevice {
        val payload = ExhibitionDevice(groupId, modelId, name, screenOrientation,null, exhibitionId, location)
        val result: ExhibitionDevice = this.getApi().createExhibitionDevice(exhibitionId, payload)
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
    fun findExhibitionDevice(exhibitionId: UUID, exhibitionDeviceId: UUID): ExhibitionDevice? {
        return api.findExhibitionDevice(exhibitionId, exhibitionDeviceId)
    }

    /**
     * Lists exhibition Devices
     *
     * @param exhibitionId exhibition id
     * @param exhibitionGroupId exhibition group id
     * @return exhibition Devices
     */
    fun listExhibitionDevices(exhibitionId: UUID, exhibitionGroupId: UUID?): Array<ExhibitionDevice> {
        return api.listExhibitionDevices(exhibitionId, exhibitionGroupId)
    }

    /**
     * Updates exhibition Device
     *
     * @param exhibitionId exhibition id
     * @param body update body
     * @return updated exhibition Device
     */
    fun updateExhibitionDevice(exhibitionId: UUID, body: ExhibitionDevice): ExhibitionDevice? {
        return api.updateExhibitionDevice(exhibitionId, body.id!!, body)
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
            closeableExhibitionDevice.id!!.equals(exhibitionDeviceId)
        }
    }

    /**
     * Asserts exhibitionDevice count within the system
     *
     * @param expected expected count
     * @param exhibitionId exhibition id
     * @param exhibitionGroupId exhibition group id
     */
    fun assertCount(expected: Int, exhibitionId: UUID, exhibitionGroupId: UUID?) {
        assertEquals(expected, api.listExhibitionDevices(exhibitionId, exhibitionGroupId).size)
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
     */
    fun assertListFail(expectedStatus: Int, exhibitionId: UUID, exhibitionGroupId: UUID?) {
        try {
            api.listExhibitionDevices(exhibitionId, exhibitionGroupId)
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
     * @param groupId group
     * @param modelId model
     * @param name name
     * @param screenOrientation screen orientation
     * @param location location
     */
    fun assertCreateFail(expectedStatus: Int, exhibitionId: UUID, groupId: UUID, modelId: UUID, name: String, location: Point?, screenOrientation: ScreenOrientation) {
        try {
            create(exhibitionId, groupId, modelId, name, location, screenOrientation)
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
     * @param body body
     */
    fun assertUpdateFail(expectedStatus: Int, exhibitionId: UUID, body: ExhibitionDevice) {
        try {
            updateExhibitionDevice(exhibitionId, body)
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
        this.getApi().deleteExhibitionDevice(exhibitionDevice.exhibitionId!!, exhibitionDevice.id!!)
    }

    override fun getApi(): ExhibitionDevicesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ExhibitionDevicesApi(TestSettings.apiBasePath)
    }

}