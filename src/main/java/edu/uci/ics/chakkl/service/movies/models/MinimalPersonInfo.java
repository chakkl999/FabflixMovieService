package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MinimalPersonInfo {
    @JsonProperty(value = "person_id", required = true)
    private int person_id;
    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonCreator
    public MinimalPersonInfo(@JsonProperty(value = "person_id", required = true) int person_id,
                             @JsonProperty(value = "name", required = true) String name)
    {
        this.person_id = person_id;
        this.name = name;
    }

    @JsonProperty("person_id")
    public int getPerson_id()
    {
        return person_id;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }
}
