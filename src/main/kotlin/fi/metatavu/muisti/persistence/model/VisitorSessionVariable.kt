package fi.metatavu.muisti.persistence.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty


/**
 * JPA entity representing visitor session variable
 *
 * @author Antti Leppä
 * @author Antti Leinonen
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["visitorSession_id", "name"])])
open class VisitorSessionVariable {

    @Id
    lateinit var id: UUID

    @ManyToOne
    var visitorSession: VisitorSession? = null

    @NotEmpty
    @Column(nullable = false)
    var name: String? = null

    @NotEmpty
    @Column(nullable = false)
    var value: String? = null

}