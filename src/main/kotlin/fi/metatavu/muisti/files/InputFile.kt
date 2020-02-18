package fi.metatavu.muisti.files

import java.io.InputStream


/**
 * Class representing a file uploaded into the system but not yet persisted into the database
 *
 * @author Antti Leppä
 */
open class InputFile
/**
 * Constructor
 *
 * @param folder folder the file is stored in
 * @param meta file meta
 * @param data file data
 */(
        /**
         * Returns folder the file is stored in
         *
         * @return folder the file is stored in
         */
        val folder: String,
        /**
         * Returns meta
         *
         * @return meta
         */
        val meta: FileMeta,
        /**
         * Returns data
         *
         * @return data
         */
        val data: InputStream?) : AutoCloseable {

    @Throws(Exception::class)
    override fun close() {
        data?.close()
    }

}