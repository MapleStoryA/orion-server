package server.config;

import lombok.extern.slf4j.Slf4j;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;

import java.io.File;

@Slf4j
public class ServerConfig {

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


}
