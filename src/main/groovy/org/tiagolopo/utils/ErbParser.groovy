package org.tiagolopo.utils

import org.jruby.embed.ScriptingContainer
import org.springframework.core.io.support.ResourcePatternResolver

class ErbParser {
    static String parse (String json, String template) {
        def container  = new ScriptingContainer()
        def script = ResourcePatternResolver.getResource("/template-parser.rb").getText()

        container.put('json', json)
        container.put('template',template)
        return container.runScriptlet(script)
    }
}
