# Run the application

To run the application you need to add a keycloak server and a postgres database, default setup process:

### KEYCLOAK

Run a Keycloak container with `admin:admin` credentials

Create a realm named `dutch_auction` and a client `dutch_auction_client`. Also create 2 roles - `ADMIN` & `USER`

### POSTGRES

Run a postgres container with password `password` and a db named `dutch_auction`

## Alternative

Just setup everything yourself and change the settings in the application.yml of each project