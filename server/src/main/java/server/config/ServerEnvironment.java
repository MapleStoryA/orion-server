package server.config;

import java.nio.file.Paths;

@lombok.extern.slf4j.Slf4j
public class ServerEnvironment {


    private static ServerConfig config = null;


    public static boolean isDebugEnabled() {
        return ServerEnvironment.getConfig().getBooleanProperty("debug");
    }


    public static boolean isSkillSavingEnabled() {
        return false;
    }

    public static synchronized ServerConfig getConfig() {
        if (config == null) {
            Environment environment = Environment.resolve();
            var loader = new ConfigLoader(environment, Paths.get("config").toAbsolutePath());
            config = loader.loadServerConfig();
        }
        return config;
    }
}
