package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonInfo extends MinimalPersonInfo {
    @JsonProperty(value = "birthday")
    private String birthday;
    @JsonProperty(value = "popularity")
    private float popularity;
    @JsonProperty(value = "profile_path")
    private String profile_path;

    @JsonCreator
    public PersonInfo(@JsonProperty(value = "person_id", required = true) int person_id,
                      @JsonProperty(value = "name", required = true) String name,
                      @JsonProperty(value = "birthday") String birthday,
                      @JsonProperty(value = "popularity") Float popularity,
                      @JsonProperty(value = "profile_path") String profile_path)
    {
        super(person_id, name);
        this.birthday = birthday;
        this.popularity = popularity;
        this.profile_path = profile_path;
    }

    @JsonProperty("birthday")
    public String getBirthday() {
        return birthday;
    }

    @JsonProperty("popularity")
    public Float getPopularity() {
        return popularity;
    }

    @JsonProperty("profile_path")
    public String getProfile_path() {
        return profile_path;
    }
}
