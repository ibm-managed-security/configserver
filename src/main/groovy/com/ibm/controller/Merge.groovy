package com.ibm.controller

import com.ibm.Application
import com.ibm.entity.Config
import com.ibm.entity.ConfigFormat
import com.ibm.service.ConfigService
import com.ibm.utils.Deflatter
import com.ibm.utils.Flatter
import com.ibm.utils.Json
import com.ibm.utils.Merger
import com.ibm.utils.Yaml
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.config.server.resource.NoSuchResourceException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class Merge {

    @Autowired
    ConfigService configService

    @RequestMapping(value = "/merge/{name}/{profile}", method = RequestMethod.GET)
    void merge(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable('name') String name,
            @PathVariable('profile') String profile,
            @RequestParam(value='format', required = false, defaultValue = "json") String format,
            @RequestParam(value='tag', required = false) String tag,
            @RequestParam(value='branch', required = false) String branch
    ) {
        def label = branch ?: tag
        List<Config> configs = getConfigs(name, Arrays.asList(profile.split(",")), label, true, branch != null)
        Config mergedConfig = getMergedConfig(configs, ConfigFormat.valueOf(format.toUpperCase()))
        response.getOutputStream().println(mergedConfig.content)
    }

    @RequestMapping(value = "/merge", method = RequestMethod.POST)
    void merge(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody List<Config> postedConfigs,
            @RequestParam(value='format', required = false, defaultValue = "json") String format,
            @RequestParam(value='tag', required = false) String tag,
            @RequestParam(value='branch', required = false) String branch
    ) {
        def label = branch ?: tag
        List<Config> configs = getConfigs(postedConfigs, label, true, branch != null)
        Config mergedConfig = getMergedConfig(configs, ConfigFormat.valueOf(format.toUpperCase()))
        response.getOutputStream().println(mergedConfig.content)
    }

    ResponseEntity<String> configurableMerge(
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


    private Config getMergedConfig(List<Config> configs, ConfigFormat outputFormat) {
        Map merged = Merger.deepMerge(*(configs.collect{getMapFromConfig(it)}.reverse()))
        String content = null
        switch(outputFormat) {
            case ConfigFormat.JSON:
                content = Json.dump(merged)
                break
            case ConfigFormat.PROPERTIES:
                content = new Flatter().flat(merged)
                break
            case ConfigFormat.YML:
            case ConfigFormat.YAML:
                content = Yaml.dump(merged)
            default:
                break
        }
        return new Config(configs[0].name, "<merged ${configs.collect{it.profile}.join(",")}>", configs[0].label, outputFormat, content)
    }

    private Map getMapFromConfig(Config config) {
        Map map = null
        switch (config.format){
            case ConfigFormat.PROPERTIES:
                Properties p = new Properties()
                p.load(new StringReader(config.content))

                // We have to load the dump because it still behaves as properties
                def obj = Json.load( Json.dump(p as Map))

                String flat = new Flatter().flat(obj)
                map = new Deflatter(flat).deflat()
                break
            case ConfigFormat.JSON:
                map = Json.load(config.content)
                break
            case ConfigFormat.YAML:
            case ConfigFormat.YML:
                map = Yaml.load(config.content)
                break
        }
        map
    }

    /**
     * Populate a given list of profiles with default profiles and -secret suffixes
     * following our convention.
     * @param profiles
     * @return
     */
    private List<String> getProfilesWithDefaultsAndSecrets(List<String> profiles) {
        LinkedList list = new LinkedList(profiles)
        list = list as Queue

        LinkedList result = ['default', 'default-secret' ]

        list[0] == 'default' && { list.poll() }()
        list[0] == 'default-secret' && { list.poll() }()
        list.each { e ->
            result.push e
            result.push "${e}-secret"
        }
        return result
    }

    private List<Config> getConfigs(List<Config> configs, String label, boolean includeDefaultsAndSecrets, boolean clearCache) {
        List<Config> returnConfigs = new ArrayList<Config>()
        configs.each {
            returnConfigs.addAll(getConfigs(it.name, [it.profile], label, includeDefaultsAndSecrets, clearCache))
        }
        returnConfigs
    }

    private List<Config> getConfigs(String name, List profiles, String label, boolean includeDefaultsAndSecrets, boolean clearCache) {
        if (includeDefaultsAndSecrets) {
            profiles = getProfilesWithDefaultsAndSecrets(profiles)
        }

        List<Config> configs = new ArrayList<Config>()
        profiles.each { profile ->
            for (ConfigFormat format : ConfigFormat.values()) {
                if (clearCache) {
                    configService.clearCache(name, profile, label, format)
                }
                Config config = configService.get(name, profile, label, format)
                if (config) {
                    configs.push(config)
                    break
                }
            }
        }
        if (configs.size() == 0) {
            throw new NoSuchResourceException("Unable to get configs for name: ${name} profile: ${profiles.join(",")} label: ${label}")
        }
        configs
    }




}
