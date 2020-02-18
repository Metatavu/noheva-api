package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for ExhibitionDeviceGroup
 *
 * @author Antti Leppä
 */
@ApplicationScoped
open class ExhibitionDeviceGroupDAO() : AbstractDAO<ExhibitionDeviceGroup>() {

    /**
     * Creates new ExhibitionDeviceGroup
     *
     * @param id id
     * @param exhibition exhibition
     * @param name name
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created exhibitionDeviceGroup
     */
    open fun create(id: UUID, exhibition: Exhibition, name: String, creatorId: UUID, lastModifierId: UUID): ExhibitionDeviceGroup {
        val exhibitionDeviceGroup = ExhibitionDeviceGroup()
        exhibitionDeviceGroup.id = id
        exhibitionDeviceGroup.name = name
        exhibitionDeviceGroup.exhibition = exhibition
        exhibitionDeviceGroup.creatorId = creatorId
        exhibitionDeviceGroup.lastModifierId = lastModifierId
        return persist(exhibitionDeviceGroup)
    }

    /**
     * Lists ExhibitionDeviceGroups by exhibition
     *
     * @param exhibition exhibition
     * @return List of ExhibitionDeviceGroups
     */
    open fun listByExhibition(exhibition: Exhibition): List<ExhibitionDeviceGroup> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<ExhibitionDeviceGroup> = criteriaBuilder.createQuery(ExhibitionDeviceGroup::class.java)
        val root: Root<ExhibitionDeviceGroup> = criteria.from(ExhibitionDeviceGroup::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ExhibitionDeviceGroup_.exhibition), exhibition))
        val query: TypedQuery<ExhibitionDeviceGroup> = entityManager.createQuery<ExhibitionDeviceGroup>(criteria)
        return query.getResultList()
    }

    /**
     * Updates name
     *
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated exhibitionDeviceGroup
     */
    open fun updateName(exhibitionDeviceGroup: ExhibitionDeviceGroup, name: String, lastModifierId: UUID): ExhibitionDeviceGroup {
        exhibitionDeviceGroup.lastModifierId = lastModifierId
        exhibitionDeviceGroup.name = name
        return persist(exhibitionDeviceGroup)
    }

}