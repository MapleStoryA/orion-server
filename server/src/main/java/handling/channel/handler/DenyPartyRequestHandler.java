package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.PartyHandlerUtils;
import handling.channel.handler.utils.PartyHandlerUtils.PartyOperation;
import handling.world.World;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MapleUserPackets;

public class DenyPartyRequestHandler extends AbstractMaplePacketHandler {


  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    // System.out.println("party response..." + slea.toString());
    final int action = slea.readByte();
    final int partyid = slea.readInt();
    final MapleCharacter chr = c.getPlayer();
    if (chr.getParty() != null) {
      chr.dropMessage(5, "You can't join the party as you are already in one.");
      return;
    }

    final MapleParty party = World.Party.getParty(partyid);
    if (party == null) {
      if (action == 0x1B) {
        chr.dropMessage(5, "The party you are trying to join does not exist.");
      }
      return;
    }
    if (party != null && party.getExpeditionId() > 0) {
      chr.dropMessage(5, "The party you are trying to join does not exist.");
      return;
    }
    if (action == 0x1B) { // Accept
      if (party.getMembers().size() < 6) {
        chr.setParty(party);
        World.Party.updateParty(partyid, PartyOperation.JOIN, new MaplePartyCharacter(chr));
        chr.receivePartyMemberHP();
        chr.updatePartyMemberHP();
      } else {
        c.getSession().write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.PARTY_FULL));
      }
    } else {
      final MapleCharacter cfrom = c.getChannelServer().getPlayerStorage()
          .getCharacterById(party.getLeader().getId());
      if (cfrom != null) {
        if (action == 0x16) {
          cfrom.getClient().getSession()
              .write(MapleUserPackets.partyStatusMessage(PartyHandlerUtils.INVITE_MSG, chr.getName()));
        } else if (action == 0x19) {
          cfrom.dropMessage(5, "You have already invited '" + chr.getName() + "' to your party.");
        } else { // Deny
          cfrom.dropMessage(5, chr.getName() + " have denied request to the party.");
        }
      }
    }

  }

}
