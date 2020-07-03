package fi.metatavu.muisti.api.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.muisti.api.spec.model.ExhibitionPageResource
import fi.metatavu.muisti.persistence.dao.ContentVersionRoomDAO
import fi.metatavu.muisti.persistence.dao.VisitorSessionVisitorDAO
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.streams.toList

/**
 * Translator for translating JPA content version entities into REST resources
 */
@ApplicationScoped
class ContentVersionTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.ContentVersion, fi.metatavu.muisti.api.spec.model.ContentVersion>() {

    @Inject
    private lateinit var contentVersionRoomDAO: ContentVersionRoomDAO

    override fun translate(entity: fi.metatavu.muisti.persistence.model.ContentVersion): fi.metatavu.muisti.api.spec.model.ContentVersion {
        val result: fi.metatavu.muisti.api.spec.model.ContentVersion = fi.metatavu.muisti.api.spec.model.ContentVersion()

        val roomIds = contentVersionRoomDAO.listRoomsByContentVersion(entity).stream()
            .map { it.exhibitionRoom?.id!! }
            .toList()

        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.name = entity.name
        result.language = entity.language
        result.rooms = roomIds
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt
        return result
    }

}

