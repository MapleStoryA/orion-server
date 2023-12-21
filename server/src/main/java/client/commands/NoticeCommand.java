package client.commands;

import client.MapleClient;
import handling.world.WorldServer;
import tools.MaplePacketCreator;
import tools.helper.Api;

@Api
public class NoticeCommand implements Command {

    @Override
    public int execute(MapleClient c, String[] args) {
        String message = args[0];
        if (message == null) {
            c.getPlayer().dropMessage(6, "The syntax is !notice <message>");
        } else {
            byte[] packet = MaplePacketCreator.serverNotice(0, message);
            WorldServer.getInstance().getChannel(c.getChannel()).broadcastPacket(packet);
        }
        return 1;
    }

    @Override
    public String getTrigger() {
        return "notice";
    }
}
