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
            val layout = it.admin().exhibitionPageLayouts().create(exhibitionId)
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
            val layout = it.admin().exhibitionPageLayouts().create(exhibitionId)
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
            val layout = it.admin().exhibitionPageLayouts().create(exhibitionId)
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
            val createLayout = it.admin().exhibitionPageLayouts().create(exhibitionId)
            val createLayoutId = createLayout.id!!
            val updateLayout = it.admin().exhibitionPageLayouts().create(exhibitionId)
            val updateLayoutId = updateLayout.id!!

            val navigatePage = it.admin().exhibitionPages().create(exhibitionId, createLayoutId)
            val navigatePageId = navigatePage.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val createResource = ExhibitionPageResource("createresid","https://example.com/image.png")
            val createEvent = ExhibitionPageEvent(
                id ="createeventid",
                type = ExhibitionPageEventType.navigate,
                properties = arrayOf(
                    ExhibitionPageEventProperty(
                        name = ExhibitionPageEventPropertyName.navigatePageId,
                        type = ExhibitionPageEventPropertyType.page,
                        value = navigatePageId.toString()
                    )
                )
            )

            val createEventTriggerTimed = ExhibitionPageEventTimedTrigger(
                id = "createtimedeventtriggerid",
                eventId = "createeventid",
                delay = 0.0,
                next = arrayOf()
            )

            val createEventTriggerClick = ExhibitionPageEventClickTrigger(
                id = "createclicktriggerid",
                eventId = "createeventid",
                viewId =  "createviewid"
            )

            val createPage = ExhibitionPage(
                layoutId = createLayoutId,
                name = "create page",
                resources = arrayOf(createResource),
                events = arrayOf(createEvent),
                eventTriggers = ExhibitionPageEventTriggers(
                    click = arrayOf(createEventTriggerClick),
                    timed = arrayOf(createEventTriggerTimed)
                )
            )

            val createdExhibitionPage = it.admin().exhibitionPages().create(exhibitionId, createPage)
            val createdExhibitionPageId = createdExhibitionPage.id!!
            val foundCreatedExhibitionPage = it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId)

            assertEquals(createPage.name, createdExhibitionPage.name)
            assertEquals(createPage.layoutId, createdExhibitionPage.layoutId)
            assertJsonsEqual(createdExhibitionPage, foundCreatedExhibitionPage)
            assertJsonsEqual(createPage.events, createdExhibitionPage.events)
            assertJsonsEqual(createPage.eventTriggers, createdExhibitionPage.eventTriggers)
            assertJsonsEqual(createPage.resources, createdExhibitionPage.resources)

            val updateResource = ExhibitionPageResource("updateresid","https://example.com/updated.png")
            val updateEvent = ExhibitionPageEvent(
                    id ="updateeventid",
                    type = ExhibitionPageEventType.hide,
                    properties = arrayOf(
                            ExhibitionPageEventProperty(
                                    name = ExhibitionPageEventPropertyName.hideViewid,
                                    type = ExhibitionPageEventPropertyType.view,
                                    value = "updateview"
                            )
                    )
            )

            val updateEventTriggerTimed = ExhibitionPageEventTimedTrigger(
                id = "updatetimedeventtriggerid",
                eventId = "updateeventid",
                delay = 1.0,
                next = arrayOf()
            )

            val updateEventTriggerClick = ExhibitionPageEventClickTrigger(
                id = "updateclicktriggerid",
                eventId = "updateeventid",
                viewId =  "updateviewid"
            )

            val updatePage = ExhibitionPage(
                id = createdExhibitionPageId,
                layoutId = updateLayoutId,
                name = "update page",
                resources = arrayOf(updateResource),
                events = arrayOf(updateEvent),
                eventTriggers = ExhibitionPageEventTriggers(
                    click = arrayOf(updateEventTriggerClick),
                    timed = arrayOf(updateEventTriggerTimed)
                )
            )

            val updatedExhibitionPage = it.admin().exhibitionPages().updateExhibitionPage(exhibitionId, updatePage)
            val foundUpdateExhibitionPage = it.admin().exhibitionPages().findExhibitionPage(exhibitionId, createdExhibitionPageId)
            assertJsonsEqual(updatedExhibitionPage?.copy( modifiedAt = foundUpdateExhibitionPage?.modifiedAt ), foundUpdateExhibitionPage)

            assertNotNull(updatedExhibitionPage)
            assertEquals(updatePage.name, updatedExhibitionPage!!.name)
            assertEquals(updatePage.layoutId, updatedExhibitionPage.layoutId)
            assertJsonsEqual(updatedExhibitionPage, updatedExhibitionPage)
            assertJsonsEqual(updatePage.events, updatedExhibitionPage.events)
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
            val layout = it.admin().exhibitionPageLayouts().create(exhibitionId)
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