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

    //request parameter ("format" by default) should be used to determine the requested media type
    configurer.favorParameter(true).
    //the favour parameter is set to "mediaType" instead of default "format"
    parameterName("mediaType").
    //ignore the accept headers
    ignoreAcceptHeader(true).
    defaultContentType(MediaType.TEXT_HTML).
    mediaType("html", MediaType.TEXT_HTML).
    mediaType("json", MediaType.APPLICATION_JSON);
  }
}
