package fi.metatavu.noheva.persistence.model

import fi.metatavu.noheva.api.spec.model.DeviceGroupVisitorSessionStartStrategy
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing exhibition device group
 *
 * @author Antti Leppä
 */
@Entity
class ExhibitionDeviceGroup {

    @Id
    var id: UUID? = null

    @ManyToOne
    var exhibition: Exhibition? = null

    @ManyToOne
    var room: ExhibitionRoom? = null

    @Column(nullable = false)
    var allowVisitorSessionCreation: Boolean? = null

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var visitorSessionEndTimeout: Long? = null

    @Column
    var indexPageTimeout: Long? = null

    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    var visitorSessionStartStrategy: DeviceGroupVisitorSessionStartStrategy? = null

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