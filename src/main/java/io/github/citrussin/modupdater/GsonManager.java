package io.github.citrussin.modupdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonManager {
    public static final Gson prettyGson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();

    public static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static final Gson mapGson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .enableComplexMapKeySerialization()
            .create();

}
