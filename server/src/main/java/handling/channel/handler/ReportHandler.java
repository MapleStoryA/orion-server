package handling.channel.handler;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.packet.ReportPackets;

@Slf4j
public class ReportHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        packet.readByte();
        String victim = packet.readMapleAsciiString();
        packet.readByte();
        String description = packet.readMapleAsciiString();
        if (c.getPlayer().getPossibleReports() > 0) {
            if (c.getPlayer().getMeso() > 349) {
                c.getPlayer().gainMeso(349, true);
                c.getSession().write(ReportPackets.reportResponse((byte) 2, 1));
                c.getChannelServer()
                        .broadcastGMPacket(MaplePacketCreator.serverNotice(
                                6, c.getPlayer().getName() + " reported " + victim + " for: " + description));
            }
        } else {
            c.getPlayer().dropMessage(1, "You do not have any reports left.");
        }
    }
}
