package eu.attempto.dutch_auction_microservices.backend_service.schedules;

import eu.attempto.dutch_auction_microservices.backend_service.core.users.UsersService;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.dto.CreateUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnApplicationRun implements ApplicationRunner {
  private final SchedulesService schedulesService;
  private final RestTemplate restTemplate;
  private final UsersService usersService;

  @Override
  public void run(ApplicationArguments args) {
    var createUserDto =
        CreateUserDto.builder().email("admin@attempto.eu").name("Admin").isAdmin(true).build();
    restTemplate.getForObject("http://auth-service/default-admin", String.class);
    usersService.createUser(createUserDto);
    log.info("Created default admin");

    schedulesService.onApplicationStart();
    log.info("Start required auctions");
  }
}
