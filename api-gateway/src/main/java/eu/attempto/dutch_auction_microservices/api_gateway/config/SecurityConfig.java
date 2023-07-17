package eu.attempto.dutch_auction_microservices.api_gateway.config;

import eu.attempto.dutch_auction_microservices.api_gateway.converters.JwtAuthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
  private String jwkSetUri;

  private final JwtAuthConverter jwtAuthConverter;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
    httpSecurity
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchange ->
                exchange
                    .pathMatchers("/eureka/**", "/api/backend/**", "/api/auth/**", "/*.*")
                    .permitAll()
                    .pathMatchers("/api/dashboard/**")
                    .hasAuthority("ADMIN")
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oAuth2 ->
                oAuth2.jwt(
                    jwt -> jwt.jwkSetUri(jwkSetUri).jwtAuthenticationConverter(jwtAuthConverter)));

    return httpSecurity.build();
  }
}
