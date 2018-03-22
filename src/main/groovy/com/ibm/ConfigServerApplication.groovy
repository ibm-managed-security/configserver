package com.ibm

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.config.server.EnableConfigServer

@EnableConfigServer
@SpringBootApplication
@EnableCaching
class ConfigServerApplication {

	static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args)
	}

}
