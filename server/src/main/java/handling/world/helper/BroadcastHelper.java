package handling.world.helper;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import java.util.List;

public class BroadcastHelper {

    public static void broadcastSmega(byte[] message) {
        for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
            cs.broadcastSmega(message);
        }
    }

    public static void broadcastGMMessage(byte[] message) {
        for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
            cs.broadcastGMMessage(message);
        }
    }

    public static void broadcastMessage(byte[] message) {
        for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
            cs.broadcastMessage(message);
        }
    }

    public static void sendPacket(List<Integer> targetIds, byte[] packet, int exception) {
        MapleCharacter c;
        for (int i : targetIds) {
            if (i == exception) {
                continue;
            }
            int ch = FindCommand.findChannel(i);
            if (ch < 0) {
                continue;
            }
            c = WorldServer.getInstance().getChannel(ch).getPlayerStorage().getCharacterById(i);
            if (c != null) {
                c.getClient().getSession().write(packet);
            }
        }
    }

    public static void sendGuildPacket(int targetIds, byte[] packet, int exception, int guildid) {
        if (targetIds == exception) {
            return;
        }
        int ch = FindCommand.findChannel(targetIds);
        if (ch < 0) {
            return;
        }
        final MapleCharacter c =
                WorldServer.getInstance().getChannel(ch).getPlayerStorage().getCharacterById(targetIds);
        if (c != null && c.getGuildId() == guildid) {
            c.getClient().getSession().write(packet);
        }
    }
}
