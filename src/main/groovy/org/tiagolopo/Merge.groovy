package org.tiagolopo

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
class Merge {

    @ResponseBody
    @RequestMapping(value = "/merge/{appName}/{profile}/{label}", method = RequestMethod.GET)
    void merge(
            @PathVariable("appName") String appName,
            @PathVariable("profile") String profile,
            @PathVariable("label") String label,
            HttpServletRequest request,
            HttpServletResponse response) {

        doSomething(response,appName,profile,label)
    }

    @ResponseBody
    @RequestMapping(value = "/merge/{appName}/{profile}", method = RequestMethod.GET)
    void merge(
            @PathVariable("appName") String appName,
            @PathVariable("profile") String profile,
            HttpServletRequest request,
            HttpServletResponse response) {

        doSomething(response,appName,profile)
    }

    void doSomething ( HttpServletResponse response, String appName, String profile, String label=null ) {
        def result = new URL("http://localhost:8080/cfg/"+appName+ "/" + profile).text
        response.getOutputStream().println(result)
        response.getOutputStream().println('abc')
    }
}
