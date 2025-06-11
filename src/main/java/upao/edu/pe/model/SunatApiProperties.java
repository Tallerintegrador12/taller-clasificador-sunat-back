package upao.edu.pe.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sunat.api")
public class SunatApiProperties {
    private String url;
    private String cookies;

    // Getters y Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCookies() { return cookies; }
    public void setCookies(String cookies) { this.cookies = cookies; }
}
