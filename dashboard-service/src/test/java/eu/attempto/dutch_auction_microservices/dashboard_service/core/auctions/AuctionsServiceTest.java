package eu.attempto.dutch_auction_microservices.dashboard_service.core.auctions;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto.ChangeAuctionDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.dto.CreateAuctionDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.images.ImagesService;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.events.EventsService;
import eu.attempto.dutch_auction_microservices.backend_service.exceptions.BadRequestException;
import eu.attempto.dutch_auction_microservices.backend_service.schedules.SchedulesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionsServiceTest {
  @Mock private AuctionsRepository auctionsRepository;
  @Mock private ImagesService imagesService;
  @Mock private SchedulesService schedulesService;
  @Mock private EventsService eventsService;
  @InjectMocks private AuctionsService auctionsService;

  private User user;

  @BeforeEach
  void startUp() {
    user = User.builder().name("User").build();
  }

  @Test
  void createAuction_WithoutStartTime_ReturnsActiveAuction() {
    // Arrange
    var createAuctionDto = CreateAuctionDto.builder().title("Auction").build();
    given(auctionsRepository.save(Mockito.any(Auction.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = auctionsService.createAuction(user, new MockMultipartFile[] {}, createAuctionDto);

    // Assert
    assertThat(result.getAuthor()).isEqualTo(user);
    assertThat(result.getActive()).isTrue();
    assertThat(result.getImages()).isEmpty();
  }

  @Test
  void createAuction_WithStartTime_ReturnsNotActiveAuction() {
    // Arrange
    var createAuctionDto =
        CreateAuctionDto.builder()
            .title("Auction")
            .startTime(ZonedDateTime.now().plusMinutes(10))
            .build();
    given(auctionsRepository.save(Mockito.any(Auction.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = auctionsService.createAuction(user, new MockMultipartFile[] {}, createAuctionDto);

    // Assert
    assertThat(result.getAuthor()).isEqualTo(user);
    assertThat(result.getActive()).isFalse();
    assertThat(result.getImages()).isEmpty();
  }

  @Test
  void createAuction_WithImages_SavesImagesToAuction() {
    // Arrange
    var createAuctionDto =
        CreateAuctionDto.builder()
            .title("Auction")
            .startTime(ZonedDateTime.now().plusMinutes(10))
            .build();
    given(auctionsRepository.save(Mockito.any(Auction.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result =
        auctionsService.createAuction(
            user,
            new MockMultipartFile[] {new MockMultipartFile("image.png", new byte[] {})},
            createAuctionDto);

    // Assert
    assertThat(result.getAuthor()).isEqualTo(user);
    assertThat(result.getActive()).isFalse();
    assertThat(result.getImages()).hasSize(1);
  }

  @Test
  void getAuctions_ReturnsAuctionPage() {
    // Arrange
    var pageable = Pageable.unpaged();
    var auctionList = new ArrayList<Auction>();
    auctionList.add(new Auction());
    auctionList.add(new Auction());
    var expectedPage = new PageImpl<>(auctionList);
    when(auctionsRepository.findAll(pageable)).thenReturn(expectedPage);

    // Act
    var result = auctionsService.getAuctions(pageable);

    // Assert
    assertThat(result).isNotNull().isEqualTo(expectedPage);
  }

  @Test
  void deleteUserAuctions_ReturnsDeletedAuctions() {
    // Arrange
    when(auctionsRepository.deleteByAuthor(Mockito.any(User.class))).thenReturn(List.of());

    // Act
    var result = auctionsService.deleteUserAuctions(user);

    // Assert
    assertThat(result).isEqualTo(List.of());
    verify(auctionsRepository, times(1)).deleteByAuthor(Mockito.any(User.class));
  }

  @Test
  void getAuctionById_ExistingAuction_ReturnsAuction() {
    // Arrange
    var id = 1L;
    var auction = Auction.builder().author(user).title("Auction").build();
    when(auctionsRepository.findById(id)).thenReturn(Optional.of(auction));

    // Act
    var result = auctionsService.getAuctionById(id);

    // Assert
    assertThat(result).isEqualTo(auction);
  }

  @Test
  void getAuctionById_NotExistingAuction_ThrowsBadRequestException() {
    // Arrange
    var id = 1L;
    when(auctionsRepository.findById(id)).thenReturn(Optional.empty());

    // Act
    var result =
        assertThrows(
            BadRequestException.class,
            () -> auctionsService.getAuctionById(id),
            "Expected to throw that auction with this id was not found");

    // Assert
    assertThat(result).hasMessage("Auction with this id was not found");
  }

  @Test
  void subscribeToAuction_GetsAuctionAndSubscribesToIt() {
    // Arrange
    var id = 1L;
    when(auctionsRepository.findById(id)).thenReturn(Optional.of(new Auction()));
    when(eventsService.addEventAndSubscribe(Mockito.any(Auction.class))).thenReturn(null);

    // Act
    var result = auctionsService.subscribeToAuction(id);

    // Assert
    assertThat(result).isNull();
    verify(auctionsRepository, times(1)).findById(id);
  }

  @Test
  void placeBid_ReturnsBidWithEndTimeLaterThenFiveMin() {
    // Arrange
    var price = BigDecimal.TEN;
    var auction =
        Auction.builder()
            .author(user)
            .price(price)
            .endTime(ZonedDateTime.now().plusMinutes(10))
            .build();
    given(auctionsRepository.save(Mockito.any(Auction.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = auctionsService.placeBid(auction);

    // Assert
    assertThat(result.getPrice()).isEqualTo(price.subtract(new BigDecimal("0.1")));
    assertThat(result.getEndTime()).isEqualTo(auction.getEndTime());
  }

  @Test
  void placeBid_ReturnsBidWithEndTimePlusFiveMin() {
    // Arrange
    var price = BigDecimal.TEN;
    var auction =
        Auction.builder()
            .author(user)
            .price(price)
            .endTime(ZonedDateTime.now().plusMinutes(4))
            .build();
    given(auctionsRepository.save(Mockito.any(Auction.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = auctionsService.placeBid(auction);

    // Assert
    assertThat(result.getPrice()).isEqualTo(price.subtract(new BigDecimal("0.1")));
    assertThat(result.getEndTime())
        .isBetween(
            ZonedDateTime.now().plusMinutes(4).plusSeconds(59),
            ZonedDateTime.now().plusMinutes(5).plusSeconds(1));
  }

  @Test
  void placeBid_ReturnsBidWithEndTimePlusTwoAndHalfMin() {
    // Arrange
    var price = BigDecimal.TEN;
    var auction =
        Auction.builder()
            .author(user)
            .price(price)
            .endTime(ZonedDateTime.now().plusMinutes(1).plusSeconds(30))
            .build();
    given(auctionsRepository.save(Mockito.any(Auction.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = auctionsService.placeBid(auction);

    // Assert
    assertThat(result.getPrice()).isEqualTo(price.subtract(new BigDecimal("0.1")));
    assertThat(result.getEndTime())
        .isBetween(
            ZonedDateTime.now().plusMinutes(2).plusSeconds(29),
            ZonedDateTime.now().plusMinutes(2).plusSeconds(31));
  }

  @Test
  void placeBid_ReturnsBidWithEndTimePlusOneMin() {
    // Arrange
    var price = BigDecimal.TEN;
    var auction =
        Auction.builder()
            .author(user)
            .price(price)
            .endTime(ZonedDateTime.now().plusSeconds(30))
            .build();
    given(auctionsRepository.save(Mockito.any(Auction.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = auctionsService.placeBid(auction);

    // Assert
    assertThat(result.getPrice()).isEqualTo(price.subtract(new BigDecimal("0.1")));
    assertThat(result.getEndTime())
        .isBetween(
            ZonedDateTime.now().plusSeconds(59), ZonedDateTime.now().plusMinutes(1).plusSeconds(1));
  }

  @Test
  void changeAuction_ReturnsOK() {
    // Arrange
    var id = 1L;
    var auction = Auction.builder().price(BigDecimal.ONE).build();
    var newPrice = BigDecimal.TEN;
    var changeAuctionDto = ChangeAuctionDto.builder().id(id).price(newPrice).build();
    when(auctionsRepository.findById(id)).thenReturn(Optional.of(auction));

    // Act
    var result = auctionsService.changeAuction(changeAuctionDto);

    // Assert
    assertThat(result).isEqualTo("OK");
    assertThat(auction.getPrice()).isEqualTo(newPrice);
    verify(auctionsRepository, times(1)).save(Mockito.any(Auction.class));
  }
}
