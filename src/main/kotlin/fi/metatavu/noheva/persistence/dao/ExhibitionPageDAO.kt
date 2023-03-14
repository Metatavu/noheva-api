package fi.metatavu.noheva.persistence.dao

import fi.metatavu.noheva.persistence.model.*
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionPage
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExhibitionPageDAO : AbstractDAO<ExhibitionPage>() {

    /**
     * Creates new ExhibitionPage
     *
     * @param id id
     * @param exhibition exhibition
     * @param contentVersion content version
     * @param layout layout
     * @param name name
     * @param orderNumber order number
     * @param resources resources
     * @param eventTriggers event triggers
     * @param enterTransitions page enter transitions
     * @param exitTransitions page exit transitions
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionPage
     */
    fun create(id: UUID, exhibition: Exhibition, contentVersion: ContentVersion, device : ExhibitionDevice, layout: PageLayout, name: String, orderNumber: Int, resources: String, eventTriggers: String, enterTransitions: String?, exitTransitions: String?, creatorId: UUID, lastModifierId: UUID): ExhibitionPage {
        val exhibitionPage = ExhibitionPage()
        exhibitionPage.id = id
        exhibitionPage.device = device
        exhibitionPage.layout = layout
        exhibitionPage.contentVersion = contentVersion
        exhibitionPage.name = name
        exhibitionPage.orderNumber = orderNumber
        exhibitionPage.eventTriggers = eventTriggers
        exhibitionPage.resources = resources
        exhibitionPage.enterTransitions = enterTransitions
        exhibitionPage.exitTransitions = exitTransitions
        exhibitionPage.exhibition = exhibition
        exhibitionPage.creatorId = creatorId
        exhibitionPage.lastModifierId = lastModifierId
        return persist(exhibitionPage)
    }

    /**
     * Lists exhibition pages
     *
     * @param exhibition exhibition
     * @param exhibitionDevice filter by exhibition device. Ignored if null is passed
     * @param contentVersion filter by content version. Ignored if null is passed
     * @param pageLayout filter by page layout. Ignored if null is passed
     * @return List of exhibition pages
     */
    fun list(exhibition: Exhibition, exhibitionDevice : ExhibitionDevice?, contentVersion: ContentVersion?, pageLayout: PageLayout?): List<ExhibitionPage> {
        
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionPage> = criteriaBuilder.createQuery(ExhibitionPage::class.java)
        val root: Root<ExhibitionPage> = criteria.from(ExhibitionPage::class.java)

        val restrictions = ArrayList<Predicate>()
        restrictions.add(criteriaBuilder.equal(root.get(ExhibitionPage_.exhibition), exhibition))

        if (exhibitionDevice != null) {
            restrictions.add(criteriaBuilder.equal(root.get(ExhibitionPage_.device), exhibitionDevice))
        }

        if (contentVersion != null) {
            restrictions.add(criteriaBuilder.equal(root.get(ExhibitionPage_.contentVersion), contentVersion))
        }

        if (pageLayout != null) {
            restrictions.add(criteriaBuilder.equal(root.get(ExhibitionPage_.layout), pageLayout))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())
        val query: TypedQuery<ExhibitionPage> = getEntityManager().createQuery<ExhibitionPage>(criteria)
        return query.resultList
    }

    /**
     * Lists ExhibitionPages by layout
     *
     * @param layout layout
     * @return List of ExhibitionPages
     */
    fun listByLayout(layout: PageLayout): List<ExhibitionPage> {
        
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionPage> = criteriaBuilder.createQuery(ExhibitionPage::class.java)
        val root: Root<ExhibitionPage> = criteria.from(ExhibitionPage::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionPage_.layout), layout))
        val query: TypedQuery<ExhibitionPage> = getEntityManager().createQuery<ExhibitionPage>(criteria)
        return query.resultList
    }

    /**
     * Lists pages by device group
     *
     * @param deviceGroup device group
     * @return List of pages in device group
     */
    fun listByDeviceGroup(deviceGroup: ExhibitionDeviceGroup): List<ExhibitionPage> {
        
        val criteriaBuilder = getEntityManager().criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionPage> = criteriaBuilder.createQuery(ExhibitionPage::class.java)
        val root: Root<ExhibitionPage> = criteria.from(ExhibitionPage::class.java)
        val deviceJoin = root.join(ExhibitionPage_.device)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(deviceJoin.get(ExhibitionDevice_.exhibitionDeviceGroup), deviceGroup))
        val query: TypedQuery<ExhibitionPage> = getEntityManager().createQuery<ExhibitionPage>(criteria)
        return query.resultList
    }

    /**
     * Updates layout
     *
     * @param exhibitionPage exhibition page
     * @param layout layout
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateLayout(exhibitionPage: ExhibitionPage, layout: PageLayout, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.layout = layout
        return persist(exhibitionPage)
    }

    /**
     * Updates device
     *
     * @param exhibitionPage exhibition page
     * @param device device
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateDevice(exhibitionPage: ExhibitionPage, device: ExhibitionDevice, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.device = device
        return persist(exhibitionPage)
    }

    /**
     * Updates content version
     *
     * @param exhibitionPage exhibition page
     * @param contentVersion content version
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateContentVersion(exhibitionPage: ExhibitionPage, contentVersion: ContentVersion, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.contentVersion = contentVersion
        return persist(exhibitionPage)
    }

    /**
     * Updates name
     *
     * @param exhibitionPage exhibition page
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibition page
     */
    fun updateName(exhibitionPage: ExhibitionPage, name: String, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.name = name
        return persist(exhibitionPage)
    }

    /**
     * Updates order number
     *
     * @param exhibitionPage exhibition page
     * @param orderNumber order number
     * @param lastModifierId last modifier's id
     * @return updated exhibition page
     */
    fun updateOrderNumber(exhibitionPage: ExhibitionPage, orderNumber: Int, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.orderNumber = orderNumber
        return persist(exhibitionPage)
    }

    /**
     * Updates resources
     *
     * @param exhibitionPage exhibition page
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
     * Updates event triggers
     *
     * @param exhibitionPage exhibition page
     * @param eventTriggers event triggers
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateEventTriggers(exhibitionPage: ExhibitionPage, eventTriggers: String, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.eventTriggers = eventTriggers
        return persist(exhibitionPage)
    }

    /**
     * Update page enter transitions
     *
     * @param exhibitionPage exhibition page
     * @param enterTransitions page enter transitions
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateEnterTransitions(exhibitionPage: ExhibitionPage, enterTransitions: String?, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.enterTransitions = enterTransitions
        return persist(exhibitionPage)
    }

    /**
     * Update page exit transitions
     *
     * @param exhibitionPage exhibition page
     * @param exitTransitions page exit transitions
     * @param lastModifierId last modifier's id
     * @return updated exhibitionPage
     */
    fun updateExitTransitions(exhibitionPage: ExhibitionPage, exitTransitions: String?, lastModifierId: UUID): ExhibitionPage {
        exhibitionPage.lastModifierId = lastModifierId
        exhibitionPage.exitTransitions = exitTransitions
        return persist(exhibitionPage)
    }

}