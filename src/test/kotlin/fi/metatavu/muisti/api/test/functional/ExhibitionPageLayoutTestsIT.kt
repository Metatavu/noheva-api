package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.ExhibitionPageLayout
import fi.metatavu.muisti.api.client.models.ExhibitionPageLayoutView
import fi.metatavu.muisti.api.client.models.ExhibitionPageLayoutViewProperty
import fi.metatavu.muisti.api.client.models.ExhibitionPageLayoutViewPropertyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.*

/**
 * Test class for testing exhibition pageLayout API
 *
 * @author Antti Leppä
 */
class ExhibitionPageLayoutTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateExhibitionPageLayout() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val createdExhibitionPageLayout = it.admin().exhibitionPageLayouts().create(exhibition.id!!)
            assertNotNull(createdExhibitionPageLayout)
            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindExhibitionPageLayout() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingExhibitionPageLayoutId = UUID.randomUUID()
            val createdExhibitionPageLayout = it.admin().exhibitionPageLayouts().create(exhibitionId)
            val createdExhibitionPageLayoutId = createdExhibitionPageLayout.id!!

            it.admin().exhibitionPageLayouts().assertFindFail(404, exhibitionId, nonExistingExhibitionPageLayoutId)
            it.admin().exhibitionPageLayouts().assertFindFail(404, nonExistingExhibitionId, nonExistingExhibitionPageLayoutId)
            it.admin().exhibitionPageLayouts().assertFindFail(404, nonExistingExhibitionId, createdExhibitionPageLayoutId)
            assertNotNull(it.admin().exhibitionPageLayouts().findExhibitionPageLayout(exhibitionId, createdExhibitionPageLayoutId))
        }
    }

    @Test
    fun testListExhibitionPageLayouts() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().exhibitionPageLayouts().assertListFail(404, nonExistingExhibitionId)
            assertEquals(0, it.admin().exhibitionPageLayouts().listExhibitionPageLayouts(exhibitionId).size)

            val createdExhibitionPageLayout = it.admin().exhibitionPageLayouts().create(exhibitionId)
            val createdExhibitionPageLayoutId = createdExhibitionPageLayout.id!!
            val exhibitionPageLayout = it.admin().exhibitionPageLayouts().listExhibitionPageLayouts(exhibitionId)
            assertEquals(1, exhibitionPageLayout.size)
            assertEquals(createdExhibitionPageLayoutId, exhibitionPageLayout[0].id)
            it.admin().exhibitionPageLayouts().delete(exhibitionId, createdExhibitionPageLayoutId)
            assertEquals(0, it.admin().exhibitionPageLayouts().listExhibitionPageLayouts(exhibitionId).size)
        }
    }

    @Test
    fun testUpdateExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            val createdProperties = arrayOf(ExhibitionPageLayoutViewProperty("name", "true", ExhibitionPageLayoutViewPropertyType.boolean))
            val createdChildren = arrayOf(ExhibitionPageLayoutView("childid", "child", arrayOf(), arrayOf()))
            val createdData = ExhibitionPageLayoutView("rootid", "created widget", createdProperties, createdChildren)

            val createdExhibitionPageLayout = it.admin().exhibitionPageLayouts().create(exhibitionId, "created name", createdData)
            val createdExhibitionPageLayoutId = createdExhibitionPageLayout.id!!

            val foundCreatedExhibitionPageLayout = it.admin().exhibitionPageLayouts().findExhibitionPageLayout(exhibitionId, createdExhibitionPageLayoutId)
            assertEquals(createdExhibitionPageLayout.id, foundCreatedExhibitionPageLayout?.id)
            assertEquals("created name", createdExhibitionPageLayout.name)
            assertEquals("created widget", createdExhibitionPageLayout.data.widget)
            assertEquals(1, createdExhibitionPageLayout.data.properties.size)
            assertEquals("name", createdExhibitionPageLayout.data.properties[0].name)
            assertEquals("true", createdExhibitionPageLayout.data.properties[0].value)
            assertEquals(ExhibitionPageLayoutViewPropertyType.boolean, createdExhibitionPageLayout.data.properties[0].type)
            assertEquals(1, createdExhibitionPageLayout.data.children.size)
            assertEquals(createdChildren[0].id, createdExhibitionPageLayout.data.children[0].id)


            val updatedProperties = arrayOf(ExhibitionPageLayoutViewProperty("uname", "str", ExhibitionPageLayoutViewPropertyType.string))
            val updatedChildren = arrayOf<ExhibitionPageLayoutView>()
            val updatedData = ExhibitionPageLayoutView("updatedid", "updated widget", updatedProperties, updatedChildren)

            val updatedExhibitionPageLayout = it.admin().exhibitionPageLayouts().updateExhibitionPageLayout(exhibitionId, ExhibitionPageLayout("updated name", updatedData, createdExhibitionPageLayoutId))
            val foundUpdatedExhibitionPageLayout = it.admin().exhibitionPageLayouts().findExhibitionPageLayout(exhibitionId, createdExhibitionPageLayoutId)

            assertEquals(updatedExhibitionPageLayout!!.id, foundUpdatedExhibitionPageLayout?.id)
            assertEquals("updated name", updatedExhibitionPageLayout.name)
            assertEquals("updated widget", updatedExhibitionPageLayout.data.widget)
            assertEquals(1, updatedExhibitionPageLayout.data.properties.size)
            assertEquals("uname", updatedExhibitionPageLayout.data.properties[0].name)
            assertEquals("str", updatedExhibitionPageLayout.data.properties[0].value)
            assertEquals(ExhibitionPageLayoutViewPropertyType.string, updatedExhibitionPageLayout.data.properties[0].type)
            assertEquals(0, updatedExhibitionPageLayout.data.children.size)

            it.admin().exhibitionPageLayouts().assertUpdateFail(404, nonExistingExhibitionId, ExhibitionPageLayout("name", updatedData, createdExhibitionPageLayoutId))
        }
    }

    @Test
    fun testDeleteExhibition() {
        TestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdExhibitionPageLayout = it.admin().exhibitionPageLayouts().create(exhibitionId)
            val createdExhibitionPageLayoutId = createdExhibitionPageLayout.id!!

            assertNotNull(it.admin().exhibitionPageLayouts().findExhibitionPageLayout(exhibitionId, createdExhibitionPageLayoutId))
            it.admin().exhibitionPageLayouts().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().exhibitionPageLayouts().assertDeleteFail(404, nonExistingExhibitionId, createdExhibitionPageLayoutId)
            it.admin().exhibitionPageLayouts().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().exhibitionPageLayouts().delete(exhibitionId, createdExhibitionPageLayout)

            it.admin().exhibitionPageLayouts().assertDeleteFail(404, exhibitionId, createdExhibitionPageLayoutId)
        }
    }

}