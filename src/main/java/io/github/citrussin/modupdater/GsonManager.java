package io.github.citrussin.modupdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonManager {
    public static final Gson prettyGsonExcludeWithoutExpose = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();

    public static final Gson prettyGson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static final Gson gsonExcludeWithoutExpose = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static final Gson mapGson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();

    public static final Gson mapGsonExcludeWithoutExpose = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .enableComplexMapKeySerialization()
            .create();
}
