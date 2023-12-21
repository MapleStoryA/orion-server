package server.maps;

import client.MapleClient;
import java.awt.*;
import lombok.extern.slf4j.Slf4j;
import scripting.ReactorScriptManager;
import server.Timer.MapTimer;
import tools.MaplePacketCreator;
import tools.collection.Pair;

@Slf4j
public class MapleReactor extends AbstractMapleMapObject {

    private final int rid;
    private final MapleReactorStats stats;
    private byte state;
    private int delay;
    private MapleMap map;
    private String name = "";
    private boolean timerActive, alive;

    public MapleReactor(MapleReactorStats stats, int rid) {
        this.stats = stats;
        this.rid = rid;
        alive = true;
    }

    public final byte getFacingDirection() {
        return stats.getFacingDirection();
    }

    public boolean isTimerActive() {
        return timerActive;
    }

    public void setTimerActive(boolean active) {
        this.timerActive = active;
    }

    public int getReactorId() {
        return rid;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.REACTOR;
    }

    public int getReactorType() {
        return stats.getType(state);
    }

    public MapleMap getMap() {
        return map;
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public Pair<Integer, Integer> getReactItem() {
        return stats.getReactItem(state);
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.destroyReactor(this));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.spawnReactor(this));
    }

    public void forceStartReactor(MapleClient c) {
        ReactorScriptManager.getInstance().act(c, this);
    }

    public void forceHitReactor(final byte newState) {
        setState(newState);
        setTimerActive(false);
        map.broadcastMessage(MaplePacketCreator.triggerReactor(this, (short) 0));
    }

    // hitReactor command for item-triggered reactors
    public void hitReactor(MapleClient c) {
        hitReactor(0, (short) 0, c);
    }

    public void forceTrigger() {
        map.broadcastMessage(MaplePacketCreator.triggerReactor(this, (short) 0));
    }

    public void delayedDestroyReactor(long delay) {
        MapTimer.getInstance()
                .schedule(
                        new Runnable() {

                            @Override
                            public void run() {
                                map.destroyReactor(getObjectId());
                            }
                        },
                        delay);
    }

    public void hitReactor(int charPos, short stance, MapleClient c) {
        if (stats.getType(state) < 999 && stats.getType(state) != -1) {
            // type 2 = only hit from right (kerning swamp plants), 00 is air left 02 is ground left
            final byte oldState = state;
            if (!(stats.getType(state) == 2 && (charPos == 0 || charPos == 2))) { // next state
                state = stats.getNextState(state);

                if (stats.getNextState(state) == -1 || stats.getType(state) == 999) { // end of reactor
                    if ((stats.getType(state) < 100 || stats.getType(state) == 999) && delay > 0) { // reactor broken
                        map.destroyReactor(getObjectId());
                    } else { // item-triggered on final step
                        map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                    }
                    ReactorScriptManager.getInstance().act(c, this);
                } else { // reactor not broken yet
                    boolean done = false;
                    map.broadcastMessage(MaplePacketCreator.triggerReactor(
                            this, stance)); // magatia is weird cause full beaker can be activated
                    // by gm hat o.o
                    if (state == stats.getNextState(state)
                            || rid == 2618000
                            || rid == 2309000) { // current state = next state, looping reactor
                        ReactorScriptManager.getInstance().act(c, this);
                        done = true;
                    }
                    if (stats.getTimeOut(state) > 0) {
                        if (!done) {
                            ReactorScriptManager.getInstance().act(c, this);
                        }
                        scheduleSetState(state, oldState, stats.getTimeOut(state));
                    }
                }
            }
        }
    }

    public Rectangle getArea() {
        int height = stats.getBR().y - stats.getTL().y;
        int width = stats.getBR().x - stats.getTL().x;
        int origX = getPosition().x + stats.getTL().x;
        int origY = getPosition().y + stats.getTL().y;

        return new Rectangle(origX, origY, width, height);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Reactor "
                + getObjectId()
                + " of id "
                + rid
                + " at position "
                + getPosition().toString()
                + " state"
                + state
                + " type "
                + stats.getType(state);
    }

    public void delayedHitReactor(final MapleClient c, long delay) {
        MapTimer.getInstance()
                .schedule(
                        new Runnable() {

                            @Override
                            public void run() {
                                hitReactor(c);
                            }
                        },
                        delay);
    }

    public void scheduleSetState(final byte oldState, final byte newState, long delay) {
        MapTimer.getInstance()
                .schedule(
                        new Runnable() {

                            @Override
                            public void run() {
                                if (MapleReactor.this.state == oldState) {
                                    forceHitReactor(newState);
                                }
                            }
                        },
                        delay);
    }
}
