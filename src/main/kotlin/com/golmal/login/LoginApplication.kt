package com.golmal.login

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication(
    exclude = [MongoAutoConfiguration::class, MongoDataAutoConfiguration::class, WebMvcAutoConfiguration::class]
)
@EnableFeignClients
@EnableEurekaClient
class LoginApplication

fun main(args: Array<String>) {
    runApplication<LoginApplication>(*args)
}
