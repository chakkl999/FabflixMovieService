package edu.uci.ics.chakkl.service.movies.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.chakkl.service.movies.MoviesService;
import edu.uci.ics.chakkl.service.movies.logger.ServiceLogger;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class Util {
    public static PreparedStatement preparedStatement(String query, ArrayList<Parameter> parameter) throws SQLException {
        ServiceLogger.LOGGER.info("Preparing statement.");
        PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
        int index = 1;
        for(Parameter p: parameter)
            ps.setObject(index++, p.getObject(), p.getType());
        ServiceLogger.LOGGER.info("Finished preparing statement.");
        ServiceLogger.LOGGER.info(ps.toString());
        return ps;
    }

    public static <T> T mapping(String jsonText, Class<T> className)
    {
        ObjectMapper mapper = new ObjectMapper();

        ServiceLogger.LOGGER.info("Mapping object: " + className.getName());

        try {
            return mapper.readValue(jsonText, className);
        } catch (IOException e) {
            ServiceLogger.LOGGER.info("Mapping Object Failed: " + e.getMessage());
            return null;
        }
    }

    public static Response internal_server_error()
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
