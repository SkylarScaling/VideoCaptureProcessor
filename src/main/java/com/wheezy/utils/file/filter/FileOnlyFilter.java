package com.wheezy.utils.file.filter;

import java.io.File;
import java.io.FileFilter;

public class FileOnlyFilter implements FileFilter
{
    public boolean accept(File file)
    {
        return !file.isDirectory();
    }

}
