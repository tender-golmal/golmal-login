package com.golmal.login.handler

import com.golmal.data.domain.Role
import com.golmal.data.domain.UserProfile
import com.golmal.data.dto.ApiResponse
import com.golmal.data.dto.LoginRequest
import com.golmal.data.dto.LoginResponse
import com.golmal.data.dto.RegisterRequest
import com.golmal.login.service.UserClient
import com.golmal.login.util.Constant
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Function


@Component
class AuthHandler(
    @field:Autowired private val userClient: UserClient,
    @field:Autowired private val passwordEncoder: BCryptPasswordEncoder
) {
    @Autowired
    private val tokenProvider: TokenProvider? = null
    fun login(request: LoginRequest): Any =
        with(userClient.findUserByEmail(request.email)) {
            if (this != null && passwordEncoder.matches(request.password, this.password))
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON).body(
                        BodyInserters.fromValue<Any>(
                            LoginResponse(
                                tokenProvider!!.generateToken(this)
                            )
                        )
                    )
            else ServerResponse.badRequest()
                .body(BodyInserters.fromValue<Any>(ApiResponse(400, "Invalid credentials", "")))


        }


    fun signUp(request: RegisterRequest): Any = with(userClient.findUserByEmail(request.email)) {
        if (this != null) ServerResponse.badRequest()
            .body(BodyInserters.fromValue<Any>(ApiResponse(400, "User already exist", "")))
        else {
            request.password = passwordEncoder.encode(request.password)
            val savedUser = userClient.save(request)
            ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(savedUser))

        }
    }

}


@Component
class TokenProvider : Serializable {
    fun getUsernameFromToken(token: String?): String {
        return getClaimFromToken(token) { obj: Claims -> obj.subject }
    }

    fun getExpirationDateFromToken(token: String?): Date {
        return getClaimFromToken(
            token
        ) { obj: Claims -> obj.expiration }
    }

    fun <T> getClaimFromToken(token: String?, claimsResolver: Function<Claims, T>): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver.apply(claims)
    }

    fun getAllClaimsFromToken(token: String?): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(Constant.SIGNING_KEY)
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun isTokenExpired(token: String?): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    fun generateToken(user: UserProfile): String {
        val authorities = user.roles.map { obj: Role -> obj.role.name }
        return Jwts.builder()
            .setSubject(user.email)
            .claim(Constant.AUTHORITIES_KEY, authorities)
            .signWith(Keys.hmacShaKeyFor(Constant.SIGNING_KEY.toByteArray(StandardCharsets.UTF_8)))
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + Constant.ACCESS_TOKEN_VALIDITY_SECONDS * 1000))
            .compact()
    }
}