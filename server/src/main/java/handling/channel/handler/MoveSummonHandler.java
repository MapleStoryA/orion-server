package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import server.movement.MovePath;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class MoveSummonHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final int oid = slea.readInt();
    MapleCharacter chr = c.getPlayer();
    final MovePath path = new MovePath();
    path.decode(slea);
    if (chr == null) {
      return;
    }
    for (MapleSummon sum : chr.getSummons().values()) {
      if (sum.getObjectId() == oid && sum.getMovementType() != SummonMovementType.STATIONARY) {
        updatePosition(path, sum, 0);
        byte[] packet = MaplePacketCreator.moveSummon(chr.getId(), oid, path);
        chr.getMap().broadcastMessage(chr, packet,  sum.getPosition());
        break;
      }
    }

  }

}
