package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import fi.metatavu.muisti.persistence.model.*
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
class ExhibitionDeviceDAO : AbstractDAO<ExhibitionDevice>() {

    /**
     * Creates new ExhibitionDevice
     *
     * @param id id
     * @param exhibition exhibition
     * @param exhibitionDeviceGroup exhibitionDeviceGroup
     * @param deviceModel deviceModel
     * @param name name
     * @param locationX location x
     * @param locationY location y
     * @param screenOrientation screen orientation
     * @param idlePage idle page
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionDevice
     */
    fun create(id: UUID, exhibition: Exhibition, exhibitionDeviceGroup: ExhibitionDeviceGroup, deviceModel: DeviceModel, name: String, locationX: Double?, locationY: Double?, screenOrientation: ScreenOrientation, idlePage: ExhibitionPage?, creatorId: UUID, lastModifierId: UUID): ExhibitionDevice {
        val exhibitionDevice = ExhibitionDevice()
        exhibitionDevice.id = id
        exhibitionDevice.name = name
        exhibitionDevice.exhibition = exhibition
        exhibitionDevice.exhibitionDeviceGroup = exhibitionDeviceGroup
        exhibitionDevice.deviceModel = deviceModel
        exhibitionDevice.locationX = locationX
        exhibitionDevice.locationY = locationY
        exhibitionDevice.screenOrientation = screenOrientation
        exhibitionDevice.idlePage = idlePage
        exhibitionDevice.creatorId = creatorId
        exhibitionDevice.lastModifierId = lastModifierId
        return persist(exhibitionDevice)
    }

    /**
     * Lists devices
     *
     * @param exhibition exhibition
     * @param exhibitionDeviceGroup filter by exhibition device group. Ignored if null is passed
     * @return List of devices
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
        return query.resultList
    }

    /**
     * Updates exhibition device model
     *
     * @param exhibitionDevice exhibition device to be updated
     * @param deviceModel model
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDevice
     */
    fun updateExhibitionDeviceModel(exhibitionDevice: ExhibitionDevice, deviceModel: DeviceModel, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.deviceModel = deviceModel
        return persist(exhibitionDevice)
    }

    /**
     * Updates name
     *
     * @param exhibitionDevice exhibition device to be updated
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
     * @param exhibitionDevice exhibition device to be updated
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
     * @param exhibitionDevice exhibition device to be updated
     * @param locationY location y
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDevice
     */
    fun updateLocationY(exhibitionDevice: ExhibitionDevice, locationY: Double?, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.locationY = locationY
        return persist(exhibitionDevice)
    }

    /**
     * Updates screen orientation
     *
     * @param exhibitionDevice exhibition device to be updated
     * @param screenOrientation screen orientation
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDevice
     */
    fun updateScreenOrientation(exhibitionDevice: ExhibitionDevice, screenOrientation: ScreenOrientation, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.screenOrientation = screenOrientation
        return persist(exhibitionDevice)
    }

    /**
     * Updates exhibition device group
     *
     * @param exhibitionDevice exhibition device to be updated
     * @param exhibitionDeviceGroup exhibition device group
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDevice
     */
    fun updateExhibitionDeviceGroup(exhibitionDevice: ExhibitionDevice, exhibitionDeviceGroup: ExhibitionDeviceGroup, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.exhibitionDeviceGroup = exhibitionDeviceGroup
        return persist(exhibitionDevice)
    }

    /**
     * Updates idle page
     *
     * @param exhibitionDevice exhibition device to be updated
     * @param idlePage idle page
     * @param lastModifierId last modifier's id
     * @return updated exhibition device
     */
    fun updateIdlePage(exhibitionDevice: ExhibitionDevice, idlePage: ExhibitionPage?, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.idlePage = idlePage
        return persist(exhibitionDevice)
    }

}