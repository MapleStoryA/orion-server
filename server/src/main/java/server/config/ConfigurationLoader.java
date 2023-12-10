package server.config;

public interface ConfigurationLoader {

    String getExtension();

    Config load(String path);
}
