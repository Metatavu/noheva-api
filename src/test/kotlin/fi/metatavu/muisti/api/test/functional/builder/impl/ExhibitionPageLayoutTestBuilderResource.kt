package fi.metatavu.muisti.api.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.muisti.api.client.apis.ExhibitionPageLayoutsApi
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.client.infrastructure.ClientException
import fi.metatavu.muisti.api.client.models.ExhibitionPageLayout
import fi.metatavu.muisti.api.client.models.ExhibitionPageLayoutView
import fi.metatavu.muisti.api.client.models.ExhibitionPageLayoutViewProperty
import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Test builder resource for handling exhibitionPageLayouts
 */
class ExhibitionPageLayoutTestBuilderResource(testBuilder: AbstractTestBuilder<ApiClient?>?, val accessTokenProvider: AccessTokenProvider?, apiClient: ApiClient) : ApiTestBuilderResource<ExhibitionPageLayout, ApiClient?>(testBuilder, apiClient) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates new exhibition PageLayout with default values
     *
     * @param exhibitionId
     * @return created exhibition PageLayout
     */
    fun create(exhibitionId: UUID): ExhibitionPageLayout {
        val properties: Array<ExhibitionPageLayoutViewProperty> = arrayOf()
        val children: Array<ExhibitionPageLayoutView> = arrayOf()
        return create(exhibitionId, "default page layout", ExhibitionPageLayoutView(UUID.randomUUID(), "TextView", properties, children))
    }

    /**
     * Creates new exhibition PageLayout
     *
     * @param exhibitionId exhibition id
     * @param name name
     * @param data data
     * @return created exhibition PageLayout
     */
    fun create(exhibitionId: UUID, name: String, data: ExhibitionPageLayoutView): ExhibitionPageLayout {
        val result: ExhibitionPageLayout = this.getApi().createExhibitionPageLayout(exhibitionId, ExhibitionPageLayout(name, data))
        addClosable(result)
        return result
    }

    /**
     * Finds exhibition PageLayout
     *
     * @param exhibitionId exhibition id
     * @param exhibitionPageLayoutId exhibition PageLayout id
     * @return exhibition PageLayout
     */
    fun findExhibitionPageLayout(exhibitionId: UUID, exhibitionPageLayoutId: UUID): ExhibitionPageLayout? {
        return api.findExhibitionPageLayout(exhibitionId, exhibitionPageLayoutId)
    }

    /**
     * Lists exhibition PageLayouts
     *
     * @param exhibitionId exhibition id
     * @return exhibition PageLayouts
     */
    fun listExhibitionPageLayouts(exhibitionId: UUID): Array<ExhibitionPageLayout> {
        return api.listExhibitionPageLayouts(exhibitionId)
    }

    /**
     * Updates exhibition PageLayout
     *
     * @param exhibitionId exhibition id
     * @param body update body
     * @return updated exhibition PageLayout
     */
    fun updateExhibitionPageLayout(exhibitionId: UUID, body: ExhibitionPageLayout): ExhibitionPageLayout? {
        return api.updateExhibitionPageLayout(exhibitionId, body.id!!, body)
    }

    /**
     * Deletes a exhibitionPageLayout from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionPageLayout exhibitionPageLayout to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionPageLayout: ExhibitionPageLayout) {
        delete(exhibitionId, exhibitionPageLayout.id!!)
    }

    /**
     * Deletes a exhibitionPageLayout from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionPageLayoutId exhibitionPageLayout id to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionPageLayoutId: UUID) {
        api.deleteExhibitionPageLayout(exhibitionId, exhibitionPageLayoutId)
        removeCloseable { closable: Any ->
            if (closable !is ExhibitionPageLayout) {
                return@removeCloseable false
            }

            val closeableExhibitionPageLayout: ExhibitionPageLayout = closable
            closeableExhibitionPageLayout.id!!.equals(exhibitionPageLayoutId)
        }
    }

    /**
     * Asserts exhibitionPageLayout count within the system
     *
     * @param expected expected count
     * @param exhibitionId exhibition id
     */
    fun assertCount(expected: Int, exhibitionId: UUID) {
        assertEquals(expected, api.listExhibitionPageLayouts(exhibitionId).size)
    }

    /**
     * Asserts find fails with given status
     *
     * @param expectedStatus expected status code
     * @param exhibitionId exhibition id
     * @param exhibitionPageLayoutId exhibitionPageLayout id
     */
    fun assertFindFailStatus(expectedStatus: Int, exhibitionId: UUID, exhibitionPageLayoutId: UUID) {
        assertFindFailStatus(expectedStatus, exhibitionId, exhibitionPageLayoutId)
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param exhibitionPageLayoutId exhibitionPageLayout id
     */
    fun assertFindFail(expectedStatus: Int, exhibitionId: UUID, exhibitionPageLayoutId: UUID) {
        try {
            api.findExhibitionPageLayout(exhibitionId, exhibitionPageLayoutId)
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
            api.listExhibitionPageLayouts(exhibitionId)
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
     * @param data data
     */
    fun assertCreateFail(expectedStatus: Int, exhibitionId: UUID, name: String, data: ExhibitionPageLayoutView) {
        try {
            create(exhibitionId, name, data)
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
    fun assertUpdateFail(expectedStatus: Int, exhibitionId: UUID, body: ExhibitionPageLayout) {
        try {
            updateExhibitionPageLayout(exhibitionId, body)
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

    override fun clean(exhibitionPageLayout: ExhibitionPageLayout) {
        this.getApi().deleteExhibitionPageLayout(exhibitionPageLayout.exhibitionId!!, exhibitionPageLayout.id!!)
    }

    override fun getApi(): ExhibitionPageLayoutsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ExhibitionPageLayoutsApi(TestSettings.apiBasePath)
    }

}