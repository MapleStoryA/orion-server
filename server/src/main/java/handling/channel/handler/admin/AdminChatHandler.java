package handling.channel.handler.admin;

import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;

/** @author kevintjuh93 */
@lombok.extern.slf4j.Slf4j
public class AdminChatHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(InPacket slea, MapleClient c) {
        if (!c.getPlayer().isGameMaster()) { // if ( (signed int)CWvsContext::GetAdminLevel((void *)v294) > 2
            // )
            return;
        }
        byte mode = slea.readByte();
        // not saving slides...
        byte[] packet = MaplePacketCreator.serverNotice(
                slea.readByte(), slea.readMapleAsciiString()); // maybe I should make a check for the
        // slea.readByte()... but I just hope gm's
        // don't fuck things up :)
        switch (mode) {
            case 0: // /alertall, /noticeall, /slideall
                c.getChannelServer().broadcastPacket(packet);
                break;
            case 1: // /alertch, /noticech, /slidech
                c.getChannelServer().broadcastPacket(packet);
                break;
            case 2: // /alertm /alertmap, /noticem /noticemap, /slidem /slidemap
                c.getPlayer().getMap().broadcastMessage(packet);
                break;
        }
    }
}
