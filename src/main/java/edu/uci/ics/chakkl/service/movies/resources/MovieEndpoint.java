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
        ArrayList<MovieInfo> movies = new ArrayList<>();
        try {
            ResultSet rs = Util.preparedStatement(query, p).executeQuery();
            if (!rs.isBeforeFirst()) {
                ServiceLogger.LOGGER.info("No movies found.");
            }
            while(rs.next()) {
                movies.add(Util.mapping(rs.getString("Movies"), MovieInfo.class));
            }
            if(hidden == null || !getPlevel(header.getEmail(), 4))
                for(MovieInfo m: movies)
                    m.setHidden(null);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error when searching for movies: " + e.getMessage());
            return Util.internal_server_error();
        }
        MovieInfo m[] = new MovieInfo[movies.size()];
        SearchResponseModel responseModel = new SearchResponseModel(movies.toArray(m));
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
        ArrayList<MovieInfo> movies = new ArrayList<>();
        try {
            ResultSet rs = Util.preparedStatement(query, p).executeQuery();
            if (!rs.isBeforeFirst()) {
                ServiceLogger.LOGGER.info("No movies found.");
            }
            while(rs.next()) {
                movies.add(Util.mapping(rs.getString("Movies"), MovieInfo.class));
            }
            if(!getPlevel(header.getEmail(), 4))
                for(MovieInfo m: movies)
                    m.setHidden(null);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL error when browsing for movies: " + e.getMessage());
            return Util.internal_server_error();
        }
        MovieInfo m[] = new MovieInfo[movies.size()];
        SearchResponseModel responseModel = new SearchResponseModel(movies.toArray(m));
        Response.ResponseBuilder builder = responseModel.buildResponse();
        builder.header("email", header.getEmail());
        builder.header("session_id", header.getSession_id());
        return builder.build();
    }

    private String createSearchQuery(String title, Integer year, String director, String genre, Boolean hidden,
                               String orderby, String direction, Header header)
    {
        String query =  "SELECT DISTINCT JSON_OBJECT('movie_id', m.movie_id, 'title', m.title, 'year', m.year, 'director', p.name, 'rating', m.rating, 'backdrop_path', m.backdrop_path, 'poster_path', m.poster_path, 'hidden', m.hidden) as Movies\n" +
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
        else if (!getPlevel(header.getEmail(), 4)) //if they're plevel 5
            query += " AND m.hidden = 0";
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
        return p;
    }

    private String createBrowseQuery(String phrase, String orderby, String direction, Header header)
    {
        String query =  "SELECT DISTINCT JSON_OBJECT('movie_id', m.movie_id, 'title', m.title, 'year', m.year, 'director', p.name, 'rating', m.rating, 'backdrop_path', m.backdrop_path, 'poster_path', m.poster_path, 'hidden', m.hidden) as Movies\n" +
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
        if (!getPlevel(header.getEmail(), 4))
            query += " AND m.hidden = 0";
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

    private ArrayList<Parameter> createBrowseParameters(String phrase, Integer limit, Integer offset)
    {
        ArrayList<Parameter> p = new ArrayList<>();
        String phrases[] = phrase.split(",");
        for(int index = 1; index <= phrases.length; ++index) {
            p.add(Parameter.createParameter(Types.VARCHAR, phrases[index-1]));
        }
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
        return p;
    }

    private boolean getPlevel(String email, int plevel)
    {
        PlevelRequestModel requestModel = new PlevelRequestModel(email, plevel);

        ServiceLogger.LOGGER.info("Building client...");
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);


        ServiceLogger.LOGGER.info("Building WebTarget...");
        IdmConfigs temp = MoviesService.getIdmConfigs();
        WebTarget webTarget = client.target(temp.getScheme()+temp.getHostName()+":"+temp.getPort()+temp.getPath()).path(MoviesService.getIdmConfigs().getPrivilegePath());
        ServiceLogger.LOGGER.info("Sending to path: " + MoviesService.getIdmConfigs().getPath() + MoviesService.getIdmConfigs().getPrivilegePath());

        ServiceLogger.LOGGER.info("Starting invocation builder...");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);


        ServiceLogger.LOGGER.info("Sending request...");
        Response response = invocationBuilder.post(Entity.entity(requestModel, MediaType.APPLICATION_JSON));
        ServiceLogger.LOGGER.info("Request sent.");
        PlevelResponseModel responseModel = Util.mapping(response.readEntity(String.class), PlevelResponseModel.class);
        if(responseModel == null)
            return false;
        if(responseModel.getResultCode() == 140)
            return true;
        return false;
    }

}
