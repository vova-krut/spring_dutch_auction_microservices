package eu.attempto.dutch_auction_microservices.backend_service.core.roles;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RolesService {
  private final RolesRepository rolesRepository;

  public Role getRole(RolesEnum name) {
    var role = rolesRepository.findByName(name);
    if (role.isPresent()) {
      return role.get();
    }

    var newRole = Role.builder().name(name).build();
    return rolesRepository.save(newRole);
  }
}
