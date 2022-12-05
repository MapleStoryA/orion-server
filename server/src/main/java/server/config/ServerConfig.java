package server.config;

import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ServerConfig {

    private final Environment environment;

    private final Path path;
    private final Map<String, String> mergedConfig;
    private final Map<String, Map<String, String>> originalConfig;


    public ServerConfig(Environment environment, Path path, Map<String, Map<String, String>> originalConfig) {
        this.environment = environment;
        this.path = path;
        this.mergedConfig = new HashMap<>();
        this.originalConfig = originalConfig;
        originalConfig.values()
                .stream()
                .forEach(this::loadConfig);
    }

    private void loadConfig(Map<String, String> properties) {
        properties.forEach((key, value) -> mergedConfig.put(key.toString(), value.toString()));
    }

    public String getProperty(String key) {
        return this.mergedConfig.get(key);
    }

    public String getProperty(String key, String defaultValue) {
        return this.mergedConfig.getOrDefault(key, defaultValue);
    }

    public MapleDataProvider getDataProvider(String path) {
        return MapleDataProviderFactory.getDataProvider(new File("config/" + path));
    }

    public Properties getFileAsProperties(String file) {
        final var properties = new Properties();
        originalConfig.computeIfPresent(file, (e, v) -> {
            properties.putAll(v);
            return null;
        });
        return properties;
    }
}
