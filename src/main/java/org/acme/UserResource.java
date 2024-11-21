package org.acme;

import io.quarkus.security.Authenticated;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserRepository userRepository;

    // Sign Up Endpoint
    @POST
    @Path("/signup")
    public Response signUp(User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Username cannot be empty").build();
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Password cannot be empty").build();
        }
        // รหัสผ่านต้องมีความยาวตามที่ต้องการ เช่น 8 ตัวขึ้นไป
        if (user.getPassword().length() < 8) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Password must be at least 8 characters long").build();
        }

        // ตรวจสอบชื่อผู้ใช้งานซ้ำ
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Username already exists.").build();
        }

        // Hash รหัสผ่านก่อนบันทึก
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        userRepository.persist(user);

        return Response.status(Response.Status.CREATED).entity("User created successfully").build();
    }



    // Login Endpoint
    @POST
    @Path("/login")
    public Response login(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser == null || !BCrypt.checkpw(user.getPassword(), existingUser.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid username or password").build();
        }

        // สร้าง JWT ใหม่
        String jwt = Jwt.issuer("quarkus")
                .upn(existingUser.getUsername())
                .sign();

        return Response.ok().entity(new LoginResponse(jwt)).build();
    }

    @GET
    @Path("/users")
    public List<User> getAllUsers() {
        return userRepository.listAll();
    }

    // เปลี่ยนรหัสผ่าน

    @PUT
    @Path("/change-password")
    @Authenticated
    public Response changePassword(UserPasswordRequest request, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        User existingUser = userRepository.findByUsername(username);

        if (existingUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }

        // ตรวจสอบว่า รหัสผ่านเก่าถูกต้องไหม
        if (!BCrypt.checkpw(request.getOldPassword(), existingUser.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Old password is incorrect").build();
        }

        // เปลี่ยนรหัสผ่าน
        existingUser.setPassword(BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt()));
        userRepository.persist(existingUser);

        // สร้าง JWT ใหม่หลังจากเปลี่ยนรหัสผ่าน
        String newJwt = Jwt.issuer("quarkus")
                .upn(existingUser.getUsername())
                .sign();

        // ส่งผลลัพธ์กลับพร้อมกับ JWT ใหม่
        return Response.ok(new LoginResponse(newJwt)).build();
    }

    public static class UserPasswordRequest {
        private String oldPassword;
        private String newPassword;

        // Getter และ Setter สำหรับ oldPassword
        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        // Getter และ Setter สำหรับ newPassword
        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }



    // Simple response class for returning JWT token
    public static class LoginResponse {
        public String token;

        public LoginResponse(String token) {
            this.token = token;
        }
    }
}
