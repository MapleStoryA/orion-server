package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class PlayerDisconectHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        c.getPlayer().saveToDB(true, false);//Prevent  player is logged in
    }

}
