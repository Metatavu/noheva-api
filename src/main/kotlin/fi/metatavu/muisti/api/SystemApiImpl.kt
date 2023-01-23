package fi.metatavu.muisti.api

import fi.metatavu.muisti.api.spec.SystemApi
import fi.metatavu.muisti.api.spec.model.SystemMemory
import org.apache.commons.io.FileUtils
import javax.enterprise.context.RequestScoped
import javax.ws.rs.core.Response

@RequestScoped
class SystemApiImpl : SystemApi {
    override fun memory(): Response {
        val runtime = Runtime.getRuntime()

        val result = SystemMemory(
            availableProcessors = runtime.availableProcessors().toString(),
            freeMemory = FileUtils.byteCountToDisplaySize(runtime.freeMemory()),
            maxMemory = FileUtils.byteCountToDisplaySize(runtime.maxMemory())
        )

        return Response.ok(result).build()
    }

    override fun ping(): Response {
        return Response.ok("pong").build()
    }
}
