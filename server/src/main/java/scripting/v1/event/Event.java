package scripting.v1.event;

import client.MapleCharacter;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import server.config.ServerConfig;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Event {
    private final String eventName;
    private final int channel;
    private final GameEventManager gameEventManager;
    private Scriptable globalScope;
    private ScheduledExecutorService executorService;

    private static final String SCRIPT_PATH = ServerConfig.serverConfig().getScriptsPath() + "/" + "instances";

    public Event(int channel, String eventName, GameEventManager gameEventManager) {
        this.channel = channel;
        this.eventName = eventName;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.gameEventManager = gameEventManager;
    }


    public void enter() {
        evaluateScript();
        invokeMethod("onEventStart", globalScope);
    }

    public void finish() {
        executorService.shutdownNow();
        gameEventManager.onEventFinish(eventName);
    }

    public void schedule(String method, long timeInSeconds) {
        executorService.schedule(() -> {
            invokeMethod(method, globalScope);
        }, timeInSeconds, TimeUnit.SECONDS);
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
            context.evaluateReader(globalScope, reader, eventName, -1, null);
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

    public void addObjectToContext(String key, Object obj) {
        Context.enter();
        try {
            globalScope.put(key, globalScope, Context.javaToJS(obj, globalScope));
        } finally {
            Context.exit();
        }
    }

    public String getEventName() {
        return eventName;
    }

    public int getChannel() {
        return channel;
    }

    public void onCharacterJoinParty(MapleCharacter player) {
        invokeMethod("onCharacterJoinParty", globalScope);
    }

    public void onCharacterLeaveParty(MapleCharacter player) {
        invokeMethod("onCharacterLeaveParty", globalScope);
    }

    public void onCharacterDie(MapleCharacter player) {
        invokeMethod("onCharacterDie", globalScope);
    }

    private String getInstancePath() {
        return SCRIPT_PATH + File.separator + eventName + ".ts";
    }

    public static void invokeMethod(String name, Scriptable globalScope, Object... args) {
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
}
