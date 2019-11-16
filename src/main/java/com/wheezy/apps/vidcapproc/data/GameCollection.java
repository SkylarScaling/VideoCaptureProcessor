package com.wheezy.apps.vidcapproc.data;

import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GameCollection
{
    public static final int MOVE_UP = 1;
    public static final int MOVE_DOWN = 2;

    private LinkedHashSet<Game> gameSet = new LinkedHashSet<Game>();

    @XmlElement
    public LinkedHashSet<Game> getGameSet()
    {
        return gameSet;
    }

    public Game getGame(String gameTitle)
    {
        for (Game game : gameSet)
        {
            if (game.getGameTitle().equals(gameTitle))
            {
                return game;
            }
        }
        return null;
    }

    public void addGame(Game game)
    {
        gameSet.add(game);
    }

    public void deleteGame(Game game)
    {
        gameSet.remove(game);
    }

    public void updateGame(Game origGame, Game newGame)
    {
        if (gameSet.contains(origGame))
        {
            LinkedHashSet<Game> newSet = new LinkedHashSet<Game>();

            for (Game nextGame : gameSet)
            {
                if (nextGame.equals(origGame))
                {
                    newSet.add(newGame);
                }
                else
                {
                    newSet.add(nextGame);
                }
            }

            gameSet = newSet;
        }
    }

    public void moveGame(Game game, int direction)
    {
        int gameIndex = -1;
        int i = 0;
        for (Game nextGame : gameSet)
        {
            if (nextGame.equals(game))
            {
                gameIndex = i;
                break;
            }
            i++;
        }

        if (gameIndex != -1)
        {
            if (!(gameIndex == 0 && direction == MOVE_UP) &&
                    !(gameIndex == (gameSet.size() - 1) && direction == MOVE_DOWN))
            {
                gameSet.remove(game);

                LinkedHashSet<Game> newSet = new LinkedHashSet<Game>();
                i = 0;
                for (Game nextGame : gameSet)
                {
                    if ((direction == MOVE_UP && gameIndex - 1 == i) ||
                            (direction == MOVE_DOWN && gameIndex + 1 == i))
                    {
                        newSet.add(game);
                    }
                    newSet.add(nextGame);
                    i++;
                }

                if (gameIndex + 1 == gameSet.size())
                {
                    newSet.add(game);
                }

                gameSet = newSet;
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Game Collection {");
        for (Game g : gameSet)
        {
            sb.append(" (");
            sb.append(g.toString());
            sb.append(") ");
        }
        sb.append("}");

        return sb.toString();
    }
}
