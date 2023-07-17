package eu.attempto.dutch_auction_microservices.dashboard_service.filters;

import eu.attempto.dutch_auction_microservices.backend_service.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {
  @Mock private JwtService jwtService;
  @InjectMocks private JwtAuthFilter jwtAuthFilter;

  @Test
  void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
    // Arrange
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var filterChain = mock(FilterChain.class);
    var validToken = "validToken";
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
    when(jwtService.validateToken(validToken)).thenReturn(mock(Authentication.class));

    // Act
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext()).isNotNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_InvalidToken_DoesNotAuthenticate() throws ServletException, IOException {
    // Arrange
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var filterChain = mock(FilterChain.class);
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalidToken");
    when(jwtService.validateToken(any(String.class))).thenReturn(null);

    // Act
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext()).isEqualTo(new SecurityContextImpl());
    verify(filterChain).doFilter(request, response);
  }

  @ValueSource(strings = {"BearerTokenFormIsNotValid", "NotBearer SoTokenFormIsNotValid"})
  @ParameterizedTest
  void doFilterInternal_InvalidTokenForms(String token) throws ServletException, IOException {
    // Arrange
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var filterChain = mock(FilterChain.class);
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);

    // Act
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_NullToken_DoesNotAuthenticate() throws ServletException, IOException {
    // Arrange
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var filterChain = mock(FilterChain.class);
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

    // Act
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_WhenRuntimeIsThrownItClearsSecurityContext_DoesNotAuthenticate() {
    // Arrange
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var filterChain = mock(FilterChain.class);
    var token = "token";
    var runtimeExceptionMessage = "Runtime";
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
    when(jwtService.validateToken(token)).thenThrow(new RuntimeException(runtimeExceptionMessage));

    // Act
    var result =
        assertThrows(
            RuntimeException.class,
            () -> jwtAuthFilter.doFilterInternal(request, response, filterChain),
            "Expected to clear security context and rethrow exception");

    // Assert
    assertThat(SecurityContextHolder.getContext()).isEqualTo(new SecurityContextImpl());
    assertThat(result).hasMessage(runtimeExceptionMessage);
  }
}
