package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.Point
import fi.metatavu.noheva.api.spec.model.RfidAntenna
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA RFID antennas into REST resources
 */
@ApplicationScoped
class RfidAntennaTranslator :
    AbstractTranslator<fi.metatavu.noheva.persistence.model.RfidAntenna, RfidAntenna>() {

    override fun translate(entity: fi.metatavu.noheva.persistence.model.RfidAntenna): RfidAntenna {
        val location = Point(
            x = entity.locationX,
            y = entity.locationY
        )

        return RfidAntenna(
            id = entity.id,
            exhibitionId = entity.exhibition?.id,
            groupId = entity.deviceGroup?.id,
            roomId = entity.room!!.id!!,
            readerId = entity.readerId!!,
            antennaNumber = entity.antennaNumber!!,
            name = entity.name!!,
            location = location,
            visitorSessionStartThreshold = entity.visitorSessionStartThreshold!!,
            visitorSessionEndThreshold = entity.visitorSessionEndThreshold!!,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

}

