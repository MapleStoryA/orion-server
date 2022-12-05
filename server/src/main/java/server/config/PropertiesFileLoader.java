package server.config;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesFileLoader implements FileLoader {
    @Override
    public String getExtension() {
        return "properties";
    }

    @Override
    public Map<String, String> load(String path) {
        return convertToMap(loadProperties(path));
    }

    private Properties loadProperties(String path) {
        try {
            var properties = new Properties();
            properties.load(new FileReader(path));
            return properties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> convertToMap(Properties loadProperties) {
        final Map<String, String> config = new HashMap<>();
        loadProperties.forEach((k, v) -> config.put(k.toString(), v.toString()));
        return config;
    }
}
