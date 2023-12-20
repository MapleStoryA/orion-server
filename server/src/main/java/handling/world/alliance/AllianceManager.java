package handling.world.alliance;

import handling.world.guild.GuildManager;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import tools.MaplePacketCreator;

@Slf4j
public class AllianceManager {

    private static final Map<Integer, MapleGuildAlliance> alliances = new LinkedHashMap<>();
    // TODO: Temporary public
    public static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    static {
        log.info("[MapleGuildAlliance] Loading GuildAlliances");
        Collection<MapleGuildAlliance> allGuilds = MapleGuildAlliance.loadAll();
        for (MapleGuildAlliance g : allGuilds) {
            alliances.put(g.getId(), g);
        }
    }

    public static MapleGuildAlliance getAlliance(final int allianceid) {
        MapleGuildAlliance ret = null;
        lock.readLock().lock();
        try {
            ret = alliances.get(allianceid);
        } finally {
            lock.readLock().unlock();
        }
        if (ret == null) {
            lock.writeLock().lock();
            try {
                ret = new MapleGuildAlliance(allianceid);
                if (ret == null || ret.getId() <= 0) { // failed to load
                    return null;
                }
                alliances.put(allianceid, ret);
            } finally {
                lock.writeLock().unlock();
            }
        }
        return ret;
    }

