package scripting;

import client.MapleClient;
import server.config.ServerEnvironment;
import tools.StringUtil;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author Matze
 */
public abstract class AbstractScriptManager {

    protected ScriptEngine engine;
    private final ScriptEngineManager sem;

    private static boolean isDebugMode;

    protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractScriptManager.class);

    protected AbstractScriptManager() {
        isDebugMode = ServerEnvironment.isDebugEnabled();
        sem = new ScriptEngineManager();
    }

    protected Invocable getInvocable(String path, String scriptId, MapleClient c) {
        try {
            path = "dist/scripts/" + path + "/" + scriptId + ".js";

            if (isDebugMode) {
                System.out.println("Loading file " + path);
            }

            engine = sem.getEngineByName("nashorn");
            if (c != null) {
                c.setScriptEngine(path, engine);
            }
            StringBuilder builder = new StringBuilder();
            builder.append("load('nashorn:mozilla_compat.js');" + System.lineSeparator());
            builder.append("function scriptName(){ return \"$1\"; }".replace("$1", scriptId));
            builder.append("function getScriptPath(){ return \"dist/scripts\"}");
            String content = StringUtil.readFileAsString(path);
            if (content.isEmpty()) {
                return null;
            } else {
                builder.append(content);
            }

            engine.eval(builder.toString());
            return (Invocable) engine;
        } catch (ScriptException e) {
            log.error("Error executing script " + path, e);
            System.out.println("Error executing script " + path + e.getMessage() + " line: " + e.getLineNumber()
                    + " column: " + e.getColumnNumber());
            return null;
        } catch (Exception ex) {
            log.error("Error executing script " + path, ex);
            System.out.println("Error executing script " + path);
            return null;
        }

    }

    protected void resetContext(String path, MapleClient c) {
        path = "dist/scripts/" + path;
        c.removeScriptEngine(path);
    }

}