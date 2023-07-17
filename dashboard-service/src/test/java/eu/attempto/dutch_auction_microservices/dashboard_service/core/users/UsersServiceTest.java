package eu.attempto.dutch_auction_microservices.dashboard_service.core.users;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.Auction;
import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.AuctionsService;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.Bid;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.BidsService;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.Role;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesEnum;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesService;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.dto.BuyCoinsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {
  @Mock private UsersRepository usersRepository;
  @Mock private RolesService rolesService;
  @Mock private AuctionsService auctionsService;
  @Mock private BidsService bidsService;
  @InjectMocks private UsersService usersService;

  private User user;

  @BeforeEach
  void setUp() {
    user = User.builder().name("User").email("email@test.com").build();
  }

  @Test
  void findUserById_ExistingId_ReturnsUser() {
    // Arrange
    var userId = 1L;
    when(usersRepository.findById(userId)).thenReturn(Optional.of(user));

    // Act
    var result = usersService.findUserById(userId);

    // Assert
    assertThat(result).isPresent().contains(user);
  }

  @Test
  void findUserById_NonExistingId_ReturnsEmptyOptional() {
    // Arrange
    var userId = 1L;
    when(usersRepository.findById(userId)).thenReturn(Optional.empty());

    // Act
    var result = usersService.findUserById(userId);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void findByEmail_ExistingEmail_ReturnsUser() {
    // Arrange
    var email = "email@test.com";
    when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));

    // Act
    var result = usersService.findByEmail(email);

    // Assert
    assertThat(result).isPresent().contains(user);
  }

  @Test
  void findByEmail_NonExistingEmail_ReturnsEmptyOptional() {
    // Arrange
    var email = "test@example.com";
    when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

    // Act
    var result = usersService.findByEmail(email);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void createUser_Admin_ReturnsCreatedUserWithAdminRole() {
    // Arrange
    when(rolesService.getRole(RolesEnum.ADMIN))
        .thenReturn(Role.builder().name(RolesEnum.ADMIN).build());
    given(usersRepository.save(Mockito.any(User.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = usersService.createUser(user, true);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getRole().getName()).isEqualTo(RolesEnum.ADMIN);
    verify(usersRepository, times(1)).save(user);
  }

  @Test
  void createUser_NotAdmin_ReturnsCreatedUserWithUserRole() {
    // Arrange
    when(rolesService.getRole(RolesEnum.USER))
        .thenReturn(Role.builder().name(RolesEnum.USER).build());
    given(usersRepository.save(Mockito.any(User.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Act
    var result = usersService.createUser(user, false);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getRole().getName()).isEqualTo(RolesEnum.USER);
    verify(usersRepository, times(1)).save(user);
  }

  @Test
  void getUsers_ReturnsUserPage() {
    // Arrange
    var pageable = Pageable.unpaged();
    var userList = new ArrayList<User>();
    userList.add(new User());
    userList.add(new User());
    var expectedPage = new PageImpl<>(userList);
    when(usersRepository.findAll(pageable)).thenReturn(expectedPage);

    // Act
    var result = usersService.getUsers(pageable);

    // Assert
    assertThat(result).isNotNull().isEqualTo(expectedPage);
  }

  @Test
  void deleteMe_DeletesUserAndRelatedAuctionsAndBids() {
    // Arrange
    var auctions = List.of(new Auction(), new Auction());
    var bids = List.of(new Bid(), new Bid());

    when(auctionsService.deleteUserAuctions(user)).thenReturn(auctions);
    when(bidsService.deleteUserBids(user)).thenReturn(bids);

    // Act
    var result = usersService.deleteMe(user);

    // Assert
    assertThat(result).isEqualTo("OK");
    verify(auctionsService, times(1)).deleteUserAuctions(user);
    verify(bidsService, times(1)).deleteUserBids(user);
    verify(usersRepository, times(1)).delete(user);
  }

  @Test
  void buyCoins_IncreasesUserBalance() {
    // Arrange
    var buyCoinsDto = new BuyCoinsDto();
    buyCoinsDto.setEuro(BigDecimal.TEN);

    var initialBalance = user.getBalance();
    var expectedBalance = initialBalance.add(buyCoinsDto.getEuro().multiply(new BigDecimal(2)));

    // Act
    var result = usersService.buyCoins(user, buyCoinsDto);

    // Assert
    assertThat(result).isEqualTo("OK");
    assertThat(user.getBalance()).isEqualTo(expectedBalance);
    verify(usersRepository, times(1)).save(user);
  }
}
