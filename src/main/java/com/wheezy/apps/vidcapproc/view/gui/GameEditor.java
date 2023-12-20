package com.wheezy.apps.vidcapproc.view.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import com.wheezy.apps.vidcapproc.VideoCaptureProcessor;
import com.wheezy.apps.vidcapproc.controller.GameController;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.utils.file.FileUtility;
import com.wheezy.utils.file.ImageFileChooser;
import com.wheezy.utils.image.ImageUtility;

public class GameEditor extends JDialog
{
  private static final long serialVersionUID = 1L;

  private static final Dimension LABEL_DIMENSION = new Dimension(100, 20);

  private static final Logger logger = Logger.getLogger(GameEditor.class.getName());

  private final JPanel contentPanel = new JPanel();
  private JTextField gameTitleField;
  private JTextField filenameLabelField;
  private JLabel iconLabel;
  private File selectedImage = new File("images/default.jpg");
  private final Game game;

  private static final GameController controller;

  static
  {
    controller = GameController.getInstance();
  }

  public GameEditor(Game game)
  {
    this.game = game;

    this.setTitle("Add New Game");
    this.setIconImage(VideoCaptureProcessor.WINDOW_ICON);
    this.setBounds(100, 100, 450, 300);

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

    initialize();
    if (game != null)
    {
      populateGame();
    }

    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    this.pack();
    this.setResizable(false);
    this.setModal(true);
    this.setAlwaysOnTop(VideoCaptureProcessor.isAlwaysOnTop());
  }

