package fi.metatavu.noheva.api.test.functional

import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.resources.KeycloakResource
import fi.metatavu.noheva.api.test.functional.resources.MqttResource
import fi.metatavu.noheva.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing sub layout API
 *
 * @author Jari Nykänen
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class SubLayoutTestsIT : AbstractFunctionalTest() {

    @Test
    fun testCreateSubLayout() {
        createTestBuilder().use {
            val createdSubLayout = it.admin.subLayouts.create()
            Assertions.assertNotNull(createdSubLayout)

            // invalid data/layout type relation
            it.admin.subLayouts.assertCreateFail(400, createdSubLayout.copy(data = "invalid data", layoutType = LayoutType.ANDROID))
            it.admin.subLayouts.assertCreateFail(400, createdSubLayout.copy(data = PageLayoutViewHtml(html = ""), layoutType = LayoutType.ANDROID))
        }
    }

    @Test
    fun testFindSubLayout() {
        createTestBuilder().use {
            val nonExistingSubLayoutId = UUID.randomUUID()
            val createdSubLayoutId = it.admin.subLayouts.create().id!!
            it.admin.subLayouts.assertFindFail(404, nonExistingSubLayoutId)
            Assertions.assertNotNull(it.admin.subLayouts.findSubLayout(createdSubLayoutId))
        }
    }

    @Test
    fun testListSubLayouts() {
        createTestBuilder().use {
            Assertions.assertEquals(0, it.admin.subLayouts.listSubLayouts().size)

            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.BOOLEAN))
            val createdChildren = arrayOf(PageLayoutView("childid", PageLayoutWidgetType.BUTTON, arrayOf(), arrayOf()))
            val createdData =
                PageLayoutView("rootid", PageLayoutWidgetType.FRAME_LAYOUT, createdProperties, createdChildren)

            val defaultSubLayout = SubLayout(
                name = "created name",
                layoutType = LayoutType.ANDROID,
                data = createdData
            )

            it.admin.subLayouts.create(defaultSubLayout)
            it.admin.subLayouts.create(defaultSubLayout)
            it.admin.subLayouts.create(defaultSubLayout)
            it.admin.subLayouts.create(
                SubLayout(
                    name = "created name",
                    layoutType = LayoutType.ANDROID,
                    data = createdData
                )
            )

            val allSubLayouts = it.admin.subLayouts.listSubLayouts()
            Assertions.assertEquals(4, allSubLayouts.size)
        }
    }

    @Test
    fun testUpdateSubLayout() {
        createTestBuilder().use {
            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.BOOLEAN))
            val createdChildren = arrayOf(PageLayoutView("childid", PageLayoutWidgetType.BUTTON, arrayOf(), arrayOf()))
            val createdData =
                PageLayoutView("rootid", PageLayoutWidgetType.FRAME_LAYOUT, createdProperties, createdChildren)

            val createdSubLayout = it.admin.subLayouts.create(
                SubLayout(
                    name = "created name",
                    layoutType = LayoutType.ANDROID,
                    data = createdData
                )
            )

            val createdSubLayoutId = createdSubLayout.id!!

            val foundCreatedSubLayout = it.admin.subLayouts.findSubLayout(createdSubLayoutId)
            Assertions.assertEquals(createdSubLayout.id, foundCreatedSubLayout.id)
            Assertions.assertEquals("created name", createdSubLayout.name)

            val createdSubLayoutData = parsePageLayoutViewDataAndroid(createdSubLayout.data)

            Assertions.assertEquals(PageLayoutWidgetType.FRAME_LAYOUT, createdSubLayoutData!!.widget)
            Assertions.assertEquals(1, createdSubLayoutData.properties.size)
            Assertions.assertEquals("name", createdSubLayoutData.properties[0].name)
            Assertions.assertEquals("true", createdSubLayoutData.properties[0].value)
            Assertions.assertEquals(PageLayoutViewPropertyType.BOOLEAN, createdSubLayoutData.properties[0].type)
            Assertions.assertEquals(1, createdSubLayoutData.children.size)
            Assertions.assertEquals(createdChildren[0].id, createdSubLayoutData.children[0].id)

            // Update to another Android layout
            val updatedProperties = arrayOf(PageLayoutViewProperty("uname", "str", PageLayoutViewPropertyType.STRING))
            val updatedChildren = arrayOf<PageLayoutView>()
            val updatedData1 = PageLayoutView(
                id = "updatedid",
                widget = PageLayoutWidgetType.MEDIA_VIEW,
                properties = updatedProperties,
                children = updatedChildren
            )

            val updatedSubLayout1 = it.admin.subLayouts.updateSubLayout(
                SubLayout(
                    id = createdSubLayoutId,
                    name = "updated name",
                    data = updatedData1,
                    layoutType = LayoutType.ANDROID
                )
            )

            val foundUpdatedSubLayout = it.admin.subLayouts.findSubLayout(createdSubLayoutId)

            Assertions.assertEquals(updatedSubLayout1.id, foundUpdatedSubLayout.id)
            Assertions.assertEquals("updated name", updatedSubLayout1.name)

            val updatedSubLayoutData1 = parsePageLayoutViewDataAndroid(updatedSubLayout1.data)

            Assertions.assertEquals(PageLayoutWidgetType.MEDIA_VIEW, updatedSubLayoutData1!!.widget)
            Assertions.assertEquals(1, updatedSubLayoutData1.properties.size)
            Assertions.assertEquals("uname", updatedSubLayoutData1.properties[0].name)
            Assertions.assertEquals("str", updatedSubLayoutData1.properties[0].value)
            Assertions.assertEquals(PageLayoutViewPropertyType.STRING, updatedSubLayoutData1.properties[0].type)
            Assertions.assertEquals(0, updatedSubLayoutData1.children.size)

            // Test updating with invalid format of the data
            val htmlData = PageLayoutViewHtml(html = "<html></html>")
            it.admin.subLayouts.assertUpdateFail(400, foundUpdatedSubLayout.copy(data = htmlData))
            //Test that updating the layout type is not allowed
            it.admin.subLayouts.assertUpdateFail(400, foundUpdatedSubLayout.copy(layoutType = LayoutType.HTML, data = htmlData))
        }
    }

    @Test
    fun testDeleteSubLayout() {
        createTestBuilder().use {
            val nonExistingSubLayoutId = UUID.randomUUID()
            val createdSubLayout = it.admin.subLayouts.create()
            val createdSubLayoutId = createdSubLayout.id!!
            Assertions.assertNotNull(it.admin.subLayouts.findSubLayout(createdSubLayoutId))
            it.admin.subLayouts.assertDeleteFail(404, nonExistingSubLayoutId)
            it.admin.subLayouts.delete(createdSubLayout)
            it.admin.subLayouts.assertDeleteFail(404, createdSubLayoutId)
        }
    }

}