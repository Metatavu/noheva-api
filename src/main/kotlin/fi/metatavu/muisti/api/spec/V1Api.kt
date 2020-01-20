package fi.metatavu.muisti.api.spec

import fi.metatavu.muisti.api.spec.model.Error
import javax.ws.rs.*
import javax.ws.rs.core.Response
import java.io.InputStream
import java.util.Map
import java.util.List
import javax.validation.constraints.*
import javax.validation.Valid

@Path("/v1")
open interface V1Api {
    @GET
    @Path("/system/ping")
    @Produces("text/plain")
    open fun ping(): Response?
}