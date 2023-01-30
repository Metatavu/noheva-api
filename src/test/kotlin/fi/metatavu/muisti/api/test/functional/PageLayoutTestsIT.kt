package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import fi.metatavu.muisti.api.test.functional.resources.KeycloakResource
import fi.metatavu.muisti.api.test.functional.resources.MqttResource
import fi.metatavu.muisti.api.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test class for testing exhibition page layout API
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(KeycloakResource::class),
    QuarkusTestResource(MqttResource::class)
)
class PageLayoutTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreatePageLayout() {
        createTestBuilder().use {
            val deviceModel = it.admin().deviceModels.create()
            val createdPageLayout = it.admin().pageLayouts.create(deviceModel)
            assertNotNull(createdPageLayout)
        }
   }

    @Test
    fun testFindPageLayout() {
        createTestBuilder().use {
            val nonExistingPageLayoutId = UUID.randomUUID()
            val deviceModel = it.admin().deviceModels.create()
            val createdPageLayout = it.admin().pageLayouts.create(deviceModel)
            val createdPageLayoutId = createdPageLayout.id!!
            it.admin().pageLayouts.assertFindFail(404, nonExistingPageLayoutId)
            assertNotNull(it.admin().pageLayouts.findPageLayout(createdPageLayoutId))
        }
    }

    @Test
    fun testListPageLayouts() {
        createTestBuilder().use {
            assertEquals(0, it.admin().pageLayouts.listPageLayouts().size)

            val deviceModel = it.admin().deviceModels.create()
            val secondDeviceModel = it.admin().deviceModels.create()

            val createdModelId = deviceModel.id!!
            val anotherCreatedModelId = secondDeviceModel.id!!

            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.BOOLEAN))
            val createdChildren = arrayOf(PageLayoutView("childid", PageLayoutWidgetType.BUTTON, arrayOf(), arrayOf()))
            val createdData = PageLayoutView("rootid", PageLayoutWidgetType.FRAME_LAYOUT, createdProperties, createdChildren)

            val defaultPageLayout = PageLayout(
                    name = "created name",
                    data = createdData,
                    thumbnailUrl = "http://example.com/thumbnail.png",
                    screenOrientation = ScreenOrientation.PORTRAIT,
                    modelId = createdModelId
            )

            it.admin().pageLayouts.create(defaultPageLayout)
            it.admin().pageLayouts.create(defaultPageLayout)
            it.admin().pageLayouts.create(defaultPageLayout)
            it.admin().pageLayouts.create(PageLayout(
                    name = "created name",
                    data = createdData,
                    thumbnailUrl = "http://example.com/thumbnail.png",
                    screenOrientation = ScreenOrientation.LANDSCAPE,
                    modelId = anotherCreatedModelId
            ))

            val portrait = ScreenOrientation.PORTRAIT.toString()
            val landscape = ScreenOrientation.LANDSCAPE.toString()

            val allPageLayouts = it.admin().pageLayouts.listPageLayouts()
            assertEquals(4, allPageLayouts.size)

            val pageLayoutsByDeviceModelIdAndOrientation = it.admin().pageLayouts.listPageLayouts(createdModelId, portrait)
            assertEquals(3, pageLayoutsByDeviceModelIdAndOrientation.size)

            val pageLayoutsByDeviceModelIdAndOrientation2 = it.admin().pageLayouts.listPageLayouts(anotherCreatedModelId, landscape)
            assertEquals(1, pageLayoutsByDeviceModelIdAndOrientation2.size)

            val pageLayoutsByDeviceModelIdAndIncorrectOrientation = it.admin().pageLayouts.listPageLayouts(createdModelId, landscape)
            assertEquals(0, pageLayoutsByDeviceModelIdAndIncorrectOrientation.size)

            val pageLayoutsById = it.admin().pageLayouts.listPageLayouts(createdModelId, null)
            assertEquals(3, pageLayoutsById.size)

            val pageLayoutsByOrientationPORTRAIT = it.admin().pageLayouts.listPageLayouts(null, portrait)
            assertEquals(3, pageLayoutsByOrientationPORTRAIT.size)

            val pageLayoutsByOrientationLandscape = it.admin().pageLayouts.listPageLayouts(null, landscape)
            assertEquals(1, pageLayoutsByOrientationLandscape.size)

            it.admin().pageLayouts.assertListFail(400, UUID.randomUUID(), portrait)
            it.admin().pageLayouts.assertListFail(400, createdModelId, "thisShouldThrowError")
        }
    }

    @Test
    fun testUpdatePageLayout() {
        createTestBuilder().use {
            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.BOOLEAN))
            val createdChildren = arrayOf(PageLayoutView("childid", PageLayoutWidgetType.BUTTON, arrayOf(), arrayOf()))
            val createdData = PageLayoutView("rootid", PageLayoutWidgetType.FRAME_LAYOUT, createdProperties, createdChildren)
            val createdDeviceModelId = it.admin().deviceModels.create().id!!
            val updateDeviceModelId = it.admin().deviceModels.create().id!!

            val createdPageLayout = it.admin().pageLayouts.create(PageLayout(
                name = "created name",
                data = createdData,
                thumbnailUrl = "http://example.com/thumbnail.png",
                screenOrientation = ScreenOrientation.PORTRAIT,
                modelId = createdDeviceModelId
            ))

            val createdPageLayoutId = createdPageLayout.id!!

            val foundCreatedPageLayout = it.admin().pageLayouts.findPageLayout(createdPageLayoutId)
            assertEquals(createdPageLayout.id, foundCreatedPageLayout.id)
            assertEquals("created name", createdPageLayout.name)
            assertEquals("http://example.com/thumbnail.png", createdPageLayout.thumbnailUrl)
            assertEquals(createdDeviceModelId, createdPageLayout.modelId)
            assertEquals(ScreenOrientation.PORTRAIT, createdPageLayout.screenOrientation)
            assertEquals(PageLayoutWidgetType.FRAME_LAYOUT, createdPageLayout.data.widget)
            assertEquals(1, createdPageLayout.data.properties.size)
            assertEquals("name", createdPageLayout.data.properties[0].name)
            assertEquals("true", createdPageLayout.data.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.BOOLEAN, createdPageLayout.data.properties[0].type)
            assertEquals(1, createdPageLayout.data.children.size)
            assertEquals(createdChildren[0].id, createdPageLayout.data.children[0].id)

            val updatedProperties = arrayOf(PageLayoutViewProperty("uname", "str", PageLayoutViewPropertyType.STRING))
            val updatedChildren = arrayOf<PageLayoutView>()
            val updatedData = PageLayoutView(
                id = "updatedid",
                widget = PageLayoutWidgetType.MEDIA_VIEW,
                properties = updatedProperties,
                children = updatedChildren
            )

            val updatedPageLayout = it.admin().pageLayouts.updatePageLayout(PageLayout(
                id = createdPageLayoutId,
                name = "updated name",
                data = updatedData,
                thumbnailUrl = "http://example.com/updated.png",
                screenOrientation = ScreenOrientation.LANDSCAPE,
                modelId = updateDeviceModelId
            ))

            val foundUpdatedPageLayout = it.admin().pageLayouts.findPageLayout(createdPageLayoutId)

            assertEquals(updatedPageLayout.id, foundUpdatedPageLayout.id)
            assertEquals("updated name", updatedPageLayout.name)
            assertEquals("http://example.com/updated.png", updatedPageLayout.thumbnailUrl)
            assertEquals(updateDeviceModelId, updatedPageLayout.modelId)
            assertEquals(ScreenOrientation.LANDSCAPE, updatedPageLayout.screenOrientation)
            assertEquals(PageLayoutWidgetType.MEDIA_VIEW, updatedPageLayout.data.widget)
            assertEquals(1, updatedPageLayout.data.properties.size)
            assertEquals("uname", updatedPageLayout.data.properties[0].name)
            assertEquals("str", updatedPageLayout.data.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.STRING, updatedPageLayout.data.properties[0].type)
            assertEquals(0, updatedPageLayout.data.children.size)
        }
    }

    @Test
    fun testDeletePageLayout() {
        createTestBuilder().use {
            val nonExistingPageLayoutId = UUID.randomUUID()
            val deviceModel = it.admin().deviceModels.create()
            val createdPageLayout = it.admin().pageLayouts.create(deviceModel)
            val createdPageLayoutId = createdPageLayout.id!!
            assertNotNull(it.admin().pageLayouts.findPageLayout(createdPageLayoutId))
            it.admin().pageLayouts.assertDeleteFail(404, nonExistingPageLayoutId)
            it.admin().pageLayouts.delete(createdPageLayout)
            it.admin().pageLayouts.assertDeleteFail(404, createdPageLayoutId)
        }
    }

}