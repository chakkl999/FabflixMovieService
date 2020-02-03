package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MorePersonInfo extends PersonInfo {
    @JsonProperty(value = "deathday")
    private String deathday;
    @JsonProperty(value = "biography")
    private String biography;
    @JsonProperty(value = "birthplace")
    private String birthplace;
    @JsonProperty(value = "gender")
    private String gender;

    @JsonCreator
    public MorePersonInfo(@JsonProperty(value = "person_id", required = true) int person_id,
                          @JsonProperty(value = "name", required = true) String name,
                          @JsonProperty(value = "birthday") String birthday,
                          @JsonProperty(value = "popularity") Float popularity,
                          @JsonProperty(value = "profile_path") String profile_path,
                          @JsonProperty(value = "deathday") String deathday,
                          @JsonProperty(value = "biography") String biography,
                          @JsonProperty(value = "birthplace") String birthplace,
                          @JsonProperty(value = "gender") String gender)
    {
        super(person_id, name, birthday, popularity, profile_path);
        this.deathday = deathday;
        this.biography = biography;
        this.birthplace = birthplace;
        this.gender = gender;
    }

    @JsonProperty("deathday")
    public String getDeathday() {
        return deathday;
    }

    @JsonProperty("biography")
    public String getBiography() {
        return biography;
    }

    @JsonProperty("birthplace")
    public String getBirthplace() {
        return birthplace;
    }

    @JsonProperty("gender")
    public String getGender() {
        return gender;
    }
}
