package edu.uci.ics.chakkl.service.movies.util;

import javax.ws.rs.core.Response;

public enum Result {
    FOUND_MOVIE_WItH_SEARCH_PARAMETERS (210, "Found movie(s) with search parameters.", Response.Status.OK),
    NO_MOVIES_FOUND_WITH_SEARCH_PARAMETERS (211, "No movies found with search parameters.", Response.Status.OK);

    private final int resultCode;
    private final String message;
    private final Response.Status httpCode;

    Result(int resultCode, String message, Response.Status httpCode)
    {
        this.resultCode = resultCode;
        this.message = message;
        this.httpCode = httpCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getMessage() {
        return message;
    }

    public Response.Status getHttpCode() {
        return httpCode;
    }
}
