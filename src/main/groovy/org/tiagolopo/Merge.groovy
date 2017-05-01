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

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@RestController
class Merge {

    @Value('${server.port ?: 8080}')
    Integer serverPort
    String baseLocalUrl

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

        def result

        ExecutorService es = Executors.newFixedThreadPool(10)
        LinkedList profiles = profile.split(',')
        LinkedList cfgList
        LinkedList taskList = []

        if ( profiles[0] != 'default' ){
            profiles.addFirst('default')
        }

        profiles.each { p ->
            taskList.push ( { getConfig(appName, p, label, response) } as Callable)
            taskList.push ( { getConfig(appName, "${p}-secret", label, response) } as Callable)
        }

        cfgList = es.invokeAll(taskList)
        es.shutdown()

        result = deepMerge( *(cfgList.collect{it.get()}) )
        result = convertTo(result, format)
        response.getOutputStream().println(result)
    }


    private Map deepMerge (Map... maps) {
        Map result

        if (maps.length == 0) {
            result = [:]
        } else if (maps.length == 1) {
            result = maps[0]
        } else {
            result = [:]
            maps.each { map ->
                map.each { k, v ->
                    result[k] = result[k] instanceof Map ? deepMerge(result[k], v) : v
                }
            }
        }
        result
    }

    private getConfig (appName, profile, label , HttpServletResponse response){
        baseLocalUrl = "http://localhost:${serverPort}"
        String path = "/cfg/${appName}/${profile}"
        path += label ? "/${label}" : ''

        RESTClient rc = new RESTClient(baseLocalUrl)
        def resp
        try {
            println "Hitting: ${path}"
            resp = rc.get(path: path)
        }catch(ex){
            println "Failed to hit ${baseLocalUrl}/${path}"
            if(ex.response) {
                response.setStatus(ex.response.status)
                response.getOutputStream().println( JsonOutput.toJson(ex.response.data) )
            }
            ex.printStackTrace()
            return
        }

        def data = resp ? resp.data : null
        if (data && data.propertySources) {
            return data.propertySources[0].source
        }
        return {}
    }

    private String convertTo (Object obj, String format){
        if (!obj) { return '{}' }
        format = format ?: 'json'
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
