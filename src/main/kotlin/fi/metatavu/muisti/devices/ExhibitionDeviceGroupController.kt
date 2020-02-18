package fi.metatavu.muisti.devices

import fi.metatavu.muisti.persistence.dao.ExhibitionDeviceGroupDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionDeviceGroup
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition device groups
 */
@ApplicationScoped
open class ExhibitionDeviceGroupController() {

    @Inject
    private lateinit var exhibitionDeviceGroupDAO: ExhibitionDeviceGroupDAO

    /**
     * Creates new exhibition device group
     *
     * @param name device group name
     * @param creatorId creating user id
     * @return created exhibition device group
     */
    open fun createExhibitionDeviceGroup(exhibition: Exhibition, name: String, creatorId: UUID): ExhibitionDeviceGroup {
        return exhibitionDeviceGroupDAO.create(UUID.randomUUID(), exhibition, name, creatorId, creatorId)
    }

    /**
     * Finds an exhibition device group by id
     *
     * @param id exhibition device group id
     * @return found exhibition device group or null if not found
     */
    open fun findExhibitionDeviceGroupById(id: UUID): ExhibitionDeviceGroup? {
        return exhibitionDeviceGroupDAO.findById(id)
    }

    /**
     * Lists device groups in an exhibitions
     *
     * @returns all deviceGroups in an exhibition
     */
    open fun listExhibitionDeviceGroups(exhibition: Exhibition): List<ExhibitionDeviceGroup> {
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
    open fun updateExhibitionDeviceGroup(exhibitionDeviceGroup: ExhibitionDeviceGroup, name: String, modifierId: UUID): ExhibitionDeviceGroup {
      return exhibitionDeviceGroupDAO.updateName(exhibitionDeviceGroup, name, modifierId)
    }

    /**
     * Deletes an exhibition device group
     *
     * @param exhibitionDeviceGroup exhibition device group to be deleted
     */
    open fun deleteExhibitionDeviceGroup(exhibitionDeviceGroup: ExhibitionDeviceGroup) {
        return exhibitionDeviceGroupDAO.delete(exhibitionDeviceGroup)
    }

}