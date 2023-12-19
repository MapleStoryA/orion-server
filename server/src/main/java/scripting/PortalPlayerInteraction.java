package scripting;

import client.MapleClient;
import server.MaplePortal;
import server.quest.MapleQuest;

@lombok.extern.slf4j.Slf4j
public class PortalPlayerInteraction extends AbstractPlayerInteraction {

    private final MaplePortal portal;

    public PortalPlayerInteraction(final MapleClient c, final MaplePortal portal) {
        super(c, portal.getId(), c.getPlayer().getMapId());
        this.portal = portal;
    }

    public final MaplePortal getPortal() {
        return portal;
    }

    public final void inFreeMarket() {
        if (getMapId() != 910000000) {
            saveLocation("FREE_MARKET");
            playPortalSE();
            warp(910000000, "out00");
        }
    }

    // summon one monster on reactor location
    @Override
    public void spawnMonster(int id) {
        spawnMonster(id, 1, portal.getPosition());
    }

    // summon monsters on reactor location
    @Override
    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, portal.getPosition());
    }

    public void completeQuest(int id, int npcId) {
        MapleQuest.getInstance(id).complete(getPlayer(), npcId);
    }
}
