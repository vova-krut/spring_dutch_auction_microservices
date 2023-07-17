package eu.attempto.dutch_auction_microservices.backend_service.core.roles;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RolesRepository {
  private final RestTemplate restTemplate;

  public Optional<Role> findByName(RolesEnum rolesEnum) {
    var role =
        restTemplate.getForObject(
            "http://db-service/roles/search/findByName?name={name}", Role.class, rolesEnum.name());

    return Optional.ofNullable(role);
  }

  public Role save(Role role) {
    if (role.getId() != null) {
      restTemplate.put("http://db-service/roles/{userId}", role, role.getId());
      return role;
    }

    return restTemplate.postForObject("http://db-service/roles", role, Role.class);
  }
}
