package fi.metatavu.muisti.persistence.model

import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing exhibition device
 *
 * @author Antti Leppä
 */
@Entity
class ExhibitionDevice {

    @Id
    var id: UUID? = null

    @ManyToOne
    var exhibition: Exhibition? = null

    @ManyToOne
    var exhibitionDeviceGroup: ExhibitionDeviceGroup? = null

    @ManyToOne
    var deviceModel: DeviceModel? = null

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    var locationX: Double? = null

    var locationY: Double? = null

    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    var screenOrientation: ScreenOrientation? = null

    @ManyToOne
    var idlePage: ExhibitionPage? = null

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