package fi.metatavu.noheva.devices

import fi.metatavu.noheva.api.spec.model.DeviceApprovalStatus
import fi.metatavu.noheva.api.spec.model.DeviceStatus
import fi.metatavu.noheva.persistence.dao.DeviceDAO
import fi.metatavu.noheva.persistence.model.Device
import fi.metatavu.noheva.persistence.model.DeviceModel
import java.security.PublicKey
import java.time.OffsetDateTime
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
     * @param deviceModel device model
     * @param userId user id
     * @return updated device
     */
    fun updateDevice(
        existingDevice: Device,
        newDevice: fi.metatavu.noheva.api.spec.model.Device,
        deviceModel: DeviceModel?,
        userId: UUID
    ): Device {
        var result = deviceDAO.updateName(existingDevice, newDevice.name)
        result = deviceDAO.updateDescription(result, newDevice.description)
        result = deviceDAO.updateStatus(result, newDevice.status)
        result = deviceDAO.updateApprovalStatus(result, newDevice.approvalStatus)
        result = deviceDAO.updateDeviceModel(result, deviceModel)
        result = deviceDAO.updateSerialNumber(result, newDevice.serialNumber)
        result = deviceDAO.updateWarrantyExpire(result, newDevice.warrantyExpiry)

        return deviceDAO.updateLastModifierId(result, userId)
    }

    /**
     * Deletes Device
     *
     * @param device device
     */
    fun deleteDevice(device: Device) {
        return deviceDAO.delete(device)
    }

    /**
     * Stores Device key
     *
     * @param device device
     * @param key key
     * @return updated device
     */
    fun storeDeviceKey(device: Device, key: PublicKey): Device {
        val result = deviceDAO.updateDeviceKey(device, key.encoded)

        return deviceDAO.updateApprovalStatus(result, DeviceApprovalStatus.READY)
    }

    /**
     * Re-initiates device approval
     *
     * @param device device
     * @param name name
     * @param description description
     * @param version version
     * @return updated device
     */
    fun reInitiateDeviceApproval(device: Device, name: String?, description: String?, version: String): Device {
        var result = deviceDAO.updateApprovalStatus(device, DeviceApprovalStatus.PENDING)
        result = deviceDAO.updateName(result, name)
        result = deviceDAO.updateDescription(result, description)
        result = deviceDAO.updateVersion(result, version)

        return deviceDAO.updateDeviceKey(result, null)
    }

    /**
     * Gets Device public key by id
     *
     * @param id id
     * @return device public key encoded
     */
    fun getDeviceKey(id: UUID): ByteArray? {
        return deviceDAO.findById(id)?.deviceKey
    }

    /**
     * Handles device status messages
     * When status is [DeviceStatus.OFFLINE], calculates usage hours based on last connection time
     *
     * @param device device
     * @param status status
     * @param version version
     */
    fun handleDeviceStatusMessage(device: Device, status: DeviceStatus, version: String) {
        val updatedDevice = deviceDAO.updateVersion(device, version)
        when (status) {
            DeviceStatus.ONLINE -> handleOnlineStatusMessage(updatedDevice)
            DeviceStatus.OFFLINE -> handleOfflineStatusMessage(updatedDevice)
        }
    }

    /**
     * Handles device status messages when status is [DeviceStatus.OFFLINE]
     *
     * @param device device
     * @return updated device
     */
    private fun handleOfflineStatusMessage(device: Device): Device {
        var updatedDevice = deviceDAO.updateStatus(device, DeviceStatus.OFFLINE)
        val lastConnected = device.lastConnected?.toEpochSecond()

        val now = OffsetDateTime.now().toEpochSecond()
        val currentUsageHours = updatedDevice.usageHours
        if (lastConnected != null) {
            val usageHours = (now - lastConnected.toDouble()) / 3600
             updatedDevice = deviceDAO.updateUsageHours(
                device = updatedDevice,
                usageHours = usageHours + (currentUsageHours ?: 0.0)
            )
        }

        return updatedDevice
    }

    /**
     * Handles device status messages when status is [DeviceStatus.ONLINE]
     *
     * @param device device
     * @return updated device
     */
    private fun handleOnlineStatusMessage(device: Device): Device {
        var updatedDevice = device
        if (updatedDevice.status == DeviceStatus.OFFLINE) {
            updatedDevice = deviceDAO.updateLastConnected(
                device = updatedDevice,
                lastConnected = OffsetDateTime.now())
        }

        return deviceDAO.updateStatus(updatedDevice, DeviceStatus.ONLINE)
    }
}