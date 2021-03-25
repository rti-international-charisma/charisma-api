package com.rti.charisma.api.route

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rti.charisma.api.service.UserService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post

data class Signup(val username: String, val password: String, val secQuestionId: Int, val secQuestionAnswer: String)
data class Login (val username: String, val password: String)

@KtorExperimentalLocationsAPI
fun Routing.userRoute(userService: UserService) {

    post("/signup") {
        val signupModel = jacksonObjectMapper().readValue<Signup>(call.receiveText())
        userService.registerUser(signupModel)
        call.respond(HttpStatusCode.OK, "User registered ")
    }

    get("/securityquestions") {
        call.respond(HttpStatusCode.OK, userService.getSecurityQuestions(call.request.queryParameters["id"]?.toInt()))
    }
}