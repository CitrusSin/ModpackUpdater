package io.github.citrussin.modupdater.client.utils;

import com.google.gson.reflect.TypeToken;
import io.github.citrussin.modupdater.GsonManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18nUtils {
    public static Map<String, String> language;

    private static final Pattern langTokenPattern = Pattern.compile("\\$\\{(.*?)}");

    public static String getContext(String langToken) {
        return language.get(langToken);
    }

    public static String localize(String raw) {
        List<String> tokenList = new LinkedList<>();
        Matcher matcher = langTokenPattern.matcher(raw);
        while (matcher.find()) {
            tokenList.add(matcher.group(1));
        }

        String rep = raw;
        for (String token : tokenList) {
            rep = rep.replace("${"+token+"}", language.get(token));
        }
        return rep;
    }

    public static void loadLanguage(Locale locale) {
        InputStream inputStream =
                ClassLoader.getSystemClassLoader()
                        .getResourceAsStream(
                                String.format("assets/modupdater/lang/%s_%s.json",
                                        locale.getLanguage(),
                                        locale.getCountry()
                                )
                        );
        if (inputStream == null) {
            inputStream = ClassLoader.getSystemClassLoader()
                    .getResourceAsStream(
                            String.format("assets/modupdater/lang/%s.json",
                                    locale.getLanguage()
                            )
                    );
        }
        if (inputStream == null) {
            inputStream = ClassLoader.getSystemClassLoader()
                    .getResourceAsStream(
                            "assets/modupdater/lang/en_US.json"
                    );
        }
        assert inputStream != null;
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        language = GsonManager.mapGson.fromJson(reader, type);
    }
}
