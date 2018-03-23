package com.ibm.properties
class ApplicationUser {
    private String username;
    private String password;
    private String[] roles;

    String getUsername() {
        return username
    }

    void setUsername(String username) {
        this.username = username
    }

    String getPassword() {
        return password
    }

    void setPassword(String password) {
        this.password = password
    }

    String[] getRoles() {
        return roles
    }

    void setRoles(String[] roles) {
        this.roles = roles
    }
}