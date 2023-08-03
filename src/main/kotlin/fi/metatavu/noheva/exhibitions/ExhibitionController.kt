package fi.metatavu.noheva.exhibitions

import fi.metatavu.noheva.contents.ContentVersionController
import fi.metatavu.noheva.devices.ExhibitionDeviceGroupController
import fi.metatavu.noheva.persistence.dao.ExhibitionDAO
import fi.metatavu.noheva.persistence.model.*
import fi.metatavu.noheva.utils.CopyException
import fi.metatavu.noheva.utils.IdMapper
import fi.metatavu.noheva.visitors.VisitorVariableController
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibitions
 */
@ApplicationScoped
class ExhibitionController {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var visitorVariableController: VisitorVariableController

    @Inject
    lateinit var floorController: ExhibitionFloorController

    @Inject
    lateinit var roomController: ExhibitionRoomController

    @Inject
    lateinit var deviceGroupController: ExhibitionDeviceGroupController

    @Inject
    lateinit var contentVersionController: ContentVersionController

    @Inject
    lateinit var exhibitionDAO: ExhibitionDAO

    /**
     * Creates new exhibition
     *
     * @param name exhibition name
     * @param creatorId creating user id
     * @return created exhibition
     */
    fun createExhibition(name: String, creatorId: UUID): Exhibition {
        return exhibitionDAO.create(UUID.randomUUID(), name, creatorId, creatorId)
    }

    /**
     * Copy exhibition
     *
     * @param idMapper id mapper
     * @param sourceExhibition source exhibition
     * @param creatorId creating user id
     * @return copied exhibition
     */
    fun copyExhibition(
        idMapper: IdMapper,
        sourceExhibition: Exhibition,
        creatorId: UUID
    ): Exhibition {
        val sourceId = sourceExhibition.id ?: throw CopyException("Source exhibition id not found")
        val id = idMapper.assignId(sourceId) ?: throw CopyException("Target exhibition id not found")
        val name = sourceExhibition.name ?: throw CopyException("Source exhibition name not found")

        val result = exhibitionDAO.create(
            id,
            name = getUniqueName(name),
            creatorId = creatorId,
            lastModifierId = creatorId
        )

        val sourceVisitorVariables = visitorVariableController.listVisitorVariables(
            exhibition = sourceExhibition,
            name = null
        )

        val sourceFloors = floorController.listExhibitionFloors(
            exhibition = sourceExhibition
        )

        val sourceRooms = roomController.listExhibitionRooms(
            exhibition = sourceExhibition,
            floor = null
        )

        val sourceDeviceGroups = deviceGroupController.listExhibitionDeviceGroups(
            exhibition = sourceExhibition,
            room = null
        )

        /* Copy content versions that belong to exhibition but not groups because those ones will be copied
        *  later at copyDeviceGroups stage */
        val sourceContentVersions = contentVersionController.listContentVersionsWithoutDeviceGroup(
            exhibition = sourceExhibition
        )

        sourceContentVersions.forEach {
            println(it.id.toString() + " " +it.deviceGroup?.id )
        }
        sourceVisitorVariables.map(VisitorVariable::id).map(idMapper::assignId)
        sourceFloors.map(ExhibitionFloor::id).map(idMapper::assignId)
        sourceRooms.map(ExhibitionRoom::id).map(idMapper::assignId)
        sourceContentVersions.map(ContentVersion::id).map(idMapper::assignId)

        copyVisitorVariables(
            idMapper = idMapper,
            sourceVisitorVariables = sourceVisitorVariables,
            targetExhibition = result,
            creatorId = creatorId
        )

        copyFloors(
            idMapper = idMapper,
            sourceFloors = sourceFloors,
            targetExhibition = result,
            creatorId = creatorId
        )

        copyRooms(
            idMapper = idMapper,
            sourceRooms = sourceRooms,
            creatorId = creatorId
        )

        copyContentVersions(
            idMapper = idMapper,
            sourceContentVersions = sourceContentVersions,
            targetExhibition = result,
            creatorId = creatorId
        )

        copyDeviceGroups(
            idMapper = idMapper,
            sourceDeviceGroups = sourceDeviceGroups,
            creatorId = creatorId,
        )

        return result
    }

    /**
     * Copy content versions
     *
     * @param idMapper id mapper
     * @param sourceContentVersions source content versions
     * @param targetExhibition target exhibition
     * @param creatorId creating user id
     * @return copied content versions
     */
    private fun copyContentVersions(
        idMapper: IdMapper,
        sourceContentVersions: List<ContentVersion>,
        targetExhibition: Exhibition,
        creatorId: UUID
    ) {
        sourceContentVersions.map { sourceContentVersion ->
            val targetContentVersion = contentVersionController.copyContentVersion(
                sourceContentVersion = sourceContentVersion,
                targetExhibition = targetExhibition,
                targetDeviceGroup = sourceContentVersion.deviceGroup,
                idMapper = idMapper,
                creatorId = creatorId
            )

            logger.debug("Copied content version {} -> {}", sourceContentVersion.id, targetContentVersion.id)

            targetContentVersion
        }
    }

