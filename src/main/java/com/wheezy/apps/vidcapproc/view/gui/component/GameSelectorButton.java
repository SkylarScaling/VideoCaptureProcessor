package com.wheezy.apps.vidcapproc.view.gui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.MatteBorder;

import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.view.gui.GameEditor;

public class GameSelectorButton extends JButton
{
  private static final long serialVersionUID = 1L;
  
  private static Dimension gameButtonDimension = new Dimension(74, 74);
  private static Color selectedBorderColor = Color.RED;
  private static MatteBorder selectedBorder = BorderFactory.createMatteBorder(4, 4, 4, 4, selectedBorderColor);

  private boolean selected = false;
  private Game game;
  private JPopupMenu rightClickMenu = new JPopupMenu();

  public GameSelectorButton(Game game)
  {
    super();

    this.game = game;

    JMenuItem editGameMenuItem = new JMenuItem("Edit...");
    editGameMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        GameEditor gameEditor = new GameEditor(GameSelectorButton.this.game);
        gameEditor.setLocationRelativeTo(GameSelectorButton.this);
        gameEditor.setVisible(true);
      }
    });
    editGameMenuItem.setFont(new Font("Arial", Font.BOLD, 11));
    rightClickMenu.add(editGameMenuItem);

    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (e.isPopupTrigger())
        {
          showPopupMenu(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        if (e.isPopupTrigger())
        {
          showPopupMenu(e);
        }
      }

      private void showPopupMenu(MouseEvent e)
      {
        rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    });
    this.setIcon(new ImageIcon(game.getIconImageFilepath()));
    this.game = game;
    this.setToolTipText(game.getGameTitle());
    this.setPreferredSize(gameButtonDimension);
  }

  public String getGameTitle()
  {
    return game.getGameTitle();
  }

  public void selected()
  {
    setBorder(selectedBorder);
    selected = true;
  }

  @Override
  public boolean isSelected()
  {
    return selected;
  }

  public void unselected()
  {
    setBorder(null);
    selected = false;
  }
}
