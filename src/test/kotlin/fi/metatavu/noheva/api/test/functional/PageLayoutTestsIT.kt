package fi.metatavu.noheva.api.test.functional

import fi.metatavu.noheva.api.client.models.*
import fi.metatavu.noheva.api.test.functional.resources.KeycloakResource
import fi.metatavu.noheva.api.test.functional.resources.MqttResource
import fi.metatavu.noheva.api.test.functional.resources.MysqlResource
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
            val deviceModel = it.admin.deviceModels.create()
            // Android layout creation
            val createdPageLayoutAndroid = it.admin.pageLayouts.create(deviceModel)
            assertNotNull(createdPageLayoutAndroid)

            // Html layout creation
            val htmlLayout = createdPageLayoutAndroid.copy(
                data = PageLayoutViewHtml("<html></html>"),
                layoutType = LayoutType.HTML
            )
            val createdPageLayoutHtml = it.admin.pageLayouts.create(htmlLayout)
            val createdHtmlData = parsePageLayoutViewDataHtml(createdPageLayoutHtml.data)
            assertEquals("<html></html>", createdHtmlData!!.html)

            // Invalid layout type-data creation
            it.admin.pageLayouts.assertCreateFail(400, htmlLayout.copy(layoutType = LayoutType.ANDROID))
        }
   }

    @Test
    fun testFindPageLayout() {
        createTestBuilder().use {
            val nonExistingPageLayoutId = UUID.randomUUID()
            val deviceModel = it.admin.deviceModels.create()
            val createdPageLayout = it.admin.pageLayouts.create(deviceModel)
            val createdPageLayoutId = createdPageLayout.id!!
            it.admin.pageLayouts.assertFindFail(404, nonExistingPageLayoutId)
            assertNotNull(it.admin.pageLayouts.findPageLayout(createdPageLayoutId))
        }
    }

    @Test
    fun testListPageLayouts() {
        createTestBuilder().use {
            assertEquals(0, it.admin.pageLayouts.listPageLayouts().size)

            val deviceModel = it.admin.deviceModels.create()
            val secondDeviceModel = it.admin.deviceModels.create()

            val createdModelId = deviceModel.id!!
            val anotherCreatedModelId = secondDeviceModel.id!!

            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.BOOLEAN))
            val createdChildren = arrayOf(PageLayoutView("childid", PageLayoutWidgetType.BUTTON, arrayOf(), arrayOf()))
            val createdDataAndroid = PageLayoutView("rootid", PageLayoutWidgetType.FRAME_LAYOUT, createdProperties, createdChildren)

            val defaultPageLayout = PageLayout(
                name = "created name",
                data = createdDataAndroid,
                thumbnailUrl = "http://example.com/thumbnail.png",
                screenOrientation = ScreenOrientation.PORTRAIT,
                layoutType = LayoutType.ANDROID,
                modelId = createdModelId
            )

            it.admin.pageLayouts.create(defaultPageLayout)
            it.admin.pageLayouts.create(defaultPageLayout)
            it.admin.pageLayouts.create(defaultPageLayout)
            it.admin.pageLayouts.create(PageLayout(
                    name = "created name",
                    data = createdDataAndroid,
                    thumbnailUrl = "http://example.com/thumbnail.png",
                    screenOrientation = ScreenOrientation.LANDSCAPE,
                    layoutType = LayoutType.ANDROID,
                    modelId = anotherCreatedModelId
            ))

            val portrait = ScreenOrientation.PORTRAIT.toString()
            val landscape = ScreenOrientation.LANDSCAPE.toString()

            val allPageLayouts = it.admin.pageLayouts.listPageLayouts()
            assertEquals(4, allPageLayouts.size)

            val pageLayoutsByDeviceModelIdAndOrientation = it.admin.pageLayouts.listPageLayouts(createdModelId, portrait)
            assertEquals(3, pageLayoutsByDeviceModelIdAndOrientation.size)

            val pageLayoutsByDeviceModelIdAndOrientation2 = it.admin.pageLayouts.listPageLayouts(anotherCreatedModelId, landscape)
            assertEquals(1, pageLayoutsByDeviceModelIdAndOrientation2.size)

            val pageLayoutsByDeviceModelIdAndIncorrectOrientation = it.admin.pageLayouts.listPageLayouts(createdModelId, landscape)
            assertEquals(0, pageLayoutsByDeviceModelIdAndIncorrectOrientation.size)

            val pageLayoutsById = it.admin.pageLayouts.listPageLayouts(createdModelId, null)
            assertEquals(3, pageLayoutsById.size)

            val pageLayoutsByOrientationPORTRAIT = it.admin.pageLayouts.listPageLayouts(null, portrait)
            assertEquals(3, pageLayoutsByOrientationPORTRAIT.size)

            val pageLayoutsByOrientationLandscape = it.admin.pageLayouts.listPageLayouts(null, landscape)
            assertEquals(1, pageLayoutsByOrientationLandscape.size)

            it.admin.pageLayouts.assertListFail(400, UUID.randomUUID(), portrait)
            it.admin.pageLayouts.assertListFail(400, createdModelId, "thisShouldThrowError")
        }
    }

    @Test
    fun testUpdatePageLayout() {
        createTestBuilder().use {
            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.BOOLEAN))
            val createdChildren = arrayOf(PageLayoutView("childid", PageLayoutWidgetType.BUTTON, arrayOf(), arrayOf()))
            val createdData = PageLayoutView("rootid", PageLayoutWidgetType.FRAME_LAYOUT, createdProperties, createdChildren)
            val createdDeviceModelId = it.admin.deviceModels.create().id!!
            val updateDeviceModelId = it.admin.deviceModels.create().id!!

            val createdPageLayout = it.admin.pageLayouts.create(PageLayout(
                name = "created name",
                data = createdData,
                thumbnailUrl = "http://example.com/thumbnail.png",
                screenOrientation = ScreenOrientation.PORTRAIT,
                layoutType = LayoutType.ANDROID,
                modelId = createdDeviceModelId
            ))

            val createdPageLayoutId = createdPageLayout.id!!

            val foundCreatedPageLayout = it.admin.pageLayouts.findPageLayout(createdPageLayoutId)
            assertEquals(createdPageLayout.id, foundCreatedPageLayout.id)
            assertEquals("created name", createdPageLayout.name)
            assertEquals("http://example.com/thumbnail.png", createdPageLayout.thumbnailUrl)
            assertEquals(createdDeviceModelId, createdPageLayout.modelId)
            assertEquals(ScreenOrientation.PORTRAIT, createdPageLayout.screenOrientation)

            val createdDataParsed = parsePageLayoutViewDataAndroid(createdPageLayout.data)

            assertEquals(PageLayoutWidgetType.FRAME_LAYOUT, createdDataParsed!!.widget)
            assertEquals(1, createdDataParsed.properties.size)
            assertEquals("name", createdDataParsed.properties[0].name)
            assertEquals("true", createdDataParsed.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.BOOLEAN, createdDataParsed.properties[0].type)
            assertEquals(1, createdDataParsed.children.size)
            assertEquals(createdChildren[0].id, createdDataParsed.children[0].id)

            // Update with different Android layout data
            val updatedProperties = arrayOf(PageLayoutViewProperty("uname", "str", PageLayoutViewPropertyType.STRING))
            val updatedChildren = arrayOf<PageLayoutView>()
            val updatedData = PageLayoutView(
                id = "updatedid",
                widget = PageLayoutWidgetType.MEDIA_VIEW,
                properties = updatedProperties,
                children = updatedChildren
            )

            val updatedPageLayout = it.admin.pageLayouts.updatePageLayout(
                PageLayout(
                    id = createdPageLayoutId,
                    name = "updated name",
                    data = updatedData,
                    thumbnailUrl = "http://example.com/updated.png",
                    screenOrientation = ScreenOrientation.LANDSCAPE,
                    layoutType = LayoutType.ANDROID,
                    modelId = updateDeviceModelId
                )
            )

            val foundUpdatedPageLayout = it.admin.pageLayouts.findPageLayout(createdPageLayoutId)

            assertEquals(updatedPageLayout.id, foundUpdatedPageLayout.id)
            assertEquals("updated name", updatedPageLayout.name)
            assertEquals("http://example.com/updated.png", updatedPageLayout.thumbnailUrl)
            assertEquals(updateDeviceModelId, updatedPageLayout.modelId)
            assertEquals(ScreenOrientation.LANDSCAPE, updatedPageLayout.screenOrientation)

            val updatedDataParsed = parsePageLayoutViewDataAndroid(updatedPageLayout.data)
            assertEquals(PageLayoutWidgetType.MEDIA_VIEW, updatedDataParsed!!.widget)
            assertEquals(1, updatedDataParsed.properties.size)
            assertEquals("uname", updatedDataParsed.properties[0].name)
            assertEquals("str", updatedDataParsed.properties[0].value)
            assertEquals(PageLayoutViewPropertyType.STRING, updatedDataParsed.properties[0].type)
            assertEquals(0, updatedDataParsed.children.size)

            val htmlLayoutData = PageLayoutViewHtml("<html><body><h1>Test</h1></body></html>")

            // invalid update layout data attempt (type android but data is html)
            it.admin.pageLayouts.assertUpdateFail(400, foundCreatedPageLayout.copy(data = htmlLayoutData, layoutType = LayoutType.ANDROID))
            // Verify that layout type cannot be changed (type html and data is html)
            it.admin.pageLayouts.assertUpdateFail(400,
                PageLayout(
                    id = createdPageLayoutId,
                    name = "valid html layout",
                    data = htmlLayoutData,
                    screenOrientation = ScreenOrientation.LANDSCAPE,
                    layoutType = LayoutType.HTML,
                    modelId = updateDeviceModelId
                )
            )
        }
    }

    @Test
    fun testDeletePageLayout() {
        createTestBuilder().use {
            val nonExistingPageLayoutId = UUID.randomUUID()
            val deviceModel = it.admin.deviceModels.create()
            val createdPageLayout = it.admin.pageLayouts.create(deviceModel)
            val createdPageLayoutId = createdPageLayout.id!!
            assertNotNull(it.admin.pageLayouts.findPageLayout(createdPageLayoutId))
            it.admin.pageLayouts.assertDeleteFail(404, nonExistingPageLayoutId)
            it.admin.pageLayouts.delete(createdPageLayout)
            it.admin.pageLayouts.assertDeleteFail(404, createdPageLayoutId)
        }
    }

}