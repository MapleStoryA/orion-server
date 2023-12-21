package server.base.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;

@Slf4j
public class ServerConfig {

    private static ServerConfig INSTANCE = null;

    private final Config configuration;

    public ServerConfig(Config configuration) {
        this.configuration = configuration;
    }

    public MapleDataProvider getDataProvider(String path) {
        return MapleDataProviderFactory.getDataProvider(new File("config/" + path));
    }

    public String getScriptsPath() {
        return "config/scripts";
    }

    public Config getConfig() {
        return configuration;
    }

    public static boolean isDebugEnabled() {
        return INSTANCE.getConfig().isDebug();
    }

    public static boolean isSkillSavingEnabled() {
        return false;
    }

    public static synchronized ServerConfig serverConfig() {
        if (INSTANCE == null) {
            Environment environment = Environment.resolve();
            INSTANCE = loadServerConfig(environment, Paths.get("config").toAbsolutePath());
        }
        return INSTANCE;
    }

    public static ServerConfig loadServerConfig(Environment environment, Path path) {
        String folder = path.toString() + "/env/" + environment.toString().toLowerCase();
        if (!new File(folder).exists()) {
            throw new RuntimeException("The folder " + folder + " does not exist");
        }
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Config config = loader.load(folder + "/" + "config.yaml");
        return new ServerConfig(config);
    }
}
