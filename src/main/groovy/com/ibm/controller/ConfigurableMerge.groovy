package com.ibm.controller

import com.ibm.Application
import com.ibm.service.ConfigService
import com.ibm.entity.Config
import com.ibm.entity.ConfigFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class ConfigurableMerge extends AbstractMerge {

    @Autowired
    ConfigService configService

    ResponseEntity<String> merge(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable('name') String name,
            @PathVariable('profile') String profile,
            @PathVariable('format') String format,
            @RequestParam(value='branch', required = false) String branch,
            @RequestParam(value='tag', required = false) String tag
            ) throws Exception {

        // Needs request to know its path
        // Based on path it will parse its settings on application.ctx
        // Do the merge accordingly

        branch = branch ?: 'master'

        def ctx = Application.instance.ctx as Map
        def endpointName = request.getRequestURI().split("/")[1]
        List profiles = profile.split(",")
        //List allProfiles = null

        Map endpoint = ctx.endpoints[endpointName] as Map
        List configs = []

        endpoint.merge.each { Map app ->
            String appName
            if (app.application == 'CURRENT') {
                appName = name
            }else {
                appName = app.application
            }
            List allProfiles = (app.profiles + profiles).unique {a,b -> a<=>b }
            String label = tag ?: branch
            configs +=  getConfigs(appName, allProfiles,label,false)
        }

        Config mergedConfig = getMergedConfig(configs, (format.toUpperCase() as ConfigFormat))
        response
        String str = mergedConfig.content

        //return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<String>(str, null, HttpStatus.OK)
    }


}
