package client.commands;

import client.MapleCharacter;
import client.MapleClient;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import java.util.LinkedList;
import java.util.List;
import server.maps.MapleMap;
import tools.Scripting;

@Scripting
class WarpCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        MapleCharacter victim = null; // =
        List<String> possibility = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        for (ChannelServer ch : WorldServer.getInstance().getAllChannels()) {
            for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
                if (chr.getName().toLowerCase().contains(args[0].toLowerCase()) && victim == null) {
                    victim = chr;
                    possibility.add(chr.getName());
                } else if (chr.getName().contains(args[0]) && victim != null) {
                    // key++;
                    possibility.add(chr.getName());
                }
            }
        }
        if (possibility.size() > 1) {
            sb.append("There were more than 1 player found, do !warp : ").append(possibility);
            c.getPlayer().dcolormsg(5, sb.toString());
        } else {
            if (victim != null) {
                MapleMap target = WorldServer.getInstance()
                        .getChannel(c.getChannel())
                        .getMapFactory()
                        .getMap(Integer.parseInt(args[1]));
                victim.changeMap(target, target.getPortal(0));
            }
        }
    }

    @Override
    public String getTrigger() {
        return "warp";
    }
}
