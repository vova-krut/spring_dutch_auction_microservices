package eu.attempto.dutch_auction_microservices.dashboard_service.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.Role;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesEnum;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
  @Mock private UsersService usersService;
  @InjectMocks private JwtService jwtService;

  private final String secretKey = Base64.getEncoder().encodeToString("secretKey".getBytes());

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
  }

  @Test
  void createToken_ReturnsNewValidTokenForGivenId() {
    // Arrange
    var id = 1L;
    var algorithm = Algorithm.HMAC256(secretKey);
    var verifier = JWT.require(algorithm).build();
    var fourteenMinFiftyNineSec =
        Instant.now().plus(14, ChronoUnit.MINUTES).plus(59, ChronoUnit.SECONDS);
    var fifteenMinOneSec = Instant.now().plus(15, ChronoUnit.MINUTES).plus(1, ChronoUnit.SECONDS);

    // Act
    var result = jwtService.createToken(id);
    var decoded = verifier.verify(result);

    // Assert
    assertThat(decoded.getSubject()).isEqualTo(String.valueOf(1L));
    assertThat(decoded.getExpiresAt()).isBetween(fourteenMinFiftyNineSec, fifteenMinOneSec);
  }

  @Test
  void validateToken_ValidToken_ReturnsAuthentication() {
    // Arrange
    var id = 1L;
    var role = Role.builder().name(RolesEnum.USER).build();
    var user = User.builder().id(id).name("User").role(role).build();
    var algorithm = Algorithm.HMAC256(secretKey);
    var token =
        JWT.create()
            .withSubject(String.valueOf(id))
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
            .sign(algorithm);
    when(usersService.findUserById(id)).thenReturn(Optional.of(user));

    // Act
    var result = jwtService.validateToken(token);

    // Assert
    assertThat(result.isAuthenticated()).isTrue();
    assertThat(result.getPrincipal()).isEqualTo(user);
  }

  @Test
  void validateToken_InvalidToken_ReturnsNull() {
    // Act
    var result = jwtService.validateToken("token");

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void validateToken_ValidTokenUserIsAbsentInDb_ReturnsNull() {
    // Arrange
    var id = 1L;
    var algorithm = Algorithm.HMAC256(secretKey);
    var token =
        JWT.create()
            .withSubject(String.valueOf(id))
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
            .sign(algorithm);
    when(usersService.findUserById(id)).thenReturn(Optional.empty());

    // Act
    var result = jwtService.validateToken(token);

    // Assert
    assertThat(result).isNull();
  }
}
