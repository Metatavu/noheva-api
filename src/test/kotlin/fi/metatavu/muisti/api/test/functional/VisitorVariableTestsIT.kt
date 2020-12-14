package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.client.models.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Test class for testing variable sessions API
 *
 * @author Antti Leppä
 */
class VariableVariableTestsIT: AbstractFunctionalTest() {

    @Test
    fun testCreateVisitorVariable() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!

            val createdVisitorVariable = it.admin().visitorVariables().create(exhibitionId, VisitorVariable(
                name = "bool",
                type = VisitorVariableType.boolean
            ))

            assertNotNull(createdVisitorVariable)
            assertEquals("bool", createdVisitorVariable.name)
            assertEquals(VisitorVariableType.boolean, createdVisitorVariable.type)

            it.admin().exhibitions().assertCreateFail(400, "")
        }
   }

    @Test
    fun testFindVisitorVariable() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingVisitorVariableId = UUID.randomUUID()
            val createdVisitorVariable = it.admin().visitorVariables().create(exhibitionId, VisitorVariable(name = "var", type = VisitorVariableType.number))
            val createdVisitorVariableId = createdVisitorVariable.id!!

            it.admin().visitorVariables().assertFindFail(404, exhibitionId, nonExistingVisitorVariableId)
            it.admin().visitorVariables().assertFindFail(404, nonExistingExhibitionId, nonExistingVisitorVariableId)
            it.admin().visitorVariables().assertFindFail(404, nonExistingExhibitionId, createdVisitorVariableId)
            assertNotNull(it.admin().visitorVariables().findVisitorVariable(exhibitionId, createdVisitorVariableId))
        }
    }

    @Test
    fun testListVisitorVariables() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val anotherExhibition = it.admin().exhibitions().create()
            val anotherExhibitionId = anotherExhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()

            it.admin().visitorVariables().assertListFail(404, exhibitionId = nonExistingExhibitionId, name = null)
            assertEquals(0, it.admin().visitorVariables().listVisitorVariables(exhibitionId = exhibitionId, name = null).size)

            val createdVisitorVariable = it.admin().visitorVariables().create(exhibitionId, VisitorVariable(name = "name", type = VisitorVariableType.number))
            val createdVisitorVariableId = createdVisitorVariable.id!!

            assertEquals(1, it.admin().visitorVariables().listVisitorVariables(exhibitionId = exhibitionId, name = null).size)
            assertEquals(1, it.admin().visitorVariables().listVisitorVariables(exhibitionId = exhibitionId, name = "name").size)
            assertEquals(0, it.admin().visitorVariables().listVisitorVariables(exhibitionId = exhibitionId, name = "another").size)
            assertEquals(0, it.admin().visitorVariables().listVisitorVariables(exhibitionId = anotherExhibitionId, name = "name").size)
            assertEquals(0, it.admin().visitorVariables().listVisitorVariables(exhibitionId = anotherExhibitionId, name = "another").size)

            it.admin().visitorVariables().delete(exhibitionId, createdVisitorVariableId)
            assertEquals(0, it.admin().visitorVariables().listVisitorVariables(exhibitionId = exhibitionId, name = null).size)
        }
    }

    @Test
    fun testUpdateVisitorVariable() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!

            val createdVisitorVariable = it.admin().visitorVariables().create(exhibitionId, VisitorVariable(
                name = "bool",
                type = VisitorVariableType.text,
                enum = arrayOf("one", "two")
            ))

            assertEquals("bool", createdVisitorVariable.name)
            assertEquals(VisitorVariableType.text, createdVisitorVariable.type)
            assertArrayEquals(arrayOf("one", "two"), createdVisitorVariable.enum)

            val updatedVisitorVariable = it.admin().visitorVariables().updateVisitorVariable(exhibitionId = exhibitionId, body = createdVisitorVariable.copy(name = "upd", type = VisitorVariableType.number, enum = arrayOf("one", "three")))
            assertEquals("upd", updatedVisitorVariable?.name)
            assertEquals(VisitorVariableType.number, updatedVisitorVariable?.type)
            assertArrayEquals(arrayOf("one", "three"), updatedVisitorVariable?.enum)

            val foundVisitorVariable = it.admin().visitorVariables().findVisitorVariable(exhibitionId = exhibitionId, visitorVariableId = createdVisitorVariable.id!!)
            assertEquals("upd", foundVisitorVariable?.name)
            assertEquals(VisitorVariableType.number, foundVisitorVariable?.type)
            assertArrayEquals(arrayOf("one", "three"), foundVisitorVariable?.enum)
        }
    }

    @Test
    fun testDeleteVisitorVariable() {
        ApiTestBuilder().use {
            val exhibition = it.admin().exhibitions().create()
            val exhibitionId = exhibition.id!!
            val nonExistingExhibitionId = UUID.randomUUID()
            val nonExistingSessionVariableId = UUID.randomUUID()
            val createdVisitorVariable = it.admin().visitorVariables().create(exhibitionId, VisitorVariable(
                name = "bool",
                type = VisitorVariableType.boolean
            ))

            val createdVisitorVariableId = createdVisitorVariable.id!!

            assertNotNull(it.admin().visitorVariables().findVisitorVariable(exhibitionId, createdVisitorVariableId))
            it.admin().visitorVariables().assertDeleteFail(404, exhibitionId, nonExistingSessionVariableId)
            it.admin().visitorVariables().assertDeleteFail(404, nonExistingExhibitionId, createdVisitorVariableId)
            it.admin().visitorVariables().assertDeleteFail(404, nonExistingExhibitionId, nonExistingSessionVariableId)

            it.admin().visitorVariables().delete(exhibitionId, createdVisitorVariable)

            it.admin().visitorVariables().assertDeleteFail(404, exhibitionId, createdVisitorVariableId)
        }
    }
}
