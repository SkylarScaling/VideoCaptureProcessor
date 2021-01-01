package com.wheezy.utils.file;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.wheezy.utils.file.filter.chooser.ImageOnlyChooserFilter;
import com.wheezy.utils.image.component.ImagePreviewPanel;

public class ImageFileChooser extends JFileChooser
{
  private static final long serialVersionUID = 1L;

  private static final Dimension IMAGE_FILE_CHOOSER_SIZE = new Dimension(550, 300);

  public ImageFileChooser(File startPath, String logName)
  {
    super(startPath);
    ImagePreviewPanel previewPanel = new ImagePreviewPanel(logName);
    this.setFileFilter(new ImageOnlyChooserFilter());
    this.setAccessory(previewPanel);
    this.addPropertyChangeListener(previewPanel);
    this.setPreferredSize(IMAGE_FILE_CHOOSER_SIZE);
  }

  public static void main(String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e)
    {
      e.printStackTrace();
    }

    ImageFileChooser ifc = new ImageFileChooser(new File("images"), "test.log");
    ifc.showOpenDialog(null);
  }
}
