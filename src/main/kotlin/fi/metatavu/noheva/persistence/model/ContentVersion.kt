package fi.metatavu.noheva.persistence.model

import fi.metatavu.noheva.api.spec.model.ContentVersionStatus
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * JPA entity representing content version
 *
 * @author Antti Leppä
 * @author Jari Nykänen
 */
@Entity
class ContentVersion {

    @Id
    var id: UUID? = null

    @ManyToOne
    var exhibition: Exhibition? = null

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    @NotEmpty
    @Column(nullable = false)
    var language: String? = null

    var activeConditionUserVariable: String? = null

    var activeConditionEquals: String? = null

    @OneToMany(mappedBy = "contentVersion", cascade = [CascadeType.DETACH], orphanRemoval = false, targetEntity = ContentVersionRoom::class)
    var contentVersionRooms: List<ContentVersionRoom>? = null

    @Column
    var status: ContentVersionStatus? = null

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