package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.anticheat.CheatingOffense;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class FaceExpressionHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final int emote = slea.readInt();
    if (emote > 7) {
      final int emoteid = 5159992 + emote;
      final MapleInventoryType type = GameConstants.getInventoryType(emoteid);
      if (chr.getInventory(type).findById(emoteid) == null) {
        chr.getCheatTracker().registerOffense(CheatingOffense.USING_UNAVAILABLE_ITEM, Integer.toString(emoteid));
        return;
      }
    }
    if (emote > 0 && chr != null && chr.getMap() != null) { //O_o
      chr.getMap().broadcastMessage(chr, MaplePacketCreator.facialExpression(chr, emote), false);

    }

  }

}
