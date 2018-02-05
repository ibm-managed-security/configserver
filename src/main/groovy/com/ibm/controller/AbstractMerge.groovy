package com.ibm.controller

import com.ibm.entity.Config
import com.ibm.entity.ConfigFormat
import com.ibm.utils.Deflatter
import com.ibm.utils.Flatter
import com.ibm.utils.Json
import com.ibm.utils.Merger
import com.ibm.utils.Yaml
import org.springframework.cloud.config.server.resource.NoSuchResourceException

import javax.servlet.http.HttpServletRequest

class AbstractMerge {
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

    private List<Config> getConfigs(String name, List profiles, String label) {
        return getConfigs(name, profiles, label, true)
    }

    private List<Config> getConfigs(String name, List profiles, String label, boolean addDefaultsAndSecrets) {
        if (addDefaultsAndSecrets) {
            profiles = getProfilesWithDefaultsAndSecrets(profiles)
        }

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

    String merge(HttpServletRequest request,
                 String name,
                 List profiles,
                 String label=null,
                 String format=null ) {
        List<Config> configs = getConfigs(name, profiles, label)
        Config mergedConfig = getMergedConfig(configs, ConfigFormat.valueOf(format.toUpperCase()))
        mergedConfig.content
    }

}
