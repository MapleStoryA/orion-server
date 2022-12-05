package server.config;

import java.util.Map;

public interface FileLoader {

    String getExtension();

    Map<String, String> load(String path);
}
