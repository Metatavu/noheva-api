package fi.metatavu.noheva.api.test.functional.resources

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.slf4j.event.Level
import org.testcontainers.hivemq.HiveMQContainer
import org.testcontainers.utility.DockerImageName


/**
 * Starts test container for mysql
 */
class MqttResource : QuarkusTestResourceLifecycleManager {
    override fun start(): Map<String, String> {
        hivemqCe.start()
        val config: MutableMap<String, String> = HashMap()
        config["mqtt.server.url"] = hivemqCe.host +":"+ hivemqCe.mqttPort
        config["mqtt.topic"] = "test"
        return config
    }

    override fun stop() {
        hivemqCe.stop()
    }

    companion object {
        var hivemqCe: HiveMQContainer = HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce")).withLogLevel(Level.DEBUG)
    }
}