package com.ibm.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ApplicationUsers.class)
class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    ApplicationUsers application;

    @Autowired
    private ApplicationBasicAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
            .csrf().disable()
    }

    @Bean
    @Override
    UserDetailsService userDetailsService() {
        List<UserDetails> users = new ArrayList<>()
        for (ApplicationUser user : application.getUsers()) {
            users.add(User.withDefaultPasswordEncoder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRoles())
                    .build())
        }
        return new InMemoryUserDetailsManager(users)
    }
}