package com.ibm.filter

import io.jsonwebtoken.JwtException
import org.springframework.http.HttpStatus
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(response.getStatus()+": "+e.getMessage());
        } catch (RuntimeException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write(response.getStatus()+": "+e.getMessage());
        }
    }
}
