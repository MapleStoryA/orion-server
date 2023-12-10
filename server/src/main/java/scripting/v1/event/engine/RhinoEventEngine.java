package scripting.v1.event.engine;

import handling.world.party.MapleParty;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripting.v1.game.TargetScripting;
import server.config.ServerConfig;
import server.life.MapleMonster;
import server.maps.MapleMap;

@lombok.extern.slf4j.Slf4j
public class RhinoEventEngine implements EventEngine {

    private static final Logger LOG = LoggerFactory.getLogger(RhinoEventEngine.class);

    private static final String SCRIPT_PATH = ServerConfig.serverConfig().getScriptsPath() + "/" + "instances";
    private final String name;
    private Scriptable globalScope;

    public RhinoEventEngine(String name) {
        this.name = name;
    }

    @Override
    public void loadScript() {
        String file = getInstancePath();
        LOG.info("Loading instance: {}", file);
        try {
            Context context = Context.enter();
            FileReader reader = new FileReader(file);
            globalScope = context.initStandardObjects();
            context.setLanguageVersion(Context.VERSION_ES6);
            context.setOptimizationLevel(-1);
            context.evaluateReader(globalScope, reader, name, -1, null);
            reader.close();
        } catch (IOException e) {
            LOG.debug("Error loading instance: {}", file);
        } catch (EvaluatorException e) {
            log.info("Error at line: " + e.lineSource() + " " + e.lineNumber());

        } finally {
            Context.exit();
        }
    }

    @Override
    public void onEventStart() {
        LOG.info("Calling onEventStart of instance {}", name);
        RhinoEngineUtils.invokeMethod("onEventStart", globalScope);
    }

    @Override
    public void onEventFinish() {
        RhinoEngineUtils.invokeMethod("onEventFinish", globalScope);
    }

    @Override
    public void onPlayerDisconnected(TargetScripting player) {
        RhinoEngineUtils.invokeMethod("onPlayerDisconnected", globalScope, player);
    }

    @Override
    public void onPlayerJoin(TargetScripting player) {
        RhinoEngineUtils.invokeMethod("onPlayerJoin", globalScope, player);
    }

    @Override
    public void onPlayerLeave(TargetScripting player) {
        RhinoEngineUtils.invokeMethod("onPlayerLeave", globalScope, player);
    }

    @Override
    public void addToContext(String key, Object obj) {
        Context.enter();
        try {
            globalScope.put(key, globalScope, Context.javaToJS(obj, globalScope));
        } finally {
            Context.exit();
        }
    }

    @Override
    public void invokeAction(String method) {
        RhinoEngineUtils.invokeMethod(method, globalScope);
    }

    private String getInstancePath() {
        return SCRIPT_PATH + File.separator + name + ".js";
    }

    @Override
    public void onMobKilled(TargetScripting killer, MapleMonster mob) {
        RhinoEngineUtils.invokeMethod("onMobKilled", globalScope, killer, mob);
    }

    @Override
    public void onPlayerDied(TargetScripting player) {
        RhinoEngineUtils.invokeMethod("onPlayerDied", globalScope);
    }

    @Override
    public String getProperty(String key) {
        try {
            Context.enter();
            return String.valueOf(globalScope.get(key, globalScope));
        } finally {
            Context.exit();
        }
    }

    @Override
    public void onPartyDisband(MapleParty party) {}

    @Override
    public void removeFromContext(String key) {
        Context.enter();
        try {
            globalScope.delete(key);
        } finally {
            Context.exit();
        }
    }

    @Override
    public void onPlayerExitMap(TargetScripting mapleCharacter, MapleMap map) {
        RhinoEngineUtils.invokeMethod("onPlayerExitMap", globalScope, mapleCharacter, map);
    }
}
