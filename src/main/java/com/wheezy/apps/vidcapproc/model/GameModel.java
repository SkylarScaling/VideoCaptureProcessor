package com.wheezy.apps.vidcapproc.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Observable;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.wheezy.apps.vidcapproc.VideoCaptureProcessor;
import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.data.GameCollection;
import com.wheezy.utils.file.FileUtility;

public class GameModel extends Observable
{
    private static GameCollection gameCollection = new GameCollection();
    private static File saveFile = new File("SavedGames.xml");
    private static Logger logger = Logger.getLogger(GameModel.class.getName());

    public GameModel()
    {
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

        loadGameCollection();
    }

    /**
     * Returns a copy of the Games in the set. This is to prevent
     * non-Controller modification of the model.
     */
    public LinkedHashSet<Game> getGames()
    {
        //Return a copy of the set, not a reference.
        return new LinkedHashSet<Game>(gameCollection.getGameSet());
    }

    public Game getGame(String gameTitle)
    {
        return gameCollection.getGame(gameTitle);
    }

    public void addGame(Game game)
    {
        gameCollection.addGame(game);
        setChanged();
        notifyObservers("ADDED:" + game.getGameTitle());
    }

    public void deleteGame(Game game)
    {
        gameCollection.deleteGame(game);
        setChanged();
        notifyObservers("DELETED:" + game.getGameTitle());
    }

    public void updateGame(Game origGame, Game newGame)
    {
        gameCollection.updateGame(origGame, newGame);
        setChanged();
        if (origGame.getGameTitle().equals(newGame.getGameTitle()))
        {
            notifyObservers("UPDATED:" + newGame.getGameTitle());
        }
        else
        {
            notifyObservers("DELETED:" + origGame.getGameTitle());
            notifyObservers("ADDED:" + origGame.getGameTitle());
        }
    }

    public void moveGame(Game game, int direction)
    {
        gameCollection.moveGame(game, direction);
        File backup = new File("backup.bak");
        try
        {
            FileUtility.copyFiles(saveFile, backup, false);
        }
        catch (IOException e1)
        {
            // File copy failed, GUI will update, but move will not be saved
            logger.log(Level.WARNING, "Game Order Not Saved", e1);
            setChanged();
            notifyObservers("MOVED:" + game.getGameTitle());
            return;
        }

        if (!saveFile.delete())
        {
            //File was not deleted, but this isn't a problem worth reporting.
            //Cleanup will catch the file.
        }

        try
        {
            saveGameCollection();
        }
        catch (Exception e)
        {
            // GUI is updated, but move will not be saved.
            // Restore backed up file
            try
            {
                FileUtility.copyFiles(backup, saveFile, false);
                backup.delete();
            }
            catch (IOException e1)
            {
                // Shit.
                logger.log(Level.SEVERE, "Game Save File Lost!", e1);
            }
        }
        setChanged();
        notifyObservers("MOVED:" + game.getGameTitle());
    }

    public void loadGameCollection()
    {
        // Create a JAXB context passing in the class of the object we want to unmarshal
        JAXBContext context;
        try
        {
            context = JAXBContext.newInstance(GameCollection.class);
            // Create the unmarshaller
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Unmarshal the XML in the reader back into an object
            if (saveFile.exists())
            {
                setGameCollection((GameCollection) unmarshaller.unmarshal(new FileReader(saveFile)));
            }
        }
        catch (JAXBException e)
        {
            // An empty GameSet will be returned, which is the best way to handle this.
            logger.log(Level.WARNING, "Game Load Error", e);
        }
        catch (FileNotFoundException e)
        {
            // We checked that it exists. Impossible?
            logger.log(Level.WARNING, "Game File Not Found", e);
        }
    }

    private static void setGameCollection(GameCollection collection)
    {
        gameCollection = collection;
    }

    public void saveGameCollection() throws JAXBException, IOException
    {
        // Create a JAXB context passing in the class of the object we want to marshal
        JAXBContext context;
        context = JAXBContext.newInstance(GameCollection.class);

        // Create the marshaller
        Marshaller marshaller = context.createMarshaller();

        // Create a writer to hold the XML
        FileWriter fileWriter = new FileWriter(saveFile);

        // Marshal the javaObject and write the XML to the writer
        marshaller.marshal(gameCollection, fileWriter);
    }

    public static void main(String[] args) throws Throwable
    {
        GameModel model = new GameModel();
        Game game = new Game();
        game.setGameTitle("Black Ops");
        game.setFilenameLabel("BO");
        game.setIconImageFilepath("images/BlackOps.jpg");
        Game game2 = new Game();
        game2.setGameTitle("Modern Warfare 2");
        game2.setFilenameLabel("MW2");
        game2.setIconImageFilepath("images/MW2.jpg");
        model.addGame(game2);
        model.addGame(game);
        model.saveGameCollection();
        model.loadGameCollection();
        System.out.println(gameCollection.toString());
    }
}
