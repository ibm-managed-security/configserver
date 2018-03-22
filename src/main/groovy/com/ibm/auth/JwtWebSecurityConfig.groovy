package com.ibm.auth

import com.ibm.filter.ExceptionHandlerFilter
import com.ibm.filter.JwtFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.csrf.CsrfFilter

@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
class JwtWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    JwtProperties jwtProperties

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterAfter(new ExceptionHandlerFilter(), CsrfFilter.class)
            .addFilterAfter(new JwtFilter(jwtProperties), ExceptionHandlerFilter.class)
    }

}