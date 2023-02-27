package fi.metatavu.muisti.media

import org.slf4j.Logger
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Image scaler
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
class ImageScaler {

    @Inject
    lateinit var logger: Logger

    /**
     * Scales image to cover size x size. Accepts imageObserver
     *
     * @param originalImage image
     * @param size desired size
     * @param downScaleOnly whether to return original if both proportions are lower than desired size
     * @param imageObserver image observer
     * @return scaled image
     */
    fun scaleToCover(originalImage: BufferedImage, size: Int, downScaleOnly: Boolean, imageObserver: ImageObserver? = null): BufferedImage? {
        var width = originalImage.width
        var height = originalImage.height

        if (downScaleOnly && (width < size || height < size)) {
            return originalImage
        }

        if (width > height) {
            width = -1
            height = size
        } else {
            width = size
            height = -1
        }

        val scaledInstance = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)
        if (imageObserver != null) {
            scaledInstance.getWidth(imageObserver)
            scaledInstance.getHeight(imageObserver)
        } else {
            scaledInstance.getWidth(null)
            scaledInstance.getHeight(null)
        }
        return toBufferedImage(scaledInstance)
    }

    /**
     * Down scales image to fix size x size. Accepts image observer
     *
     * @param originalImage original image
     * @param size max width / height of new image
     * @param imageObserver image observer
     * @return scaled image
     */
    fun scaleToFit(originalImage: BufferedImage, size: Int, imageObserver: ImageObserver? = null): BufferedImage? {
        var width = size
        var height = size
        if (originalImage.height < size && originalImage.width < size) {
            return originalImage
        }
        if (originalImage.height / size > originalImage.width / size) width = -1 else height = -1
        val scaledInstance = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)
        if (imageObserver != null) {
            scaledInstance.getWidth(imageObserver)
            scaledInstance.getHeight(imageObserver)
        } else {
            scaledInstance.getWidth(null)
            scaledInstance.getHeight(null)
        }
        return toBufferedImage(scaledInstance)
    }

    /**
     * Converts image into buffered image
     *
     * @param image image
     * @return buffered image
     */
    private fun toBufferedImage(image: Image): BufferedImage? {
        return if (image is BufferedImage) {
            image
        } else {
            val bufferedImage = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)

            val graphics2D = bufferedImage.createGraphics()
            graphics2D.drawImage(image, 0, 0, null)
            graphics2D.dispose()

            bufferedImage
        }
    }

}
