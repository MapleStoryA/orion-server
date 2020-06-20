package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.ChannelServer;
import handling.channel.handler.utils.PartyHandlerUtils;
import handling.channel.handler.utils.PartyHandlerUtils.PartyOperation;
import handling.world.World;
import handling.world.expedition.ExpeditionType;
import handling.world.expedition.MapleExpedition;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MapleUserPackets;

public class ExpeditionOperationHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null || chr.getMap() == null) {
      return;
    }

    final byte mode = slea.readByte();
    switch (mode) {
      case 0x2D: // Creating
        final ExpeditionType et = ExpeditionType.getById(slea.readInt());
        if (chr.getParty() != null || et == null) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.ALREADY_JOINED));
          return;
        }
        if (chr.getLevel() > et.maxLevel || chr.getLevel() < et.minLevel) {
          c.getSession().write(MapleUserPackets.expeditionStatusMessage(3, chr.getName()));
          return;
        }
        final MapleParty party = World.Party.createParty(new MaplePartyCharacter(chr), et.exped);
        chr.setParty(party);
        c.getSession().write(MapleUserPackets.partyCreated(party.getId()));
        c.getSession()
            .write(MapleUserPackets.showExpedition(World.Party.getExped(party.getExpeditionId()), true, false));
        break;
      case 0x2E: // Invite
        final String name = slea.readMapleAsciiString();
        int theCh = World.Find.findChannel(name);
        if (theCh <= 0) {
          c.getSession().write(MapleUserPackets.expeditionStatusMessage(0, name));
          return;
        }
        final MapleCharacter invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(name);
        if (invited == null) {
          c.getSession().write(MapleUserPackets.expeditionStatusMessage(0, name));
          return;
        }
        final MapleParty partyI = chr.getParty();
        if (invited.getParty() != null || partyI == null || partyI.getExpeditionId() <= 0) {
          c.getSession().write(MapleUserPackets.expeditionStatusMessage(2, name));
          return;
        }
        final MapleExpedition me = World.Party.getExped(partyI.getExpeditionId());
        if (me != null && me.getAllMembers() < me.getType().maxMembers
            && invited.getLevel() <= me.getType().maxLevel && invited.getLevel() >= me.getType().minLevel) {
          invited.getClient().getSession().write(MapleUserPackets.expeditionInvite(chr, me.getType().exped));
        } else {
          c.getSession().write(MapleUserPackets.expeditionStatusMessage(3, invited.getName()));
        }
        break;
      case 0x2F: // Response
        final String recvName = slea.readMapleAsciiString();
        final int action = slea.readInt(); // 7 = send invite, 8 = accept, 9
        // = deny

        int theChh = World.Find.findChannel(recvName);
        if (theChh <= 0) {
          break;
        }
        final MapleCharacter cfrom = ChannelServer.getInstance(theChh).getPlayerStorage()
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
            cfrom.getClient().getSession().write(MapleUserPackets.expeditionStatusMessage(7, chr.getName()));
          }
        } else if (action == 8 || action == 9) {
          if (cfrom.getParty() == null || cfrom.getParty().getExpeditionId() <= 0) {
            if (action == 8) {
              chr.dropMessage(1, "The expedition you are trying to join does not exist.");
            }
            return;
          }
          MapleParty partyN = cfrom.getParty();
          final MapleExpedition exped = World.Party.getExped(partyN.getExpeditionId());
          if (action == 8) {
            if (exped == null || chr.getParty() != null) {
              if (chr.getParty() != null) {
                cfrom.getClient().getSession()
                    .write(MapleUserPackets.expeditionStatusMessage(2, chr.getName()));
              }
              chr.dropMessage(1, "The expedition you are trying to join does not exist.");
              return;
            }
            if (chr.getLevel() <= exped.getType().maxLevel && chr.getLevel() >= exped.getType().minLevel
                && exped.getAllMembers() < exped.getType().maxMembers) {
              int partyId = exped.getFreeParty();
              if (partyId < 0) {
                c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.PARTY_FULL));
              } else if (partyId == 0) {
                partyN = World.Party.createPartyAndAdd(new MaplePartyCharacter(chr), exped.getId());
                chr.setParty(partyN);
                c.getSession().write(MapleUserPackets.partyCreated(partyN.getId()));
                c.getSession().write(MapleUserPackets.showExpedition(exped, false, false));
                World.Party.expedPacket(exped.getId(), MapleUserPackets.expeditionNotice(56, chr.getName()),
                    new MaplePartyCharacter(chr));
                World.Party.expedPacket(exped.getId(),
                    MapleUserPackets.expeditionUpdate(exped.getIndex(partyN.getId()), partyN), null);
              } else {
                chr.setParty(World.Party.getParty(partyId));
                World.Party.updateParty(partyId, PartyOperation.JOIN, new MaplePartyCharacter(chr));
                chr.receivePartyMemberHP();
                chr.updatePartyMemberHP();
                c.getSession().write(MapleUserPackets.showExpedition(exped, false, false));
                World.Party.expedPacket(exped.getId(), MapleUserPackets.expeditionNotice(56, chr.getName()),
                    new MaplePartyCharacter(chr));
              }
            } else {
              c.getSession().write(MapleUserPackets.expeditionStatusMessage(3, cfrom.getName()));
            }
          } else if (action == 9) {
            cfrom.dropMessage(5, "'" + chr.getName() + " has declined the expedition invitation.");
          }
        } else {
          System.out.println("Unhandled Expedition Operation found: " + slea.toString());
        }
        break;
      case 0x30: // Leave
        final MapleParty part = chr.getParty();
        if (part == null || part.getExpeditionId() <= 0) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
          break;
        }
        MapleExpedition exped1 = World.Party.getExped(part.getExpeditionId());
        if (exped1 != null) {
          if (exped1.getLeader() == chr.getId()) {
            World.Party.expedPacket(exped1.getId(), MapleUserPackets.removeExpedition(64), null);
            World.Party.disbandExped(exped1.getId());
            if (chr.getEventInstance() != null) {
              chr.getEventInstance().disbandParty();
            }
          } else {
            if (part.getLeader().getId() == chr.getId()) {
              World.Party.updateParty(part.getId(), PartyOperation.DISBAND_IN_EXPEDITION,
                  new MaplePartyCharacter(chr));
              if (chr.getEventInstance() != null) {
                chr.getEventInstance().disbandParty();
              }
              World.Party.expedPacket(exped1.getId(), MapleUserPackets.showExpedition(exped1, false, true),
                  null);
            } else {
              World.Party.updateParty(part.getId(), PartyOperation.LEAVE, new MaplePartyCharacter(chr));
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
      case 0x31: // Kick
        final MapleParty currentParty = chr.getParty();
        if (currentParty == null || currentParty.getExpeditionId() <= 0) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
          break;
        }
        final MapleExpedition currexped = World.Party.getExped(currentParty.getExpeditionId());
        if (currexped != null && currexped.getLeader() == chr.getId()) {
          final int toKick = slea.readInt();
          for (Integer i : currexped.getParties()) {
            final MapleParty partyy = World.Party.getParty(i);
            if (partyy != null) {
              MaplePartyCharacter expelled = partyy.getMemberById(toKick);
              if (expelled != null) {
                World.Party.updateParty(i, PartyOperation.EXPEL, expelled);
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
      case 0x32: // Change Expedition Captain
        final MapleParty mparty = chr.getParty();
        if (mparty == null || mparty.getExpeditionId() <= 0) {
          break;
        }
        final MapleExpedition expedd = World.Party.getExped(mparty.getExpeditionId());
        if (expedd != null && expedd.getLeader() == chr.getId()) {
          final int cid = slea.readInt();
          final MaplePartyCharacter newleader = mparty.getMemberById(cid);
          if (newleader != null) {
            World.Party.updateParty(mparty.getId(), PartyOperation.CHANGE_LEADER, newleader);
            expedd.setLeader(newleader.getId());
            World.Party.expedPacket(expedd.getId(), MapleUserPackets.changeExpeditionLeader(0), null);
          } else {
            chr.dropMessage(5, "You only can perform this action when the character is in the same party.");
          }
        }
        break;
      case 0x33: // Change Party Leader
        final MapleParty mparty1 = chr.getParty();
        if (mparty1 == null || mparty1.getExpeditionId() <= 0) {
          break;
        }
        MapleExpedition expedit = World.Party.getExped(mparty1.getExpeditionId());
        if (expedit != null && expedit.getLeader() == chr.getId()) {
          final int toCid = slea.readInt();
          for (Integer i : expedit.getParties()) {
            final MapleParty par = World.Party.getParty(i);
            if (par != null) {
              MaplePartyCharacter newleader = par.getMemberById(toCid);
              if (newleader != null && par.getId() != mparty1.getId()) {
                if (par.getLeader() != newleader) {
                  World.Party.updateParty(par.getId(), PartyOperation.CHANGE_LEADER, newleader);
                } else {
                  chr.dropMessage(5,
                      "You cannot perform this action as the character is already the party leader.");
                }
              }
            }
          }
        }
        break;
      case 0x34: // Move to new party (got to check this from msea), ask
        // someone mvoe me to new party
        final MapleParty oriPart = chr.getParty();
        if (oriPart == null || oriPart.getExpeditionId() <= 0) {
          break;
        }
        final MapleExpedition nowExped = World.Party.getExped(oriPart.getExpeditionId());
        if (nowExped == null || nowExped.getLeader() != chr.getId()) {
          break;
        }
        final int partyIndexTo = slea.readInt();
        if (partyIndexTo < nowExped.getType().maxParty && partyIndexTo <= nowExped.getParties().size()) {
          final int Tcid = slea.readInt();
          for (Integer i : nowExped.getParties()) {
            final MapleParty par = World.Party.getParty(i);
            if (par == null) {
              continue;
            }
            MaplePartyCharacter expelled = par.getMemberById(Tcid);
            if (expelled != null && expelled.isOnline()) {
              final MapleCharacter player = World.getStorage(expelled.getChannel())
                  .getCharacterById(expelled.getId());
              if (player == null) {
                break;
              }
              if (partyIndexTo < nowExped.getParties().size()) {
                final MapleParty partyIndex = World.Party.getParty(nowExped.getParties().get(partyIndexTo));
                if (partyIndex == null || partyIndex.getMembers().size() >= 6) {
                  chr.dropMessage(5, "You can't move a character to a non-existent party.");
                  break;
                }
              }
              World.Party.updateParty(i, PartyOperation.MOVE_MEMBER, expelled);
              if (partyIndexTo < nowExped.getParties().size()) {
                final MapleParty oldParty = World.Party.getParty(nowExped.getParties().get(partyIndexTo));
                if (oldParty != null && oldParty.getMembers().size() < 6) {
                  World.Party.updateParty(oldParty.getId(), PartyOperation.JOIN, expelled);
                  player.receivePartyMemberHP();
                  player.updatePartyMemberHP();
                  player.getClient().getSession()
                      .write(MapleUserPackets.showExpedition(nowExped, false, true));
                }
              } else { // Moving to a new party
                final MapleParty newParty = World.Party.createPartyAndAdd(expelled, nowExped.getId());
                player.setParty(newParty);
                player.getClient().getSession().write(MapleUserPackets.partyCreated(newParty.getId()));
                player.getClient().getSession()
                    .write(MapleUserPackets.showExpedition(nowExped, false, true));
                World.Party.expedPacket(nowExped.getId(),
                    MapleUserPackets.expeditionUpdate(nowExped.getIndex(newParty.getId()), newParty),
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
