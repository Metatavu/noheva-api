package fi.metatavu.muisti.api

import javax.enterprise.context.RequestScoped
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

@Path("/")
@RequestScoped
class TestApi {

    @GET
    @Path("/test")
    @Produces("application/json")
    fun ping(): Response {
        return Response.ok("pong").build()
    }
}