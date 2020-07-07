package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition deviceModels API
 *
 * @author Antti Leppä
 */
class DeviceModelTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateDeviceModel() {
        ApiTestBuilder().use {
            val dimensions = DeviceModelDimensions(8000.0, 6000.0, 1.0, 7900.0, 5900.0)
            val displayMetrics = DeviceModelDisplayMetrics(
                heightPixels = 12288,
                widthPixels = 8192,
                density = 3.5,
                xdpi = 515.154,
                ydpi = 514.597
            )

            val capabilities = DeviceModelCapabilities(false)
            val createdDeviceModel = it.admin().deviceModels().create(DeviceModel(
                manufacturer = "manu",
                model = "model",
                dimensions = dimensions,
                displayMetrics = displayMetrics,
                capabilities = capabilities
            ))

            assertNotNull(createdDeviceModel)
            assertEquals(8000.0, createdDeviceModel.dimensions.deviceWidth)
            assertEquals(6000.0, createdDeviceModel.dimensions.deviceHeight)
            assertEquals(1.0, createdDeviceModel.dimensions.deviceDepth)
            assertEquals(7900.0, createdDeviceModel.dimensions.screenWidth)
            assertEquals(5900.0, createdDeviceModel.dimensions.screenHeight)
            assertEquals(12288, createdDeviceModel.displayMetrics.heightPixels)
            assertEquals(8192, createdDeviceModel.displayMetrics.widthPixels)
            assertEquals(3.5, createdDeviceModel.displayMetrics.density)
            assertEquals(515.154, createdDeviceModel.displayMetrics.xdpi)
            assertEquals(514.597, createdDeviceModel.displayMetrics.ydpi)
            assertEquals(false, createdDeviceModel.capabilities.touch)
            assertEquals("manu", createdDeviceModel.manufacturer)
            assertEquals("model", createdDeviceModel.model)

            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindDeviceModel() {
        ApiTestBuilder().use {
            val createdDeviceModel = it.admin().deviceModels().create()
            val createdDeviceModelId = createdDeviceModel.id!!
            assertNotNull(it.admin().deviceModels().findDeviceModel(createdDeviceModelId))
        }
    }

    @Test
    fun testListDeviceModels() {
        ApiTestBuilder().use {
            assertEquals(0, it.admin().deviceModels().listDeviceModels().size)
            val createdDeviceModel = it.admin().deviceModels().create()
            val createdDeviceModelId = createdDeviceModel.id!!
            val deviceModels = it.admin().deviceModels().listDeviceModels()
            assertEquals(1, deviceModels.size)
            assertEquals(createdDeviceModelId, deviceModels[0].id)
            it.admin().deviceModels().delete(createdDeviceModelId)
            assertEquals(0, it.admin().deviceModels().listDeviceModels().size)
        }
    }

    @Test
    fun testUpdateDeviceModel() {
        ApiTestBuilder().use {
            val createDimensions = DeviceModelDimensions(8000.0, 6000.0, 1.0, 7900.0, 5900.0)
            val createDisplayMetrics = DeviceModelDisplayMetrics(
                heightPixels = 12288,
                widthPixels = 8192,
                density = 3.5,
                xdpi = 515.154,
                ydpi = 514.597
            )

            val createCapabilities = DeviceModelCapabilities(false)
            val createdDeviceModel = it.admin().deviceModels().create(DeviceModel(
                manufacturer = "manu",
                model = "model",
                dimensions = createDimensions,
                displayMetrics = createDisplayMetrics,
                capabilities = createCapabilities
            ))

            val createdDeviceModelId = createdDeviceModel.id!!

            val foundCreatedDeviceModel = it.admin().deviceModels().findDeviceModel(createdDeviceModelId)
            assertEquals(createdDeviceModel.id, foundCreatedDeviceModel?.id)
            assertNotNull(createdDeviceModel)
            assertEquals(8000.0, createdDeviceModel.dimensions.deviceWidth)
            assertEquals(6000.0, createdDeviceModel.dimensions.deviceHeight)
            assertEquals(1.0, createdDeviceModel.dimensions.deviceDepth)
            assertEquals(7900.0, createdDeviceModel.dimensions.screenWidth)
            assertEquals(5900.0, createdDeviceModel.dimensions.screenHeight)
            assertEquals(12288, createdDeviceModel.displayMetrics.heightPixels)
            assertEquals(8192, createdDeviceModel.displayMetrics.widthPixels)
            assertEquals(3.5, createdDeviceModel.displayMetrics.density)
            assertEquals(515.154, createdDeviceModel.displayMetrics.xdpi)
            assertEquals(514.597, createdDeviceModel.displayMetrics.ydpi)

            assertEquals(false, createdDeviceModel.capabilities.touch)
            assertEquals("manu", createdDeviceModel.manufacturer)
            assertEquals("model", createdDeviceModel.model)

            val updateDimensions = DeviceModelDimensions(5000.0, 4000.0, 2.0, 4900.0, 3900.0)
            val updateDisplayMetrics = DeviceModelDisplayMetrics(
                    heightPixels = 22288,
                    widthPixels = 2192,
                    density = 2.5,
                    xdpi = 215.154,
                    ydpi = 214.597
            )

            val updateCapabilities = DeviceModelCapabilities(true)
            val updatedDeviceModel = it.admin().deviceModels().updateDeviceModel(DeviceModel("altmanu", "altmodel", updateDimensions, updateDisplayMetrics, updateCapabilities, createdDeviceModelId))
            val foundUpdatedDeviceModel = it.admin().deviceModels().findDeviceModel(createdDeviceModelId)

            assertEquals(updatedDeviceModel!!.id, foundUpdatedDeviceModel?.id)
            assertEquals(5000.0, updatedDeviceModel.dimensions.deviceWidth)
            assertEquals(4000.0, updatedDeviceModel.dimensions.deviceHeight)
            assertEquals(2.0, updatedDeviceModel.dimensions.deviceDepth)
            assertEquals(4900.0, updatedDeviceModel.dimensions.screenWidth)
            assertEquals(3900.0, updatedDeviceModel.dimensions.screenHeight)
            assertEquals(22288, updatedDeviceModel.displayMetrics.heightPixels)
            assertEquals(2192, updatedDeviceModel.displayMetrics.widthPixels)
            assertEquals(2.5, updatedDeviceModel.displayMetrics.density)
            assertEquals(215.154, updatedDeviceModel.displayMetrics.xdpi)
            assertEquals(214.597, updatedDeviceModel.displayMetrics.ydpi)
            assertEquals(true, updatedDeviceModel.capabilities.touch)
            assertEquals("altmanu", updatedDeviceModel.manufacturer)
            assertEquals("altmodel", updatedDeviceModel.model)
        }
    }

    @Test
    fun testDeleteDeviceModel() {
        ApiTestBuilder().use {
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdDeviceModel = it.admin().deviceModels().create()
            val createdDeviceModelId = createdDeviceModel.id!!

            val createdProperties = arrayOf(PageLayoutViewProperty("name", "true", PageLayoutViewPropertyType.boolean))
            val createdChildren = arrayOf(PageLayoutView("childid", PageLayoutWidgetType.button, arrayOf(), arrayOf()))
            val createdData = PageLayoutView("rootid", PageLayoutWidgetType.frameLayout, createdProperties, createdChildren)
            val createdPageLayout = it.admin().pageLayouts().create(PageLayout(
                name = "created name",
                data = createdData,
                thumbnailUrl = "http://example.com/thumbnail.png",
                screenOrientation = ScreenOrientation.portrait,
                modelId = createdDeviceModelId
            ))

            assertNotNull(it.admin().deviceModels().findDeviceModel(createdDeviceModelId))
            it.admin().deviceModels().assertDeleteFail(404, nonExistingSessionVariableId)
            it.admin().deviceModels().assertDeleteFail(400, createdDeviceModelId)

            it.admin().pageLayouts().delete(createdPageLayout.id!!)

            it.admin().deviceModels().delete(createdDeviceModel)

            it.admin().deviceModels().assertDeleteFail(404, createdDeviceModelId)
        }
    }

}