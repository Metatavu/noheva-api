package fi.metatavu.noheva.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * JPA entity representing visitor
 *
 * @author Antti Leppä
 */
@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "UN_EXHIBITION_TAG_ID",
            columnNames = ["exhibition_id", "tagId"]
        )
    ]
)
class Visitor {

    @Id
    var id: UUID? = null

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

    @NotNull
    @Column(nullable = false)
    var tagId: String? = null

    @Column(nullable = false)
    var userId: UUID? = null

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