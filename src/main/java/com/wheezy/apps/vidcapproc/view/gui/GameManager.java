package com.wheezy.apps.vidcapproc.view.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.wheezy.apps.vidcapproc.VideoCaptureProcessor;
import com.wheezy.apps.vidcapproc.controller.GameController;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.view.gui.component.GamePanel;

public class GameManager extends JDialog implements Observer
{
  private static final long serialVersionUID = 1L;

  private static final int MAX_SCROLLPANE_HEIGHT = 520;
  private static final int SCROLLBAR_UNIT_INCREMENT = 16;

  private static Logger logger = Logger.getLogger(GameManager.class.getName());
  private static GameController controller;

  static
  {
    controller = GameController.getInstance();
  }

  private JPanel gamesPanel;
  private JScrollPane scrollPane;

  public GameManager()
  {
    this.setTitle("Game Manager");
    this.setIconImage(VideoCaptureProcessor.WINDOW_ICON);

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

    controller.registerModelObserver(this);

    initialize();
    populateDialog();

    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    this.setResizable(false);
    this.setModal(true);
    this.pack();
    this.setLocationRelativeTo(null); // Center on screen
    this.setAlwaysOnTop(VideoCaptureProcessor.isAlwaysOnTop());
  }

  private void initialize()
  {
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[]
    { 0, 0 };
    gridBagLayout.rowHeights = new int[]
    { 0, 0, 0, 0 };
    gridBagLayout.columnWeights = new double[]
    { 1.0, Double.MIN_VALUE };
    gridBagLayout.rowWeights = new double[]
    { 0.0, 1.0, 1.0, Double.MIN_VALUE };
    getContentPane().setLayout(gridBagLayout);

    JPanel topPanel = new JPanel();
    GridBagConstraints gbc_topPanel = new GridBagConstraints();
    gbc_topPanel.fill = GridBagConstraints.BOTH;
    gbc_topPanel.insets = new Insets(0, 0, 5, 0);
    gbc_topPanel.gridx = 0;
    gbc_topPanel.gridy = 0;
    getContentPane().add(topPanel, gbc_topPanel);
    GridBagLayout gbl_topPanel = new GridBagLayout();
    gbl_topPanel.columnWidths = new int[]
    { 0, 0 };
    gbl_topPanel.rowHeights = new int[]
    { 0, 0, 0 };
    gbl_topPanel.columnWeights = new double[]
    { 1.0, Double.MIN_VALUE };
    gbl_topPanel.rowWeights = new double[]
    { 0.0, 0.0, Double.MIN_VALUE };
    topPanel.setLayout(gbl_topPanel);

    JButton addGameButton = new JButton("Add Game...");
    addGameButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        GameEditor gameEditor = new GameEditor(null);
        gameEditor.setLocationRelativeTo(GameManager.this);
        gameEditor.setVisible(true);
      }
    });
    GridBagConstraints gbc_addGameButton = new GridBagConstraints();
    gbc_addGameButton.insets = new Insets(0, 0, 5, 0);
    gbc_addGameButton.gridx = 0;
    gbc_addGameButton.gridy = 0;
    topPanel.add(addGameButton, gbc_addGameButton);
    addGameButton.setPreferredSize(new Dimension(100, 30));
    addGameButton.setFont(new Font("Tahoma", Font.BOLD, 11));

    JLabel infoLabel = new JLabel("(Click on Games to Toggle Display Setting)");
    infoLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
    GridBagConstraints gbc_infoLabel = new GridBagConstraints();
    gbc_infoLabel.gridx = 0;
    gbc_infoLabel.gridy = 1;
    topPanel.add(infoLabel, gbc_infoLabel);

    scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
    {
      private static final long serialVersionUID = 1L;

      @Override
      public Dimension getPreferredSize()
      {
        int width = (int) super.getPreferredSize().getWidth() + 10;
        int height = (int) super.getPreferredSize().getHeight();
        if ((int) super.getPreferredSize().getHeight() > MAX_SCROLLPANE_HEIGHT)
        {
          height = MAX_SCROLLPANE_HEIGHT;
          width += 5;
        }

        return new Dimension(width, height);
      }
    };
    scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLLBAR_UNIT_INCREMENT);
    GridBagConstraints gbc_scrollPane = new GridBagConstraints();
    gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
    gbc_scrollPane.fill = GridBagConstraints.BOTH;
    gbc_scrollPane.gridx = 0;
    gbc_scrollPane.gridy = 1;
    getContentPane().add(scrollPane, gbc_scrollPane);
  }

  private void populateDialog()
  {
    gamesPanel = new JPanel();
    scrollPane.setViewportView(gamesPanel);

    gamesPanel.setLayout(new GridLayout(0, 1, 0, 0));

    for (Game game : controller.getGames())
    {
      GamePanel newPanel = new GamePanel(game);
      // gamePanelSet.add(newPanel);
      gamesPanel.add(newPanel);
    }
  }

  @Override
  public void update(Observable paramObservable, Object paramObject)
  {
    int scrollPosition = scrollPane.getVerticalScrollBar().getValue();
    populateDialog();
    this.pack();
    scrollPane.getVerticalScrollBar().setValue(scrollPosition);
    this.setLocationRelativeTo(null); // Center on screen
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

    new GameManager().setVisible(true);
  }
}
