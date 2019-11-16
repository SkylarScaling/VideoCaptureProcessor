package com.wheezy.utils.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;

import com.wheezy.utils.file.filter.list.FileExtensionListFilter;
import com.wheezy.utils.file.filter.list.FileOnlyListFilter;
import com.wheezy.utils.file.filter.list.FileStartsWithListFilter;

public class FileUtility
{
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  public static final String FILE_SEPARATOR = System.getProperty("file.separator");

  public static final int SUCCESS = 0;
  public static final int FILE_DOES_NOT_EXIST = 1;
  public static final int DIRECTORY_CREATION_ERROR = 2;
  public static final int FILE_MOVE_ERROR = 3;
  public static final int INVALID_COPY_LOCATION = 4;
  public static final int DIRECTORY_DELETE_ERROR = 5;
  public static final int FILE_DELETE_ERROR = 6;

  public static final String[] ERROR_DESCRIPTIONS = { "Success", // 0
      "File does not exist", // 1
      "Directory creation error", // 2
      "File move error", // 3
      "Invalid copy location", // 4
      "Directory delete error", // 5
      "File delete error" // 6
  };

  /**
   * Copy files or directories from one location to another. Source and
   * destination must be mutually exclusive.
   *
   * @return Return Codes:<br>
   *         FileUtility.SUCCESS<br>
   *         FileUtility.SRC_FILE_DOES_NOT_EXIST<br>
   *         FileUtility.DIRECTORY_CREATION_ERROR<br>
   *         FileUtility.INVALID_COPY_LOCATION<br>
   * @throws IOException
   */
  public static int copyFiles(String srcFilename, String destFilename) throws IOException
  {
    return copyFiles(new File(srcFilename), new File(destFilename), false);
  }

  /**
   * Copy files or directories from one location to another. Source and
   * destination must be mutually exclusive.
   *
   * @return Return Codes:<br>
   *         FileUtility.SUCCESS<br>
   *         FileUtility.SRC_FILE_DOES_NOT_EXIST<br>
   *         FileUtility.DIRECTORY_CREATION_ERROR<br>
   *         FileUtility.INVALID_COPY_LOCATION<br>
   * @throws IOException
   */
  public static int copyFiles(File srcFile, File destFile) throws IOException
  {
    return copyFiles(srcFile, destFile, false);
  }

  /**
   * Copy files or directories from one location to another. Source and
   * destination must be mutually exclusive. If the 'contentsOnly' flag is true,
   * only the contents of the top level directory will be copied, not the folder
   * itself.
   *
   * @return Return Codes:<br>
   *         FileUtility.SUCCESS<br>
   *         FileUtility.FILE_DOES_NOT_EXIST<br>
   *         FileUtility.DIRECTORY_CREATION_ERROR<br>
   * @throws IOException
   */
  public static int copyFiles(File srcFile, File destFile, boolean contentsOnly) throws IOException
  {
    // Make sure srcFile exists
    if (!srcFile.exists())
    {
      return FileUtility.FILE_DOES_NOT_EXIST;
    }

    // Check if srcFile is a directory
    if (srcFile.isDirectory())
    {
      if (!contentsOnly)
      {
        destFile = new File(destFile.getCanonicalPath() + FILE_SEPARATOR + srcFile.getName());
      }

      if (!destFile.exists())
      {
        if (!destFile.mkdirs())
        {
          return FileUtility.DIRECTORY_CREATION_ERROR;
        }
      }

      // Copy all files in the directory
      String files[] = srcFile.list();
      for (String file : files)
      {
        File dest = new File(destFile, file);
        File src = new File(srcFile, file);
        copyFiles(src, dest, true);
      }
    }
    else
    {
      // Copy file
      FileInputStream fis = new FileInputStream(srcFile);
      FileOutputStream fos = new FileOutputStream(destFile);

      FileChannel srcChannel = null;
      FileChannel destChannel = null;
      try
      {
        srcChannel = fis.getChannel();
        destChannel = fos.getChannel();
        /*
         * TODO This kind of transfer can cause problems if files are over 1GB and we're
         * running on a 32-bit Java.
         * 
         * Alternative: // For creating a byte type buffer byte[] buf = new byte[1024];
         * // For writing to another specified file from buffer buf out.write(buf, 0,
         * len);
         */
        destChannel.transferFrom(srcChannel, 0, srcChannel.size());
      }
      finally
      {
        if (srcChannel != null)
        {
          try
          {
            srcChannel.close();
          }
          catch (IOException e)
          {
            // Don't care
          }
        }
        if (destChannel != null)
        {
          try
          {
            destChannel.close();
          }
          catch (IOException e)
          {
            // Don't care
          }
        }
        if (fis != null)
        {
          try
          {
            fis.close();
          }
          catch (IOException e)
          {
            // Don't care
          }
        }
        if (fos != null)
        {
          try
          {
            fos.close();
          }
          catch (IOException e)
          {
            // Don't care
          }
        }
      }
    }

    return FileUtility.SUCCESS;
  }

