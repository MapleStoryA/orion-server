package scripting.v1;

import client.MapleClient;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import scripting.v1.binding.FieldScript;
import scripting.v1.binding.InventoryScript;
import scripting.v1.binding.NpcScript;
import scripting.v1.binding.QuestScript;
import scripting.v1.binding.TargetScript;
import scripting.v1.dispatch.PacketDispatcher;
import server.quest.MapleQuest;
import tools.StringUtil;

public class NpcScriptingManager {

    private final String scriptPath;
    private PacketDispatcher dispatcher;


    public NpcScriptingManager(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public void setDispatcher(PacketDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public boolean runQuestScript(int npc, int quest, MapleClient client) {
        Context ctx = setUpContext();
        NpcScript npcScript = null;
        TargetScript target = null;
        QuestScript questScript = null;
        InventoryScript inventory = null;
        try {
            String file = "function main() {" +
                    StringUtil.readFileAsString(scriptPath + "/questNew/" + quest + ".js") +
                    "}" +
                    "main();";


            Script script = ctx.compileString(file, "questNew/" + quest + ".js", 1, null);
            Scriptable globalScope = ctx.initStandardObjects();
            npcScript = new NpcScript(npc, client, globalScope, dispatcher);
            target = new TargetScript(client, dispatcher);
            inventory = new InventoryScript(client, dispatcher);
            MapleQuest questObj = MapleQuest.getInstance(quest);
            questScript = new QuestScript(client, questObj, dispatcher);
            globalScope.put("npc", globalScope, Context.javaToJS(npc, globalScope));
            globalScope.put("self", globalScope, Context.javaToJS(npcScript, globalScope));
            globalScope.put("target", globalScope, Context.javaToJS(target, globalScope));
            globalScope.put("quest", globalScope, Context.javaToJS(questScript, globalScope));
            globalScope.put("inventory", globalScope, Context.javaToJS(inventory, globalScope));

            client.setNpcScript(npcScript);
            ctx.executeScriptWithContinuations(script, globalScope);
            return true;
        } finally {
            Context.exit();
        }
    }

    public boolean runScript(int npc, MapleClient client) {
        Context ctx = setUpContext();
        NpcScript npcScript = null;
        TargetScript target = null;
        InventoryScript inventory = null;
        FieldScript field = null;
        try {

            String file = "function main() {" +
                    StringUtil.readFileAsString(scriptPath + "/npcNew/" + npc + ".js") +
                    "}" +
                    "main();";

            Script script = ctx.compileString(file, "npcNew/" + npc + ".js", 1, null);

            Scriptable globalScope = ctx.initStandardObjects();
            npcScript = new NpcScript(npc, client, globalScope, dispatcher);
            target = new TargetScript(client, dispatcher);
            inventory = new InventoryScript(client, dispatcher);
            field = client.getPlayer().getMap().getField();
            globalScope.put("self", globalScope, Context.javaToJS(npcScript, globalScope));
            globalScope.put("target", globalScope, Context.javaToJS(target, globalScope));
            globalScope.put("inventory", globalScope, Context.javaToJS(inventory, globalScope));
            globalScope.put("field", globalScope, Context.javaToJS(field, globalScope));
            client.setNpcScript(npcScript);
            ctx.executeScriptWithContinuations(script, globalScope);
            return true;
        } catch (ContinuationPending ex) {
            throw ex;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            Context.exit();
        }
        return false;
    }

    private Context setUpContext() {
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        ctx.setLanguageVersion(Context.VERSION_ES6);
        ctx.getWrapFactory().setJavaPrimitiveWrap(false);
        return ctx;
    }

}
