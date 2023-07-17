package eu.attempto.dutch_auction_microservices.db_service.bids;

import static org.assertj.core.api.Assertions.assertThat;

import eu.attempto.dutch_auction_microservices.db_service.auctions.Auction;
import eu.attempto.dutch_auction_microservices.db_service.auctions.AuctionsRepository;
import eu.attempto.dutch_auction_microservices.db_service.roles.Role;
import eu.attempto.dutch_auction_microservices.db_service.roles.RolesEnum;
import eu.attempto.dutch_auction_microservices.db_service.roles.RolesRepository;
import eu.attempto.dutch_auction_microservices.db_service.users.User;
import eu.attempto.dutch_auction_microservices.db_service.users.UsersRepository;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class BidsRepositoryTest {
  @Autowired private BidsRepository bidsRepository;
  @Autowired private UsersRepository usersRepository;
  @Autowired private RolesRepository rolesRepository;
  @Autowired private AuctionsRepository auctionsRepository;

  private Role role;
  private User user;
  private Auction auction;

  @BeforeEach
  void setUp() {
    rolesRepository.deleteAll();
    usersRepository.deleteAll();
    auctionsRepository.deleteAll();
    bidsRepository.deleteAll();

    role = Role.builder().name(RolesEnum.USER).build();
    rolesRepository.save(role);

    user = User.builder().name("User").role(role).build();
    usersRepository.save(user);

    auction = Auction.builder().title("Test").author(user).build();
    auctionsRepository.save(auction);
  }

  @Test
  void findByUser_UserWithNoBids_ReturnsEmptyList() {
    // Act
    var result = bidsRepository.findByUser(user.getId());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findByUser_UserWithBids_ReturnsBidsList() {
    // Arrange
    var bids =
        List.of(
            Bid.builder().user(user).auction(auction).build(),
            Bid.builder().user(user).auction(auction).build());
    bidsRepository.saveAll(bids);

    // Act
    var result = bidsRepository.findByUser(user.getId());

    // Assert
    assertThat(result).hasSize(2).isEqualTo(bids);
  }

  @Test
  void deleteByUser_UserWithNoBids_ReturnsEmptyList() {
    // Act
    var result = bidsRepository.deleteByUser(user.getId());
    var searchResultAfterDeletion = bidsRepository.findByUser(user.getId());

    // Assert
    assertThat(result).isEmpty();
    assertThat(searchResultAfterDeletion).isEmpty();
  }

  @Test
  void deleteByUser_UserWithBids_ReturnsBidsList() {
    // Arrange
    var bids =
        List.of(
            Bid.builder().user(user).auction(auction).build(),
            Bid.builder().user(user).auction(auction).build());
    bidsRepository.saveAll(bids);

    // Act
    var result = bidsRepository.deleteByUser(user.getId());
    var searchResultAfterDeletion = bidsRepository.findByUser(user.getId());

    // Assert
    assertThat(result).hasSize(2).isEqualTo(bids);
    assertThat(searchResultAfterDeletion).isEmpty();
  }

  @Test
  void findByAuctionOrderByCreatedAtDesc_AuctionHasNoBids_ReturnsEmptyList() {
    // Act
    var result = bidsRepository.findLastAuctionBid(auction.getId());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findByAuctionOrderByCreatedAtDesc_AuctionHasBids_ReturnsBidsListOrderedDescByCreatedAt() {
    // Arrange
    var otherUser = User.builder().name("Other user").role(role).build();
    usersRepository.save(otherUser);

    var bid = Bid.builder().user(user).auction(auction).build();
    bidsRepository.save(bid);
    var lastBid = Bid.builder().user(otherUser).auction(auction).build();
    bidsRepository.save(lastBid);

    // Act
    var result = bidsRepository.findLastAuctionBid(auction.getId());

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getCreatedAt()).isAfterOrEqualTo(bid.getCreatedAt());
  }
}
