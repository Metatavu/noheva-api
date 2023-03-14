package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.Visitor
import fi.metatavu.noheva.keycloak.KeycloakController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA visitor -entities into REST resources
 */
@ApplicationScoped
class VisitorTranslator: AbstractTranslator<fi.metatavu.noheva.persistence.model.Visitor, Visitor>() {

    @Inject
    lateinit var keycloakController: KeycloakController

    override fun translate(entity: fi.metatavu.noheva.persistence.model.Visitor): Visitor {
        val userRepresentation = keycloakController.findUserById(entity.userId)

        return Visitor(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            email = userRepresentation?.email ?: "",
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

