package com.example.foodndeliv.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    // These values will be injected from your application.properties file
    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}") // This is the realm the admin client itself authenticates against (e.g., "master")
    private String realm;

    @Value("${keycloak.client-id}") // The client ID for the admin client (e.g., "admin-cli")
    private String clientId;

    @Value("${keycloak.client-secret}") // The secret for the admin client
    private String clientSecret;

    // If you were to use direct admin user credentials (less common for service-to-service)
    // you would uncomment and use these:
    // @Value("${keycloak.username}")
    // private String username;
    // @Value("${keycloak.password}")
    // private String password;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm) // Realm for the admin client to authenticate (e.g., "master")
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) // Recommended for service accounts/clients
                .clientId(clientId)                             // e.g., "admin-cli"
                .clientSecret(clientSecret)                     // Secret for the "admin-cli" or your service account client
                // If using username/password for the admin client (not recommended for service-to-service):
                // .grantType(OAuth2Constants.PASSWORD)
                // .username(username) // e.g. Keycloak admin user
                // .password(password) // Keycloak admin user's password
                .build();
    }
}