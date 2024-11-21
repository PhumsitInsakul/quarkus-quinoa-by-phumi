package org.acme;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/protected")
@Authenticated
public class ProtectedResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProtectedData() {
        return Response.ok("{\"message\": \"This is a protected resource!\"}").build();
    }

}
