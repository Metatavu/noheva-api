package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing sub layout API
 *
 * @author Jari Nykänen
 */
class SubLayoutTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateSubLayout() {
        ApiTestBuilder().use {
            val createdSubLayout = it.admin().subLayouts().create()
            assertNotNull(createdSubLayout)
        }
   }

    @Test
    fun testFindSubLayout() {
        ApiTestBuilder().use {
            val nonExistingSubLayoutId = UUID.randomUUID()
            val createdSubLayoutId = it.admin().subLayouts().create().id!!
            it.admin().subLayouts().assertFindFail(404, nonExistingSubLayoutId)
            assertNotNull(it.admin().subLayouts().findSubLayout(createdSubLayoutId))
        }
    }

    @Test
    fun testListSubLayouts() {
        ApiTestBuilder().use {
            assertEquals(0, it.admin().subLayouts().listSubLayouts().size)

            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.BOOLEAN))
            val createdChildren = arrayOf(PageLayoutView("childid", PageLayoutWidgetType.BUTTON, arrayOf(), arrayOf()))
            val createdData = PageLayoutView("rootid", PageLayoutWidgetType.FRAME_LAYOUT, createdProperties, createdChildren)

            val defaultSubLayout = SubLayout(
                name = "created name",
                data = createdData
            )

            it.admin().subLayouts().create(defaultSubLayout)
            it.admin().subLayouts().create(defaultSubLayout)
            it.admin().subLayouts().create(defaultSubLayout)
            it.admin().subLayouts().create(SubLayout(
                name = "created name",
                data = createdData
            ))

            val allSubLayouts = it.admin().subLayouts().listSubLayouts()
            assertEquals(4, allSubLayouts.size)
        }
    }

    @Test
    fun testUpdateSubLayout() {
        ApiTestBuilder().use {
            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.BOOLEAN))
            val createdChildren = arrayOf(PageLayoutView("childid", PageLayoutWidgetType.BUTTON, arrayOf(), arrayOf()))
            val createdData = PageLayoutView("rootid", PageLayoutWidgetType.FRAME_LAYOUT, createdProperties, createdChildren)

            val createdSubLayout = it.admin().subLayouts().create(SubLayout(
                name = "created name",
                data = createdData
            ))

            val createdSubLayoutId = createdSubLayout.id!!

            val foundCreatedSubLayout = it.admin().subLayouts().findSubLayout(createdSubLayoutId)
            assertEquals(createdSubLayout.id, foundCreatedSubLayout?.id)
            assertEquals("created name", createdSubLayout.name)
            assertEquals(PageLayoutWidgetType.FRAME_LAYOUT, createdSubLayout.data.widget)
            assertEquals(1, createdSubLayout.data.properties.size)
            assertEquals("name", createdSubLayout.data.properties[0].name)
            assertEquals("true", createdSubLayout.data.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.BOOLEAN, createdSubLayout.data.properties[0].type)
            assertEquals(1, createdSubLayout.data.children.size)
            assertEquals(createdChildren[0].id, createdSubLayout.data.children[0].id)

            val updatedProperties = arrayOf(PageLayoutViewProperty("uname", "str", PageLayoutViewPropertyType.STRING))
            val updatedChildren = arrayOf<PageLayoutView>()
            val updatedData = PageLayoutView(
                id = "updatedid",
                widget = PageLayoutWidgetType.MEDIA_VIEW,
                properties = updatedProperties,
                children = updatedChildren
            )

            val updatedSubLayout = it.admin().subLayouts().updateSubLayout(SubLayout(
                id = createdSubLayoutId,
                name = "updated name",
                data = updatedData
            ))

            val foundUpdatedSubLayout = it.admin().subLayouts().findSubLayout(createdSubLayoutId)

            assertEquals(updatedSubLayout!!.id, foundUpdatedSubLayout?.id)
            assertEquals("updated name", updatedSubLayout.name)
            assertEquals(PageLayoutWidgetType.MEDIA_VIEW, updatedSubLayout.data.widget)
            assertEquals(1, updatedSubLayout.data.properties.size)
            assertEquals("uname", updatedSubLayout.data.properties[0].name)
            assertEquals("str", updatedSubLayout.data.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.STRING, updatedSubLayout.data.properties[0].type)
            assertEquals(0, updatedSubLayout.data.children.size)
        }
    }

    @Test
    fun testDeleteSubLayout() {
        ApiTestBuilder().use {
            val nonExistingSubLayoutId = UUID.randomUUID()
            val createdSubLayout = it.admin().subLayouts().create()
            val createdSubLayoutId = createdSubLayout.id!!
            assertNotNull(it.admin().subLayouts().findSubLayout(createdSubLayoutId))
            it.admin().subLayouts().assertDeleteFail(404, nonExistingSubLayoutId)
            it.admin().subLayouts().delete(createdSubLayout)
            it.admin().subLayouts().assertDeleteFail(404, createdSubLayoutId)
        }
    }

}