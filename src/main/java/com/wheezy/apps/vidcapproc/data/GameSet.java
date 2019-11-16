package com.wheezy.apps.vidcapproc.data;

import java.util.HashSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GameSet
{
    @XmlElement
    private HashSet<Game> gameSet;

    public HashSet<Game> getGameSet()
    {
        if (gameSet == null)
        {
            gameSet = new HashSet<Game>();
        }
        return gameSet;
    }

    public void addGame(Game game)
    {
        getGameSet().add(game);
    }
}
