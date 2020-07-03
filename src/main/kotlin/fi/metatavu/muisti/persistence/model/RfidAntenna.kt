package fi.metatavu.muisti.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing single RFID antenna
 *
 * @author Antti Leppä
 */
@Entity
class RfidAntenna {

    @Id
    var id: UUID? = null

    @ManyToOne
    var exhibition: Exhibition? = null

    @ManyToOne
    var room: ExhibitionRoom? = null

    @ManyToOne
    var deviceGroup: ExhibitionDeviceGroup? = null

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    @NotEmpty
    @Column(nullable = false)
    var readerId: String? = null

    @Column(nullable = false)
    var antennaNumber: Int? = null

    var locationX: Double? = null

    var locationY: Double? = null

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