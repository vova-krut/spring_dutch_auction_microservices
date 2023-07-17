package eu.attempto.dutch_auction_microservices.backend_service.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Date;

public class FutureDateValidator implements ConstraintValidator<FutureDate, Long> {

  @Override
  public boolean isValid(Long value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    var date = new Date(value);
    var now = new Date();
    return date.after(now);
  }
}
