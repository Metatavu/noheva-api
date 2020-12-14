package fi.metatavu.muisti.visitors

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.VisitorVariableType
import fi.metatavu.muisti.persistence.dao.*
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.VisitorVariable
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for visitor variables
 */
@ApplicationScoped
class VisitorVariableController {

    @Inject
    private lateinit var visitorVariableDAO: VisitorVariableDAO
    
    /**
     * Creates a new visitor variable
     *
     * @param exhibition exhibition
     * @param name name
     * @param type type
     * @param enum enumerated type allowed values
     * @param creatorId creator id
     * @return created visitor variable
     */
    fun createVisitorVariable(exhibition: Exhibition, name: String, type: VisitorVariableType, enum: List<String>, creatorId: UUID): VisitorVariable {
        return visitorVariableDAO.create(
            id = UUID.randomUUID(),
            exhibition = exhibition,
            name = name,
            type = type,
            enum = getEnumAsString(enum),
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Finds a visitor variable by id.
     *
     * @param id id
     * @return visitor variable or null if not found
     */
    fun findVisitorVariableById(id: UUID): VisitorVariable? {
        return visitorVariableDAO.findById(id = id)
    }

    /**
     * Finds a visitor variable by exhibition and name
     *
     * @param exhibition exhibition
     * @param name name
     * @return visitor variable or null if not found
     */
    fun findVisitorVariableByExhibitionAndName(exhibition: Exhibition, name: String): VisitorVariable? {
        return visitorVariableDAO.list(exhibition = exhibition, name = name).firstOrNull()
    }

    /**
     * Lists visitor variables
     *
     * @param exhibition filter by exhibition
     * @param name filter by name
     * @return list of visitor variables
     */
    fun listVisitorVariables(exhibition: Exhibition, name: String?): List<VisitorVariable> {
        return visitorVariableDAO.list(exhibition = exhibition, name = name)
    }

    /**
     * Updates visitor variable
     *
     * @param visitorVariable visitor variable to be updated
     * @param name new name
     * @param type type
     * @param enum enumerated type allowed values
     * @param lastModifierId modifier user id
     * @return updated visitor variable
     */
    fun updateVisitorVariable(visitorVariable: VisitorVariable, name: String, type: VisitorVariableType, enum: List<String>?, lastModifierId: UUID): VisitorVariable {
        var result = visitorVariableDAO.updateName(visitorVariable, name, lastModifierId)
        result = visitorVariableDAO.updateType(result, type, lastModifierId)
        result = visitorVariableDAO.updateEnum(result, getEnumAsString(enum), lastModifierId)
        return result
    }

    /**
     * Deletes visitor variable
     *
     * @param visitorVariable visitor variable
     */
    fun deleteVisitorVariable(visitorVariable: VisitorVariable) {
        visitorVariableDAO.delete(visitorVariable)
    }

    /**
     * Serializes the enum JSON string
     *
     * @param enum enum
     * @return JSON string
     */
    private fun getEnumAsString(enum: List<String>?): String? {
        enum ?: return null
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(enum)
    }

}
