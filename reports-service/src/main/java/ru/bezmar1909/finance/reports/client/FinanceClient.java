package ru.bezmar1909.finance.reports.client;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FinanceClient {
    private final RestTemplate restTemplate;
    private final String financeBaseUrl;
    private final String internalToken;

    public FinanceClient(
            RestTemplate restTemplate,
            @Value("${app.finance-service-url}") String financeBaseUrl,
            @Value("${app.internal-token}") String internalToken
    ) {
        this.restTemplate = restTemplate;
        this.financeBaseUrl = financeBaseUrl;
        this.internalToken = internalToken;
    }

    public List<FinanceOperationDto> operations(Long actorUserId, LocalDate from, LocalDate to, Long groupId, List<Long> userIds) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(financeBaseUrl + "/internal/operations")
                .queryParam("actorUserId", actorUserId)
                .queryParam("from", from)
                .queryParam("to", to);
        if (groupId != null) {
            uri.queryParam("groupId", groupId);
        }
        if (userIds != null && !userIds.isEmpty()) {
            userIds.forEach(userId -> uri.queryParam("userIds", userId));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", internalToken);
        ResponseEntity<FinanceOperationDto[]> response = exchangeWithRetry(uri.toUriString(), headers);
        FinanceOperationDto[] body = response.getBody();
        return body == null ? List.of() : Arrays.asList(body);
    }

    private ResponseEntity<FinanceOperationDto[]> exchangeWithRetry(String uri, HttpHeaders headers) {
        RestClientException lastFailure = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<FinanceOperationDto[]>() {
                        }
                );
            } catch (RestClientException ex) {
                lastFailure = ex;
                if (attempt == 3) {
                    break;
                }
                try {
                    Thread.sleep(100L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        throw lastFailure;
    }
}
