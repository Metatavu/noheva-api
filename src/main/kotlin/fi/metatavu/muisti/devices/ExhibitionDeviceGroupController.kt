package fi.metatavu.muisti.devices

import fi.metatavu.muisti.persistence.dao.ExhibitionDeviceGroupDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import fi.metatavu.muisti.persistence.model.ExhibitionRoom
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition device groups
 */
@ApplicationScoped
class ExhibitionDeviceGroupController() {

    @Inject
    private lateinit var exhibitionDeviceGroupDAO: ExhibitionDeviceGroupDAO

    /**
     * Creates new exhibition device group
     *
     * @param exhibition exhibition
     * @param room room the device group is in
     * @param name device group name
     * @param allowVisitorSessionCreation whether the group allows new visitor session creation
     * @param creatorId creating user id
     * @return created exhibition device group
     */
    fun createExhibitionDeviceGroup(exhibition: Exhibition, room: ExhibitionRoom, name: String, allowVisitorSessionCreation: Boolean, creatorId: UUID): ExhibitionDeviceGroup {
        return exhibitionDeviceGroupDAO.create(
            id = UUID.randomUUID(),
            exhibition = exhibition,
            room = room,
            name = name,
            allowVisitorSessionCreation = allowVisitorSessionCreation,
            creatorId = creatorId,
            lastModifierId = creatorId
        )
    }

    /**
     * Finds an exhibition device group by id
     *
     * @param id exhibition device group id
     * @return found exhibition device group or null if not found
     */
    fun findExhibitionDeviceGroupById(id: UUID): ExhibitionDeviceGroup? {
        return exhibitionDeviceGroupDAO.findById(id)
    }

    /**
     * Lists exhibition device groups
     *
     * @param exhibition exhibition
     * @param room filter by room. Ignored if null
     * @return List exhibition device groups
     */
    fun listExhibitionDeviceGroups(exhibition: Exhibition, room: ExhibitionRoom?): List<ExhibitionDeviceGroup> {
        return exhibitionDeviceGroupDAO.list(exhibition, room)
    }

    /**
     * Updates an exhibition device group
     *
     * @param exhibitionDeviceGroup exhibition device group to be updated
     * @param name group name
     * @param allowVisitorSessionCreation whether the group allows new visitor session creation
     * @param room room
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionDeviceGroup(exhibitionDeviceGroup: ExhibitionDeviceGroup, name: String, allowVisitorSessionCreation: Boolean, room: ExhibitionRoom, modifierId: UUID): ExhibitionDeviceGroup {
      var result = exhibitionDeviceGroupDAO.updateName(exhibitionDeviceGroup, name, modifierId)
      result = exhibitionDeviceGroupDAO.updateRoom(result, room, modifierId)
      result = exhibitionDeviceGroupDAO.updateAllowVisitorSessionCreation(result, allowVisitorSessionCreation, modifierId)
      return result
    }

    /**
     * Deletes an exhibition device group
     *
     * @param exhibitionDeviceGroup exhibition device group to be deleted
     */
    fun deleteExhibitionDeviceGroup(exhibitionDeviceGroup: ExhibitionDeviceGroup) {
        return exhibitionDeviceGroupDAO.delete(exhibitionDeviceGroup)
    }

}