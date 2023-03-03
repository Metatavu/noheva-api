package fi.metatavu.muisti.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

/**
 * JPA entity representing visitor session visited device group
 *
 * @author Antti Leppä
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(name="UN_VISITOR_SESSION_VISITED_DEVICE_GROUP_IDS", columnNames = ["visitorSession_id", "deviceGroup_id"])])
class VisitorSessionVisitedDeviceGroup {

    @Id
    var id: UUID? = null

    @ManyToOne
    var visitorSession: VisitorSession? = null

    @ManyToOne
    var deviceGroup: ExhibitionDeviceGroup? = null

    @Column(nullable = false)
    var enteredAt: OffsetDateTime? = null

    @Column(nullable = false)
    var exitedAt: OffsetDateTime? = null

}