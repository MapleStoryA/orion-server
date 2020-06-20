package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.maps.MapleDoor;
import server.maps.MapleMapObject;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseDoorHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final int oid = slea.readInt();
    final boolean mode = slea.readByte() == 0; // specifies if backwarp or
    // not, 1 town to target, 0
    // target to town

    for (MapleMapObject obj : chr.getMap().getAllDoorsThreadsafe()) {
      final MapleDoor door = (MapleDoor) obj;
      if (door.getOwnerId() == oid) {
        door.warp(chr, mode);
        break;
      }
    }

  }

}
