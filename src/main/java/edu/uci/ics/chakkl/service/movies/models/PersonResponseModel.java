package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.chakkl.service.movies.util.Result;

public class PersonResponseModel extends BaseResponseModel {
    @JsonProperty(value = "person", required = true)
    private MorePersonInfo person;

    @JsonCreator
    public PersonResponseModel(@JsonProperty(value = "person", required = true) MorePersonInfo person)
    {
        if(person == null)
            this.setResult(Result.NO_PEOPLE_FOUND_WITH_SEARCH_PARAMETERS);
        else
            this.setResult(Result.FOUND_PEOPLE_WITH_SEARCH_PARAMETERS);
        this.person = person;
    }

    @JsonProperty("person")
    public MorePersonInfo getPerson() {
        return person;
    }
}
