package edu.uci.ics.chakkl.service.movies.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.chakkl.service.movies.MoviesService;
import edu.uci.ics.chakkl.service.movies.configs.IdmConfigs;
import edu.uci.ics.chakkl.service.movies.logger.ServiceLogger;
import edu.uci.ics.chakkl.service.movies.models.PlevelRequestModel;
import edu.uci.ics.chakkl.service.movies.models.PlevelResponseModel;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
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
        if(jsonText == null) {
            ServiceLogger.LOGGER.info("Nothing to map.");
            return null;
        }
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

    public static boolean getPlevel(String email, int plevel)
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
