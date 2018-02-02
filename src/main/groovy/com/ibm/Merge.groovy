package com.ibm

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.config.server.resource.NoSuchResourceException
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class Merge {

    @Autowired
    ConfigService configService

    @ResponseBody
    @RequestMapping(value = "/merge/{name}/{profile}.{format}", method = RequestMethod.GET)
    void merge(
            @PathVariable('name') String name,
            @PathVariable('profile') String profile,
            @PathVariable('format') String format,
//            @RequestParam(value='tag', required = false) String tag,
//            @RequestParam(value='branch', required = false) String branch,
            @RequestParam(value='label', required = false) String label,
            HttpServletRequest request,
            HttpServletResponse response) {
        response.getOutputStream().println(merge(request,name,Arrays.asList(profile.split(",")),label,format))
    }

    String merge(HttpServletRequest request,
                 String name,
                 List profiles,
                 String label=null,
                 String format=null ) {
        List<Config> configs = getConfigs(name, profiles, label)
        Config mergedConfig = getMergedConfig(configs, ConfigFormat.valueOf(format.toUpperCase()))
        mergedConfig.content
    }

    /**
     * Returns a list of Strings corresponding to the multiple resources requested
     * @param name
     * @param profiles
     * @param label
     * @return
     */
    private List<Config> getConfigs(String name, List profiles, String label) {
        profiles = getProfilesWithDefaultsAndSecrets(profiles)

        List<Config> configs = new ArrayList<Config>()
        profiles.each { profile ->
            for (ConfigFormat format : ConfigFormat.values()) {
                Config config = configService.get(name, profile, label, format)
                if (config) {
                    configs.push(config)
                    break
                }
            }
        }
        if (configs.size() == 0) {
            throw new NoSuchResourceException("Unable to get configs for name: ${name} profile: ${profile} label: ${label}")
        }
        configs
    }

    private Config getMergedConfig(List<Config> configs, ConfigFormat outputFormat) {
        Map merged = deepMerge(*(configs.collect{getMapFromConfig(it)}.reverse()))
        String content = null
        switch(outputFormat) {
            case ConfigFormat.YML:
            case ConfigFormat.YAML:
                content = yamlDump(merged)
            default:
                break
        }
        return new Config(configs[0].name, "<merged ${configs.collect{it.profile}.join(",")}>", configs[0].label, outputFormat, content)
    }

    private Map getMapFromConfig(Config config) {
        Map map = null
        switch (config.format){
            case ConfigFormat.YAML:
            case ConfigFormat.YML:
                map = getMapFromYaml(config.content)
        }
        map
    }

    private Map getMapFromYaml(String yaml) {
        new Yaml().load(yaml)
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

    private String yamlDump(obj){
        DumperOptions options = new DumperOptions()
        options.explicitStart = true
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        new Yaml(options).dump(obj)
    }
}