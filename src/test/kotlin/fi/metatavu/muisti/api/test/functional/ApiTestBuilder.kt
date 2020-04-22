package fi.metatavu.muisti.api.test.functional

import fi.metatavu.muisti.api.test.functional.mqtt.TestMqttClient
import fi.metatavu.muisti.api.test.functional.settings.ApiTestSettings

class ApiTestBuilder: TestBuilder(ApiTestSettings()) {

    private var mqtt: TestMqttClient? = null

    /**
     * Returns initialized test MQTT client
     *
     * @return initialized test MQTT client
     */
    fun mqtt(): TestMqttClient {
        if (mqtt == null) {
            mqtt = TestMqttClient(settings)
        }

        return mqtt!!
    }

    override fun close() {
        mqtt?.close()
        mqtt = null
        super.close()
    }

}