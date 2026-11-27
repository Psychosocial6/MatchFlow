package ru.matchflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.net.URI;

@ConfigurationProperties(prefix = "matchflow.vk")
public record VkAuthProperties(Long clientId, URI redirectUri, String serviceToken) {
    public boolean configured() {
        return clientId != null && clientId > 0
                && redirectUri != null && redirectUri.getHost() != null
                && ("https".equalsIgnoreCase(redirectUri.getScheme())
                        || "http".equalsIgnoreCase(redirectUri.getScheme()))
                && redirectUri.getFragment() == null && redirectUri.getUserInfo() == null
                && StringUtils.hasText(serviceToken);
    }
}
