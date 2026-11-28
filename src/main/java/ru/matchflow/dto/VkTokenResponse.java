package ru.matchflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VkTokenResponse(@JsonProperty("user_id") Long userId, String state, String error) {
}
