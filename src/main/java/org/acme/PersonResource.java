package org.acme;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    @Inject
    PersonRepository personRepository;

    // GET: ดึงข้อมูลทั้งหมด
    @GET
    public List<Person> getAllPersons() {
        return personRepository.listAll();
    }

    // GET: ดึงข้อมูลตาม ID
    @GET
    @Path("/{id}")
    public Response getPersonById(@PathParam("id") Long id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            // 404 Not Found
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Person with ID " + id + " not found")
                           .build();
        }
        return Response.ok(person).build();
    }

    // POST: เพิ่มข้อมูลใหม่
    @POST
    @Transactional
    public Response addPerson(Person person) {
        if (person.getName() == null || person.getName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Name cannot be null or empty")
                    .build();
        }
        if (person.getAge() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Age cannot be null")
                    .build();
        }
        if (personRepository.find("name", person.getName()).firstResult() != null) {
            // 409 Conflict: ข้อมูลซ้ำ
            return Response.status(Response.Status.CONFLICT)
                    .entity("Person with name " + person.getName() + " already exists.")
                    .build();
        }
        personRepository.persist(person);
        return Response.status(Response.Status.CREATED).entity(person).build();
    }

    // PUT: แก้ไขข้อมูลตาม ID
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updatePerson(@PathParam("id") Long id, Person person) {
        if (person.getName() == null || person.getName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Name cannot be null or empty")
                    .build();
        }
        if (person.getAge() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Age cannot be null")
                    .build();
        }
        Person existingPerson = personRepository.findById(id);
        if (existingPerson == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Person with ID " + id + " not found")
                    .build();
        }
        existingPerson.setName(person.getName());
        existingPerson.setAge(person.getAge());
        personRepository.persist(existingPerson);
        return Response.ok(existingPerson).build();
    }


    // DELETE: ลบข้อมูลตาม ID
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deletePerson(@PathParam("id") Long id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            // 404 Not Found
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Person with ID " + id + " not found")
                           .build();
        }
        personRepository.delete(person);
        return Response.noContent().build();
    }

    // ค้นหาผู้ใช้งานโดยชื่อ
    @GET
    @Path("/search")
    public Response searchPersonByName(@QueryParam("name") String name) {
        List<Person> persons = personRepository.find("name", name).list();
        if (persons.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                        .entity("No person found with name " + name)
                        .build();
        }
        return Response.ok(persons).build();
    }

    // ฟิลเตอร์ด้วยอายุ
    @GET
    @Path("/age")
    public Response filterByAge(@QueryParam("age") Integer age) {
        List<Person> persons = personRepository.find("age = ?1", age).list();
        if (persons.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                        .entity("No person found with age " + age)
                        .build();
        }
        return Response.ok(persons).build();
    }
}

/*

 1. 400 Bad Request ในกรณี name, age เป็น null
 2. Authentication, Authorization ผ่าน JWT ไหมในกรณีที่ "POST/GET ไปยังหน้าที่ไม่ได้รับอนุญาต"
 
 */
