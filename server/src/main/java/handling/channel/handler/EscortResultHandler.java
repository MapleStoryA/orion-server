package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.FileOutputUtil;
import tools.data.input.SeekableLittleEndianAccessor;

@lombok.extern.slf4j.Slf4j
public class EscortResultHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        FileOutputUtil.logPacket("", "[ESCORT_RESULT] " + slea.toString());

    }

}
