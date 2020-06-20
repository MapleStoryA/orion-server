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
import constants.ServerEnvironment;
import server.MaplePortal;
import tools.FileoutputUtil;

import javax.script.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PortalScriptManager {

  private static final PortalScriptManager instance = new PortalScriptManager();
  private final Map<String, PortalScript> scripts = new HashMap<String, PortalScript>();
  private final static ScriptEngineFactory sef = new ScriptEngineManager().getEngineByName("javascript").getFactory();

  public final static PortalScriptManager getInstance() {
    return instance;
  }

  private final PortalScript getPortalScript(final String scriptName) {
    String path = "dist/scripts/portal/" + scriptName + ".js";

    if (!ServerEnvironment.isDebugEnabled()) {
      if (scripts.containsKey(scriptName)) {
        return scripts.get(scriptName);
      }
    } else {
      System.out.println("Loading script: " + path);
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
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error executing Portal script. (" + scriptName + ") " + e);
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
      System.out.println("Unhandled portal script " + portal.getScriptName() + " on map " + c.getPlayer().getMapId());
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Unhandled portal script " + portal.getScriptName() + " on map " + c.getPlayer().getMapId());
    }
  }

  public final void clearScripts() {
    scripts.clear();
  }
}
