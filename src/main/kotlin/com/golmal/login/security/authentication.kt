package com.golmal.login.security

import com.golmal.login.handler.TokenProvider
import com.golmal.login.util.Constant
import io.jsonwebtoken.Claims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class AuthenticatedUser(private val userId: String, private val roles: List<SimpleGrantedAuthority>) :
    Authentication {
    private var authenticated = true
    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return roles
    }

    override fun getCredentials(): Any {
        return userId
    }

    override fun getDetails(): Any? {
        return null
    }

    override fun getPrincipal(): Any {
        return userId
    }

    override fun isAuthenticated(): Boolean {
        return authenticated
    }

    @Throws(IllegalArgumentException::class)
    override fun setAuthenticated(b: Boolean) {
        authenticated = b
    }

    override fun getName(): String {
        return userId
    }

    companion object {
        private const val serialVersionUID = 6861381095901879822L
    }
}

@Component
class AuthenticationManager : ReactiveAuthenticationManager {
    @Autowired
    private val tokenProvider: TokenProvider? = null
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.credentials.toString()
        val username: String? = try {
            tokenProvider!!.getUsernameFromToken(authToken)
        } catch (e: Exception) {
            null
        }
        return if (username != null && !tokenProvider!!.isTokenExpired(authToken)) {
            val claims: Claims = tokenProvider.getAllClaimsFromToken(authToken)
            val roles = claims.get(Constant.AUTHORITIES_KEY, List::class.java)
            val authorities = roles!!.map {
                SimpleGrantedAuthority(
                    it.toString()
                )
            }
            val auth = UsernamePasswordAuthenticationToken(username, username, authorities)
            SecurityContextHolder.getContext().authentication =
                AuthenticatedUser(username, authorities)
            Mono.just(auth)
        } else {
            Mono.empty()
        }
    }
}

@Component
class SecurityContextRepository : ServerSecurityContextRepository {
    @Autowired
    private val authenticationManager: AuthenticationManager? = null
    override fun save(swe: ServerWebExchange, sc: SecurityContext): Mono<Void> {
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun load(swe: ServerWebExchange): Mono<SecurityContext> {
        val request = swe.request
        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        var authToken: String? = null
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            authToken = authHeader.replace(TOKEN_PREFIX, "")
        } else {
            logger.warn("couldn't find bearer string, will ignore the header.")
        }
        return if (authToken != null) {
            val auth: Authentication = UsernamePasswordAuthenticationToken(authToken, authToken)
            authenticationManager!!.authenticate(auth).map { authentication: Authentication? ->
                SecurityContextImpl(
                    authentication
                )
            }
        } else {
            Mono.empty()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SecurityContextRepository::class.java)
        private const val TOKEN_PREFIX = "Bearer "
    }
}