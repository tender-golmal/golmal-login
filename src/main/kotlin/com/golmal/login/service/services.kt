package com.golmal.login.service

import com.golmal.data.domain.UserProfile
import com.golmal.data.dto.RegisterRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono

@FeignClient(name="golmal-repo")
interface UserClient{
    @GetMapping("/users/{email}")
    fun findUserByEmail(@PathVariable("email") email:String): UserProfile?

    @PostMapping("/users")
    fun save(@RequestBody userProfile: RegisterRequest):UserProfile
}