package fi.metatavu.noheva.persistence.model

import fi.metatavu.noheva.api.spec.model.GroupContentVersionStatus
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing group content version
 *
 * @author Jari Nykänen
 */
@Entity
class GroupContentVersion {

    @Id
    var id: UUID? = null

    @ManyToOne
    var exhibition: Exhibition? = null

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var status: GroupContentVersionStatus? = null

    @ManyToOne
    var contentVersion: ContentVersion? = null

    @ManyToOne
    var deviceGroup: ExhibitionDeviceGroup? = null

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