  /**
   * Moves files or directories to a specified location.
   *
   * @return Return Codes:<br>
   *         FileUtility.SUCCESS<br>
   *         FileUtility.FILE_MOVE_ERROR<br>
   *         FileUtility.INVALID_COPY_LOCATION<br>
   *         FileUtility.DIRECTORY_CREATION_ERROR<br>
   *         FileUtility.DIRECTORY_DELETE_ERROR<br>
   *         FileUtility.FILE_DELETE_ERROR<br>
   * @throws IOException
   */
  public static int moveFiles(File srcFile, File destDir) throws IOException
  {
    return moveFiles(srcFile, destDir, null);
  }

  /**
   * Moves files or directories to a specified location. If destFilename is not
   * null, then srcFile will be renamed to destFilename during move.
   *
   * @return Return Codes:<br>
   *         FileUtility.SUCCESS<br>
   *         FileUtility.FILE_MOVE_ERROR<br>
   *         FileUtility.INVALID_COPY_LOCATION<br>
   *         FileUtility.DIRECTORY_CREATION_ERROR<br>
   *         FileUtility.DIRECTORY_DELETE_ERROR<br>
   *         FileUtility.FILE_DELETE_ERROR<br>
   * @throws IOException
   */
  public static int moveFiles(File srcFile, File destDir, String destFilename) throws IOException
  {
    if (srcFile.isDirectory() && !destDir.exists())
    {
      if (!destDir.mkdirs())
      {
        return FileUtility.DIRECTORY_CREATION_ERROR;
      }
    }

    /*
     * If destination is not a directory or the destination path is a sub-directory
     * of the source path
     */
    if (!destDir.isDirectory()
        || (srcFile.isDirectory() && destDir.getCanonicalPath().contains(srcFile.getCanonicalPath())))
    {
      return FileUtility.INVALID_COPY_LOCATION;
    }

    if (!srcFile.isDirectory())
    {
      destDir = new File(destDir.getCanonicalPath() + FILE_SEPARATOR + srcFile.getName());
    }

    if (destFilename != null)
    {
      StringBuilder newFilename = new StringBuilder(destDir.getParent());
      newFilename.append(FILE_SEPARATOR);
      newFilename.append(destFilename);
      destDir = new File(newFilename.toString());
    }

    if (copyFiles(srcFile, destDir, false) == FileUtility.SUCCESS)
    {
      if (srcFile.isDirectory())
      {
        if (!deleteDirectory(srcFile))
        {
          return FileUtility.DIRECTORY_DELETE_ERROR;
        }
        return FileUtility.SUCCESS;
      }
      else
      {
        if (!srcFile.delete())
        {
          return FileUtility.FILE_DELETE_ERROR;
        }
        return FileUtility.SUCCESS;
      }
    }

    return FileUtility.FILE_MOVE_ERROR;
  }

  /**
   * Reads a file in as a String.
   *
   * @return Contents of file
   * @throws IOException
   */
  public static String readFileAsString(String filePath) throws IOException
  {
    StringBuilder contents = new StringBuilder();
    BufferedReader fileIn = null;
    try
    {
      File file = new File(filePath);
      fileIn = new BufferedReader(new FileReader(file));
      String line;
      while ((line = fileIn.readLine()) != null)
      {
        contents.append(line);
        contents.append(LINE_SEPARATOR);
      }

      return contents.toString();
    }
    finally
    {
      if (fileIn != null)
      {
        try
        {
          fileIn.close();
        }
        catch (IOException e)
        {
          // Don't care
        }
      }
    }
  }

  /**
   * Writes the specified string to a file. If append is false, string will
   * replace contents of file. If append is true, a newline will also be added to
   * contents so subsequent appends will begin on a new line.
   *
   * @throws IOException
   */
  public static void writeStringToFile(String contents, File file, boolean append) throws IOException
  {
    if (append)
    {
      contents += LINE_SEPARATOR;
    }

    Writer output = null;
    try
    {
      output = new BufferedWriter(new FileWriter(file, append));
      output.write(contents);
    }
    finally
    {
      if (output != null)
      {
        try
        {
          output.close();
        }
        catch (IOException e)
        {
          // Don't care
        }
      }
    }
  }

