package fi.metatavu.muisti.persistence.model

import fi.metatavu.muisti.api.spec.model.ScreenOrientation
import org.hibernate.validator.constraints.URL
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing sub layout
 *
 * @author Jari Nykänen
 */
@Entity
class SubLayout {

    @Id
    lateinit var id: UUID

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    @NotEmpty
    @Column(nullable = false)
    @Lob
    var data: String? = null

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