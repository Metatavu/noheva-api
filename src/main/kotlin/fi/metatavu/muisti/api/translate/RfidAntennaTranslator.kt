package fi.metatavu.muisti.api.translate

import fi.metatavu.muisti.api.spec.model.Point
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA RFID antennas into REST resources
 */
@ApplicationScoped
class RfidAntennaTranslator: AbstractTranslator<fi.metatavu.muisti.persistence.model.RfidAntenna, fi.metatavu.muisti.api.spec.model.RfidAntenna>() {

    override fun translate(entity: fi.metatavu.muisti.persistence.model.RfidAntenna): fi.metatavu.muisti.api.spec.model.RfidAntenna {
        val location = Point()
        location.x = entity.locationX
        location.y = entity.locationY

        val result: fi.metatavu.muisti.api.spec.model.RfidAntenna = fi.metatavu.muisti.api.spec.model.RfidAntenna()
        result.id = entity.id
        result.exhibitionId = entity.exhibition?.id
        result.groupId = entity.deviceGroup?.id
        result.roomId = entity.room?.id
        result.readerId = entity.readerId
        result.antennaNumber = entity.antennaNumber
        result.name = entity.name
        result.location = location
        result.visitorSessionStartThreshold = entity.visitorSessionStartThreshold
        result.visitorSessionEndThreshold = entity.visitorSessionEndThreshold
        result.creatorId = entity.creatorId
        result.lastModifierId = entity.lastModifierId
        result.createdAt = entity.createdAt
        result.modifiedAt = entity.modifiedAt

        return result
    }

}

