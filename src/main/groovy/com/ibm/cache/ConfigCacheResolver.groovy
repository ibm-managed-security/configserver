package com.ibm.cache

import com.ibm.service.ConfigService
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.CacheOperationInvocationContext
import org.springframework.cache.interceptor.CacheResolver

public class ConfigCacheResolver implements CacheResolver {

    private final CacheManager cacheManager;

    public ConfigCacheResolver(CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    @Override
    /**
     * Resolves a custom cache name so we can support precise clearing of caches vs. clearing everything.
     * Questionable efficiency as it creates a cache for each value... but for our purposes this may be just
     * fine to reduce the amount of code we have to write. Sadly the CacheEvict annotation doesn't support
     * evicting based on key patterns.
     */
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<Cache> caches = new ArrayList<>();
        if(context.getTarget().getClass() == ConfigService.class){
            if(context.getMethod().getName().equals("get") || context.getMethod().getName().equals("clearCache")){
                String cacheName = "config"
                context.getArgs().each { p ->
                    cacheName += "."+p.toString()
                }
                caches.add(cacheManager.getCache(cacheName));
            }
        }
        return caches;
    }
}