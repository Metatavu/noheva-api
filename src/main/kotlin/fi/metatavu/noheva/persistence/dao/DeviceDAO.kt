package fi.metatavu.noheva.persistence.dao

import fi.metatavu.noheva.api.spec.model.DeviceApprovalStatus
import fi.metatavu.noheva.api.spec.model.DeviceStatus
import fi.metatavu.noheva.persistence.model.Device
import fi.metatavu.noheva.persistence.model.DeviceModel
import fi.metatavu.noheva.persistence.model.Device_
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate

/**
 * DAO class for Device
 */
@ApplicationScoped
class DeviceDAO: AbstractDAO<Device>() {

    /**
     * Creates new Device
     *
     * @param id id
     * @param serialNumber serial number
     * @param name name
     * @param description description
     * @param version version
     * @return created device
     */
    fun create(
        id: UUID,
        serialNumber: String,
        name: String?,
        description: String?,
        version: String
    ): Device {
        val device = Device()
        device.id = id
        device.serialNumber = serialNumber
        device.name = name
        device.description = description
        device.status = DeviceStatus.OFFLINE
        device.approvalStatus = DeviceApprovalStatus.PENDING
        device.version = version

        return persist(device)
    }

    /**
     * Finds Device by serial number
     *
     * @param serialNumber serial number
     * @return found device or null if not found
     */
    fun findBySerialNumber(serialNumber: String): Device? {
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<Device> = criteriaBuilder.createQuery(Device::class.java)
        val root = criteria.from(Device::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(Device_.serialNumber), serialNumber))

        return getSingleResult(getEntityManager().createQuery(criteria))
    }

    /**
     * Lists Devices with filters
     *
     * @param status status
     * @param approvalStatus approval status
     * @return found devices
     */
    fun list(status: DeviceStatus?, approvalStatus: DeviceApprovalStatus?): List<Device> {
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<Device> = criteriaBuilder.createQuery(Device::class.java)
        val root = criteria.from(Device::class.java)
        val restrictions = ArrayList<Predicate>()

        if (status != null) {
            restrictions.add(criteriaBuilder.equal(root.get(Device_.status), status))
        }

        if (approvalStatus != null) {
            restrictions.add(criteriaBuilder.equal(root.get(Device_.approvalStatus), approvalStatus))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())

        return getEntityManager().createQuery(criteria).resultList
    }

    /**
     * Updates Devices name
     *
     * @param device device
     * @param name name
     * @return updated device
     */
    fun updateName(device: Device, name: String?): Device {
        device.name = name

        return persist(device)
    }

    /**
     * Updates Devices description
     *
     * @param device device
     * @param description description
     * @return updated device
     */
    fun updateDescription(device: Device, description: String?): Device {
        device.description = description

        return persist(device)
    }

    /**
     * Updates devices status
     *
     * @param device device
     * @param status status
     * @return updated device
     */
    fun updateStatus(device: Device, status: DeviceStatus): Device {
        device.status = status

        return persist(device)
    }

    /**
     * Updates devices approval status
     *
     * @param device device
     * @param approvalStatus approval status
     * @return updated device
     */
    fun updateApprovalStatus(device: Device, approvalStatus: DeviceApprovalStatus): Device {
        device.approvalStatus = approvalStatus

        return persist(device)
    }

    /**
     * Updates devices version
     *
     * @param device device
     * @param version version
     * @return updated device
     */
    fun updateVersion(device: Device, version: String): Device {
        device.version = version

        return persist(device)
    }

    /**
     * Updates devices serial number
     *
     * @param device device
     * @param serialNumber serial number
     * @return updated device
     */
    fun updateSerialNumber(device: Device, serialNumber: String): Device {
        device.serialNumber = serialNumber

        return persist(device)
    }

    /**
     * Updates devices device model
     *
     * @param device device
     * @param deviceModel device model
     * @return updated device
     */
    fun updateDeviceModel(device: Device, deviceModel: DeviceModel?): Device {
        device.deviceModel = deviceModel

        return persist(device)
    }

    /**
     * Updates devices last modifier id
     *
     * @param device device
     * @param lastModifierId last modifier id
     * @return updated device
     */
    fun updateLastModifierId(device: Device, lastModifierId: UUID?): Device {
        device.lastModifierId = lastModifierId

        return persist(device)
    }

    /**
     * Updates devices device key
     *
     * @param device device
     * @param deviceKey device key
     * @return updated device
     */
    fun updateDeviceKey(device: Device, deviceKey: ByteArray?): Device {
        device.deviceKey = deviceKey

        return persist(device)
    }

    /**
     * Updates devices last connected
     *
     * @param device device
     * @param lastConnected last connected
     * @return updated device
     */
    fun updateLastConnected(device: Device, lastConnected: OffsetDateTime): Device {
        device.lastConnected = lastConnected

        return persist(device)
    }

    /**
     * Updates devices usage hours
     *
     * @param device device
     * @param usageHours usage hours
     * @return updated device
     */
    fun updateUsageHours(device: Device, usageHours: Double): Device {
        device.usageHours = usageHours

        return persist(device)
    }

    /**
     * Updates devices warranty expire
     *
     * @param device device
     * @param warrantyExpire warranty expire
     * @return updated device
     */
    fun updateWarrantyExpire(device: Device, warrantyExpire: OffsetDateTime?): Device {
        device.warrantyExpiry = warrantyExpire

        return persist(device)
    }
}