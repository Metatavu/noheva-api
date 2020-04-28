package fi.metatavu.muisti.exhibitions

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.muisti.api.spec.model.Polygon
import fi.metatavu.muisti.persistence.dao.ExhibitionRoomDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import fi.metatavu.muisti.persistence.model.ExhibitionFloor
import fi.metatavu.muisti.persistence.model.ExhibitionRoom
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for exhibition rooms
 */
@ApplicationScoped
class ExhibitionRoomController() {

    @Inject
    private lateinit var exhibitionRoomDAO: ExhibitionRoomDAO

    /**
     * Creates new exhibition room
     *
     * @param exhibition exhibition
     * @param floor floor
     * @param name room name
     * @param geoShape geoShape polygon
     * @param creatorId creating user id
     * @return created exhibition room
     */
    fun createExhibitionRoom(exhibition: Exhibition, floor: ExhibitionFloor, name: String, geoShape: Polygon?, creatorId: UUID): ExhibitionRoom {
        return exhibitionRoomDAO.create(id = UUID.randomUUID(), exhibition = exhibition, floor = floor, name = name, geoShape = serializeGeoShape(geoShape), creatorId = creatorId, lastModifierId = creatorId)
    }

    /**
     * Finds an exhibition room by id
     *
     * @param id exhibition room id
     * @return found exhibition room or null if not found
     */
    fun findExhibitionRoomById(id: UUID): ExhibitionRoom? {
        return exhibitionRoomDAO.findById(id)
    }

    /**
     * Lists exhibition rooms
     *
     * @param exhibition exhibition
     * @param floor floor filter by floor. Ignored if null
     * @return List of ExhibitionRooms
     */
    fun listExhibitionRooms(exhibition: Exhibition, floor: ExhibitionFloor?): List<ExhibitionRoom> {
        return exhibitionRoomDAO.list(exhibition, floor)
    }

    /**
     * Updates an exhibition room
     *
     * @param exhibitionRoom exhibition room to be updated
     * @param name room name
     * @param geoShape geoShape polygon
     * @param floor floor
     * @param modifierId modifying user id
     * @return updated exhibition
     */
    fun updateExhibitionRoom(exhibitionRoom: ExhibitionRoom, floor: ExhibitionFloor, name: String, geoShape: Polygon?, modifierId: UUID): ExhibitionRoom {
      var result = exhibitionRoomDAO.updateName(exhibitionRoom, name, modifierId)
      result = exhibitionRoomDAO.updateFloor(result, floor, modifierId)
      result = exhibitionRoomDAO.updateGeoShape(result, serializeGeoShape(geoShape), modifierId)
      return result
    }

    /**
     * Deletes an exhibition room
     *
     * @param exhibitionRoom exhibition room to be deleted
     */
    fun deleteExhibitionRoom(exhibitionRoom: ExhibitionRoom) {
        return exhibitionRoomDAO.delete(exhibitionRoom)
    }

  /**
   * Serialize GeoJSON data
   *
   * @param geoShape polygon data
   * @return null or parsed geoShape as string
   */
  fun serializeGeoShape(geoShape: Polygon?): String? {
    geoShape ?: return null
    val objectMapper = ObjectMapper()
    return objectMapper.writeValueAsString(geoShape)
  }

}