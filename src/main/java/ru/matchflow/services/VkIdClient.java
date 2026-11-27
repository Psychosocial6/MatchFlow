package ru.matchflow.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import ru.matchflow.config.VkAuthProperties;
import ru.matchflow.dto.VkLoginRequest;
import ru.matchflow.dto.VkTokenResponse;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class VkIdClient {
    private final VkAuthProperties properties;
    private final RestClient client;

    public VkIdClient(VkAuthProperties properties) {
        this.properties = properties;
        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.client = RestClient.builder()
                .baseUrl("https://id.vk.ru")
                .requestFactory(factory)
                .build();
    }

    public String exchange(VkLoginRequest request) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId().toString());
        form.add("redirect_uri", properties.redirectUri().toString());
        form.add("service_token", properties.serviceToken());
        form.add("code", request.code());
        form.add("device_id", request.deviceId());
        form.add("code_verifier", request.codeVerifier());
        form.add("state", request.state());

        VkTokenResponse response;
        try {
            response = client.post()
                    .uri("/oauth2/auth")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(VkTokenResponse.class);
        } catch (RestClientResponseException failure) {
            if (failure.getStatusCode().is4xxClientError()) {
                throw rejectedLogin();
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "VK ID is unavailable");
        } catch (RestClientException failure) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not complete the request to VK ID");
        }

        if (response == null || response.error() != null || response.userId() == null
                || response.userId() <= 0 || !request.state().equals(response.state())) {
            throw rejectedLogin();
        }
        return response.userId().toString();
    }

    private static ResponseStatusException rejectedLogin() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "VK ID rejected the login. Check the VK application settings and try again.");
    }
}
