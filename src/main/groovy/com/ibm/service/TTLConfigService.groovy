package com.ibm.service

import com.ibm.entity.Config
import com.ibm.entity.ConfigFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
/*
We need to cache all calls for at least a few seconds to ensure that hundreds of parallel calls
for the same set of resources don't incur Git repository fetch costs. 15 seconds seems a reasonable
TTL to start with but we could make it configurable.

Could not embed this logic in the ConfigService because Spring Caching doesn't work with calls
made to cached methods in the cacheable class itself.
*/
class TTLConfigService {
    @Autowired
    ConfigService configService

    private static final Logger logger = LoggerFactory.getLogger(TTLConfigService.class)

    private static Map<String, Long> lastCacheClears = new HashMap<>()
    private static int ttlSeconds = 15

    String getCacheClearKey(String name, String profile, String label, ConfigFormat[] formats) {
        return name + profile + label + formats.join("_")
    }

    // Saves from a situation where hundreds of clients are asking for the same uncached config at once
    void clearCacheWithTTL(String name, String profile, String label, ConfigFormat[] formats) {
        long current = System.currentTimeMillis()
        Long lastCacheClear = lastCacheClears.get(getCacheClearKey(name, profile, label, formats))
        if (!lastCacheClear) {
            // Init to zero the first time so we clear the cache and properly set the lastCacheClear value
            lastCacheClear = 0
        }

        if (!lastCacheClear || current > lastCacheClear + ttlSeconds * 1000) {
            lastCacheClears.put(getCacheClearKey(name, profile, label, formats), current)
            configService.clearCache(name, profile, label, formats)
        }
    }

    synchronized List<Config> get(String name, String profiles, String label, ConfigFormat[] formats) {
        return configService.get(name, profiles, label, formats)
    }


    synchronized List<Config> retrieve(String name, String profiles, String label, String[] paths) throws IOException {
        return configService.retrieve(name, profiles, label, paths)
    }

}