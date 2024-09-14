package com.api.framework.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a resource with details such as name, trademark, stock, price, and other attributes.
 * <p>
 * This class uses Lombok annotations for reducing boilerplate code.
 * </p>
 *
 * <ul>
 *   <li>{@link Builder} - Provides a builder pattern for constructing instances of the class.</li>
 *   <li>{@link Data} - Generates getters, setters, toString, equals, and hashcode methods.</li>
 *   <li>{@link NoArgsConstructor} - Generates a no-argument constructor.</li>
 *   <li>{@link AllArgsConstructor} - Generates an all-arguments constructor.</li>
 * </ul>
 *
 * <pre>
 * Example usage:
 * {@code
 * Resource resource = Resource.builder()
 *                             .name("Laptop")
 *                             .trademark("BrandX")
 *                             .stock("50")
 *                             .price("1500.00")
 *                             .description("High-end gaming laptop")
 *                             .id("98765")
 *                             .tags("electronics,gaming,computer")
 *                             .active(true)
 *                             .build();
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
public class Resource {
    /**
     * The name of the resource.
     */
    private String name;

    /**
     * The trademark or brand associated with the resource.
     */
    private String trademark;

    /**
     * The number of units available in stock.
     */
    private String stock;

    /**
     * The price of the resource.
     */
    private String price;

    /**
     * A brief description of the resource.
     */
    private String description;

    /**
     * The unique identifier of the resource.
     */
    private String id;

    /**
     * Comma-separated tags associated with the resource.
     */
    private String tags;

    /**
     * Indicates whether the resource is active.
     */
    private Boolean active;
}