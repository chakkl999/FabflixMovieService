package edu.uci.ics.chakkl.service.movies.resources;

import edu.uci.ics.chakkl.service.movies.MoviesService;
import edu.uci.ics.chakkl.service.movies.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.movies.models.*;
import edu.uci.ics.chakkl.service.movies.util.Header;
import edu.uci.ics.chakkl.service.movies.util.Parameter;
import edu.uci.ics.chakkl.service.movies.util.Result;
import edu.uci.ics.chakkl.service.movies.util.Util;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;
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
            return Util.internal_server_error(header);
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Creating response...");
        SearchResponseModel responseModel = new SearchResponseModel(movies);
        Response.ResponseBuilder builder = responseModel.buildResponse();
        header.setHeader(builder);
        ServiceLogger.LOGGER.info("Sending response...");
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
            return Util.internal_server_error(header);
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Creating response...");
        SearchResponseModel responseModel = new SearchResponseModel(movies);
        Response.ResponseBuilder builder = responseModel.buildResponse();
        header.setHeader(builder);
        ServiceLogger.LOGGER.info("Sending response...");
        return builder.build();
    }

    @Path("get/{movie_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMovie(@Context HttpHeaders headers, @PathParam("movie_id") String movie_id)
    {
        Header header = new Header(headers);
        ArrayList<Parameter> p = createGetParameters(movie_id);
        MoreMovieInfo movies = null;
        try {
            ResultSet rs = Util.preparedStatement(createGetQuery(header), p).executeQuery();
            if(rs.next()) {
                movies = Util.mapping(rs.getString("MovieInfo"), MoreMovieInfo.class);
                if(movies == null) {
                    ServiceLogger.LOGGER.info("No movies found.");
                }
                else {
                    rs = Util.preparedStatement(createGetGenreQuery(), p).executeQuery();
                    rs.next();
                    GenreModel genre[] = Util.mapping(rs.getString("genre"), GenreModel[].class);
                    rs = Util.preparedStatement(createGetPeopleQuery(), p).executeQuery();
                    rs.next();
                    MinimalPersonInfo people[] = Util.mapping(rs.getString("people"), MinimalPersonInfo[].class);
                    movies.setGenres(genre);
                    movies.setPeople(people);
                    if (!Util.getPlevel(header.getEmail(), 4))
                        movies.setHidden(null);
                    ServiceLogger.LOGGER.info("Movies found.");
                }
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error when browsing for movies: " + e.getMessage());
            return Util.internal_server_error(header);
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Creating response...");
        GetResponseModel responseModel = new GetResponseModel(movies);
        Response.ResponseBuilder builder = responseModel.buildResponse();
        header.setHeader(builder);
        ServiceLogger.LOGGER.info("Sending response...");
        return builder.build();
    }

    @Path("thumbnail")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThumbnail(@Context HttpHeaders headers, String jsonText)
    {
        Header header = new Header(headers);
        ThumbnailRequestModel requestModel = Util.mapping(jsonText, ThumbnailRequestModel.class);
        if(requestModel == null)
            return Util.internal_server_error(header);
        ArrayList<Parameter> p = createThumbnailParameters(requestModel.getMovie_id());
        MinimalMovieInfo movies[] = null;
        try {
            ResultSet rs = Util.preparedStatement(createThumbnailQuery(requestModel.getMovie_id().length), p).executeQuery();
            if(rs.next()) {
                movies = Util.mapping(rs.getString("MovieThumbnail"), MinimalMovieInfo[].class);
            }
            if(movies == null) {
                ServiceLogger.LOGGER.info("No movies found.");
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error: " + e.getMessage());
            return Util.internal_server_error(header);
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Creating response...");
        ThumbnailResponseModel responseModel = new ThumbnailResponseModel(movies);
        Response.ResponseBuilder builder = responseModel.buildResponse();
        header.setHeader(builder);
        ServiceLogger.LOGGER.info("Sending response...");
        return builder.build();
    }

    @Path("people")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response PeopleMovieSearch(@Context HttpHeaders headers, @QueryParam("name") String name, @QueryParam("limit") Integer limit,
                                @QueryParam("offset") Integer offset, @QueryParam("orderby") String orderby,
                                @QueryParam("direction") String direction)
    {
        Header header = new Header(headers);
        SearchResponseModel responseModel = new SearchResponseModel();
        MovieInfo[] movies = null;
        Response.ResponseBuilder builder;
        if(name == null || !checkIFPersonExist(name)) {
            ServiceLogger.LOGGER.info("Person does not exist.");
            responseModel.setResult(Result.NO_PEOPLE_FOUND_WITH_SEARCH_PARAMETERS);
            responseModel.setMovies(movies);
            builder = responseModel.buildResponse();
            header.setHeader(builder);
            return builder.build();
        }
        ArrayList<String> movie_id = new ArrayList<>();
        String movie_idQuery = createSearchMovieIDFromPeopleQuery();
        try {
            PreparedStatement ps = MoviesService.getCon().prepareStatement(movie_idQuery);
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                movie_id.add(rs.getString("movie_id"));
            }
            if(movie_id.size() == 0) {
                ServiceLogger.LOGGER.info("No movie found.");
                responseModel.setResult(Result.NO_MOVIES_FOUND_WITH_SEARCH_PARAMETERS);
                responseModel.setMovies(movies);
                builder = responseModel.buildResponse();
                header.setHeader(builder);
                return builder.build();
            }
            rs = Util.preparedStatement(createSearchMovieFromPeopleQuery(movie_id.size(), orderby, direction, header), createSearchMovieFromPeopleParameters(movie_id, limit, offset)).executeQuery();
            if(rs.next()) {
                movies = Util.mapping(rs.getString("MovieInfo"), MovieInfo[].class);
                if(!Util.getPlevel(header.getEmail(), 4))
                    for(MovieInfo m: movies)
                        m.setHidden(null);
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error: " + e.getMessage());
            return Util.internal_server_error(header);
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Creating response...");
        responseModel.setResult(Result.FOUND_MOVIE_WItH_SEARCH_PARAMETERS);
        responseModel.setMovies(movies);
        builder = responseModel.buildResponse();
        header.setHeader(builder);
        ServiceLogger.LOGGER.info("Sending response...");
        return builder.build();
    }

    @Path("people/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchPeople(@Context HttpHeaders headers, @QueryParam("name") String name,
                                 @QueryParam("birthday")String birthday, @QueryParam("movie_title") String title,
                                 @QueryParam("limit") Integer limit, @QueryParam("offsets") Integer offset,
                                 @QueryParam("orderby") String orderby, @QueryParam("direction") String direction)
    {
        Header header = new Header(headers);
        String query = createPeopleSearchQuery(name, birthday, title, orderby, direction);
        ArrayList<Parameter> p = createPeopleSearchParameters(name, birthday, title, limit, offset);
        PersonInfo people[] = null;
        try {
            ResultSet rs = Util.preparedStatement(query, p).executeQuery();
            if(rs.next()) {
                people = Util.mapping(rs.getString("PersonInfo"), PersonInfo[].class);
            }
            if(people == null) {
                ServiceLogger.LOGGER.info("No person found.");
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error: " + e.getMessage());
            return Util.internal_server_error(header);
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Creating response...");
        PeopleResponseModel responseModel = new PeopleResponseModel(people);
        Response.ResponseBuilder builder = responseModel.buildResponse();
        header.setHeader(builder);
        ServiceLogger.LOGGER.info("Sending response...");
        return builder.build();
    }

    @Path("people/get/{person_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPerson(@Context HttpHeaders headers, @PathParam("person_id") String person_id)
    {
        Header header = new Header(headers);
        String query = createGetPersonIDQuery();
        MorePersonInfo p = null;
        try {
            PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
            ps.setString(1, person_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                p = Util.mapping(rs.getString("PersonInfo"), MorePersonInfo.class);
            }
            if(p == null) {
                ServiceLogger.LOGGER.info("No person found.");
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error: " + e.getMessage());
            return Util.internal_server_error(header);
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return Util.internal_server_error(header);
        }
        ServiceLogger.LOGGER.info("Creating response...");
        PersonResponseModel responseModel = new PersonResponseModel(p);
        Response.ResponseBuilder builder = responseModel.buildResponse();
        header.setHeader(builder);
        ServiceLogger.LOGGER.info("Sending response...");
        return builder.build();
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

    private String createGetQuery(Header header)
    {
        String query = "SELECT DISTINCT JSON_OBJECT('movie_id', m.movie_id ,'title', m.title, 'year', m.year, 'director', p.name, 'rating', m.rating, 'num_votes', m.num_votes, 'budget', m.budget, 'revenue', m.revenue, 'overview', m.overview, 'backdrop_path', m.backdrop_path, 'poster_path', m.poster_path, 'hidden', m.hidden) as MovieInfo\n"+
                        "FROM movie as m\n"+
                        "INNER JOIN person as p on m.director_id = p.person_id\n"+
                        "WHERE m.movie_id LIKE ?";
        if(!Util.getPlevel(header.getEmail(), 4))
            query += " AND m.hidden = 0";
        return query;
    }

    private String createGetGenreQuery()
    {
        String query = "SELECT JSON_ARRAYAGG(JSON_OBJECT('genre_id', g2.genre_id, 'name', g2.name)) as genre\n"+
                        "FROM (SELECT DISTINCT g.genre_id, g.name\n"+
                        "FROM movie as m\n"+
                        "INNER JOIN genre_in_movie gm on m.movie_id = gm.movie_id\n"+
                        "INNER JOIN genre g on g.genre_id = gm.genre_id\n"+
                        "WHERE m.movie_id LIKE ?) as g2";
        return query;
    }

    private String createGetPeopleQuery()
    {
        String query = "SELECT JSON_ARRAYAGG(JSON_OBJECT('person_id', p2.person_id, 'name', p2.name)) as people\n"+
                        "FROM (SELECT DISTINCT p.person_id, p.name\n"+
                        "FROM movie as m\n"+
                        "INNER JOIN person_in_movie pm on m.movie_id = pm.movie_id\n"+
                        "INNER JOIN person p on p.person_id = pm.person_id\n"+
                        "WHERE m.movie_id LIKE ?) as p2";
        return query;
    }

    private ArrayList<Parameter> createGetParameters(String movie_id)
    {
        ArrayList<Parameter> p = new ArrayList<>();
        p.add(Parameter.createParameter(Types.VARCHAR, movie_id));
        return p;
    }

    private String createThumbnailQuery(int num)
    {
        String query = "SELECT JSON_ARRAYAGG(JSON_OBJECT('movie_id', m2.movie_id, 'title', m2.title, 'backdrop_path', m2.backdrop_path, 'poster_path', m2.poster_path)) as MovieThumbnail\n"+
                        "FROM (SELECT DISTINCT m.movie_id, m.title, m.backdrop_path, m.poster_path\n"+
                        "FROM movie as m\n"+
                        "WHERE m.movie_id LIKE ?";
        for(int i = 1; i < num; ++i)
            query += " OR m.movie_id LIKE ?";
        query += ") as m2";
        return query;
    }

    private ArrayList<Parameter> createThumbnailParameters(String movie_id[])
    {
        ArrayList<Parameter> p = new ArrayList<>();
        for(String id: movie_id)
            p.add(Parameter.createParameter(Types.VARCHAR, id));
        return p;
    }

    private boolean checkIFPersonExist(String name)
    {
        String query = "SELECT person_id FROM person as p WHERE p.name LIKE ?";
        try {
            PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return true;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            ServiceLogger.LOGGER.info("Unknown error: " + e.getMessage());
            return false;
        }
        return false;
    }

    private String createSearchMovieIDFromPeopleQuery()
    {
        String query = "SELECT m.movie_id\n"+
                        "FROM movie as m\n"+
                        "INNER JOIN person_in_movie as pm on m.movie_id = pm.movie_id\n"+
                        "INNER JOIN person as p on p.person_id = pm.person_id\n"+
                        "WHERE p.name LIKE ?";
        return query;
    }

    private String createSearchMovieFromPeopleQuery(int num, String orderby, String direction, Header header)
    {
        String query = "SELECT JSON_ARRAYAGG(JSON_OBJECT('movie_id', m2.movie_id, 'title', m2.title, 'year', m2.year, 'director', m2.name, 'rating', m2.rating, 'backdrop_path', m2.backdrop_path, 'poster_path', m2.poster_path, 'hidden', m2.hidden)) as MovieInfo\n" +
                        "FROM (SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden\n" +
                        "FROM movie as m\n" +
                        "INNER JOIN person as p on m.director_id = p.person_id\n"+
                        "WHERE m.movie_id LIKE ?";
        for(int i = 1; i < num; ++i)
            query += " OR m.movie_id LIKE ?";
        if(!Util.getPlevel(header.getEmail(), 4))
            query += " AND m.hidden = 0";
        query += (setOrder(orderby, direction) + ") as m2");
        return query;
    }

    private ArrayList<Parameter> createSearchMovieFromPeopleParameters(ArrayList<String> movie_id, Integer limit, Integer offset)
    {
        ArrayList<Parameter> p = new ArrayList<>();
        for(String id: movie_id)
            p.add(Parameter.createParameter(Types.VARCHAR, id));
        setOffsetAndLimit(p, limit, offset);
        return p;
    }

    private String createPeopleSearchQuery(String name, String birthday, String title, String orderby, String direction)
    {
        String query = "SELECT JSON_ARRAYAGG(JSON_OBJECT('person_id', p2.person_id, 'name', p2.name, 'birthday', p2.birthday, 'popularity', p2.popularity, 'profile_path', p2.profile_path)) as PersonInfo\n" +
                        "FROM (SELECT DISTINCT p.person_id, p.name, p.birthday, p.popularity, p.profile_path\n"+
                        "FROM person as p\n";
        if(title != null)
            query += "INNER JOIN person_in_movie as pm on p.person_id = pm.person_id\n" + "INNER JOIN movie as m on m.movie_id = pm.movie_id\n" + "WHERE m.title LIKE ?";
        else
            query += "WHERE 1=1";
        if(name != null)
            query += " AND p.name LIKE ?";
        if(birthday != null)
            query += " AND p.birthday = ?";
        query += (setPeopleOrder(orderby, direction) + ") as p2");
        return query;
    }

    private ArrayList<Parameter> createPeopleSearchParameters(String name, String birthday, String title, Integer limit,
                                                              Integer offset)
    {
        ArrayList<Parameter> p = new ArrayList<>();
        if(title != null)
            p.add(Parameter.createParameter(Types.VARCHAR, "%" + title +"%"));
        if(name != null)
            p.add(Parameter.createParameter(Types.VARCHAR, "%" + name + "%"));
        if(birthday != null)
            p.add(Parameter.createParameter(Types.DATE, birthday));
        setOffsetAndLimit(p, limit, offset);
        return p;
    }

    private String createGetPersonIDQuery()
    {
        String query = "SELECT JSON_OBJECT('person_id', p.person_id, 'name', p.name, 'gender', g.gender_name, 'birthday', p.birthday, 'deathday', p.deathday, 'biography', p.biography, 'birthplace', p.birthplace, 'popularity', p.popularity, 'profile_path', p.profile_path) as PersonInfo\n" +
                        "FROM person as p\n"+
                        "INNER JOIN gender as g on p.gender_id = g.gender_id\n"+
                        "WHERE p.person_id LIKE ?";
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

    private String setPeopleOrder(String orderby, String direction)
    {
        String query = "";
        if(direction == null) {
            direction = "ASC";
        } else {
            if (!direction.equals("asc") && !direction.equals("desc")) {
                direction = "ASC";
            }
        }
        if(orderby == null) {
            orderby = "name";
        }
        if(orderby.equals("name")) {
            query += "\nORDER BY p.name " + direction + ", p.popularity DESC";
        }
        else if (orderby.equals("birthday")) {
            query += "\nORDER BY p.birthday " + direction + ", p.popularity DESC";
        }
        else if (orderby.equals("popularity")) {
            query += "\nORDER BY p.popularity " + direction + ", p.name DESC";
        }
        else {
            query += "\nORDER BY p.name " + direction + ", p.popularity DESC";
        }
        query += "\nLIMIT ? OFFSET ?";
        return query;
    }
}
