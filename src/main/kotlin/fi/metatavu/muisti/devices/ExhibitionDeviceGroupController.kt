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
     * @param name device group name
     * @param room room the device group is in
     * @param creatorId creating user id
     * @return created exhibition device group
     */
    fun createExhibitionDeviceGroup(exhibition: Exhibition, room: ExhibitionRoom, name: String, creatorId: UUID): ExhibitionDeviceGroup {
        return exhibitionDeviceGroupDAO.create(UUID.randomUUID(), exhibition, room, name, creatorId, creatorId)
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
     * Lists device groups in an exhibitions
     *
     * @returns all deviceGroups in an exhibition
     */
    fun listExhibitionDeviceGroups(exhibition: Exhibition): List<ExhibitionDeviceGroup> {
        return exhibitionDeviceGroupDAO.listByExhibition(exhibition)
    }

    /**
     * Updates an exhibition device group
     *
     * @param exhibitionDeviceGroup exhibition device group to be updated
     * @param name group name
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionDeviceGroup(exhibitionDeviceGroup: ExhibitionDeviceGroup, name: String, room: ExhibitionRoom, modifierId: UUID): ExhibitionDeviceGroup {
      var result = exhibitionDeviceGroupDAO.updateName(exhibitionDeviceGroup, name, modifierId)
      result = exhibitionDeviceGroupDAO.updateRoom(result, room, modifierId)
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