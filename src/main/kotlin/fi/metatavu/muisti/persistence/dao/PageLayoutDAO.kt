package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import fi.metatavu.muisti.persistence.model.*
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for page layout
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class PageLayoutDAO() : AbstractDAO<PageLayout>() {

    /**
     * Creates new PageLayout
     *
     * @param id id
     * @param name name
     * @param data data
     * @param thumbnailUrl thumbnail URL
     * @param deviceModel device model
     * @param screenOrientation screen orientation
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created pageLayout
     */
    fun create(id: UUID, name: String, data: String, thumbnailUrl: String?, deviceModel: DeviceModel?, screenOrientation: ScreenOrientation, creatorId: UUID, lastModifierId: UUID): PageLayout {
        val pageLayout = PageLayout()
        pageLayout.id = id
        pageLayout.name = name
        pageLayout.data = data
        pageLayout.thumbnailUrl = thumbnailUrl
        pageLayout.deviceModel = deviceModel
        pageLayout.screenOrientation = screenOrientation
        pageLayout.creatorId = creatorId
        pageLayout.lastModifierId = lastModifierId
        return persist(pageLayout)
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated pageLayout
     */
    fun updateName(pageLayout: PageLayout, name: String, lastModifierId: UUID): PageLayout {
        pageLayout.lastModifierId = lastModifierId
        pageLayout.name = name
        return persist(pageLayout)
    }

    /**
     * Updates data
     *
     * @param data data
     * @param lastModifierId last modifier's id
     * @return updated pageLayout
     */
    fun updateData(pageLayout: PageLayout, data: String, lastModifierId: UUID): PageLayout {
        pageLayout.lastModifierId = lastModifierId
        pageLayout.data = data
        return persist(pageLayout)
    }

    /**
     * Updates thumbnailUrl
     *
     * @param thumbnailUrl thumbnail URL
     * @param lastModifierId last modifier's id
     * @return updated pageLayout
     */
    fun updateThumbnailUrl(pageLayout: PageLayout, thumbnailUrl: String?, lastModifierId: UUID): PageLayout {
        pageLayout.lastModifierId = lastModifierId
        pageLayout.thumbnailUrl = thumbnailUrl
        return persist(pageLayout)
    }

    /**
     * Updates device model
     *
     * @param deviceModel device model
     * @param lastModifierId last modifier's id
     * @return updated pageLayout
     */
    fun updateDeviceModel(pageLayout: PageLayout, deviceModel: DeviceModel, lastModifierId: UUID): PageLayout {
        pageLayout.lastModifierId = lastModifierId
        pageLayout.deviceModel = deviceModel
        return persist(pageLayout)
    }

    /**
     * Updates screen orientation
     *
     * @param screenOrientation screen orientation
     * @param lastModifierId last modifier's id
     * @return updated pageLayout
     */
    fun updateScreenOrientation(pageLayout: PageLayout, screenOrientation: ScreenOrientation, lastModifierId: UUID): PageLayout {
        pageLayout.lastModifierId = lastModifierId
        pageLayout.screenOrientation = screenOrientation
        return persist(pageLayout)
    }

    /**
     * List Page layouts by device model and screen orientation
     *
     * @param deviceModel device model
     * @param screenOrientation screen orientation
     * @return list of page layouts
     */
    fun list(deviceModel: DeviceModel?, screenOrientation: ScreenOrientation?): List<PageLayout> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<PageLayout> = criteriaBuilder.createQuery(PageLayout::class.java)
        val root: Root<PageLayout> = criteria.from(PageLayout::class.java)

        val restrictions = ArrayList<Predicate>()

        if (deviceModel != null) {
            restrictions.add(criteriaBuilder.equal(root.get(PageLayout_.deviceModel), deviceModel))
        }

        if (screenOrientation != null) {
            restrictions.add(criteriaBuilder.equal(root.get(PageLayout_.screenOrientation), screenOrientation))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
        val query: TypedQuery<PageLayout> = entityManager.createQuery<PageLayout>(criteria)
        return query.resultList
    }

}