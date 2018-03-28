package com.ibm.utils

class Merger {
    static Map deepMerge(Map ... sources) {
        if (sources.length == 0) return [:]
        if (sources.length == 1) return sources[0]

        sources.inject([:]) { result, source ->
            source.each { k, v ->
                result[k] = result[k] instanceof Map ? deepMerge(result[k], v) : v
            }
            result
        }
    }
}
