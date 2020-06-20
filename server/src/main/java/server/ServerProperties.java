package server;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Emilyx3
 */
public class ServerProperties {

  private static final Properties props = new Properties();
  private static final String[] toLoad = {
      "dist/world.properties",
      "dist/db.properties",
      "dist/login.properties",
      "dist/channel.properties"
  };

  private ServerProperties() {
  }

  static {
    load();
  }

  public static String getProperty(String s) {
    return props.getProperty(s);
  }

  public static void setProperty(String prop, String newInf) {
    props.setProperty(prop, newInf);
  }

  public static String getProperty(String s, String def) {
    return props.getProperty(s, def);
  }


  public static void load() {
    for (String s : toLoad) {
      FileReader fr;
      try {
        fr = new FileReader(s);
        props.load(fr);
        fr.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
