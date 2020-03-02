package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionPage
import fi.metatavu.muisti.persistence.model.ExhibitionPageLayout
import fi.metatavu.muisti.persistence.model.ExhibitionPage_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionPage
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionPageDAO() : AbstractDAO<ExhibitionPage>() {

    /**
     * Creates new ExhibitionPage
     *
     * @param id id
     * @param exhibition exhibition
     * @param layout layout
     * @param name name
     * @param resources resources
     * @param events events
     * @param eventTriggers event triggers
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionPage
     */
    fun create(id: UUID, exhibition: Exhibition, layout: ExhibitionPageLayout, name: String, resources: String, events: String, eventTriggers: String, creatorId: UUID, lastModifierId: UUID): ExhibitionPage {
        val exhibitionPage = ExhibitionPage()
        exhibitionPage.id = id
        exhibitionPage.layout = layout
        exhibitionPage.name = name
        exhibitionPage.eventTriggers = eventTriggers
        exhibitionPage.resources = resources
        exhibitionPage.events = events
        exhibitionPage.exhibition = exhibition
        exhibitionPage.creatorId = creatorId
        exhibitionPage.lastModifierId = lastModifierId
        return persist(exhibitionPage)
    }

    /**
     * Lists ExhibitionPages by exhibition
     *
     * @param exhibition exhibition
     * @return List of ExhibitionPages
     */
    fun listByExhibition(exhibition: Exhibition): List<ExhibitionPage> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionPage> = criteriaBuilder.createQuery(ExhibitionPage::class.java)
        val root: Root<ExhibitionPage> = criteria.from(ExhibitionPage::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionPage_.exhibition), exhibition))
        val query: TypedQuery<ExhibitionPage> = entityManager.createQuery<ExhibitionPage>(criteria)
        return query.getResultList()
    }

    /**
     * Updates layout
     *
     * @param layout layout
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateLayout(exhibitionPage: ExhibitionPage, layout: ExhibitionPageLayout, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.layout = layout
        return persist(exhibitionPage)
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateName(exhibitionPage: ExhibitionPage, name: String, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.name = name
        return persist(exhibitionPage)
    }

    /**
     * Updates resources
     *
     * @param resources resources
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateResources(exhibitionPage: ExhibitionPage, resources: String, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.resources = resources
        return persist(exhibitionPage)
    }

    /**
     * Updates events
     *
     * @param events events
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateEvents(exhibitionPage: ExhibitionPage, events: String, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.events = events
        return persist(exhibitionPage)
    }

    /**
     * Updates event triggers
     *
     * @param eventTriggers event triggers
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateEventTriggers(exhibitionPage: ExhibitionPage, eventTriggers: String, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.eventTriggers = eventTriggers
        return persist(exhibitionPage)
    }

}