package com.automation.api.models;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A resource row.
 *
 * <p>Types match the database rather than the old mock: {@code stock} is an integer,
 * {@code price} a {@code numeric(10,2)}, {@code id} a serial. Every one of these was a String
 * before, which is how the live mock ended up holding
 * {@code "stock": "Invalid faker method - da..."} — a value no typed column would have
 * accepted, sitting in a dataset the suite then validated against a schema demanding a number.
 *
 * <p>{@link BigDecimal} for price, not double: money compared with {@code ==} on a float is a
 * bug waiting for the first value that cannot be represented exactly.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    private Integer id;
    private String name;
    private String trademark;
    private Integer stock;
    private BigDecimal price;
    private String description;
    private String tags;
    private Boolean active;
}
