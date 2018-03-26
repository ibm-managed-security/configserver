package com.ibm.auth

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
class BasicAuthConfig extends WebSecurityConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BasicAuthConfig.class)

    @Autowired
    AuthProperties authProperties;

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
        logger.info("Building user details service")
        List<UserDetails> users = new ArrayList<>()
        for (AuthUser user : authProperties.getUsers()) {
            logger.info("Adding user ${user.getUsername()}")
            users.add(User.withDefaultPasswordEncoder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRoles())
                    .build())
        }
        return new InMemoryUserDetailsManager(users)
    }
}