package eu.attempto.dutch_auction_microservices.backend_service.core.users;

import eu.attempto.dutch_auction_microservices.backend_service.core.users.dto.BuyCoinsDto;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.dto.CreateUserDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Users")
@RequestMapping("/users")
public class UsersController {
  private final UsersService usersService;

  @PostMapping
  public User createUser(@RequestBody CreateUserDto createUserDto) {
    return usersService.createUser(createUserDto);
  }

  @GetMapping("/me")
  public User getMe(Authentication authentication) {
    log.info(String.valueOf(authentication.getAuthorities()));
    return (User) authentication.getPrincipal();
  }

  @GetMapping
  public Page<User> getUsers(Pageable pageable) {
    return usersService.getUsers(pageable);
  }

  @DeleteMapping("/me")
  public String deleteMe(Authentication authentication) {
    return usersService.deleteMe((User) authentication.getPrincipal());
  }

  @PostMapping("/coins")
  public String buyCoins(
      Authentication authentication, @Valid @RequestBody BuyCoinsDto buyCoinsDto) {
    return usersService.buyCoins((User) authentication.getPrincipal(), buyCoinsDto);
  }
}
