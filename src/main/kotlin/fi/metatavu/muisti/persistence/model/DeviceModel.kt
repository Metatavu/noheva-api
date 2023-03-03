package fi.metatavu.muisti.persistence.model

import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing device model
 *
 * @author Antti Leppä
 */
@Entity
class DeviceModel {

    @Id
    var id: UUID? = null

    @NotEmpty
    @Column(nullable = false)
    var manufacturer: String? = null

    @NotEmpty
    @Column(nullable = false)
    var model: String? = null

    var deviceWidth: Double? = null

    var deviceHeight: Double? = null

    var deviceDepth: Double? = null

    var screenHeight: Double? = null

    var screenWidth: Double? = null

    var heightPixels: Int? = null

    var widthPixels: Int? = null

    var density: Double? = null

    var xdpi: Double? = null

    var ydpi: Double? = null

    @Column(nullable = false)
    var capabilityTouch: Boolean = false

    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    var screenOrientation: ScreenOrientation? = null

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