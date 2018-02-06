package com.ibm.application

import com.ibm.service.ConfigurableMergeService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.config.server.EnableConfigServer
import org.springframework.context.annotation.Bean

@EnableConfigServer
@SpringBootApplication
@EnableCaching
class ConfigServerApplication {
	static private final Logger logger = LoggerFactory.getLogger(this.getClass())

	@Autowired
	ConfigurableMergeService cms

	static void main(String[] args) {
		Application.instance.ctx
		SpringApplication.run(ConfigServerApplication.class, args)
	}

	@Bean
	Object initDynamicMapping() {
		logger.info("------  Mapping dynamic endpoints -----")
		def ctx = Application.instance.ctx as Map
        ctx.endpoints.each { k,v   ->
			logger.info("Mapping endpoint ${k}")
			cms.addMapping( v.path as String )
		}
        return null
	}
}
