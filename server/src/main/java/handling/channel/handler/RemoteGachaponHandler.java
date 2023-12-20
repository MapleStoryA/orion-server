package handling.channel.handler;

import client.MapleClient;
import networking.packet.AbstractMaplePacketHandler;
import scripting.NPCScriptManager;
import server.gachapon.GachaponLocation;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class RemoteGachaponHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        packet.readInt();
        byte city = packet.readByte();
        if (city < 0 && city > 8) {
            return;
        }
        int baseCity = 9100100 + city;
        if (baseCity == 9100108) {
            baseCity = GachaponLocation.NLC.getValue();
        }
        // TODO: Fix remote gachapon for showa town sauna
        if (baseCity == 9100106 || baseCity == 9100107) {
            baseCity = GachaponLocation.MUSHROM_SHRINE.getValue();
        }
        NPCScriptManager.getInstance().start(c, baseCity);
        c.enableActions();
    }
}
