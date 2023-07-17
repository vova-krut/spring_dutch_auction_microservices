package eu.attempto.dutch_auction_microservices.dashboard_service.core.bids;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.dto.PlaceBidDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.Role;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesEnum;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = BidsController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class BidsControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private BidsService bidsService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser
  void placeBid_Authenticated_PlacesBidAndReturnsOk() throws Exception {
    // Arrange
    var placeBidDto = PlaceBidDto.builder().auctionId(1L).build();
    when(bidsService.placeBid(Mockito.any(UserDetails.class), Mockito.any(PlaceBidDto.class)))
        .thenReturn("OK");

    // Act
    var response =
        mockMvc.perform(
            post("/bids")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(placeBidDto)));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is("OK")));
  }

  @Test
  void placeBid_NotAuthenticated_ThrowsUnauthorized() throws Exception {
    // Arrange
    var placeBidDto = PlaceBidDto.builder().auctionId(1L).build();
    when(bidsService.placeBid(Mockito.any(UserDetails.class), Mockito.any(PlaceBidDto.class)))
        .thenReturn("OK");

    // Act
    var response =
        mockMvc.perform(
            post("/bids")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(placeBidDto)));

    // Assert
    response.andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void getMyBids_ReturnsPlacedBids() throws Exception {
    // Arrange
    var role = Role.builder().name(RolesEnum.USER).build();
    var user = User.builder().name("User").role(role).build();
    var bids = List.of(Bid.builder().user(user).build());
    when(bidsService.getUserBids(Mockito.any(UserDetails.class))).thenReturn(bids);

    // Act
    var response = mockMvc.perform(get("/bids"));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(bids.size())));
  }
}