  /**
   * Delete all files from specified directory.
   *
   * @return Number of files that could not be deleted
   */
  public static int deleteFilesFromDirectory(File directory)
  {
    int filesNotDeleted = 0;

    File[] list = directory.listFiles(new FileOnlyListFilter());

    if (list.length == 0)
    {
      return 0;
    }
    for (File aList : list)
    {
      try
      {
        if (!aList.delete())
        {
          filesNotDeleted++;
        }
      }
      catch (SecurityException se)
      {
        System.out.println("Error deleting file " + aList.getName());
      }
    }
    return filesNotDeleted;
  }

  /**
   * Deletes all files in the specified directory that have the specified
   * extension.
   *
   * @return Number of files that could not be deleted
   */
  public static int deleteFilesWithExtension(File dirFile, String extension)
  {
    int filesNotDeleted = 0;
    FileExtensionListFilter filter = new FileExtensionListFilter(extension);

    File[] list = dirFile.listFiles(filter);

    if (list != null)
    {
      for (File aList : list)
      {
        try
        {
          if (!aList.delete())
          {
            filesNotDeleted++;
          }
        }
        catch (SecurityException se)
        {
          System.out.println("Error deleting file " + aList.getName());
        }
      }
    }
    return filesNotDeleted;
  }

  /**
   * Delete directory and all sub-directories and files.
   *
   * @return true only if directory is successfully deleted
   */
  public static boolean deleteDirectory(File dir)
  {
    if (dir.isDirectory())
    {
      String[] children = dir.list();
      for (String aChildren : children)
      {
        if (!deleteDirectory(new File(dir, aChildren)))
        {
          return false;
        }
      }
    }
    return dir.delete();
  }

  /**
   * Find the file with the latest modified date in a specified directory. Returns
   * null if there are no files in the directory.
   *
   * @return File object for file with latest lastModified() date
   */
  public static File findNewestFileInDirectory(String dirPath)
  {
    File dir = new File(dirPath);
    if (!dir.isDirectory())
    {
      dir = dir.getParentFile();
    }

    File[] fileList = dir.listFiles(new FileOnlyListFilter());
    if (fileList.length == 0)
    {
      return null;
    }
    File newestFile = fileList[0];
    for (int i = 1; i < fileList.length; i++)
    {
      File nextFile = fileList[i];
      if (nextFile.lastModified() > newestFile.lastModified())
      {
        newestFile = nextFile;
      }
    }

    return newestFile;
  }

  /**
   * Get a list of files in the specified directory. If the path specified is a
   * file and not a directory, the parent directory will be searched.
   *
   * @return Array of files in the directory
   */
  public static File[] getListOfFilesInDirectory(String dirPath, String filenameStartsWith)
  {
    File dir = new File(dirPath);
    if (!dir.isDirectory())
    {
      dir = dir.getParentFile();
    }

    if (filenameStartsWith != null)
    {
      return dir.listFiles(new FileStartsWithListFilter(filenameStartsWith));
    }
    else
    {
      return dir.listFiles(new FileOnlyListFilter());
    }
  }

  /**
   * Get the filename for the provided File. If includeExtension is false, the
   * file extension will be removed from the returned filename.
   *
   * @return filename
   */
  public static String getFilename(File file, boolean includeExtension)
  {
    String filename = file.getName();

    if (!includeExtension && filename.lastIndexOf(".") != -1)
    {
      filename = filename.substring(0, filename.lastIndexOf("."));
    }

    return filename;
  }

  /**
   * Get the extension for the provided file. If there is no file extension, an
   * empty String is returned. If includePeriod is true, it will return
   * .{extension} instead of {extension}
   * 
   * @param file
   * @param includePeriod
   * @return
   */
  public static String getFileExtension(File file, boolean includePeriod)
  {
    String filename = file.getName();
    String extension = "";

    if (filename.lastIndexOf(".") != -1)
    {
      int startIdx = (includePeriod ? filename.lastIndexOf(".") : filename.lastIndexOf(".") + 1);
      extension = filename.substring(startIdx, filename.length());
    }

    return extension;
  }
  
  /**
   * Returns a sanitized filename string. Specifically, this replaces every character that is not a 
   * letter, number, or underscore with an underscore.
   * 
   * @param filenameString
   * @return
   */
  public static String sanitizeStringForFilename(String filenameString)
  {
    return filenameString.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
  }

  /**
   * Test driver
   */
  public static void main(String[] args)
  {

  }
}