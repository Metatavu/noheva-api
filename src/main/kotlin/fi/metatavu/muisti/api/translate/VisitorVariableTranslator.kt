package fi.metatavu.muisti.api.translate

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
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

}

