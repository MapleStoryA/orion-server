package handling.world.guild;

import client.MapleCharacter;
import client.MapleClient;
import client.base.MapleCharacterHelper;
import database.DatabaseConnection;
import handling.world.Broadcast;
import handling.world.alliance.AllianceManager;
import handling.world.guild.MapleBBSThread.MapleBBSReply;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;
import tools.MaplePacketCreator;
import tools.packet.UIPacket;

@Slf4j
public class MapleGuild implements java.io.Serializable {

    public static final long serialVersionUID = 6322150443228168192L;
    private final List<MapleGuildCharacter> members = new CopyOnWriteArrayList<>();
    private final String[] rankTitles = new String[5]; // 1 = master, 2 = jr, 5 = lowest member
    private final Map<Integer, MapleBBSThread> bbs = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rL = lock.readLock(), wL = lock.writeLock();
    private String name, notice;
    private int id, gp, logo, logoColor, leader, capacity, logoBG, logoBGColor, signature;
    private boolean bDirty = true, proper = true;
    private int allianceid = 0, invitedid = 0;
    private boolean init = false;

    public MapleGuild(final int guildid) {
        super();

        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildid);
            ResultSet rs = ps.executeQuery();

            if (!rs.first()) {
                rs.close();
                ps.close();
                id = -1;
                return;
            }
            id = guildid;
            name = rs.getString("name");
            gp = rs.getInt("GP");
            logo = rs.getInt("logo");
            logoColor = rs.getInt("logoColor");
            logoBG = rs.getInt("logoBG");
            logoBGColor = rs.getInt("logoBGColor");
            capacity = rs.getInt("capacity");
            rankTitles[0] = rs.getString("rank1title");
            rankTitles[1] = rs.getString("rank2title");
            rankTitles[2] = rs.getString("rank3title");
            rankTitles[3] = rs.getString("rank4title");
            rankTitles[4] = rs.getString("rank5title");
            leader = rs.getInt("leader");
            notice = rs.getString("notice");
            signature = rs.getInt("signature");
            allianceid = rs.getInt("alliance");
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT id, name, level, job, guildrank, alliancerank FROM characters"
                    + " WHERE guildid = ? ORDER BY guildrank ASC, name ASC");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();

            if (!rs.first()) {
                System.err.println("No members in guild " + id + ".  Impossible... guild is disbanding");
                rs.close();
                ps.close();
                writeToDB(true);
                proper = false;
                return;
            }
            boolean leaderCheck = false;
            do {
                if (rs.getInt("id") == leader) {
                    leaderCheck = true;
                }
                members.add(new MapleGuildCharacter(
                        rs.getInt("id"),
                        rs.getShort("level"),
                        rs.getString("name"),
                        (byte) -1,
                        rs.getInt("job"),
                        rs.getByte("guildrank"),
                        rs.getByte("alliancerank"),
                        guildid,
                        false));
            } while (rs.next());
            rs.close();
            ps.close();

            if (!leaderCheck) {
                System.err.println(
                        "Leader " + leader + " isn't in guild " + id + ".  Impossible... guild is disbanding.");
                writeToDB(true);
                proper = false;
                return;
            }

            ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? ORDER BY localthreadid" + " DESC");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            while (rs.next()) {
                final MapleBBSThread thread = new MapleBBSThread(
                        rs.getInt("localthreadid"),
                        rs.getString("name"),
                        rs.getString("startpost"),
                        rs.getLong("timestamp"),
                        guildid,
                        rs.getInt("postercid"),
                        rs.getInt("icon"));
                final PreparedStatement pse = con.prepareStatement("SELECT * FROM bbs_replies WHERE threadid = ?");
                pse.setInt(1, rs.getInt("threadid"));
                final ResultSet rse = pse.executeQuery();
                while (rse.next()) {
                    thread.replies.put(
                            thread.replies.size(),
                            new MapleBBSReply(
                                    thread.replies.size(),
                                    rse.getInt("postercid"),
                                    rse.getString("content"),
                                    rse.getLong("timestamp")));
                }
                rse.close();
                pse.close();
                bbs.put(rs.getInt("localthreadid"), thread);
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            System.err.println("unable to read guild information from sql");
            se.printStackTrace();
        }
    }

    public static final void loadAll() {
        MapleGuild g;
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                g = new MapleGuild(rs.getInt("guildid"));
                if (g.getId() > 0) {
                    GuildManager.addLoadedGuild(g);
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            System.err.println("unable to read guild information from sql");
            se.printStackTrace();
        }
    }

    // function to create guild, returns the guild id if successful, 0 if not
    public static final int createGuild(final int leaderId, final String name) {
        if (name.length() > 12) {
            return 0;
        }
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.first()) { // name taken
                rs.close();
                ps.close();
                return 0;
            }
            ps.close();
            rs.close();

            ps = con.prepareStatement(
                    "INSERT INTO guilds (`leader`, `name`, `signature`, `alliance`) VALUES" + " (?, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, leaderId);
            ps.setString(2, name);
            ps.setInt(3, (int) (System.currentTimeMillis() / 1000));
            ps.execute();
            rs = ps.getGeneratedKeys();
            int ret = 0;
            if (rs.next()) {
                ret = rs.getInt(1);
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException se) {
            System.err.println("SQL THROW");
            se.printStackTrace();
            return 0;
        }
    }

    // null indicates successful invitation being sent
    // keep in mind that this will be called by a handler most of the time
    // so this will be running mostly on a channel server, unlike the rest
    // of the class
    public static final MapleGuildResponse sendInvite(final MapleClient c, final String targetName) {
        final MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(targetName);
        if (mc == null) {
            return MapleGuildResponse.NOT_IN_CHANNEL;
        }
        if (mc.getGuildId() > 0) {
            return MapleGuildResponse.ALREADY_IN_GUILD;
        }
        mc.getClient()
                .getSession()
                .write(MaplePacketCreator.guildInvite(
                        c.getPlayer().getGuildId(),
                        c.getPlayer().getName(),
                        c.getPlayer().getLevel(),
                        c.getPlayer().getJob().getId()));
        return null;
    }

    public static void setOfflineGuildStatus(int guildid, byte guildrank, byte alliancerank, int cid) {
        try (var con = DatabaseConnection.getConnection()) {
            java.sql.PreparedStatement ps = con.prepareStatement(
                    "UPDATE characters SET guildid = ?, guildrank = ?, alliancerank = ?" + " WHERE id = ?");
            ps.setInt(1, guildid);
            ps.setInt(2, guildrank);
            ps.setInt(3, alliancerank);
            ps.setInt(4, cid);
            ps.execute();
            ps.close();
        } catch (SQLException se) {
            log.info("SQLException: " + se.getLocalizedMessage());
            se.printStackTrace();
        }
    }

    public boolean isProper() {
        return proper;
    }

    public final void writeToDB(final boolean bDisband) {
        try (var con = DatabaseConnection.getConnection()) {
            if (!bDisband) {
                StringBuilder buf = new StringBuilder(
                        "UPDATE guilds SET GP = ?, logo = ?, logoColor = ?, logoBG = ?," + " logoBGColor = ?, ");
                for (int i = 1; i < 6; i++) {
                    buf.append("rank" + i + "title = ?, ");
                }
                buf.append("capacity = ?, notice = ?, name = ?, alliance = ? WHERE guildid = ?");

                PreparedStatement ps = con.prepareStatement(buf.toString());
                ps.setInt(1, gp);
                ps.setInt(2, logo);
                ps.setInt(3, logoColor);
                ps.setInt(4, logoBG);
                ps.setInt(5, logoBGColor);
                ps.setString(6, rankTitles[0]);
                ps.setString(7, rankTitles[1]);
                ps.setString(8, rankTitles[2]);
                ps.setString(9, rankTitles[3]);
                ps.setString(10, rankTitles[4]);
                ps.setInt(11, capacity);
                ps.setString(12, notice);
                ps.setString(13, name);
                ps.setInt(14, allianceid);
                ps.setInt(15, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement(
                        "INSERT INTO bbs_threads(`postercid`, `name`, `timestamp`, `icon`,"
                                + " `startpost`, `guildid`, `localthreadid`) VALUES(?, ?, ?, ?,"
                                + " ?, ?, ?)",
                        DatabaseConnection.RETURN_GENERATED_KEYS);
                ps.setInt(6, id);
                for (MapleBBSThread bb : bbs.values()) {
                    ps.setInt(1, bb.ownerID);
                    ps.setString(2, bb.name);
                    ps.setLong(3, bb.timestamp);
                    ps.setInt(4, bb.icon);
                    ps.setString(5, bb.text);
                    ps.setInt(7, bb.localthreadID);
                    ps.executeUpdate();
                    final ResultSet rs = ps.getGeneratedKeys();
                    if (!rs.next()) {
                        rs.close();
                        continue;
                    }
                    final PreparedStatement pse =
                            con.prepareStatement("INSERT INTO bbs_replies (`threadid`, `postercid`, `timestamp`,"
                                    + " `content`, `guildid`) VALUES (?, ?, ?, ?, ?)");
                    pse.setInt(5, id);
                    for (MapleBBSReply r : bb.replies.values()) {
                        pse.setInt(1, rs.getInt(1));
                        pse.setInt(2, r.ownerID);
                        pse.setLong(3, r.timestamp);
                        pse.setString(4, r.content);
                        pse.execute();
                    }
                    pse.close();
                    rs.close();
                }
                ps.close();
            } else {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE characters SET guildid = 0, guildrank = 5, alliancerank = 5" + " WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                if (allianceid > 0) {
                    final MapleGuildAlliance alliance = AllianceManager.getAlliance(allianceid);
                    if (alliance != null) {
                        alliance.removeGuild(id, false);
                    }
                }

                broadcast(MaplePacketCreator.guildDisband(id));
            }
        } catch (SQLException se) {
            System.err.println("Error saving guild to SQL");
            se.printStackTrace();
        }
    }

    public final int getId() {
        return id;
    }

    public final int getLeaderId() {
        return leader;
    }

    public final MapleCharacter getLeader(final MapleClient c) {
        return c.getChannelServer().getPlayerStorage().getCharacterById(leader);
    }

    public final int getGP() {
        return gp;
    }

    public final int getLogo() {
        return logo;
    }

    public final void setLogo(final int l) {
        logo = l;
    }

    public final int getLogoColor() {
        return logoColor;
    }

    public final void setLogoColor(final int c) {
        logoColor = c;
    }

    public final int getLogoBG() {
        return logoBG;
    }

    public final void setLogoBG(final int bg) {
        logoBG = bg;
    }

    public final int getLogoBGColor() {
        return logoBGColor;
    }

    public final void setLogoBGColor(final int c) {
        logoBGColor = c;
    }

    public final String getNotice() {
        if (notice == null) {
            return "";
        }
        return notice;
    }

    public final String getName() {
        return name;
    }

    public final int getCapacity() {
        return capacity;
    }

    public final int getSignature() {
        return signature;
    }

    public final void broadcast(final byte[] packet) {
        broadcast(packet, -1, BCOp.NONE);
    }

    public final void broadcast(final byte[] packet, final int exception) {
        broadcast(packet, exception, BCOp.NONE);
    }

    // multi-purpose function that reaches every member of guild (except the character with
    // exceptionId) in all channels with as little access to rmi as possible
    public final void broadcast(final byte[] packet, final int exceptionId, final BCOp bcop) {
        wL.lock();
        try {
            buildNotifications();
        } finally {
            wL.unlock();
        }

        rL.lock();
        try {
            for (MapleGuildCharacter mgc : members) {
                if (bcop == BCOp.DISBAND) {
                    if (mgc.isOnline()) {
                        GuildManager.setGuildAndRank(mgc.getId(), 0, 5, 5);
                    } else {
                        setOfflineGuildStatus(0, (byte) 5, (byte) 5, mgc.getId());
                    }
                } else if (mgc.isOnline() && mgc.getId() != exceptionId) {
                    if (bcop == BCOp.EMBELMCHANGE) {
                        GuildManager.changeEmblem(id, mgc.getId(), new MapleGuildSummary(this));
                    } else if (bcop == BCOp.NAMECHANGE) {
                        GuildManager.changeName(id, mgc.getId(), new MapleGuildSummary(this));
                    } else {
                        Broadcast.sendGuildPacket(mgc.getId(), packet, exceptionId, id);
                    }
                }
            }
        } finally {
            rL.unlock();
        }
    }

    private final void buildNotifications() {
        if (!bDirty) {
            return;
        }
        final List<Integer> mem = new LinkedList<>();
        final Iterator<MapleGuildCharacter> toRemove = members.iterator();
        while (toRemove.hasNext()) {
            MapleGuildCharacter mgc = toRemove.next();
            if (!mgc.isOnline()) {
                continue;
            }
            if (mem.contains(mgc.getId()) || mgc.getGuildId() != id) {
                members.remove(mgc);
                continue;
            }
            mem.add(mgc.getId());
        }
        bDirty = false;
    }

    public final void setOnline(final int cid, final boolean online, final int channel) {
        boolean bBroadcast = true;
        for (MapleGuildCharacter mgc : members) {
            if (mgc.getGuildId() == id && mgc.getId() == cid) {
                if (mgc.isOnline() == online) {
                    bBroadcast = false;
                }
                mgc.setOnline(online);
                mgc.setChannel((byte) channel);
                break;
            }
        }
        if (bBroadcast) {
            broadcast(MaplePacketCreator.guildMemberOnline(id, cid, online), cid);
            if (allianceid > 0) {
                AllianceManager.sendGuild(
                        MaplePacketCreator.allianceMemberOnline(allianceid, id, cid, online), id, allianceid);
            }
        }
        bDirty = true; // member formation has changed, update notifications
        init = true;
    }

    public final void guildChat(final String name, final int cid, final String msg) {
        broadcast(MaplePacketCreator.multiChat(name, msg, 2), cid);
    }

    public final void allianceChat(final String name, final int cid, final String msg) {
        broadcast(MaplePacketCreator.multiChat(name, msg, 3), cid);
    }

    public final String getRankTitle(final int rank) {
        return rankTitles[rank - 1];
    }

    public int getAllianceId() {
        // return alliance.getId();
        return this.allianceid;
    }

    public void setAllianceId(int a) {
        this.allianceid = a;
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE guilds SET alliance = ? WHERE guildid = ?");
            ps.setInt(1, a);
            ps.setInt(2, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Saving allianceid ERROR" + e);
        }
    }

    public int getInvitedId() {
        return this.invitedid;
    }

    public void setInvitedId(int iid) {
        this.invitedid = iid;
    }

    public final int addGuildMember(final MapleGuildCharacter mgc) {
        // first of all, insert it into the members keeping alphabetical order of lowest ranks ;)
        wL.lock();
        try {
            if (members.size() >= capacity) {
                return 0;
            }
            for (int i = members.size() - 1; i >= 0; i--) {
                if (members.get(i).getGuildRank() < 5
                        || members.get(i).getName().compareTo(mgc.getName()) < 0) {
                    members.add(i + 1, mgc);
                    bDirty = true;
                    break;
                }
            }
        } finally {
            wL.unlock();
        }
        broadcast(MaplePacketCreator.newGuildMember(mgc));
        if (allianceid > 0) {
            AllianceManager.sendGuild(allianceid);
        }
        return 1;
    }

    public final void leaveGuild(final MapleGuildCharacter mgc) {
        broadcast(MaplePacketCreator.memberLeft(mgc, false));
        wL.lock();
        try {
            bDirty = true;
            members.remove(mgc);
            if (mgc.isOnline()) {
                GuildManager.setGuildAndRank(mgc.getId(), 0, 5, 5);
            } else {
                setOfflineGuildStatus((short) 0, (byte) 5, (byte) 5, mgc.getId());
            }
            if (allianceid > 0) {
                AllianceManager.sendGuild(allianceid);
            }
        } finally {
            wL.unlock();
        }
    }

    public final void expelMember(final MapleGuildCharacter initiator, final String name, final int cid) {
        wL.lock();
        try {
            final Iterator<MapleGuildCharacter> itr = members.iterator();
            while (itr.hasNext()) {
                final MapleGuildCharacter mgc = itr.next();

                if (mgc.getId() == cid && initiator.getGuildRank() < mgc.getGuildRank()) {
                    broadcast(MaplePacketCreator.memberLeft(mgc, true));

                    bDirty = true;

                    if (allianceid > 0) {
                        AllianceManager.sendGuild(allianceid);
                    }
                    if (mgc.isOnline()) {
                        GuildManager.setGuildAndRank(cid, 0, 5, 5);
                    } else {
                        MapleCharacterHelper.sendNote(
                                mgc.getName(), initiator.getName(), "You have been expelled from the guild.", 0);
                        setOfflineGuildStatus((short) 0, (byte) 5, (byte) 5, cid);
                    }
                    members.remove(mgc);
                    break;
                }
            }
        } finally {
            wL.unlock();
        }
    }

    public final void changeARank() {
        changeARank(false);
    }

    public final void changeARank(final boolean leader) {
        for (final MapleGuildCharacter mgc : members) {
            if (this.leader == mgc.getId()) {
                changeARank(mgc.getId(), leader ? 1 : 2);
            } else {
                changeARank(mgc.getId(), 3);
            }
        }
    }

    public final void changeARank(final int newRank) {
        for (final MapleGuildCharacter mgc : members) {
            changeARank(mgc.getId(), newRank);
        }
    }

    public final void changeARank(final int cid, final int newRank) {
        if (allianceid <= 0) {
            return;
        }
        for (final MapleGuildCharacter mgc : members) {
            if (cid == mgc.getId()) {
                if (mgc.isOnline()) {
                    GuildManager.setGuildAndRank(cid, this.id, mgc.getGuildRank(), newRank);
                } else {
                    setOfflineGuildStatus((short) this.id, mgc.getGuildRank(), (byte) newRank, cid);
                }
                mgc.setAllianceRank((byte) newRank);
                // WorldRegistryImpl.getInstance().sendGuild(MaplePacketCreator.changeAllianceRank(allianceid, mgc), -1,
                // allianceid);
                // WorldRegistryImpl.getInstance().sendGuild(MaplePacketCreator.updateAllianceRank(allianceid, mgc), -1,
                // allianceid);
                AllianceManager.sendGuild(allianceid);
                return;
            }
        }
        // it should never get to this point unless cid was incorrect o_O
        System.err.println("INFO: unable to find the correct id for changeRank({" + cid + "}, {" + newRank + "})");
    }

    public final void changeRank(final int cid, final int newRank) {
        for (final MapleGuildCharacter mgc : members) {
            if (cid == mgc.getId()) {
                if (mgc.isOnline()) {
                    GuildManager.setGuildAndRank(cid, this.id, newRank, mgc.getAllianceRank());
                } else {
                    setOfflineGuildStatus((short) this.id, (byte) newRank, mgc.getAllianceRank(), cid);
                }
                mgc.setGuildRank((byte) newRank);
                broadcast(MaplePacketCreator.changeRank(mgc));
                return;
            }
        }
        // it should never get to this point unless cid was incorrect o_O
        System.err.println("INFO: unable to find the correct id for changeRank({" + cid + "}, {" + newRank + "})");
    }

    /*
    public final void memberLevelJobUpdate(final MapleGuildCharacter mgc) {
    	for (final MapleGuildCharacter member : members) {
    		if (member.getId() == mgc.getId()) {
    			int old_level = member.getLevel();
    			int old_job = member.getJobId();
    			member.setJobId(mgc.getJobId());
    			member.setLevel((short) mgc.getLevel());
    			if (mgc.getLevel() > old_level) {
    				gainGP((mgc.getLevel() - old_level) * mgc.getLevel() / 10, false); //level 199->200 = 20 gp
    			}
    			if (old_level != mgc.getLevel()) {
    				this.broadcast(MaplePacketCreator.sendLevelup(false, mgc.getLevel(), mgc.getName()), mgc.getId());
    			}
    			if (old_job != mgc.getJobId()) {
    				this.broadcast(MaplePacketCreator.sendJobup(false, mgc.getJobId(), mgc.getName()), mgc.getId());
    			}
    			broadcast(MaplePacketCreator.guildMemberLevelJobUpdate(mgc));
    			if (allianceid > 0) {
    				World.Alliance.sendGuild(MaplePacketCreator.updateAlliance(mgc, allianceid), id, allianceid);
    			}
    			break;
    		}
    	}
    }*/
    // fucking annoying notice when someone in the guild level up

    public final void setGuildNotice(final String notice) {
        this.notice = notice;
        broadcast(MaplePacketCreator.guildNotice(id, notice));
    }

    public final void changeRankTitle(final String[] ranks) {
        System.arraycopy(ranks, 0, rankTitles, 0, 5);
        broadcast(MaplePacketCreator.rankTitleChange(id, ranks));
    }

    public final void disbandGuild() {
        writeToDB(true);
        broadcast(null, -1, BCOp.DISBAND);
    }

    public final void setGuildEmblem(final short bg, final byte bgcolor, final short logo, final byte logocolor) {
        this.logoBG = bg;
        this.logoBGColor = bgcolor;
        this.logo = logo;
        this.logoColor = logocolor;
        broadcast(null, -1, BCOp.EMBELMCHANGE);

        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE guilds SET logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?" + " WHERE guildid = ?");
            ps.setInt(1, logo);
            ps.setInt(2, logoColor);
            ps.setInt(3, logoBG);
            ps.setInt(4, logoBGColor);
            ps.setInt(5, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Saving guild logo / BG colo ERROR");
            e.printStackTrace();
        }
    }

    public final void setGuildName(final String name) {
        this.name = name;
        broadcast(null, -1, BCOp.NAMECHANGE);
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE guilds SET `name` = ? WHERE guildid = ?");
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Saving guild name ERROR. ");
            e.printStackTrace();
        }
    }

    public final MapleGuildCharacter getMGC(final int cid) {
        for (final MapleGuildCharacter mgc : members) {
            if (mgc.getId() == cid) {
                return mgc;
            }
        }
        return null;
    }

    public final boolean increaseCapacity() {
        if (capacity >= 100 || ((capacity + 5) > 100)) {
            return false;
        }
        capacity += 5;
        broadcast(MaplePacketCreator.guildCapacityChange(this.id, this.capacity));

        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE guilds SET capacity = ? WHERE guildid = ?");
            ps.setInt(1, this.capacity);
            ps.setInt(2, this.id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Saving guild capacity ERROR");
            e.printStackTrace();
        }
        return true;
    }

    public final void gainGP(final int amount) {
        gainGP(amount, true);
    }

    public final void gainGP(int amount, final boolean broadcast) {
        if (amount == 0) { // no change, no broadcast and no sql.
            return;
        }
        if (amount + gp < 0) {
            amount = -gp;
        } // 0 lowest
        gp += amount;
        broadcast(MaplePacketCreator.updateGP(id, gp));
        if (broadcast) {
            broadcast(UIPacket.getGPMsg(amount));
        }
    }

    public final void addMemberData(final OutPacket packet) {
        packet.write(members.size());

        for (final MapleGuildCharacter mgc : members) {
            packet.writeInt(mgc.getId());
        }
        for (final MapleGuildCharacter mgc : members) {
            packet.writeAsciiString(mgc.getName(), 13);
            packet.writeInt(mgc.getJobId());
            packet.writeInt(mgc.getLevel());
            packet.writeInt(mgc.getGuildRank());
            packet.writeInt(mgc.isOnline() ? 1 : 0);
            packet.writeInt(signature);
            packet.writeInt(mgc.getAllianceRank());
        }
    }

    public java.util.Collection<MapleGuildCharacter> getMembers() {
        return java.util.Collections.unmodifiableCollection(members);
    }

    public final boolean isInit() {
        return init;
    }

    public final List<MapleBBSThread> getBBS() {
        final List<MapleBBSThread> ret = new ArrayList<>(bbs.values());
        ret.sort(new MapleBBSThread.ThreadComparator());
        return ret;
    }

    public final int addBBSThread(
            final String title, final String text, final int icon, final boolean bNotice, final int posterID) {
        final int add = bbs.get(0) == null ? 1 : 0; // add 1 if no notice
        final int ret = bNotice ? 0 : Math.max(1, bbs.size() + add);
        bbs.put(ret, new MapleBBSThread(ret, title, text, System.currentTimeMillis(), this.id, posterID, icon));
        return ret;
    }

    public final void editBBSThread(
            final int localthreadid,
            final String title,
            final String text,
            final int icon,
            final int posterID,
            final int guildRank) {
        final MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            bbs.put(
                    localthreadid,
                    new MapleBBSThread(
                            localthreadid, title, text, System.currentTimeMillis(), this.id, thread.ownerID, icon));
        }
    }

    public final void deleteBBSThread(final int localthreadid, final int posterID, final int guildRank) {
        final MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            bbs.remove(localthreadid);
        }
    }

    public final void addBBSReply(final int localthreadid, final String text, final int posterID) {
        final MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null) {
            thread.replies.put(
                    thread.replies.size(),
                    new MapleBBSReply(thread.replies.size(), posterID, text, System.currentTimeMillis()));
        }
    }

    public final void deleteBBSReply(
            final int localthreadid, final int replyid, final int posterID, final int guildRank) {
        final MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null) {
            final MapleBBSReply reply = thread.replies.get(replyid);
            if (reply != null && (reply.ownerID == posterID || guildRank <= 2)) {
                thread.replies.remove(replyid);
            }
        }
    }

    public final void memberLevelJobUpdate(final MapleGuildCharacter mgc) {
        for (final MapleGuildCharacter member : members) {
            if (member.getId() == mgc.getId()) {
                int old_level = member.getLevel();
                int old_job = member.getJobId();
                member.setJobId(mgc.getJobId());
                member.setLevel((short) mgc.getLevel());
                if (mgc.getLevel() > old_level) {
                    gainGP((mgc.getLevel() - old_level) * mgc.getLevel() / 10, false); // level 199->200 = 20 gp
                }
                if (old_level != mgc.getLevel()) {
                    this.broadcast(MaplePacketCreator.sendLevelup(false, mgc.getLevel(), mgc.getName()), mgc.getId());
                }
                if (old_job != mgc.getJobId()) {
                    this.broadcast(MaplePacketCreator.sendJobup(false, mgc.getJobId(), mgc.getName()), mgc.getId());
                }
                broadcast(MaplePacketCreator.guildMemberLevelJobUpdate(mgc));
                if (allianceid > 0) {
                    AllianceManager.sendGuild(MaplePacketCreator.updateAlliance(mgc, allianceid), id, allianceid);
                }
                break;
            }
        }
    }

    private enum BCOp {
        NONE,
        DISBAND,
        EMBELMCHANGE,
        NAMECHANGE
    }
}
