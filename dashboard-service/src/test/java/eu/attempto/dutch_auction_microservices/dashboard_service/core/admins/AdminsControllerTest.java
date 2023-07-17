package eu.attempto.dutch_auction_microservices.dashboard_service.core.admins;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.attempto.dutch_auction_microservices.backend_service.auth.dto.RegistrationDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto.ChangeAuctionDto;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = AdminsController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class AdminsControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private AdminsService adminsService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser(roles = "ADMIN")
  void createAdmin_ByAdmin_ReturnsOk() throws Exception {
    // Arrange
    var registrationDto =
        RegistrationDto.builder().name("Name").email("email@test.com").password("password").build();
    when(adminsService.createAdmin(registrationDto)).thenReturn("OK");

    // Act
    var response =
        mockMvc.perform(
            post("/admins")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is("OK")));
  }

  @Test
  void createAdmin_NoAuthentication_ThrowsUnauthorized() throws Exception {
    // Arrange
    var registrationDto =
        RegistrationDto.builder().name("Name").email("email@test.com").password("password").build();
    when(adminsService.createAdmin(registrationDto)).thenReturn("OK");

    // Act
    var response =
        mockMvc.perform(
            post("/admins")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)));

    // Assert
    response.andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void changeAuction_ReturnsOk() throws Exception {
    // Arrange
    var changeAuctionDto = ChangeAuctionDto.builder().id(1L).price(BigDecimal.ONE).build();
    when(adminsService.changeAuction(Mockito.any(ChangeAuctionDto.class))).thenReturn("OK");

    // Act
    var response =
        mockMvc.perform(
            patch("/admins/auctions")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeAuctionDto)));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is("OK")));
  }
}
