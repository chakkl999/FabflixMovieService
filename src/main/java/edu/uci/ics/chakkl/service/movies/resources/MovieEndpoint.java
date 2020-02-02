package edu.uci.ics.chakkl.service.movies.resources;

import edu.uci.ics.chakkl.service.movies.MoviesService;
import edu.uci.ics.chakkl.service.movies.configs.IdmConfigs;
import edu.uci.ics.chakkl.service.movies.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.movies.models.*;
import edu.uci.ics.chakkl.service.movies.util.Header;
import edu.uci.ics.chakkl.service.movies.util.Parameter;
import edu.uci.ics.chakkl.service.movies.util.Util;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.Service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

@Path("/")
public class MovieEndpoint {
    @Path("search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchMovie(@Context HttpHeaders headers, @QueryParam("title") String title, @QueryParam("year") Integer year,
                                @QueryParam("director") String director, @QueryParam("genre") String genre, @QueryParam("hidden") Boolean hidden,
                                @QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset, @QueryParam("orderby") String orderby,
                                @QueryParam("direction") String direction)
    {
        Header header = new Header(headers);
        String query = createSearchQuery(title,year,director,genre,hidden,orderby,direction,header);
        ArrayList<Parameter> p = createSearchParameters(title,year,director,genre,limit,offset);
        MovieInfo movies[] = null;
        ServiceLogger.LOGGER.info(query);
        try {
            ResultSet rs = Util.preparedStatement(query, p).executeQuery();
            if(rs.next()) {
                movies = Util.mapping(rs.getString("MovieInfo"), MovieInfo[].class);
                if (movies == null) {
                    ServiceLogger.LOGGER.info("No movies found.");
                } else {
                    if (!Util.getPlevel(header.getEmail(), 4))
                        for (MovieInfo m : movies)
                            m.setHidden(null);
                    ServiceLogger.LOGGER.info("Movies found.");
                }
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error when searching for movies: " + e.getMessage());
            return Util.internal_server_error();
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return Util.internal_server_error();
        }
        SearchResponseModel responseModel = new SearchResponseModel(movies);
        Response.ResponseBuilder builder = responseModel.buildResponse();
        builder.header("email", header.getEmail());
        builder.header("session_id", header.getSession_id());
        return builder.build();
    }

    @Path("browse/{phrase}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response browseMovies(@Context HttpHeaders headers, @PathParam("phrase") String phrase, @QueryParam("limit") Integer limit,
                                 @QueryParam("offset") Integer offset, @QueryParam("orderby") String orderby,
                                 @QueryParam("direction") String direction)
    {
        Header header = new Header(headers);
        String query = createBrowseQuery(phrase,orderby,direction,header);
        ArrayList<Parameter> p = createBrowseParameters(phrase,limit,offset);
        MovieInfo movies[] = null;
        try {
            ResultSet rs = Util.preparedStatement(query, p).executeQuery();
            if(rs.next()) {
                movies = Util.mapping(rs.getString("MovieInfo"), MovieInfo[].class);
                if(movies == null) {
                    ServiceLogger.LOGGER.info("No movies found.");
                }
                else {
                    if (!Util.getPlevel(header.getEmail(), 4))
                        for (MovieInfo m : movies)
                            m.setHidden(null);
                    ServiceLogger.LOGGER.info("Movies found.");
                }
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error when browsing for movies: " + e.getMessage());
            return Util.internal_server_error();
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return Util.internal_server_error();
        }
        SearchResponseModel responseModel = new SearchResponseModel(movies);
        Response.ResponseBuilder builder = responseModel.buildResponse();
        builder.header("email", header.getEmail());
        builder.header("session_id", header.getSession_id());
        return builder.build();
    }

    @Path("get/{movie_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMovie(@Context HttpHeaders headers, @PathParam("movie_id") String movie_id)
    {
        Header header = new Header(headers);
        return Util.internal_server_error();
    }

    private String createSearchQuery(String title, Integer year, String director, String genre, Boolean hidden,
                               String orderby, String direction, Header header)
    {
        String query =  "SELECT JSON_ARRAYAGG(JSON_OBJECT('movie_id', m2.movie_id, 'title', m2.title, 'year', m2.year, 'director', m2.name, 'rating', m2.rating, 'backdrop_path', m2.backdrop_path, 'poster_path', m2.poster_path, 'hidden', m2.hidden)) as MovieInfo\n" +
                        "FROM (SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden\n" +
                        "FROM movie as m\n" +
                        "INNER JOIN person as p on m.director_id = p.person_id\n" +
                        "INNER JOIN genre_in_movie as gim on m.movie_id = gim.movie_id\n" +
                        "INNER JOIN genre as g on gim.genre_id = g.genre_id\n" + "WHERE 1=1";
        if(title != null)
            query += " AND m.title LIKE ?";
        if(year != null)
            query += " AND m.year = ?";
        if(director != null)
            query += " AND p.name LIKE ?";
        if(genre != null)
            query += " AND g.name LIKE ?";
        if(hidden == null)
            query += " AND m.hidden = 0";
        else if (!hidden.booleanValue())
            query += " AND m.hidden = 0";
        else if (!Util.getPlevel(header.getEmail(), 4)) //if they're plevel 5
            query += " AND m.hidden = 0";
        query += (setOrder(orderby, direction) + ") as m2");
        return query;
    }

    private ArrayList<Parameter> createSearchParameters(String title, Integer year, String director, String genre, Integer limit,
                                       Integer offset)
    {
        ArrayList<Parameter> p = new ArrayList<>();
        if(title != null)
            p.add(Parameter.createParameter(Types.VARCHAR, "%" + title + "%"));
        if(year != null)
            p.add(Parameter.createParameter(Types.INTEGER, year));
        if(director != null)
            p.add(Parameter.createParameter(Types.VARCHAR, "%" + director + "%"));
        if(genre != null)
            p.add(Parameter.createParameter(Types.VARCHAR, "%" + genre + "%"));
        setOffsetAndLimit(p, limit, offset);
        return p;
    }

    private String createBrowseQuery(String phrase, String orderby, String direction, Header header)
    {
        String query = "SELECT JSON_ARRAYAGG(JSON_OBJECT('movie_id', m2.movie_id, 'title', m2.title, 'year', m2.year, 'director', m2.name, 'rating', m2.rating, 'backdrop_path', m2.backdrop_path, 'poster_path', m2.poster_path, 'hidden', m2.hidden)) as MovieInfo\n" +
                        "FROM (SELECT DISTINCT m.movie_id ,m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden\n" +
                        "FROM movie as m\n" +
                        "INNER JOIN person as p on m.director_id = p.person_id\n";
        String phrases[] = phrase.split(",");
        for(int index = 1; index <= phrases.length; ++index) {
            query += ("INNER JOIN keyword_in_movie as km" + index + " on m.movie_id = km" + index +".movie_id\n" +
                     "INNER JOIN keyword as k" + index + " on km" + index + ".keyword_id = k" + index + ".keyword_id\n");
        }
        query += "WHERE 1=1";
        for(int index = 1; index <= phrases.length; ++index) {
            query += (" AND k" + index + ".name LIKE ?");
        }
        if (!Util.getPlevel(header.getEmail(), 4))
            query += " AND m.hidden = 0";
        query += (setOrder(orderby, direction) + ") as m2");
        return query;
    }

    private ArrayList<Parameter> createBrowseParameters(String phrase, Integer limit, Integer offset)
    {
        ArrayList<Parameter> p = new ArrayList<>();
        String phrases[] = phrase.split(",");
        for(int index = 1; index <= phrases.length; ++index) {
            p.add(Parameter.createParameter(Types.VARCHAR, phrases[index-1]));
        }
        setOffsetAndLimit(p, limit, offset);
        return p;
    }

    private String createGetQuery(String movie_id, Header header)
    {
        String query = "SELECT DISTINCT JSON_OBJECT('movie_id', m.movie_id, 'title', m.title, 'year', m.year, 'director', p.name, 'rating', m.rating, 'num_votes', m.num_votes, 'budget', m.budget, 'revenue', m.revenue, 'overview', m.overview, 'backdrop_path', m.backdrop_path, 'poster_path', 'hidden', m.hidden) as Movies\n" +
                    "FROM movie as m\n"+
                    "INNER JOIN person as p on m.director_id = p.person_id\n"+
                    "WHERE 1=1 AND m.movie_id = ?";
        String q2 = "SELECT DISTINCT ";
        return query;
    }

    private void setOffsetAndLimit(ArrayList<Parameter> p, Integer limit, Integer offset)
    {
        if(limit == null)
            limit = 10;
        else if (limit != 10 && limit != 25 && limit != 50 && limit != 100)
            limit = 10;
        if(offset == null)
            offset = 0;
        else if (offset % limit != 0)
            offset = 0;
        p.add(Parameter.createParameter(Types.INTEGER, limit));
        p.add(Parameter.createParameter(Types.INTEGER, offset));
    }

    private String setOrder(String orderby, String direction)
    {
        String query = "";
        if(direction == null) {
            direction = "ASC";
        }
        if(orderby == null) {
            orderby = "title";
        }
        if(orderby.equals("title")) {
            query += "\nORDER BY m.title " + direction + ", m.rating DESC";
        }
        else if (orderby.equals("rating")) {
            query += "\nORDER BY m.rating " + direction + ", m.title ASC";
        }
        else if (orderby.equals("year")) {
            query += "\nORDER BY m.year " + direction + ", m.rating DESC";
        }
        else {
            query += "\nORDER BY m.title " + direction + ", m.rating DESC";
        }
        query += "\nLIMIT ? OFFSET ?";
        return query;
    }
}
