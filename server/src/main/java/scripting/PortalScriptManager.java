/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import server.MaplePortal;
import server.config.ServerEnvironment;

@lombok.extern.slf4j.Slf4j
public class PortalScriptManager {

    private static final PortalScriptManager instance = new PortalScriptManager();
    private static final ScriptEngineFactory sef =
            new ScriptEngineManager().getEngineByName("javascript").getFactory();
    private final Map<String, PortalScript> scripts = new HashMap<String, PortalScript>();

    public static final PortalScriptManager getInstance() {
        return instance;
    }

    private PortalScript getPortalScript(final String scriptName) {
        String path =
                ServerEnvironment.getConfig().getScriptsPath() + "/portal/" + scriptName + ".js";

        if (!ServerEnvironment.isDebugEnabled()) {
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
            log.info(
                    "Log_Script_Except.rtf"
                            + " : "
                            + ("Error executing Portal script. (" + scriptName + ") " + e));
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
                System.err.println(
                        "Error entering Portalscript: "
                                + portal.getScriptName()
                                + ":"
                                + e.getMessage());
            }
        } else {
            log.info(
                    "Unhandled portal script "
                            + portal.getScriptName()
                            + " on map "
                            + c.getPlayer().getMapId());
            final String msg =
                    "Unhandled portal script "
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
