package io.github.citrussin.modupdater.cli;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandParser<T> {
    private final Class<T> targetClazz;

    public CommandParser(Class<T> clazz) {
        this.targetClazz = clazz;
    }

    public T parseCommand(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T inst = targetClazz.getConstructor().newInstance();

        // Store arguments and their corresponding fields as a list and a map
        List<Argument> arguments = new ArrayList<>();
        Map<Argument, Field> fieldMap = new HashMap<>();
        for (Field f : targetClazz.getDeclaredFields()) {
            Argument arg = f.getAnnotation(Argument.class);
            if (arg != null) {
                arguments.add(arg);
                fieldMap.put(arg, f);
            }
        }

        // Read args
        Map<String, String> options = new HashMap<>();
        List<String> normalArgs = new ArrayList<>();
        for (String s: args) {
            if (s.startsWith("--")) {
                String text = s.substring(2);
                String[] splits = text.split("=");
                if (splits.length == 1) {
                    options.put(text, "true");
                } else if (splits.length >= 2) {
                    options.put(splits[0], splits[1]);
                }
            } else if (s.startsWith("-")) {
                for (char c: s.substring(1).toCharArray()) {
                    options.put(String.valueOf(c), "true");
                }
            } else {
                normalArgs.add(s);
            }
        }

        // Set normal args and set options to false as default
        for (Argument arg: arguments) {
            Field f = fieldMap.get(arg);
            if (!arg.isOption()) {
                f.set(inst, f.getType().getConstructor(String.class).newInstance(normalArgs.get(arg.index())));
            } else {
                if (f.getType() == boolean.class || f.getType() == Boolean.class) {
                    f.set(inst, false);
                }
            }
        }

        // Set options
        for (Map.Entry<String, String> option: options.entrySet()) {
            Argument arg = searchOption(arguments, option.getKey());
            if (arg != null) {
                Field f = fieldMap.get(arg);
                if (f.getType() == boolean.class) {
                    f.set(inst, Boolean.valueOf(option.getValue()));
                } else {
                    f.set(inst, f.getType().getConstructor(String.class).newInstance(option.getValue()));
                }
            }
        }
        return inst;
    }

    private static Argument searchOption(List<Argument> arguments, String name) {
        for (Argument arg: arguments) {
            if (arg.isOption()) {
                if (arg.value().equals(name)) {
                    return arg;
                } else {
                    String[] aliases = arg.aliases();
                    for (String alias : aliases) {
                        if (alias.equals(name)) {
                            return arg;
                        }
                    }
                }
            }
        }
        return null;
    }
}
