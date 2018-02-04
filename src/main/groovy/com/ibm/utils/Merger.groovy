package com.ibm.utils

class Merger {
    static Map deepMerge(Map onto, Map... overrides) {
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
}
