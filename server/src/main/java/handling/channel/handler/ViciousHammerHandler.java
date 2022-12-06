package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

@lombok.extern.slf4j.Slf4j
public class ViciousHammerHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(8);
        c.getSession().write(MTSCSPacket.ViciousHammer(false, (byte) 0));

    }

}
