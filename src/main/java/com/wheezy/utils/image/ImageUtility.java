package com.wheezy.utils.image;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageUtility
{
    /**
     * Minimum pixel size at which image blurring is not used.
     * Blurring allows images smaller than the threshold to look clearer.
     * This threshold applies to the smallest dimension of a processed image.
     */
    private static final int IMAGE_SIZE_BLUR_THRESHOLD = 50;

    /**
     * Resizes an image from a file to the specified dimensions.
     *
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage resizeImageFromFile(File imageFile, int width,
                                                    int height, boolean keepAspectRatio) throws IOException
    {
        return resizeImage(ImageIO.read(imageFile), width, height, keepAspectRatio);
    }

    /**
     * Resizes an image to the specified dimensions.
     *
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage resizeImage(BufferedImage image, int width,
                                            int height, boolean keepAspectRatio) throws IOException
    {
        if (keepAspectRatio)
        {
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            Float ratio = ((float) imageWidth / (float) imageHeight);
            if (ratio <= 1)
            {
                width = (int) (width * ratio);
            }
            else
            {
                height = (int) (height / ratio);
            }
        }

        if (width < IMAGE_SIZE_BLUR_THRESHOLD || height < IMAGE_SIZE_BLUR_THRESHOLD)
        {
            return resizeToSmallImage(image, width, height);
        }
        return resize(image, width, height);
    }

    /**
     * Saves the provided image to the designated file path. Path will automatically
     * have the .png extension added if it is not included.
     *
     * @return File path for created file
     * @throws IOException
     */
    public static String saveImageToPngFile(BufferedImage image, String filepath) throws IOException
    {
        if (!filepath.endsWith(".png"))
        {
            if (filepath.lastIndexOf(".") != -1)
            {
                filepath = filepath.substring(0, filepath.lastIndexOf("."));
            }
            filepath += ".png";
        }
        ImageIO.write(image, "png", new File(filepath));
        return filepath;
    }

    /**
     * Performs an image resize with anti-aliasing.
     *
     * @throws IOException
     */
    private static BufferedImage resize(BufferedImage image, int width, int height) throws IOException
    {
        int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    /**
     * Performs an image resize with anti-aliasing and blurring.
     * Recommended for creating very small images.
     *
     * @throws IOException
     */
    private static BufferedImage resizeToSmallImage(BufferedImage image, int width, int height)
            throws IOException
    {
        image = createCompatibleImage(image);
        image = resize(image, 100, 100);
        image = blurImage(image);

        return resize(image, width, height);
    }

    /**
     * Blurs an image. Used when resizing images to very small sizes.
     */
    private static BufferedImage blurImage(BufferedImage image)
    {
        float ninth = 1.0f / 9.0f;
        float[] blurKernel =
                {
                        ninth, ninth, ninth,
                        ninth, ninth, ninth,
                        ninth, ninth, ninth
                };

        Map<RenderingHints.Key, Object> map = new HashMap<RenderingHints.Key, Object>();
        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RenderingHints hints = new RenderingHints(map);
        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, hints);

        return op.filter(image, null);
    }

    private static BufferedImage createCompatibleImage(BufferedImage image)
    {
    	GraphicsConfiguration gc = new Canvas(null).getGraphicsConfiguration();
        BufferedImage result = gc.createCompatibleImage(image.getWidth(), image.getHeight(),
                Transparency.TRANSLUCENT);
        Graphics2D g2 = result.createGraphics();
        g2.drawRenderedImage(image, null);
        g2.dispose();

        return result;
    }

    public static void main(String[] args)
    {
        try
        {
            saveImageToPngFile(resizeImageFromFile(
                    new File("images/Evie.jpg"), 64, 64, true), "images/generated");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
