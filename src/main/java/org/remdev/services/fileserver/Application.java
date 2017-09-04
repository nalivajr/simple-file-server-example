package org.remdev.services.fileserver;

import org.remdev.services.fileserver.auth.AuthService;
import org.remdev.services.fileserver.auth.AuthServiceImpl;
import org.remdev.services.fileserver.filters.ApiAuthFilter;
import org.remdev.services.fileserver.filters.WebUIAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableAutoConfiguration(exclude={
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    public AuthService authService(@Value("${fileserver.data.dir}") String dataDir) {
        return new AuthServiceImpl(dataDir);
    }

    @Bean
    public FilterRegistrationBean apiFilterRegistration(@Autowired AuthService authService) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ApiAuthFilter(authService));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean uiFilterRegistration(@Autowired AuthService authService) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new WebUIAuthFilter(authService));
        registration.addUrlPatterns("/ui/*");
        registration.setOrder(2);
        return registration;
    }
}
