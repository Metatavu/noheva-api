package fi.metatavu.noheva.persistence.dao

import fi.metatavu.noheva.api.spec.model.DeviceImageLoadStrategy
import fi.metatavu.noheva.api.spec.model.ScreenOrientation
import fi.metatavu.noheva.persistence.model.*
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
     * @param device device
     * @param name name
     * @param locationX location x
     * @param locationY location y
     * @param screenOrientation screen orientation
     * @param imageLoadStrategy image load strategy
     * @param idlePage idle page
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionDevice
     */
    fun create(
        id: UUID,
        exhibition: Exhibition,
        exhibitionDeviceGroup: ExhibitionDeviceGroup,
        device: Device?,
        name: String,
        locationX: Double?,
        locationY: Double?,
        screenOrientation: ScreenOrientation,
        imageLoadStrategy: DeviceImageLoadStrategy,
        idlePage: ExhibitionPage?,
        creatorId: UUID,
        lastModifierId: UUID
    ): ExhibitionDevice {
        val exhibitionDevice = ExhibitionDevice()
        exhibitionDevice.id = id
        exhibitionDevice.name = name
        exhibitionDevice.exhibition = exhibition
        exhibitionDevice.exhibitionDeviceGroup = exhibitionDeviceGroup
        exhibitionDevice.device = device
        exhibitionDevice.locationX = locationX
        exhibitionDevice.locationY = locationY
        exhibitionDevice.screenOrientation = screenOrientation
        exhibitionDevice.imageLoadStrategy = imageLoadStrategy
        exhibitionDevice.idlePage = idlePage
        exhibitionDevice.creatorId = creatorId
        exhibitionDevice.lastModifierId = lastModifierId
        return persist(exhibitionDevice)
    }

    /**
     * Lists exhibition devices
     *
     * @param exhibition exhibition
     * @param exhibitionDeviceGroup filter by exhibition device group. Ignored if null is passed
     * @param deviceModel filter by device model. Ignored if null is passed
     * @return List of exhibition devices
     */
    fun list(exhibition: Exhibition, exhibitionDeviceGroup: ExhibitionDeviceGroup?, deviceModel: DeviceModel?): List<ExhibitionDevice> {
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionDevice> = criteriaBuilder.createQuery(ExhibitionDevice::class.java)
        val root: Root<ExhibitionDevice> = criteria.from(ExhibitionDevice::class.java)

        val restrictions = ArrayList<Predicate>()
        restrictions.add(criteriaBuilder.equal(root.get(ExhibitionDevice_.exhibition), exhibition))

        if (exhibitionDeviceGroup != null) {
            restrictions.add(criteriaBuilder.equal(root.get(ExhibitionDevice_.exhibitionDeviceGroup), exhibitionDeviceGroup))
        }

        if (deviceModel != null) {
            val deviceJoin = root.join(ExhibitionDevice_.device)
            restrictions.add(criteriaBuilder.equal(deviceJoin.get(Device_.deviceModel), deviceModel))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())

        val query: TypedQuery<ExhibitionDevice> = getEntityManager().createQuery<ExhibitionDevice>(criteria)
        return query.resultList
    }

    /**
     * Lists exhibition devices by device
     *
     * @param device device
     * @return list of exhibition devices
     */
    fun listByDevice(device: Device): List<ExhibitionDevice> {
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionDevice> = criteriaBuilder.createQuery(ExhibitionDevice::class.java)
        val root: Root<ExhibitionDevice> = criteria.from(ExhibitionDevice::class.java)

        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionDevice_.device), device))

        val query: TypedQuery<ExhibitionDevice> = getEntityManager().createQuery(criteria)

        return query.resultList
    }

    /**
     * Lists devices by idle page
     *
     * @param idlePage idlePage
     * @return List of exhibition devices
     */
    fun listByIdlePage(idlePage: ExhibitionPage): List<ExhibitionDevice> {
        
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionDevice> = criteriaBuilder.createQuery(ExhibitionDevice::class.java)
        val root: Root<ExhibitionDevice> = criteria.from(ExhibitionDevice::class.java)

        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionDevice_.idlePage), idlePage))

        val query: TypedQuery<ExhibitionDevice> = getEntityManager().createQuery<ExhibitionDevice>(criteria)
        return query.resultList
    }

    /**
     * Updates exhibition devices device
     *
     * @param exhibitionDevice exhibition device to be updated
     * @param device device
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDevice
     */
    fun updateExhibitionDevicesDevice(exhibitionDevice: ExhibitionDevice, device: Device?, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.device = device
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
     * Updates image load strategy
     *
     * @param exhibitionDevice exhibition device to be updated
     * @param imageLoadStrategy image load strategy
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDevice
     */
    fun updateImageLoadStrategy(exhibitionDevice: ExhibitionDevice, imageLoadStrategy: DeviceImageLoadStrategy, lastModifierId: UUID): ExhibitionDevice {
        exhibitionDevice.lastModifierId = lastModifierId
        exhibitionDevice.imageLoadStrategy = imageLoadStrategy
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