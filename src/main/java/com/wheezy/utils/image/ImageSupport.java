package com.wheezy.utils.image;

import java.io.File;

public final class ImageSupport
{
    /**
     * Supported extensions:<br>
     * .tif<br>
     * .tiff<br>
     * .jpg<br>
     * .jpeg<br>
     * .gif<br>
     * .png<br>
     */
    public static final String[] SUPPORTED_IMAGE_EXTENSIONS =
            {
                    ".tif", ".tiff", ".jpg", ".jpeg", ".gif", ".png"
            };

    /**
     * Checks to see if file format is supported based on the file extension.
     */
    public static boolean isImageFormatSupported(File imageFile)
    {
        for (String ext : SUPPORTED_IMAGE_EXTENSIONS)
        {
            if (imageFile.getName().endsWith(ext))
            {
                return true;
            }
        }
        return false;
    }
}
