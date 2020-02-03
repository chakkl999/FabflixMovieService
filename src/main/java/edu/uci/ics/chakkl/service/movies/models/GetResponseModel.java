package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.chakkl.service.movies.util.Result;

public class GetResponseModel extends BaseResponseModel {
    @JsonProperty(value = "movie", required = true)
    private MoreMovieInfo movie;

    public GetResponseModel() {}

    @JsonCreator
    public GetResponseModel(MoreMovieInfo movie)
    {
        if(movie == null) {
            this.setResult(Result.NO_MOVIES_FOUND_WITH_SEARCH_PARAMETERS);
            this.movie = null;
        }
        else {
            this.setResult(Result.FOUND_MOVIE_WItH_SEARCH_PARAMETERS);
            this.movie = movie;
        }
    }

    @JsonIgnore
    public void setMovies(MoreMovieInfo movie)
    {
        if(movie == null)
            this.setResult(Result.NO_MOVIES_FOUND_WITH_SEARCH_PARAMETERS);
        else
            this.setResult(Result.FOUND_MOVIE_WItH_SEARCH_PARAMETERS);
        this.movie = movie;
    }

    @JsonProperty("movie")
    public MoreMovieInfo getMovie() {
        return movie;
    }
}
