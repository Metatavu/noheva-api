package fi.metatavu.muisti.api.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.muisti.api.client.apis.PageLayoutsApi
import fi.metatavu.muisti.api.client.infrastructure.ApiClient
import fi.metatavu.muisti.api.client.infrastructure.ClientException
import fi.metatavu.muisti.api.client.models.PageLayout
import fi.metatavu.muisti.api.client.models.PageLayoutView
import fi.metatavu.muisti.api.client.models.PageLayoutViewProperty
import fi.metatavu.muisti.api.client.models.ScreenOrientation
import fi.metatavu.muisti.api.test.functional.impl.ApiTestBuilderResource
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
     * Creates new exhibition page layout with default values
     *
     * @return created exhibition page layout
     */
    fun create(): PageLayout {
        val properties: Array<PageLayoutViewProperty> = arrayOf()
        val children: Array<PageLayoutView> = arrayOf()
        return create(PageLayout(
            name = "default page layout",
            data = PageLayoutView("defaultid", "TextView", properties, children),
            screenOrientation = ScreenOrientation.portrait
        ))
    }

    /**
     * Creates new exhibition page layout
     *
     * @param payload payload
     * @return created exhibition page layout
     */
    fun create(payload: PageLayout): PageLayout {
        val result: PageLayout = this.getApi().createPageLayout(payload)
        addClosable(result)
        return result
    }

    /**
     * Finds exhibition page layout
     *
     * @param pageLayoutId exhibition page layout
     * @return exhibition page layout
     */
    fun findPageLayout(pageLayoutId: UUID): PageLayout? {
        return api.findPageLayout(pageLayoutId)
    }

    /**
     * Lists exhibition page layouts
     *
     * @return exhibition page layouts
     */
    fun listPageLayouts(): Array<PageLayout> {
        return api.listPageLayouts()
    }

    /**
     * Updates exhibition page layout
     *
     * @param body update body
     * @return updated exhibition page layout
     */
    fun updatePageLayout(body: PageLayout): PageLayout? {
        return api.updatePageLayout(body.id!!, body)
    }

    /**
     * Deletes a page layout from the API
     *
     * @param pageLayout page layout to be deleted
     */
    fun delete(pageLayout: PageLayout) {
        delete(pageLayout.id!!)
    }

    /**
     * Deletes a page layout from the API
     *
     * @param pageLayoutId page layout id to be deleted
     */
    fun delete(pageLayoutId: UUID) {
        api.deletePageLayout(pageLayoutId)
        removeCloseable { closable: Any ->
            if (closable !is PageLayout) {
                return@removeCloseable false
            }

            val closeablePageLayout: PageLayout = closable
            closeablePageLayout.id!!.equals(pageLayoutId)
        }
    }

    /**
     * Asserts page layout count within the system
     *
     * @param expected expected count
     */
    fun assertCount(expected: Int) {
        assertEquals(expected, api.listPageLayouts().size)
    }

    /**
     * Asserts find fails with given status
     *
     * @param expectedStatus expected status code
     * @param pageLayoutId pageLayout id
     */
    fun assertFindFailStatus(expectedStatus: Int, pageLayoutId: UUID) {
        assertFindFailStatus(expectedStatus, pageLayoutId)
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status
     * @param pageLayoutId pageLayout id
     */
    fun assertFindFail(expectedStatus: Int, pageLayoutId: UUID) {
        try {
            api.findPageLayout(pageLayoutId)
            fail(String.format("Expected find to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts list status fails with given status code
     *
     * @param expectedStatus expected status
     */
    fun assertListFail(expectedStatus: Int) {
        try {
            api.listPageLayouts()
            fail(String.format("Expected list to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts create fails with given status
     *
     * @param expectedStatus expected status
     * @param payload payload
     */
    fun assertCreateFail(expectedStatus: Int, payload: PageLayout) {
        try {
            create(payload)
            fail(String.format("Expected create to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts update fails with given status
     *
     * @param expectedStatus expected status
     * @param body body
     */
    fun assertUpdateFail(expectedStatus: Int, body: PageLayout) {
        try {
            updatePageLayout(body)
            fail(String.format("Expected update to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    /**
     * Asserts delete fails with given status
     *
     * @param expectedStatus expected status
     * @param id id
     */
    fun assertDeleteFail(expectedStatus: Int, id: UUID) {
        try {
            delete(id)
            fail(String.format("Expected delete to fail with message %d", expectedStatus))
        } catch (e: ClientException) {
            assertClientExceptionStatus(expectedStatus, e)
        }
    }

    override fun clean(pageLayout: PageLayout) {
        this.getApi().deletePageLayout(pageLayout.id!!)
    }

    override fun getApi(): PageLayoutsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return PageLayoutsApi(TestSettings.apiBasePath)
    }

}