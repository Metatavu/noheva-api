package fi.metatavu.muisti.persistence.dao

import fi.metatavu.muisti.api.spec.model.GroupContentVersionStatus
import fi.metatavu.muisti.persistence.model.*
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * DAO class for GroupContentVersion
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class GroupContentVersionDAO() : AbstractDAO<GroupContentVersion>() {

    /**
     * Creates new GroupContentVersion
     *
     * @param id id
     * @param exhibition exhibition
     * @param name name
     * @param status group content version status
     * @param contentVersion content version
     * @param deviceGroup device group
     * @param creatorId creator's id
     * @param lastModifierId last modifier's id
     * @return created groupContentVersion
     */
    fun create(id: UUID, exhibition: Exhibition, name: String, status: GroupContentVersionStatus, contentVersion: ContentVersion, deviceGroup: ExhibitionDeviceGroup, creatorId: UUID, lastModifierId: UUID): GroupContentVersion {
        val groupContentVersion = GroupContentVersion()
        groupContentVersion.id = id
        groupContentVersion.name = name
        groupContentVersion.status = status
        groupContentVersion.contentVersion = contentVersion
        groupContentVersion.deviceGroup = deviceGroup
        groupContentVersion.exhibition = exhibition
        groupContentVersion.creatorId = creatorId
        groupContentVersion.lastModifierId = lastModifierId
        return persist(groupContentVersion)
    }

    /**
     * Lists group content versions
     *
     * @param exhibition filter by exhibition
     * @param contentVersion filter by content version. Ignored if null is passed
     * @return List of group content versions
     */
    fun list(exhibition: Exhibition, contentVersion: ContentVersion?): List<GroupContentVersion> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<GroupContentVersion> = criteriaBuilder.createQuery(GroupContentVersion::class.java)
        val root: Root<GroupContentVersion> = criteria.from(GroupContentVersion::class.java)

        val restrictions = ArrayList<Predicate>()
        restrictions.add(criteriaBuilder.equal(root.get(GroupContentVersion_.exhibition), exhibition))

        if (contentVersion != null) {
            restrictions.add(criteriaBuilder.equal(root.get(GroupContentVersion_.contentVersion), contentVersion))
        }

        criteria.select(root)
        criteria.where(*restrictions.toTypedArray())

        val query: TypedQuery<GroupContentVersion> = entityManager.createQuery<GroupContentVersion>(criteria)
        return query.resultList
    }

    /**
     * Updates name
     *
     * @param groupContentVersion exhibition content version
     * @param name name
     * @param lastModifierId last modifier's id
     * @return updated groupContentVersion
     */
    fun updateName(groupContentVersion: GroupContentVersion, name: String, lastModifierId: UUID): GroupContentVersion {
        groupContentVersion.lastModifierId = lastModifierId
        groupContentVersion.name = name
        return persist(groupContentVersion)
    }

    /**
     * Updates status
     *
     * @param groupContentVersion group content version
     * @param status status
     * @param lastModifierId last modifier's id
     * @return updated groupContentVersion
     */
    fun updateStatus(groupContentVersion: GroupContentVersion, status: GroupContentVersionStatus, lastModifierId: UUID): GroupContentVersion {
        groupContentVersion.lastModifierId = lastModifierId
        groupContentVersion.status = status
        return persist(groupContentVersion)
    }

    /**
     * Updates content version
     *
     * @param groupContentVersion group content version
     * @param contentVersion content version
     * @param lastModifierId last modifier's id
     * @return updated groupContentVersion
     */
    fun updateContentVersion(groupContentVersion: GroupContentVersion, contentVersion: ContentVersion, lastModifierId: UUID): GroupContentVersion {
        groupContentVersion.lastModifierId = lastModifierId
        groupContentVersion.contentVersion = contentVersion
        return persist(groupContentVersion)
    }

    /**
     * Updates device group
     *
     * @param groupContentVersion group content version
     * @param deviceGroup device group
     * @param lastModifierId last modifier's id
     * @return updated groupContentVersion
     */
    fun updateDeviceGroup(groupContentVersion: GroupContentVersion, deviceGroup: ExhibitionDeviceGroup, lastModifierId: UUID): GroupContentVersion {
        groupContentVersion.lastModifierId = lastModifierId
        groupContentVersion.deviceGroup = deviceGroup
        return persist(groupContentVersion)
    }

}
