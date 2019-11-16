package com.wheezy.apps.vidcapproc.data;

import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class GameCollectionMapAdapter
        extends XmlAdapter<HashSet<Game>, LinkedHashMap<String, Game>>
{
    @Override
    public HashSet<Game> marshal(LinkedHashMap<String, Game> gameMap) throws Exception
    {
        HashSet<Game> mapElements = new HashSet<Game>();
        for (Game game : gameMap.values())
        {
            mapElements.add(game);
        }

        return mapElements;
    }

    @Override
    public LinkedHashMap<String, Game> unmarshal(HashSet<Game> elements) throws Exception
    {
        LinkedHashMap<String, Game> gameMap = new LinkedHashMap<String, Game>();
        for (Game mapelement : elements)
        {
            gameMap.put(mapelement.getGameTitle(), mapelement);
        }
        return gameMap;
    }
}
