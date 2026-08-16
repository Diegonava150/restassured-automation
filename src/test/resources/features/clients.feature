Feature: Clients endpoint

  CRUD against /clients, with each happy path followed by the ways it can legitimately fail.
  The negative cases are the reason this suite runs against a real database: every rejection
  below comes from a Postgres constraint, and the error code asserted is the SQLSTATE the
  database raised.

  Background:
    Given there are at least 10 registered clients

  # ------------------------------------------------------------------ read

  @smoke
  Scenario: List every client
    When I request the list of clients
    Then the response status is 200
    And the response matches the client schema
    And every client has a non-empty "email"

  Scenario: Read a single client by id
    When I request the client with id "1"
    Then the response status is 200
    And the client in the response has:
      | name  | lastName | country  | city     | phone      | email                     |
      | Diego | Navarro  | Colombia | Medellin | 3003391905 | diego.navarro@example.com |
    And the response matches the client schema

  # A missing row is not an error in PostgREST — the filter simply matched nothing, and an
  # empty result set is a perfectly good answer to "give me the rows where id = 999999".
  # Asserting 404 here would be asserting the mock's old behaviour, not this API's.
  Scenario: Reading a client that does not exist returns an empty list, not an error
    When I request the client with id "999999"
    Then the response status is 200
    And the response is an empty list

  # ---------------------------------------------------------------- create

  @smoke
  Scenario: Create a client
    Given I have a client with the following details:
      | name    | lastName | country  | city   | phone      | email                     |
      | Valeria | Cardona  | Colombia | Bogota | 3012223344 | valeria.cardona@example.com |
    When I create the client
    Then the response status is 201
    And the created client has a server-assigned id
    And the response matches the client schema

  Scenario: Creating a client without a required field is rejected
    When I create a client with the body:
      """
      {"lastName": "NoName", "country": "Colombia", "city": "Cali", "phone": "3001112233", "email": "no.name@example.com"}
      """
    Then the response status is 400
    And the error code is "23502"

  @smoke
  Scenario: Creating a client with a duplicate email is rejected
    When I create a client with the body:
      """
      {"name": "Impostor", "lastName": "Navarro", "country": "Colombia", "city": "Medellin", "phone": "3009998877", "email": "diego.navarro@example.com"}
      """
    Then the response status is 409
    And the error code is "23505"

  Scenario: Creating a client with a malformed email is rejected
    When I create a client with the body:
      """
      {"name": "Bad", "lastName": "Email", "country": "Colombia", "city": "Cali", "phone": "3004445566", "email": "not-an-email"}
      """
    Then the response status is 400
    And the error code is "23514"

  Scenario: Creating a client from malformed JSON is rejected
    When I create a client with the body:
      """
      {"name": "Broken",
      """
    Then the response status is 400
    And the error code is "PGRST102"

  # ---------------------------------------------------------------- update

  Scenario: Update a client
    When I update the client with id "2" with the body:
      """
      {"city": "Cartagena", "phone": "3150000000"}
      """
    Then the response status is 200
    And the client in the response has:
      | name  | lastName | country  | city      | phone      | email                    |
      | Laura | Ospina   | Colombia | Cartagena | 3150000000 | laura.ospina@example.com |

  Scenario: Updating a client that does not exist changes nothing
    When I update the client with id "999999" with the body:
      """
      {"city": "Nowhere"}
      """
    Then the response status is 200
    And the response is an empty list

  Scenario: Updating a client into a duplicate email is rejected
    When I update the client with id "3" with the body:
      """
      {"email": "diego.navarro@example.com"}
      """
    Then the response status is 409
    And the error code is "23505"

  # ---------------------------------------------------------------- delete

  Scenario: Delete a client
    When I delete the client with id "10"
    Then the response status is 204
    And the client with id "10" no longer exists

  # DELETE is idempotent here: deleting nothing succeeds, because "ensure no row matches this
  # filter" is already true. Worth pinning down — it is the kind of behaviour a caller relies
  # on for retries and that a refactor could silently change.
  Scenario: Deleting a client that does not exist still succeeds
    When I delete the client with id "999999"
    Then the response status is 204
