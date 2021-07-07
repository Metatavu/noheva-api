package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.SystemApi
import org.apache.commons.io.FileUtils
import javax.enterprise.context.RequestScoped
import javax.transaction.Transactional
import javax.ws.rs.Path
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

    @Path("/memory")
    fun memory(): Response? {
        val runtime = Runtime.getRuntime();

        val result = mutableMapOf<String, String>()
        result.put("freeMemory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()))
        result.put("availableProcessors", runtime.availableProcessors().toString())
        result.put("maxMemory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()))

        return Response.ok(result).build()
    }

}
