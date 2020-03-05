package fi.metatavu.muisti.api.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.muisti.api.client.apis.PageLayoutsApi
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.client.infrastructure.ClientException
import fi.metatavu.muisti.api.client.models.PageLayout
import fi.metatavu.muisti.api.client.models.PageLayoutView
import fi.metatavu.muisti.api.client.models.PageLayoutViewProperty
import fi.metatavu.muisti.api.test.functional.settings.TestSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Test builder resource for handling pageLayouts
 */
class PageLayoutTestBuilderResource(testBuilder: AbstractTestBuilder<ApiClient?>?, val accessTokenProvider: AccessTokenProvider?, apiClient: ApiClient) : ApiTestBuilderResource<PageLayout, ApiClient?>(testBuilder, apiClient) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates new exhibition PageLayout with default values
     *
     * @param exhibitionId
     * @return created exhibition PageLayout
     */
    fun create(exhibitionId: UUID): PageLayout {
        val properties: Array<PageLayoutViewProperty> = arrayOf()
        val children: Array<PageLayoutView> = arrayOf()
        return create(exhibitionId, "default page layout", PageLayoutView("defaultid", "TextView", properties, children))
    }

    /**
     * Creates new exhibition PageLayout
     *
     * @param exhibitionId exhibition id
     * @param name name
     * @param data data
     * @return created exhibition PageLayout
     */
    fun create(exhibitionId: UUID, name: String, data: PageLayoutView): PageLayout {
        val result: PageLayout = this.getApi().createPageLayout(exhibitionId, PageLayout(name, data))
        addClosable(result)
        return result
    }

    /**
     * Finds exhibition PageLayout
     *
     * @param exhibitionId exhibition id
     * @param pageLayoutId exhibition PageLayout id
     * @return exhibition PageLayout
     */
    fun findPageLayout(exhibitionId: UUID, pageLayoutId: UUID): PageLayout? {
        return api.findPageLayout(exhibitionId, pageLayoutId)
    }

    /**
     * Lists exhibition PageLayouts
     *
     * @param exhibitionId exhibition id
     * @return exhibition PageLayouts
     */
    fun listPageLayouts(exhibitionId: UUID): Array<PageLayout> {
        return api.listPageLayouts(exhibitionId)
    }

    /**
     * Updates exhibition PageLayout
     *
     * @param exhibitionId exhibition id
     * @param body update body
     * @return updated exhibition PageLayout
     */
    fun updatePageLayout(exhibitionId: UUID, body: PageLayout): PageLayout? {
        return api.updatePageLayout(exhibitionId, body.id!!, body)
    }

    /**
     * Deletes a pageLayout from the API
     *
     * @param exhibitionId exhibition id
     * @param pageLayout pageLayout to be deleted
     */
    fun delete(exhibitionId: UUID, pageLayout: PageLayout) {
        delete(exhibitionId, pageLayout.id!!)
    }

    /**
     * Deletes a pageLayout from the API
     *
     * @param exhibitionId exhibition id
     * @param pageLayoutId pageLayout id to be deleted
     */
    fun delete(exhibitionId: UUID, pageLayoutId: UUID) {
        api.deletePageLayout(exhibitionId, pageLayoutId)
        removeCloseable { closable: Any ->
            if (closable !is PageLayout) {
                return@removeCloseable false
            }

            val closeablePageLayout: PageLayout = closable
            closeablePageLayout.id!!.equals(pageLayoutId)
        }
    }

    /**
     * Asserts pageLayout count within the system
     *
     * @param expected expected count
     * @param exhibitionId exhibition id
     */
    fun assertCount(expected: Int, exhibitionId: UUID) {
        assertEquals(expected, api.listPageLayouts(exhibitionId).size)
    }

    /**
     * Asserts find fails with given status
     *
     * @param expectedStatus expected status code
     * @param exhibitionId exhibition id
     * @param pageLayoutId pageLayout id
     */
    fun assertFindFailStatus(expectedStatus: Int, exhibitionId: UUID, pageLayoutId: UUID) {
        assertFindFailStatus(expectedStatus, exhibitionId, pageLayoutId)
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param pageLayoutId pageLayout id
     */
    fun assertFindFail(expectedStatus: Int, exhibitionId: UUID, pageLayoutId: UUID) {
        try {
            api.findPageLayout(exhibitionId, pageLayoutId)
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
            api.listPageLayouts(exhibitionId)
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
    fun assertCreateFail(expectedStatus: Int, exhibitionId: UUID, name: String, data: PageLayoutView) {
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
    fun assertUpdateFail(expectedStatus: Int, exhibitionId: UUID, body: PageLayout) {
        try {
            updatePageLayout(exhibitionId, body)
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

    override fun clean(pageLayout: PageLayout) {
        this.getApi().deletePageLayout(pageLayout.exhibitionId!!, pageLayout.id!!)
    }

    override fun getApi(): PageLayoutsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return PageLayoutsApi(TestSettings.apiBasePath)
    }

}