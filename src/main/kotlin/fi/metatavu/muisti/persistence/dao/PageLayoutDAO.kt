package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import fi.metatavu.muisti.persistence.model.*
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
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
     * @param modelId device model id
     * @param screenOrientation screen orientation
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created pageLayout
     */
    fun create(id: UUID, name: String, data: String, thumbnailUrl: String?, modelId: UUID, screenOrientation: ScreenOrientation, creatorId: UUID, lastModifierId: UUID): PageLayout {
        val pageLayout = PageLayout()
        pageLayout.id = id
        pageLayout.name = name
        pageLayout.data = data
        pageLayout.thumbnailUrl = thumbnailUrl
        pageLayout.modelId = modelId
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
     * Updates model id
     *
     * @param modelId model id
     * @param lastModifierId last modifier's id
     * @return updated pageLayout
     */
    fun updateModelId(pageLayout: PageLayout, modelId: UUID, lastModifierId: UUID): PageLayout {
        pageLayout.lastModifierId = lastModifierId
        pageLayout.modelId = modelId
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
     * List Page layouts by device model ID and screen orientation
     *
     * @param deviceModelId device model id
     * @param screenOrientation screen orientation
     * @return list of page layouts
     */
    fun listByDeviceModelIdAndOrientation(deviceModelId: UUID, screenOrientation: ScreenOrientation): List<PageLayout> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<PageLayout> = criteriaBuilder.createQuery(PageLayout::class.java)
        val root: Root<PageLayout> = criteria.from(PageLayout::class.java)
        criteria.select(root)
        criteria.where(
                criteriaBuilder.equal(root.get<String>(PageLayout_.MODEL_ID), deviceModelId),
                criteriaBuilder.equal(root.get<String>(PageLayout_.SCREEN_ORIENTATION), screenOrientation)
        )
        val query: TypedQuery<PageLayout> = entityManager.createQuery<PageLayout>(criteria)
        return query.resultList
    }

    /**
     * List page layouts by device model id
     *
     * @param deviceModelId device model id
     * @return list of page layouts
     */
    fun listByDeviceModelId(deviceModelId: UUID): List<PageLayout> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<PageLayout> = criteriaBuilder.createQuery(PageLayout::class.java)
        val root: Root<PageLayout> = criteria.from(PageLayout::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get<String>(PageLayout_.MODEL_ID), deviceModelId))
        val query: TypedQuery<PageLayout> = entityManager.createQuery<PageLayout>(criteria)
        return query.resultList
    }

    /**
     * List page layouts by screen orientation
     *
     * @param screenOrientation device orientation
     * @return list of page layouts
     */
    fun listByScreenOrientation(screenOrientation: ScreenOrientation): List<PageLayout> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<PageLayout> = criteriaBuilder.createQuery(PageLayout::class.java)
        val root: Root<PageLayout> = criteria.from(PageLayout::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get<String>(PageLayout_.SCREEN_ORIENTATION), screenOrientation))
        val query: TypedQuery<PageLayout> = entityManager.createQuery<PageLayout>(criteria)
        return query.resultList
    }

}