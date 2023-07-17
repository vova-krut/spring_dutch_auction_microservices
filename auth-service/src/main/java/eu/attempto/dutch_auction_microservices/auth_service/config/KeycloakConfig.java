package eu.attempto.dutch_auction_microservices.auth_service.config;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
  @Value("${keycloak.server.url}")
  private String serverUrl;

  @Value("${keycloak.server.username}")
  private String username;

  @Value("${keycloak.server.password}")
  private String password;

  @Bean
  public Keycloak keycloak() {
    return Keycloak.getInstance(serverUrl, "master", username, password, "admin-cli");
  }
}
