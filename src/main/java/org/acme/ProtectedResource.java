package org.acme;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/protected")
@Authenticated
public class ProtectedResource {

    @GET
    public String getProtectedData() {
        return "This is a protected resource!";
    }
}
