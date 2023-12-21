package scripting.v1.base;

import client.MapleClient;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import tools.MaplePacketCreator;
import tools.helper.Api;

@Slf4j
public class PlayerScripting extends BaseScripting {

    public PlayerScripting(MapleClient client) {
        super(client);
    }

    protected ChannelServer getChannelServer() {
        return WorldServer.getInstance().getChannel(client.getChannel());
    }

    @Api
    public void test() {
        sendPacket(MaplePacketCreator.updateQuestFinish(21015, 2005, 0));
    }

    @Api
    public long currentTime() {
        return System.currentTimeMillis();
    }

    @Api
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

    @Api
    public final void showBallon(final String msg, final int width, final int height) {
        sendPacket(MaplePacketCreator.sendHint(msg, width, height));
    }

    @Api()
    public void megaphone(String message, boolean whisper) {
        client.sendPacket(MaplePacketCreator.serverMessage(2, client.getChannel(), message, whisper));
    }

    @Api
    public void popup(String message) {
        sendPacket(MaplePacketCreator.serverMessage(1, client.getChannel(), message, false));
    }

    @Api
    public void pinkText(String message) {
        sendPacket(MaplePacketCreator.serverMessage(5, client.getChannel(), message, false));
    }

    @Api
    public void yellowSupermega(String message) {
        sendPacket(MaplePacketCreator.serverMessage(
                9, client.getChannel(), client.getPlayer().getName() + " : " + message, false));
    }
}
