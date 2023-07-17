package eu.attempto.dutch_auction_microservices.dashboard_service.core.auctions;

import eu.attempto.dutch_auction_microservices.backend_service.core.roles.Role;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesEnum;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesRepository;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.User;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class AuctionsRepositoryTest {
  @Autowired private AuctionsRepository auctionsRepository;
  @Autowired private RolesRepository rolesRepository;
  @Autowired private UsersRepository usersRepository;

  private User user;

  @BeforeEach
  void setUp() {
    rolesRepository.deleteAll();
    usersRepository.deleteAll();
    auctionsRepository.deleteAll();

    var role = Role.builder().name(RolesEnum.USER).build();
    rolesRepository.save(role);

    user = User.builder().name("User").role(role).build();
    usersRepository.save(user);
  }

  @Test
  void deleteByAuthor_AuthorWithNoAuctions_ReturnsEmptyList() {
    // Act
    var result = auctionsRepository.deleteByAuthor(user);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void deleteByAuthor_AuthorWithAuctions_ReturnsDeletedAuctions() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder().author(user).title("Title").build(),
            Auction.builder().author(user).title("Other title").build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.deleteByAuthor(user);
    var searchResultAfterDeletion = auctionsRepository.findAll();

    // Assert
    assertThat(result).hasSize(2).isEqualTo(auctions);
    assertThat(searchResultAfterDeletion).isEmpty();
  }

  @Test
  void findByActiveTrueAndEndTimeAfter_EndTimeInFutureAndIsActive() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder()
                .author(user)
                .active(true)
                .title("Title")
                .endTime(ZonedDateTime.now().plusMinutes(60))
                .build(),
            Auction.builder()
                .author(user)
                .active(true)
                .title("Other title")
                .endTime(ZonedDateTime.now().plusMinutes(30))
                .build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.findByActiveTrueAndEndTimeAfter(ZonedDateTime.now());

    // Assert
    assertThat(result).hasSize(2).isEqualTo(auctions);
  }

  @Test
  void findByActiveTrueAndEndTimeAfter_EndTimeInFutureAndOneIsActive() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder()
                .author(user)
                .active(false)
                .title("Title")
                .endTime(ZonedDateTime.now().plusMinutes(60))
                .build(),
            Auction.builder()
                .author(user)
                .active(true)
                .title("Other title")
                .endTime(ZonedDateTime.now().plusMinutes(30))
                .build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.findByActiveTrueAndEndTimeAfter(ZonedDateTime.now());

    // Assert
    assertThat(result).hasSize(1).isEqualTo(List.of(auctions.get(1)));
  }

  @Test
  void findByActiveTrueAndEndTimeAfter_EndTimeInPastAndOneIsActive() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder()
                .author(user)
                .active(false)
                .title("Title")
                .endTime(ZonedDateTime.now().minusMinutes(60))
                .build(),
            Auction.builder()
                .author(user)
                .active(true)
                .title("Other title")
                .endTime(ZonedDateTime.now().minusMinutes(30))
                .build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.findByActiveTrueAndEndTimeAfter(ZonedDateTime.now());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findByActiveFalseAndStartTimeBefore_StartTimeInPastAndIsNotActive() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder()
                .author(user)
                .active(false)
                .title("Title")
                .startTime(ZonedDateTime.now().minusMinutes(60))
                .build(),
            Auction.builder()
                .author(user)
                .active(false)
                .title("Other title")
                .startTime(ZonedDateTime.now().minusMinutes(30))
                .build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.findByActiveFalseAndStartTimeBefore(ZonedDateTime.now());

    // Assert
    assertThat(result).hasSize(2).isEqualTo(auctions);
  }

  @Test
  void findByActiveFalseAndStartTimeBefore_StartTimeInPastAndOneIsNotActive() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder()
                .author(user)
                .active(true)
                .title("Title")
                .startTime(ZonedDateTime.now().minusMinutes(60))
                .build(),
            Auction.builder()
                .author(user)
                .active(false)
                .title("Other title")
                .startTime(ZonedDateTime.now().minusMinutes(30))
                .build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.findByActiveFalseAndStartTimeBefore(ZonedDateTime.now());

    // Assert
    assertThat(result).hasSize(1).isEqualTo(List.of(auctions.get(1)));
  }

  @Test
  void findByActiveFalseAndStartTimeBefore_StartTimeInFutureAndOneIsNotActive() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder()
                .author(user)
                .active(true)
                .title("Title")
                .startTime(ZonedDateTime.now().plusMinutes(60))
                .build(),
            Auction.builder()
                .author(user)
                .active(false)
                .title("Other title")
                .startTime(ZonedDateTime.now().plusMinutes(30))
                .build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.findByActiveFalseAndStartTimeBefore(ZonedDateTime.now());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findByActiveTrueAndEndTimeLessThanEqual_EndTimeInPastAndIsActive() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder()
                .author(user)
                .active(true)
                .title("Title")
                .endTime(ZonedDateTime.now().minusMinutes(60))
                .build(),
            Auction.builder()
                .author(user)
                .active(true)
                .title("Other title")
                .endTime(ZonedDateTime.now().minusMinutes(30))
                .build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.findByActiveTrueAndEndTimeLessThanEqual(ZonedDateTime.now());

    // Assert
    assertThat(result).hasSize(2).isEqualTo(auctions);
  }

  @Test
  void findByActiveTrueAndEndTimeLessThanEqual_EndTimeInPastAndOneIsActive() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder()
                .author(user)
                .active(false)
                .title("Title")
                .endTime(ZonedDateTime.now().minusMinutes(60))
                .build(),
            Auction.builder()
                .author(user)
                .active(true)
                .title("Other title")
                .endTime(ZonedDateTime.now().minusMinutes(30))
                .build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.findByActiveTrueAndEndTimeLessThanEqual(ZonedDateTime.now());

    // Assert
    assertThat(result).hasSize(1).isEqualTo(List.of(auctions.get(1)));
  }

  @Test
  void findByActiveTrueAndEndTimeLessThanEqual_EndTimeInFutureAndOneIsActive() {
    // Arrange
    var auctions =
        List.of(
            Auction.builder()
                .author(user)
                .active(false)
                .title("Title")
                .endTime(ZonedDateTime.now().plusMinutes(60))
                .build(),
            Auction.builder()
                .author(user)
                .active(true)
                .title("Other title")
                .endTime(ZonedDateTime.now().plusMinutes(30))
                .build());
    auctionsRepository.saveAll(auctions);

    // Act
    var result = auctionsRepository.findByActiveTrueAndEndTimeLessThanEqual(ZonedDateTime.now());

    // Assert
    assertThat(result).isEmpty();
  }
}
