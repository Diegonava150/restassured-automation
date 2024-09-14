package com.api.framework.utils;

import com.google.gson.Gson;
import com.api.framework.models.Client;
import com.api.framework.models.Resource;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * JsonFileReader class provides methods to read JSON files and deserialize them into Java objects.
 * <p>
 * This utility class uses Gson for JSON parsing.
 * </p>
 * Example usage:
 * {@code
 * JsonFileReader jsonFileReader = new JsonFileReader();
 * Client client = jsonFileReader.getClientByJson("path/to/client.json");
 * }
 */
public class JsonFileReader {

    /**
     * Reads a JSON file and deserializes its content into a Client object.
     *
     * @param jsonFileName The file path of the JSON file.
     * @return A Client object deserialized from the JSON file.
     */
    public Client getClientByJson(String jsonFileName) {
        Client client = null;
        try (Reader reader = new FileReader(jsonFileName)) {
            Gson gson = new Gson();
            client = gson.fromJson(reader, Client.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return client;
    }

    /**
     * Reads a JSON file and deserializes its content into a Resource object.
     *
     * @param jsonFileName The file path of the JSON file.
     * @return A Resource object deserialized from the JSON file.
     */
    public Resource getResourceByJson(String jsonFileName) {
        Resource resource = null;
        try (Reader reader = new FileReader(jsonFileName)) {
            Gson gson = new Gson();
            resource = gson.fromJson(reader, Resource.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resource;
    }
}