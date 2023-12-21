package server.maps;

import client.MapleCharacter;
import handling.world.helper.BroadcastHelper;
import java.awt.*;
import server.MapleItemInformationProvider;
import server.Timer.EventTimer;
import server.config.ServerConfig;
import tools.MaplePacketCreator;
import tools.helper.Randomizer;

@lombok.extern.slf4j.Slf4j
public class AramiaFireWorks {

    public static final int MAX_SUN = 2500;
    private static final int MAPLE_LEAF = 4001126;
    private static final int[] REWARDS = {
        2022176, 1092030, 2022175, 2000002, 2000002, 2002001, 2002002, 2002003, 2002004, 2002005, 2050004, 1012098,
        1012101, 1012102, 1012103
    };
    private static final int[] array_X = {720, 180, 630, 270, 360, 540, 450, 142, 142, 218, 772, 810, 848, 232, 308, 142
    };
    private static final int[] array_Y = {
        1234, 1234, 1174, 1234, 1174, 1174, 1174, 1260, 1234, 1234, 1234, 1234, 1234, 1114, 1114, 1140
    };
    private short sunshines = 0;

    private final void broadcastServer(final MapleCharacter c, final int itemid) {
        BroadcastHelper.broadcastMessage(MaplePacketCreator.serverNotice(
                6,
                itemid,
                "<Channel "
                        + c.getClient().getChannel()
                        + "> "
                        + c.getMap().getMapName()
                        + " : The amount of {"
                        + MapleItemInformationProvider.getInstance().getName(itemid)
                        + "} has reached the limit!"));
    }

    public final void giveSuns(final MapleCharacter c, final int kegs) {
        this.sunshines += kegs;
        // have to broadcast a Reactor?
        final MapleMap map = c.getClient().getChannelServer().getMapFactory().getMap(970010000);
        final MapleReactor reactor = map.getReactorByName("mapleTree");

        if (kegs < 1) {
            return;
        }
        int currentState = (sunshines / 500);
        if (ServerConfig.isDebugEnabled()) {
            log.info("Current tree state: " + currentState);
        }
        while (reactor.getState() < currentState) {
            reactor.setState((byte) (reactor.getState() + 1));
            reactor.setTimerActive(false);
            map.broadcastMessage(MaplePacketCreator.triggerReactor(reactor, reactor.getState()));
        }

        if (this.sunshines >= MAX_SUN) {
            this.sunshines = 0;
            broadcastSun(c);
            map.resetReactors(); // back to state 0
        }
    }

    public final short getSunsPercentage() {
        return sunshines;
    }

    public final short getSunshine() {
        return sunshines;
    }

    private final void broadcastSun(final MapleCharacter c) {
        broadcastServer(c, MAPLE_LEAF);
        // Henesys Park
        EventTimer.getInstance()
                .schedule(
                        new Runnable() {

                            @Override
                            public void run() {
                                startSun(c.getClient()
                                        .getChannelServer()
                                        .getMapFactory()
                                        .getMap(970010000));
                            }
                        },
                        10000);
    }

    private final void startSun(final MapleMap map) {
        if (map == null) {
            return;
        }
        map.startMapEffect("The tree is bursting with sunshine!", 5120008);
        for (int i = 0; i < 3; i++) {
            EventTimer.getInstance()
                    .schedule(
                            new Runnable() {

                                @Override
                                public void run() {
                                    spawnItem(map);
                                }
                            },
                            1000 + (i * 10000));
        }
    }

    private final void spawnItem(final MapleMap map) {
        Point pos;
        for (int i = 0; i < array_X.length; i++) {
            pos = new Point(array_X[i], array_Y[i]);
            int rand = Randomizer.nextInt((REWARDS.length - 1 + 10));
            if (rand > REWARDS.length - 1) {
                return;
            }

            int materialid = REWARDS[rand];

            // ItemID calculation end
            map.spawnAutoDrop(materialid, pos); // drop the material
        }
    }
}
