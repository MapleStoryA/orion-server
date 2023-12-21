package server.autosave;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoSaver {

    private static AutoSaver instance;

    private AutoSaver() {}

    public static AutoSaver getInstance() {
        if (instance == null) {
            instance = new AutoSaver();
        }
        return instance;
    }

    public void executeSave(ChannelServer channel) {
        for (MapleCharacter player : channel.getPlayerStorage().getAllCharacters()) {
            player.saveToDB(false, false);
        }
    }
}
