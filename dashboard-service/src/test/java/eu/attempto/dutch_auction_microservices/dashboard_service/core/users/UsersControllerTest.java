package eu.attempto.dutch_auction_microservices.dashboard_service.core.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.Role;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesEnum;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.dto.BuyCoinsDto;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = UsersController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UsersControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private UsersService usersService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser
  void getMe_Authenticated_ReturnsUser() throws Exception {
    // Act
    var response = mockMvc.perform(get("/users/me"));

    // Assert
    response.andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  void getMe_NotAuthenticated_ThrowsUnauthorized() throws Exception {
    // Act
    var response = mockMvc.perform(get("/users/me"));

    // Assert
    response.andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void getUsers_ReturnsAllUsers() throws Exception {
    // Arrange
    var userList = new ArrayList<User>();
    userList.add(
        User.builder().name("1st user").role(Role.builder().name(RolesEnum.USER).build()).build());
    userList.add(
        User.builder().name("2nd user").role(Role.builder().name(RolesEnum.USER).build()).build());
    var expectedPage = new PageImpl<>(userList);
    when(usersService.getUsers(Mockito.any(Pageable.class))).thenReturn(expectedPage);

    // Act
    var response = mockMvc.perform(get("/users").with(SecurityMockMvcRequestPostProcessors.csrf()));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userList.size())));
  }

  @Test
  @WithMockUser
  void deleteMe_DeletesCurrentUser() throws Exception {
    // Arrange
    when(usersService.deleteMe(Mockito.any())).thenReturn("OK");

    // Act
    var response =
        mockMvc.perform(delete("/users/me").with(SecurityMockMvcRequestPostProcessors.csrf()));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is("OK")));
  }

  @Test
  @WithMockUser
  void buyCoins_PositiveAmount_AddsCoinsAndReturnsOK() throws Exception {
    // Arrange
    var buyCoinsDto = new BuyCoinsDto();
    buyCoinsDto.setEuro(BigDecimal.TEN);
    when(usersService.buyCoins(Mockito.any(UserDetails.class), Mockito.any(BuyCoinsDto.class)))
        .thenReturn("OK");

    // Act
    var response =
        mockMvc.perform(
            post("/users/coins")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyCoinsDto)));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is("OK")));
  }

  @Test
  @WithMockUser
  void buyCoins_LessOrEqualZeroAmount_ThrowsBadRequest() throws Exception {
    // Arrange
    var buyCoinsDto = BuyCoinsDto.builder().euro(BigDecimal.ZERO).build();
    when(usersService.buyCoins(Mockito.any(UserDetails.class), Mockito.any(BuyCoinsDto.class)))
        .thenReturn("OK");

    // Act
    var response =
        mockMvc.perform(
            post("/users/coins")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyCoinsDto)));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.errors.euro", CoreMatchers.is("must be greater than 0")));
  }
}
