package org.tiagolopo

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import groovyx.net.http.RESTClient
import groovy.json.JsonOutput

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
        String host = "http://localhost:8080"
        String path = "/cfg/"+appName+ "/" + profile

        RESTClient rc = new RESTClient(host)
        def  resp = rc.get(path: path)
        def data = resp.data
        def result = null


        if ( data && data['propertySources'] && data['propertySources'].size > 1 ) {
            Map.metaClass.addNested = { Map rhs ->
                def lhs = delegate
                rhs.each { k, v -> lhs[k] = lhs[k] in Map ? lhs[k].addNested(v) : v }
                lhs
            }
            result = (data.propertySources[1].addNested(data.propertySources[0])).source
        }else if (data && data['propertySources']) {
            result = data.propertySources[0].source
        }

        println "DATA:\n${resp.data}"

        response.getOutputStream().println(JsonOutput.toJson(result ?: {} ) )
    }
}
