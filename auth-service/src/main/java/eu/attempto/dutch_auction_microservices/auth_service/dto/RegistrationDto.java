package eu.attempto.dutch_auction_microservices.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDto {
  @NotBlank @Email private String email;

  @NotBlank
  @Length(min = 4)
  private String password;

  @NotBlank private String name;
}
