package io.semillita.hugame.editor;

import java.awt.image.BufferedImage;

public class Utils {
  public static BufferedImage scale(BufferedImage image, int newWidth, int newHeight) {
    var newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    var graphics = newImage.createGraphics();
    graphics.drawImage(image, 0, 0, newWidth, newHeight, null);

    return newImage;
  }
}
