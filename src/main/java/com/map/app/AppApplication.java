package com.map.app;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import com.map.app.service.TrafficAndRoutingService;

/**
 * @author Siftee
 */

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, XADataSourceAutoConfiguration.class})
public class AppApplication {
    @Autowired
    TrafficAndRoutingService ts;

    private static void insertCMDProperties(String[] args) {
        try {
            for (String arg : args) {
                if (arg.length() <= 2 || arg.charAt(0) != '-' && arg.charAt(1) != '-') {
                    throw new IOException();
                }
                String[] map = arg.substring(2).split("=");
                if (map.length != 2) {
                    throw new IOException();
                }
                Properties prop = new Properties();
                try (FileInputStream ip = new FileInputStream("config.properties")) {
                    prop.load(ip);
                    if (prop.getProperty(map[0]) == null) {
                        throw new IOException();
                    } else {
                        prop.setProperty(map[0], map[1]);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Config properties are not valid. Aborting ...");
                }
                prop.store(new FileOutputStream("config.properties"), null);
            }
        } catch (IOException e) {
            throw new RuntimeException("Invalid argument. Please follow proper syntax.Aborting...");
        }
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            throw new RuntimeException("Enter arguments in comma-seperated fashion");
        }
        if (args.length == 1) {
            String[] CmdArgs = args[0].split(",");
            insertCMDProperties(CmdArgs);
        }
        SpringApplication.run(AppApplication.class, args);

    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    void jobInitializer() {
        ts.start();
    }

    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/");
    }
}

@Configuration
@EnableScheduling
class SchedulingConfiguration {

}