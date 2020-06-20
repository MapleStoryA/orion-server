package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.ReportPackets;

public class ReportHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    slea.readByte();
    String victim = slea.readMapleAsciiString();
    slea.readByte();
    String description = slea.readMapleAsciiString();
    if (c.getPlayer().getPossibleReports() > 0) {
      if (c.getPlayer().getMeso() > 349) {
        c.getPlayer().gainMeso(349, true);
        c.getSession().write(ReportPackets.reportResponse((byte) 2, 1));
        c.getChannelServer().broadcastGMPacket(MaplePacketCreator.serverNotice(6,
            c.getPlayer().getName() + " reported " + victim + " for: " + description));
      }
    } else {
      c.getPlayer().dropMessage(1, "You do not have any reports left.");
    }
  }

}
