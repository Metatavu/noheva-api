package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.PageLayout
import fi.metatavu.muisti.persistence.model.PageLayout_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for PageLayout
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class PageLayoutDAO() : AbstractDAO<PageLayout>() {

    /**
     * Creates new PageLayout
     *
     * @param id id
     * @param exhibition exhibition
     * @param name name
     * @param data data
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created pageLayout
     */
    fun create(id: UUID, exhibition: Exhibition, name: String, data: String, creatorId: UUID, lastModifierId: UUID): PageLayout {
        val pageLayout = PageLayout()
        pageLayout.id = id
        pageLayout.name = name
        pageLayout.data = data
        pageLayout.exhibition = exhibition
        pageLayout.creatorId = creatorId
        pageLayout.lastModifierId = lastModifierId
        return persist(pageLayout)
    }

    /**
     * Lists PageLayouts by exhibition
     *
     * @param exhibition exhibition
     * @return List of PageLayouts
     */
    fun listByExhibition(exhibition: Exhibition): List<PageLayout> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<PageLayout> = criteriaBuilder.createQuery(PageLayout::class.java)
        val root: Root<PageLayout> = criteria.from(PageLayout::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(PageLayout_.exhibition), exhibition))
        val query: TypedQuery<PageLayout> = entityManager.createQuery<PageLayout>(criteria)
        return query.getResultList()
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

}