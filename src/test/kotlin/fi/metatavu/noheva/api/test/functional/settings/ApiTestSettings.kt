package fi.metatavu.noheva.api.test.functional.settings

/**
 * Settings implementation for test builder
 *
 * @author Antti Leppä
 */
class ApiTestSettings() {
    companion object {

        /**
         * Returns API service base path
         */
        val apiBasePath: String
            get() = "http://localhost:8081"

        /**
         * Returns API service base path
         */
        val apiBasePathNoPort: String
            get() = "http://localhost"

        /**
         * Returns API service port
         */
        val apiBasePort: Int
            get() = 8081

    }
}