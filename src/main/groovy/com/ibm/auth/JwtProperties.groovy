package com.ibm.auth
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jwt")
class JwtProperties {
    String signingKey
}