package fi.metatavu.muisti.api.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.muisti.api.client.apis.ExhibitionDeviceGroupsApi
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.client.infrastructure.ClientException
import fi.metatavu.muisti.api.client.models.ExhibitionDeviceGroup
import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Test builder resource for handling exhibitionDeviceGroups
 */
class ExhibitionDeviceGroupTestBuilderResource(testBuilder: AbstractTestBuilder<ApiClient?>?, val accessTokenProvider: AccessTokenProvider?, apiClient: ApiClient) : ApiTestBuilderResource<ExhibitionDeviceGroup, ApiClient?>(testBuilder, apiClient) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates new exhibition DeviceGroup with default values
     *
     * @param exhibitionId
     * @return created exhibition DeviceGroup
     */
    fun create(exhibitionId: UUID): ExhibitionDeviceGroup {
        return create(exhibitionId, "default deviceGroup")
    }

    /**
     * Creates new exhibition DeviceGroup
     *
     * @param exhibitionId exhibition id
     * @param name name
     * @return created exhibition DeviceGroup
     */
    fun create(exhibitionId: UUID, name: String): ExhibitionDeviceGroup {
        val payload = ExhibitionDeviceGroup(name, null, exhibitionId)
        val result: ExhibitionDeviceGroup = this.getApi().createExhibitionDeviceGroup(exhibitionId, payload)
        addClosable(result)
        return result
    }

    /**
     * Finds exhibition DeviceGroup
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceGroupId exhibition DeviceGroup id
     * @return exhibition DeviceGroup
     */
    fun findExhibitionDeviceGroup(exhibitionId: UUID, exhibitionDeviceGroupId: UUID): ExhibitionDeviceGroup? {
        return api.findExhibitionDeviceGroup(exhibitionId, exhibitionDeviceGroupId)
    }

    /**
     * Lists exhibition DeviceGroups
     *
     * @param exhibitionId exhibition id
     * @return exhibition DeviceGroups
     */
    fun listExhibitionDeviceGroups(exhibitionId: UUID): Array<ExhibitionDeviceGroup> {
        return api.listExhibitionDeviceGroups(exhibitionId)
    }

    /**
     * Updates exhibition DeviceGroup
     *
     * @param exhibitionId exhibition id
     * @param body update body
     * @return updated exhibition DeviceGroup
     */
    fun updateExhibitionDeviceGroup(exhibitionId: UUID, body: ExhibitionDeviceGroup): ExhibitionDeviceGroup? {
        return api.updateExhibitionDeviceGroup(exhibitionId, body.id!!, body)
    }

    /**
     * Deletes a exhibitionDeviceGroup from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceGroup exhibitionDeviceGroup to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionDeviceGroup: ExhibitionDeviceGroup) {
        delete(exhibitionId, exhibitionDeviceGroup.id!!)
    }

    /**
     * Deletes a exhibitionDeviceGroup from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceGroupId exhibitionDeviceGroup id to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionDeviceGroupId: UUID) {
        api.deleteExhibitionDeviceGroup(exhibitionId, exhibitionDeviceGroupId)
        removeCloseable { closable: Any ->
            if (closable !is ExhibitionDeviceGroup) {
                return@removeCloseable false
            }

            val closeableExhibitionDeviceGroup: ExhibitionDeviceGroup = closable
            closeableExhibitionDeviceGroup.id!!.equals(exhibitionDeviceGroupId)
        }
    }

    /**
     * Asserts exhibitionDeviceGroup count within the system
     *
     * @param expected expected count
     * @param exhibitionId exhibition id
     */
    fun assertCount(expected: Int, exhibitionId: UUID) {
        assertEquals(expected, api.listExhibitionDeviceGroups(exhibitionId).size)
    }

    /**
     * Asserts find fails with given status
     *
     * @param expectedStatus expected status code
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceGroupId exhibitionDeviceGroup id
     */
    fun assertFindFailStatus(expectedStatus: Int, exhibitionId: UUID, exhibitionDeviceGroupId: UUID) {
        assertFindFailStatus(expectedStatus, exhibitionId, exhibitionDeviceGroupId)
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceGroupId exhibitionDeviceGroup id
     */
    fun assertFindFail(expectedStatus: Int, exhibitionId: UUID, exhibitionDeviceGroupId: UUID) {
        try {
            api.findExhibitionDeviceGroup(exhibitionId, exhibitionDeviceGroupId)
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
     */
    fun assertListFail(expectedStatus: Int, exhibitionId: UUID) {
        try {
            api.listExhibitionDeviceGroups(exhibitionId)
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
     * @param name name
     */
    fun assertCreateFail(expectedStatus: Int, exhibitionId: UUID, name: String) {
        try {
            create(exhibitionId, name)
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
    fun assertUpdateFail(expectedStatus: Int, exhibitionId: UUID, body: ExhibitionDeviceGroup) {
        try {
            updateExhibitionDeviceGroup(exhibitionId, body)
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

    override fun clean(exhibitionDeviceGroup: ExhibitionDeviceGroup) {
        this.getApi().deleteExhibitionDeviceGroup(exhibitionDeviceGroup.exhibitionId!!, exhibitionDeviceGroup.id!!)
    }

    override fun getApi(): ExhibitionDeviceGroupsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ExhibitionDeviceGroupsApi(TestSettings.apiBasePath)
    }

}