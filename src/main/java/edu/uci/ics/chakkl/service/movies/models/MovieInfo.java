package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieInfo extends MinimalMovieInfo{
    @JsonProperty(value = "year", required = true)
    private int year;
    @JsonProperty(value = "director", required = true)
    private String director;
    @JsonProperty(value = "rating", required = true)
    private float rating;
    @JsonProperty(value = "hidden")
    private Boolean hidden;

    @JsonCreator
    public MovieInfo(@JsonProperty(value = "movie_id", required = true) String movie_id,
                     @JsonProperty(value = "title", required = true) String title,
                     @JsonProperty(value = "year", required = true) int year,
                     @JsonProperty(value = "director", required = true) String director,
                     @JsonProperty(value = "rating", required = true) float rating,
                     @JsonProperty(value = "backdrop_path", required = true) String backdrop_path,
                     @JsonProperty(value = "poster_path") String poster_path, @JsonProperty(value = "hidden") Boolean hidden)
    {
        super(movie_id, title, backdrop_path, poster_path);
        this.year = year;
        this.director = director;
        this.rating = rating;
        this.hidden = hidden;
    }

    @JsonProperty("year")
    public int getYear() {
        return year;
    }

    @JsonProperty("director")
    public String getDirector() {
        return director;
    }

    @JsonProperty("rating")
    public float getRating() {
        return rating;
    }

    @JsonProperty("hidden")
    public Boolean getHidden() {
        return hidden;
    }

    @JsonIgnore
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}
