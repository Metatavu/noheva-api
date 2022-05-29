package fi.metatavu.muisti.visitors

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.VisitorVariableType
import fi.metatavu.muisti.persistence.dao.*
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.VisitorVariable
import fi.metatavu.muisti.utils.CopyException
import fi.metatavu.muisti.utils.IdMapper
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
     * @param editableFromUI whether the visitor variable should be editable from the customer service UI
     * @param creatorId creator id
     * @return created visitor variable
     */
    fun createVisitorVariable(exhibition: Exhibition, name: String, type: VisitorVariableType, enum: List<String>, editableFromUI: Boolean, creatorId: UUID): VisitorVariable {
        return visitorVariableDAO.create(
            id = UUID.randomUUID(),
            exhibition = exhibition,
            name = name,
            type = type,
            editableFromUI = editableFromUI,
            enum = getEnumAsString(enum),
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Copies visitor variable
     *
     * @param idMapper id mapper
     * @param sourceVisitorVariable source visitor variable
     * @param targetExhibition target exhibition
     * @param creatorId creator id
     * @return copied visitor variable
     */
    fun copyVisitorVariable(
        idMapper: IdMapper,
        sourceVisitorVariable: VisitorVariable,
        targetExhibition: Exhibition,
        creatorId: UUID
    ): VisitorVariable {
        val id = idMapper.getNewId(sourceVisitorVariable.id) ?: throw CopyException("Target visitor variable id not found")

        return visitorVariableDAO.create(
            id = id,
            exhibition = targetExhibition,
            name = sourceVisitorVariable.name ?: throw CopyException("Source visitor variable name not found"),
            type = sourceVisitorVariable.type ?: throw CopyException("Source visitor variable type not found"),
            editableFromUI = sourceVisitorVariable.editableFromUI ?: throw CopyException("Source visitor variable editableFromUI not found"),
            enum = sourceVisitorVariable.enum,
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
     * @param editableFromUI whether the visitor variable should be editable from the customer service UI
     * @param lastModifierId modifier user id
     * @return updated visitor variable
     */
    fun updateVisitorVariable(visitorVariable: VisitorVariable, name: String, type: VisitorVariableType, enum: List<String>?, editableFromUI: Boolean, lastModifierId: UUID): VisitorVariable {
        var result = visitorVariableDAO.updateName(visitorVariable, name, lastModifierId)
        result = visitorVariableDAO.updateType(result, type, lastModifierId)
        result = visitorVariableDAO.updateEnum(result, getEnumAsString(enum), lastModifierId)
        result = visitorVariableDAO.updateEditableFromUI(result, editableFromUI, lastModifierId)
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
