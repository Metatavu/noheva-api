package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.api.spec.model.ContentVersionActiveCondition
import fi.metatavu.muisti.persistence.dao.ContentVersionRoomDAO
import fi.metatavu.muisti.persistence.model.ContentVersion
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.streams.toList

/**
 * Translator for translating JPA content version entities into REST resources
 */
@ApplicationScoped
class ContentVersionTranslator: AbstractTranslator<ContentVersion, fi.metatavu.muisti.api.spec.model.ContentVersion>() {

    @Inject
    private lateinit var contentVersionRoomDAO: ContentVersionRoomDAO

    override fun translate(entity: ContentVersion): fi.metatavu.muisti.api.spec.model.ContentVersion {
        val result: fi.metatavu.muisti.api.spec.model.ContentVersion = fi.metatavu.muisti.api.spec.model.ContentVersion()

        val roomIds = contentVersionRoomDAO.listRoomsByContentVersion(entity)
            .map { it.exhibitionRoom?.id!! }
            .toList()

        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.activeCondition = getActiveCondition(entity)
        result.name = entity.name
        result.language = entity.language
        result.rooms = roomIds
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

    /**
     * Returns content version active condition
     *
     * @param entity entity
     * @return content version active condition
     */
    private fun getActiveCondition(entity: ContentVersion): ContentVersionActiveCondition? {
        entity.activeConditionUserVariable ?: return null
        val result = ContentVersionActiveCondition()
        result.equals = entity.activeConditionEquals
        result.userVariable = entity.activeConditionUserVariable
        return result
    }

}