  private void initialize()
  {
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    GridBagLayout gbl_contentPanel = new GridBagLayout();
    gbl_contentPanel.columnWidths = new int[] { 281, 0 };
    gbl_contentPanel.rowHeights = new int[] { 33, 33, 33, 0 };
    gbl_contentPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
    gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
    contentPanel.setLayout(gbl_contentPanel);

    JPanel titlePanel = new JPanel();
    FlowLayout flowLayout = (FlowLayout) titlePanel.getLayout();
    flowLayout.setAlignment(FlowLayout.LEFT);
    GridBagConstraints gbc_titlePanel = new GridBagConstraints();
    gbc_titlePanel.fill = GridBagConstraints.BOTH;
    gbc_titlePanel.insets = new Insets(0, 0, 5, 0);
    gbc_titlePanel.gridx = 0;
    gbc_titlePanel.gridy = 0;
    contentPanel.add(titlePanel, gbc_titlePanel);

    JLabel gameTitleLabel = new JLabel("Game Title:");
    gameTitleLabel.setPreferredSize(LABEL_DIMENSION);
    titlePanel.add(gameTitleLabel);

    gameTitleField = new JTextField();
    titlePanel.add(gameTitleField);
    gameTitleField.setColumns(20);

    JPanel labelPanel = new JPanel();
    FlowLayout fl_labelPanel = (FlowLayout) labelPanel.getLayout();
    fl_labelPanel.setAlignment(FlowLayout.LEFT);
    GridBagConstraints gbc_labelPanel = new GridBagConstraints();
    gbc_labelPanel.fill = GridBagConstraints.BOTH;
    gbc_labelPanel.insets = new Insets(0, 0, 5, 0);
    gbc_labelPanel.gridx = 0;
    gbc_labelPanel.gridy = 1;
    contentPanel.add(labelPanel, gbc_labelPanel);

    JLabel filenameLabelLabel = new JLabel("Filename Label:");
    filenameLabelLabel.setPreferredSize(LABEL_DIMENSION);
    labelPanel.add(filenameLabelLabel);

    filenameLabelField = new JTextField();
    filenameLabelField.setColumns(20);
    labelPanel.add(filenameLabelField);

    JPanel iconPanel = new JPanel();
    FlowLayout flowLayout_1 = (FlowLayout) iconPanel.getLayout();
    flowLayout_1.setAlignment(FlowLayout.LEFT);
    GridBagConstraints gbc_iconPanel = new GridBagConstraints();
    gbc_iconPanel.fill = GridBagConstraints.BOTH;
    gbc_iconPanel.gridx = 0;
    gbc_iconPanel.gridy = 2;
    contentPanel.add(iconPanel, gbc_iconPanel);

    JLabel iconDetailLabel = new JLabel("Game Icon:");
    iconDetailLabel.setPreferredSize(LABEL_DIMENSION);
    iconPanel.add(iconDetailLabel);

    JPanel panel = new JPanel();
    iconPanel.add(panel);

    iconLabel = new JLabel(new ImageIcon("images/default.jpg"));
    panel.add(iconLabel);

    JButton browseButton = new JButton("Browse...");
    browseButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ImageFileChooser ifc = new ImageFileChooser(VideoCaptureProcessor.getFileChooserLastPath(),
            VideoCaptureProcessor.LOG_FILE_NAME);
        ifc.setDialogTitle("Select Image for Game Icon");
        ifc.showOpenDialog(GameEditor.this);
        selectedImage = ifc.getSelectedFile();
        if (selectedImage != null)
        {
          try
          {
            VideoCaptureProcessor.setFileChooserLastPath(selectedImage.getParentFile());

            iconLabel.setIcon(new ImageIcon(ImageUtility.resizeImageFromFile(selectedImage,
                VideoCaptureProcessor.GAME_ICON_WIDTH, VideoCaptureProcessor.GAME_ICON_HEIGHT, true)));
          }
          catch (IOException e1)
          {
            JOptionPane.showMessageDialog(GameEditor.this,
                "Error encountered while attempting to load image for game icon.", "Error Loading Image",
                JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "Image Load Error", e1);
          }
          GameEditor.this.pack();
        }
      }
    });
    panel.add(browseButton);
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        Game newGame = new Game();
        newGame.setGameTitle(gameTitleField.getText());
        newGame.setFilenameLabel(filenameLabelField.getText());
        try
        {
          String defaultPath = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
          String imagesDirPath = new StringBuffer(defaultPath).append(File.separator).append("images").toString();
          new File(imagesDirPath).mkdir();
          String newPath = new StringBuffer(imagesDirPath).append(FileUtility.FILE_SEPARATOR)
              .append(selectedImage.getName()).toString();
          File copiedImage = new File(newPath);

          // Make sure file isn't going to be copied over itself in /images
          if (!copiedImage.getCanonicalPath().equalsIgnoreCase(selectedImage.getCanonicalPath()))
          {
            newPath = ImageUtility.saveImageToPngFile(ImageUtility.resizeImageFromFile(selectedImage,
                VideoCaptureProcessor.GAME_ICON_WIDTH, VideoCaptureProcessor.GAME_ICON_HEIGHT, true), newPath);
          }

          newGame.setIconImageFilepath(newPath);
        }
        catch (IOException e1)
        {
          // Couldn't copy file to images directory, just use the selected path
          newGame.setIconImageFilepath(selectedImage.getPath());
          logger.log(Level.WARNING, "Image Path Save Error", e1);
        }

        newGame.setDisplayed(true);

        if (game != null)
        {
          newGame.setDisplayed(game.isDisplayed());
          controller.updateGame(game, newGame);
        }
        else
        {
          if (controller.getGame(newGame.getGameTitle()) == null)
          {
            controller.addGame(newGame);
          }
          else
          {
            JOptionPane.showMessageDialog(GameEditor.this, "Game Title must be unique. Please enter a new Game Title.",
                "Game Title Must Be Unique", JOptionPane.WARNING_MESSAGE);
            return;
          }
        }

        try
        {
          controller.saveGames();
          GameEditor.this.dispose();
        }
        catch (Exception e)
        {
          JOptionPane.showMessageDialog(GameEditor.this,
              "Error encountered while attempting to save game to the file system.", "Error Saving Game",
              JOptionPane.ERROR_MESSAGE);
          logger.log(Level.SEVERE, "Game Save Error", e);
        }
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
        GameEditor.this.dispose();
      }
    });
    cancelButton.setActionCommand("Cancel");
    buttonPane.add(cancelButton);
  }

  private void populateGame()
  {
    gameTitleField.setText(game.getGameTitle());
    filenameLabelField.setText(game.getFilenameLabel());
    iconLabel.setIcon(new ImageIcon(game.getIconImageFilepath()));
    selectedImage = new File(game.getIconImageFilepath());
  }

  public static void main(String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e)
    {
      logger.log(Level.SEVERE, "Exception", e);
    }

    new GameEditor(null).setVisible(true);
  }
}
