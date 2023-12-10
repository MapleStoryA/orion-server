package server.config;

import java.io.File;
import java.nio.file.Path;

public class ConfigLoader {

    private final Environment environment;
    private final String folder;

    private final Path path;


    public ConfigLoader(Environment environment, Path path) {
        this.environment = environment;
        this.path = path;
        this.folder = path.toString() + "/env/" + environment.toString().toLowerCase();
    }


    public ServerConfig loadServerConfig() {
        if (!new File(folder).exists()) {
            throw new RuntimeException("The folder " + folder + " does not exist");
        }
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Config config = loader.load(folder + "/" + "config.yaml");
        return new ServerConfig(config);
    }

}
