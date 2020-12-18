package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA visitor variable entities into REST resources
 */
@ApplicationScoped
class VisitorVariableTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.VisitorVariable, fi.metatavu.muisti.api.spec.model.VisitorVariable>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.VisitorVariable): fi.metatavu.muisti.api.spec.model.VisitorVariable {
        val result: fi.metatavu.muisti.api.spec.model.VisitorVariable = fi.metatavu.muisti.api.spec.model.VisitorVariable()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.type = entity.type
        result.enum = getEnum(entity.enum)
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
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

