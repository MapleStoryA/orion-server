package server;

import server.config.ServerEnvironment;

public class ServerProperties {


    public static String getProperty(String key) {
        return ServerEnvironment.getConfig().getProperty(key);
    }

    public static String getProperty(String s, String def) {
        return ServerEnvironment.getConfig().getProperty(s, def);
    }


}
