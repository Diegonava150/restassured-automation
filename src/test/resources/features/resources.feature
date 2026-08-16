Feature: Resources endpoint

  Reads, filtering, a bulk update and the constraints that reject bad data.

  Background:
    Given there are at least 15 resources

  # ------------------------------------------------------------------ read

  @smoke
  Scenario: List every resource
    When I request the list of resources
    Then the response status is 200
    And the response matches the resource schema
    And every resource has a non-negative stock

  # The filter runs in Postgres (?active=is.true), not in the test. A suite that fetches
  # everything and filters in Java is not testing the API's filtering at all.
  Scenario: Filter to active resources only
    Given there are at least 5 active resources
    When I request the active resources
    Then the response status is 200
    And the response matches the resource schema

  # ---------------------------------------------------------------- update

  Scenario: Update the most recently created resource
    When I request the most recently created resource
    Then the response status is 200
    When I update that resource with:
      | name        | trademark | stock | price | description | tags       | active |
      | UpdatedName | NewBrand  | 10    | 99.99 | NewDesc     | UpdatedTag | true   |
    Then the response status is 200
    And the resource in the response has:
      | name        | trademark | stock | price | description | tags       | active |
      | UpdatedName | NewBrand  | 10    | 99.99 | NewDesc     | UpdatedTag | true   |
    And the response matches the resource schema

  Scenario: Deactivate every active resource in one request
    Given there are at least 5 active resources
    When I deactivate every active resource
    Then the response status is 200
    And no resources are active

  # ---------------------------------------------------------------- create

  @smoke
  Scenario: Create a resource
    When I create a resource with the body:
      """
      {"name": "Titanium Spork", "trademark": "Acme", "stock": 7, "price": 14.25, "description": "Camping cutlery", "tags": "outdoor", "active": true}
      """
    Then the response status is 201
    And the response matches the resource schema

  @smoke
  Scenario: Creating a resource with negative stock is rejected
    When I create a resource with the body:
      """
      {"name": "Impossible", "trademark": "Acme", "stock": -5, "price": 10.00, "description": "Negative stock", "tags": "", "active": true}
      """
    Then the response status is 400
    And the error code is "23514"

  Scenario: Creating a resource with a negative price is rejected
    When I create a resource with the body:
      """
      {"name": "Free Money", "trademark": "Acme", "stock": 1, "price": -0.01, "description": "Negative price", "tags": "", "active": true}
      """
    Then the response status is 400
    And the error code is "23514"

  # The old suite's mock happily stored the string "Invalid faker method - da..." in stock and
  # then validated it against a schema demanding a number. A typed column refuses it outright.
  Scenario: Creating a resource with a non-numeric stock is rejected
    When I create a resource with the body:
      """
      {"name": "Wrong Type", "trademark": "Acme", "stock": "not-a-number", "price": 5.00, "description": "Bad type", "tags": "", "active": true}
      """
    Then the response status is 400

  Scenario: Creating a resource with a blank name is rejected
    When I create a resource with the body:
      """
      {"name": "   ", "trademark": "Acme", "stock": 1, "price": 5.00, "description": "Blank name", "tags": "", "active": true}
      """
    Then the response status is 400
    And the error code is "23514"
