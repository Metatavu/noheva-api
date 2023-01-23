package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.ExhibitionDeviceGroupsApi
import fi.metatavu.muisti.api.spec.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.api.translate.ExhibitionDeviceGroupTranslator
import fi.metatavu.muisti.contents.GroupContentVersionController
import fi.metatavu.muisti.devices.ExhibitionDeviceGroupController
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.exhibitions.ExhibitionRoomController
import fi.metatavu.muisti.realtime.RealtimeNotificationController
import fi.metatavu.muisti.utils.CopyException
import fi.metatavu.muisti.utils.IdMapper
import org.jboss.logging.Logger
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

@RequestScoped
class ExhibitionDevicesGroupsApiImpl : ExhibitionDeviceGroupsApi, AbstractApi() {

    @Inject
    lateinit var realtimeNotificationController: RealtimeNotificationController

    @Inject
    lateinit var exhibitionDeviceGroupController: ExhibitionDeviceGroupController

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var exhibitionRoomController: ExhibitionRoomController

    @Inject
    lateinit var exhibitionDeviceGroupTranslator: ExhibitionDeviceGroupTranslator

    @Inject
    lateinit var groupContentVersionController: GroupContentVersionController

    @Inject
    lateinit var logger: Logger

    /* V1 */
    override fun listExhibitionDeviceGroups(exhibitionId: UUID, roomId: UUID?): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        var room: fi.metatavu.muisti.persistence.model.ExhibitionRoom? = null

        if (roomId != null) {
            room = exhibitionRoomController.findExhibitionRoomById(roomId)
                ?: return createBadRequest("Could not find room $roomId")
        }

        val exhibitionDeviceGroups = exhibitionDeviceGroupController.listExhibitionDeviceGroups(exhibition, room)

        return createOk(exhibitionDeviceGroups.map(exhibitionDeviceGroupTranslator::translate))

    }

    override fun createExhibitionDeviceGroup(
        exhibitionId: UUID,
        sourceDeviceGroupId: UUID?,
        exhibitionDeviceGroup: ExhibitionDeviceGroup?
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        val deviceGroup = if (sourceDeviceGroupId != null) {
            val sourceDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(id = sourceDeviceGroupId)
                ?: return createBadRequest("Source device group $sourceDeviceGroupId not found")

            try {
                val idMapper = IdMapper()

                exhibitionDeviceGroupController.copyDependingContentVersions(
                    idMapper = idMapper,
                    sourceDeviceGroup = sourceDeviceGroup,
                    targetExhibition = exhibition,
                    creatorId = userId
                )

                exhibitionDeviceGroupController.copyDeviceGroup(
                    idMapper = idMapper,
                    sourceDeviceGroup = sourceDeviceGroup,
                    targetRoom = sourceDeviceGroup.room
                        ?: return createBadRequest("Source device group $sourceDeviceGroupId has no room"),
                    creatorId = userId
                )
            } catch (e: CopyException) {
                logger.error("Failed to copy device group", e)
                return createInternalServerError("Failed to copy device group")
            }
        } else {
            exhibitionDeviceGroup ?: return createBadRequest(MISSING_REQUEST_BODY)
            val room = exhibitionRoomController.findExhibitionRoomById(exhibitionDeviceGroup.roomId)
                ?: return createNotFound("Exhibition room ${exhibitionDeviceGroup.roomId} not found")

            exhibitionDeviceGroupController.createExhibitionDeviceGroup(
                exhibition = exhibition,
                name = exhibitionDeviceGroup.name,
                allowVisitorSessionCreation = exhibitionDeviceGroup.allowVisitorSessionCreation,
                room = room,
                visitorSessionEndTimeout = exhibitionDeviceGroup.visitorSessionEndTimeout,
                visitorSessionStartStrategy = exhibitionDeviceGroup.visitorSessionStartStrategy,
                indexPageTimeout = exhibitionDeviceGroup.indexPageTimeout,
                creatorId = userId
            )
        }

        realtimeNotificationController.notifyDeviceGroupCreate(id = deviceGroup.id!!, exhibitionId = exhibitionId)
        return createOk(exhibitionDeviceGroupTranslator.translate(deviceGroup))

    }

    override fun findExhibitionDeviceGroup(exhibitionId: UUID, deviceGroupId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId)
            ?: return createNotFound("Room $deviceGroupId not found")

        if (!exhibitionDeviceGroup.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("Room not found")
        }

        return createOk(exhibitionDeviceGroupTranslator.translate(exhibitionDeviceGroup))
    }

    override fun updateExhibitionDeviceGroup(
        exhibitionId: UUID,
        deviceGroupId: UUID,
        exhibitionDeviceGroup: ExhibitionDeviceGroup
    ): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val room = exhibitionRoomController.findExhibitionRoomById(exhibitionDeviceGroup.roomId)
            ?: return createNotFound("Exhibition room ${exhibitionDeviceGroup.roomId} not found")
        val visitorSessionEndTimeout = exhibitionDeviceGroup.visitorSessionEndTimeout
        val visitorSessionStartStrategy = exhibitionDeviceGroup.visitorSessionStartStrategy
        val foundExhibitionDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId)
            ?: return createNotFound("Room $deviceGroupId not found")
        val result = exhibitionDeviceGroupController.updateExhibitionDeviceGroup(
            exhibitionDeviceGroup = foundExhibitionDeviceGroup,
            room = room,
            name = exhibitionDeviceGroup.name,
            allowVisitorSessionCreation = exhibitionDeviceGroup.allowVisitorSessionCreation,
            visitorSessionEndTimeout = visitorSessionEndTimeout,
            visitorSessionStartStrategy = visitorSessionStartStrategy,
            indexPageTimeout = exhibitionDeviceGroup.indexPageTimeout,
            modifierId = userId
        )

        realtimeNotificationController.notifyDeviceGroupUpdate(id = deviceGroupId, exhibitionId = exhibitionId)

        return createOk(exhibitionDeviceGroupTranslator.translate(result))
    }

    override fun deleteExhibitionDeviceGroup(exhibitionId: UUID, deviceGroupId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val exhibitionDeviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId)
            ?: return createNotFound("Room $deviceGroupId not found")

        val groupContentVersions = groupContentVersionController.listGroupContentVersions(
            exhibition = exhibition,
            contentVersion = null,
            deviceGroup = exhibitionDeviceGroup
        )

        if (groupContentVersions.isNotEmpty()) {
            val groupContentVersionIds = groupContentVersions.map { it.id }.joinToString()
            return createBadRequest("Cannot delete device group $deviceGroupId because it's used in group content versions $groupContentVersionIds")
        }

        exhibitionDeviceGroupController.deleteExhibitionDeviceGroup(exhibitionDeviceGroup)

        realtimeNotificationController.notifyDeviceGroupDelete(id = deviceGroupId, exhibitionId = exhibitionId)

        return createNoContent()
    }
}
