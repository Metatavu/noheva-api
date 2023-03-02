package fi.metatavu.muisti.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing exhibition page
 *
 * @author Antti Leppä
 */
@Entity
class ExhibitionPage {

    @Id
    lateinit var id: UUID

    @ManyToOne
    var exhibition: Exhibition? = null

    @ManyToOne
    lateinit var device: ExhibitionDevice

    @ManyToOne
    lateinit var layout: PageLayout

    @ManyToOne
    lateinit var contentVersion: ContentVersion

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    @NotEmpty
    @Column(nullable = false)
    @Lob
    var resources: String? = null

    @NotEmpty
    @Column(nullable = false)
    @Lob
    var eventTriggers: String? = null

    @Lob
    var enterTransitions: String? = null

    @Lob
    var exitTransitions: String? = null

    @Column(nullable = false)
    var orderNumber: Int? = null

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