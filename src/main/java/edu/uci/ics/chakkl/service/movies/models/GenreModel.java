package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GenreModel {
    @JsonProperty(value = "genre_id", required = true)
    private int genre_id;
    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonCreator
    public GenreModel(@JsonProperty(value = "genre_id", required = true) int genre_id,
                      @JsonProperty(value = "name", required = true) String name)
    {
        this.genre_id = genre_id;
        this.name = name;
    }

    @JsonProperty("genre_id")
    public int getGenre_id()
    {
        return genre_id;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }
}
