package com.automation.api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A client row.
 *
 * <p>{@code id} is an {@link Integer}, not a String. It is a Postgres {@code serial} and
 * PostgREST serialises it as a JSON number — the old hosted mock returned {@code "id": "7"}
 * as a string, and the models and JSON schemas were shaped around that. Anything asserting on
 * a string id was asserting on the mock's quirk rather than on an API contract.
 *
 * <p>Left null when creating: the database assigns it.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    private Integer id;
    private String name;
    private String lastName;
    private String country;
    private String city;
    private String phone;
    private String email;
}
