package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import server.maps.MapleNodes.MapleNodeInfo;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class MobNodeHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From
    final int newNode = slea.readInt();
    final int nodeSize = chr.getMap().getNodes().size();
    if (mob_from != null && nodeSize > 0 && nodeSize >= newNode) {
      final MapleNodeInfo mni = chr.getMap().getNode(newNode);
      if (mni == null) {
        return;
      }
      if (mni.attr == 2) { // talk
        chr.getMap().talkMonster("Please escort me carefully.", 5120035, mob_from.getObjectId()); // temporary
      }
      if (mob_from.getLastNode() >= newNode) {
        return;
      }
      mob_from.setLastNode(newNode);
      if (nodeSize == newNode) { // the last node on the map.
        int newMap = -1;
        switch (chr.getMapId() / 100) {
          case 9211200:
            newMap = 921120100;
            break;
          case 9211201:
            newMap = 921120200;
            break;
          case 9211202:
            newMap = 921120300;
            break;
          case 9211203:
            newMap = 921120400;
            break;
          case 9211204:
            chr.getMap().removeMonster(mob_from);
            break;

        }
        if (newMap > 0) {
          chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, "Proceed to the next stage."));
          chr.getMap().removeMonster(mob_from);
        }
      }
    }

  }

}
