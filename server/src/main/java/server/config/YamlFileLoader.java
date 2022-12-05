package server.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlFileLoader implements FileLoader {
    @Override
    public String getExtension() {
        return "yaml";
    }

    @Override
    public Map<String, String> load(String path) {
        final var yaml = new Yaml(new SafeConstructor());
        try {
            Map<String, Map<String, String>> configs = yaml.load(new FileReader(path));
            Map<String, String> finalConfig = new HashMap<>();
            parseEntry("", configs, finalConfig);
            return finalConfig;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void parseEntry(String baseKey, Map<String, ? extends Object> parseEntry, Map<String, String> outputConfig) {
        final String separator = baseKey.length() == 0 ? "" : ".";
        for (var entry : parseEntry.entrySet()) {
            String base = baseKey + separator + entry.getKey();
            Object value = entry.getValue();
            if (value.getClass().equals(String.class)) {
                outputConfig.put(base, String.valueOf(value));
            } else {
                var casted = (Map<String, ?>) value;
                for (var subEntry : casted.entrySet()) {
                    if (LinkedHashMap.class.equals(subEntry.getValue().getClass())) {
                        parseEntry(base + "." + subEntry.getKey(), (Map<String, String>) subEntry.getValue(), outputConfig);
                    } else {
                        outputConfig.put(base + "." + subEntry.getKey(), String.valueOf(subEntry.getValue()));
                    }
                }
            }
        }
    }
}
