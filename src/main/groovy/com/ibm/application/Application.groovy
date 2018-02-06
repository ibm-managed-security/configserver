package com.ibm.application

import com.ibm.utils.Merger
import org.springframework.core.io.support.ResourcePatternResolver
import com.ibm.utils.Yaml

@Singleton
class Application {
    static final Object ctx
    static {
        def defaultConfStr = ResourcePatternResolver.getResource("/configserver-default.yaml").getText()
        Map defaultConf  = Yaml.load( defaultConfStr)

        if(System.getProperty('conf') != null ) {
            def filename = System.getProperty('conf')
            Map conf = Yaml.load( (filename as File).text )
            ctx = Merger.deepMerge(conf,defaultConf)
        }else{
            ctx = defaultConf
        }
        println(Yaml.dump(ctx))
    }
}