    public static int getAllianceLeader(final int allianceid) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            return mga.getLeaderId();
        }
        return 0;
    }

    public static void updateAllianceRanks(final int allianceid, final String[] ranks) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            mga.setRank(ranks);
        }
    }

    public static void updateAllianceNotice(final int allianceid, final String notice) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            mga.setNotice(notice);
        }
    }

    public static boolean canInvite(final int allianceid) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            return mga.getCapacity() > mga.getNoGuilds();
        }
        return false;
    }

    public static boolean changeAllianceLeader(final int allianceid, final int cid) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            return mga.setLeaderId(cid);
        }
        return false;
    }

    public static boolean changeAllianceRank(final int allianceid, final int cid, final int change) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            return mga.changeAllianceRank(cid, change);
        }
        return false;
    }

    public static boolean changeAllianceCapacity(final int allianceid) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            return mga.setCapacity();
        }
        return false;
    }

    public static boolean disbandAlliance(final int allianceid) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            return mga.disband();
        }
        return false;
    }

    public static boolean addGuildToAlliance(final int allianceid, final int gid) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            return mga.addGuild(gid);
        }
        return false;
    }

    public static boolean removeGuildFromAlliance(final int allianceid, final int gid, final boolean expelled) {
        final MapleGuildAlliance mga = getAlliance(allianceid);
        if (mga != null) {
            return mga.removeGuild(gid, expelled);
        }
        return false;
    }

    public static void sendGuild(final int allianceid) {
        final MapleGuildAlliance alliance = getAlliance(allianceid);
        if (alliance != null) {
            sendGuild(MaplePacketCreator.getAllianceUpdate(alliance), -1, allianceid);
            sendGuild(MaplePacketCreator.getGuildAlliance(alliance), -1, allianceid);
        }
    }

    public static void sendGuild(final byte[] packet, final int exceptionId, final int allianceid) {
        final MapleGuildAlliance alliance = getAlliance(allianceid);
        if (alliance != null) {
            for (int i = 0; i < alliance.getNoGuilds(); i++) {
                int gid = alliance.getGuildId(i);
                if (gid > 0 && gid != exceptionId) {
                    GuildManager.guildPacket(gid, packet);
                }
            }
        }
    }

    public static boolean createAlliance(
            final String alliancename, final int cid, final int cid2, final int gid, final int gid2) {
        final int allianceid = MapleGuildAlliance.createToDb(cid, alliancename, gid, gid2);
        if (allianceid <= 0) {
            return false;
        }
        final MapleGuild g = GuildManager.getGuild(gid), g_ = GuildManager.getGuild(gid2);
        g.setAllianceId(allianceid);
        g_.setAllianceId(allianceid);
        g.changeARank(true);
        g_.changeARank(false);

        final MapleGuildAlliance alliance = getAlliance(allianceid);

        sendGuild(MaplePacketCreator.createGuildAlliance(alliance), -1, allianceid);
        sendGuild(MaplePacketCreator.getAllianceInfo(alliance), -1, allianceid);
        sendGuild(MaplePacketCreator.getGuildAlliance(alliance), -1, allianceid);
        sendGuild(MaplePacketCreator.changeAlliance(alliance, true), -1, allianceid);
        return true;
    }

    public static void allianceChat(final int gid, final String name, final int cid, final String msg) {
        final MapleGuild g = GuildManager.getGuild(gid);
        if (g != null) {
            final MapleGuildAlliance ga = getAlliance(g.getAllianceId());
            if (ga != null) {
                for (int i = 0; i < ga.getNoGuilds(); i++) {
                    final MapleGuild g_ = GuildManager.getGuild(ga.getGuildId(i));
                    if (g_ != null) {
                        g_.allianceChat(name, cid, msg);
                    }
                }
            }
        }
    }

    public static void setNewAlliance(final int gid, final int allianceid) {
        final MapleGuildAlliance alliance = getAlliance(allianceid);
        final MapleGuild guild = GuildManager.getGuild(gid);
        if (alliance != null && guild != null) {
            for (int i = 0; i < alliance.getNoGuilds(); i++) {
                if (gid == alliance.getGuildId(i)) {
                    guild.setAllianceId(allianceid);
                    guild.broadcast(MaplePacketCreator.getAllianceInfo(alliance));
                    guild.broadcast(MaplePacketCreator.getGuildAlliance(alliance));
                    guild.broadcast(MaplePacketCreator.changeAlliance(alliance, true));
                    guild.changeARank();
                    guild.writeToDB(false);
                } else {
                    final MapleGuild g_ = GuildManager.getGuild(alliance.getGuildId(i));
                    if (g_ != null) {
                        g_.broadcast(MaplePacketCreator.addGuildToAlliance(alliance, guild));
                        g_.broadcast(MaplePacketCreator.changeGuildInAlliance(alliance, guild, true));
                    }
                }
            }
        }
    }

    public static void setOldAlliance(final int gid, final boolean expelled, final int allianceid) {
        final MapleGuildAlliance alliance = getAlliance(allianceid);
        final MapleGuild g_ = GuildManager.getGuild(gid);
        if (alliance != null) {
            for (int i = 0; i < alliance.getNoGuilds(); i++) {
                final MapleGuild guild = GuildManager.getGuild(alliance.getGuildId(i));
                if (guild == null) {
                    if (gid != alliance.getGuildId(i)) {
                        alliance.removeGuild(gid, false);
                    }
                    continue; // just skip
                }
                if (g_ == null || gid == alliance.getGuildId(i)) {
                    guild.changeARank(5);
                    guild.setAllianceId(0);
                    guild.broadcast(MaplePacketCreator.disbandAlliance(allianceid));
                } else if (g_ != null) {
                    guild.broadcast(
                            MaplePacketCreator.serverNotice(5, "[" + g_.getName() + "] Guild has left the alliance."));
                    guild.broadcast(MaplePacketCreator.changeGuildInAlliance(alliance, g_, false));
                    guild.broadcast(MaplePacketCreator.removeGuildFromAlliance(alliance, g_, expelled));
                }
            }
        }

        if (gid == -1) {
            lock.writeLock().lock();
            try {
                alliances.remove(allianceid);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public static List<byte[]> getAllianceInfo(final int allianceid, final boolean start) {
        List<byte[]> ret = new ArrayList<>();
        final MapleGuildAlliance alliance = getAlliance(allianceid);
        if (alliance != null) {
            if (start) {
                ret.add(MaplePacketCreator.getAllianceInfo(alliance));
                ret.add(MaplePacketCreator.getGuildAlliance(alliance));
            }
            ret.add(MaplePacketCreator.getAllianceUpdate(alliance));
        }
        return ret;
    }

    public static void save() {
        log.info("Saving alliances...");
        lock.writeLock().lock();
        try {
            for (MapleGuildAlliance a : alliances.values()) {
                a.saveToDb();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
