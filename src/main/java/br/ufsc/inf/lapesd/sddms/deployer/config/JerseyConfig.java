package br.ufsc.inf.lapesd.sddms.deployer.config;

import java.io.IOException;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath("/sddms-deployer")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() throws IOException {
        this.register(RequestContextFilter.class);
        this.packages("br.ufsc.inf.lapesd.sddms.deployer.endpoint");
        this.register(CorsInterceptor.class);
    }
}