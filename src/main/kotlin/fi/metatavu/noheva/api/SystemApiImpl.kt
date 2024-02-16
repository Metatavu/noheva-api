package fi.metatavu.noheva.api

import fi.metatavu.noheva.api.spec.SystemApi
import fi.metatavu.noheva.api.spec.model.SystemMemory
import fi.metatavu.noheva.realtime.mqtt.MqttController
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.core.Response

/**
 * System api implementation
 */
@RequestScoped
@Suppress ("unused")
class SystemApiImpl : SystemApi {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var mqttController: MqttController

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
        if (!mqttController.checkConnectionStatus()) {
            logger.warn("MQTT Connection status check failed")

            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("MQTT Connection status check failed")
                .build()
        }

        return Response.ok("pong").build()
    }
}
