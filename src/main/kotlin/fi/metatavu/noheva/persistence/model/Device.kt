package fi.metatavu.noheva.persistence.model

import fi.metatavu.noheva.api.spec.model.DeviceApprovalStatus
import fi.metatavu.noheva.api.spec.model.DeviceStatus
import fi.metatavu.noheva.api.spec.model.DeviceType
import java.time.OffsetDateTime
import java.util.UUID
import javax.persistence.*

/**
 * JPA entity representing device
 */
@Entity
class Device {

    @Id
    lateinit var id: UUID

    @ManyToOne
    var deviceModel: DeviceModel? = null

    @Column(nullable = true)
    var name: String? = null

    @Column(nullable = false)
    lateinit var serialNumber: String

    @Column(nullable = true)
    var description: String? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var status: DeviceStatus

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var approvalStatus: DeviceApprovalStatus

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var deviceType: DeviceType

    @Column(nullable = false)
    lateinit var version: String

    @Lob
    @Column(nullable = true)
    var deviceKey: ByteArray? = null

    @Column(nullable = true)
    var warrantyExpiry: OffsetDateTime? = null

    @Column(nullable = true)
    var usageHours: Double? = null

    @Column(nullable = true)
    var lastConnected: OffsetDateTime? = null

    @Column(nullable = false)
    lateinit var lastSeen: OffsetDateTime

    @Column(nullable = false)
    lateinit var createdAt: OffsetDateTime

    @Column(nullable = false)
    lateinit var modifiedAt: OffsetDateTime

    @Column(nullable = true)
    var lastModifierId: UUID? = null

    /**
     * JPA pre-persist event handler
     */
    @PrePersist
    fun onCreate() {
        val now = OffsetDateTime.now()
        createdAt = now
        modifiedAt = now
        lastSeen = now
    }

    /**
     * JPA pre-update event handler
     */
    @PreUpdate
    fun onUpdate() {
        modifiedAt = OffsetDateTime.now()
    }
}