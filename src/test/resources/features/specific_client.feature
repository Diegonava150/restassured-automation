@active
Feature: Manage client phone numbers

  Background:
    Given there are at least 10 registered clients in the system

  @smoke
  Scenario: Update and verify Laura's phone number
    When I retrieve the details of the client with name "Laura"
    And I save her current phone number
    And I send a PUT request to update the client with ID "1"
    """
    {
      "name": "Laura",
      "lastName": "Ospina",
      "country": "Colombia",
      "city": "Medellin",
      "phone": "41424141",
      "email": "laura1256@example.com"
    }
    """
    Then the client should have a status code of 200
    And the client should have the following details:
      | Name  | LastName | Country | City      | Id | Phone       | Email          |
      | Laura | Ospina    | Colombia   | Medellin | 1  | 41424141  | laura1256@example.com |
    And validates the response with client JSON schema

  @smoke
  Scenario: Clean up registered clients
    Then I delete all the registered clients
    And the client should have a status code of 200


