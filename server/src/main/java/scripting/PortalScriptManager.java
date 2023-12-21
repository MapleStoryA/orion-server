package scripting;

import client.MapleClient;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import lombok.extern.slf4j.Slf4j;
import server.MaplePortal;
import server.base.config.ServerConfig;

@Slf4j
public class PortalScriptManager {

    private static final PortalScriptManager instance = new PortalScriptManager();
    private static final ScriptEngineFactory sef =
            new ScriptEngineManager().getEngineByName("javascript").getFactory();
    private final Map<String, PortalScript> scripts = new HashMap<String, PortalScript>();

    public static final PortalScriptManager getInstance() {
        return instance;
    }

    private PortalScript getPortalScript(final String scriptName) {
        String path = ServerConfig.serverConfig().getScriptsPath() + "/portal/" + scriptName + ".js";

        if (!ServerConfig.isDebugEnabled()) {
            if (scripts.containsKey(scriptName)) {
                return scripts.get(scriptName);
            }
        } else {
            log.info("Loading script: " + path);
        }

        final File scriptFile = new File(path);
        if (!scriptFile.exists()) {
            scripts.put(scriptName, null);
            return null;
        }

        FileReader fr = null;
        final ScriptEngine portal = sef.getScriptEngine();
        try {
            fr = new FileReader(scriptFile);
            CompiledScript compiled = ((Compilable) portal).compile(fr);
            compiled.eval();
        } catch (final Exception e) {
            System.err.println("Error executing Portalscript: " + scriptName + ":" + e);
            log.info("Log_Script_Except.rtf" + " : " + ("Error executing Portal script. (" + scriptName + ") " + e));
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (final IOException e) {
                    System.err.println("ERROR CLOSING" + e);
                }
            }
        }
        final PortalScript script = ((Invocable) portal).getInterface(PortalScript.class);
        scripts.put(scriptName, script);
        return script;
    }

    public final void executePortalScript(final MaplePortal portal, final MapleClient c) {
        final PortalScript script = getPortalScript(portal.getScriptName());

        if (script != null) {
            try {
                script.enter(new PortalPlayerInteraction(c, portal));
            } catch (Exception e) {
                System.err.println("Error entering Portalscript: " + portal.getScriptName() + ":" + e.getMessage());
            }
        } else {
            log.info("Unhandled portal script "
                    + portal.getScriptName()
                    + " on map "
                    + c.getPlayer().getMapId());
            final String msg = "Unhandled portal script "
                    + portal.getScriptName()
                    + " on map "
                    + c.getPlayer().getMapId();
            log.info("Log_Script_Except.rtf" + " : " + msg);
        }
    }

    public final void clearScripts() {
        scripts.clear();
    }
}
