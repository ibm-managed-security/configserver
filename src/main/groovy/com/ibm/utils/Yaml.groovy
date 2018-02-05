package com.ibm.utils

import org.yaml.snakeyaml.DumperOptions

class Yaml {
   static String dump (obj){
        DumperOptions options = new DumperOptions()
        options.explicitStart = true
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        new org.yaml.snakeyaml.Yaml(options).dump(obj)
    }

    static Object load (String str){
        new org.yaml.snakeyaml.Yaml().load(str)
    }
}
