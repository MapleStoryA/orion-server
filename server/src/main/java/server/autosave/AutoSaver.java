package server.autosave;

import client.MapleCharacter;
import handling.channel.ChannelServer;

public class AutoSaver {

  private static AutoSaver instance;

  private AutoSaver() {
  }

  public static AutoSaver getInstance() {
    if (instance == null) {
      instance = new AutoSaver();
    }
    return instance;
  }

  public void executeSave(ChannelServer channel) {
    for (MapleCharacter player : channel.getPlayerStorage().getAllCharacters()) {
      try {
        player.saveToDB(false, false);
      } finally {
        //System.out.println("Saved player: " + player.getName());
      }
    }
  }
}
