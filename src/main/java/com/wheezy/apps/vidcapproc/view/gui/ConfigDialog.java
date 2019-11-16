package com.wheezy.apps.vidcapproc.view.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import com.wheezy.apps.vidcapproc.VideoCaptureProcessor;
import com.wheezy.apps.vidcapproc.utils.props.CaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.props.CaptureProcessorPropertiesUtility;

public class ConfigDialog extends JDialog
{
  private static final long serialVersionUID = 1L;

  private static final Dimension CONFIG_LABEL_DIMENSION = new Dimension(100, 20);
  private static final int CONFIG_TEXT_FIELD_COLUMNS = 30;

  private static Logger logger = Logger.getLogger(ConfigDialog.class.getName());

  private JPanel contentPanel = new JPanel();
  private JTextField captureTextField;
  private JTextField keepersTextField;
  private JTextField clipsTextField;
  private CaptureProcessorPropertiesUtility propertiesInstance;

  public ConfigDialog()
  {
    setResizable(false);

    FileHandler handler;
    try
    {
      handler = new FileHandler(VideoCaptureProcessor.LOG_FILE_NAME, true);
      logger.addHandler(handler);
    }
    catch (Exception e)
    {
      logger.log(Level.SEVERE, "Unable to create log file", e);
    }

    try
    {
      propertiesInstance = CaptureProcessorPropertiesUtility.getInstance();
    }
    catch (IOException e2)
    {
      JOptionPane.showMessageDialog(ConfigDialog.this,
          "An error was encountered while reading from the properties file.",
          "Error Reading Properties", JOptionPane.ERROR_MESSAGE);
      logger.log(Level.SEVERE, "Property Read Error", e2);
    }

    String defaultFolder = propertiesInstance
        .getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName());
    if (defaultFolder != null)
    {
      VideoCaptureProcessor.setFileChooserLastPath(new File(defaultFolder));
    }
    setDefaultFolders();

