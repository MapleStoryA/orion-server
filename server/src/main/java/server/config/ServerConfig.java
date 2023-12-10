package server.config;

import java.io.File;
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
            var loader = new ConfigLoader(environment, Paths.get("config").toAbsolutePath());
            INSTANCE = loader.loadServerConfig();
        }
        return INSTANCE;
    }
}
