package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.maps.MapleDragon;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.Map.Entry;

public class EnterMapRequestHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

    if (c.getPlayer().isHidden() && c.getPlayer().isGM() || c.getPlayer().getMap() == null) {
      c.enableActions();
      return;
    }
    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnPlayerMapobject(c.getPlayer()));
    MapleDragon dragon = c.getPlayer().getDragon();
    if (dragon != null) {
      c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnDragon(dragon));
    }

    for (Entry<Integer, MapleSummon> summon : c.getPlayer().getSummons().entrySet()) {
      c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnSummon(summon.getValue(), false));
    }


  }

}
