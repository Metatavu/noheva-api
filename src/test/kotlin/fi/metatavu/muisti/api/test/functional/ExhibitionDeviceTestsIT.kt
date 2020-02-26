package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ExhibitionDevice
import fi.metatavu.muisti.api.client.models.Point
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition devices API
 *
 * @author Antti Leppä
 */
class ExhibitionDeviceTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionDevice() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId)
            val model = it.admin().exhibitionDeviceModels().create(exhibitionId)
            assertNotNull(it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!, "name", null))
            it.admin().exhibitionDevices().assertCreateFail(400, exhibitionId, UUID.randomUUID(), model.id!!,"name", null)
            it.admin().exhibitionDevices().assertCreateFail(400, exhibitionId, group.id!!, UUID.randomUUID(),"name", null)
        }
   }

    @Test
    fun testFindExhibitionDevice() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId)
            val model = it.admin().exhibitionDeviceModels().create(exhibitionId)
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionDeviceId = UUID.randomUUID()
            val createdExhibitionDevice = it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!)
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            it.admin().exhibitionDevices().assertFindFail(404, exhibitionId, nonExistingExhibitionDeviceId)
            it.admin().exhibitionDevices().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionDeviceId)
            it.admin().exhibitionDevices().assertFindFail(404, nonExistingExhibitionId, createdExhibitionDeviceId)
            assertNotNull(it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId, createdExhibitionDeviceId))
        }
    }

    @Test
    fun testListExhibitionDevices() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val group1 = it.admin().exhibitionDeviceGroups().create(exhibitionId)
            val group2 = it.admin().exhibitionDeviceGroups().create(exhibitionId)
            val model = it.admin().exhibitionDeviceModels().create(exhibitionId)
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().exhibitionDevices().assertListFail(404, nonExistingExhibitionId, null)
            assertEquals(0, it.admin().exhibitionDevices().listExhibitionDevices(exhibitionId, null).size)

            val createdExhibitionDevice = it.admin().exhibitionDevices().create(exhibitionId, group1.id!!, model.id!!)
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            it.admin().exhibitionDevices().assertCount(1, exhibitionId, null)
            it.admin().exhibitionDevices().assertCount(1, exhibitionId, group1.id!!)
            it.admin().exhibitionDevices().assertCount(0, exhibitionId, group2.id!!)

            val exhibitionDevices = it.admin().exhibitionDevices().listExhibitionDevices(exhibitionId, null)
            assertEquals(1, exhibitionDevices.size)
            assertEquals(createdExhibitionDeviceId, exhibitionDevices[0].id)
            it.admin().exhibitionDevices().delete(exhibitionId, createdExhibitionDeviceId)
            assertEquals(0, it.admin().exhibitionDevices().listExhibitionDevices(exhibitionId, null).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val group1 = it.admin().exhibitionDeviceGroups().create(exhibitionId)
            val model1 = it.admin().exhibitionDeviceModels().create(exhibitionId)
            val model2 = it.admin().exhibitionDeviceModels().create(exhibitionId)
            val nonExistingGroupId = UUID.randomUUID()
            val nonExistingModelId = UUID.randomUUID()

            val createdExhibitionDevice = it.admin().exhibitionDevices().create(exhibitionId, group1.id!!, model1.id!!,"created name", Point(-123.0, 234.0))
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            val foundCreatedExhibitionDevice = it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId, createdExhibitionDeviceId)
            assertEquals(createdExhibitionDevice.id, foundCreatedExhibitionDevice?.id)
            assertEquals("created name", createdExhibitionDevice.name)
            assertEquals(-123.0, createdExhibitionDevice.location?.x)
            assertEquals(234.0, createdExhibitionDevice.location?.y)
            assertEquals(model1.id, createdExhibitionDevice.modelId)

            val updatedExhibitionDevice = it.admin().exhibitionDevices().updateExhibitionDevice(exhibitionId, ExhibitionDevice(group1.id!!, model2.id!!, "updated name", createdExhibitionDeviceId, exhibitionId, Point(123.2, -234.4)))
            val foundUpdatedExhibitionDevice = it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId, createdExhibitionDeviceId)

            assertEquals(updatedExhibitionDevice!!.id, foundUpdatedExhibitionDevice?.id)
            assertEquals("updated name", updatedExhibitionDevice.name)
            assertEquals(123.2, updatedExhibitionDevice.location?.x)
            assertEquals(-234.4, updatedExhibitionDevice.location?.y)
            assertEquals(model2.id, updatedExhibitionDevice.modelId)

            it.admin().exhibitionDevices().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionDevice(group1.id!!, model2.id!!,"name", createdExhibitionDeviceId))
            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId, ExhibitionDevice(nonExistingGroupId, model2.id!!,"updated name", createdExhibitionDeviceId))
            it.admin().exhibitionDevices().assertUpdateFail(400, exhibitionId, ExhibitionDevice(group1.id!!, nonExistingModelId,"updated name", createdExhibitionDeviceId))
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val group = it.admin().exhibitionDeviceGroups().create(exhibitionId)
            val model = it.admin().exhibitionDeviceModels().create(exhibitionId)
            val createdExhibitionDevice = it.admin().exhibitionDevices().create(exhibitionId, group.id!!, model.id!!)
            val createdExhibitionDeviceId = createdExhibitionDevice.id!!

            assertNotNull(it.admin().exhibitionDevices().findExhibitionDevice(exhibitionId, createdExhibitionDeviceId))
            it.admin().exhibitionDevices().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionDevices().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionDeviceId)
            it.admin().exhibitionDevices().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionDevices().delete(exhibitionId, createdExhibitionDevice)

            it.admin().exhibitionDevices().assertDeleteFail(404, exhibitionId, createdExhibitionDeviceId)
        }
    }

}