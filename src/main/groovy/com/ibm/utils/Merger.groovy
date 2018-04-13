package com.ibm.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Merger {

    private static final Logger logger = LoggerFactory.getLogger(Merger.class)

    static Map deepMerge(Map ... maps) {
        Map merged = new HashMap()
        maps.each{ m ->
            merged = merge(merged, m)
            //logger.info(marker + ": IN MERGER: "+merged.toString().contains("janus"))
        }
        return merged
    }

    private static Map merge(Map original, Map newMap) {
        for (Object key : newMap.keySet()) {
            if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
                Map originalChild = new LinkedHashMap(original.get(key));
                Map newChild = new LinkedHashMap(newMap.get(key));
                original.put(key, merge(originalChild, newChild));
            } else if (newMap.get(key) instanceof List && original.get(key) instanceof List) {
                List originalChild = new ArrayList(original.get(key));
                List newChild = new ArrayList(newMap.get(key));
                for (Object each : newChild) {
                    if (!originalChild.contains(each)) {
                        originalChild.add(each);
                    }
                }
            } else {
                original.put(key, newMap.get(key));
            }
        }
        return original;
    }
}
