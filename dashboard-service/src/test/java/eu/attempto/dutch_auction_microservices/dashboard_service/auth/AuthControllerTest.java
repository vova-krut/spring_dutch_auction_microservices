package eu.attempto.dutch_auction_microservices.dashboard_service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.attempto.dutch_auction_microservices.backend_service.auth.dto.AuthResponse;
import eu.attempto.dutch_auction_microservices.backend_service.auth.dto.LoginDto;
import eu.attempto.dutch_auction_microservices.backend_service.auth.dto.RegistrationDto;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean AuthService authService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void registration_ReturnsAccessToken() throws Exception {
    // Arrange
    var registrationDto =
        RegistrationDto.builder().name("Name").email("email@test.com").password("password").build();
    var mockToken = "accessToken";
    var authResponse = AuthResponse.builder().accessToken(mockToken).build();
    when(authService.registration(registrationDto, false)).thenReturn(authResponse);

    // Act
    var response =
        mockMvc.perform(
            post("/auth/registration")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken", CoreMatchers.is(mockToken)));
  }

  @Test
  void login_ReturnsAccessToken() throws Exception {
    // Arrange
    var loginDto = LoginDto.builder().email("email@test.com").password("password").build();
    var mockToken = "accessToken";
    var authResponse = AuthResponse.builder().accessToken(mockToken).build();
    when(authService.login(loginDto)).thenReturn(authResponse);

    // Act
    var response =
        mockMvc.perform(
            post("/auth/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken", CoreMatchers.is(mockToken)));
  }
}
