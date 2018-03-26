package com.ibm.auth

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties
class AuthProperties {
    private static final Logger logger = LoggerFactory.getLogger(AuthProperties.class)

    private List<AuthUser> users = new ArrayList<>();

    List<AuthUser> getUsers() {
        return this.users;
    }

    void setUsers(List<AuthUser> users) {
        this.users = users
    }

    @Bean
    static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        try {
            YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean()
            Resource authYmlResource = new ClassPathResource("auth.yml")
            String authYmlPath = "/etc/configserver/auth.yml"
            if (new File(authYmlPath)) {
                authYmlResource = new FileSystemResource(authYmlPath)
            }
            logger.info("Using ${authYmlResource.getPath()} for authentication configuration")
            yaml.setResources(authYmlResource)
            propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject())
        } catch (IllegalStateException e) {
            // do nothing
        } catch (Exception e) {
            e.printStackTrace()
        }
        return propertySourcesPlaceholderConfigurer;
    }

}