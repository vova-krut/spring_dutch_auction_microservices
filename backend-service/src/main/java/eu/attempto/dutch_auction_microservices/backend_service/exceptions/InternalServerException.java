package eu.attempto.dutch_auction_microservices.backend_service.exceptions;

public class InternalServerException extends RuntimeException {
  public InternalServerException(String message) {
    super(message);
  }
}
