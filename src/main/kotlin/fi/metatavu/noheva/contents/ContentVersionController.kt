package fi.metatavu.noheva.contents

import fi.metatavu.noheva.api.spec.model.ContentVersionActiveCondition
import fi.metatavu.noheva.api.spec.model.ContentVersionStatus
import fi.metatavu.noheva.exhibitions.ExhibitionRoomController
import fi.metatavu.noheva.persistence.dao.ContentVersionDAO
import fi.metatavu.noheva.persistence.dao.ContentVersionRoomDAO
import fi.metatavu.noheva.persistence.model.*
import fi.metatavu.noheva.utils.CopyException
import fi.metatavu.noheva.utils.IdMapper
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for content versions
 */
@ApplicationScoped
class ContentVersionController {

    @Inject
    lateinit var contentVersionDAO: ContentVersionDAO

    @Inject
    lateinit var contentVersionRoomDAO: ContentVersionRoomDAO

    @Inject
    lateinit var roomController: ExhibitionRoomController

    /**
     * Creates new content version
     *
     * @param name content version name
     * @param language language code
     * @param activeCondition active condition
     * @param status status
     * @param deviceGroup device group
     * @param creatorId creating user id
     * @return created exhibition content version
     */
    fun createContentVersion(
        exhibition: Exhibition,
        name: String,
        language: String,
        activeCondition: ContentVersionActiveCondition?,
        status: ContentVersionStatus?,
        deviceGroup: ExhibitionDeviceGroup?,
        creatorId: UUID
    ): ContentVersion {
        return contentVersionDAO.create(
            id = UUID.randomUUID(),
            exhibition = exhibition,
            name = name,
            language = language,
            activeConditionEquals = activeCondition?.equals,
            activeConditionUserVariable = activeCondition?.userVariable,
            status = status,
            deviceGroup = deviceGroup,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Creates a copy of a content version
     *
     * @param sourceContentVersion source content version
     * @param targetExhibition target exhibition
     * @param idMapper id mapper
     * @param targetDeviceGroup target device group for the content version
     * @param creatorId id of user that created the copy
     * @return a copy of a content version
     */
    fun copyContentVersion(
        sourceContentVersion: ContentVersion,
        targetExhibition: Exhibition,
        idMapper: IdMapper,
        targetDeviceGroup: ExhibitionDeviceGroup?,
        creatorId: UUID
    ): ContentVersion {
        val id = idMapper.getNewId(sourceContentVersion.id) ?: throw CopyException("Target content version id not found")
        val sourceName = sourceContentVersion.name ?: throw CopyException("Source content version name not found")
        val language = sourceContentVersion.language ?: throw CopyException("Source content language name not found")
        val sameExhibition = targetExhibition.id == sourceContentVersion.exhibition?.id

        val sourceRooms = contentVersionRoomDAO.listRoomsByContentVersion(sourceContentVersion)
            .mapNotNull { contentVersionRoom -> contentVersionRoom.exhibitionRoom }

        val targetRooms = if (sameExhibition) sourceRooms else sourceRooms.map { sourceRoom ->
            val targetRoomId = idMapper.getNewId(sourceRoom.id) ?: throw CopyException("Target room id not found")
            roomController.findExhibitionRoomById(targetRoomId) ?: throw CopyException("Target room not found")
        }

        val name = if (!sameExhibition) sourceName else getUniqueName(
            desiredName = sourceName,
            language = language,
            rooms = sourceRooms
        )

        val result = contentVersionDAO.create(
            id = id,
            exhibition = targetExhibition,
            name = name,
            language = language,
            activeConditionUserVariable = sourceContentVersion.activeConditionUserVariable,
            activeConditionEquals = sourceContentVersion.activeConditionEquals,
            status = sourceContentVersion.status,
            deviceGroup = targetDeviceGroup,
            creatorId = creatorId,
            lastModifierId = creatorId
        )

        setContentVersionRooms(
            contentVersion = result,
            rooms = targetRooms
        )

        return result
    }

    /**
     * Finds content version by id
     *
     * @param id content version id
     * @return found content version or null if not found
     */
    fun findContentVersionById(id: UUID): ContentVersion? {
        return contentVersionDAO.findById(id)
    }

    /**
     * Finds content version by name, room and language
     *
     * @param name name
     * @param language language
     * @param room room
     * @return found content version or null if not found
     */
    fun findContentVersionByNameRoomAndLanguage(name: String, language: String, room: ExhibitionRoom): ContentVersion? {
        return contentVersionDAO.findByNameRoomAndLanguage(
            name = name,
            room = room,
            language = language
        )
    }

    /**
     * Finds content version by name, rooms and language 
     *
     * @param name name
     * @param language language
     * @param rooms rooms
     * @return found content version or null if not found
     */
    fun findContentVersionByNameRoomsAndLanguage(name: String, language: String, rooms: List<ExhibitionRoom>): ContentVersion? {
        return rooms.map { findContentVersionByNameRoomAndLanguage(
            language = language,
            name = name,
            room = it
        ) }.firstOrNull()
    }

    /**
     * Lists content versions in an exhibitions
     * @param exhibition exhibition
     * @param exhibitionRoom exhibition room
     * @param deviceGroup device group
     * @returns list of content versions
     */
    fun listContentVersions(
        exhibition: Exhibition,
        exhibitionRoom: ExhibitionRoom?,
        deviceGroup: ExhibitionDeviceGroup?,
    ): List<ContentVersion> {

        if (exhibitionRoom == null && deviceGroup == null) {
            return contentVersionDAO.listByExhibition(exhibition)
        }

        return contentVersionDAO.listContentVersions(exhibitionRoom, deviceGroup)
    }

    /**
     * Lists content versions based on exhibitions that are not connected to a device group
     *
     * @param exhibition exhibition
     * @returns list of content versions
     */
    fun listContentVersionsWithoutDeviceGroup(
        exhibition: Exhibition,
    ): List<ContentVersion> {
        return contentVersionDAO.listByExhibitionWithoutDeviceGroup(exhibition)
    }

    /**
     * Sets content version rooms
     *
     * @param contentVersion content version
     * @param rooms list of exhibition rooms
     */
    fun setContentVersionRooms(contentVersion: ContentVersion, rooms: List<ExhibitionRoom>) {
        val existingContentVersionRooms = contentVersionRoomDAO.listRoomsByContentVersion(contentVersion).toMutableList()

        for (room in rooms) {
            val existingContentVersionRoom = existingContentVersionRooms.find { it.exhibitionRoom?.id == room.id }
            if (existingContentVersionRoom == null) {
                contentVersionRoomDAO.create(UUID.randomUUID(), contentVersion, room)
            } else {
                existingContentVersionRooms.remove(existingContentVersionRoom)
            }
        }
        existingContentVersionRooms.forEach(contentVersionRoomDAO::delete)
    }

    /**
     * Updates content version
     *
     * @param contentVersion content version to be updated
     * @param name group name
     * @param language language code
     * @param activeCondition active condition
     * @param status status
     * @param deviceGroup device group
     * @param modifierId modifying user id
     * @return updated ContentVersion
     */
    fun updateContentVersion(
        contentVersion: ContentVersion,
        name: String,
        language: String,
        activeCondition: ContentVersionActiveCondition?,
        status: ContentVersionStatus?,
        deviceGroup: ExhibitionDeviceGroup?,
        modifierId: UUID
    ): ContentVersion {
        var result = contentVersionDAO.updateName(contentVersion, name, modifierId)
        result = contentVersionDAO.updateLanguage(result, language, modifierId)
        result = contentVersionDAO.updateActiveConditionUserVariable(result, activeCondition?.userVariable, modifierId)
        result = contentVersionDAO.updateActiveConditionEquals(result, activeCondition?.equals, modifierId)
        result = contentVersionDAO.updateStatus(result, status, modifierId)
        result = contentVersionDAO.updateDeviceGroup(result, deviceGroup, modifierId)
        return result
    }

    /**
     * Deletes a content version and all relations
     *
     * @param contentVersion content version to be deleted
     */
    fun deleteContentVersion(contentVersion: ContentVersion) {
        contentVersionRoomDAO.listRoomsByContentVersion(contentVersion).forEach(contentVersionRoomDAO::delete)
        contentVersionDAO.delete(contentVersion)
    }

    /**
     * Returns unique name for content version
     *
     * @param desiredName desired name
     * @param language language
     * @param rooms rooms of content version
     * @return unique name for content version
     */
    private fun getUniqueName(
        desiredName: String,
        language: String,
        rooms: List<ExhibitionRoom>
    ): String {
        var result = desiredName
        var index = 1

        do {
            findContentVersionByNameRoomsAndLanguage(
                name = result,
                language = language,
                rooms = rooms
            ) ?: return result

            index++

            result = "$desiredName $index"
        } while (true)
    }
}
