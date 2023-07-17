package eu.attempto.dutch_auction_microservices.auth_service;

import eu.attempto.dutch_auction_microservices.auth_service.dto.AuthResponse;
import eu.attempto.dutch_auction_microservices.auth_service.dto.LoginDto;
import eu.attempto.dutch_auction_microservices.auth_service.dto.RegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/registration")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse registration(@Valid @RequestBody RegistrationDto registrationDto) {
    return authService.registration(registrationDto, false);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginDto loginDto) {
    return authService.login(loginDto);
  }

  @GetMapping("/default-admin")
  public String createDefaultAdmin() {
    return authService.createDefaultAdmin();
  }
}
