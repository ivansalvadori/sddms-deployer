package br.ufsc.inf.lapesd.sddms.deployer.config;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("br.ufsc.inf.lapesd.sddms.deployer")
public class App {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(App.class, args);
    }
}
