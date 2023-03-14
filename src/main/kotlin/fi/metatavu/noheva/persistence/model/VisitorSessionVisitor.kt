package fi.metatavu.noheva.persistence.model

import java.util.*
import javax.persistence.*


/**
 * JPA entity representing visitor session visitor
 *
 * @author Antti Leppä
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["visitorSession_id", "visitor_id"])])
class VisitorSessionVisitor {

    @Id
    var id: UUID? = null

    @ManyToOne
    var visitorSession: VisitorSession? = null

    @ManyToOne
    var visitor: Visitor? = null

}