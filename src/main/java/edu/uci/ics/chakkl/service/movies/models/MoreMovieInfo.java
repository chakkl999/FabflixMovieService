package edu.uci.ics.chakkl.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MoreMovieInfo extends MovieInfo {
    @JsonProperty(value = "num_votes", required = true)
    private int num_votes;
    @JsonProperty(value = "budget")
    private String budget;
    @JsonProperty(value = "revenue")
    private String revenue;
    @JsonProperty(value = "overview")
    private String overview;
    @JsonProperty(value = "genres")
    private GenreModel genres[];
    @JsonProperty(value = "people")
    private MinimalPersonInfo people[];

    @JsonCreator
    public MoreMovieInfo(@JsonProperty(value = "movie_id", required = true) String movie_id,
                         @JsonProperty(value = "title", required = true) String title,
                         @JsonProperty(value = "year", required = true) int year,
                         @JsonProperty(value = "director", required = true) String director,
                         @JsonProperty(value = "rating", required = true) float rating,
                         @JsonProperty(value = "backdrop_path") String backdrop_path,
                         @JsonProperty(value = "poster_path") String poster_path,
                         @JsonProperty(value = "hidden") Boolean hidden,
                         @JsonProperty(value = "num_votes", required = true) int num_votes,
                         @JsonProperty(value = "budget") String budget,
                         @JsonProperty(value = "revenue") String revenue,
                         @JsonProperty(value = "overview") String overview,
                         @JsonProperty(value = "genres") GenreModel genres[],
                         @JsonProperty(value = "people") MinimalPersonInfo people[])
    {
        super(movie_id, title, year, director, rating, backdrop_path, poster_path, hidden);
        this.num_votes = num_votes;
        this.budget = budget;
        this.revenue = revenue;
        this.overview = overview;
        this.genres = genres;
        this.people = people;
    }

    @JsonProperty("num_votes")
    public int getNum_votes()
    {
        return num_votes;
    }

    @JsonProperty("budget")
    public String getBudget()
    {
        return budget;
    }

    @JsonProperty("revenue")
    public String getRevenue()
    {
        return revenue;
    }

    @JsonProperty("overview")
    public String getOverview()
    {
        return overview;
    }

    @JsonProperty("genres")
    public GenreModel[] getGenres()
    {
        return genres;
    }

    @JsonProperty("people")
    public MinimalPersonInfo[] getPeople()
    {
        return people;
    }

    @JsonIgnore
    public void setGenres(GenreModel g[])
    {
        if(g == null)
            genres = new GenreModel[0];
        else
            genres = g;
    }

    @JsonIgnore
    public void setPeople(MinimalPersonInfo p[])
    {
        if(p == null)
            people = new MinimalPersonInfo[0];
        else
            people = p;
    }
}
