package eu.attempto.dutch_auction_microservices.backend_service.core.users;

import eu.attempto.dutch_auction_microservices.backend_service.core.auctions.AuctionsService;
import eu.attempto.dutch_auction_microservices.backend_service.core.bids.BidsService;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesEnum;
import eu.attempto.dutch_auction_microservices.backend_service.core.roles.RolesService;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.dto.BuyCoinsDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.dto.CreateUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UsersService {
  private final UsersRepository usersRepository;
  private final RolesService rolesService;
  private final AuctionsService auctionsService;
  private final BidsService bidsService;

  public Optional<User> findUserById(Long id) {
    return usersRepository.findById(id);
  }

  public Optional<User> findByEmail(String email) {
    return usersRepository.findByEmail(email);
  }

  public User createUser(CreateUserDto createUserDto) {
    var role = rolesService.getRole(createUserDto.isAdmin() ? RolesEnum.ADMIN : RolesEnum.USER);
    var user =
        User.builder()
            .email(createUserDto.getEmail())
            .name(createUserDto.getName())
            .role(role)
            .build();

    return usersRepository.save(user);
  }

  public Page<User> getUsers(Pageable pageable) {
    return usersRepository.findAll(pageable);
  }

  public String deleteMe(User user) {
    var future1 = CompletableFuture.supplyAsync(() -> auctionsService.deleteUserAuctions(user));
    var future2 = CompletableFuture.supplyAsync(() -> bidsService.deleteUserBids(user));

    // Wait for all tasks to complete
    CompletableFuture.allOf(future1, future2).join();

    usersRepository.delete(user);

    return "OK";
  }

  public String buyCoins(User user, BuyCoinsDto buyCoinsDto) {
    user.setBalance(user.getBalance().add(buyCoinsDto.getEuro().multiply(new BigDecimal(2))));
    usersRepository.save(user);

    return "OK";
  }
}
