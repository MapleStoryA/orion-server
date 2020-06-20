package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import scripting.EventManager;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class RequestBoatStatusHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient client) {
    int mapId = slea.readInt();

    if (client.getPlayer().getMap().getId() == mapId) {

      EventManager manager;
      if (mapId == 260000100 || mapId == 200000151) {// ariant
        manager = client.getChannelServer().getEventSM().getEventManager("Geenie");
      } else {
        manager = client.getChannelServer().getEventSM().getEventManager("Boats");
      }

      String docked = manager.getProperty("docked");
      if (docked != null && Boolean.valueOf(docked) == true) {
        client.getSession().write(MaplePacketCreator.boatPacket(0));
      } else {
        client.getSession().write(MaplePacketCreator.boatPacket(2));
      }

    }


  }

}
