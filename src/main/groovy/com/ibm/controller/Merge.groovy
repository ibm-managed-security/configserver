package com.ibm.controller

import com.ibm.entity.Config
import com.ibm.entity.ConfigFactory
import com.ibm.entity.ConfigFormat
import com.ibm.service.TTLConfigService
import com.ibm.utils.Flatter
import com.ibm.utils.Json
import com.ibm.utils.Merger
import com.ibm.utils.ReferenceResolver
import com.ibm.utils.Yaml
import org.apache.commons.beanutils.PropertyUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.config.server.resource.NoSuchResourceException
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class Merge {

    private static final Logger logger = LoggerFactory.getLogger(Merge.class)


    @Autowired
    TTLConfigService configService

    @Autowired
    private ConfigFactory configFactory


    @RequestMapping(value = "/merge", method = RequestMethod.GET)
    @Secured(["ROLE_USER"])
    void mergeGet(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value='format', required = false, defaultValue = "json") String format,
            @RequestParam(value='tag', required = false) String tag,
            @RequestParam(value='branch', required = false) String branch,
            @RequestParam(value='path', required = false) String path
    ) {
        long t1 = System.currentTimeMillis()
        if (!branch && !tag) {
            throw new IllegalArgumentException("Either 'branch' or 'tag' are required.")
        }
        def label = branch ?: tag
        def reservedParams = ["format", "tag", "branch", "path"] as HashSet
        List<Config> paramConfigs = new ArrayList<>()
        request.getParameterNames().each { n ->
            if (!reservedParams.contains(n)) {
                paramConfigs.push(configFactory.create(n, request.getParameter(n), label, null, null, null))
            }
        }
        List<Config> configs = getConfigs(paramConfigs, label, true, branch != null)
        Config mergedConfig = getMergedConfig(configs, ConfigFormat.valueOf(format.toUpperCase()), path)
        response.getOutputStream().println(mergedConfig.content)
        logger.info("mergeGet took ${System.currentTimeMillis()-t1}ms")

    }

    @RequestMapping(value = "/merge", method = RequestMethod.POST)
    @Secured(["ROLE_USER"])
    void mergePost(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody List<Config> postedConfigs,
            @RequestParam(value='format', required = false, defaultValue = "json") String format,
            @RequestParam(value='tag', required = false) String tag,
            @RequestParam(value='branch', required = false) String branch,
            @RequestParam(value='path', required = false) String path
    ) {
        if (!branch && !tag) {
            throw new IllegalArgumentException("Either 'branch' or 'tag' are required.")
        }
        def label = branch ?: tag
        List<Config> configs = getConfigs(postedConfigs, label, true, branch != null)
        Config mergedConfig = getMergedConfig(configs, ConfigFormat.valueOf(format.toUpperCase()), path)
        response.getOutputStream().println(mergedConfig.content)
    }

    private Config getMergedConfig(List<Config> configs, ConfigFormat outputFormat, String path) {
        long t1 = System.currentTimeMillis()
        def merged = Merger.deepMerge(*(configs.collect{it.object}))
        logger.info("Deep merge took ${System.currentTimeMillis()-t1}ms")
        t1 = System.currentTimeMillis()
        merged = ReferenceResolver.resolve(merged)
        logger.info("Reference resolver took ${System.currentTimeMillis()-t1}ms")
        if (outputFormat == ConfigFormat.VALUE && !path) {
            throw new IllegalArgumentException("A 'path' parameter is required for the 'value' format")
        } else if (path) {
            merged = PropertyUtils.getNestedProperty(merged, path)
        }
        t1 = System.currentTimeMillis()
        Config config = configFactory.createFromObject(configs[0].name, "<merged ${configs.collect{it.profile}.join(",")}>", configs[0].label, outputFormat, merged)
        logger.info("Config create took ${System.currentTimeMillis()-t1}ms")
        return config
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
        long t1 = System.currentTimeMillis()
        List<Config> returnConfigs = new ArrayList<Config>()
        configs.each {
            try {
                returnConfigs.addAll(getConfigs(it.name, [it.profile], label, includeDefaultsAndSecrets, clearCache))
            } catch (NoSuchResourceException ex) {
                // Do nothing
            }
        }
        if (configs.size() == 0) {
            throw new NoSuchResourceException("Unable to get configs matching: "+configs.join(", "))
        }
        logger.info("Get all configs took ${System.currentTimeMillis()-t1}ms")
        returnConfigs
    }

    private List<Config> getConfigs(String name, List profiles, String label, boolean includeDefaultsAndSecrets, boolean clearCache) {
        if (includeDefaultsAndSecrets) {
            profiles = getProfilesWithDefaultsAndSecrets(profiles)
        }

        List<Config> configs = new ArrayList<Config>()
        if (clearCache) {
            configService.clearCacheWithTTL(name, profiles.join(","), label, ConfigFormat.values())
        }
        long t1 = System.currentTimeMillis()
        List<Config> newConfigs = configService.get(name, profiles.join(","), label, ConfigFormat.values())
        logger.info("Get configs ${name}:${profiles.join(",")}:${label} took ${System.currentTimeMillis()-t1}ms")

        if (newConfigs) {
            configs.addAll(newConfigs)
        }
        if (configs.size() == 0) {
            throw new NoSuchResourceException("Unable to get configs for name: ${name} profile: ${profiles.join(",")} label: ${label}")
        }
        configs
    }




}
