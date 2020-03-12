package fi.metatavu.muisti.api.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.muisti.api.client.apis.ExhibitionDeviceModelsApi
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.client.infrastructure.ClientException
import fi.metatavu.muisti.api.client.models.ExhibitionDeviceModel
import fi.metatavu.muisti.api.client.models.ExhibitionDeviceModelCapabilities
import fi.metatavu.muisti.api.client.models.ExhibitionDeviceModelDimensions
import fi.metatavu.muisti.api.client.models.ExhibitionDeviceModelDisplayMetrics
import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Test builder resource for handling exhibitionDeviceModels
 */
class ExhibitionDeviceModelTestBuilderResource(testBuilder: AbstractTestBuilder<ApiClient?>?, val accessTokenProvider: AccessTokenProvider?, apiClient: ApiClient) : ApiTestBuilderResource<ExhibitionDeviceModel, ApiClient?>(testBuilder, apiClient) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates new exhibition DeviceModel with default values
     *
     * @param exhibitionId
     * @return created exhibition DeviceModel
     */
    fun create(exhibitionId: UUID): ExhibitionDeviceModel {
        return create(exhibitionId, ExhibitionDeviceModel(
            manufacturer = "default manufacturer",
            model = "default model",
            dimensions = ExhibitionDeviceModelDimensions(),
            displayMetrics = ExhibitionDeviceModelDisplayMetrics(),
            capabilities = ExhibitionDeviceModelCapabilities( touch = true)
        ))
    }

    /**
     * Creates new exhibition DeviceModel
     *
     * @param exhibitionId exhibition id
     * @param payload payload
     * @return created exhibition DeviceModel
     */
    fun create(exhibitionId: UUID, payload: ExhibitionDeviceModel): ExhibitionDeviceModel {
        val result: ExhibitionDeviceModel = this.getApi().createExhibitionDeviceModel(exhibitionId, payload)
        addClosable(result)
        return result
    }

    /**
     * Finds exhibition DeviceModel
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceModelId exhibition DeviceModel id
     * @return exhibition DeviceModel
     */
    fun findExhibitionDeviceModel(exhibitionId: UUID, exhibitionDeviceModelId: UUID): ExhibitionDeviceModel? {
        return api.findExhibitionDeviceModel(exhibitionId, exhibitionDeviceModelId)
    }

    /**
     * Lists exhibition DeviceModels
     *
     * @param exhibitionId exhibition id
     * @return exhibition DeviceModels
     */
    fun listExhibitionDeviceModels(exhibitionId: UUID): Array<ExhibitionDeviceModel> {
        return api.listExhibitionDeviceModels(exhibitionId)
    }

    /**
     * Updates exhibition DeviceModel
     *
     * @param exhibitionId exhibition id
     * @param body update body
     * @return updated exhibition DeviceModel
     */
    fun updateExhibitionDeviceModel(exhibitionId: UUID, body: ExhibitionDeviceModel): ExhibitionDeviceModel? {
        return api.updateExhibitionDeviceModel(exhibitionId, body.id!!, body)
    }

    /**
     * Deletes a exhibitionDeviceModel from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceModel exhibitionDeviceModel to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionDeviceModel: ExhibitionDeviceModel) {
        delete(exhibitionId, exhibitionDeviceModel.id!!)
    }

    /**
     * Deletes a exhibitionDeviceModel from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceModelId exhibitionDeviceModel id to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionDeviceModelId: UUID) {
        api.deleteExhibitionDeviceModel(exhibitionId, exhibitionDeviceModelId)
        removeCloseable { closable: Any ->
            if (closable !is ExhibitionDeviceModel) {
                return@removeCloseable false
            }

            val closeableExhibitionDeviceModel: ExhibitionDeviceModel = closable
            closeableExhibitionDeviceModel.id!!.equals(exhibitionDeviceModelId)
        }
    }

    /**
     * Asserts exhibitionDeviceModel count within the system
     *
     * @param expected expected count
     * @param exhibitionId exhibition id
     */
    fun assertCount(expected: Int, exhibitionId: UUID) {
        assertEquals(expected, api.listExhibitionDeviceModels(exhibitionId).size)
    }

    /**
     * Asserts find fails with given status
     *
     * @param expectedStatus expected status code
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceModelId exhibitionDeviceModel id
     */
    fun assertFindFailStatus(expectedStatus: Int, exhibitionId: UUID, exhibitionDeviceModelId: UUID) {
        assertFindFailStatus(expectedStatus, exhibitionId, exhibitionDeviceModelId)
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceModelId exhibitionDeviceModel id
     */
    fun assertFindFail(expectedStatus: Int, exhibitionId: UUID, exhibitionDeviceModelId: UUID) {
        try {
            api.findExhibitionDeviceModel(exhibitionId, exhibitionDeviceModelId)
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
            api.listExhibitionDeviceModels(exhibitionId)
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
     * @return created exhibition DeviceModel
     */
    fun assertCreateFail(expectedStatus: Int, exhibitionId: UUID, payload: ExhibitionDeviceModel) {
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
     * @param body body
     */
    fun assertUpdateFail(expectedStatus: Int, exhibitionId: UUID, body: ExhibitionDeviceModel) {
        try {
            updateExhibitionDeviceModel(exhibitionId, body)
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

    override fun clean(exhibitionDeviceModel: ExhibitionDeviceModel) {
        this.getApi().deleteExhibitionDeviceModel(exhibitionDeviceModel.exhibitionId!!, exhibitionDeviceModel.id!!)
    }

    override fun getApi(): ExhibitionDeviceModelsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ExhibitionDeviceModelsApi(TestSettings.apiBasePath)
    }

}