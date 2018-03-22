package com.ibm.auth

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


//@EnableWebSecurity
// Disabled until discussion with Tiago
class ConfigAuth extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Require role dev for .*-dev.*
        // Require role prod for .*-prod.*
        http.csrf().disable().authorizeRequests()
                .regexMatchers(".*-prod.*").hasRole("prod")
                .regexMatchers(".*-dev.*").hasRole("dev")
                .anyRequest()
                .fullyAuthenticated()
                .and()
                .httpBasic()
        .and().authorizeRequests()
                .anyRequest()
                .permitAll()
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("prod").password("{noop}prod").roles("prod")
        .and()
                .withUser("dev").password("{noop}dev").roles("dev")
    }
}
