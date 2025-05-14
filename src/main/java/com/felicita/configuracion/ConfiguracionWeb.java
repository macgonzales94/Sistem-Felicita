
package com.felicita.configuracion;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Controladores de vista para páginas básicas
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/acerca").setViewName("acerca");
        registry.addViewController("/contacto").setViewName("contacto");
        
        // Páginas de error
        registry.addViewController("/error/403").setViewName("error/403");
        registry.addViewController("/error/404").setViewName("error/404");
        registry.addViewController("/error/500").setViewName("error/500");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuración de recursos estáticos
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}