package com.ibm.properties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jwt")
class JwtProperties {
    String signingKey
}