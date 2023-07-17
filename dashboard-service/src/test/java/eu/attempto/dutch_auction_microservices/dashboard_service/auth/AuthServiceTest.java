package eu.attempto.dutch_auction_microservices.dashboard_service.auth;

import eu.attempto.dutch_auction_microservices.backend_service.auth.dto.LoginDto;
import eu.attempto.dutch_auction_microservices.backend_service.auth.dto.RegistrationDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersService;
import eu.attempto.dutch_auction_microservices.backend_service.exceptions.BadRequestException;
import eu.attempto.dutch_auction_microservices.backend_service.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock private UsersService usersService;
  @Mock private JwtService jwtService;
  @Mock private PasswordEncoder encoder;
  @InjectMocks private AuthService authService;

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void registration_NewUser_ReturnsAccessToken(boolean isAdmin) {
    // Arrange
    var registrationDto =
        RegistrationDto.builder().name("Name").email("email@test.com").password("password").build();
    var mockToken = "accessToken";
    when(usersService.findByEmail(registrationDto.getEmail())).thenReturn(Optional.empty());
    when(usersService.createUser(Mockito.any(User.class), Mockito.eq(isAdmin)))
        .thenReturn(registrationDto.toEntity());
    when(jwtService.createToken(Mockito.any())).thenReturn(mockToken);
    given(encoder.encode(Mockito.any(String.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = authService.registration(registrationDto, isAdmin);

    // Assert
    assertThat(result.getAccessToken()).isEqualTo(mockToken);
    verify(encoder, times(1)).encode(Mockito.any(String.class));
  }

  @Test
  void registration_UserAlreadyExists_ThrowsBadRequest() {
    // Arrange
    var registrationDto =
        RegistrationDto.builder().name("Name").email("email@test.com").password("password").build();
    when(usersService.findByEmail(registrationDto.getEmail()))
        .thenReturn(Optional.of(registrationDto.toEntity()));

    // Act
    var result =
        assertThrows(
            BadRequestException.class,
            () -> authService.registration(registrationDto, false),
            "Expected to throw BadRequestException since user with this email already exists");

    // Assert
    assertThat(result).hasMessage("User with this email already exists");
  }

  @Test
  void login_ExistingUserWithRightCredentials_ReturnsAccessToken() {
    // Arrange
    var loginDto = LoginDto.builder().email("email@test.com").password("password").build();
    var mockToken = "accessToken";
    when(usersService.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(new User()));
    when(encoder.matches(Mockito.any(String.class), Mockito.any())).thenReturn(true);
    when(jwtService.createToken(Mockito.any())).thenReturn(mockToken);

    // Act
    var result = authService.login(loginDto);

    // Assert
    assertThat(result.getAccessToken()).isEqualTo(mockToken);
    verify(encoder, times(1)).matches(Mockito.any(String.class), Mockito.any());
  }

  @Test
  void login_NonExistingUser_ThrowsBadRequest() {
    // Arrange
    var loginDto = LoginDto.builder().email("email@test.com").password("password").build();
    when(usersService.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

    // Act
    var result =
        assertThrows(
            BadRequestException.class,
            () -> authService.login(loginDto),
            "Expected to throw if user with email from loginDto was not found");

    // Assert
    assertThat(result).hasMessage("Email or password is invalid");
  }

  @Test
  void login_ExistingUserWithWrongCredentials_ThrowsBadRequest() {
    // Arrange
    var loginDto = LoginDto.builder().email("email@test.com").password("password").build();
    when(usersService.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(new User()));
    when(encoder.matches(Mockito.any(String.class), Mockito.any())).thenReturn(false);

    // Act
    var result =
        assertThrows(
            BadRequestException.class,
            () -> authService.login(loginDto),
            "Expected to throw if user with email from loginDto was not found");

    // Assert
    assertThat(result).hasMessage("Email or password is invalid");
  }
}
