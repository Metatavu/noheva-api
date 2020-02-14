package fi.metatavu.muisti.persistence.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty


/**
 * JPA entity representing visitor session user
 *
 * @author Antti Leppä
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["visitorSession_id", "userId"])])
open class VisitorSessionUser {

    @Id
    var id: UUID? = null

    @ManyToOne
    var visitorSession: VisitorSession? = null

    @Column(nullable = false)
    var userId: UUID? = null

    @Column(nullable = false)
    @NotEmpty
    var tagId: String? = null

}