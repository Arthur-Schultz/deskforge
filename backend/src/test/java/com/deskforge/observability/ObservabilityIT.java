package com.deskforge.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.deskforge.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.server.port=0")
class ObservabilityIT extends PostgresIntegrationTest {

    private final HttpClient http = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @LocalManagementPort
    private int managementPort;

    @Test
    void everyResponseCarriesARequestIdAndValidIncomingIdsAreKept() throws Exception {
        HttpResponse<String> generated = get(port, "/api/categories", null);
        HttpResponse<String> propagated = get(port, "/api/categories", "pedido-1234-abcd");
        HttpResponse<String> rejected = get(port, "/api/categories", "<script>");

        assertThat(generated.headers().firstValue("X-Request-Id")).hasValueSatisfying(id -> assertThat(id).hasSize(36));
        assertThat(propagated.headers().firstValue("X-Request-Id")).hasValue("pedido-1234-abcd");
        assertThat(rejected.headers().firstValue("X-Request-Id")).hasValueSatisfying(id -> assertThat(id).hasSize(36));
    }

    @Test
    void metricsAndHealthLiveOnTheInternalManagementPortOnly() throws Exception {
        get(port, "/api/products", null);

        HttpResponse<String> metrics = get(managementPort, "/actuator/prometheus", null);
        assertThat(metrics.statusCode()).isEqualTo(200);
        assertThat(metrics.body()).contains("http_server_requests_seconds", "deskforge_emails_pending",
                "application=\"deskforge-api\"");
        assertThat(get(managementPort, "/actuator/health/readiness", null).body()).contains("UP");

        assertThat(get(port, "/actuator/prometheus", null).statusCode()).isIn(401, 404);
    }

    private HttpResponse<String> get(int targetPort, String path, String requestId) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + targetPort + path));
        if (requestId != null) {
            request.header("X-Request-Id", requestId);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
