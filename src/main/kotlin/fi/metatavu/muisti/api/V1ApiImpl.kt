package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.V1Api

import java.util.UUID
import java.util.stream.Collectors
import javax.ejb.Stateful
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.Response


/**
 * REST endpoints
 *
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
open class V1ApiImpl(): V1Api {

    override fun ping(): Response? {
        return Response.ok("pong").build()
    }

}
