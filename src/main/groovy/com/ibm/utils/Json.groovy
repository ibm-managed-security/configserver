package com.ibm.utils

import groovy.json.JsonOutput

class Json {
    static String dump(Object obj) {
        JsonOutput.prettyPrint(JsonOutput.toJson(obj))
    }

    static Object load (String str) {
        Yaml.load(str)
    }

}
