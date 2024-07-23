package tech.siloxa.clipboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Clipboard.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private String name;

    private String cdn;

    public String getName() {
        return name;
    }

    public String getCdn() {
        return cdn;
    }
}
