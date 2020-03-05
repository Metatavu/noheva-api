package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.PageLayout
import fi.metatavu.muisti.api.client.models.PageLayoutView
import fi.metatavu.muisti.api.client.models.PageLayoutViewProperty
import fi.metatavu.muisti.api.client.models.PageLayoutViewPropertyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition pageLayout API
 *
 * @author Antti Leppä
 */
class PageLayoutTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreatePageLayout() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val createdPageLayout = it.admin().pageLayouts().create(exhibition.id!!)
            assertNotNull(createdPageLayout)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindPageLayout() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingPageLayoutId = UUID.randomUUID()
            val createdPageLayout = it.admin().pageLayouts().create(exhibitionId)
            val createdPageLayoutId = createdPageLayout.id!!

            it.admin().pageLayouts().assertFindFail(404, exhibitionId, nonExistingPageLayoutId)
            it.admin().pageLayouts().assertFindFail(404, nonExistingExhibitionId, nonExistingPageLayoutId)
            it.admin().pageLayouts().assertFindFail(404, nonExistingExhibitionId, createdPageLayoutId)
            assertNotNull(it.admin().pageLayouts().findPageLayout(exhibitionId, createdPageLayoutId))
        }
    }

    @Test
    fun testListPageLayouts() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().pageLayouts().assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin().pageLayouts().listPageLayouts(exhibitionId).size)

            val createdPageLayout = it.admin().pageLayouts().create(exhibitionId)
            val createdPageLayoutId = createdPageLayout.id!!
            val pageLayout = it.admin().pageLayouts().listPageLayouts(exhibitionId)
            assertEquals(1, pageLayout.size)
            assertEquals(createdPageLayoutId, pageLayout[0].id)
            it.admin().pageLayouts().delete(exhibitionId, createdPageLayoutId)
            assertEquals(0, it.admin().pageLayouts().listPageLayouts(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.boolean))
            val createdChildren = arrayOf(PageLayoutView("childid", "child", arrayOf(), arrayOf()))
            val createdData = PageLayoutView("rootid", "created widget", createdProperties, createdChildren)

            val createdPageLayout = it.admin().pageLayouts().create(exhibitionId, "created name", createdData)
            val createdPageLayoutId = createdPageLayout.id!!

            val foundCreatedPageLayout = it.admin().pageLayouts().findPageLayout(exhibitionId, createdPageLayoutId)
            assertEquals(createdPageLayout.id, foundCreatedPageLayout?.id)
            assertEquals("created name", createdPageLayout.name)
            assertEquals("created widget", createdPageLayout.data.widget)
            assertEquals(1, createdPageLayout.data.properties.size)
            assertEquals("name", createdPageLayout.data.properties[0].name)
            assertEquals("true", createdPageLayout.data.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.boolean, createdPageLayout.data.properties[0].type)
            assertEquals(1, createdPageLayout.data.children.size)
            assertEquals(createdChildren[0].id, createdPageLayout.data.children[0].id)


            val updatedProperties = arrayOf(PageLayoutViewProperty("uname", "str", PageLayoutViewPropertyType.string))
            val updatedChildren = arrayOf<PageLayoutView>()
            val updatedData = PageLayoutView("updatedid", "updated widget", updatedProperties, updatedChildren)

            val updatedPageLayout = it.admin().pageLayouts().updatePageLayout(exhibitionId, PageLayout("updated name", updatedData, createdPageLayoutId))
            val foundUpdatedPageLayout = it.admin().pageLayouts().findPageLayout(exhibitionId, createdPageLayoutId)

            assertEquals(updatedPageLayout!!.id, foundUpdatedPageLayout?.id)
            assertEquals("updated name", updatedPageLayout.name)
            assertEquals("updated widget", updatedPageLayout.data.widget)
            assertEquals(1, updatedPageLayout.data.properties.size)
            assertEquals("uname", updatedPageLayout.data.properties[0].name)
            assertEquals("str", updatedPageLayout.data.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.string, updatedPageLayout.data.properties[0].type)
            assertEquals(0, updatedPageLayout.data.children.size)

            it.admin().pageLayouts().assertUpdateFail(404, nonExistingExhibitionId, PageLayout("name", updatedData, createdPageLayoutId))
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdPageLayout = it.admin().pageLayouts().create(exhibitionId)
            val createdPageLayoutId = createdPageLayout.id!!

            assertNotNull(it.admin().pageLayouts().findPageLayout(exhibitionId, createdPageLayoutId))
            it.admin().pageLayouts().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().pageLayouts().assertDeleteFail(404, nonExistingExhibitionId, createdPageLayoutId)
            it.admin().pageLayouts().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().pageLayouts().delete(exhibitionId, createdPageLayout)

            it.admin().pageLayouts().assertDeleteFail(404, exhibitionId, createdPageLayoutId)
        }
    }

}