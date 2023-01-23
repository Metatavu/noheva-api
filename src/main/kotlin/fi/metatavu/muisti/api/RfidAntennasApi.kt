package fi.metatavu.muisti.api


import fi.metatavu.muisti.api.spec.RfidAntennasApi
import fi.metatavu.muisti.api.spec.model.RfidAntenna
import fi.metatavu.muisti.api.translate.RfidAntennaTranslator
import fi.metatavu.muisti.devices.ExhibitionDeviceGroupController
import fi.metatavu.muisti.devices.RfidAntennaController
import fi.metatavu.muisti.exhibitions.ExhibitionController
import fi.metatavu.muisti.exhibitions.ExhibitionRoomController
import fi.metatavu.muisti.realtime.RealtimeNotificationController
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

@RequestScoped
class RfidAntennasApi : RfidAntennasApi, AbstractApi() {

    @Inject
    lateinit var exhibitionController: ExhibitionController

    @Inject
    lateinit var exhibitionDeviceGroupController: ExhibitionDeviceGroupController

    @Inject
    lateinit var exhibitionRoomController: ExhibitionRoomController

    @Inject
    lateinit var rfidAntennaController: RfidAntennaController

    @Inject
    lateinit var rfidAntennaTranslator: RfidAntennaTranslator

    @Inject
    lateinit var realtimeNotificationController: RealtimeNotificationController

    override fun listRfidAntennas(exhibitionId: UUID, roomId: UUID?, deviceGroupId: UUID?): Response {
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        var deviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (deviceGroupId != null) {
            deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(deviceGroupId) ?: return createBadRequest(
                "Invalid device group id $deviceGroupId"
            )
        }

        var room: fi.metatavu.muisti.persistence.model.ExhibitionRoom? = null
        if (roomId != null) {
            room = exhibitionRoomController.findExhibitionRoomById(roomId)
                ?: return createBadRequest("Invalid room id $roomId")
        }

        val rfidAntennas = rfidAntennaController.listRfidAntennas(
            exhibition = exhibition,
            room = room,
            deviceGroup = deviceGroup
        )

        return createOk(rfidAntennas.map(rfidAntennaTranslator::translate))
    }

    override fun createRfidAntenna(exhibitionId: UUID, rfidAntenna: RfidAntenna): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        if (rfidAntenna.name.isEmpty()) {
            return createBadRequest("Name cannot be empty")
        }

        if (rfidAntenna.readerId.isEmpty()) {
            return createBadRequest("ReaderId cannot be empty")
        }

        var deviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (rfidAntenna.groupId != null) {
            deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(rfidAntenna.groupId)
                ?: return createBadRequest("Invalid device group id ${rfidAntenna.groupId}")
        }

        val room = exhibitionRoomController.findExhibitionRoomById(rfidAntenna.roomId)
            ?: return createBadRequest("Invalid room id ${rfidAntenna.roomId}")

        val visitorSessionStartThreshold = rfidAntenna.visitorSessionStartThreshold
        val visitorSessionEndThreshold = rfidAntenna.visitorSessionEndThreshold

        val created = rfidAntennaController.createRfidAntenna(
            exhibition = exhibition,
            deviceGroup = deviceGroup,
            room = room,
            name = rfidAntenna.name,
            readerId = rfidAntenna.readerId,
            antennaNumber = rfidAntenna.antennaNumber,
            location = rfidAntenna.location,
            visitorSessionStartThreshold = visitorSessionStartThreshold,
            visitorSessionEndThreshold = visitorSessionEndThreshold,
            creatorId = userId
        )

        realtimeNotificationController.notifyRfidAntennaCreate(id = created.id!!, exhibitionId = exhibitionId)

        return createOk(rfidAntennaTranslator.translate(created))
    }

    override fun findRfidAntenna(exhibitionId: UUID, rfidAntennaId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val rfidAntenna = rfidAntennaController.findRfidAntennaById(rfidAntennaId)
            ?: return createNotFound("RFID antenna $rfidAntennaId not found")

        if (!rfidAntenna.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("RFID antenna $rfidAntennaId not found")
        }

        return createOk(rfidAntennaTranslator.translate(rfidAntenna))
    }

    override fun updateRfidAntenna(exhibitionId: UUID, rfidAntennaId: UUID, rfidAntenna: RfidAntenna): Response {
        val userId = loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")

        val rfidAntennaFound = rfidAntennaController.findRfidAntennaById(rfidAntennaId)
            ?: return createNotFound("RFID antenna $rfidAntennaId not found")

        if (!rfidAntennaFound.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("RFID antenna $rfidAntennaId not found")
        }

        if (rfidAntenna.name.isEmpty()) {
            return createBadRequest("Name cannot be empty")
        }

        if (rfidAntenna.readerId.isEmpty()) {
            return createBadRequest("ReaderId cannot be empty")
        }

        var deviceGroup: fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup? = null
        if (rfidAntenna.groupId != null) {
            deviceGroup = exhibitionDeviceGroupController.findDeviceGroupById(rfidAntenna.groupId)
                ?: return createBadRequest("Invalid device group id ${rfidAntenna.groupId}")
        }

        val room = exhibitionRoomController.findExhibitionRoomById(rfidAntenna.roomId)
            ?: return createBadRequest("Invalid room id ${rfidAntenna.roomId}")

        val visitorSessionStartThreshold = rfidAntenna.visitorSessionStartThreshold
        val visitorSessionEndThreshold = rfidAntenna.visitorSessionEndThreshold

        val groupChanged = rfidAntennaFound.deviceGroup?.id != deviceGroup?.id

        val result = rfidAntennaController.updateRfidAntenna(
            rfidAntenna = rfidAntennaFound,
            deviceGroup = deviceGroup,
            room = room,
            name = rfidAntenna.name,
            readerId = rfidAntenna.readerId,
            antennaNumber = rfidAntenna.antennaNumber,
            location = rfidAntenna.location,
            visitorSessionStartThreshold = visitorSessionStartThreshold,
            visitorSessionEndThreshold = visitorSessionEndThreshold,
            modifierId = userId
        )

        realtimeNotificationController.notifyRfidAntennaUpdate(
            id = rfidAntennaId,
            exhibitionId = exhibitionId,
            groupChanged = groupChanged
        )

        return createOk(rfidAntennaTranslator.translate(result))
    }

    override fun deleteRfidAntenna(exhibitionId: UUID, rfidAntennaId: UUID): Response {
        loggedUserId ?: return createUnauthorized(UNAUTHORIZED)
        val exhibition = exhibitionController.findExhibitionById(exhibitionId)
            ?: return createNotFound("Exhibition $exhibitionId not found")
        val rfidAntenna = rfidAntennaController.findRfidAntennaById(rfidAntennaId)
            ?: return createNotFound("RFID antenna $rfidAntennaId not found")
        if (!rfidAntenna.exhibition?.id?.equals(exhibition.id)!!) {
            return createNotFound("RFID antenna $rfidAntennaId not found")
        }

        rfidAntennaController.deleteRfidAntenna(rfidAntenna)
        realtimeNotificationController.notifyRfidAntennaDelete(id = rfidAntennaId, exhibitionId = exhibitionId)

        return createNoContent()
    }
}
