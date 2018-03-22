package com.ibm.security

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.stereotype.Component

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
public class ApplicationBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Override
    public void commence (HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
    throws IOException, ServletException {
        response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName() + "");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.println("HTTP Status 401 - " + authEx.getMessage());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setRealmName("configserver");
        super.afterPropertiesSet();
    }
}