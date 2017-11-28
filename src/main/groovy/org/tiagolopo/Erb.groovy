package org.tiagolopo

import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import org.tiagolopo.utils.ErbParser

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import groovy.json.JsonOutput

@RestController
class Erb {

    @Autowired
    Merge merge

    @Value('${server.port ?: 8080}')
    Integer serverPort
    String baseLocalUrl

    @ResponseBody
    @RequestMapping(value = "/template/{appName}/{profiles}/{erbFile}", method = RequestMethod.GET)
    void endPointTemplate(
            @PathVariable("appName") String appName,
            @PathVariable("erbFile") String erbFile,
            @PathVariable("profiles") String profiles,
            @RequestParam( value= 'label', required = false ) String label,
            HttpServletRequest request,
            HttpServletResponse response) {

        label = label?: 'master'

        def databag = getDataBag(appName,profiles,label)

        def template = getTemplate(label, erbFile)

        def parsed = ErbParser.parse(databag,template)
        response.getOutputStream().println(parsed)
    }

    private String jsonDump(obj){
        JsonOutput.prettyPrint(JsonOutput.toJson(obj))
    }

    String getDataBag (String appName, String profiles, String label) {
        def merged = merge.getMerged(appName,profiles,label)
        return jsonDump(merged)
    }

    String getTemplate (String label='master', String erbFile) {
        baseLocalUrl = "http://localhost:${serverPort}"
        String path = "/cfg/templates/default/${label}/${erbFile}.erb"
        RESTClient rc = new RESTClient(baseLocalUrl)
        println "Hitting: ${path}"
        def resp = rc.get(path: path)
        resp.data.text
    }
}
