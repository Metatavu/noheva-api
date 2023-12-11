package fi.metatavu.noheva.api.translate

import fi.metatavu.noheva.api.spec.model.DeviceStatus
import fi.metatavu.noheva.persistence.model.Device
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for translating JPA device entities into REST resources
 */
@ApplicationScoped
class DeviceTranslator: AbstractTranslator<Device, fi.metatavu.noheva.api.spec.model.Device>() {
    override fun translate(entity: Device): fi.metatavu.noheva.api.spec.model.Device {
        val deviceLastSeen = entity.lastSeen
        val deviceStatus = if (deviceLastSeen.isBefore(OffsetDateTime.now().minusMinutes(2))) {
            DeviceStatus.OFFLINE
        } else {
            entity.status
        }

        return fi.metatavu.noheva.api.spec.model.Device(
            id = entity.id,
            deviceModelId = entity.deviceModel?.id,
            name = entity.name,
            serialNumber = entity.serialNumber,
            deviceType = entity.deviceType,
            description = entity.description,
            status = deviceStatus,
            approvalStatus = entity.approvalStatus,
            version = entity.version,
            lastSeen = entity.lastSeen,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            usageHours = entity.usageHours,
            warrantyExpiry = entity.warrantyExpiry,
            lastConnected = entity.lastConnected
        )
    }
}