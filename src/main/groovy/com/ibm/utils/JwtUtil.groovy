package com.ibm.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm

class JwtUtil {
    static String create(String subject, String[] roles, String secretKey) {
        return Jwts.builder().setSubject(subject).claim("roles", roles.join(",")).setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }

    static Claims parse(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody()
    }

    public static void main(String[] args) {
        def secretKey = "zTh8bPAEX_FJnF7Gc&F-xT+s87GHFHBQ"
        def token = JwtUtil.create("apiuser", ['user'] as String[], secretKey)
        println token
        println parse(token, secretKey)
    }
}
