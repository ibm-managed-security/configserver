package com.ibm

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class ConfigurableMerge {

    static ResponseEntity<String> merge(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable('name') String name,
            @PathVariable('profile') String profile,
            @PathVariable('format') String format
    ) throws Exception {
        // Needs request to know its path
        // Based on path it will parse its settings on application.ctx
        // Do the merge accordingly

        def ctx = Application.instance.ctx as Map
        def endpointName = request.getRequestURI().split("/")[1]

        def endpoint = ctx.endpoints[endpointName]
        response
        String str = """
            name ${name}
            profile ${profile}
            format ${format}
            endpoint  ${endpoint}
        """

        //return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<String>(str, null, HttpStatus.OK)
    }

}
