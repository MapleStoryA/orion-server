package client.commands;

import client.MapleClient;
import handling.world.WorldServer;
import tools.helper.Scripting;

@Scripting
class OnlineCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        c.getPlayer().dropMessage(6, "Total amount of players connected to server:");
        c.getPlayer().dropMessage(6, "" + WorldServer.getInstance().getConnected() + "");
        c.getPlayer().dropMessage(6, "Characters connected to channel " + c.getChannel() + ":");
        c.getPlayer().dropMessage(6, c.getChannelServer().getPlayerStorage().getOnlinePlayers(true));
    }

    @Override
    public String getTrigger() {
        return "online";
    }
}
