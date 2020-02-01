package edu.uci.ics.chakkl.service.movies.util;

import javax.ws.rs.core.HttpHeaders;

public class Header {
    private String email, session_id, transaction_id;

    public Header(HttpHeaders headers)
    {
        email = headers.getHeaderString("email");
        session_id = headers.getHeaderString("session_id");
        transaction_id = headers.getHeaderString("transaction_id");
    }

    public String getEmail() {
        return email;
    }

    public String getSession_id() {
        return session_id;
    }

    public String getTransaction_id() {
        return transaction_id;
    }
}
