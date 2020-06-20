package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.ClientStorage;
import server.maps.FieldLimitType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeChannelHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null) {
      System.out.println("Cannot change channel of null character");
      c.getSession().close();
    }
    if (!chr.isAlive() || chr.getEventInstance() != null || chr.getMap() == null || FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit())) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    chr.changeChannel(slea.readByte() + 1);
    ClientStorage.addClient(c);

  }

}
