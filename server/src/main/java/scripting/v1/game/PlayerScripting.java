package scripting.v1.game;

import client.MapleClient;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import scripting.v1.game.helper.ScriptingApi;
import tools.MaplePacketCreator;

import java.util.ArrayList;
import java.util.List;

@lombok.extern.slf4j.Slf4j
public class PlayerScripting extends BaseScripting {

    public PlayerScripting(MapleClient client) {
        super(client);
    }

    protected ChannelServer getChannelServer() {
        return WorldServer.getInstance().getChannel(client.getChannel());
    }


    @ScriptingApi
    public void test() {
        sendPacket(MaplePacketCreator.updateQuestFinish(21015, 2005, 0));
    }

    @ScriptingApi
    public long currentTime() {
        return System.currentTimeMillis();
    }


    @ScriptingApi
    public String shuffle(int i, String str) {
        List<String> list = new ArrayList<>();
        for (char c : str.toCharArray()) {
            list.add(String.valueOf(c));
        }
        String ret = "";
        for (String c : list) {
            ret += c;
        }
        return ret;
    }

    @ScriptingApi
    public final void showBallon(final String msg, final int width, final int height) {
        sendPacket(MaplePacketCreator.sendHint(msg, width, height));
    }

    @ScriptingApi()
    public void megaphone(String message, boolean whisper) {
        client.sendPacket(MaplePacketCreator.serverMessage(2, client.getChannel(), message, whisper));
    }

    @ScriptingApi
    public void popup(String message) {
        sendPacket(MaplePacketCreator.serverMessage(1, client.getChannel(), message, false));
    }

    @ScriptingApi
    public void pinkText(String message) {
        sendPacket(MaplePacketCreator.serverMessage(5, client.getChannel(), message, false));
    }

    @ScriptingApi
    public void yellowSupermega(String message) {
        sendPacket(MaplePacketCreator.serverMessage(9, client.getChannel(), client.getPlayer().getName() + " : " + message, false));
    }


}
