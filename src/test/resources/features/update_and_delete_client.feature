@active
Feature: Verify Clients endpoints

  @smoke
  Scenario: Create a new client
    Given I have a client with the following details:
      | Name | LastName | Country | City     | Phone      | Email         |
      | Diego | Navarro      | Colombia     | Medellin | 3003391905 | diegonava@example.com |
    When I send a POST request to create a client
    Then the client should have a status code of 201
    And the response should include the details of the created client
    And validates the response with client JSON schema

  @smoke
  Scenario: Read details of an existing client
    Given there are at least 10 registered clients in the system
    When I retrieve the details of the client with id "1"
    Then the client should have a status code of 200
    And the client should have the following details:
      | Name   | LastName | Country  | City   | Id | Phone       | Email           |
      | Diego | Navarro    | Colombia | Medellin | 1  | 3003391905  | diegonava@example.com |
    And validates the response with client JSON schema

  @smoke
  Scenario: Update client details
    Given there are at least 10 registered clients in the system
    And I retrieve the details of the client with name "Diego"
    When I send a PUT request to update the client with ID "1"
    """
    {
      "name": "Diego",
      "lastName": "Navarro",
      "country": "Colombia",
      "city": "Bogota",
      "phone": "3132131",
      "email": "diegonava1@example.com"
    }
    """
    Then the client should have a status code of 200
    And the client should have the following details:
      | Name  | LastName | Country | City      | Id | Phone       | Email          |
      | Diego | Navarro    | Colombia   | Bogota | 1  | 3132131  | diegonava1@example.com |
    And validates the response with client JSON schema

  @smoke @regression
  Scenario: Delete an existing client
    Given there are at least 10 registered clients in the system
    When I send a DELETE request to delete the client with ID "1"
    Then the client should have a status code of 200
    And the client should have the following details:
      | Name  | LastName | Country | City      | Id | Phone       | Email          |
      | Diego | Navarro    | Colombia   | Bogota | 1  | 3132131  | diegonava1@example.com |
    And validates the response with client JSON schema
    And I delete all the registered clients