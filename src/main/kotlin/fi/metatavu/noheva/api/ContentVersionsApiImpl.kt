package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.ContentVersionsApi
import fi.metatavu.noheva.api.spec.model.ContentVersion
import fi.metatavu.noheva.api.translate.ContentVersionTranslator
import fi.metatavu.noheva.contents.ContentVersionController
import fi.metatavu.noheva.devices.ExhibitionDeviceGroupController
import fi.metatavu.noheva.exhibitions.ExhibitionController
import fi.metatavu.noheva.exhibitions.ExhibitionRoomController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Content Versions API implementation
 */
@RequestScoped
@Transactional
@Suppress("unused")
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
    lateinit var exhibitionDeviceGroupController: ExhibitionDeviceGroupController

    override fun listContentVersions(exhibitionId: UUID, roomId: UUID?, deviceGroupId: UUID?): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val room = if (roomId != null ) {
            exhibitionRoomController.findExhibitionRoomById(roomId) ?: return createNotFound("Room $roomId not found")
        } else {
            null
        }

        val deviceGroup = if (deviceGroupId != null) {
            exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createNotFound("Device group $deviceGroupId not found")
        } else {
            null
        }

        val contentVersions = contentVersionController.listContentVersions(exhibition, room, deviceGroup)
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
        val deviceGroupId = contentVersion.deviceGroupId

        var deviceGroup: fi.metatavu.noheva.persistence.model.ExhibitionDeviceGroup? = null
        if (deviceGroupId != null) {
            deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId)?: return createBadRequest("Device group $deviceGroupId not found")
        }

        val exhibitionRooms = mutableListOf<fi.metatavu.noheva.persistence.model.ExhibitionRoom>()
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
            status = contentVersion.status,
            deviceGroup = deviceGroup,
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
        val deviceGroupId = contentVersion.deviceGroupId
        val deviceGroup = if (deviceGroupId != null) {
            exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createBadRequest("Invalid exhibition group id $deviceGroupId")
        } else null

        val exhibitionRooms = mutableListOf<fi.metatavu.noheva.persistence.model.ExhibitionRoom>()
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
            status = contentVersion.status,
            deviceGroup = deviceGroup,
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
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val contentVersion = contentVersionController.findContentVersionById(contentVersionId) ?: return createNotFound(
            "Content version $contentVersionId not found"
        )

        contentVersionController.deleteContentVersion(contentVersion)
        return createNoContent()
    }
}
