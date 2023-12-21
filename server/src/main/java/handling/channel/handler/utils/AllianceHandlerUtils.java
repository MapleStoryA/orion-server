/*
This file is part of the ZeroFusion MapleStory Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>
ZeroFusion organized by "RMZero213" <RMZero213@hotmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package handling.channel.handler.utils;

import client.MapleCharacter;
import client.MapleClient;
import handling.world.alliance.AllianceManager;
import handling.world.guild.GuildManager;
import handling.world.guild.MapleGuild;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import tools.MaplePacketCreator;

@Slf4j
public class AllianceHandlerUtils {

    public static final void HandleAlliance(final InPacket packet, final MapleClient c, boolean denied) {
        if (c.getPlayer().getGuildId() <= 0) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        final MapleGuild gs = GuildManager.getGuild(c.getPlayer().getGuildId());
        if (gs == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        // log.info("Unhandled GuildAlliance \n" + packet.toString());
        byte op = packet.readByte();
        if (c.getPlayer().getGuildRank() != 1 && op != 1) { // only updating doesn't need guild leader
            return;
        }
        if (op == 22) {
            denied = true;
        }
        int leaderid = 0;
        if (gs.getAllianceId() > 0) {
            leaderid = AllianceManager.getAllianceLeader(gs.getAllianceId());
        }
        // accept invite, and deny invite don't need allianceid.
        if (op != 4 && !denied) {
            if (gs.getAllianceId() <= 0 || leaderid <= 0) {
                return;
            }
        } else if (leaderid > 0 || gs.getAllianceId() > 0) { // infact, if they have allianceid it's suspicious
            return;
        }
        if (denied) {
            DenyInvite(c, gs);
            return;
        }
        MapleCharacter chr;
        int inviteid;
        switch (op) {
            case 1: // load... must be in world op
                for (byte[] pack : AllianceManager.getAllianceInfo(gs.getAllianceId(), false)) {
                    if (pack != null) {
                        c.getSession().write(pack);
                    }
                }
                break;
            case 3: // invite
                final int newGuild = GuildManager.getGuildLeader(packet.readMapleAsciiString());
                if (newGuild > 0
                        && c.getPlayer().getAllianceRank() == 1
                        && leaderid == c.getPlayer().getId()) {
                    chr = c.getChannelServer().getPlayerStorage().getCharacterById(newGuild);
                    if (chr != null && chr.getGuildId() > 0 && AllianceManager.canInvite(gs.getAllianceId())) {
                        chr.getClient()
                                .getSession()
                                .write(MaplePacketCreator.sendAllianceInvite(
                                        AllianceManager.getAlliance(gs.getAllianceId())
                                                .getName(),
                                        c.getPlayer()));
                        GuildManager.setInvitedId(chr.getGuildId(), gs.getAllianceId());
                    }
                }
                break;
            case 4: // accept invite... guildid that invited(int, a/b check) -> guildname that was
                // invited? but we dont care about that
                inviteid = GuildManager.getInvitedId(c.getPlayer().getGuildId());
                if (inviteid > 0) {
                    if (!AllianceManager.addGuildToAlliance(
                            inviteid, c.getPlayer().getGuildId())) {
                        c.getPlayer().dropMessage(5, "An error occured when adding guild.");
                    }
                    GuildManager.setInvitedId(c.getPlayer().getGuildId(), 0);
                }
                break;
            case 2: // leave; nothing
            case 6: // expel, guildid(int) -> allianceid(don't care, a/b check)
                final int gid;
                if (op == 6 && packet.available() >= 4) {
                    gid = packet.readInt();
                    if (packet.available() >= 4 && gs.getAllianceId() != packet.readInt()) {
                        break;
                    }
                } else {
                    gid = c.getPlayer().getGuildId();
                }
                if (c.getPlayer().getAllianceRank() <= 2
                        && (c.getPlayer().getAllianceRank() == 1
                                || c.getPlayer().getGuildId() == gid)) {
                    if (!AllianceManager.removeGuildFromAlliance(
                            gs.getAllianceId(), gid, c.getPlayer().getGuildId() != gid)) {
                        c.getPlayer().dropMessage(5, "An error occured when removing guild.");
                    }
                }
                break;
            case 7: // change leader
                if (c.getPlayer().getAllianceRank() == 1
                        && leaderid == c.getPlayer().getId()) {
                    if (!AllianceManager.changeAllianceLeader(gs.getAllianceId(), packet.readInt())) {
                        c.getPlayer().dropMessage(5, "An error occured when changing leader.");
                    }
                }
                break;
            case 8: // title update
                if (c.getPlayer().getAllianceRank() == 1
                        && leaderid == c.getPlayer().getId()) {
                    String[] ranks = new String[5];
                    for (int i = 0; i < 5; i++) {
                        ranks[i] = packet.readMapleAsciiString();
                    }
                    AllianceManager.updateAllianceRanks(gs.getAllianceId(), ranks);
                }
                break;
            case 9:
                if (c.getPlayer().getAllianceRank() <= 2) {
                    if (!AllianceManager.changeAllianceRank(gs.getAllianceId(), packet.readInt(), packet.readByte())) {
                        c.getPlayer().dropMessage(5, "An error occured when changing rank.");
                    }
                }
                break;
            case 10: // notice update
                if (c.getPlayer().getAllianceRank() <= 2) {
                    final String notice = packet.readMapleAsciiString();
                    if (notice.length() > 100) {
                        break;
                    }
                    AllianceManager.updateAllianceNotice(gs.getAllianceId(), notice);
                }
                break;
            default:
                log.info("Unhandled GuildAlliance op: " + op + ", \n" + packet);
                break;
        }
        // c.getSession().write(MaplePacketCreator.enableActions());
    }

    public static final void DenyInvite(
            MapleClient c,
            final MapleGuild gs) { // playername that invited -> guildname that was invited but we also don't
        // care
        final int inviteid = GuildManager.getInvitedId(c.getPlayer().getGuildId());
        if (inviteid > 0) {
            final int newAlliance = AllianceManager.getAllianceLeader(inviteid);
            if (newAlliance > 0) {
                final MapleCharacter chr =
                        c.getChannelServer().getPlayerStorage().getCharacterById(newAlliance);
                if (chr != null) {
                    chr.dropMessage(5, gs.getName() + " Guild has rejected the Guild Union invitation.");
                }
                GuildManager.setInvitedId(c.getPlayer().getGuildId(), 0);
            }
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}
