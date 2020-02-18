package fi.metatavu.muisti.files

import java.net.URI

/**
 * Class representing a persisted file
 *
 * @author Antti Leppä
 */
data class OutputFile (

        var meta: FileMeta,

        var uri: URI
)