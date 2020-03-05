package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition page API
 *
 * @author Antti Leppä
 */
class ExhibitionPageTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionPage() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val layout = it.admin().pageLayouts().create(exhibitionId)
            val layoutId = layout.id!!
            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibition.id!!, layoutId)
            assertNotNull(createdExhibitionPage)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibitionPage() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val layout = it.admin().pageLayouts().create(exhibitionId)
            val layoutId = layout.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionPageId = UUID.randomUUID()
            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId, layoutId)
            val createdExhibitionPageId = createdExhibitionPage.id!!

            it.admin().exhibitionPages().assertFindFail(404, exhibitionId, nonExistingExhibitionPageId)
            it.admin().exhibitionPages().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionPageId)
            it.admin().exhibitionPages().assertFindFail(404, nonExistingExhibitionId, createdExhibitionPageId)
            assertNotNull(it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId))
        }
    }

    @Test
    fun testListExhibitionPages() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val layout = it.admin().pageLayouts().create(exhibitionId)
            val layoutId = layout.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().exhibitionPages().assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin().exhibitionPages().listExhibitionPages(exhibitionId).size)

            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId, layoutId)
            val createdExhibitionPageId = createdExhibitionPage.id!!
            val exhibitionPage = it.admin().exhibitionPages().listExhibitionPages(exhibitionId)
            assertEquals(1, exhibitionPage.size)
            assertEquals(createdExhibitionPageId, exhibitionPage[0].id)
            it.admin().exhibitionPages().delete(exhibitionId, createdExhibitionPageId)
            assertEquals(0, it.admin().exhibitionPages().listExhibitionPages(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val createLayout = it.admin().pageLayouts().create(exhibitionId)
            val createLayoutId = createLayout.id!!
            val updateLayout = it.admin().pageLayouts().create(exhibitionId)
            val updateLayoutId = updateLayout.id!!

            val navigatePage = it.admin().exhibitionPages().create(exhibitionId, createLayoutId)
            val navigatePageId = navigatePage.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val createResource = ExhibitionPageResource(
                id = "createresid",
                data = "https://example.com/image.png",
                type = ExhibitionPageResourceType.image
            )

            val createEvent = ExhibitionPageEvent(
                type = ExhibitionPageEventType.navigate,
                properties = arrayOf(
                    ExhibitionPageEventProperty(
                        name = "pageId",
                        type = ExhibitionPageEventPropertyType.string,
                        value = navigatePageId.toString()
                    )
                )
            )

            val createEventTrigger = ExhibitionPageEventTrigger(
                events = arrayOf(createEvent),
                clickViewId =  "createviewid",
                delay = 0.0,
                next = arrayOf()
            )

            val createPage = ExhibitionPage(
                layoutId = createLayoutId,
                name = "create page",
                resources = arrayOf(createResource),
                eventTriggers = arrayOf(createEventTrigger)
            )

            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId, createPage)
            val createdExhibitionPageId = createdExhibitionPage.id!!
            val foundCreatedExhibitionPage = it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId)

            assertEquals(createPage.name, createdExhibitionPage.name)
            assertEquals(createPage.layoutId, createdExhibitionPage.layoutId)
            assertJsonsEqual(createdExhibitionPage, foundCreatedExhibitionPage)
            assertJsonsEqual(createPage.eventTriggers, createdExhibitionPage.eventTriggers)
            assertJsonsEqual(createPage.resources, createdExhibitionPage.resources)

            val updateResource = ExhibitionPageResource(
                id = "updateresid",
                data = "https://example.com/updated.png",
                type = ExhibitionPageResourceType.video
            )

            val updateEvent = ExhibitionPageEvent(
                type = ExhibitionPageEventType.hide,
                properties = arrayOf(
                    ExhibitionPageEventProperty(
                        name = "background",
                        type = ExhibitionPageEventPropertyType.color,
                        value = "#fff"
                    )
                )
            )

            val updateEventTrigger = ExhibitionPageEventTrigger(
                events = arrayOf(updateEvent),
                clickViewId =  "updateviewid",
                delay = 2.0,
                next = arrayOf()
            )

            val updatePage = ExhibitionPage(
                id = createdExhibitionPageId,
                layoutId = updateLayoutId,
                name = "update page",
                resources = arrayOf(updateResource),
                eventTriggers = arrayOf(updateEventTrigger)
            )

            val updatedExhibitionPage = it.admin().exhibitionPages().updateExhibitionPage(exhibitionId, updatePage)
            val foundUpdateExhibitionPage = it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId)
            assertJsonsEqual(updatedExhibitionPage?.copy( modifiedAt = foundUpdateExhibitionPage?.modifiedAt ), foundUpdateExhibitionPage)

            assertNotNull(updatedExhibitionPage)
            assertEquals(updatePage.name, updatedExhibitionPage!!.name)
            assertEquals(updatePage.layoutId, updatedExhibitionPage.layoutId)
            assertJsonsEqual(updatedExhibitionPage, updatedExhibitionPage)
            assertJsonsEqual(updatePage.eventTriggers, updatedExhibitionPage.eventTriggers)
            assertJsonsEqual(updatePage.resources, updatedExhibitionPage.resources)

            it.admin().exhibitionPages().assertUpdateFail(404, nonExistingExhibitionId, updatePage)
            it.admin().exhibitionPages().assertUpdateFail(400, exhibitionId, updatePage.copy( layoutId = UUID.randomUUID()))
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val layout = it.admin().pageLayouts().create(exhibitionId)
            val layoutId = layout.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId, layoutId)
            val createdExhibitionPageId = createdExhibitionPage.id!!

            assertNotNull(it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId))
            it.admin().exhibitionPages().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionPages().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionPageId)
            it.admin().exhibitionPages().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionPages().delete(exhibitionId, createdExhibitionPage)

            it.admin().exhibitionPages().assertDeleteFail(404, exhibitionId, createdExhibitionPageId)
        }
    }

}