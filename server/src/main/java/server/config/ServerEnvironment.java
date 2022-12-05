package server.config;

import java.nio.file.Paths;

public class ServerEnvironment {


    private static ServerConfig config = null;


    public static boolean isDebugEnabled() {
        return false;
    }


    public static boolean isSkillSavingEnabled() {
        return false;
    }

    public static synchronized ServerConfig getConfig() {
        if (config == null) {
            var loader = new ConfigLoader(Environment.LOCAL, Paths.get("config").toAbsolutePath());
            config = loader.loadServerConfig();
        }
        return config;
    }
}