    /**
     * Copies exhibition floors
     *
     * @param idMapper id mapper
     * @param sourceFloors source floors
     * @param targetExhibition target exhibition
     * @param creatorId creating user id
     * @return copied floors
     */
    private fun copyFloors(
        idMapper: IdMapper,
        sourceFloors: List<ExhibitionFloor>,
        targetExhibition: Exhibition,
        creatorId: UUID
    ): List<ExhibitionFloor> {
        return sourceFloors.map { sourceFloor ->
            val targetFloor = floorController.copyExhibitionFloor(
                idMapper = idMapper,
                sourceFloor = sourceFloor,
                targetExhibition = targetExhibition,
                creatorId = creatorId
            )

            logger.debug("Copied floor {} -> {}", sourceFloor.id, targetFloor.id)

            targetFloor
        }
    }

    /**
     * Copies exhibition rooms
     *
     * @param idMapper id mapper
     * @param sourceRooms source rooms
     * @param creatorId creating user id
     * @return copied rooms
     */
    private fun copyRooms(
        idMapper: IdMapper,
        sourceRooms: List<ExhibitionRoom>,
        creatorId: UUID
    ): List<ExhibitionRoom> {
        return sourceRooms.map { sourceRoom ->
            val targetFloorId = idMapper.getNewId(sourceRoom.floor?.id) ?: throw CopyException("Target floor id not found")
            val targetFloor = floorController.findExhibitionFloorById(targetFloorId) ?: throw CopyException("Target floor not found")
            val targetRoom = roomController.copyRoom(
                idMapper = idMapper,
                sourceRoom = sourceRoom,
                targetFloor = targetFloor,
                creatorId = creatorId
            )

            logger.debug("Copied room {} -> {}", sourceRoom.id, targetRoom.id)

            targetRoom
        }
    }

    /**
     * Copies exhibition visitor variables
     *
     * @param idMapper id mapper
     * @param sourceDeviceGroups source device groups
     * @param targetContentVersionExhibition target content version exhibition
     * @param creatorId creating user id
     * @return copied device groups
     */
    private fun copyDeviceGroups(
        idMapper: IdMapper,
        sourceDeviceGroups: List<ExhibitionDeviceGroup>,
        creatorId: UUID,
    ): List<ExhibitionDeviceGroup> {
        return sourceDeviceGroups.map { sourceDeviceGroup ->
            val sourceRoom = sourceDeviceGroup.room ?: throw CopyException("Source room not found")
            val targetRoom = roomController.findExhibitionRoomById(idMapper.getNewId(sourceRoom.id)) ?: throw CopyException("Target room not found")

            val targetDeviceGroup = deviceGroupController.copyDeviceGroup(
                idMapper = idMapper,
                sourceDeviceGroup = sourceDeviceGroup,
                targetRoom = targetRoom,
                creatorId = creatorId
            )

            logger.debug("Copied device group {} -> {}", sourceDeviceGroup.id, targetDeviceGroup.id)

            targetDeviceGroup
        }
    }

    /**
     * Copies exhibition visitor variables
     *
     * @param idMapper id mapper
     * @param sourceVisitorVariables source visitor variables
     * @param targetExhibition target exhibition
     * @param creatorId creating user id
     * @return copied visitor variables
     */
    private fun copyVisitorVariables(
        idMapper: IdMapper,
        sourceVisitorVariables: List<VisitorVariable>,
        targetExhibition: Exhibition,
        creatorId: UUID
    ): List<VisitorVariable> {
        return sourceVisitorVariables.map { sourceVisitorVariable ->
            val targetVisitorVariable = visitorVariableController.copyVisitorVariable(
                idMapper = idMapper,
                sourceVisitorVariable = sourceVisitorVariable,
                targetExhibition = targetExhibition,
                creatorId = creatorId
            )

            logger.debug("Copied visitor variable {} -> {}", sourceVisitorVariable.id, targetVisitorVariable.id)

            targetVisitorVariable
        }
    }

    /**
     * Finds an exhibition by id
     *
     * @param id exhibition id
     * @return found exhibition or null if not found
     */
    fun findExhibitionById(id: UUID): Exhibition? {
        return exhibitionDAO.findById(id)
    }

    /**
     * Lists all exhibitions in a system
     *
     * @returns all exhibitions in a system
     */
    fun listExhibitions(): List<Exhibition> {
        return exhibitionDAO.listAll()
    }

    /**
     * Updates an exhibition
     *
     * @param exhibition exhibition to be updated
     * @param name new name for exhibition
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibition(exhibition: Exhibition, name: String, modifierId: UUID): Exhibition {
      return exhibitionDAO.updateName(exhibition, name, modifierId)
    }

    /**
     * Deletes an exhibition
     *
     * @param exhibition exhibition to be deleted
     */
    fun deleteExhibition(exhibition: Exhibition) {
        return exhibitionDAO.delete(exhibition)
    }

    /**
     * Returns unique name for exhibition
     *
     * @param desiredName desired name
     * @return unique name for exhibition
     */
    private fun getUniqueName(desiredName: String): String {
        var result = desiredName
        var index = 1

        do {
            exhibitionDAO.findByName(
                name = result
            ) ?: return result

            index++

            result = "$desiredName $index"
        } while (true)
    }

}