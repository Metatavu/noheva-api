package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.ContentVersionActiveCondition
import fi.metatavu.noheva.persistence.dao.ContentVersionRoomDAO
import fi.metatavu.noheva.persistence.model.ContentVersion
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for translating JPA content version entities into REST resources
 */
@ApplicationScoped
class ContentVersionTranslator :
    AbstractTranslator<ContentVersion, fi.metatavu.noheva.api.spec.model.ContentVersion>() {

    @Inject
    lateinit var contentVersionRoomDAO: ContentVersionRoomDAO

    override fun translate(entity: ContentVersion): fi.metatavu.noheva.api.spec.model.ContentVersion {

        val roomIds = contentVersionRoomDAO.listRoomsByContentVersion(entity)
            .map { it.exhibitionRoom?.id!! }
            .toList()

        return fi.metatavu.noheva.api.spec.model.ContentVersion(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            activeCondition = getActiveCondition(entity),
            name = entity.name!!,
            language = entity.language!!,
            rooms = roomIds,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

    /**
     * Returns content version active condition
     *
     * @param entity entity
     * @return content version active condition
     */
    private fun getActiveCondition(entity: ContentVersion): ContentVersionActiveCondition? {
        entity.activeConditionUserVariable ?: return null
        return ContentVersionActiveCondition(
            equals = entity.activeConditionEquals,
            userVariable = entity.activeConditionUserVariable!!
        )
    }

}

