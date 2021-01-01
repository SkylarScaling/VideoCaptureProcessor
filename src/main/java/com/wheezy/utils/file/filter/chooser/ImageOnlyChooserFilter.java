package com.wheezy.utils.file.filter.chooser;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.wheezy.utils.image.ImageSupport;

public class ImageOnlyChooserFilter extends FileFilter
{
    @Override
    public boolean accept(File file)
    {
        return file.isDirectory() || ImageSupport.isImageFormatSupported(file);
    }

    @Override
    public String getDescription()
    {
        StringBuilder buff = new StringBuilder("Supported Images (");
        for (String ext : ImageSupport.SUPPORTED_IMAGE_EXTENSIONS)
        {
            buff.append("*");
            buff.append(ext);
            buff.append(",");
        }
        buff.deleteCharAt(buff.lastIndexOf(","));
        buff.append(")");

        return buff.toString();
    }

    public static void main(String[] args)
    {

    }
}
