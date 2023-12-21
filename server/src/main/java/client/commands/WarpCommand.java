package client.commands;

import client.MapleCharacter;
import client.MapleClient;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import tools.helper.Api;

@Api
class WarpCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        MapleCharacter victim = null;
        for (ChannelServer ch : WorldServer.getInstance().getAllChannels()) {
            for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
                if (chr.getName().toLowerCase().contains(args[0].toLowerCase())) {
                    victim = chr;
                }
            }
        }
        if (victim != null) {
            c.getPlayer().changeMap(victim.getMap(), victim.getPosition());
        } else {
            c.getPlayer().dcolormsg(5, "The player was not found online");
        }
    }

    @Override
    public String getTrigger() {
        return "warp";
    }
}
