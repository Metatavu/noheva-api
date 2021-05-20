package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.SystemApi
import javax.enterprise.context.RequestScoped
import javax.transaction.Transactional
import javax.ws.rs.core.Response


/**
 * System API REST endpoints
 *
 * @author Antti Leppä
 */
@RequestScoped
@Transactional
open class SystemApiImpl(): SystemApi {

    override fun ping(): Response? {
        return Response.ok("pong").build()
    }

}
