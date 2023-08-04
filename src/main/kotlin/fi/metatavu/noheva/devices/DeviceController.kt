package fi.metatavu.noheva.devices

import fi.metatavu.noheva.api.spec.model.DeviceApprovalStatus
import fi.metatavu.noheva.api.spec.model.DeviceStatus
import fi.metatavu.noheva.persistence.dao.DeviceDAO
import fi.metatavu.noheva.persistence.model.Device
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Devices
 */
@ApplicationScoped
class DeviceController {

    @Inject
    lateinit var deviceDAO: DeviceDAO

    /**
     * Creates new device
     *
     * @param serialNumber serial number
     * @param name name
     * @param description description
     * @param version version
     * @return created device
     */
    fun createDevice(
        serialNumber: String,
        name: String?,
        description: String?,
        version: String
    ): Device {
        return deviceDAO.create(
            id = UUID.randomUUID(),
            serialNumber = serialNumber,
            name = name,
            description = description,
            version = version
        )
    }

    /**
     * Finds Device by serial number
     *
     * @param serialNumber serial number
     * @return found device or null if not found
     */
    fun findDevice(serialNumber: String): Device? {
        return deviceDAO.findBySerialNumber(serialNumber)
    }

    /**
     * Finds Device by id
     *
     * @param id id
     * @return found device or null if not found
     */
    fun findDevice(id: UUID): Device? {
        return deviceDAO.findById(id)
    }

    /**
     * Lists Devices with filters
     *
     * @param status status
     * @param approvalStatus approval status
     * @return found devices
     */
    fun listDevices(status: DeviceStatus?, approvalStatus: DeviceApprovalStatus?): List<Device> {
        return deviceDAO.list(status = status, approvalStatus = approvalStatus)
    }

    /**
     * Updates Device
     *
     * @param existingDevice existing device
     * @param newDevice new device
     * @param userId user id
     * @return updated device
     */
    fun updateDevice(
        existingDevice: Device,
        newDevice: fi.metatavu.noheva.api.spec.model.Device,
        userId: UUID
    ): Device {
        existingDevice.name = newDevice.name
        existingDevice.description = newDevice.description
        existingDevice.status = newDevice.status
        existingDevice.approvalStatus = newDevice.approvalStatus
        existingDevice.version = newDevice.version
        existingDevice.lastModifierId = userId

        return deviceDAO.update(existingDevice)
    }

    /**
     * Deletes Device
     *
     * @param device device
     */
    fun deleteDevice(device: Device) {
        return deviceDAO.delete(device)
    }
}