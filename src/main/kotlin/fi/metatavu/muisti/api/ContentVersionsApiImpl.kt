package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.ContentVersionsApi
import fi.metatavu.muisti.api.spec.model.ContentVersion
import fi.metatavu.muisti.api.translate.ContentVersionTranslator
import fi.metatavu.muisti.contents.ContentVersionController
import fi.metatavu.muisti.contents.GroupContentVersionController
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.exhibitions.ExhibitionRoomController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response


@RequestScoped
class ContentVersionsApiImpl : ContentVersionsApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var exhibitionRoomController: ExhibitionRoomController

    @Inject
    lateinit var contentVersionController: ContentVersionController

    @Inject
    lateinit var contentVersionTranslator: ContentVersionTranslator

    @Inject
    lateinit var groupContentVersionController: GroupContentVersionController

    //V1
    override fun listContentVersions(exhibitionId: UUID, roomId: UUID?): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val room = exhibitionRoomController.findExhibitionRoomById(roomId)
        val contentVersions = contentVersionController.listContentVersions(exhibition, room)
        return createOk(contentVersions.map(contentVersionTranslator::translate))
    }

    override fun createContentVersion(
        exhibitionId: UUID,
        contentVersion: ContentVersion
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val name = contentVersion.name
        val language = contentVersion.language

        val exhibitionRooms = mutableListOf<fi.metatavu.muisti.persistence.model.ExhibitionRoom>()
        for (roomId in contentVersion.rooms) {
            val room = exhibitionRoomController.findExhibitionRoomById(roomId)
            room ?: return createBadRequest("Invalid room $roomId")
            exhibitionRooms.add(room)

            val anotherContentVersion = contentVersionController.findContentVersionByNameRoomAndLanguage(
                name = name,
                language = language,
                room = room
            )

            if (anotherContentVersion != null) {
                return createBadRequest("Content version with same name and language already exists in given room")
            }
        }

        val result = contentVersionController.createContentVersion(
            exhibition = exhibition,
            name = name,
            language = language,
            activeCondition = contentVersion.activeCondition,
            creatorId = userId
        )

        contentVersionController.setContentVersionRooms(result, exhibitionRooms)
        return createOk(contentVersionTranslator.translate(result))
    }

    override fun findContentVersion(exhibitionId: UUID, contentVersionId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound(
            "Content version $contentVersionId not found"
        )

        if (!contentVersion.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound(CONTENT_VERSION_NOT_FOUND)
        }

        return createOk(contentVersionTranslator.translate(contentVersion))
    }

    override fun updateContentVersion(
        exhibitionId: UUID,
        contentVersionId: UUID,
        contentVersion: ContentVersion
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val foundContentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound(
            "Content version $contentVersionId not found"
        )
        val name = contentVersion.name
        val language = contentVersion.language
        val exhibitionRooms = mutableListOf<fi.metatavu.muisti.persistence.model.ExhibitionRoom>()
        for (roomId in contentVersion.rooms) {
            val room = exhibitionRoomController.findExhibitionRoomById(roomId)
            room ?: return createBadRequest("Invalid room id $roomId")
            exhibitionRooms.add(room)

            val anotherContentVersion = contentVersionController.findContentVersionByNameRoomAndLanguage(
                name = name,
                language = language,
                room = room
            )

            if (anotherContentVersion != null && contentVersionId != foundContentVersion.id) {
                return createBadRequest("Another content version with same name and language already exists in given room")
            }
        }

        val result = contentVersionController.updateContentVersion(
            contentVersion = foundContentVersion,
            name = name,
            language = language,
            activeCondition = contentVersion.activeCondition,
            modifierId = userId
        )

        contentVersionController.setContentVersionRooms(result, exhibitionRooms)
        return createOk(contentVersionTranslator.translate(result))
    }

    override fun deleteContentVersion(
        exhibitionId: UUID,
        contentVersionId: UUID
    ): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound(
            "Content version $contentVersionId not found"
        )

        val groupContentVersions = groupContentVersionController.listGroupContentVersions(
            contentVersion = contentVersion,
            exhibition = exhibition,
            deviceGroup = null
        )

        if (groupContentVersions.isNotEmpty()) {
            val groupContentVersionIds = groupContentVersions.map { it.id }.joinToString()
            return createBadRequest("Cannot delete content version $contentVersionId because it's used in group content versions $groupContentVersionIds")
        }

        contentVersionController.deleteContentVersion(contentVersion)
        return createNoContent()
    }
}
