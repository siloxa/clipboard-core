package tech.siloxa.clipboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class PublicResourceConfiguration implements WebMvcConfigurer {

    private final String publicPath;

    // TODO: run this command: `sudo chown -R $USER:$USER /opt/`
    public PublicResourceConfiguration(ApplicationProperties applicationProperties) throws IOException {
        publicPath = "/opt/" + applicationProperties.getName() + "/";
        Files.createDirectories(Paths.get(publicPath));
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/public/**")
            .addResourceLocations("file:" + publicPath);
    }

    @Bean("publicPath")
    public String getPublicPath() {
        return publicPath;
    }
}
