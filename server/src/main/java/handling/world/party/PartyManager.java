package handling.world.party;

import client.MapleCharacter;
import database.DatabaseConnection;
import handling.channel.handler.utils.PartyHandlerUtils;
import handling.world.WorldServer;
import handling.world.expedition.ExpeditionType;
import handling.world.expedition.MapleExpedition;
import handling.world.helper.FindCommand;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import tools.MaplePacketCreator;
import tools.packet.MapleUserPackets;

public class PartyManager {

    private static final Map<Integer, MapleParty> parties = new HashMap<>();
    private static final Map<Integer, MapleExpedition> expeditions = new HashMap<>();
    private static final AtomicInteger runningPartyId = new AtomicInteger(1);
    private static final AtomicInteger runningExpedId = new AtomicInteger(1);

    static {
        try (var con = DatabaseConnection.getConnection();
                PreparedStatement ps =
                        con.prepareStatement("UPDATE `characters` SET `party` = -1")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void partyChat(int partyid, String chattext, String namefrom) {
        partyChat(partyid, chattext, namefrom, 1);
    }

    public static void expedChat(int expedId, String chattext, String namefrom) {
        MapleExpedition ex = getExped(expedId);
        if (ex == null) {
            return;
        }
        for (Integer i : ex.getParties()) {
            partyChat(i, chattext, namefrom, 6);
        }
    }

    public static void partyChat(int partyid, String chattext, String namefrom, int mode) {
        MapleParty party = getParty(partyid);
        if (party == null) {
            return;
        }

        for (MaplePartyCharacter partychar : party.getMembers()) {
            int ch = FindCommand.findChannel(partychar.getName());
            if (ch > 0) {
                MapleCharacter chr =
                        WorldServer.getInstance()
                                .getChannel(ch)
                                .getPlayerStorage()
                                .getCharacterByName(partychar.getName());
                if (chr != null
                        && !chr.getName().equalsIgnoreCase(namefrom)) { // Extra check just in case
                    chr.getClient()
                            .getSession()
                            .write(MaplePacketCreator.multiChat(namefrom, chattext, mode));
                }
            }
        }
    }

    public static void expedPacket(int expedId, byte[] packet, MaplePartyCharacter exception) {
        MapleExpedition ex = getExped(expedId);
        if (ex == null) {
            return;
        }
        for (Integer i : ex.getParties()) {
            partyPacket(i, packet, exception);
        }
    }

    public static void partyPacket(int partyid, byte[] packet, MaplePartyCharacter exception) {
        MapleParty party = getParty(partyid);
        if (party == null) {
            return;
        }

        for (MaplePartyCharacter partychar : party.getMembers()) {
            int ch = FindCommand.findChannel(partychar.getName());
            if (ch > 0 && (exception == null || partychar.getId() != exception.getId())) {
                MapleCharacter chr =
                        WorldServer.getInstance()
                                .getChannel(ch)
                                .getPlayerStorage()
                                .getCharacterByName(partychar.getName());
                if (chr != null) {
                    chr.getClient().getSession().write(packet);
                }
            }
        }
    }

    public static void expedMessage(int expedId, String chattext) {
        MapleExpedition ex = getExped(expedId);
        if (ex == null) {
            return;
        }
        for (Integer i : ex.getParties()) {
            partyMessage(i, chattext);
        }
    }

    public static void partyMessage(int partyid, String chattext) {
        MapleParty party = getParty(partyid);
        if (party == null) {
            return;
        }

        for (MaplePartyCharacter partychar : party.getMembers()) {
            int ch = FindCommand.findChannel(partychar.getName());
            if (ch > 0) {
                MapleCharacter chr =
                        WorldServer.getInstance()
                                .getChannel(ch)
                                .getPlayerStorage()
                                .getCharacterByName(partychar.getName());
                if (chr != null) {
                    chr.dropMessage(5, chattext);
                }
            }
        }
    }

    public static void updateParty(
            int partyid, PartyHandlerUtils.PartyOperation operation, MaplePartyCharacter target) {
        MapleParty party = getParty(partyid);
        if (party == null) {
            return;
        }

        int oldExped = party.getExpeditionId();
        int oldInd = -1;
        if (oldExped > 0) {
            MapleExpedition exped = getExped(oldExped);
            if (exped != null) {
                oldInd = exped.getIndex(partyid);
            }
        }
        switch (operation) {
            case JOIN:
                party.addMember(target);
                break;
            case EXPEL:
            case LEAVE:
            case MOVE_MEMBER:
                party.removeMember(target);
                break;
            case DISBAND:
            case DISBAND_IN_EXPEDITION:
                disbandParty(
                        partyid,
                        operation == PartyHandlerUtils.PartyOperation.DISBAND_IN_EXPEDITION);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                party.updateMember(target);
                break;
            case CHANGE_LEADER:
            case CHANGE_LEADER_DC:
                party.setLeader(target);
                break;
            default:
                throw new RuntimeException("Unhandeled updateParty operation " + operation.name());
        }

        if (operation == PartyHandlerUtils.PartyOperation.LEAVE
                || operation == PartyHandlerUtils.PartyOperation.MOVE_MEMBER
                || operation == PartyHandlerUtils.PartyOperation.EXPEL) {
            int chz = FindCommand.findChannel(target.getName());
            if (chz > 0) {
                MapleCharacter chr =
                        WorldServer.getInstance()
                                .getStorage(chz)
                                .getCharacterByName(target.getName());
                if (chr != null) {
                    chr.setParty(null);
                    chr.getClient()
                            .getSession()
                            .write(
                                    MapleUserPackets.updateParty(
                                            chr.getClient().getChannel(),
                                            party,
                                            operation,
                                            target));
                    if (oldExped > 0 && operation != PartyHandlerUtils.PartyOperation.MOVE_MEMBER) {
                        // Broadcast to self
                        chr.getClient()
                                .getSession()
                                .write(
                                        MapleUserPackets.removeExpedition(
                                                operation == PartyHandlerUtils.PartyOperation.LEAVE
                                                        ? 61
                                                        : 63));
                        // Broadcast to remaining member
                        expedPacket(
                                oldExped,
                                MapleUserPackets.expeditionNotice(
                                        operation == PartyHandlerUtils.PartyOperation.LEAVE
                                                ? 60
                                                : 62,
                                        chr.getName()),
                                new MaplePartyCharacter(chr));
                    }
                }
            }
        }
        if (party.getMembers().size() <= 0) {
            disbandParty(
                    partyid, operation == PartyHandlerUtils.PartyOperation.DISBAND_IN_EXPEDITION);
        }
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar == null) {
                continue;
            }
            int ch = FindCommand.findChannel(partychar.getName());
            if (ch > 0) {
                MapleCharacter chr =
                        WorldServer.getInstance()
                                .getChannel(ch)
                                .getPlayerStorage()
                                .getCharacterByName(partychar.getName());
                if (chr != null) {
                    if (operation == PartyHandlerUtils.PartyOperation.DISBAND
                            || operation
                                    == PartyHandlerUtils.PartyOperation.DISBAND_IN_EXPEDITION) {
                        chr.setParty(null);
                        if (oldExped > 0 && oldInd > -1) {
                            // Broadcast to self ("You have left the expedition")
                            chr.getClient()
                                    .getSession()
                                    .write(MapleUserPackets.removeExpedition(61));
                            // Broadcast to others
                            expedPacket(
                                    oldExped,
                                    MapleUserPackets.expeditionNotice(60, chr.getName()),
                                    new MaplePartyCharacter(chr));
                        }
                    } else {
                        chr.setParty(party);
                    }
                    chr.getClient()
                            .getSession()
                            .write(
                                    MapleUserPackets.updateParty(
                                            chr.getClient().getChannel(),
                                            party,
                                            operation,
                                            target));
                }
            }
        }
        if (oldExped > 0
                && oldInd
                        > -1 /*&& operation != PartyOperation.DISBAND && operation != PartyOperation.EXPEL && operation != PartyOperation.LEAVE*/) {
            expedPacket(
                    oldExped,
                    MapleUserPackets.expeditionUpdate(oldInd, party),
                    (operation == PartyHandlerUtils.PartyOperation.LOG_ONOFF
                                    || operation == PartyHandlerUtils.PartyOperation.SILENT_UPDATE)
                            ? target
                            : null);
        }
    }

