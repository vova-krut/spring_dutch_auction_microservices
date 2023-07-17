package eu.attempto.dutch_auction_microservices.backend_service.converters;

import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;

@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {
  private final UsersRepository usersRepository;

  @Override
  public UsernamePasswordAuthenticationToken convert(Jwt token) {
    var username = (String) token.getClaim("preferred_username");

    var userContainer = usersRepository.findByEmail(username);
    if (userContainer.isEmpty()) {
      return null;
    }

    var user = userContainer.get();
    return new UsernamePasswordAuthenticationToken(
        user, null, AuthorityUtils.createAuthorityList(user.getRole().getName().name()));
  }
}
