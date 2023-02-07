package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import fi.metatavu.muisti.api.spec.model.VisitorVariable
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA visitor variable entities into REST resources
 */
@ApplicationScoped
class VisitorVariableTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.VisitorVariable, VisitorVariable>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.VisitorVariable): VisitorVariable {
        return VisitorVariable(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            name = entity.name!!,
            type = entity.type!!,
            enum = getEnum(entity.enum),
            editableFromUI = entity.editableFromUI!!,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

    /**
     * Deserializes the enum from JSON string
     *
     * @param enum enum string
     * @return deserialized enum
     */
    private fun getEnum(enum: String?): List<String>? {
        enum ?: return null
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(enum, jacksonTypeRef<List<String>>())
    }

}

