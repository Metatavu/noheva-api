package fi.metatavu.muisti.persistence.model

import java.util.*
import javax.persistence.*


/**
 * JPA entity representing content version room
 *
 * @author Jari Nykänen
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["contentVersion_id", "exhibitionRoom_id"])])
class ContentVersionRoom {

    @Id
    lateinit var id: UUID

    @ManyToOne
    var contentVersion: ContentVersion? = null

    @ManyToOne
    var exhibitionRoom: ExhibitionRoom? = null

}