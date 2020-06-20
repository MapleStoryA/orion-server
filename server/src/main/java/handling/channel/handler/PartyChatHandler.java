package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.CommandProcessor;
import constants.ServerConstants.CommandType;
import handling.AbstractMaplePacketHandler;
import handling.world.World;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PartyChatHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.getPlayer().updateTick(slea.readInt());
    MapleCharacter chr = c.getPlayer();
    final int type = slea.readByte();
    final byte numRecipients = slea.readByte();
    int recipients[] = new int[numRecipients];

    for (byte i = 0; i < numRecipients; i++) {
      recipients[i] = slea.readInt();
    }
    final String chattext = slea.readMapleAsciiString();
    if (chr == null || !chr.getCanTalk()) {
      c.getSession()
          .write(MaplePacketCreator.serverNotice(6, "You have been muted and are therefore unable to talk."));
      return;
    }
    if (CommandProcessor.processCommand(c, chattext, CommandType.NORMAL)) {
      return;
    }
    chr.getCheatTracker().checkMsg();
    switch (type) {
      case 0:
        World.Buddy.buddyChat(recipients, chr.getId(), chr.getName(), chattext);
        break;
      case 1:
        if (chr.getParty() == null) {
          break;
        }
        World.Party.partyChat(chr.getParty().getId(), chattext, chr.getName());
        break;
      case 2:
        if (chr.getGuildId() <= 0) {
          break;
        }
        World.Guild.guildChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
        break;
      case 3:
        if (chr.getGuildId() <= 0) {
          break;
        }
        World.Alliance.allianceChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
        break;
      case 6:
        if (chr.getParty() == null || chr.getParty().getExpeditionId() <= 0) {
          break;
        }
        World.Party.expedChat(chr.getParty().getExpeditionId(), chattext, chr.getName());
    }

  }

}
