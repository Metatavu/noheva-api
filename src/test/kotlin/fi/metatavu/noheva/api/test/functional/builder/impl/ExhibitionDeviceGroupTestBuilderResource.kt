package fi.metatavu.noheva.api.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.noheva.api.client.apis.ExhibitionDeviceGroupsApi
import fi.metatavu.noheva.api.client.infrastructure.ApiClient
import fi.metatavu.noheva.api.client.infrastructure.ClientException
import fi.metatavu.noheva.api.client.models.DeviceGroupVisitorSessionStartStrategy
import fi.metatavu.noheva.api.client.models.Exhibition
import fi.metatavu.noheva.api.client.models.ExhibitionDeviceGroup
import fi.metatavu.noheva.api.client.models.ExhibitionRoom
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import fi.metatavu.noheva.api.test.functional.settings.ApiTestSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import java.util.*

/**
 * Test builder resource for handling exhibitionDeviceGroups
 */
class ExhibitionDeviceGroupTestBuilderResource(
    testBuilder: TestBuilder,
    val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<ExhibitionDeviceGroup, ApiClient?>(testBuilder, apiClient) {

    /**
     * Creates new exhibition device group with default values
     *
     * @param exhibitionId exhibition id
     * @param roomId room id
     * @param name name
     * @return created exhibition DeviceGroup
     */
    fun create(exhibitionId: UUID, roomId: UUID, name: String): ExhibitionDeviceGroup {
        return create(
            exhibitionId = exhibitionId,
            sourceDeviceGroupId = null,
            payload = ExhibitionDeviceGroup(
                roomId = roomId,
                name = name,
                allowVisitorSessionCreation = false,
                visitorSessionEndTimeout = 5000,
                visitorSessionStartStrategy = DeviceGroupVisitorSessionStartStrategy.OTHERSBLOCK
            )
        )
    }

    /**
     * Creates new exhibition device group with default values
     *
     * @param exhibition exhibition id
     * @param room room
     * @param name name
     * @return created exhibition DeviceGroup
     */
    fun create(exhibition: Exhibition, room: ExhibitionRoom, name: String): ExhibitionDeviceGroup {
        return create(exhibitionId = exhibition.id!!, roomId = room.id!!, name = name)
    }

    /**
     * Copies device group and returns copied group as response
     *
     * @param exhibitionId exhibition id
     * @param sourceDeviceGroupId source device group id
     * @return copied group
     */
    fun copy(exhibitionId: UUID, sourceDeviceGroupId: UUID?): ExhibitionDeviceGroup {
        return create(
            exhibitionId = exhibitionId,
            sourceDeviceGroupId = sourceDeviceGroupId,
            payload = null
        )
    }

    /**
     * Creates new exhibition DeviceGroup
     *
     * @param exhibitionId exhibition id
     * @param sourceDeviceGroupId source device group
     * @param payload payload
     * @return created exhibition DeviceGroup
     */
    fun create(exhibitionId: UUID, sourceDeviceGroupId: UUID?, payload: ExhibitionDeviceGroup?): ExhibitionDeviceGroup {
        val result: ExhibitionDeviceGroup = api.createExhibitionDeviceGroup(
            exhibitionId = exhibitionId,
            sourceDeviceGroupId = sourceDeviceGroupId,
            exhibitionDeviceGroup = payload
        )

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
    fun findExhibitionDeviceGroup(exhibitionId: UUID, exhibitionDeviceGroupId: UUID): ExhibitionDeviceGroup {
        return api.findExhibitionDeviceGroup(exhibitionId, exhibitionDeviceGroupId)
    }

    /**
     * Lists exhibition DeviceGroups
     *
     * @param exhibitionId exhibition id
     * @param roomId filter by room id. Ignored if null
     * @return exhibition DeviceGroups
     */
    fun listExhibitionDeviceGroups(exhibitionId: UUID, roomId: UUID?): Array<ExhibitionDeviceGroup> {
        return api.listExhibitionDeviceGroups(exhibitionId = exhibitionId, roomId = roomId)
    }

    /**
     * Updates exhibition DeviceGroup
     *
     * @param exhibitionId exhibition id
     * @param body update body
     * @return updated exhibition DeviceGroup
     */
    fun updateExhibitionDeviceGroup(exhibitionId: UUID, body: ExhibitionDeviceGroup): ExhibitionDeviceGroup {
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
            closeableExhibitionDeviceGroup.id!! == exhibitionDeviceGroupId
        }
    }

    /**
     * Asserts exhibitionDeviceGroup count within the system
     *
     * @param expected expected count
     * @param exhibitionId exhibition id
     * @param roomId filter by room id. Ignored if null
     */
    fun assertCount(expected: Int, exhibitionId: UUID, roomId: UUID?) {
        assertEquals(expected, api.listExhibitionDeviceGroups(exhibitionId = exhibitionId, roomId = roomId).size)
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
     * @param roomId filter by room id. Ignored if null
     */
    fun assertListFail(expectedStatus: Int, exhibitionId: UUID, roomId: UUID?) {
        try {
            api.listExhibitionDeviceGroups(exhibitionId = exhibitionId, roomId = roomId)
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
     * @param sourceDeviceGroupId source device group id
     * @param payload payload
     */
    fun assertCreateFail(
        expectedStatus: Int,
        exhibitionId: UUID,
        sourceDeviceGroupId: UUID?,
        payload: ExhibitionDeviceGroup?
    ) {
        try {
            create(
                exhibitionId = exhibitionId,
                sourceDeviceGroupId = sourceDeviceGroupId,
                payload = payload
            )
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
        api.deleteExhibitionDeviceGroup(exhibitionDeviceGroup.exhibitionId!!, exhibitionDeviceGroup.id!!)
    }

    override fun getApi(): ExhibitionDeviceGroupsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ExhibitionDeviceGroupsApi(ApiTestSettings.apiBasePath)
    }

}