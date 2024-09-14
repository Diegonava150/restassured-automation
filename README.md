# API Testing Framework

This project is an API testing framework built using Java, RestAssured, Cucumber, and JUnit. It is designed to provide a robust and flexible way to perform automated testing of APIs. The framework includes a set of predefined steps and hooks for common API operations, allowing for easy and reusable test scenarios.

## Project Structure

The project is organized as follows:

- `src/main/java/com/api/framework/models`: Contains model classes representing the data structures used in the API.
- `src/main/java/com/api/framework/requests`: Contains request classes that encapsulate the API calls.
- `src/main/java/com/api/framework/utils`: Contains utility classes, such as constants and schema validation.
- `src/test/java/com/testing/framework/stepDefinitions`: Contains the step definitions for Cucumber scenarios.
- `src/test/resources/features`: Contains the feature files that describe the test scenarios using Gherkin syntax.
- `schemas`: Contains JSON schema files used for response validation.

## Prerequisites

- Java 11 or higher
- Maven

## Setup

1. Clone the repository:
   git clone https://github.com/yourusername/api-testing-framework.git
   cd api-testing-framework
2. Install dependencies:
   mvn clean install
3. Run the tests:
   mvn test
