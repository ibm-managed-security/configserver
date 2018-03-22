package com.ibm.security
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application")
class ApplicationUsers {

    private List<ApplicationUser> users = new ArrayList<>();

    List<ApplicationUser> getUsers() {
        return this.users;
    }

    void setUsers(List<ApplicationUser> users) {
        this.users = users
    }

}