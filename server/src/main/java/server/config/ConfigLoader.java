package server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@lombok.extern.slf4j.Slf4j
public class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private final Environment environment;
    private final String folder;

    private final Path path;

    private final Map<String, FileLoader> fileLoaderMap = new HashMap<>() {
        {
            var propertiesFileLoader = new PropertiesFileLoader();
            put(propertiesFileLoader.getExtension(), propertiesFileLoader);

            var yamlLoader = new YamlFileLoader();
            put(yamlLoader.getExtension(), yamlLoader);
        }
    };

    public ConfigLoader(Environment environment, Path path) {
        this.environment = environment;
        this.path = path;
        this.folder = path.toString() + "/env/" + environment.toString().toLowerCase();
    }

    private static String getExtension(String file) {
        var index = file.lastIndexOf('.');
        var extension = "";
        if (index > 0) {
            extension = file.substring(index + 1);
        }
        return extension;
    }

    public static void main(String[] args) {
        var loader = new ConfigLoader(Environment.LOCAL, Paths.get("config").toAbsolutePath());
        ServerConfig serverConfig = loader.loadServerConfig();
        String config = serverConfig.getProperty("login.host");
        log.info("Loaded config", serverConfig);
    }

    public ServerConfig loadServerConfig() {
        if (!new File(folder).exists()) {
            throw new RuntimeException("The folder " + folder + " does not exist");
        }
        final Map<String, Map<String, String>> propertiesMap = new HashMap<>();
        Stream.of(new File(folder).listFiles())
                .forEach(file -> {
                    var loader = fileLoaderMap.get(getExtension(file.getName()));
                    if (loader != null) {
                        propertiesMap.put(file.getName(), loader.load(folder + "/" + file.getName()));
                    }
                });
        return new ServerConfig(environment, path, propertiesMap);
    }

}
