package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.channel.handler.utils.PartyHandlerUtils;
import handling.channel.handler.utils.PartyHandlerUtils.PartyOperation;
import handling.packet.AbstractMaplePacketHandler;
import handling.world.WorldServer;
import handling.world.expedition.ExpeditionType;
import handling.world.expedition.MapleExpedition;
import handling.world.helper.FindCommand;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartyManager;
import tools.data.input.InPacket;
import tools.packet.MapleUserPackets;

@lombok.extern.slf4j.Slf4j
public class ExpeditionOperationHandler extends AbstractMaplePacketHandler {

    public static final int CREATING = 0x2D;
    public static final int INVITE = 0x2E;
    public static final int RESPONSE = 0x2F;

    public static final int LEAVE = 0x30;
    public static final int KICK = 0x31;
    public static final int CHANGE_EXPEDITION_CAPTAIN = 0x32;
    public static final int CHANGE_PARTY_LEADER = 0x33;
    public static final int MOVE_TO_NEW_PARTY = 0x34;

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.getMap() == null) {
            return;
        }

        final byte mode = packet.readByte();
        switch (mode) {
            case CREATING:
                final ExpeditionType et = ExpeditionType.getById(packet.readInt());
                if (chr.getParty() != null || et == null) {
                    c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.ALREADY_JOINED));
                    return;
                }
                if (chr.getLevel() > et.maxLevel || chr.getLevel() < et.minLevel) {
                    c.getSession().write(MapleUserPackets.expeditionStatusMessage(3, chr.getName()));
                    return;
                }
                final MapleParty party = PartyManager.createParty(new MaplePartyCharacter(chr), et.exped);
                chr.setParty(party);
                c.getSession().write(MapleUserPackets.partyCreated(party.getId()));
                c.getSession()
                        .write(MapleUserPackets.showExpedition(
                                PartyManager.getExped(party.getExpeditionId()), true, false));
                break;
            case INVITE:
                final String name = packet.readMapleAsciiString();
                int theCh = FindCommand.findChannel(name);
                if (theCh <= 0) {
                    c.getSession().write(MapleUserPackets.expeditionStatusMessage(0, name));
                    return;
                }
                final MapleCharacter invited = WorldServer.getInstance()
                        .getChannel(theCh)
                        .getPlayerStorage()
                        .getCharacterByName(name);
                if (invited == null) {
                    c.getSession().write(MapleUserPackets.expeditionStatusMessage(0, name));
                    return;
                }
                final MapleParty partyI = chr.getParty();
                if (invited.getParty() != null || partyI == null || partyI.getExpeditionId() <= 0) {
                    c.getSession().write(MapleUserPackets.expeditionStatusMessage(2, name));
                    return;
                }
                final MapleExpedition me = PartyManager.getExped(partyI.getExpeditionId());
                if (me != null
                        && me.getAllMembers() < me.getType().maxMembers
                        && invited.getLevel() <= me.getType().maxLevel
                        && invited.getLevel() >= me.getType().minLevel) {
                    invited.getClient().getSession().write(MapleUserPackets.expeditionInvite(chr, me.getType().exped));
                } else {
                    c.getSession().write(MapleUserPackets.expeditionStatusMessage(3, invited.getName()));
                }
                break;
            case RESPONSE:
                final String recvName = packet.readMapleAsciiString();
                final int action = packet.readInt(); // 7 = send invite, 8 = accept, 9
                // = deny

                int theChh = FindCommand.findChannel(recvName);
                if (theChh <= 0) {
                    break;
                }
                final MapleCharacter cfrom = WorldServer.getInstance()
                        .getChannel(theChh)
                        .getPlayerStorage()
                        .getCharacterByName(recvName);
                if (cfrom == null) {
                    break;
                }
                if (action == 6 || action == 7) {
                    if (cfrom.getParty() == null || cfrom.getParty().getExpeditionId() <= 0) {
                        c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
                        return;
                    }
                    if (action == 6) {
                        cfrom.dropMessage(5, "You have already invited '" + chr.getName() + "' to the expedition.");
                    } else {
                        cfrom.getClient()
                                .getSession()
                                .write(MapleUserPackets.expeditionStatusMessage(7, chr.getName()));
                    }
                } else if (action == 8 || action == 9) {
                    if (cfrom.getParty() == null || cfrom.getParty().getExpeditionId() <= 0) {
                        if (action == 8) {
                            chr.dropMessage(1, "The expedition you are trying to join does not exist.");
                        }
                        return;
                    }
                    MapleParty partyN = cfrom.getParty();
                    final MapleExpedition exped = PartyManager.getExped(partyN.getExpeditionId());
                    if (action == 8) {
                        if (exped == null || chr.getParty() != null) {
                            if (chr.getParty() != null) {
                                cfrom.getClient()
                                        .getSession()
                                        .write(MapleUserPackets.expeditionStatusMessage(2, chr.getName()));
                            }
                            chr.dropMessage(1, "The expedition you are trying to join does not exist.");
                            return;
                        }
                        if (chr.getLevel() <= exped.getType().maxLevel
                                && chr.getLevel() >= exped.getType().minLevel
                                && exped.getAllMembers() < exped.getType().maxMembers) {
                            int partyId = exped.getFreeParty();
                            if (partyId < 0) {
                                c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.PARTY_FULL));
                            } else if (partyId == 0) {
                                partyN = PartyManager.createPartyAndAdd(new MaplePartyCharacter(chr), exped.getId());
                                chr.setParty(partyN);
                                c.getSession().write(MapleUserPackets.partyCreated(partyN.getId()));
                                c.getSession().write(MapleUserPackets.showExpedition(exped, false, false));
                                PartyManager.expedPacket(
                                        exped.getId(),
                                        MapleUserPackets.expeditionNotice(56, chr.getName()),
                                        new MaplePartyCharacter(chr));
                                PartyManager.expedPacket(
                                        exped.getId(),
                                        MapleUserPackets.expeditionUpdate(exped.getIndex(partyN.getId()), partyN),
                                        null);
                            } else {
                                chr.setParty(PartyManager.getParty(partyId));
                                PartyManager.updateParty(partyId, PartyOperation.JOIN, new MaplePartyCharacter(chr));
                                chr.receivePartyMemberHP();
                                chr.updatePartyMemberHP();
                                c.getSession().write(MapleUserPackets.showExpedition(exped, false, false));
                                PartyManager.expedPacket(
                                        exped.getId(),
                                        MapleUserPackets.expeditionNotice(56, chr.getName()),
                                        new MaplePartyCharacter(chr));
                            }
                        } else {
                            c.getSession().write(MapleUserPackets.expeditionStatusMessage(3, cfrom.getName()));
                        }
                    } else if (action == 9) {
                        cfrom.dropMessage(5, "'" + chr.getName() + " has declined the expedition invitation.");
                    }
                } else {
                    log.info("Unhandled Expedition Operation found: " + packet);
                }
                break;
            case LEAVE:
                final MapleParty part = chr.getParty();
                if (part == null || part.getExpeditionId() <= 0) {
                    c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
                    break;
                }
                MapleExpedition exped1 = PartyManager.getExped(part.getExpeditionId());
                if (exped1 != null) {
                    if (exped1.getLeader() == chr.getId()) {
                        PartyManager.expedPacket(exped1.getId(), MapleUserPackets.removeExpedition(64), null);
                        PartyManager.disbandExped(exped1.getId());
                        if (chr.getEventInstance() != null) {
                            chr.getEventInstance().disbandParty();
                        }
                    } else {
                        if (part.getLeader().getId() == chr.getId()) {
                            PartyManager.updateParty(
                                    part.getId(), PartyOperation.DISBAND_IN_EXPEDITION, new MaplePartyCharacter(chr));
                            if (chr.getEventInstance() != null) {
                                chr.getEventInstance().disbandParty();
                            }
                            PartyManager.expedPacket(
                                    exped1.getId(), MapleUserPackets.showExpedition(exped1, false, true), null);
                        } else {
                            PartyManager.updateParty(part.getId(), PartyOperation.LEAVE, new MaplePartyCharacter(chr));
                            if (chr.getEventInstance() != null) {
                                chr.getEventInstance().leftParty(chr);
                            }
                        }
                    }
                    if (chr.getPyramidSubway() != null) {
                        chr.getPyramidSubway().fail(chr);
                    }
                    chr.setParty(null);
                }
                break;
            case KICK:
                final MapleParty currentParty = chr.getParty();
                if (currentParty == null || currentParty.getExpeditionId() <= 0) {
                    c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
                    break;
                }
                final MapleExpedition currexped = PartyManager.getExped(currentParty.getExpeditionId());
                if (currexped != null && currexped.getLeader() == chr.getId()) {
                    final int toKick = packet.readInt();
                    for (Integer i : currexped.getParties()) {
                        final MapleParty partyy = PartyManager.getParty(i);
                        if (partyy != null) {
                            MaplePartyCharacter expelled = partyy.getMemberById(toKick);
                            if (expelled != null) {
                                PartyManager.updateParty(i, PartyOperation.EXPEL, expelled);
                                if (chr.getEventInstance() != null && expelled.isOnline()) {
                                    chr.getEventInstance().disbandParty();
                                }
                                if (chr.getPyramidSubway() != null && expelled.isOnline()) {
                                    chr.getPyramidSubway().fail(chr);
                                }
                                break;
                            }
                        }
                    }
                }
                break;
            case CHANGE_EXPEDITION_CAPTAIN:
                final MapleParty mparty = chr.getParty();
                if (mparty == null || mparty.getExpeditionId() <= 0) {
                    break;
                }
                final MapleExpedition expedd = PartyManager.getExped(mparty.getExpeditionId());
                if (expedd != null && expedd.getLeader() == chr.getId()) {
                    final int cid = packet.readInt();
                    final MaplePartyCharacter newleader = mparty.getMemberById(cid);
                    if (newleader != null) {
                        PartyManager.updateParty(mparty.getId(), PartyOperation.CHANGE_LEADER, newleader);
                        expedd.setLeader(newleader.getId());
                        PartyManager.expedPacket(expedd.getId(), MapleUserPackets.changeExpeditionLeader(0), null);
                    } else {
                        chr.dropMessage(
                                5, "You only can perform this action when the character is in the same" + " party.");
                    }
                }
                break;
            case CHANGE_PARTY_LEADER:
                final MapleParty mparty1 = chr.getParty();
                if (mparty1 == null || mparty1.getExpeditionId() <= 0) {
                    break;
                }
                MapleExpedition expedit = PartyManager.getExped(mparty1.getExpeditionId());
                if (expedit != null && expedit.getLeader() == chr.getId()) {
                    final int toCid = packet.readInt();
                    for (Integer i : expedit.getParties()) {
                        final MapleParty par = PartyManager.getParty(i);
                        if (par != null) {
                            MaplePartyCharacter newleader = par.getMemberById(toCid);
                            if (newleader != null && par.getId() != mparty1.getId()) {
                                if (par.getLeader() != newleader) {
                                    PartyManager.updateParty(par.getId(), PartyOperation.CHANGE_LEADER, newleader);
                                } else {
                                    chr.dropMessage(
                                            5,
                                            "You cannot perform this action as the character is"
                                                    + " already the party leader.");
                                }
                            }
                        }
                    }
                }
                break;
            case MOVE_TO_NEW_PARTY:
                final MapleParty oriPart = chr.getParty();
                if (oriPart == null || oriPart.getExpeditionId() <= 0) {
                    break;
                }
                final MapleExpedition nowExped = PartyManager.getExped(oriPart.getExpeditionId());
                if (nowExped == null || nowExped.getLeader() != chr.getId()) {
                    break;
                }
                final int partyIndexTo = packet.readInt();
                if (partyIndexTo < nowExped.getType().maxParty
                        && partyIndexTo <= nowExped.getParties().size()) {
                    final int Tcid = packet.readInt();
                    for (Integer i : nowExped.getParties()) {
                        final MapleParty par = PartyManager.getParty(i);
                        if (par == null) {
                            continue;
                        }
                        MaplePartyCharacter expelled = par.getMemberById(Tcid);
                        if (expelled != null && expelled.isOnline()) {
                            final MapleCharacter player = WorldServer.getInstance()
                                    .getStorage(expelled.getChannel())
                                    .getCharacterById(expelled.getId());
                            if (player == null) {
                                break;
                            }
                            if (partyIndexTo < nowExped.getParties().size()) {
                                final MapleParty partyIndex = PartyManager.getParty(
                                        nowExped.getParties().get(partyIndexTo));
                                if (partyIndex == null
                                        || partyIndex.getMembers().size() >= 6) {
                                    chr.dropMessage(5, "You can't move a character to a non-existent party.");
                                    break;
                                }
                            }
                            PartyManager.updateParty(i, PartyOperation.MOVE_MEMBER, expelled);
                            if (partyIndexTo < nowExped.getParties().size()) {
                                final MapleParty oldParty = PartyManager.getParty(
                                        nowExped.getParties().get(partyIndexTo));
                                if (oldParty != null && oldParty.getMembers().size() < 6) {
                                    PartyManager.updateParty(oldParty.getId(), PartyOperation.JOIN, expelled);
                                    player.receivePartyMemberHP();
                                    player.updatePartyMemberHP();
                                    player.getClient()
                                            .getSession()
                                            .write(MapleUserPackets.showExpedition(nowExped, false, true));
                                }
                            } else { // Moving to a new party
                                final MapleParty newParty = PartyManager.createPartyAndAdd(expelled, nowExped.getId());
                                player.setParty(newParty);
                                player.getClient().getSession().write(MapleUserPackets.partyCreated(newParty.getId()));
                                player.getClient()
                                        .getSession()
                                        .write(MapleUserPackets.showExpedition(nowExped, false, true));
                                PartyManager.expedPacket(
                                        nowExped.getId(),
                                        MapleUserPackets.expeditionUpdate(
                                                nowExped.getIndex(newParty.getId()), newParty),
                                        null);
                            }
                            if (chr.getEventInstance() != null && expelled.isOnline()) {
                                chr.getEventInstance().disbandParty();
                            }
                            if (chr.getPyramidSubway() != null) {
                                chr.getPyramidSubway().fail(chr);
                            }
                            break;
                        }
                    }
                }
                break;
        }
    }
}
