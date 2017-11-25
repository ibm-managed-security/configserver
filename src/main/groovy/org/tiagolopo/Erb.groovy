package org.tiagolopo

import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import org.jruby.embed.ScriptingContainer
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import org.tiagolopo.utils.Deflatter
import org.tiagolopo.utils.ErbParser
import org.tiagolopo.utils.Flatter
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class Erb {

    @Value('${server.port ?: 8080}')
    Integer serverPort
    String baseLocalUrl

    @ResponseBody
    @RequestMapping(value = "/erb", method = RequestMethod.GET)
    void erb(
            @RequestParam( value= 'format', required = false ) String format,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        response.getOutputStream().println(fromRuby())

    }

    private String fromRuby () {
        ErbParser.parse('{"foo":"bar"}','<%=node["foo"]%>')
    }

}
