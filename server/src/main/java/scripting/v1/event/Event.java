package scripting.v1.event;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.party.MapleParty;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import scripting.v1.base.FieldScripting;
import server.config.ServerConfig;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

@Slf4j
public class Event {
    private final String id;
    private final String name;
    private final GameEventManager gameEventManager;
    private final ChannelServer channelServer;
    private final ScheduledExecutorService executorService;
    private long startTime;
    private long eventTime;
    private Scriptable globalScope;
    private List<Integer> mapIds = new ArrayList<>();
    private MapleCharacter eventLeader;

    public Event(String id, String name, GameEventManager gameEventManager, ChannelServer channelServer) {
        this.id = id;
        this.name = name;
        this.gameEventManager = gameEventManager;
        this.channelServer = channelServer;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.startTime = 0;
        this.eventTime = 0;
    }

    public void init() {
        evaluateScript();
        invokeMethod("onEventInit", globalScope);
    }

    public void startEvent(MapleCharacter player, int[] mapIds, int timerInSeconds) {
        MapleParty party = player.getParty();
        int startMapId = mapIds[0];
        int endMapId = mapIds[mapIds.length - 1];
        MapleMap map = channelServer.getMapFactory().getMap(startMapId);
        eventLeader = player;
        Arrays.stream(mapIds).forEach(this.mapIds::add);
        for (var member : party.getMembers()) {
            MapleCharacter playerMember = channelServer.getPlayerStorage().getCharacterById(member.getId());
            if (playerMember != null) {
                playerMember.changeMap(startMapId, 0);
            }
        }
        startTime = System.currentTimeMillis();
        eventTime = timerInSeconds;
        invokeMethod("onEventStart", globalScope);
        map.broadcastMessage(MaplePacketCreator.getClock(timerInSeconds));
        executorService.schedule(
                () -> {
                    invokeMethod("onEventEnd", globalScope);
                    for (var member : party.getMembers()) {
                        MapleCharacter playerMember =
                                channelServer.getPlayerStorage().getCharacterById(member.getId());
                        if (playerMember != null) {
                            playerMember.changeMap(endMapId, 0);
                        }
                        playerMember.leaveEvent();
                    }
                    gameEventManager.onEventEnd(this);
                    executorService.shutdownNow();
                },
                timerInSeconds,
                TimeUnit.SECONDS);
    }

    private long getTimeLeft() {
        long currentTimestamp = System.currentTimeMillis();
        long endTime = startTime + eventTime * 1000;
        return (endTime - currentTimestamp) / 1000;
    }

    private void evaluateScript() {
        String file = getInstancePath();
        log.info("Loading instance: {}", file);
        try {
            Context context = Context.enter();
            FileReader reader = new FileReader(file);
            globalScope = context.initStandardObjects();
            context.setLanguageVersion(Context.VERSION_ES6);
            context.setOptimizationLevel(-1);
            context.evaluateReader(globalScope, reader, name, -1, null);
            reader.close();
        } catch (IOException e) {
            log.debug("Error loading instance: {}", file);
        } catch (EvaluatorException e) {
            log.info("Error at line: " + e.lineSource() + " " + e.lineNumber());
        } finally {
            Context.exit();
        }
        addObjectToContext("event", new EventScripting(this));
    }

    private void addObjectToContext(String key, Object obj) {
        Context.enter();
        try {
            globalScope.put(key, globalScope, Context.javaToJS(obj, globalScope));
        } finally {
            Context.exit();
        }
    }

    private static void invokeMethod(String name, Scriptable globalScope, Object... args) {
        try {
            Context context = Context.enter();
            Function f1 = (Function) globalScope.get(name, globalScope);
            f1.call(context, globalScope, globalScope, args);
        } catch (Exception ex) {
            log.info(ex.getMessage());
        } finally {
            Context.exit();
        }
    }

    private String getInstancePath() {
        String SCRIPT_PATH = ServerConfig.serverConfig().getScriptsPath() + "/" + "instances";
        return SCRIPT_PATH + File.separator + name + ".js";
    }

    protected String getName() {
        return this.name;
    }

    public void onChangeMap(MapleMap mapleMap, MapleCharacter chr) {
        if (mapIds.contains(mapleMap.getId())) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock((int) (getTimeLeft())));
        }
    }

    protected MapleCharacter getLeader() {
        return eventLeader;
    }

    protected FieldScripting getField(int mapId) {
        MapleMap map = channelServer.getMapFactory().getMap(mapId);
        return new FieldScripting(map);
    }
}
