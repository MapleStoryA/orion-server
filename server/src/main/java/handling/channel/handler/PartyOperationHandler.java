package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.PartyHandlerUtils;
import handling.channel.handler.utils.PartyHandlerUtils.PartyOperation;
import handling.world.World;
import handling.world.expedition.MapleExpedition;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MapleUserPackets;

public class PartyOperationHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

    final int operation = slea.readByte();
    final MapleCharacter chr = c.getPlayer();
    final MaplePartyCharacter partyplayer = new MaplePartyCharacter(chr);
    MapleParty party = chr.getParty();

    switch (operation) {
      case 1: // Create
        if (((chr.getJob() == 0 || chr.getJob() == 1000 || chr.getJob() == 2000 || chr.getJob() == 2001)
            && chr.getLevel() <= 10) || chr.getLevel() <= 10) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.BEGINNER_NO_PARTY));
          return;
        }
        if (chr.getParty() == null) {
          party = World.Party.createParty(partyplayer);
          if (party == null) {
            c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
            return;
          }
          chr.setParty(party);
          c.getSession().write(MapleUserPackets.partyCreated(party.getId()));
        } else {
          if (party.getExpeditionId() > 0) {
            chr.dropMessage(5, "You may not do party operations while in a raid.");
            return;
          }
          if (partyplayer.equals(party.getLeader()) && party.getMembers().size() == 1) { // only
            // one,
            // reupdate
            c.getSession().write(MapleUserPackets.partyCreated(party.getId()));
          } else {
            c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.ALREADY_JOINED));
          }
        }
        break;
      case 2: // Leave
        if (party != null) {
          if (party.getExpeditionId() > 0) {
            final MapleExpedition exped1 = World.Party.getExped(party.getExpeditionId());
            if (exped1 != null) {
              if (exped1.getLeader() == chr.getId()) {
                World.Party.expedPacket(exped1.getId(), MapleUserPackets.removeExpedition(64), null);
                World.Party.disbandExped(exped1.getId());
                if (chr.getEventInstance() != null) {
                  chr.getEventInstance().disbandParty();
                  chr.getNewEventInstance().onPartyDisband(chr);

                }
              } else {
                if (party.getLeader().getId() == chr.getId()) {
                  World.Party.updateParty(party.getId(), PartyOperation.DISBAND_IN_EXPEDITION,
                      new MaplePartyCharacter(chr));
                  if (chr.getEventInstance() != null) {
                    chr.getEventInstance().disbandParty();
                    chr.getNewEventInstance().onPartyDisband(chr);
                  }
                  World.Party.expedPacket(exped1.getId(),
                      MapleUserPackets.showExpedition(exped1, false, true), null);
                } else {
                  World.Party.updateParty(party.getId(), PartyOperation.LEAVE,
                      new MaplePartyCharacter(chr));
                  if (chr.getEventInstance() != null) {
                    chr.getEventInstance().leftParty(chr);
                    chr.getNewEventInstance().onPlayerLeaveParty(chr);
                  }
                }
              }
              if (chr.getPyramidSubway() != null) {
                chr.getPyramidSubway().fail(chr);
              }
            }
          } else {
            if (partyplayer.equals(party.getLeader())) { // disband
              World.Party.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
              if (chr.getEventInstance() != null) {
                chr.getEventInstance().disbandParty();
              }
              if (chr.getPyramidSubway() != null) {
                chr.getPyramidSubway().fail(chr);
              }
            } else {
              World.Party.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
              if (chr.getEventInstance() != null) {
                chr.getEventInstance().leftParty(chr);
              }
              if (chr.getPyramidSubway() != null) {
                chr.getPyramidSubway().fail(chr);
              }
            }
          }
        } else {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
        }
        chr.setParty(null);
        break;
      case 4: // Invite
        final String name = slea.readMapleAsciiString();
        final MapleCharacter invited = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
        if (invited == null) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NON_EXISTANT));
          return;
        }
        if (invited.getParty() != null) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.ALREADY_JOINED));
          return;
        }
        if (party == null) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
          return;
        }
        if (party.getExpeditionId() > 0) {
          chr.dropMessage(5, "You cannot send an invite when you are in a raid.");
          return;
        }
        if (party.getMembers().size() < 6) {
          invited.getClient().getSession().write(MapleUserPackets.partyInvite(chr));
        } else {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.PARTY_FULL)); // Full
          // capacity
        }
        break;
      case 5: // Expel
        final int cid = slea.readInt();
        if (party == null || partyplayer == null) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
          return;
        }
        if (!partyplayer.equals(party.getLeader())) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NO_EXPEL));
          return;
        }
        if (party.getExpeditionId() > 0) {
          chr.dropMessage(5, "You may not do party operations while in a raid.");
          return;
        }
        final MaplePartyCharacter expelled = party.getMemberById(cid);
        if (expelled != null) { // todo: add map field limit check
          World.Party.updateParty(party.getId(), PartyOperation.EXPEL, expelled);
          if (chr.getEventInstance() != null && expelled.isOnline()) {
            chr.getEventInstance().disbandParty();
          }
          if (chr.getPyramidSubway() != null && expelled.isOnline()) {
            chr.getPyramidSubway().fail(chr);
          }
        }
        break;
      case 6: // Change leader
        final int newLeader = slea.readInt();
        if (party == null) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NOT_IN_PARTY));
          return;
        }
        if (!partyplayer.equals(party.getLeader())) {
          c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.NO_EXPEL));
          return;
        }
        if (party.getExpeditionId() > 0) {
          chr.dropMessage(5, "You may not do party operations while in a raid.");
          return;
        }
        final MaplePartyCharacter newLeadr = party.getMemberById(newLeader);
        final MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterById(newLeader);
        if (newLeadr != null && cfrom.getMapId() == chr.getMapId()) { // todo:
          // add
          // map
          // field
          // limit
          // check
          World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newLeadr);
        } else {
          chr.dropMessage(5, "The Party Leader can only be handed over to the party member in the same map.");
        }
        break;
      default:
        System.out.println("Unhandled Party function." + operation);
        break;
    }

  }

}
