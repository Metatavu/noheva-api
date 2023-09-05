package fi.metatavu.noheva.api.translate

import java.util.*
import java.util.stream.Collectors
/**
 * Abstract translator class
 *
 * @author Antti Leppä
 */
abstract class AbstractTranslator<E, R> {

    abstract fun translate(entity: E): R

    /**
     * Translates list of entities
     *
     * @param entities list of entities to translate
     * @return List of translated entities
     */
    @Suppress("UNCHECKED_CAST")
    open fun translate(entities: List<E>): List<R> {
        return entities.stream()
          .map { entity: E -> this.translate(entity) }
          .filter(Objects::nonNull)
          .collect(Collectors.toList()) as List<R>
    }

}