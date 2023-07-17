package eu.attempto.dutch_auction_microservices.dashboard_service.core.bids;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.AuctionsService;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.dto.PlaceBidDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidsServiceTest {
  @Mock private BidsRepository bidsRepository;
  @Mock private UsersRepository usersRepository;
  @Mock private AuctionsService auctionsService;
  @Mock private SchedulesService schedulesService;
  @Mock private EventsService eventsService;
  @InjectMocks private BidsService bidsService;

  private User user;
  private Auction auction;

  @BeforeEach
  void setUp() {
    user =
        User.builder().id(1L).name("User").email("email@test.com").balance(BigDecimal.TEN).build();
    auction =
        Auction.builder()
            .title("Auction")
            .active(true)
            .author(new User())
            .price(BigDecimal.ONE)
            .build();
  }

  @Test
  void placeBid_ValidBidValidUser_ReturnsOk() {
    // Arrange
    var auctionId = 1L;
    var placeBidDto = PlaceBidDto.builder().auctionId(auctionId).build();
    when(auctionsService.getAuctionById(Mockito.any(Long.class))).thenReturn(auction);
    when(auctionsService.placeBid(Mockito.any(Auction.class))).thenReturn(auction);
    given(bidsRepository.save(Mockito.any(Bid.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = bidsService.placeBid(user, placeBidDto);

    // Assert
    assertThat(result).isEqualTo("OK");
    verify(auctionsService, times(1)).getAuctionById(auctionId);
    verify(auctionsService, times(1)).placeBid(auction);
    verify(usersRepository, times(1)).save(Mockito.any(User.class));
    verify(bidsRepository, times(1)).save(Mockito.any(Bid.class));
  }

  @Test
  void placeBid_NotActiveAuction_ThrowsBadRequest() {
    // Arrange
    var auctionId = 1L;
    var placeBidDto = PlaceBidDto.builder().auctionId(auctionId).build();
    auction.setActive(false);
    when(auctionsService.getAuctionById(Mockito.any(Long.class))).thenReturn(auction);

    // Act
    var thrown =
        assertThrows(
            BadRequestException.class,
            () -> bidsService.placeBid(user, placeBidDto),
            "Expected to throw that auction is not active");

    // Assert
    assertThat(thrown).hasMessage("This auction is not active");
  }

  @Test
  void placeBid_AuthorBidsOnHisAuction_ThrowsBadRequest() {
    // Arrange
    var auctionId = 1L;
    var placeBidDto = PlaceBidDto.builder().auctionId(auctionId).build();
    auction.setAuthor(user);
    when(auctionsService.getAuctionById(Mockito.any(Long.class))).thenReturn(auction);

    // Act
    var thrown =
        assertThrows(
            BadRequestException.class,
            () -> bidsService.placeBid(user, placeBidDto),
            "Expected to throw that user can not bet on his auction");

    // Assert
    assertThat(thrown).hasMessage("You can't bid on your own auction");
  }

  @Test
  void placeBid_UserDoesNotHaveEnoughCoins_ThrowsBadRequest() {
    // Arrange
    var auctionId = 1L;
    var placeBidDto = PlaceBidDto.builder().auctionId(auctionId).build();
    auction.setPrice(new BigDecimal(100));
    when(auctionsService.getAuctionById(Mockito.any(Long.class))).thenReturn(auction);

    // Act
    var thrown =
        assertThrows(
            BadRequestException.class,
            () -> bidsService.placeBid(user, placeBidDto),
            "Expected to throw that user does not have enough coins");

    // Assert
    assertThat(thrown).hasMessage("You don't have enough coins to make a bid and pay for the lot");
  }

  @Test
  void getUserBids_ReturnsUserBids() {
    // Arrange
    when(bidsRepository.findByUser(Mockito.any(User.class))).thenReturn(List.of());

    // Act
    var result = bidsService.getUserBids(user);

    // Assert
    assertThat(result).isEqualTo(List.of());
    verify(bidsRepository, times(1)).findByUser(Mockito.any(User.class));
  }

  @Test
  void deleteUserBids_DeletesAndReturnsUserBids() {
    // Arrange
    when(bidsRepository.deleteByUser(Mockito.any(User.class))).thenReturn(List.of());

    // Act
    var result = bidsService.deleteUserBids(user);

    // Assert
    assertThat(result).isEqualTo(List.of());
    verify(bidsRepository, times(1)).deleteByUser(Mockito.any(User.class));
  }
}
