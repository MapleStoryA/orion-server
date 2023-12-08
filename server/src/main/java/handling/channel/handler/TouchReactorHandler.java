package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import scripting.ReactorScriptManager;
import server.maps.MapleReactor;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class TouchReactorHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        final int oid = packet.readInt();
        final boolean touched = packet.readByte() > 0;
        final MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);
        if (!touched
                || reactor == null
                || !reactor.isAlive()
                || reactor.getReactorId() < 6109013
                || reactor.getReactorId() > 6109027) {
            return;
        }
        ReactorScriptManager.getInstance().act(c, reactor); // not sure how
        // touched boolean
        // comes into play

    }
}
