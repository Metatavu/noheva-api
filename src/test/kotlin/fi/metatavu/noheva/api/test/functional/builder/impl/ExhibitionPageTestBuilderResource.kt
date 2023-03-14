package fi.metatavu.noheva.api.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.noheva.api.client.apis.ExhibitionPagesApi
import fi.metatavu.noheva.api.client.infrastructure.ApiClient
import fi.metatavu.noheva.api.client.infrastructure.ClientException
import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.builder.TestBuilder
import fi.metatavu.noheva.api.test.functional.settings.ApiTestSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import java.util.*


/**
 * Test builder resource for handling exhibitionPages
 */
class ExhibitionPageTestBuilderResource(
    testBuilder: TestBuilder,
    val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<ExhibitionPage, ApiClient?>(testBuilder, apiClient) {

    /**
     * Creates new exhibition page with default values
     *
     * @param exhibitionId exhibition id
     * @param layoutId layout id
     * @param contentVersionId content version id
     * @return created exhibition page
     */
    fun create(exhibitionId: UUID, layoutId: UUID, deviceId: UUID, contentVersionId: UUID): ExhibitionPage {
        return create(
            exhibitionId, ExhibitionPage(
                layoutId = layoutId,
                deviceId = deviceId,
                contentVersionId = contentVersionId,
                orderNumber = 0,
                name = "default page",
                enterTransitions = arrayOf(),
                exitTransitions = arrayOf(),
                resources = arrayOf(),
                eventTriggers = arrayOf()
            )
        )
    }

    /**
     * Creates new exhibition page with default values
     *
     * @param exhibition exhibition
     * @param layout layout
     * @param contentVersion content version
     * @return created exhibition page
     */
    fun create(
        exhibition: Exhibition,
        layout: PageLayout,
        device: ExhibitionDevice,
        contentVersion: ContentVersion
    ): ExhibitionPage {
        return create(
            exhibitionId = exhibition.id!!,
            layoutId = layout.id!!,
            deviceId = device.id!!,
            contentVersionId = contentVersion.id!!
        )
    }

    /**
     * Creates new exhibition page
     *
     * @param exhibitionId exhibition id
     * @param payload payload
     * @return created exhibition page
     */
    fun create(exhibitionId: UUID, payload: ExhibitionPage): ExhibitionPage {
        val result: ExhibitionPage = api.createExhibitionPage(exhibitionId, payload)
        addClosable(result)
        return result
    }

    /**
     * Finds exhibition Page
     *
     * @param exhibitionId exhibition id
     * @param exhibitionPageId exhibition Page id
     * @return exhibition Page
     */
    fun findExhibitionPage(exhibitionId: UUID, exhibitionPageId: UUID): ExhibitionPage {
        return api.findExhibitionPage(exhibitionId, exhibitionPageId)
    }

    /**
     * Lists exhibition Pages
     *
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceId exhibition device id
     * @param contentVersionId exhibition content version id
     * @param pageLayoutId page layout id
     * @return exhibition Pages
     */
    fun listExhibitionPages(
        exhibitionId: UUID,
        exhibitionDeviceId: UUID?,
        contentVersionId: UUID?,
        pageLayoutId: UUID?
    ): Array<ExhibitionPage> {
        return api.listExhibitionPages(
            exhibitionId = exhibitionId,
            exhibitionDeviceId = exhibitionDeviceId,
            contentVersionId = contentVersionId,
            pageLayoutId = pageLayoutId
        )
    }

    /**
     * Updates exhibition Page
     *
     * @param exhibitionId exhibition id
     * @param body update body
     * @return updated exhibition Page
     */
    fun updateExhibitionPage(exhibitionId: UUID, body: ExhibitionPage): ExhibitionPage {
        return api.updateExhibitionPage(exhibitionId, body.id!!, body)
    }

    /**
     * Deletes a exhibitionPage from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionPage exhibitionPage to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionPage: ExhibitionPage) {
        delete(exhibitionId, exhibitionPage.id!!)
    }

    /**
     * Deletes a exhibitionPage from the API
     *
     * @param exhibitionId exhibition id
     * @param exhibitionPageId exhibitionPage id to be deleted
     */
    fun delete(exhibitionId: UUID, exhibitionPageId: UUID) {
        api.deleteExhibitionPage(exhibitionId, exhibitionPageId)
        removeCloseable { closable: Any ->
            if (closable !is ExhibitionPage) {
                return@removeCloseable false
            }

            val closeableExhibitionPage: ExhibitionPage = closable
            closeableExhibitionPage.id!! == exhibitionPageId
        }
    }

    /**
     * Asserts exhibitionPage count within the system
     *
     * @param expected expected count
     * @param exhibitionId exhibition id
     * @param exhibitionDeviceId exhibition device id
     * @param contentVersionId exhibition content version id
     * @param pageLayoutId page layout id
     */
    fun assertCount(
        expected: Int,
        exhibitionId: UUID,
        exhibitionDeviceId: UUID?,
        contentVersionId: UUID?,
        pageLayoutId: UUID?
    ) {
        assertEquals(
            expected, api.listExhibitionPages(
                exhibitionId = exhibitionId,
                exhibitionDeviceId = exhibitionDeviceId,
                contentVersionId = contentVersionId,
                pageLayoutId = pageLayoutId
            ).size
        )
    }

    /**
     * Asserts find fails with given status
     *
     * @param expectedStatus expected status code
     * @param exhibitionId exhibition id
     * @param exhibitionPageId exhibitionPage id
     */
    fun assertFindFailStatus(expectedStatus: Int, exhibitionId: UUID, exhibitionPageId: UUID) {
        assertFindFailStatus(expectedStatus, exhibitionId, exhibitionPageId)
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status
     * @param exhibitionId exhibition id
     * @param exhibitionPageId exhibitionPage id
     */
    fun assertFindFail(expectedStatus: Int, exhibitionId: UUID, exhibitionPageId: UUID) {
        try {
            api.findExhibitionPage(exhibitionId, exhibitionPageId)
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
     * @param exhibitionDeviceId exhibition device id
     * @param contentVersionId exhibition content version id
     * @param pageLayoutId page layout id
     */
    fun assertListFail(
        expectedStatus: Int,
        exhibitionId: UUID,
        exhibitionDeviceId: UUID?,
        contentVersionId: UUID?,
        pageLayoutId: UUID?
    ) {
        try {
            api.listExhibitionPages(
                exhibitionId = exhibitionId,
                exhibitionDeviceId = exhibitionDeviceId,
                contentVersionId = contentVersionId,
                pageLayoutId = pageLayoutId
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
    fun assertCreateFail(expectedStatus: Int, exhibitionId: UUID, payload: ExhibitionPage) {
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
    fun assertUpdateFail(expectedStatus: Int, exhibitionId: UUID, body: ExhibitionPage) {
        try {
            updateExhibitionPage(exhibitionId, body)
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

    override fun clean(exhibitionPage: ExhibitionPage) {
        api.deleteExhibitionPage(exhibitionPage.exhibitionId!!, exhibitionPage.id!!)
    }

    override fun getApi(): ExhibitionPagesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ExhibitionPagesApi(ApiTestSettings.apiBasePath)
    }

}