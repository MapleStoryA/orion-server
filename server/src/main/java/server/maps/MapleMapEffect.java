package server.maps;

import client.MapleClient;
import tools.MaplePacketCreator;
import tools.packet.MTSCSPacket;

@lombok.extern.slf4j.Slf4j
public class MapleMapEffect {

    private String msg = "";
    private int itemId = 0;
    private boolean active = true;
    private boolean jukebox = false;

    public MapleMapEffect(String msg, int itemId) {
        this.msg = msg;
        this.itemId = itemId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isJukebox() {
        return this.jukebox;
    }

    public void setJukebox(boolean actie) {
        this.jukebox = actie;
    }

    public byte[] makeDestroyData() { // jukebox doesn't REALLY have a destroy, but 0 stops all music
        return jukebox ? MTSCSPacket.playCashSong(0, "") : MaplePacketCreator.removeMapEffect();
    }

    public byte[] makeStartData() {
        return jukebox ? MTSCSPacket.playCashSong(itemId, msg) : MaplePacketCreator.startMapEffect(msg, itemId, active);
    }

    public void sendStartData(MapleClient c) {
        c.getSession().write(makeStartData());
    }
}
