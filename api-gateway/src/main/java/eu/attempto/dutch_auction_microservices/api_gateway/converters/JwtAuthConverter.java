package eu.attempto.dutch_auction_microservices.api_gateway.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter
    implements Converter<Jwt, Mono<? extends AbstractAuthenticationToken>> {

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
      new JwtGrantedAuthoritiesConverter();

  @Override
  public Mono<? extends AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
    var roles =
        Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractResourceRoles(jwt).stream())
            .collect(Collectors.toSet());

    return Mono.just(
        new JwtAuthenticationToken(jwt, roles, jwt.getClaimAsString("preferred_username")));
  }

  private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
    Map<String, Object> realmAccess;
    Collection<String> roles;

    if (jwt.getClaim("realm_access") == null) {
      return Set.of();
    }
    realmAccess = jwt.getClaim("realm_access");

    if (realmAccess.get("roles") == null) {
      return Set.of();
    }
    roles = (Collection<String>) realmAccess.get("roles");

    return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
  }
}
