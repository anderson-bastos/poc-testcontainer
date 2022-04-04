package org.testcontainers.localStackmodule

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PocTestContainerApplication

fun main(args: Array<String>) {
	runApplication<PocTestContainerApplication>(*args)
}
