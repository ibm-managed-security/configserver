package com.ibm.service

import com.ibm.entity.Config
import com.ibm.entity.ConfigFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cloud.config.server.resource.NoSuchResourceException
import org.springframework.cloud.config.server.resource.ResourceController
import org.springframework.stereotype.Component

@Component
class ConfigService {
    @Autowired
    ResourceController resourceController

    @Cacheable(cacheResolver = "cacheResolver")
    public Config get(String name, String profile, String label, ConfigFormat format) {
        try {
            String content = resourceController.retrieve(name, profile, label, profile + "." + format.name().toLowerCase(), false)
            return new Config(name, profile, label, format, content)

        } catch(NoSuchResourceException ex) {
            // Do nothing
            return null
        }
    }

    //@Scheduled(fixedDelay = 30000) // Could use this to evict on a schedule
    @CacheEvict(cacheResolver = "cacheResolver", allEntries=true)
    public void clearCache(String name, String profile, String label, ConfigFormat format) {
        // Intentionally left blank
    }

}
