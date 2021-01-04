package com.golmal.login.controller

import com.golmal.data.domain.UserProfile
import com.golmal.data.dto.LoginRequest
import com.golmal.data.dto.RegisterRequest
import com.golmal.login.handler.AuthHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class LoginController {
    @Autowired
    private val authHandler: AuthHandler?=null

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): Mono<Any> = Mono.just(authHandler!!.login(request))

    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: RegisterRequest): Mono<Any> = Mono.just(authHandler!!.signUp(request))

}