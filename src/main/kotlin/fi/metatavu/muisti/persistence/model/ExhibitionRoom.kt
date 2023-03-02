package fi.metatavu.muisti.persistence.model

import org.locationtech.jts.geom.Polygon
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing exhibition room
 *
 * @author Antti Leppä
 */
@Entity
class ExhibitionRoom {

    @Id
    lateinit var id: UUID

    @ManyToOne
    var exhibition: Exhibition? = null

    @ManyToOne
    lateinit var floor: ExhibitionFloor

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    var color: String? = null

    @Column
    var geoShape: Polygon? = null

    @Column(nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(nullable = false)
    var modifiedAt: OffsetDateTime? = null

    @Column(nullable = false)
    var creatorId: UUID? = null

    @Column(nullable = false)
    var lastModifierId: UUID? = null

    /**
     * JPA pre-persist event handler
     */
    @PrePersist
    fun onCreate() {
        createdAt = OffsetDateTime.now()
        modifiedAt = OffsetDateTime.now()
    }

    /**
     * JPA pre-update event handler
     */
    @PreUpdate
    fun onUpdate() {
        modifiedAt = OffsetDateTime.now()
    }
}