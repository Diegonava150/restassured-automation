@active
Feature: Manage resources

  Background:
    Given there are at least 15 resources in the system

  @smoke
  Scenario: Update the last created resource
    When I retrieve the last created resource
    And I update all the parameters of the last created resource with the following details
      | name        | trademark | stock | price | description | tags       | active |
      | UpdatedName | NewBrand  | 10    | 99.99 | NewDesc     | UpdatedTag | true   |
    Then the response should have a status code of 200
    And the response body should match the resource JSON schema
    And the response body should contain the updated data