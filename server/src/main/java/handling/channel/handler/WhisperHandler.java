package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.ChannelServer;
import handling.world.World;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class WhisperHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final byte mode = slea.readByte();
    slea.readInt(); // ticks
    switch (mode) {
      case 68: // buddy
      case 5: { // Find

        final String recipient = slea.readMapleAsciiString();
        MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
        if (player != null) {
          if (!player.isGM() || c.getPlayer().isGM() && player.isGM()) {

            c.getSession().write(MaplePacketCreator.getFindReplyWithMap(player.getName(),
                player.getMap().getId(), mode == 68));
          } else {
            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
          }
        } else { // Not found
          int ch = World.Find.findChannel(recipient);
          if (ch > 0) {
            player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
            if (player == null) {
              break;
            }
            if (player != null) {
              if (!player.isGM() || (c.getPlayer().isGM() && player.isGM())) {
                c.getSession().write(MaplePacketCreator.getFindReply(recipient, (byte) ch, mode == 68));
              } else {
                c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
              }
              return;
            }
          }
          if (ch == -10) {
            c.getSession().write(MaplePacketCreator.getFindReplyWithCS(recipient, mode == 68));
          } else if (ch == -20) {
            c.getSession().write(MaplePacketCreator.getFindReplyWithMTS(recipient, mode == 68));
          } else {
            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
          }
        }
        break;
      }
      case 6: { // Whisper
        if (!c.getPlayer().getCanTalk()) {
          c.getSession().write(
              MaplePacketCreator.serverNotice(6, "You have been muted and are therefore unable to talk."));
          return;
        }
        c.getPlayer().getCheatTracker().checkMsg();
        final String recipient = slea.readMapleAsciiString();
        final String text = slea.readMapleAsciiString();
        final int ch = World.Find.findChannel(recipient);
        if (ch > 0) {
          MapleCharacter player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
          if (player == null) {
            break;
          }
          player.getClient().getSession()
              .write(MaplePacketCreator.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
          if (!c.getPlayer().isGM() && player.isGM() && player.isHidden()) {
            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
          } else {
            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
          }
        } else {
          c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
        }
      }
      break;
    }

  }

}
