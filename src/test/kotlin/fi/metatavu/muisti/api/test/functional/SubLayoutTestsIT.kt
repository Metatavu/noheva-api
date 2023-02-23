package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MqttResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
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
@TestProfile(DefaultTestProfile::class)
class SubLayoutTestsIT : AbstractFunctionalTest() {

    @Test
    fun testCreateSubLayout() {
        createTestBuilder().use {
            val createdSubLayout = it.admin.subLayouts.create()
            Assertions.assertNotNull(createdSubLayout)
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
                data = createdData
            )

            it.admin.subLayouts.create(defaultSubLayout)
            it.admin.subLayouts.create(defaultSubLayout)
            it.admin.subLayouts.create(defaultSubLayout)
            it.admin.subLayouts.create(
                SubLayout(
                    name = "created name",
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
                    data = createdData
                )
            )

            val createdSubLayoutId = createdSubLayout.id!!

            val foundCreatedSubLayout = it.admin.subLayouts.findSubLayout(createdSubLayoutId)
            Assertions.assertEquals(createdSubLayout.id, foundCreatedSubLayout.id)
            Assertions.assertEquals("created name", createdSubLayout.name)
            Assertions.assertEquals(PageLayoutWidgetType.FRAME_LAYOUT, createdSubLayout.data.widget)
            Assertions.assertEquals(1, createdSubLayout.data.properties.size)
            Assertions.assertEquals("name", createdSubLayout.data.properties[0].name)
            Assertions.assertEquals("true", createdSubLayout.data.properties[0].value)
            Assertions.assertEquals(PageLayoutViewPropertyType.BOOLEAN, createdSubLayout.data.properties[0].type)
            Assertions.assertEquals(1, createdSubLayout.data.children.size)
            Assertions.assertEquals(createdChildren[0].id, createdSubLayout.data.children[0].id)

            val updatedProperties = arrayOf(PageLayoutViewProperty("uname", "str", PageLayoutViewPropertyType.STRING))
            val updatedChildren = arrayOf<PageLayoutView>()
            val updatedData = PageLayoutView(
                id = "updatedid",
                widget = PageLayoutWidgetType.MEDIA_VIEW,
                properties = updatedProperties,
                children = updatedChildren
            )

            val updatedSubLayout = it.admin.subLayouts.updateSubLayout(
                SubLayout(
                    id = createdSubLayoutId,
                    name = "updated name",
                    data = updatedData
                )
            )

            val foundUpdatedSubLayout = it.admin.subLayouts.findSubLayout(createdSubLayoutId)

            Assertions.assertEquals(updatedSubLayout.id, foundUpdatedSubLayout.id)
            Assertions.assertEquals("updated name", updatedSubLayout.name)
            Assertions.assertEquals(PageLayoutWidgetType.MEDIA_VIEW, updatedSubLayout.data.widget)
            Assertions.assertEquals(1, updatedSubLayout.data.properties.size)
            Assertions.assertEquals("uname", updatedSubLayout.data.properties[0].name)
            Assertions.assertEquals("str", updatedSubLayout.data.properties[0].value)
            Assertions.assertEquals(PageLayoutViewPropertyType.STRING, updatedSubLayout.data.properties[0].type)
            Assertions.assertEquals(0, updatedSubLayout.data.children.size)
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