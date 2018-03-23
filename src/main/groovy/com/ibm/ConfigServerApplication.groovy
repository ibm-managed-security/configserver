package com.ibm

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.config.server.EnableConfigServer

@EnableConfigServer
@SpringBootApplication
@EnableCaching
class ConfigServerApplication {
	static private final Logger logger = LoggerFactory.getLogger(this.getClass())

	static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args)
	}
}
