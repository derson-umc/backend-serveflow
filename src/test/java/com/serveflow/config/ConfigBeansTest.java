package com.serveflow.config;

import com.cloudinary.Cloudinary;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigBeansTest {

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Nested
    @DisplayName("AsyncConfig")
    class AsyncConfigTests {

        @Test
        @DisplayName("auditExecutor retorna ThreadPoolTaskExecutor configurado")
        void auditExecutor_returnsConfiguredExecutor() {
            AsyncConfig config = new AsyncConfig();
            Executor executor = config.auditExecutor();
            assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        }

        @Test
        @DisplayName("auditExecutor usa prefixo de thread 'audit-'")
        void auditExecutor_hasCorrectThreadPrefix() {
            AsyncConfig config = new AsyncConfig();
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.auditExecutor();
            assertThat(executor.getThreadNamePrefix()).isEqualTo("audit-");
        }

        @Test
        @DisplayName("auditExecutor tem capacidade de fila 500")
        void auditExecutor_hasQueueCapacity500() {
            AsyncConfig config = new AsyncConfig();
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.auditExecutor();
            assertThat(executor.getQueueCapacity()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("RestClientConfig")
    class RestClientConfigTests {

        @Test
        @DisplayName("restClientBuilder retorna Builder não nulo")
        void restClientBuilder_returnsNonNull() {
            RestClientConfig config = new RestClientConfig();
            RestClient.Builder builder = config.restClientBuilder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("restClientBuilder constrói RestClient sem exceção")
        void restClientBuilder_buildsClient() {
            RestClientConfig config = new RestClientConfig();
            RestClient client = config.restClientBuilder().build();
            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("TomcatConfig")
    class TomcatConfigTests {

        @Test
        @DisplayName("tomcatNio2Customizer retorna customizer não nulo")
        void customizer_isNotNull() {
            TomcatConfig config = new TomcatConfig();
            WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer =
                    config.tomcatNio2Customizer();
            assertThat(customizer).isNotNull();
        }

        @Test
        @DisplayName("customizer aplica protocolo NIO2 na factory sem lançar exceção")
        void customizer_appliesNio2Protocol() throws Exception {
            TomcatConfig config = new TomcatConfig();
            TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
            assertThatNoException().isThrownBy(() -> config.tomcatNio2Customizer().customize(factory));

            // Verify via reflection that the protocol was set
            java.lang.reflect.Field protocolField =
                    TomcatServletWebServerFactory.class.getDeclaredField("protocol");
            protocolField.setAccessible(true);
            Object protocol = protocolField.get(factory);
            assertThat(protocol.toString()).contains("Http11Nio2Protocol");
        }
    }

    @Nested
    @DisplayName("CloudinaryConfig")
    class CloudinaryConfigTests {

        @Test
        @DisplayName("cloudinary() retorna instância configurada com cloud-name correto")
        void cloudinary_returnsConfiguredBean() throws Exception {
            CloudinaryConfig config = new CloudinaryConfig();
            setField(config, "cloudName",  "test-cloud");
            setField(config, "apiKey",     "test-api-key");
            setField(config, "apiSecret",  "test-api-secret");

            Cloudinary cloudinary = config.cloudinary();

            assertThat(cloudinary).isNotNull();
            assertThat(cloudinary.config.cloudName).isEqualTo("test-cloud");
        }

        @Test
        @DisplayName("cloudinary() aplica secure=true")
        void cloudinary_hasSecureTrue() throws Exception {
            CloudinaryConfig config = new CloudinaryConfig();
            setField(config, "cloudName",  "test-cloud");
            setField(config, "apiKey",     "key");
            setField(config, "apiSecret",  "secret");

            Cloudinary cloudinary = config.cloudinary();

            assertThat(cloudinary.config.secure).isTrue();
        }
    }

    @Nested
    @DisplayName("OpenApiConfig")
    class OpenApiConfigTests {

        @Test
        @DisplayName("serveFlowOpenAPI() retorna OpenAPI com título correto")
        void openApi_hasCorrectTitle() throws Exception {
            OpenApiConfig config = new OpenApiConfig();
            setField(config, "baseUrl", "http://localhost:8080/api");

            OpenAPI openApi = config.serveFlowOpenAPI();

            assertThat(openApi.getInfo().getTitle()).isEqualTo("ServeFlow API");
        }

        @Test
        @DisplayName("serveFlowOpenAPI() define versão 1.0.0")
        void openApi_hasCorrectVersion() throws Exception {
            OpenApiConfig config = new OpenApiConfig();
            setField(config, "baseUrl", "http://localhost:8080/api");

            OpenAPI openApi = config.serveFlowOpenAPI();

            assertThat(openApi.getInfo().getVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("serveFlowOpenAPI() inclui esquema de segurança bearerAuth")
        void openApi_hasBearerAuthScheme() throws Exception {
            OpenApiConfig config = new OpenApiConfig();
            setField(config, "baseUrl", "http://localhost:8080/api");

            OpenAPI openApi = config.serveFlowOpenAPI();

            assertThat(openApi.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        }

        @Test
        @DisplayName("serveFlowOpenAPI() inclui servidor com URL base configurada")
        void openApi_hasCorrectServerUrl() throws Exception {
            OpenApiConfig config = new OpenApiConfig();
            setField(config, "baseUrl", "http://localhost:8080/api");

            OpenAPI openApi = config.serveFlowOpenAPI();

            assertThat(openApi.getServers()).isNotEmpty();
            assertThat(openApi.getServers().get(0).getUrl()).isEqualTo("http://localhost:8080/api");
        }
    }
}
