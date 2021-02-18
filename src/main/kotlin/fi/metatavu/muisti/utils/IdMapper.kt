package fi.metatavu.muisti.utils

import java.util.*

/**
 * Id mapper.
 *
 * Mapper can be used to assign new ids and to match old and new ids then copying resources
 */
class IdMapper {

    private val idMap = mutableMapOf<UUID, UUID>()

    /**
     * Assigns new id for resource to be copied
     *
     * @param oldId old id
     * @return new id
     */
    fun assignId(oldId: UUID?): UUID? {
        oldId ?: return null
        val newId = UUID.randomUUID()
        idMap[oldId] = newId
        return newId
    }

    /**
     * Returns new id for given old id.
     *
     * Id must be mapped before using the method with assignId -method
     *
     * @param oldId old id
     * @return new id or null if not found
     */
    fun getNewId(oldId: UUID?): UUID? {
        oldId ?: return null
        return idMap[oldId]
    }

}