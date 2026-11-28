package ru.matchflow.dto;

import java.net.URI;

public record VkConfigResponse(boolean configured, Long clientId, URI redirectUri) {
}
