package eu.attempto.dutch_auction_microservices.auth_service;

import eu.attempto.dutch_auction_microservices.auth_service.dto.*;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AuthService {
  @Value("${keycloak.server.realm}")
  private String realmName;

  @Value("${keycloak.server.client_id}")
  private String clientId;

  private final Keycloak keycloak;
  private final RestTemplate restTemplate;
  private final RestTemplate localRestTemplate;

  public AuthResponse registration(RegistrationDto registrationDto, boolean isAdmin) {
    var future1 =
        CompletableFuture.runAsync(() -> registerUserInKeycloak(registrationDto, isAdmin));
    var future2 = CompletableFuture.runAsync(() -> registerUserInBackend(registrationDto, isAdmin));

    CompletableFuture.allOf(future1, future2).join();

    return localRestTemplate.postForObject(
        "http://localhost:8181/realms/{realmName}/protocol/openid-connect/token",
        getAuthRequest(registrationDto.getEmail(), registrationDto.getPassword()),
        AuthResponse.class,
        realmName);
  }

  private void registerUserInKeycloak(RegistrationDto registrationDto, boolean isAdmin) {
    var userRepresentation = new UserRepresentation();
    userRepresentation.setUsername(registrationDto.getEmail());
    userRepresentation.setFirstName(registrationDto.getName());
    userRepresentation.setEnabled(true);

    var credentialRepresentation = new CredentialRepresentation();
    credentialRepresentation.setType("password");
    credentialRepresentation.setValue(registrationDto.getPassword());
    credentialRepresentation.setTemporary(false);
    userRepresentation.setCredentials(List.of(credentialRepresentation));

    keycloak.realm(realmName).users().create(userRepresentation);
    addRealmRoleToUser(registrationDto.getEmail(), isAdmin);
  }

  private void addRealmRoleToUser(String username, boolean isAdmin) {
    var role = isAdmin ? "ADMIN" : "USER";
    var userRepresentation = keycloak.realm(realmName).users().search(username).get(0);
    var userResource = keycloak.realm(realmName).users().get(userRepresentation.getId());
    var rolesToAdd = List.of(keycloak.realm(realmName).roles().get(role).toRepresentation());

    userResource.roles().realmLevel().add(rolesToAdd);
  }

  private void registerUserInBackend(RegistrationDto registrationDto, boolean isAdmin) {
    var createBackendUserDto =
        CreateBackendUserDto.builder()
            .email(registrationDto.getEmail())
            .name(registrationDto.getName())
            .isAdmin(isAdmin)
            .build();
    restTemplate.postForObject("http://backend-service/users", createBackendUserDto, Object.class);
  }

  private MultiValueMap<String, String> getAuthRequest(String username, String password) {
    var body = new LinkedMultiValueMap<String, String>();
    body.put("username", List.of(username));
    body.put("password", List.of(password));
    body.put("grant_type", List.of("password"));
    body.put("client_id", List.of(clientId));

    return body;
  }

  public AuthResponse login(LoginDto loginDto) {
    return localRestTemplate.postForObject(
        "http://localhost:8181/realms/{realmName}/protocol/openid-connect/token",
        getAuthRequest(loginDto.getEmail(), loginDto.getPassword()),
        AuthResponse.class,
        realmName);
  }

  public String createDefaultAdmin() {
    var registrationDto =
        RegistrationDto.builder()
            .email("admin@attempto.eu")
            .name("Admin")
            .password("Administrator")
            .build();

    registerUserInKeycloak(registrationDto, true);
    return "OK";
  }
}
