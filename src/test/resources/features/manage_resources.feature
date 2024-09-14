@active
Feature: Manage resources

  Background:
    Given there are at least 5 active resources in the system

  @smoke
  Scenario: Get the list of active resources
    When I retrieve the list of all active resources
    Then the response should have a status code of 200
    And the response body should match the resources JSON schema
    And I update all the active resources as inactive