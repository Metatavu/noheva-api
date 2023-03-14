package fi.metatavu.noheva.persistence.model

import fi.metatavu.noheva.api.spec.model.VisitorSessionState
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

/**
 * JPA entity representing visitor
 *
 * @author Antti Leppä
 */
@Entity
class VisitorSession {

    @Id
    var id: UUID? = null

    @Column(nullable = false)
    var expiresAt: OffsetDateTime? = null

    @Column(nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(nullable = false)
    var modifiedAt: OffsetDateTime? = null

    @Column(nullable = false)
    var creatorId: UUID? = null

    @Column(nullable = false)
    var lastModifierId: UUID? = null

    @ManyToOne
    var exhibition: Exhibition? = null

    var state: VisitorSessionState? = null

    @Column(nullable = false)
    var language: String? = null

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