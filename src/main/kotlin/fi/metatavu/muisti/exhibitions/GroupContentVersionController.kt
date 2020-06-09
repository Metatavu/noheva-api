package fi.metatavu.muisti.exhibitions

import fi.metatavu.muisti.api.spec.model.GroupContentVersionStatus
import fi.metatavu.muisti.persistence.dao.GroupContentVersionDAO
import fi.metatavu.muisti.persistence.model.ContentVersion
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.persistence.model.GroupContentVersion
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for group content versions
 * @author Jari Nykänen
 */
@ApplicationScoped
class GroupContentVersionController() {

    @Inject
    private lateinit var groupContentVersionDAO: GroupContentVersionDAO

    /**
     * Creates new group content version
     *
     * @param name content version name
     * @param status group content version status
     * @param contentVersion content version
     * @param deviceGroup device group
     * @param creatorId creating user id
     * @return created exhibition content version
     */
    fun createGroupContentVersion(exhibition: Exhibition, name: String, status: GroupContentVersionStatus, contentVersion: ContentVersion, deviceGroup: ExhibitionDeviceGroup, creatorId: UUID): GroupContentVersion {
        return groupContentVersionDAO.create(UUID.randomUUID(), exhibition, name, status, contentVersion, deviceGroup, creatorId, creatorId)
    }

    /**
     * Finds group content version by id
     *
     * @param id group content version id
     * @return found group content version or null if not found
     */
    fun findGroupContentVersionById(id: UUID): GroupContentVersion? {
        return groupContentVersionDAO.findById(id)
    }

    /**
     * Lists group content versions in an exhibitions
     *
     * @returns all groupContentVersions in an exhibition
     */
    fun listGroupContentVersions(exhibition: Exhibition): List<GroupContentVersion> {
        return groupContentVersionDAO.listByExhibition(exhibition)
    }

    /**
     * Updates group content version
     *
     * @param groupContentVersion exhibition content version to be updated
     * @param name group name
     * @param status group content version status
     * @param contentVersion content version
     * @param deviceGroup device group
     * @param modifierId modifying user id
     * @return updated GroupContentVersion
     */
    fun updateGroupContentVersion(groupContentVersion: GroupContentVersion, name: String, status: GroupContentVersionStatus, contentVersion: ContentVersion, deviceGroup: ExhibitionDeviceGroup, modifierId: UUID): GroupContentVersion {
        var result = groupContentVersionDAO.updateName(groupContentVersion, name, modifierId)
        result = groupContentVersionDAO.updateStatus(result, status, modifierId)
        result = groupContentVersionDAO.updateContentVersion(result, contentVersion, modifierId)
        result = groupContentVersionDAO.updateDeviceGroup(result, deviceGroup, modifierId)
        return result
    }

    /**
     * Deletes a group content version
     *
     * @param groupContentVersion group content version to be deleted
     */
    fun deleteGroupContentVersion(groupContentVersion: GroupContentVersion) {
        return groupContentVersionDAO.delete(groupContentVersion)
    }

}