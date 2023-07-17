package eu.attempto.dutch_auction_microservices.backend_service.core.users;

import eu.attempto.dutch_auction_microservices.backend_service.core.roles.Role;
import eu.attempto.dutch_auction_microservices.backend_service.core.users.dto.UserRequest;
import eu.attempto.dutch_auction_microservices.backend_service.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsersRepository {
  private final RestTemplate restTemplate;

  public Optional<User> findById(Long id) {
    var user =
        restTemplate.getForObject(
            "http://db-service/users/{id}?projection=userProjection", User.class, id);

    return Optional.ofNullable(user);
  }

  public Optional<User> findByEmail(String email) {
    var user =
        restTemplate.getForObject(
            "http://db-service/users/search/findByEmail?projection=userProjection&email={email}",
            User.class,
            email);

    return Optional.ofNullable(user);
  }

  public Page<User> findAll(Pageable pageable) {
    var uriVars = new HashMap<String, Object>();
    uriVars.put("page", pageable.getPageNumber());
    uriVars.put("size", pageable.getPageSize());
    uriVars.put("sort", pageable.getSort());
    var pagedModel =
        restTemplate
            .exchange(
                "http://db-service/users?page={page}&size={size}&sort={sort}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedModel<User>>() {},
                uriVars)
            .getBody();

    if (pagedModel == null || pagedModel.getMetadata() == null) {
      return Page.empty();
    }
    return new PageImpl<>(
        pagedModel.getContent().stream().toList(),
        pageable,
        pagedModel.getMetadata().getTotalElements());
  }

  public User save(User user) {
    var userRequest = buildUserRequest(user);
    if (userRequest.getId() != null) {
      restTemplate.put("http://db-service/users/{userId}", userRequest, userRequest.getId());
      return user;
    }

    var userResponse =
        restTemplate.postForObject("http://db-service/users", userRequest, User.class);
    if (userResponse == null) {
      throw new BadRequestException("Could not save user");
    }

    return userResponse;
  }

  private UserRequest buildUserRequest(User user) {
    var role = user.getRole();
    var userRequest =
        UserRequest.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .balance(user.getBalance())
            .build();
    if (role != null) {
      userRequest.setRole(getRoleLink(role.getId()));
    }

    return userRequest;
  }

  private String getRoleLink(Long roleId) {
    var roleEntityModel =
        restTemplate
            .exchange(
                "http://db-service/roles/{id}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<EntityModel<Role>>() {},
                roleId)
            .getBody();

    if (roleEntityModel == null) {
      throw new BadRequestException("Role with this id was not found");
    }

    return roleEntityModel.getRequiredLink("self").getHref();
  }

  public void delete(User user) {
    restTemplate.delete("http://db-service/users/{userId}", user.getId());
  }
}
