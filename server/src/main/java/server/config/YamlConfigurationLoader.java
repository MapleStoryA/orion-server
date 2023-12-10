package server.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.FileNotFoundException;
import java.io.FileReader;

@lombok.extern.slf4j.Slf4j
public class YamlConfigurationLoader implements ConfigurationLoader {


    @Override
    public String getExtension() {
        return "yaml";
    }

    @Override
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
