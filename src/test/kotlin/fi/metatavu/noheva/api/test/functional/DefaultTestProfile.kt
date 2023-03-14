package fi.metatavu.noheva.api.test.functional

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Default test profile
 */
class DefaultTestProfile: QuarkusTestProfile {

    override fun getConfigOverrides(): Map<String?, String?> {
        return mapOf(
            Pair("visitor.session.timeout", "PT1M")
        )
    }

}