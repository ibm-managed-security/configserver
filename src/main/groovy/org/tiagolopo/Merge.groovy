package org.tiagolopo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.config.server.environment.EnvironmentController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.yaml.snakeyaml.DumperOptions

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import groovy.json.JsonOutput
import org.tiagolopo.utils.Deflatter
import org.tiagolopo.utils.Flatter
import org.yaml.snakeyaml.Yaml

@RestController
class Merge {

    @Autowired
    EnvironmentController environmentController

    @Value('${server.port ?: 8080}')
    Integer serverPort
    String baseLocalUrl

    @ResponseBody
    @RequestMapping(value = "/merge/{appName}/{profile}/{label:.+}", method = RequestMethod.GET)
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


        def result = getMerged(appName, profile, label)


        result = convertTo(result, format)
        response.getOutputStream().println(result)
    }

    Object getMerged (String appName,  String profile, String label) {
        def result = getConfigFromController(appName, getDefaultProfiles(profile), label)
        result = deepMerge( *(result.reverse()) )
        return result
    }

    private Map deepMerge(Map onto, Map... overrides) {
        if (!overrides)
            return onto
        else if (overrides.length == 1) {
            overrides[0]?.each { k, v ->
                if (v instanceof Map && onto[k] instanceof Map)
                    deepMerge((Map) onto[k], (Map) v)
                else
                    onto[k] = v
            }
            return onto
        }
        return overrides.inject(onto, { acc, override -> deepMerge(acc, override ?: [:]) })
    }

    private String getDefaultProfiles ( String profile) {
        LinkedList list = profile.split(',')
        list = list as Queue

        LinkedList result = ['default', 'default-secret' ]

        list[0] == 'default' && { list.poll() }()
        list[0] == 'default-secret' && { list.poll() }()
        list.each { e ->
            result.push e
            result.push "${e}-secret"
        }
        result.join(',')
    }


    private getConfigFromController (appName, profile, label) {
        def env = environmentController.labelled(appName,profile,label)
        def result = []
            env.propertySources.each {
                def obj = it.source
                obj = new Flatter().flat(obj)
                obj = new Deflatter(obj).deflat()
                result.push obj
            }
        return result
    }

    private String convertTo (Object obj, String format){
        if (!obj) { return '{}' }
        format = format ?: 'json'
        if (format.toLowerCase() == 'yaml' || format.toLowerCase() == 'yml' )  {
            return yamlDump(obj)
        }else if (format.toLowerCase() == 'properties' ) {
            return new Flatter().flat(obj)
        }else {
            return jsonDump(obj)
        }
    }

    private String jsonDump(obj){
        JsonOutput.prettyPrint(JsonOutput.toJson(obj))
    }

    private String yamlDump(obj){
        DumperOptions options = new DumperOptions()
        options.explicitStart = true
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        new Yaml(options).dump(obj)
    }
}
