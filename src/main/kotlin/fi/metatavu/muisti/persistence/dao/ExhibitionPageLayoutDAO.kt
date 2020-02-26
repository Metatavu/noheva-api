package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionPageLayout
import fi.metatavu.muisti.persistence.model.ExhibitionPageLayout_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionPageLayout
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionPageLayoutDAO() : AbstractDAO<ExhibitionPageLayout>() {

    /**
     * Creates new ExhibitionPageLayout
     *
     * @param id id
     * @param exhibition exhibition
     * @param name name
     * @param data data
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionPageLayout
     */
    fun create(id: UUID, exhibition: Exhibition, name: String, data: String, creatorId: UUID, lastModifierId: UUID): ExhibitionPageLayout {
        val exhibitionPageLayout = ExhibitionPageLayout()
        exhibitionPageLayout.id = id
        exhibitionPageLayout.name = name
        exhibitionPageLayout.data = data
        exhibitionPageLayout.exhibition = exhibition
        exhibitionPageLayout.creatorId = creatorId
        exhibitionPageLayout.lastModifierId = lastModifierId
        return persist(exhibitionPageLayout)
    }

    /**
     * Lists ExhibitionPageLayouts by exhibition
     *
     * @param exhibition exhibition
     * @return List of ExhibitionPageLayouts
     */
    fun listByExhibition(exhibition: Exhibition): List<ExhibitionPageLayout> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionPageLayout> = criteriaBuilder.createQuery(ExhibitionPageLayout::class.java)
        val root: Root<ExhibitionPageLayout> = criteria.from(ExhibitionPageLayout::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionPageLayout_.exhibition), exhibition))
        val query: TypedQuery<ExhibitionPageLayout> = entityManager.createQuery<ExhibitionPageLayout>(criteria)
        return query.getResultList()
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPageLayout
     */
    fun updateName(exhibitionPageLayout: ExhibitionPageLayout, name: String, lastModifierId: UUID): ExhibitionPageLayout {
        exhibitionPageLayout.lastModifierId = lastModifierId
        exhibitionPageLayout.name = name
        return persist(exhibitionPageLayout)
    }

    /**
     * Updates data
     *
     * @param data data
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPageLayout
     */
    fun updateData(exhibitionPageLayout: ExhibitionPageLayout, data: String, lastModifierId: UUID): ExhibitionPageLayout {
        exhibitionPageLayout.lastModifierId = lastModifierId
        exhibitionPageLayout.data = data
        return persist(exhibitionPageLayout)
    }

}