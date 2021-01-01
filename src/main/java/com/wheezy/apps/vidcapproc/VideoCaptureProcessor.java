package com.wheezy.apps.vidcapproc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

import com.wheezy.apps.vidcapproc.controller.GameController;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.utils.props.CaptureProcessorProperties;
import com.wheezy.apps.vidcapproc.utils.props.CaptureProcessorPropertiesUtility;
import com.wheezy.apps.vidcapproc.view.gui.ConfigDialog;
import com.wheezy.apps.vidcapproc.view.gui.GameManager;
import com.wheezy.apps.vidcapproc.view.gui.component.FocusTextField;
import com.wheezy.apps.vidcapproc.view.gui.component.GameSelectorButton;
import com.wheezy.utils.file.FileUtility;

/* TODO FEATURE LIST
 *
 * Check for file handle from capture before attempting move
 *
 * Menu selection for capture filename (date parsing) 
 * 
 * Place keepers/clips into game directories
 * 
 * Use native file copy for faster file movement
 * 
 * Put timestamp tags in mp4 metadata like GoPro
 *
 * Show selected game icon when game selector is collapsed
 *
 * Allow hotkeys (key bindings) for marking clips, tagging keepers to be re-mapped.
 *
 * Indexes for game manager to allow faster re-ordering
 * 
 * Send to top and send to bottom for game manager re-ordering
 *
 * Click and drag re-ordering for game manager
 * 
 * Game Mode support for game labels
 * 
 * Right-click menu for buttons in game selector (edit, game mode, hide, etc.)
 * http://stackoverflow.com/questions/766956/how-do-i-create-a-right-click-context-menu-in-java-swing
 * 
 * Expand Clip Manager:
 *  Different file formats? Black Magic, etc.
 *  Integrate video player for clip review?
 *      http://sourceforge.net/projects/videonotetaker/
 *      http://sourceforge.net/projects/dvrplus/
 *      http://sourceforge.net/projects/jveditor/
 * 
 * Add built in google image search to help users find icons for games
 * http://java.sun.com/developer/technicalArticles/J2SE/Desktop/javase6/desktop_api/
 * 
 * System Tray Icon
 * http://java.sun.com/developer/technicalArticles/J2SE/Desktop/javase6/systemtray/
 */

/* TODO BUG LIST
 * 
 * Don't set path when you click 'Cancel' on file chooser
 * Validate application position visible on screen when starting
 * Check filename label for valid characters
 */

public class VideoCaptureProcessor implements Observer
{
  // Future public static boolean isTranslucent = false;
  // Future public static float windowTranslucency = .90f;
  private static File fileChooserLastPath = null;

  public static final String LOG_FILE_NAME = "logs/CapProc.log";
  private static final String CLIPS_FILE_EXTENSION = ".clips";
  private static final String CLIPS_DIRECTORY = "clips/";
  public static final Image WINDOW_ICON = Toolkit.getDefaultToolkit().getImage("images/CapUtilAppIcon.png");
  public static int GAME_ICON_WIDTH = 64;
  public static int GAME_ICON_HEIGHT = 64;

  private static final Dimension CONTROL_BUTTON_PREFERRED_SIZE = new Dimension(150, 50);
  private static final Dimension MARK_BUTTON_PREFERRED_SIZE = new Dimension(75, 25);
  private static final Dimension TEXT_FIELD_PREFERRED_SIZE = new Dimension(100, 25);
  private static final Color KEEPERS_COLOR = new Color(53, 204, 77);
  private static final Color CLIPS_COLOR = new Color(21, 87, 255);
  private static final Color CLEANUP_COLOR = new Color(204, 16, 0);
  private static final long BUTTON_COLOR_TIMER_MILLIS = 250;
  private static final int DEFAULT_WINDOW_WIDTH = 480;
  private static final int DEFAULT_WINDOW_HEIGHT = 205;
  private static final int GAME_SELECTOR_HEIGHT_PER_ROW = 75;
  private static final int ICONS_PER_ROW = 6;
  private static final int DEFAULT_STATUS_BAR_HEIGHT = 16;
  private static final Rectangle BUTTONS_ONLY_WINDOW_SIZE = new Rectangle(100, 100, DEFAULT_WINDOW_WIDTH,
      DEFAULT_WINDOW_HEIGHT);

  private static final GameController controller;

  private enum ActionTypes
  {
    SAVE_KEEPER, SAVE_CLIP, MARK_CLIP, CLEANUP
  }

  private enum FunctionButtons
  {
    KEEPERS_BUTTON, CLIPS_BUTTON, CLEANUP_BUTTON, MARK_BUTTON
  }

