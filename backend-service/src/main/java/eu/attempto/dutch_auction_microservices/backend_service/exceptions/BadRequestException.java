package eu.attempto.dutch_auction_microservices.backend_service.exceptions;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
