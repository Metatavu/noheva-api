package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition page layout API
 *
 * @author Antti Leppä
 */
class PageLayoutTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreatePageLayout() {
        TestBuilder().use {
            val createdPageLayout = it.admin().pageLayouts().create()
            assertNotNull(createdPageLayout)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindPageLayout() {
        TestBuilder().use {
            val nonExistingPageLayoutId = UUID.randomUUID()
            val createdPageLayout = it.admin().pageLayouts().create()
            val createdPageLayoutId = createdPageLayout.id!!
            it.admin().pageLayouts().assertFindFail(404, nonExistingPageLayoutId)
            assertNotNull(it.admin().pageLayouts().findPageLayout(createdPageLayoutId))
        }
    }

    @Test
    fun testListPageLayouts() {
        TestBuilder().use {
            assertEquals(0, it.admin().pageLayouts().listPageLayouts().size)
            val createdPageLayout = it.admin().pageLayouts().create()
            val createdPageLayoutId = createdPageLayout.id!!
            val pageLayout = it.admin().pageLayouts().listPageLayouts()
            assertEquals(1, pageLayout.size)
            assertEquals(createdPageLayoutId, pageLayout[0].id)
            it.admin().pageLayouts().delete(createdPageLayoutId)
            assertEquals(0, it.admin().pageLayouts().listPageLayouts().size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.boolean))
            val createdChildren = arrayOf(PageLayoutView("childid", "child", arrayOf(), arrayOf()))
            val createdData = PageLayoutView("rootid", "created widget", createdProperties, createdChildren)

            val createdPageLayout = it.admin().pageLayouts().create(PageLayout(
                name = "created name",
                data = createdData,
                thumbnailUrl = "http://example.com/thumbnail.png",
                screenOrientation = ScreenOrientation.portrait
            ))

            val createdPageLayoutId = createdPageLayout.id!!

            val foundCreatedPageLayout = it.admin().pageLayouts().findPageLayout(createdPageLayoutId)
            assertEquals(createdPageLayout.id, foundCreatedPageLayout?.id)
            assertEquals("created name", createdPageLayout.name)
            assertEquals("http://example.com/thumbnail.png", createdPageLayout.thumbnailUrl)
            assertEquals(ScreenOrientation.portrait, createdPageLayout.screenOrientation)
            assertEquals("created widget", createdPageLayout.data.widget)
            assertEquals(1, createdPageLayout.data.properties.size)
            assertEquals("name", createdPageLayout.data.properties[0].name)
            assertEquals("true", createdPageLayout.data.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.boolean, createdPageLayout.data.properties[0].type)
            assertEquals(1, createdPageLayout.data.children.size)
            assertEquals(createdChildren[0].id, createdPageLayout.data.children[0].id)

            val updatedProperties = arrayOf(PageLayoutViewProperty("uname", "str", PageLayoutViewPropertyType.string))
            val updatedChildren = arrayOf<PageLayoutView>()
            val updatedData = PageLayoutView(
                id = "updatedid",
                widget = "updated widget",
                properties = updatedProperties,
                children = updatedChildren
            )

            val updatedPageLayout = it.admin().pageLayouts().updatePageLayout(PageLayout(
                id = createdPageLayoutId,
                name = "updated name",
                data = updatedData,
                thumbnailUrl = "http://example.com/updated.png",
                screenOrientation = ScreenOrientation.landscape
            ))

            val foundUpdatedPageLayout = it.admin().pageLayouts().findPageLayout(createdPageLayoutId)

            assertEquals(updatedPageLayout!!.id, foundUpdatedPageLayout?.id)
            assertEquals("updated name", updatedPageLayout.name)
            assertEquals("http://example.com/updated.png", updatedPageLayout.thumbnailUrl)
            assertEquals(ScreenOrientation.landscape, updatedPageLayout.screenOrientation)
            assertEquals("updated widget", updatedPageLayout.data.widget)
            assertEquals(1, updatedPageLayout.data.properties.size)
            assertEquals("uname", updatedPageLayout.data.properties[0].name)
            assertEquals("str", updatedPageLayout.data.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.string, updatedPageLayout.data.properties[0].type)
            assertEquals(0, updatedPageLayout.data.children.size)
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val nonExistingPageLayoutId = UUID.randomUUID()
            val createdPageLayout = it.admin().pageLayouts().create()
            val createdPageLayoutId = createdPageLayout.id!!
            assertNotNull(it.admin().pageLayouts().findPageLayout(createdPageLayoutId))
            it.admin().pageLayouts().assertDeleteFail(404, nonExistingPageLayoutId)
            it.admin().pageLayouts().delete(createdPageLayout)
            it.admin().pageLayouts().assertDeleteFail(404, createdPageLayoutId)
        }
    }

}