  static
  {
    // Create log file if it doesn't exist
    File logFile = new File(LOG_FILE_NAME);
    try
    {
      logFile.getParentFile().mkdirs();
      logFile.createNewFile();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    controller = GameController.getInstance();
  }

  private static CaptureProcessorPropertiesUtility propertiesInstance;
  private static boolean alwaysOnTop;

  private JFrame captureProcessorFrame;
  private JCheckBoxMenuItem alwaysOnTopMenuCheckBox;
  private JCheckBoxMenuItem gameSelectorCheckBox;
  private JButton keepersButton;
  private JButton clipsButton;
  private JButton cleanupButton;
  private JButton markClipButton;
  private FocusTextField labelField;
  private JPanel gameSelectorPanel;
  private String selectedGameTitle;
  private final LinkedHashMap<String, GameSelectorButton> gameButtonMap = new LinkedHashMap<String, GameSelectorButton>();
  private JLabel gameNameLabel;
  private JProgressBar progressBar;
  private JLabel progressLabel;
  private boolean threadRunning = false;
  private boolean working = false;
  private static final Logger logger = Logger.getLogger(VideoCaptureProcessor.class.getName());

  private VideoCaptureProcessor()
  {
    FileHandler handler;
    try
    {
      handler = new FileHandler(LOG_FILE_NAME, true);
      logger.addHandler(handler);
    }
    catch (Exception e)
    {
      logger.log(Level.SEVERE, "Unable to create log file", e);
    }

    controller.registerModelObserver(this);

    initializeFrame();
    populateGameSelectorPanel();

    loadComponentProperties();

    this.captureProcessorFrame.setVisible(true);
  }

  private void initializeFrame()
  {
    captureProcessorFrame = new JFrame();
    captureProcessorFrame.setResizable(false);
    // Future this.captureProcessorFrame.setUndecorated(true); //Required for
    // Translucency
    // Future this.captureProcessorFrame.setOpacity(windowTranslucency);
    captureProcessorFrame.setIconImage(WINDOW_ICON);
    captureProcessorFrame.setTitle("Wheezy's Capture Utility");
    captureProcessorFrame.setBounds(BUTTONS_ONLY_WINDOW_SIZE);
    captureProcessorFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    captureProcessorFrame.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        if (threadRunning)
        {
          Object[] options = { "Close Anyway", "Cancel" };
          if (JOptionPane.showOptionDialog(captureProcessorFrame,
              "A thread is running in the background. " + "There is most likely a file move in progress.\n"
                  + "Please check the status bar at the bottom of the " + "application window for the\n"
                  + "progress of actions being performed and attempt to " + "close the application\n"
                  + "after the action has completed.",
              "Thread Running", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
              options[1]) == JOptionPane.NO_OPTION)
          {
            return;
          }
        }

        propertiesInstance.setProperty(CaptureProcessorProperties.FRAME_LOCATION_X_PROPERTY.getName(),
            Double.toString(captureProcessorFrame.getLocationOnScreen().getX()));
        propertiesInstance.setProperty(CaptureProcessorProperties.FRAME_LOCATION_Y_PROPERTY.getName(),
            Double.toString(captureProcessorFrame.getLocationOnScreen().getY()));
        try
        {
          propertiesInstance.storeProperties();
        }
        catch (IOException e1)
        {
          // Failing to save a property isn't the end of the world.
          logger.log(Level.WARNING, "Property Store Error", e1);
        }
        System.exit(0);
      }
    });

    try
    {
      propertiesInstance = CaptureProcessorPropertiesUtility.getInstance();
      String pathProp = propertiesInstance
          .getProperty(CaptureProcessorProperties.FILE_CHOOSER_LOCATION_PROPERTY.getName());
      if (pathProp != null)
      {
        fileChooserLastPath = new File(pathProp);
      }
    }
    catch (IOException e3)
    {
      JOptionPane.showMessageDialog(captureProcessorFrame,
          "An error was encountered while reading from the properties file.", "Error Reading Properties",
          JOptionPane.ERROR_MESSAGE);
      logger.log(Level.SEVERE, "Property Read Error", e3);
    }

    /*
     * Menu Bar
     */
    JMenuBar menuBar = new JMenuBar();
    captureProcessorFrame.setJMenuBar(menuBar);

    // Settings Menu
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');
    menuBar.add(fileMenu);

    alwaysOnTopMenuCheckBox = new JCheckBoxMenuItem("Always On Top");
    alwaysOnTopMenuCheckBox.setMnemonic('A');
    alwaysOnTopMenuCheckBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
    alwaysOnTopMenuCheckBox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        alwaysOnTop = VideoCaptureProcessor.this.alwaysOnTopMenuCheckBox.getState();

        VideoCaptureProcessor.this.captureProcessorFrame.setAlwaysOnTop(alwaysOnTop);

        propertiesInstance.setProperty(CaptureProcessorProperties.ALWAYS_ON_TOP_PROPERTY.getName(),
            Boolean.toString(alwaysOnTop));
        try
        {
          propertiesInstance.storeProperties();
        }
        catch (IOException ioe)
        {
          // Failing to save a property isn't the end of the world.
          logger.log(Level.WARNING, "Property Store Error", ioe);
        }
      }
    });
    fileMenu.add(this.alwaysOnTopMenuCheckBox);

    JMenuItem setFoldersMenuItem = new JMenuItem("Set Folders...");
    setFoldersMenuItem.setMnemonic('S');
    setFoldersMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ConfigDialog configDialog = new ConfigDialog();
        configDialog.setLocationRelativeTo(captureProcessorFrame);
        configDialog.setVisible(true);
      }
    });
    fileMenu.add(setFoldersMenuItem);

    JMenu viewMenu = new JMenu("Games");
    viewMenu.setMnemonic('G');
    menuBar.add(viewMenu);

    gameSelectorCheckBox = new JCheckBoxMenuItem("Show Game Selector");
    gameSelectorCheckBox.setMnemonic('S');
    gameSelectorCheckBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
    gameSelectorCheckBox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        VideoCaptureProcessor.this.showGameSelector(VideoCaptureProcessor.this.gameSelectorCheckBox.getState());
      }
    });
    viewMenu.add(gameSelectorCheckBox);

    JMenuItem manageGamesMenuItem = new JMenuItem("Manage Games...");
    manageGamesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
    viewMenu.add(manageGamesMenuItem);
    manageGamesMenuItem.setMnemonic('M');
    manageGamesMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        GameManager gameManager = new GameManager();
        gameManager.setVisible(true);
      }
    });

    captureProcessorFrame.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel mainPanel = new JPanel();
    FlowLayout flowLayout = (FlowLayout) mainPanel.getLayout();
    flowLayout.setVgap(0);
    flowLayout.setHgap(0);
    captureProcessorFrame.getContentPane().add(mainPanel, BorderLayout.NORTH);
    JPanel controlButtonPanel = new JPanel();
    mainPanel.add(controlButtonPanel);
    GridBagLayout gbl_controlButtonPanel = new GridBagLayout();
    gbl_controlButtonPanel.columnWidths = new int[] { 150, 150, 150, 0 };
    gbl_controlButtonPanel.rowHeights = new int[] { 50, 23, 0 };
    gbl_controlButtonPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
    gbl_controlButtonPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
    controlButtonPanel.setLayout(gbl_controlButtonPanel);

    // Keepers Button
    keepersButton = new JButton("KEEPERS");
    GridBagConstraints gbc_keepersButton = new GridBagConstraints();
    gbc_keepersButton.insets = new Insets(5, 5, 2, 2);
    gbc_keepersButton.gridx = 0;
    gbc_keepersButton.gridy = 0;
    controlButtonPanel.add(keepersButton, gbc_keepersButton);
    keepersButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        saveNewestFile(ActionTypes.SAVE_KEEPER);
      }
    });

    keepersButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"), "saveKeeper");
    keepersButton.getActionMap().put("saveKeeper", new SaveKeeperAction());
    keepersButton.setFont(new Font("Arial", Font.BOLD, 16));
    keepersButton.setToolTipText("Move latest video to keepers folder (F1)");
    keepersButton.setPreferredSize(CONTROL_BUTTON_PREFERRED_SIZE);
    keepersButton.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null),
        new MatteBorder(4, 4, 4, 4, KEEPERS_COLOR)));

    // Clips Button
    clipsButton = new JButton("CLIPS");
    GridBagConstraints gbc_clipsButton = new GridBagConstraints();
    gbc_clipsButton.insets = new Insets(5, 2, 2, 2);
    gbc_clipsButton.gridx = 1;
    gbc_clipsButton.gridy = 0;
    controlButtonPanel.add(clipsButton, gbc_clipsButton);
    clipsButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        saveNewestFile(ActionTypes.SAVE_CLIP);
      }
    });
    clipsButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F2"), "saveClip");
    clipsButton.getActionMap().put("saveClip", new SaveClipAction());
    clipsButton.setFont(new Font("Arial", Font.BOLD, 16));
    clipsButton.setToolTipText("Move latest video to clips folder (F2)");
    clipsButton.setPreferredSize(CONTROL_BUTTON_PREFERRED_SIZE);
    clipsButton.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null),
        new MatteBorder(4, 4, 4, 4, CLIPS_COLOR)));

    // Cleanup Button
    cleanupButton = new JButton("CLEANUP");
    GridBagConstraints gbc_cleanupButton = new GridBagConstraints();
    gbc_cleanupButton.insets = new Insets(5, 2, 2, 5);
    gbc_cleanupButton.gridx = 2;
    gbc_cleanupButton.gridy = 0;
    controlButtonPanel.add(cleanupButton, gbc_cleanupButton);
    cleanupButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (JOptionPane.showConfirmDialog(captureProcessorFrame,
            "Are you sure you want to delete all " + "files from the capture directory?\n"
                + propertiesInstance.getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName()),
            "Confirm Cleanup", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION)
        {
          new FileDeleteThread().start();
          new ButtonSwingWorker(FunctionButtons.CLEANUP_BUTTON);
        }
      }
    });
    cleanupButton.setFont(new Font("Arial", Font.BOLD, 16));
    cleanupButton.setToolTipText("Delete files that have not been copied to keepers or clips");
    cleanupButton.setPreferredSize(CONTROL_BUTTON_PREFERRED_SIZE);
    cleanupButton.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null),
        new MatteBorder(4, 4, 4, 4, CLEANUP_COLOR)));

    markClipButton = new JButton("Mark Clip");
    markClipButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        markClip();
      }
    });
    markClipButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F3"), "markClip");
    markClipButton.getActionMap().put("markClip", new MarkClipAction());
    markClipButton.setToolTipText("Mark the timestamp for a clip (F3)");
    markClipButton.setPreferredSize(MARK_BUTTON_PREFERRED_SIZE);
    markClipButton.setFont(new Font("Arial", Font.BOLD, 12));
    markClipButton.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null),
        new MatteBorder(2, 2, 2, 2, CLIPS_COLOR)));
    GridBagConstraints gbc_markClipButton = new GridBagConstraints();
    gbc_markClipButton.insets = new Insets(5, 2, 5, 2);
    gbc_markClipButton.gridx = 1;
    gbc_markClipButton.gridy = 1;
    controlButtonPanel.add(markClipButton, gbc_markClipButton);

    // TODO Create custom text field with Input Hint
    labelField = new FocusTextField();
    labelField.setToolTipText("Add a label to the clip or file");
    labelField.setPreferredSize(TEXT_FIELD_PREFERRED_SIZE);
    GridBagConstraints gbc_labelField = new GridBagConstraints();
    gbc_labelField.insets = new Insets(5, 2, 8, 2);
    gbc_labelField.gridx = 1;
    gbc_labelField.gridy = 2;
    controlButtonPanel.add(labelField, gbc_labelField);

    JPanel statusBarPanel = new JPanel();
    statusBarPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
    statusBarPanel.setPreferredSize(new Dimension(DEFAULT_WINDOW_WIDTH, DEFAULT_STATUS_BAR_HEIGHT));
    statusBarPanel.setMaximumSize(new Dimension(statusBarPanel.getMaximumSize().width, DEFAULT_STATUS_BAR_HEIGHT));
    captureProcessorFrame.getContentPane().add(statusBarPanel, BorderLayout.SOUTH);
    statusBarPanel.setLayout(new BorderLayout(0, 0));

    JPanel leftStatusPanel = new JPanel();
    FlowLayout fl_leftStatusPanel = (FlowLayout) leftStatusPanel.getLayout();
    fl_leftStatusPanel.setVgap(0);
    statusBarPanel.add(leftStatusPanel, BorderLayout.WEST);

    JLabel selectedGameLabel = new JLabel("Selected Game:");
    selectedGameLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
    leftStatusPanel.add(selectedGameLabel);

    gameNameLabel = new JLabel("None");
    gameNameLabel.setFont(new Font("Tahoma", Font.BOLD, 10));
    leftStatusPanel.add(gameNameLabel);

    JPanel rightStatusPanel = new JPanel();
    FlowLayout fl_rightStatusPanel = (FlowLayout) rightStatusPanel.getLayout();
    fl_rightStatusPanel.setVgap(0);
    statusBarPanel.add(rightStatusPanel, BorderLayout.EAST);

    progressLabel = new JLabel("");
    progressLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
    rightStatusPanel.add(progressLabel);

    progressBar = new JProgressBar();
    progressBar.setEnabled(false);
    progressBar.setPreferredSize(new Dimension(100, 15));
    rightStatusPanel.add(progressBar);
  }

  private void saveNewestFile(ActionTypes at)
  {
    String locationProperty = CaptureProcessorProperties.CLIPS_LOCATION_PROPERTY.getName();
    FunctionButtons fb = FunctionButtons.CLIPS_BUTTON;

    switch (at)
    {
    case SAVE_KEEPER:
      locationProperty = CaptureProcessorProperties.KEEPERS_LOCATION_PROPERTY.getName();
      fb = FunctionButtons.KEEPERS_BUTTON;
      break;
    case SAVE_CLIP:
      locationProperty = CaptureProcessorProperties.CLIPS_LOCATION_PROPERTY.getName();
      fb = FunctionButtons.CLIPS_BUTTON;
      break;
    case CLEANUP:
      break;
    case MARK_CLIP:
      break;
    default:
      break;
    }

    File newFile = FileUtility.findNewestFileInDirectory(
        propertiesInstance.getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName()));

    if (newFile != null)
    {
      String filename = FileUtility.getFilename(newFile, false);
      new FileMoveThread(filename, new File(propertiesInstance.getProperty(locationProperty))).start();
    }
    else
    {
      JOptionPane.showMessageDialog(captureProcessorFrame, "There are no files in the capture directory!",
          "Capture Directory is Empty", JOptionPane.WARNING_MESSAGE);
    }

    new ButtonSwingWorker(fb);
  }

  private boolean keepFile(String oldFilename, File destDir, String newFilename)
  {
    File[] fileList = FileUtility.getListOfFilesInDirectory(
        propertiesInstance.getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName()), oldFilename);

    try
    {
      for (File srcFile : fileList)
      {
        int result = FileUtility.moveFiles(srcFile, destDir, newFilename + FileUtility.getFileExtension(srcFile, true));

        if (result != FileUtility.SUCCESS)
        {
          JOptionPane.showMessageDialog(
              captureProcessorFrame, "File move was unable to complete for the following reason: "
                  + FileUtility.ERROR_DESCRIPTIONS[result] + ".\n" + "Please try again.",
              "Problem Moving Files", JOptionPane.WARNING_MESSAGE);

          logger.warning("Error encountered while attempting to move file to " + destDir.getCanonicalPath() + " - "
              + FileUtility.ERROR_DESCRIPTIONS[result]);
          stopProgressBar("Move Failed");
          threadRunning = false;
          return true;
        }
      }

      File clipsFile = new File(CLIPS_DIRECTORY + oldFilename + CLIPS_FILE_EXTENSION);
      if (clipsFile.exists())
      {
        FileUtility.moveFiles(clipsFile, destDir, newFilename + CLIPS_FILE_EXTENSION);
      }
    }
    catch (IOException e)
    {
      JOptionPane.showMessageDialog(captureProcessorFrame,
          "An error was encountered while attempting to move files. " + "Check your video directories.",
          "Error Moving Files", JOptionPane.ERROR_MESSAGE);
      stopProgressBar("Move Failed");
      threadRunning = false;
      logger.log(Level.SEVERE, "Error Moving Files", e);
      return true;
    }
    return false;
  }

  private void markClip()
  {
    Date currentTime = new Date();

    File clipFile = FileUtility.findNewestFileInDirectory(
        propertiesInstance.getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName()));

    if (clipFile == null)
    {
      JOptionPane.showMessageDialog(captureProcessorFrame, "There are no files in the capture directory!",
          "Capture Directory is Empty", JOptionPane.WARNING_MESSAGE);
      return;
    }

    String filename = FileUtility.getFilename(clipFile, false);

    /*
     * TODO
     * Allow custom format Filename format: 2011_8_23_20_14_6.M2TS
     * 
     * Live Gamer Portable Filename format: (yyyyMMddHHmmss) 20110823201406.mp4
     * 
     * OBS Studio Filename format: yyyy-MM-dd-HH-mm-ss.mp4
     */
    /*
     * SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy_M_d_H_m_s",
     * Locale.ENGLISH);
     */
    SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.ENGLISH);

    Date fileDate;
    try
    {
      fileDate = filenameDateFormat.parse(filename);
    }
    catch (ParseException e)
    {
      JOptionPane.showMessageDialog(captureProcessorFrame,
          "Unable to parse date from capture file name. This clip cannot be marked.\n" + clipFile.getPath(),
          "Clip Parsing Error", JOptionPane.ERROR_MESSAGE);
      logger.log(Level.SEVERE, "Clip Date Parsing Error", e);
      return;
    }

    long timeDiff = currentTime.getTime() - fileDate.getTime();
    long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
    long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff)
        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeDiff));

    NumberFormat numFormat = new DecimalFormat("00");
    String clipTime = minutes + ":" + numFormat.format(seconds);
    String clipLabel = labelField.getText().isEmpty() ? "" : " (" + labelField.getText() + ") ";
    try
    {
      FileUtility.writeStringToFile(clipTime + clipLabel, new File(CLIPS_DIRECTORY + filename + CLIPS_FILE_EXTENSION),
          true);
      progressLabel.setText("Clip marked: " + clipTime);
    }
    catch (IOException e)
    {
      JOptionPane.showMessageDialog(captureProcessorFrame,
          "Unable to parse date from capture file name. This clip cannot be marked.", "Clip Parsing Error",
          JOptionPane.ERROR_MESSAGE);
      logger.log(Level.SEVERE, "Clip File Creation Error", e);
    }

    new ButtonSwingWorker(FunctionButtons.MARK_BUTTON);
  }

  private void resetLabelFieldAndGetFocus()
  {
    // Reset the text
    labelField.setText("");
    labelField.requestFocus();
  }

  private void loadComponentProperties()
  {
    // Always on top
    String onTop = propertiesInstance.getProperty(CaptureProcessorProperties.ALWAYS_ON_TOP_PROPERTY.getName());

    if (onTop == null)
    {
      alwaysOnTop = true;
      captureProcessorFrame.setAlwaysOnTop(true);
      alwaysOnTopMenuCheckBox.setSelected(true);
    }
    else
    {
      alwaysOnTop = Boolean.parseBoolean(onTop);
      captureProcessorFrame.setAlwaysOnTop(alwaysOnTop);
      alwaysOnTopMenuCheckBox.setSelected(alwaysOnTop);
    }

    // Game selector visible
    String showSelector = propertiesInstance
        .getProperty(CaptureProcessorProperties.GAME_SELECTOR_VISIBLE_PROPERTY.getName());

    if (showSelector == null)
    {
      showGameSelector(false);
      gameSelectorCheckBox.setSelected(false);
    }
    else
    {
      showGameSelector(Boolean.parseBoolean(showSelector));
      gameSelectorCheckBox.setSelected(Boolean.parseBoolean(showSelector));
    }

    // Selected game
    String selected = propertiesInstance.getProperty(CaptureProcessorProperties.SELECTED_GAME_PROPERTY.getName());

    if (selected == null || selected.equals("None"))
    {
      setSelectedGame(gameButtonMap.get("None").getGameTitle());
    }
    else
    {
      setSelectedGame(controller.getGame(selected).getGameTitle());
    }

    // Frame location
    String locX = propertiesInstance.getProperty(CaptureProcessorProperties.FRAME_LOCATION_X_PROPERTY.getName());
    String locY = propertiesInstance.getProperty(CaptureProcessorProperties.FRAME_LOCATION_Y_PROPERTY.getName());
    if (locX != null && locY != null)
    {
      int intX = (int) Double.parseDouble(locX);
      int intY = (int) Double.parseDouble(locY);
      captureProcessorFrame.setLocation(intX, intY);
    }
  }

  private void populateGameSelectorPanel()
  {
    gameSelectorPanel = new JPanel()
    {
      private static final long serialVersionUID = 1L;

      @Override
      public Dimension getPreferredSize()
      {
        return new Dimension(DEFAULT_WINDOW_WIDTH, (int) super.getPreferredSize().getHeight());
      }
    };

    FlowLayout flowLayout = (FlowLayout) gameSelectorPanel.getLayout();
    flowLayout.setVgap(1);

    gameButtonMap.clear();

    // Default button for no game selected
    Game clearGame = new Game();
    clearGame.setGameTitle("None");
    clearGame.setIconImageFilepath("images/clear.png");
    GameSelectorButton clearButton = new GameSelectorButton(clearGame);
    clearButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        Object source = e.getSource();
        if (source instanceof GameSelectorButton)
        {
          setSelectedGame(((GameSelectorButton) source).getGameTitle());
        }
      }
    });
    gameButtonMap.put(clearGame.getGameTitle(), clearButton);
    gameSelectorPanel.add(clearButton);

    LinkedHashSet<Game> games = controller.getGames();
    for (Game game : games)
    {
      if (game.isDisplayed())
      {
        GameSelectorButton btn = new GameSelectorButton(game);
        btn.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            Object source = e.getSource();
            if (source instanceof GameSelectorButton)
            {
              setSelectedGame(((GameSelectorButton) source).getGameTitle());
            }
          }
        });
        gameButtonMap.put(game.getGameTitle(), btn);
        gameSelectorPanel.add(btn);
      }
    }
  }

  private void showGameSelector(boolean show)
  {
    int numRows = (gameButtonMap.size() / (ICONS_PER_ROW));
    if (gameButtonMap.size() % (ICONS_PER_ROW) > 0)
    {
      numRows++;
    }

    int newHeight = DEFAULT_WINDOW_HEIGHT + (GAME_SELECTOR_HEIGHT_PER_ROW * numRows);

    Rectangle gameSelectorWindowSize = new Rectangle(100, 100, DEFAULT_WINDOW_WIDTH, newHeight);

    Rectangle newSize = (!show ? BUTTONS_ONLY_WINDOW_SIZE : gameSelectorWindowSize);

    Point frameLocation = captureProcessorFrame.getLocation();
    captureProcessorFrame.setBounds((int) frameLocation.getX(), (int) frameLocation.getY(), (int) newSize.getWidth(),
        (int) newSize.getHeight());

    if (show)
    {
      captureProcessorFrame.getContentPane().add(gameSelectorPanel, BorderLayout.CENTER);
    }
    else
    {
      captureProcessorFrame.getContentPane().remove(gameSelectorPanel);
    }

    propertiesInstance.setProperty(CaptureProcessorProperties.GAME_SELECTOR_VISIBLE_PROPERTY.getName(),
        Boolean.toString(show));
    try
    {
      propertiesInstance.storeProperties();
    }
    catch (IOException e)
    {
      // Failing to save a property isn't the end of the world.
      logger.log(Level.WARNING, "Property Store Error", e);
    }

    captureProcessorFrame.setLocation(frameLocation);
  }

  private void setSelectedGame(String gameTitle)
  {
    if (selectedGameTitle != null && gameButtonMap.get(selectedGameTitle) != null)
    {
      gameButtonMap.get(selectedGameTitle).unselected();
    }
    selectedGameTitle = gameTitle;
    if (selectedGameTitle != null && gameButtonMap.get(selectedGameTitle) != null)
    {
      gameButtonMap.get(selectedGameTitle).selected();
      gameNameLabel.setText(selectedGameTitle);
      propertiesInstance.setProperty(CaptureProcessorProperties.SELECTED_GAME_PROPERTY.getName(), selectedGameTitle);
      try
      {
        propertiesInstance.storeProperties();
      }
      catch (IOException e)
      {
        // Failing to save a property isn't the end of the world.
        logger.log(Level.WARNING, "Property Store Error", e);
      }
    }
    else
    {
      gameNameLabel.setText("None");
    }
  }

  public static boolean isAlwaysOnTop()
  {
    return alwaysOnTop;
  }

  public static File getFileChooserLastPath()
  {
    return fileChooserLastPath;
  }

  public static void setFileChooserLastPath(File path)
  {
    fileChooserLastPath = path;
    propertiesInstance.setProperty(CaptureProcessorProperties.FILE_CHOOSER_LOCATION_PROPERTY.getName(),
        fileChooserLastPath.getPath());
  }

  private void startProgressBar(String messageText)
  {
    progressLabel.setText(messageText);
    progressBar.setIndeterminate(true);
    progressBar.setEnabled(true);
  }

  private void stopProgressBar(String messageText)
  {
    progressLabel.setText(messageText);
    progressBar.setIndeterminate(false);
    progressBar.setEnabled(false);
  }

  private class ButtonSwingWorker
  {
    private final FunctionButtons selection;

    /**
     * SwingWorker thread to modify GUI during GUI ActionListeners.
     */
    public ButtonSwingWorker(FunctionButtons buttonSelection)
    {
      selection = buttonSelection;

      // Perform processing in background.
      @SuppressWarnings("rawtypes")
      SwingWorker workThread = new SwingWorker()
      {
        @Override
        protected Object doInBackground() throws Exception
        {
          if (!working)
          {
            working = true;
            Color origColor;
            switch (selection)
            {
            case KEEPERS_BUTTON:
              origColor = keepersButton.getBackground();
              keepersButton.setBackground(KEEPERS_COLOR);
              sleep();
              keepersButton.setBackground(origColor);
              break;
            case CLIPS_BUTTON:
              origColor = clipsButton.getBackground();
              clipsButton.setBackground(CLIPS_COLOR);
              sleep();
              clipsButton.setBackground(origColor);
              break;
            case CLEANUP_BUTTON:
              origColor = cleanupButton.getBackground();
              cleanupButton.setBackground(CLEANUP_COLOR);
              sleep();
              cleanupButton.setBackground(origColor);
              break;
            case MARK_BUTTON:
              origColor = markClipButton.getBackground();
              markClipButton.setBackground(CLIPS_COLOR);
              sleep();
              markClipButton.setBackground(origColor);
              break;
            }
            working = false;
          }
          return null;
        }

        private void sleep()
        {
          try
          {
            Thread.sleep(BUTTON_COLOR_TIMER_MILLIS);
          }
          catch (InterruptedException e1)
          {
            // Don't care
            logger.log(Level.WARNING, "Thread Sleep Error", e1);
          }
        }
      };

      workThread.execute();
    }
  }

  @Override
  public void update(Observable paramObservable, Object paramObject)
  {
    showGameSelector(false);
    populateGameSelectorPanel();
    if (gameSelectorCheckBox.getState())
    {
      showGameSelector(true);
    }

    if (paramObject instanceof String && ((String) paramObject).contains("DELETED") && selectedGameTitle != null
        && ((String) paramObject).contains(selectedGameTitle))
    {
      setSelectedGame(gameButtonMap.get("None").getGameTitle());
    }
    else
    {
      setSelectedGame(selectedGameTitle);
    }
  }

  private class SaveKeeperAction extends AbstractAction
  {
    private static final long serialVersionUID = -4772689411198277836L;

    @Override
    public void actionPerformed(ActionEvent e)
    {
      keepersButton.requestFocus();
      saveNewestFile(ActionTypes.SAVE_KEEPER);
    }
  }

  private class SaveClipAction extends AbstractAction
  {
    private static final long serialVersionUID = -9114434708569186487L;

    @Override
    public void actionPerformed(ActionEvent e)
    {
      clipsButton.requestFocus();
      saveNewestFile(ActionTypes.SAVE_CLIP);
    }
  }

  private class MarkClipAction extends AbstractAction
  {
    private static final long serialVersionUID = 7591236313979891645L;

    @Override
    public void actionPerformed(ActionEvent e)
    {
      markClipButton.requestFocus();
      markClip();
    }
  }

  public class FileMoveThread extends Thread
  {
    private final String oldFilename;
    private final File destDir;
    private String newFilename;

    public FileMoveThread(String filename, File destDir)
    {
      oldFilename = filename;
      newFilename = filename;
      String filenameLabel = "";

      if (selectedGameTitle != null)
      {
        Game game = controller.getGame(selectedGameTitle);
        if (game != null)
        {
          filenameLabel = game.getFilenameLabel();
        }

        newFilename = filenameLabel + "_" + FileUtility.sanitizeStringForFilename(labelField.getText()) + "_"
            + oldFilename;
      }
      this.destDir = destDir;
    }

    @Override
    public void run()
    {
      threadRunning = true;
      startProgressBar("Moving File...");

      if (keepFile(oldFilename, destDir, newFilename))
      {
        return;
      }
      stopProgressBar("Move Complete");
      threadRunning = false;
    }
  }

  public class FileDeleteThread extends Thread
  {
    public FileDeleteThread()
    {
    }

    @Override
    public void run()
    {
      threadRunning = true;
      startProgressBar("Cleaning Up Files...");

      File[] fileList = FileUtility.getListOfFilesInDirectory(CLIPS_DIRECTORY, null);
      if (fileList != null && fileList.length != 0)
      {
        Object[] options = { "Yes", "Cancel", "Save Clips" };
        int choice = JOptionPane.showOptionDialog(captureProcessorFrame,
            "Clips have been tagged for one or more files that will be deleted.\n"
                + "Are you sure you want to continue with the cleanup?",
            "Clips Exist", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);

        if (choice == JOptionPane.NO_OPTION)// Cancel
        {
          stopProgressBar("Cleanup Aborted");
          threadRunning = false;
          return;
        }
        else if (choice == JOptionPane.CANCEL_OPTION)// Save Clips
        {
          String filenamePrefix = "";
          if (selectedGameTitle != null)
          {
            Game game = controller.getGame(selectedGameTitle);
            if (game != null)
            {
              filenamePrefix = game.getFilenameLabel() + "_";
            }
          }

          for (File clip : fileList)
          {
            String filename = FileUtility.getFilename(clip, false);
            if (keepFile(filename,
                new File(propertiesInstance.getProperty(CaptureProcessorProperties.CLIPS_LOCATION_PROPERTY.getName())),
                filenamePrefix + filename))
            {
              return;
            }
          }
        }
      }

      int undeleted = FileUtility.deleteFilesFromDirectory(
          new File(propertiesInstance.getProperty(CaptureProcessorProperties.CAPTURE_LOCATION_PROPERTY.getName())));

      if (undeleted != 0)
      {
        JOptionPane.showMessageDialog(captureProcessorFrame,
            undeleted + " files could not be deleted from the capture directory.", "Files Not Deleted",
            JOptionPane.WARNING_MESSAGE);
      }

      FileUtility.deleteFilesWithExtension(new File(CLIPS_DIRECTORY), CLIPS_FILE_EXTENSION);

      stopProgressBar("Cleanup Complete");
      threadRunning = false;
    }
  }

  public static void main(String[] args)
  {
    /*
     * Future // Determine if the GraphicsDevice supports translucency
     * GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
     * GraphicsDevice gd = ge.getDefaultScreenDevice();
     * 
     * //If translucent windows aren't supported, set flag if
     * (gd.isWindowTranslucencySupported
     * (GraphicsDevice.WindowTranslucency.TRANSLUCENT)) { isTranslucent = true; }
     */

    if (args.length != 0 && args[0] != null && args[0].equalsIgnoreCase("debug"))
    {
      logger.setLevel(Level.ALL);
    }
    else
    {
      logger.setLevel(Level.SEVERE);
    }

    EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException
            | UnsupportedLookAndFeelException e)
        {
          logger.log(Level.SEVERE, "Exception", e);
        }
        new VideoCaptureProcessor();
      }
    });
  }
}