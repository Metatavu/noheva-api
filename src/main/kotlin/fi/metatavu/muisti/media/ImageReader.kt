package fi.metatavu.muisti.media

import org.slf4j.Logger
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO
import javax.inject.Inject

/**
 * Image reader
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
class ImageReader private constructor() {

    @Inject
    private lateinit var logger: Logger

    /**
     * Reads image from input stream into BufferedImage
     *
     * @param data image data
     * @return BufferedImage or null if image could not be read
     */
    fun readBufferedImage(data: InputStream?): BufferedImage? {
        try {
            return ImageIO.read(data)
        } catch (e: IOException) {
            logger.warn("Could not read image", e)
        }
        return null
    }

}
