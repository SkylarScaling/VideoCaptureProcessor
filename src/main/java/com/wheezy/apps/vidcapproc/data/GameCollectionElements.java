package com.wheezy.apps.vidcapproc.data;

import javax.xml.bind.annotation.XmlElement;

public class GameCollectionElements
{
    @XmlElement
    public String key;
    @XmlElement
    public Game value;

    @SuppressWarnings("unused")
    private GameCollectionElements()
    {
    } //Required by JAXB

    public GameCollectionElements(String key, Game value)
    {
        this.key = key;
        this.value = value;
    }
}
