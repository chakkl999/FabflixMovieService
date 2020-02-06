package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.chakkl.service.movies.util.Result;

public class SearchResponseModel extends BaseResponseModel {
    @JsonProperty(value = "movies", required = true)
    MovieInfo movies[];

    public SearchResponseModel() {}

    @JsonCreator
    public SearchResponseModel(MovieInfo movies[])
    {
        if(movies == null || movies.length == 0) {
            this.setResult(Result.NO_MOVIES_FOUND_WITH_SEARCH_PARAMETERS);
        }
        else {
            this.setResult(Result.FOUND_MOVIE_WItH_SEARCH_PARAMETERS);
        }
        this.movies = movies;
    }

    @JsonIgnore
    public void setMovies(MovieInfo movies[])
    {
        if(movies == null || movies.length == 0)
            this.setResult(Result.NO_MOVIES_FOUND_WITH_SEARCH_PARAMETERS);
        else
            this.setResult(Result.FOUND_MOVIE_WItH_SEARCH_PARAMETERS);
        this.movies = movies;
    }

    @JsonProperty("movies")
    public MovieInfo[] getMovies() {
        return movies;
    }
}
