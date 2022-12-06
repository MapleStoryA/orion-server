package scripting.v1;

import scripting.v1.dispatch.RealPacketDispatcher;
import server.config.ServerEnvironment;

@lombok.extern.slf4j.Slf4j
public class NpcScriptingManagerSingleton {

    private static NpcScriptingManager instance;

    public static NpcScriptingManager getInstance() {
        if (instance == null) {
            instance = new NpcScriptingManager(ServerEnvironment.getConfig().getScriptsPath() + "/");
            instance.setDispatcher(new RealPacketDispatcher());

        }
        return instance;
    }
}
