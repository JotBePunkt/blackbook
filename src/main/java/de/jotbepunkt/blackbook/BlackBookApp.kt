package de.jotbepunkt.blackbook

import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.VaadinServlet
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean

/**
 * Created by bait on 12.06.17.
 */
@SpringBootApplication
@ServletComponentScan
// this class may not be closed or spring is not happy
open class BlackBookApp {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(BlackBookApp::class.java, *args)
        }
    }

    @Bean fun servlet(): Servlet = Servlet()

    @Bean fun servletRegistration(): ServletRegistrationBean = ServletRegistrationBean(servlet(), "/*")

}

@VaadinServletConfiguration(ui = BlackBookUI::class, productionMode = false)
class Servlet : VaadinServlet(), ApplicationContextAware {

    lateinit var appContext: ApplicationContext

    override fun setApplicationContext(p0: ApplicationContext) {
        appContext = p0
    }
}