    setIconImage(VideoCaptureProcessor.WINDOW_ICON);
    setTitle("Settings");
    setBounds(100, 100, 466, 200);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new GridLayout(0, 1, 0, 0));

    JPanel capturePanel = new JPanel();
    FlowLayout captureLayout = (FlowLayout) capturePanel.getLayout();
    captureLayout.setAlignment(FlowLayout.LEADING);
    contentPanel.add(capturePanel);

    JLabel captureFolderLabel = new JLabel("Capture Folder:");
    captureFolderLabel.setPreferredSize(CONFIG_LABEL_DIMENSION);
    capturePanel.add(captureFolderLabel);

    captureTextField = new JTextField();
    capturePanel.add(captureTextField);
    captureTextField.setColumns(CONFIG_TEXT_FIELD_COLUMNS);
    captureTextField.setText(propertiesInstance
        .getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName()));

    JButton captureBrowseButton = new JButton("Browse...");
    captureBrowseButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        JFileChooser captureFC = new JFileChooser(VideoCaptureProcessor.getFileChooserLastPath());
        captureFC.setDialogTitle("Select Capture Directory");
        captureFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        captureFC.showOpenDialog(ConfigDialog.this);
        File capFile = captureFC.getSelectedFile();
        if (capFile != null)
        {
          VideoCaptureProcessor.setFileChooserLastPath(capFile.getParentFile());
          String capPath = "";
          try
          {
            capPath = capFile.getCanonicalPath();
          }
          catch (IOException e1)
          {
            JOptionPane.showMessageDialog(ConfigDialog.this,
                "An error prevented the chosen directory from being saved.",
                "Error Setting Folder Location", JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "Directory Save Error", e1);
          }
          captureTextField.setText(capPath);
        }
      }
    });
    capturePanel.add(captureBrowseButton);

    JPanel keepersPanel = new JPanel();
    FlowLayout keepersLayout = (FlowLayout) keepersPanel.getLayout();
    keepersLayout.setAlignment(FlowLayout.LEADING);
    contentPanel.add(keepersPanel);

    JLabel keepersFolderLabel = new JLabel("Keepers Folder:");
    keepersFolderLabel.setPreferredSize(CONFIG_LABEL_DIMENSION);
    keepersPanel.add(keepersFolderLabel);

    keepersTextField = new JTextField();
    keepersPanel.add(keepersTextField);
    keepersTextField.setColumns(CONFIG_TEXT_FIELD_COLUMNS);
    keepersTextField.setText(propertiesInstance
        .getProperty(CaptureProcessorProperties.KEEPERS_LOCATION_PROPERTY.getName()));

    JButton keepersBrowseButton = new JButton("Browse...");
    keepersBrowseButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        JFileChooser keepersFC = new JFileChooser(VideoCaptureProcessor.getFileChooserLastPath());
        keepersFC.setDialogTitle("Select Directory for Saving Keepers");
        keepersFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        keepersFC.showOpenDialog(ConfigDialog.this);
        File keepFile = keepersFC.getSelectedFile();
        if (keepFile != null)
        {
          VideoCaptureProcessor.setFileChooserLastPath(keepFile.getParentFile());
          String keepPath = "";
          try
          {
            keepPath = keepFile.getCanonicalPath();
          }
          catch (IOException e1)
          {
            JOptionPane.showMessageDialog(ConfigDialog.this,
                "An error prevented the chosen directory from being saved.",
                "Error Setting Folder Location", JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "Directory Save Error", e1);
          }
          keepersTextField.setText(keepPath);
        }
      }
    });
    keepersPanel.add(keepersBrowseButton);

    JPanel clipsPanel = new JPanel();
    FlowLayout clipsLayout = (FlowLayout) clipsPanel.getLayout();
    clipsLayout.setAlignment(FlowLayout.LEADING);
    contentPanel.add(clipsPanel);

    JLabel clipsFolderLabel = new JLabel("Clips Folder:");
    clipsFolderLabel.setPreferredSize(CONFIG_LABEL_DIMENSION);
    clipsPanel.add(clipsFolderLabel);

    clipsTextField = new JTextField();
    clipsPanel.add(clipsTextField);
    clipsTextField.setColumns(CONFIG_TEXT_FIELD_COLUMNS);
    clipsTextField.setText(propertiesInstance
        .getProperty(CaptureProcessorProperties.CLIPS_LOCATION_PROPERTY.getName()));

    JButton clipsBrowseButton = new JButton("Browse...");
    clipsBrowseButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        JFileChooser clipsFC = new JFileChooser(VideoCaptureProcessor.getFileChooserLastPath());
        clipsFC.setDialogTitle("Select Directory for Saving Clips");
        clipsFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        clipsFC.showOpenDialog(ConfigDialog.this);
        File clipFile = clipsFC.getSelectedFile();
        if (clipFile != null)
        {
          VideoCaptureProcessor.setFileChooserLastPath(clipFile.getParentFile());
          String clipPath = "";
          try
          {
            clipPath = clipFile.getCanonicalPath();
          }
          catch (IOException e1)
          {
            JOptionPane.showMessageDialog(ConfigDialog.this,
                "An error prevented the chosen directory from being saved.",
                "Error Setting Folder Location", JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "Directory Save Error", e1);
          }
          clipsTextField.setText(clipPath);
        }
      }
    });
    clipsPanel.add(clipsBrowseButton);

    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
    getContentPane().add(buttonPane, BorderLayout.SOUTH);

    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        propertiesInstance.setProperty(
            CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName(),
            captureTextField.getText());

        propertiesInstance.setProperty(
            CaptureProcessorProperties.KEEPERS_LOCATION_PROPERTY.getName(),
            keepersTextField.getText());

        propertiesInstance.setProperty(
            CaptureProcessorProperties.CLIPS_LOCATION_PROPERTY.getName(), clipsTextField.getText());

        try
        {
          propertiesInstance.storeProperties();
        }
        catch (IOException e1)
        {
          JOptionPane.showMessageDialog(ConfigDialog.this,
              "An error prevented settings from being saved.", "Error Saving Settings",
              JOptionPane.ERROR_MESSAGE);
          logger.log(Level.SEVERE, "Settings Save Error", e1);
        }
        ConfigDialog.this.dispose();
      }
    });
    saveButton.setActionCommand("Save");
    buttonPane.add(saveButton);
    getRootPane().setDefaultButton(saveButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ConfigDialog.this.dispose();
      }
    });
    cancelButton.setActionCommand("Cancel");
    buttonPane.add(cancelButton);

    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    this.pack();
    this.setModal(true);
    this.setAlwaysOnTop(VideoCaptureProcessor.isAlwaysOnTop());
  }

  private void setDefaultFolders()
  {
    if (propertiesInstance.getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY
        .getName()) == null)
    {
      propertiesInstance.setProperty(
          CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName(), "capture");
    }

    if (propertiesInstance.getProperty(CaptureProcessorProperties.KEEPERS_LOCATION_PROPERTY
        .getName()) == null)
    {
      propertiesInstance.setProperty(
          CaptureProcessorProperties.KEEPERS_LOCATION_PROPERTY.getName(), "capture");
    }

    if (propertiesInstance
        .getProperty(CaptureProcessorProperties.CLIPS_LOCATION_PROPERTY.getName()) == null)
    {
      propertiesInstance.setProperty(CaptureProcessorProperties.CLIPS_LOCATION_PROPERTY.getName(),
          "capture");
    }
  }

  public static void main(String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException
        | IllegalAccessException e)
    {
      logger.log(Level.SEVERE, "Exception", e);
    }

    new ConfigDialog().setVisible(true);
  }
}
