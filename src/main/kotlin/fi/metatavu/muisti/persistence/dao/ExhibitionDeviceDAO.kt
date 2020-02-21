package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDevice
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.persistence.model.ExhibitionDevice_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import kotlin.collections.ArrayList

/**
 * DAO class for ExhibitionDevice
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionDeviceDAO() : AbstractDAO<ExhibitionDevice>() {

    /**
     * Creates new ExhibitionDevice
     *
     * @param id id
     * @param exhibition exhibition
     * @param exhibitionDeviceGroup exhibitionDeviceGroup
     * @param name name
     * @param locationX location x
     * @param locationY location y
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionDevice
     */
    fun create(id: UUID, exhibition: Exhibition, exhibitionDeviceGroup: ExhibitionDeviceGroup, name: String, locationX: Double?, locationY: Double?, creatorId: UUID, lastModifierId: UUID): ExhibitionDevice {
        val exhibitionDevice = ExhibitionDevice()
        exhibitionDevice.id = id
        exhibitionDevice.name = name
        exhibitionDevice.exhibition = exhibition
        exhibitionDevice.exhibitionDeviceGroup = exhibitionDeviceGroup
        exhibitionDevice.locationX = locationX
        exhibitionDevice.locationY = locationY
        exhibitionDevice.creatorId = creatorId
        exhibitionDevice.lastModifierId = lastModifierId
        return persist(exhibitionDevice)
    }

    /**
     * Lists ExhibitionDevices by exhibition
     *
     * @param exhibition exhibition
     * @param exhibitionDeviceGroup filter by exhibition device group. Ignored if null is passed
     * @return List of ExhibitionDevices
     */
    fun list(exhibition: Exhibition, exhibitionDeviceGroup: ExhibitionDeviceGroup?): List<ExhibitionDevice> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionDevice> = criteriaBuilder.createQuery(ExhibitionDevice::class.java)
        val root: Root<ExhibitionDevice> = criteria.from(ExhibitionDevice::class.java)

        val restrictions = ArrayList<Predicate>()
        restrictions.add(criteriaBuilder.equal(root.get(ExhibitionDevice_.exhibition), exhibition))

        if (exhibitionDeviceGroup != null) {
            restrictions.add(criteriaBuilder.equal(root.get(ExhibitionDevice_.exhibitionDeviceGroup), exhibitionDeviceGroup))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())

        val query: TypedQuery<ExhibitionDevice> = entityManager.createQuery<ExhibitionDevice>(criteria)
        return query.getResultList()
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDevice
     */
    fun updateName(exhibitionDevice: ExhibitionDevice, name: String, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.name = name
        return persist(exhibitionDevice)
    }

    /**
     * Updates location x
     *
     * @param locationX location X
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDevice
     */
    fun updateLocationX(exhibitionDevice: ExhibitionDevice, locationX: Double?, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.locationX = locationX
        return persist(exhibitionDevice)
    }

    /**
     * Updates location y
     *
     * @param locationY location y
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDevice
     */
    fun updateLocationY(exhibitionDevice: ExhibitionDevice, locationY: Double?, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.locationY = locationY
        return persist(exhibitionDevice)
    }

}