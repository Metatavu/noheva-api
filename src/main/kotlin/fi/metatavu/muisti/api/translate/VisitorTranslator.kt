package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.api.spec.model.Visitor
import fi.metatavu.muisti.keycloak.KeycloakController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA visitor -entities into REST resources
 */
@ApplicationScoped
class VisitorTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.Visitor, Visitor>() {

    @Inject
    lateinit var keycloakController: KeycloakController

    override fun translate(entity: fi.metatavu.muisti.persistence.model.Visitor): Visitor {
        val userRepresentation = keycloakController.findUserById(entity.userId)

        return Visitor(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            email = userRepresentation?.email,
            tagId = entity.tagId!!,
            userId = entity.userId,
            firstName = userRepresentation?.firstName,
            lastName = userRepresentation?.lastName,
            birthYear = keycloakController.getUserBirthYear(userRepresentation),
            language = keycloakController.getUserLanguage(userRepresentation),
            phone = keycloakController.getUserPhone(userRepresentation),
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

