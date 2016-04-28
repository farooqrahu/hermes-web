package es.jyago.hermes.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;


public class ImageUtil {

    public static byte[] getPhotoImageAsByteArray(URL url) throws IOException {
        BufferedImage originalImage = ImageIO.read(url);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String ext = url.getPath().substring(url.getPath().lastIndexOf(".") + 1);
        if (ext == null || ext.length() == 0) {
            ext = "jpg";
        }
        ImageIO.write(originalImage, ext, baos);
        baos.close();
        return baos.toByteArray();
    }
}
