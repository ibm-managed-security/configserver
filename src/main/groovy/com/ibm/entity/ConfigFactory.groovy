package com.ibm.entity

import com.ibm.utils.Deflatter
import com.ibm.utils.Flatter
import com.ibm.utils.Json
import com.ibm.utils.Yaml
import org.springframework.stereotype.Component

@Component
class ConfigFactory {

    Config create(String name, String profile, String label, ConfigFormat format, String content, Object object) {
        return new Config(name, profile, label, format, content, object)
    }

    Config createFromContent(String name, String profile, String label, ConfigFormat format, String content) {
        Object object = getObject(content, format)
        return new Config(name, profile, label, format, content, object)
    }

    Config createFromObject(String name, String profile, String label, ConfigFormat format, Object object) {
        String content = getContent(object, format)
        return new Config(name, profile, label, format, content, object)
    }

    private String getContent(Object object, ConfigFormat format) {
        switch(format) {
            case ConfigFormat.JSON:
                return Json.dump(object)
            case ConfigFormat.PROPERTIES:
                return new Flatter().flat(object)
            case ConfigFormat.VALUE:
                if ([Collection, Map, Object[]].any { it.isAssignableFrom(object.getClass()) }) {
                    throw new IllegalArgumentException("Collection, map or array returned for value (should be a primitive value)")
                }
                return object
            case ConfigFormat.YML:
            case ConfigFormat.YAML:
                return Yaml.dump(map)
            default:
                return null
        }
    }

    private Object getObject(String content, ConfigFormat format) {
        switch (format){
            case ConfigFormat.PROPERTIES:
                Properties p = new Properties()
                p.load(new StringReader(content))

                // We have to load the dump because it still behaves as properties
                def obj = Json.load( Json.dump(p as Map))

                String flat = new Flatter().flat(obj)
                return new Deflatter(flat).deflat()
            case ConfigFormat.JSON:
                return Json.load(content)
            case ConfigFormat.YAML:
            case ConfigFormat.YML:
                return Yaml.load(content)
            default:
                return null
        }
    }

}
