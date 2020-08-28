package fi.metatavu.muisti.media

import org.apache.commons.codec.binary.StringUtils
import org.slf4j.Logger
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO
import javax.inject.Inject

/**
 * Image writer
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
class ImageWriter {

    @Inject
    private lateinit var logger: Logger

    /**
     * Returns preferred ImageIO format for given content type
     *
     * @param contentType content type
     * @return preferred ImageIO format for given content type
     */
    fun getFormatName(contentType: String?): String {
        return if (StringUtils.equals(contentType, "image/png")) {
            "png"
        } else "jpg"
    }

    /**
     * Returns appropriate content type for given ImageIO format
     *
     * @param formatName format name
     * @return appropriate content type for given ImageIO format
     */
    fun getContentTypeForFormatName(formatName: String?): String {
        return if (StringUtils.equals(formatName, "png")) {
            "image/png"
        } else "image/jpeg"
    }

    /**
     * Writes buffered image as byte array
     *
     * @param image image
     * @param formatName target format name
     * @return image data as byte array or null when writing has failed
     */
    fun writeBufferedImage(image: BufferedImage?, formatName: String?): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        try {
            if (ImageIO.write(image, formatName, outputStream)) {
                outputStream.flush()
                outputStream.close()
                return outputStream.toByteArray()
            }
        } catch (e: IOException) {
            logger.error("Failed to write buffered image", e)
        }
        return null
    }
}