package client.commands;

import client.MapleClient;
import handling.world.WorldServer;
import tools.ApiClass;
import tools.MaplePacketCreator;

@ApiClass
public class NoticeCommand implements Command {

    @Override
    public void execute(MapleClient c, String[] args) {
        String message = args[0];
        if (message == null) {
            c.getPlayer().dropMessage(6, "The syntax is !notice <message>");
        } else {
            byte[] packet = MaplePacketCreator.serverNotice(0, message);
            WorldServer.getInstance().getChannel(c.getChannel()).broadcastPacket(packet);
        }
    }

    @Override
    public String getTrigger() {
        return "notice";
    }
}
