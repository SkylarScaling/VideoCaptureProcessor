package com.wheezy.apps.vidcapproc.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Observer;

import javax.xml.bind.JAXBException;

import com.wheezy.apps.vidcapproc.data.Game;
import com.wheezy.apps.vidcapproc.model.GameModel;

public class GameController
{
    private static GameController instance;

    private GameModel model;

    public GameController()
    {
        model = new GameModel();
    }

    public LinkedHashSet<Game> getGames()
    {
        return model.getGames();
    }

    public Game getGame(String gameTitle)
    {
        return model.getGame(gameTitle);
    }

    public LinkedHashMap<String, Game> getGamesInMap()
    {
        LinkedHashMap<String, Game> gameMap = new LinkedHashMap<String, Game>();
        for (Game game : model.getGames())
        {
            gameMap.put(game.getGameTitle(), game);
        }
        return gameMap;
    }

    public void addGame(Game game)
    {
        model.addGame(game);
    }

    public void updateGame(Game origGame, Game newGame)
    {
        model.updateGame(origGame, newGame);
    }

    public void deleteGame(Game game)
    {
        model.deleteGame(game);
    }

    public void moveGame(Game game, int direction)
    {
        model.moveGame(game, direction);
    }

    public void saveGames() throws JAXBException, IOException
    {
        model.saveGameCollection();
    }

    public static GameController getInstance()
    {
        if (instance == null)
        {
            instance = new GameController();
        }
        return instance;
    }

    public void registerModelObserver(Observer observer)
    {
        model.addObserver(observer);
    }
}
