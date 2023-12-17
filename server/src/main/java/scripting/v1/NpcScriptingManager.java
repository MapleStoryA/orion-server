package scripting.v1;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import scripting.v1.base.FieldScripting;
import scripting.v1.base.InventoryScripting;
import scripting.v1.base.NpcScripting;
import scripting.v1.base.QuestScripting;
import scripting.v1.base.TargetScripting;
import server.config.ServerConfig;
import server.quest.MapleQuest;
import tools.StringUtil;

@Slf4j
public class NpcScriptingManager {

    private final String scriptPath;

    private static NpcScriptingManager INSTANCE;

    private NpcScriptingManager(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public static synchronized NpcScriptingManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NpcScriptingManager(ServerConfig.serverConfig().getScriptsPath() + "/");
        }
        return INSTANCE;
    }

    public boolean runQuestScript(int npc, int quest, MapleClient client) {
        Context ctx = setUpContext();
        NpcScripting npcScript;
        TargetScripting target;
        QuestScripting questScript;
        InventoryScripting inventory;
        try {
            String file = "function main() {"
                    + StringUtil.readFileAsString(scriptPath + "/questNew/" + quest + ".js")
                    + "}"
                    + "main();";
            file = file.replace("import './global';", "");

            Script script = ctx.compileString(file, "questNew/" + quest + ".js", 1, null);
            Scriptable globalScope = ctx.initStandardObjects();
            npcScript = new NpcScripting(npc, client, globalScope);
            target = new TargetScripting(client);
            inventory = new InventoryScripting(client);
            MapleQuest questObj = MapleQuest.getInstance(quest);
            questScript = new QuestScripting(client, questObj);
            globalScope.put("npc", globalScope, Context.javaToJS(npc, globalScope));
            globalScope.put("self", globalScope, Context.javaToJS(npcScript, globalScope));
            globalScope.put("target", globalScope, Context.javaToJS(target, globalScope));
            globalScope.put("quest", globalScope, Context.javaToJS(questScript, globalScope));
            globalScope.put("inventory", globalScope, Context.javaToJS(inventory, globalScope));

            client.setCurrentNpcScript(npcScript);
            ctx.executeScriptWithContinuations(script, globalScope);
            return true;
        } finally {
            Context.exit();
        }
    }

    public boolean runScript(int npc, String scriptName, MapleClient client) {
        Context ctx = setUpContext();
        NpcScripting npcScript;
        TargetScripting target;
        InventoryScripting inventory;
        FieldScripting field;
        try {

            String file = readScriptFile(npc, scriptName);

            log.info("Loading script {}", scriptName);
            Script script = ctx.compileString(file, file, 1, null);

            Scriptable globalScope = ctx.initStandardObjects();
            npcScript = new NpcScripting(npc, client, globalScope);
            target = new TargetScripting(client);
            inventory = new InventoryScripting(client);
            field = client.getPlayer().getMap().getField();
            globalScope.put("self", globalScope, Context.javaToJS(npcScript, globalScope));
            globalScope.put("target", globalScope, Context.javaToJS(target, globalScope));
            globalScope.put("inventory", globalScope, Context.javaToJS(inventory, globalScope));
            globalScope.put("field", globalScope, Context.javaToJS(field, globalScope));
            client.setCurrentNpcScript(npcScript);
            ctx.executeScriptWithContinuations(script, globalScope);
            return true;
        } catch (ContinuationPending ex) {
            throw ex;
        } catch (Exception ex) {
            log.info(ex.getMessage());
        } finally {
            Context.exit();
        }
        return false;
    }

    private String readScriptFile(int npc, String scriptName) {
        String file = "";
        if (scriptName == null) {
            file = "function main() {"
                    + StringUtil.readFileAsString(scriptPath + "/npcNew/" + npc + ".js")
                    + "}"
                    + "main();";
        } else {
            file = StringUtil.readFileAsString(scriptPath + "/npcNew/" + scriptName + ".js");
        }
        if (file.isEmpty()) {
            file = StringUtil.readFileAsString(scriptPath + "/npcNew/" + scriptName + ".ts");
        }
        return file.replace("import './global';", "");
    }

    private Context setUpContext() {
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        ctx.setLanguageVersion(Context.VERSION_ES6);
        ctx.getWrapFactory().setJavaPrimitiveWrap(false);
        return ctx;
    }
}
