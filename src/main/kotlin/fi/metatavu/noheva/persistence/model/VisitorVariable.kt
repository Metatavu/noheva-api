package fi.metatavu.noheva.persistence.model

import fi.metatavu.noheva.api.spec.model.VisitorVariableType
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty


/**
 * JPA entity representing visitor variable
 *
 * @author Antti Leppä
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["exhibition_id", "name"])])
class VisitorVariable {

    @Id
    var id: UUID? = null

    @ManyToOne
    var exhibition: Exhibition? = null

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    var type:VisitorVariableType? = null

    @Column(nullable = false)
    var editableFromUI: Boolean? = null

    @Lob
    var enum: String? = null

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