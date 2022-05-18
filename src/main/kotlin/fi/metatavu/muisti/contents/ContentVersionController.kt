package fi.metatavu.muisti.contents

import fi.metatavu.muisti.api.spec.model.ContentVersionActiveCondition
import fi.metatavu.muisti.exhibitions.ExhibitionRoomController
import fi.metatavu.muisti.persistence.dao.ContentVersionDAO
import fi.metatavu.muisti.persistence.dao.ContentVersionRoomDAO
import fi.metatavu.muisti.persistence.model.*
import fi.metatavu.muisti.utils.CopyException
import fi.metatavu.muisti.utils.IdMapper
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for content versions
 */
@ApplicationScoped
class ContentVersionController {

    @Inject
    private lateinit var contentVersionDAO: ContentVersionDAO

    @Inject
    private lateinit var contentVersionRoomDAO: ContentVersionRoomDAO

    @Inject
    private lateinit var roomController: ExhibitionRoomController

    /**
     * Creates new content version
     *
     * @param name content version name
     * @param language language code
     * @param activeCondition active condition
     * @param creatorId creating user id
     * @return created exhibition content version
     */
    fun createContentVersion(
        exhibition: Exhibition,
        name: String,
        language: String,
        activeCondition: ContentVersionActiveCondition?,
        creatorId: UUID
    ): ContentVersion {
        return contentVersionDAO.create(
            id = UUID.randomUUID(),
            exhibition = exhibition,
            name = name,
            language = language,
            activeConditionEquals = activeCondition?.equals,
            activeConditionUserVariable = activeCondition?.userVariable,
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
     * @param creatorId id of user that created the copy
     * @return a copy of a content version
     */
    fun copyContentVersion(
        sourceContentVersion: ContentVersion,
        targetExhibition: Exhibition,
        idMapper: IdMapper,
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
     * @returns list of content versions
     */
    fun listContentVersions(
        exhibition: Exhibition,
        exhibitionRoom: ExhibitionRoom?
    ): List<ContentVersion> {

        if (exhibitionRoom != null) {
            return contentVersionRoomDAO.listContentVersionsByRoom(exhibitionRoom)
        }

        return contentVersionDAO.listByExhibition(exhibition)
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
     * @param modifierId modifying user id
     * @return updated ContentVersion
     */
    fun updateContentVersion(
        contentVersion: ContentVersion,
        name: String,
        language: String,
        activeCondition: ContentVersionActiveCondition?,
        modifierId: UUID
    ): ContentVersion {
        var result = contentVersionDAO.updateName(contentVersion, name, modifierId)
        result = contentVersionDAO.updateLanguage(result, language, modifierId)
        result = contentVersionDAO.updateActiveConditionUserVariable(result, activeCondition?.userVariable, modifierId)
        result = contentVersionDAO.updateActiveConditionEquals(result, activeCondition?.equals, modifierId)
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
