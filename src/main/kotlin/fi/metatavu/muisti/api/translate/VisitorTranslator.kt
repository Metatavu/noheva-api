package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.keycloak.KeycloakController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA visitor -entities into REST resources
 */
@ApplicationScoped
class VisitorTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.Visitor, fi.metatavu.muisti.api.spec.model.Visitor>() {

    @Inject
    private lateinit var keycloakController: KeycloakController

    override fun translate(entity: fi.metatavu.muisti.persistence.model.Visitor): fi.metatavu.muisti.api.spec.model.Visitor {
        val userRepresentation = keycloakController.findUserById(entity.userId)

        val result: fi.metatavu.muisti.api.spec.model.Visitor = fi.metatavu.muisti.api.spec.model.Visitor()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.email = userRepresentation?.email
        result.tagId = entity.tagId
        result.userId = entity.userId
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt

        return result
    }

}

