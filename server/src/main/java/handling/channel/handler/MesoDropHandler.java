package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class MesoDropHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    chr.updateTick(slea.readInt());
    int meso = slea.readInt();


    if (!chr.isAlive() || (meso < 10 || meso > 50000) || (meso > chr.getMeso())) {
      chr.getClient().getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    chr.gainMeso(-meso, false, true);
    chr.getMap().spawnMesoDrop(meso, chr.getPosition(), chr, chr, true, (byte) 0);
    chr.getCheatTracker().checkDrop(true);
  }

}
