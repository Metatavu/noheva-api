package fi.metatavu.muisti.contents

import fi.metatavu.muisti.api.spec.model.GroupContentVersionStatus
import fi.metatavu.muisti.persistence.dao.GroupContentVersionDAO
import fi.metatavu.muisti.persistence.model.ContentVersion
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.persistence.model.GroupContentVersion
import fi.metatavu.muisti.utils.CopyException
import fi.metatavu.muisti.utils.IdMapper
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for group content versions
 * @author Jari Nykänen
 */
@ApplicationScoped
class GroupContentVersionController {

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
     * Creates a copy of a group content version
     *
     * @param sourceGroupContentVersion source group content version
     * @param targetContentVersion target content version for copied group content version
     * @param deviceGroup target device group for copied group content version
     * @param idMapper id mapper
     * @param creatorId id of user that created the copy
     * @return a copy of a group content version
     */
    fun copyGroupContentVersion(
        sourceGroupContentVersion: GroupContentVersion,
        targetContentVersion: ContentVersion,
        deviceGroup: ExhibitionDeviceGroup,
        idMapper: IdMapper,
        creatorId: UUID
    ): GroupContentVersion {
        val id = idMapper.getNewId(sourceGroupContentVersion.id) ?: throw CopyException("Target group content version id not found")

        return groupContentVersionDAO.create(
            id = id,
            exhibition = sourceGroupContentVersion.exhibition ?: throw CopyException("Source group content version exhibition not found"),
            name = sourceGroupContentVersion.name ?: throw CopyException("Source group content version name not found"),
            status = sourceGroupContentVersion.status ?: throw CopyException("Source group content status not found"),
            contentVersion = targetContentVersion,
            deviceGroup = deviceGroup,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
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
     * Lists group content versions
     *
     * @param exhibition filter by exhibition
     * @param contentVersion filter by content version. Ignored if null is passed
     * @param deviceGroup filter by device group. Ignored if null is passed
     * @return List of group content versions
     */
    fun listGroupContentVersions(exhibition: Exhibition, contentVersion: ContentVersion?, deviceGroup: ExhibitionDeviceGroup?): List<GroupContentVersion> {
        return groupContentVersionDAO.list(exhibition = exhibition, contentVersion = contentVersion, deviceGroup = deviceGroup)
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