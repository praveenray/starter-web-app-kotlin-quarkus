package my.starter.controllers

import jakarta.ws.rs.FormParam
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriBuilder
import my.starter.jooq.enums.UserGender
import my.starter.libs.Err
import my.starter.libs.FMUtils
import my.starter.libs.Ok
import my.starter.services.UserService

@Path("/users")
class UserController(
    private val fmUtils: FMUtils,
    private val userService: UserService,
) {
    @Path("/")
    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): String {
        val users = userService.getUsers()
        return fmUtils.render(
            "/users/list.ftl", mapOf(
                "users" to users
            )
        )
    }

    @GET
    @Path("/new-user")
    @Produces(MediaType.TEXT_HTML)
    fun newUser(): String {
        return fmUtils.render(
            "/users/new-user.ftl", mapOf(
                "record" to mapOf(
                    "fullName" to "",
                    "age" to 0,
                    "email" to "",
                    "gender" to ""
                )
            )
        )
    }

    @POST
    @Path("/new-user")
    @Produces(MediaType.TEXT_HTML)
    fun newUserNow(
        @FormParam("fullName") fullName: String?,
        @FormParam("email") email: String?,
        @FormParam("gender") gender: String?,
        @FormParam("age") age: String?,
    ): Response {
        val record = mapOf(
            "fullName" to fullName,
            "age" to age,
            "email" to email,
            "gender" to gender,
        )

        return (when (val result = userService.createNewUser(record)) {
            is Err<Map<String,String?>> -> {
                Response.serverError().entity(fmUtils.render("/users/new-user.ftl", mapOf(
                    "record" to record,
                    "errorMap" to result.error
                )))
            }
            is Ok<Int> -> {
                Response.seeOther(UriBuilder.fromPath("/users").build())
            }
        }).build()
    }

}