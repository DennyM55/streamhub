package com.dennymathew.streamhub.catalog;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;

class CatalogClientTest {
    @Test void retriesTemporaryServerFailureAndForwardsInternalKey() throws Exception {
        var attempts=new AtomicInteger();
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/movies/1",exchange -> {
            assertThat(exchange.getRequestHeaders().getFirst("X-Catalog-Key")).isEqualTo("internal-test-key");
            int status=attempts.incrementAndGet()==1?503:200;
            byte[] response="{\"id\":1,\"title\":\"Sintel\",\"durationMinutes\":15}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(status,response.length);exchange.getResponseBody().write(response);exchange.close();
        });
        server.start();
        try {
            var client=new CatalogClient("http://127.0.0.1:"+server.getAddress().getPort(),"internal-test-key");
            assertThat(client.getMovie(1L).title()).isEqualTo("Sintel");
            assertThat(attempts.get()).isEqualTo(2);
        } finally { server.stop(0); }
    }
    @Test void doesNotRetryNotFoundAndOpensCircuitAfterRepeatedServerFailures() throws Exception {
        var attempts=new AtomicInteger();
        var status=new AtomicInteger(404);
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/movies/1",exchange -> {
            attempts.incrementAndGet();exchange.sendResponseHeaders(status.get(),-1);exchange.close();
        });server.start();
        try {
            var client=new CatalogClient("http://127.0.0.1:"+server.getAddress().getPort(),"");
            assertThatThrownBy(() -> client.getMovie(1L)).isInstanceOf(org.springframework.web.client.HttpClientErrorException.NotFound.class);
            assertThat(attempts.get()).isEqualTo(1);
            status.set(503);
            for(int i=0;i<5;i++) assertThatThrownBy(() -> client.getMovie(1L)).isInstanceOf(org.springframework.web.client.HttpServerErrorException.class);
            int before=attempts.get();
            assertThatThrownBy(() -> client.getMovie(1L)).isInstanceOf(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class);
            assertThat(attempts.get()).isEqualTo(before);
        } finally { server.stop(0); }
    }
}
