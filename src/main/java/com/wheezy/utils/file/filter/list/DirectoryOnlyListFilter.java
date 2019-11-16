package com.wheezy.utils.file.filter.list;

import java.io.File;
import java.io.FileFilter;

public class DirectoryOnlyListFilter implements FileFilter
{
    public boolean accept(File file)
    {
        return file.isDirectory();
    }

}
