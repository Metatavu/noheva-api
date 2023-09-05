package fi.metatavu.noheva.persistence.model

import fi.metatavu.noheva.api.spec.model.LayoutType
import fi.metatavu.noheva.api.spec.model.ScreenOrientation
import org.hibernate.validator.constraints.URL
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing page layout
 *
 * @author Antti Leppä
 */
@Entity
class PageLayout {

    @Id
    lateinit var id: UUID

    @NotEmpty
    @Column(nullable = false)
    lateinit var name: String

    @NotEmpty
    @Column(nullable = false)
    @Lob
    lateinit var data: String

    @Column
    @Lob
    var defaultResources: String? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var layoutType: LayoutType

    @URL
    var thumbnailUrl: String? = null

    @ManyToOne
    var deviceModel: DeviceModel? = null

    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    lateinit var screenOrientation: ScreenOrientation

    @Column(nullable = false)
    lateinit var createdAt: OffsetDateTime

    @Column(nullable = false)
    lateinit var modifiedAt: OffsetDateTime

    @Column(nullable = false)
    lateinit var creatorId: UUID

    @Column(nullable = false)
    lateinit var lastModifierId: UUID

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