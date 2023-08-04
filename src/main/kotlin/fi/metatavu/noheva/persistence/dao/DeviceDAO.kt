package fi.metatavu.noheva.persistence.dao

import fi.metatavu.noheva.api.spec.model.DeviceApprovalStatus
import fi.metatavu.noheva.api.spec.model.DeviceStatus
import fi.metatavu.noheva.persistence.model.Device
import fi.metatavu.noheva.persistence.model.Device_
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
        device.status = DeviceStatus.ONLINE
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
     * Updates Device
     *
     * @param device device
     * @return updated device
     */
    fun update(device: Device): Device {
        return persist(device)
    }
}