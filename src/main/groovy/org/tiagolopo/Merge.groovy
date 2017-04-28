package org.tiagolopo

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.yaml.snakeyaml.DumperOptions

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import groovyx.net.http.RESTClient
import groovy.json.JsonOutput
import org.tiagolopo.utils.Deflatter
import org.tiagolopo.utils.Flatter
import org.yaml.snakeyaml.Yaml

@RestController
class Merge {

    @Value('${server.port ?: 8080}')
    Integer serverPort

    @ResponseBody
    @RequestMapping(value = "/merge/{appName}/{profile}/{label}", method = RequestMethod.GET)
    void merge(
            @PathVariable('appName') String appName,
            @PathVariable('profile') String profile,
            @PathVariable('label') String label,
            @RequestParam( value= 'format', required = false ) String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        merge(response,appName,profile,label,format)
    }

    @ResponseBody
    @RequestMapping(value = "/merge/{appName}/{profile}", method = RequestMethod.GET)
    void merge(
            @PathVariable("appName") String appName,
            @PathVariable("profile") String profile,
            @RequestParam( value= 'format', required = false ) String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        merge(response,appName,profile,null,format)
    }

    void merge (
            HttpServletResponse response,
            String appName, String profile,
            String label=null,
            String format=null ) {

        String host = "http://localhost:${serverPort}"
        String path = "/cfg/${appName}/${profile}"
        path += label ? "/${label}" : ''

        format = format ?: 'json'
        RESTClient rc = new RESTClient(host)
        def resp
        try {
            resp = rc.get(path: path)
        }catch(ex){
            println "Failed to hit ${path}"
            response.setStatus(ex.response.status)
            response.getOutputStream().println( JsonOutput.toJson(ex.response.data) )
            ex.printStackTrace()
            return
        }

        def data = resp ? resp.data : null
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

        result = convertTo(result, format)

        response.getOutputStream().println(result)
    }

    private String convertTo (Object obj, String format){
        if (!obj) { return '{}' }
        def result = new Flatter().flat(obj)
        if ( format.toLowerCase() == 'json' ) {
            result = new Deflatter(result).deflat()
            result = JsonOutput.toJson(result)
        }else if (format.toLowerCase() == 'yaml' || format.toLowerCase() == 'yml' )  {
            result = new Deflatter(result).deflat()
            DumperOptions options = new DumperOptions()
            options.explicitStart = true
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
            result = new Yaml(options).dump(result)
        }
        result
    }
}
