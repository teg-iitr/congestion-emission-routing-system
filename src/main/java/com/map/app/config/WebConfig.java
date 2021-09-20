package com.map.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

@Override
public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {

    configurer.defaultContentType(MediaType.ALL).favorParameter(true).parameterName("mediaType").
    mediaType("json", MediaType.APPLICATION_JSON);
  }
}
