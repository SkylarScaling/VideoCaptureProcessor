package com.wheezy.apps.vidcapproc.view.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import com.wheezy.apps.vidcapproc.VideoCaptureProcessor;
import com.wheezy.apps.vidcapproc.controller.GameController;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.data.GameCollection;
import com.wheezy.apps.vidcapproc.view.gui.GameEditor;

public class GamePanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    private Game game;
    private ImageIcon icon;
    private String title;
    private String label;
    private TitledBorder displayedBorder =
            new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Displayed",
                    TitledBorder.LEADING, TitledBorder.TOP, null, null);

    private JPanel iconPanel;
    private JLabel iconLabel;
    private JPanel centerPanel;
    private JPanel titlePanel;
    private JLabel titleLabel;
    private JPanel filenameLabelPanel;
    private JLabel fileLabelLabel;
    private JPanel optionsButtonsPanel;

    private static final Color DISPLAYED_BORDER_TEXT_COLOR = new Color(0, 141, 0);
    private static final Color HIDDEN_BORDER_TEXT_COLOR = new Color(200, 0, 0);

    private static Dimension iconPanelPreferredSize = new Dimension(70, 70);
    private static Logger logger = Logger.getLogger(GamePanel.class.getName());
    private static GameController controller;

    static
    {
        controller = GameController.getInstance();
    }

    private JPanel panel;
    private JButton moveUpButton;
    private JButton moveDownButton;

    public GamePanel(Game game)
    {
        this.game = game;
        this.icon = new ImageIcon(game.getIconImageFilepath());
        this.title = game.getGameTitle();
        this.label = game.getFilenameLabel();

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

        setBorder(displayedBorder);
        GridBagConstraints gamePanelConstraints = new GridBagConstraints();
        gamePanelConstraints.fill = GridBagConstraints.BOTH;
        setLayout(new BorderLayout(10, 0));

        initialize();

        setDisplayMode(game.isDisplayed());
        setToggleListeners();
    }

    private void initialize()
    {
        iconPanel = new JPanel();
        iconPanel.setPreferredSize(iconPanelPreferredSize);
        add(iconPanel, BorderLayout.WEST);
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));

        iconLabel = new JLabel(icon);
        iconLabel.setToolTipText("Game Icon");
        iconPanel.add(iconLabel);

        centerPanel = new JPanel();
        add(centerPanel, BorderLayout.CENTER);
        centerPanel.setLayout(new GridLayout(0, 2, 20, 0));

        titlePanel = new JPanel();
        centerPanel.add(titlePanel);
        titlePanel.setLayout(new GridLayout(0, 1, 0, 0));

        titleLabel = new JLabel(title);
        titlePanel.add(titleLabel);
        titleLabel.setToolTipText("Game Title");
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        filenameLabelPanel = new JPanel();
        centerPanel.add(filenameLabelPanel);
        filenameLabelPanel.setLayout(new GridLayout(0, 1, 0, 0));

        fileLabelLabel = new JLabel(label + "_<filename>");
        filenameLabelPanel.add(fileLabelLabel);
        fileLabelLabel.setToolTipText("Filename Label Format");
        fileLabelLabel.setHorizontalAlignment(SwingConstants.LEFT);
        fileLabelLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        optionsButtonsPanel = new JPanel();
        add(optionsButtonsPanel, BorderLayout.EAST);
        optionsButtonsPanel.setLayout(new BoxLayout(optionsButtonsPanel, BoxLayout.Y_AXIS));

        moveUpButton = new BasicArrowButton(BasicArrowButton.NORTH);
        moveUpButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                controller.moveGame(game, GameCollection.MOVE_UP);
            }
        });
        moveUpButton.setBorder(new EtchedBorder());
        moveUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionsButtonsPanel.add(moveUpButton);

        panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setVgap(0);
        flowLayout.setHgap(0);
        optionsButtonsPanel.add(panel);

        JButton editButton = new JButton("Edit...");
        panel.add(editButton);

        JButton deleteButton = new JButton("Delete");
        panel.add(deleteButton);

        moveDownButton = new BasicArrowButton(BasicArrowButton.SOUTH);
        moveDownButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                controller.moveGame(game, GameCollection.MOVE_DOWN);
            }
        });
        moveDownButton.setBorder(new EtchedBorder());
        moveDownButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionsButtonsPanel.add(moveDownButton);
        deleteButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                controller.deleteGame(game);
                try
                {
                    controller.saveGames();
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(GamePanel.this,
                            "Error encountered while attempting to delete game from the file system.",
                            "Error Deleting Game",
                            JOptionPane.ERROR_MESSAGE);
                    logger.log(Level.SEVERE, "Game Save Error", ex);
                }
            }
        });
        editButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                GameEditor gameEditor = new GameEditor(game);
                gameEditor.setLocationRelativeTo(GamePanel.this);
                gameEditor.setVisible(true);
            }
        });
    }

    public void toggleDisplayed()
    {
        Game origGame = game;
        game.setDisplayed(!game.isDisplayed());
        controller.updateGame(origGame, game);
        try
        {
            controller.saveGames();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(GamePanel.this,
                    "Error encountered while attempting to save game to the file system.",
                    "Error Saving Game",
                    JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "Game Save Error", e);
        }
        setDisplayMode(game.isDisplayed());
    }

    private void setDisplayMode(boolean displayMode)
    {
        if (game.isDisplayed())
        {
            displayedBorder.setTitleColor(DISPLAYED_BORDER_TEXT_COLOR);
            displayedBorder.setTitle("Displayed");
        }
        else
        {
            displayedBorder.setTitleColor(HIDDEN_BORDER_TEXT_COLOR);
            displayedBorder.setTitle("Not Displayed");
        }
    }

    private void setToggleListeners()
    {
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                toggleDisplayed();
            }
        });

        JComponent[] toggleListenerComponents = {iconPanel, iconLabel,
                centerPanel, titlePanel, titleLabel, filenameLabelPanel,
                fileLabelLabel, optionsButtonsPanel};
        for (JComponent comp : toggleListenerComponents)
        {
            comp.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    toggleDisplayed();
                }
            });
        }
    }
}
