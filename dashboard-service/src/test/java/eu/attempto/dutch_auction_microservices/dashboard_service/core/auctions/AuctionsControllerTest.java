package eu.attempto.dutch_auction_microservices.dashboard_service.core.auctions;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto.CreateAuctionDto;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = AuctionsController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class AuctionsControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private AuctionsService auctionsService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser
  void createAuction_Authenticated_ReturnsCreatedAuction() throws Exception {
    // Arrange
    var title = "Auction";
    var createAuctionDto =
        CreateAuctionDto.builder()
            .title(title)
            .description("Description")
            .endTime(ZonedDateTime.now().plusMinutes(5))
            .price(BigDecimal.ONE)
            .build();
    var mockFile = new MockMultipartFile("images", new byte[] {});
    when(auctionsService.createAuction(
            Mockito.any(UserDetails.class),
            Mockito.any(MultipartFile[].class),
            Mockito.any(CreateAuctionDto.class)))
        .thenReturn(Auction.builder().title(title).build());

    // Act
    var response =
        mockMvc.perform(
            multipart("/auctions")
                .file(mockFile)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("title", createAuctionDto.getTitle())
                .param("description", createAuctionDto.getDescription())
                .param(
                    "endTime",
                    String.valueOf(createAuctionDto.getEndTime().toInstant().toEpochMilli()))
                .param("price", createAuctionDto.getPrice().toString()));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.active", CoreMatchers.is(true)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.title", CoreMatchers.is(title)));
  }

  @Test
  void createAuction_NotAuthenticated_ThrowsUnauthorized() throws Exception {
    // Arrange
    var createAuctionDto =
        CreateAuctionDto.builder()
            .title("Auction")
            .description("Description")
            .endTime(ZonedDateTime.now().plusMinutes(5))
            .price(BigDecimal.ONE)
            .build();
    var mockFile = new MockMultipartFile("images", new byte[] {});
    when(auctionsService.createAuction(
            Mockito.any(UserDetails.class),
            Mockito.any(MultipartFile[].class),
            Mockito.any(CreateAuctionDto.class)))
        .thenReturn(new Auction());

    // Act
    var response =
        mockMvc.perform(
            multipart("/auctions")
                .file(mockFile)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("title", createAuctionDto.getTitle())
                .param("description", createAuctionDto.getDescription())
                .param(
                    "endTime",
                    String.valueOf(createAuctionDto.getEndTime().toInstant().toEpochMilli()))
                .param("price", createAuctionDto.getPrice().toString()));

    // Assert
    response.andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void createAuction_WithStartTime_ReturnsCreatedAuction() throws Exception {
    // Arrange
    var title = "Auction";
    var createAuctionDto =
        CreateAuctionDto.builder()
            .title(title)
            .description("Description")
            .startTime(ZonedDateTime.now().plusMinutes(3))
            .endTime(ZonedDateTime.now().plusMinutes(5))
            .price(BigDecimal.ONE)
            .build();
    var mockFile = new MockMultipartFile("images", new byte[] {});
    when(auctionsService.createAuction(
            Mockito.any(UserDetails.class),
            Mockito.any(MultipartFile[].class),
            Mockito.any(CreateAuctionDto.class)))
        .thenReturn(Auction.builder().title(title).active(false).build());

    // Act
    assert createAuctionDto.getStartTime() != null;
    var response =
        mockMvc.perform(
            multipart("/auctions")
                .file(mockFile)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("title", createAuctionDto.getTitle())
                .param("description", createAuctionDto.getDescription())
                .param(
                    "startTime",
                    String.valueOf(createAuctionDto.getStartTime().toInstant().toEpochMilli()))
                .param(
                    "endTime",
                    String.valueOf(createAuctionDto.getEndTime().toInstant().toEpochMilli()))
                .param("price", createAuctionDto.getPrice().toString()));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.active", CoreMatchers.is(false)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.title", CoreMatchers.is(title)));
  }

  @Test
  @WithMockUser
  void createAuction_NotValidParam_ThrowsBadRequest() throws Exception {
    // Arrange
    var createAuctionDto =
        CreateAuctionDto.builder()
            .description("Description")
            .endTime(ZonedDateTime.now().plusMinutes(5))
            .price(BigDecimal.ONE)
            .build();
    var mockFile = new MockMultipartFile("images", new byte[] {});
    when(auctionsService.createAuction(
            Mockito.any(UserDetails.class),
            Mockito.any(MultipartFile[].class),
            Mockito.any(CreateAuctionDto.class)))
        .thenReturn(new Auction());

    // Act
    var response =
        mockMvc.perform(
            multipart("/auctions")
                .file(mockFile)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("description", createAuctionDto.getDescription())
                .param(
                    "endTime",
                    String.valueOf(createAuctionDto.getEndTime().toInstant().toEpochMilli()))
                .param("price", createAuctionDto.getPrice().toString()));

    // Assert
    response.andExpect(MockMvcResultMatchers.status().isBadRequest());
  }

  @Test
  @WithMockUser
  void getAuctions_ReturnsAuctionPage() throws Exception {
    // Arrange
    var auctionList = new ArrayList<Auction>();
    auctionList.add(Auction.builder().title("Auction").build());
    auctionList.add(Auction.builder().title("Other auction").build());
    var expectedPage = new PageImpl<>(auctionList);
    when(auctionsService.getAuctions(Mockito.any(Pageable.class))).thenReturn(expectedPage);

    // Act
    var response =
        mockMvc.perform(get("/auctions").with(SecurityMockMvcRequestPostProcessors.csrf()));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.content.size()", CoreMatchers.is(auctionList.size())));
  }

  @Test
  @WithMockUser
  void getAuction_IdIsInvalidNum() throws Exception {
    // Arrange
    var id = "String";

    // Act
    var response =
        mockMvc.perform(get("/auctions/" + id).with(SecurityMockMvcRequestPostProcessors.csrf()));

    // Assert
    response.andExpect(MockMvcResultMatchers.status().isBadRequest());
  }

  @Test
  @WithMockUser
  void getAuction_IdIsValidNum() throws Exception {
    // Arrange
    var id = "1";
    var title = "Auction";
    when(auctionsService.subscribeToAuction(Mockito.any(Long.class)))
        .thenReturn(Flux.just(Auction.builder().title(title).build()));

    // Act
    var response =
        mockMvc.perform(get("/auctions/" + id).with(SecurityMockMvcRequestPostProcessors.csrf()));

    // Assert
    response
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(
            result -> {
              var content = result.getResponse().getContentAsString();
              var jsonNode = objectMapper.readTree(content.substring(5));
              var actualTitle = jsonNode.at("/title").asText();
              assertThat(actualTitle).isEqualTo(title);
            });
  }
}
