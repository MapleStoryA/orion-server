package server.base.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlConfigurationLoader {

    public String getExtension() {
        return "yaml";
    }

    public Config load(String path) {
        final var yaml = new Yaml(new Constructor(Config.class));
        try {
            Config config = yaml.loadAs(new FileReader(path), Config.class);

            return config;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
