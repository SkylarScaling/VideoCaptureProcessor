package com.wheezy.utils.image.component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.wheezy.utils.image.ImageSupport;
import com.wheezy.utils.image.ImageUtility;

public class ImagePreviewPanel extends JPanel implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private static final int ACCESSORY_SIZE = 90;
    private static final int SQUARE_SCALE_SIZE = 64;

    private static Logger logger = Logger.getLogger(ImagePreviewPanel.class.getName());

    private Image image;

    public ImagePreviewPanel(String logName)
    {
        this.setPreferredSize(new Dimension(ACCESSORY_SIZE, ACCESSORY_SIZE));
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        FileHandler handler;
        try
        {
            handler = new FileHandler(logName, true);
            logger.addHandler(handler);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Unable to create log file", e);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        String propertyName = e.getPropertyName();

        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
        {
            File selection = (File) e.getNewValue();

            if (selection == null)
            {
                return;
            }

            // AWT supported image formats
            if (ImageSupport.isImageFormatSupported(selection))
            {
                try
                {
                    image = new ImageIcon(
                            ImageUtility.resizeImageFromFile(selection,
                                    SQUARE_SCALE_SIZE, SQUARE_SCALE_SIZE, true)).getImage();
                }
                catch (IOException e1)
                {
                    logger.log(Level.WARNING, "Image Resize Error", e1);
                    return;
                }
                repaint();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(getBackground());

        // Fill is necessary to prevent draw over issues
        g.fillRect(0, 0, getWidth(), getHeight());

        // Scale image appropriately to prevent image stretch
        if (image != null)
        {
            g.drawImage(image,
                    getWidth() / 2 - image.getWidth(this) / 2,
                    getHeight() / 2 - image.getHeight(this) / 2,
                    image.getWidth(this), image.getHeight(this), this);
        }
    }

}
