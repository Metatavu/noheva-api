package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ExhibitionDeviceModel
import fi.metatavu.muisti.api.client.models.ExhibitionDeviceModelCapabilities
import fi.metatavu.muisti.api.client.models.ExhibitionDeviceModelDimensions
import fi.metatavu.muisti.api.client.models.ExhibitionDeviceModelResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition deviceModels API
 *
 * @author Antti Leppä
 */
class ExhibitionDeviceModelTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionDeviceModel() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val dimensions = ExhibitionDeviceModelDimensions(8000.0, 6000.0)
            val resolution = ExhibitionDeviceModelResolution(12288.0, 8192.0)
            val capabilities = ExhibitionDeviceModelCapabilities(false)
            val createdExhibitionDeviceModel = it.admin().exhibitionDeviceModels().create(exhibition.id!!, "manu", "model", dimensions, resolution, capabilities)
            assertNotNull(createdExhibitionDeviceModel)
            assertEquals(8000.0, createdExhibitionDeviceModel.dimensions.width)
            assertEquals(6000.0, createdExhibitionDeviceModel.dimensions.height)
            assertEquals(12288.0, createdExhibitionDeviceModel.resolution.x)
            assertEquals(8192.0, createdExhibitionDeviceModel.resolution.y)
            assertEquals(false, createdExhibitionDeviceModel.capabilities.touch)
            assertEquals("manu", createdExhibitionDeviceModel.manufacturer)
            assertEquals("model", createdExhibitionDeviceModel.model)

            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibitionDeviceModel() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionDeviceModelId = UUID.randomUUID()
            val createdExhibitionDeviceModel = it.admin().exhibitionDeviceModels().create(exhibitionId)
            val createdExhibitionDeviceModelId = createdExhibitionDeviceModel.id!!

            it.admin().exhibitionDeviceModels().assertFindFail(404, exhibitionId, nonExistingExhibitionDeviceModelId)
            it.admin().exhibitionDeviceModels().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionDeviceModelId)
            it.admin().exhibitionDeviceModels().assertFindFail(404, nonExistingExhibitionId, createdExhibitionDeviceModelId)
            assertNotNull(it.admin().exhibitionDeviceModels().findExhibitionDeviceModel(exhibitionId, createdExhibitionDeviceModelId))
        }
    }

    @Test
    fun testListExhibitionDeviceModels() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().exhibitionDeviceModels().assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin().exhibitionDeviceModels().listExhibitionDeviceModels(exhibitionId).size)

            val createdExhibitionDeviceModel = it.admin().exhibitionDeviceModels().create(exhibitionId)
            val createdExhibitionDeviceModelId = createdExhibitionDeviceModel.id!!
            val exhibitionDeviceModels = it.admin().exhibitionDeviceModels().listExhibitionDeviceModels(exhibitionId)
            assertEquals(1, exhibitionDeviceModels.size)
            assertEquals(createdExhibitionDeviceModelId, exhibitionDeviceModels[0].id)
            it.admin().exhibitionDeviceModels().delete(exhibitionId, createdExhibitionDeviceModelId)
            assertEquals(0, it.admin().exhibitionDeviceModels().listExhibitionDeviceModels(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val createDimensions = ExhibitionDeviceModelDimensions(8000.0, 6000.0)
            val createResolution = ExhibitionDeviceModelResolution(12288.0, 8192.0)
            val createCapabilities = ExhibitionDeviceModelCapabilities(false)
            val createdExhibitionDeviceModel = it.admin().exhibitionDeviceModels().create(exhibition.id!!, "manu", "model", createDimensions, createResolution, createCapabilities)

            val createdExhibitionDeviceModelId = createdExhibitionDeviceModel.id!!

            val foundCreatedExhibitionDeviceModel = it.admin().exhibitionDeviceModels().findExhibitionDeviceModel(exhibitionId, createdExhibitionDeviceModelId)
            assertEquals(createdExhibitionDeviceModel.id, foundCreatedExhibitionDeviceModel?.id)
            assertNotNull(createdExhibitionDeviceModel)
            assertEquals(8000.0, createdExhibitionDeviceModel.dimensions.width)
            assertEquals(6000.0, createdExhibitionDeviceModel.dimensions.height)
            assertEquals(12288.0, createdExhibitionDeviceModel.resolution.x)
            assertEquals(8192.0, createdExhibitionDeviceModel.resolution.y)
            assertEquals(false, createdExhibitionDeviceModel.capabilities.touch)
            assertEquals("manu", createdExhibitionDeviceModel.manufacturer)
            assertEquals("model", createdExhibitionDeviceModel.model)

            val updateDimensions = ExhibitionDeviceModelDimensions(5000.0, 4000.0)
            val updateResolution = ExhibitionDeviceModelResolution(8288.0, 2192.0)
            val updateCapabilities = ExhibitionDeviceModelCapabilities(true)
            val updatedExhibitionDeviceModel = it.admin().exhibitionDeviceModels().updateExhibitionDeviceModel(exhibitionId, ExhibitionDeviceModel("altmanu", "altmodel", updateDimensions, updateResolution, updateCapabilities, createdExhibitionDeviceModelId))
            val foundUpdatedExhibitionDeviceModel = it.admin().exhibitionDeviceModels().findExhibitionDeviceModel(exhibitionId, createdExhibitionDeviceModelId)

            assertEquals(updatedExhibitionDeviceModel!!.id, foundUpdatedExhibitionDeviceModel?.id)
            assertEquals(5000.0, updatedExhibitionDeviceModel.dimensions.width)
            assertEquals(4000.0, updatedExhibitionDeviceModel.dimensions.height)
            assertEquals(8288.0, updatedExhibitionDeviceModel.resolution.x)
            assertEquals(2192.0, updatedExhibitionDeviceModel.resolution.y)
            assertEquals(true, updatedExhibitionDeviceModel.capabilities.touch)
            assertEquals("altmanu", updatedExhibitionDeviceModel.manufacturer)
            assertEquals("altmodel", updatedExhibitionDeviceModel.model)

            it.admin().exhibitionDeviceModels().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionDeviceModel("altmanu", "altmodel", updateDimensions, updateResolution, updateCapabilities, createdExhibitionDeviceModelId))
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdExhibitionDeviceModel = it.admin().exhibitionDeviceModels().create(exhibitionId)
            val createdExhibitionDeviceModelId = createdExhibitionDeviceModel.id!!

            assertNotNull(it.admin().exhibitionDeviceModels().findExhibitionDeviceModel(exhibitionId, createdExhibitionDeviceModelId))
            it.admin().exhibitionDeviceModels().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionDeviceModels().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionDeviceModelId)
            it.admin().exhibitionDeviceModels().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionDeviceModels().delete(exhibitionId, createdExhibitionDeviceModel)

            it.admin().exhibitionDeviceModels().assertDeleteFail(404, exhibitionId, createdExhibitionDeviceModelId)
        }
    }

}