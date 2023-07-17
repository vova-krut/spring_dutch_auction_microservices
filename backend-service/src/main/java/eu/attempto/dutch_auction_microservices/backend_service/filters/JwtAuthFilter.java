package eu.attempto.dutch_auction_microservices.backend_service.filters;

import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final UsersRepository usersRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    var session = request.getSession(false);
    var sci = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");

    if (sci != null) {
      var jwt = (Jwt) sci.getAuthentication().getPrincipal();
      var username = (String) jwt.getClaim("preferred_username");
      var user = usersRepository.findByEmail(username).orElseThrow();

      sci.setAuthentication(
          new UsernamePasswordAuthenticationToken(
              user, null, AuthorityUtils.createAuthorityList(user.getRole().getName().name())));
    }

    filterChain.doFilter(request, response);
  }
}
