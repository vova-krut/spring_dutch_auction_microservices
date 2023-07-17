package eu.attempto.dutch_auction_microservices.db_service.roles;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(RolesEnum name);
}
