package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.GuildHandlerUtils;
import handling.channel.handler.utils.GuildHandlerUtils.Invited;
import handling.world.alliance.AllianceManager;
import handling.world.guild.GuildManager;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildResponse;
import tools.MaplePacketCreator;
import tools.data.input.CInPacket;

import java.util.Iterator;

@lombok.extern.slf4j.Slf4j
public class GuildOperationHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        if (System.currentTimeMillis() >= GuildHandlerUtils.nextPruneTime) {
            Iterator<Invited> itr = GuildHandlerUtils.invited.iterator();
            Invited inv;
            while (itr.hasNext()) {
                inv = itr.next();
                if (System.currentTimeMillis() >= inv.expiration) {
                    itr.remove();
                }
            }
            GuildHandlerUtils.nextPruneTime = System.currentTimeMillis() + 20 * 60 * 1000;
        }

        switch (packet.readByte()) {
            case 0x02: // Create guild
                if (c.getPlayer().getGuildId() > 0 || c.getPlayer().getMapId() != 200000301) {
                    c.getPlayer().dropMessage(1, "You cannot create a new Guild while in one.");
                    return;
                } else if (c.getPlayer().getMeso() < 5000000) {
                    c.getPlayer().dropMessage(1, "You do not have enough mesos to create a Guild.");
                    return;
                }
                final String guildName = packet.readMapleAsciiString();

                if (!GuildHandlerUtils.isGuildNameAcceptable(guildName)) {
                    c.getPlayer().dropMessage(1, "The Guild name you have chosen is not accepted.");
                    return;
                }
                int guildId = GuildManager.createGuild(c.getPlayer().getId(), guildName);
                if (guildId == 0) {
                    c.getSession().write(MaplePacketCreator.genericGuildMessage((byte) 0x1c));
                    return;
                }
                c.getPlayer().gainMeso(-5000000, true, false, true);
                c.getPlayer().setGuildId(guildId);
                c.getPlayer().setGuildRank((byte) 1);
                c.getPlayer().saveGuildStatus();
                c.getSession().write(MaplePacketCreator.showGuildInfo(c.getPlayer()));
                GuildManager.setGuildMemberOnline(c.getPlayer().getMGC(), true, c.getChannel());
                c.getPlayer().dropMessage(1, "You have successfully created a Guild.");
                GuildHandlerUtils.respawnPlayer(c.getPlayer());
                break;
            case 0x05: // invitation
                if (c.getPlayer().getGuildId() <= 0 || c.getPlayer().getGuildRank() > 2) { // 1 == guild master, 2 == jr
                    return;
                }
                String name = packet.readMapleAsciiString();
                final MapleGuildResponse mgr = MapleGuild.sendInvite(c, name);

                if (mgr != null) {
                    c.getSession().write(mgr.getPacket());
                } else {
                    Invited inv = new Invited(name, c.getPlayer().getGuildId());
                    if (!GuildHandlerUtils.invited.contains(inv)) {
                        GuildHandlerUtils.invited.add(inv);
                    }
                }
                break;
            case 0x06: // accepted guild invitation
                if (c.getPlayer().getGuildId() > 0) {
                    return;
                }
                guildId = packet.readInt();
                int cid = packet.readInt();

                if (cid != c.getPlayer().getId()) {
                    return;
                }
                name = c.getPlayer().getName().toLowerCase();
                Iterator<Invited> itr = GuildHandlerUtils.invited.iterator();

                while (itr.hasNext()) {
                    Invited inv = itr.next();
                    if (guildId == inv.gid && name.equals(inv.name)) {
                        c.getPlayer().setGuildId(guildId);
                        c.getPlayer().setGuildRank((byte) 5);
                        itr.remove();

                        int s = GuildManager.addGuildMember(c.getPlayer().getMGC());
                        if (s == 0) {
                            c.getPlayer().dropMessage(1, "The Guild you are trying to join is already full.");
                            c.getPlayer().setGuildId(0);
                            return;
                        }
                        c.getSession().write(MaplePacketCreator.showGuildInfo(c.getPlayer()));
                        final MapleGuild gs = GuildManager.getGuild(guildId);
                        for (byte[] pack : AllianceManager.getAllianceInfo(gs.getAllianceId(), true)) {
                            if (pack != null) {
                                c.getSession().write(pack);
                            }
                        }
                        c.getPlayer().saveGuildStatus();
                        GuildHandlerUtils.respawnPlayer(c.getPlayer());
                        break;
                    }
                }
                break;
            case 0x07: // leaving
                cid = packet.readInt();
                name = packet.readMapleAsciiString();

                if (cid != c.getPlayer().getId() || !name.equals(c.getPlayer().getName()) || c.getPlayer().getGuildId() <= 0) {
                    return;
                }
                GuildManager.leaveGuild(c.getPlayer().getMGC());
                c.getSession().write(MaplePacketCreator.showGuildInfo(null));
                break;
            case 0x08: // Expel
                cid = packet.readInt();
                name = packet.readMapleAsciiString();

                if (c.getPlayer().getGuildRank() > 2 || c.getPlayer().getGuildId() <= 0) {
                    return;
                }
                GuildManager.expelMember(c.getPlayer().getMGC(), name, cid);
                break;
            case 0x0d: // Guild rank titles change
                if (c.getPlayer().getGuildId() <= 0 || c.getPlayer().getGuildRank() != 1) {
                    return;
                }
                String[] ranks = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = packet.readMapleAsciiString();
                }

                GuildManager.changeRankTitle(c.getPlayer().getGuildId(), ranks);
                break;
            case 0x0e: // Rank change
                cid = packet.readInt();
                byte newRank = packet.readByte();

                if ((newRank <= 1 || newRank > 5) || c.getPlayer().getGuildRank() > 2 || (newRank <= 2 && c.getPlayer().getGuildRank() != 1) || c.getPlayer().getGuildId() <= 0) {
                    return;
                }

                GuildManager.changeRank(c.getPlayer().getGuildId(), cid, newRank);
                break;
            case 0x0f: // guild emblem change
                if (c.getPlayer().getGuildId() <= 0 || c.getPlayer().getGuildRank() != 1 || c.getPlayer().getMapId() != 200000301) {
                    return;
                }

                if (c.getPlayer().getMeso() < 15000000) {
                    c.getPlayer().dropMessage(1, "You do not have enough mesos to create a Guild.");
                    return;
                }
                final short bg = packet.readShort();
                final byte bgcolor = packet.readByte();
                final short logo = packet.readShort();
                final byte logocolor = packet.readByte();

                GuildManager.setGuildEmblem(c.getPlayer().getGuildId(), bg, bgcolor, logo, logocolor);

                c.getPlayer().gainMeso(-15000000, true, false, true);
                GuildHandlerUtils.respawnPlayer(c.getPlayer());
                break;
            case 0x10: // guild notice change
                final String notice = packet.readMapleAsciiString();
                if (notice.length() > 100 || c.getPlayer().getGuildId() <= 0 || c.getPlayer().getGuildRank() > 2) {
                    return;
                }
                GuildManager.setGuildNotice(c.getPlayer().getGuildId(), notice);
                break;
        }
    }

}
