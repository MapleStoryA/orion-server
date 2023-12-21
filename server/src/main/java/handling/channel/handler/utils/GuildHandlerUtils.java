package handling.channel.handler.utils;

import client.MapleCharacter;
import lombok.extern.slf4j.Slf4j;
import tools.MaplePacketCreator;

@Slf4j
public class GuildHandlerUtils {

    public static final java.util.List<Invited> invited = new java.util.LinkedList<Invited>();
    public static long nextPruneTime = System.currentTimeMillis() + 20 * 60 * 1000;

    public static final boolean isGuildNameAcceptable(final String name) {
        if (name.length() < 3 || name.length() > 12) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLowerCase(name.charAt(i)) && !Character.isUpperCase(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static final void respawnPlayer(final MapleCharacter mc) {
        if (mc.getMap() == null) {
            return;
        }
        mc.getMap().broadcastMessage(MaplePacketCreator.loadGuildName(mc));
        mc.getMap().broadcastMessage(MaplePacketCreator.loadGuildIcon(mc));
    }

    public static final class Invited {

        public String name;
        public int gid;
        public long expiration;

        public Invited(final String n, final int id) {
            name = n.toLowerCase();
            gid = id;
            expiration = System.currentTimeMillis() + 60 * 60 * 1000; // 1 hr expiration
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Invited oth)) {
                return false;
            }
            return (gid == oth.gid && name.equals(oth.name));
        }
    }
}