    public static MapleParty createParty(MaplePartyCharacter chrfor) {
        int partyid = runningPartyId.getAndIncrement();
        MapleParty party = new MapleParty(partyid, chrfor);
        parties.put(party.getId(), party);
        return party;
    }

    public static MapleParty createParty(MaplePartyCharacter chrfor, int expedId) {
        ExpeditionType ex = ExpeditionType.getById(expedId);
        int partyid = runningPartyId.getAndIncrement();
        int expid = runningExpedId.getAndIncrement();
        MapleParty party = new MapleParty(partyid, chrfor, ex != null ? expid : -1);
        parties.put(party.getId(), party);
        if (ex != null) {
            MapleExpedition exp = new MapleExpedition(ex, chrfor.getId(), party.getExpeditionId());
            exp.getParties().add(party.getId());
            expeditions.put(party.getExpeditionId(), exp);
        }
        return party;
    }

    public static MapleParty createPartyAndAdd(MaplePartyCharacter chrfor, int expedId) {
        MapleExpedition ex = getExped(expedId);
        if (ex == null) {
            return null;
        }
        MapleParty party = new MapleParty(runningPartyId.getAndIncrement(), chrfor, expedId);
        parties.put(party.getId(), party);
        ex.getParties().add(party.getId());
        return party;
    }

    public static MapleParty getParty(int partyid) {
        return parties.get(partyid);
    }

    public static MapleExpedition getExped(int partyid) {
        return expeditions.get(partyid);
    }

    public static MapleParty disbandParty(int partyid) {
        return disbandParty(partyid, false);
    }

    public static MapleParty disbandParty(int partyid, boolean inExpedition) {
        MapleParty ret = parties.remove(partyid);
        if (ret == null) {
            return null;
        }
        if (ret.getExpeditionId()
                > 0) { // Below only used when leader of a party in an expedition disband his/her
            // party(not exp ldr)
            MapleExpedition me = getExped(ret.getExpeditionId());
            if (me != null) {
                int ind = me.getIndex(partyid);
                if (ind >= 0) {
                    me.getParties().remove(ind);
                    // expedPacket(me.getId(), MapleUserPackets.removeExpedition(61), null);
                    // expedPacket(me.getId(), MapleUserPackets.expeditionUpdate(ind, null), null);
                }
            }
        }
        ret.disband();
        return ret;
    }

    public static MapleExpedition disbandExped(int partyid) {
        final MapleExpedition ret = expeditions.remove(partyid);
        if (ret != null) {
            for (Integer i : ret.getParties()) {
                final MapleParty pp = getParty(i);
                if (pp != null) {
                    updateParty(i, PartyHandlerUtils.PartyOperation.DISBAND, pp.getLeader());
                }
            }
        }
        return ret;
    }
}
