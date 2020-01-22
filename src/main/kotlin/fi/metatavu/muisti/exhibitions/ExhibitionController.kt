package fi.metatavu.muisti.exhibitions

import fi.metatavu.muisti.persistence.dao.ExhibitionDAO
import fi.metatavu.muisti.persistence.model.Exhibition
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
open class ExhibitionController() {

    @Inject
    private lateinit var exhibitionDAO: ExhibitionDAO

    open fun createExhibition(name: String, creatorId: UUID): Exhibition {
        return exhibitionDAO.create(UUID.randomUUID(), name, creatorId, creatorId)
    }

    open fun findExhibitionById(id: UUID): Exhibition {
        return exhibitionDAO.findById(id)
    }

    open fun listExhibitions(): List<Exhibition> {
        return exhibitionDAO.listAll()
    }

    open fun updateExhibition(exhibition: Exhibition, name: String, modifierId: UUID): Exhibition {
      return exhibitionDAO.updateName(exhibition, name, modifierId)
    }

    open fun deleteExhibition(exhibition: Exhibition) {
        return exhibitionDAO.delete(exhibition)
    }

}