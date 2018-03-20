package de.jotbepunkt.blackbook

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.ServletComponentScan

/**
 * Created by bait on 12.06.17.
 */
@SpringBootApplication()
@ServletComponentScan
// this class may not be closed or spring is not happy
open class BlackBookApp {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(BlackBookApp::class.java, *args)
        }
    }
}

