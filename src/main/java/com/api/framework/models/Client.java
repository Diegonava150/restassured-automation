package com.api.framework.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a client with personal and contact information.
 * <p>
 * This class uses Lombok annotations for boilerplate code reduction.
 * </p>
 *
 * <ul>
 *   <li>{@link Builder} - Provides a builder pattern for the class.</li>
 *   <li>{@link Data} - Generates getters, setters, toString, equals, and hashcode methods.</li>
 *   <li>{@link NoArgsConstructor} - Generates a no-argument constructor.</li>
 *   <li>{@link AllArgsConstructor} - Generates an all-arguments constructor.</li>
 * </ul>
 *
 * <pre>
 * Example usage:
 * {@code
 * Client client = Client.builder()
 *                       .name("John")
 *                       .lastName("Doe")
 *                       .country("USA")
 *                       .city("New York")
 *                       .id("12345")
 *                       .phone("555-1234")
 *                       .email("john.doe@example.com")
 *                       .build();
 * }
 * </pre>
 *
 * @see lombok.Builder
 * @see lombok.Data
 * @see lombok.NoArgsConstructor
 * @see lombok.AllArgsConstructor
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    /**
     * The first name of the client.
     */
    private String name;

    /**
     * The last name of the client.
     */
    private String lastName;

    /**
     * The country of residence of the client.
     */
    private String country;

    /**
     * The city of residence of the client.
     */
    private String city;

    /**
     * The unique identifier of the client.
     */
    private String id;

    /**
     * The phone number of the client.
     */
    private String phone;

    /**
     * The email address of the client.
     */
    private String email;
}