package com.automation.api.utils;

/**
 * Endpoint paths and header constants.
 *
 * <p>There is deliberately no {@code BASE_URL} here. It used to hold a hosted mock's address,
 * which meant the suite could only ever run against that one shared instance. The base URI is
 * now supplied at runtime from the mapped port of a container this suite starts — see
 * {@code ApiUnderTest} — so it is different on every run and cannot be a constant.
 *
 * <p>Paths are PostgREST's: the table name is the endpoint, and rows are selected with a
 * filter ({@code /clients?id=eq.1}) rather than a path segment ({@code /clients/1}).
 */
public final class Constants {

    public static final String VALUE_CONTENT_TYPE = "application/json";
    public static final String CONTENT_TYPE = "Content-Type";

    /** Asks PostgREST to return the affected rows instead of an empty body. */
    public static final String PREFER = "Prefer";

    public static final String RETURN_REPRESENTATION = "return=representation";

    public static final String CLIENTS_PATH = "/clients";
    public static final String RESOURCES_PATH = "/resources";

    /** PostgREST row filter, e.g. {@code /clients?id=eq.7}. */
    public static final String BY_ID = "%s?id=eq.%s";

    public static final String DEFAULT_CLIENT_FILE_PATH = "src/main/resources/data/defaultClient.json";
    public static final String DEFAULT_RESOURCE_FILE_PATH = "src/main/resources/data/defaultResource.json";

    private Constants() {}
}
