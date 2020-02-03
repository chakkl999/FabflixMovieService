package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.chakkl.service.movies.util.Result;

public class PeopleResponseModel extends BaseResponseModel{
    @JsonProperty(value = "people", required = true)
    PersonInfo p[];

    public PeopleResponseModel() {}

    @JsonCreator
    public PeopleResponseModel(PersonInfo p[])
    {
        if(p == null || p.length == 0) {
            this.setResult(Result.NO_PEOPLE_FOUND_WITH_SEARCH_PARAMETERS);
            this.p = new PersonInfo[0];
        }
        else {
            this.setResult(Result.FOUND_PEOPLE_WITH_SEARCH_PARAMETERS);
            this.p = p;
        }
    }

    @JsonIgnore
    public void setPeople(PersonInfo p[])
    {
        if(p.length == 0)
            this.setResult(Result.NO_PEOPLE_FOUND_WITH_SEARCH_PARAMETERS);
        else
            this.setResult(Result.FOUND_PEOPLE_WITH_SEARCH_PARAMETERS);
        this.p = p;
    }

    @JsonProperty("people")
    public PersonInfo[] getPeople() {
        return p;
    }
}
