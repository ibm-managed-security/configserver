package com.ibm.filter

import com.ibm.properties.JwtProperties
import io.jsonwebtoken.JwtException
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureException

class JwtFilter extends OncePerRequestFilter {

    JwtProperties jwtProperties

    JwtFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties
    }

    void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("authorization")

        if ("OPTIONS" == request.getMethod()) {
            response.setStatus(HttpServletResponse.SC_OK)
        } else {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new JwtException("Missing or invalid Authorization header")
            }

            String token = authHeader.substring(7)

            try {
                Claims claims = Jwts.parser().setSigningKey(jwtProperties.signingKey).parseClaimsJws(token).getBody()
                request.setAttribute("claims", claims)
            } catch (SignatureException e) {
                throw new JwtException("Invalid token")
            }
        }

        filterChain.doFilter(request, response)
    }

}