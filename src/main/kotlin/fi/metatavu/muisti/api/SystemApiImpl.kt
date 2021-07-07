package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.SystemApi
import fi.metatavu.muisti.api.spec.model.SystemMemory
import org.apache.commons.io.FileUtils
import javax.enterprise.context.RequestScoped
import javax.transaction.Transactional
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
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

    override fun memory(): Response? {
        val runtime = Runtime.getRuntime();

        val result = SystemMemory()
        result.availableProcessors = runtime.availableProcessors().toString()
        result.freeMemory = FileUtils.byteCountToDisplaySize(runtime.freeMemory())
        result.maxMemory = FileUtils.byteCountToDisplaySize(runtime.maxMemory())

        return Response.ok(result).build()
    }

}
