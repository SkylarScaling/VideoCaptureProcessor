package com.wheezy.utils.file.filter;

import java.io.File;
import java.io.FileFilter;

public final class FileExtensionFilter implements FileFilter
{
    private final String extension;

    public FileExtensionFilter(String ext)
    {
        extension = '.' + ext.toUpperCase();
    }

    public boolean accept(File file)
    {
        return file.getName().toUpperCase().endsWith(extension);
    }
}