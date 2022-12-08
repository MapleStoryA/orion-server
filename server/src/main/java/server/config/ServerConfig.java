package server.config;

import lombok.extern.slf4j.Slf4j;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
        properties.forEach((key, value) -> mergedConfig.put(key, value));
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

    public String getScriptsPath() {
        return "config/scripts";
    }

    public boolean getBooleanProperty(String key) {
        return Boolean.valueOf(getProperty(key));
    }
}
