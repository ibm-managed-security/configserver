package com.ibm.service

import com.ibm.entity.Config
import com.ibm.entity.ConfigFactory
import com.ibm.entity.ConfigFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cloud.config.server.environment.EnvironmentRepository
import org.springframework.cloud.config.server.resource.ResourceController
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import org.springframework.util.StringUtils

import java.nio.charset.Charset

@Component
class ConfigService {
    @Autowired
    ResourceController resourceController

    @Autowired
    private MyResourceRepository myResourceRepository;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private ConfigFactory configFactory

    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class)


    @Cacheable(cacheResolver = "cacheResolver")
    synchronized List<Config> get(String name, String profiles, String label, ConfigFormat[] formats) {
        long t1 = System.currentTimeMillis()

        List<String> paths = []
        for (ConfigFormat f: formats) {
            for (String p: profiles.split(",")) {
                paths.push(p+"."+f.toString().toLowerCase())
            }
        }
        List<Config> configs = retrieve(name, profiles, label, (String[]) paths.toArray(String[]))
        logger.info("Attempting to fetch configs for ${name}:${profiles}:${label}:${formats} took ${System.currentTimeMillis()-t1}ms and resulted in ${configs.size()} configs")
        return configs
    }

    //@Scheduled(fixedDelay = 30000) // Could use this to evict on a schedule
    @CacheEvict(cacheResolver = "cacheResolver", allEntries=true)
    void clearCache(String name, String profile, String label, ConfigFormat[] formats) {
        // Intentionally left blank
    }

    synchronized List<Config> retrieve(String name, String profiles, String label, String[] paths) throws IOException {
        List<Config> configs = new ArrayList<>()
        List<Resource> resources = this.myResourceRepository.findMultiple(name, profiles, label, paths)
        for(Resource r: resources) {
            String extension = StringUtils.getFilenameExtension(r.getFilename())
            String profile = StringUtils.stripFilenameExtension(r.getFilename())
            ConfigFormat format = ConfigFormat.valueOf(extension.toUpperCase())
            configs.push(configFactory.createFromContent(name, profile, label, format, StreamUtils.copyToString(r.getInputStream(), Charset.forName("UTF-8"))))
        }
        return configs
    }

}
