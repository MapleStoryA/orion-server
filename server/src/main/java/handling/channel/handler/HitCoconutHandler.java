package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.events.MapleCoconut;
import server.events.MapleCoconut.MapleCoconuts;
import server.events.MapleEventType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class HitCoconutHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    /*
     * CB 00 A6 00 06 01 A6 00 = coconut id 06 01 = ?
     */
    int id = slea.readShort();
    MapleCoconut map = (MapleCoconut) c.getChannelServer().getEvent(MapleEventType.Coconut);
    if (map == null) {
      return;
    }
    // System.out.println("Coconut1");
    MapleCoconuts nut = map.getCoconut(id);
    if (nut == null || !nut.isHittable()) {
      return;
    }
    if (System.currentTimeMillis() < nut.getHitTime()) {
      return;
    }
    // System.out.println("Coconut2");
    if (nut.getHits() > 2 && Math.random() < 0.4 && !nut.isStopped()) {
      // System.out.println("Coconut3-1");
      nut.setHittable(false);
      if (Math.random() < 0.01 && map.getStopped() > 0) {
        nut.setStopped(true);
        map.stopCoconut();
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 1));
        return;
      }
      nut.resetHits(); // For next event (without restarts)
      // System.out.println("Coconut4");
      if (Math.random() < 0.05 && map.getBombings() > 0) {
        // System.out.println("Coconut5-1");
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 2));
        map.bombCoconut();
      } else if (map.getFalling() > 0) {
        // System.out.println("Coconut5-2");
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 3));
        map.fallCoconut();
        if (c.getPlayer().getCoconutTeam() == 0) {
          map.addMapleScore();
          c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(5,
              c.getPlayer().getName() + " of Team Maple knocks down a coconut."));
        } else {
          map.addStoryScore();
          c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(5,
              c.getPlayer().getName() + " of Team Story knocks down a coconut."));
        }
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.coconutScore(map.getCoconutScore()));
      }
    } else {
      // System.out.println("Coconut3-2");
      nut.hit();
      c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 1));
    }

  }

}
