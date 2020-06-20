package scripting.v1;

import scripting.v1.dispatch.RealPacketDispatcher;

public class NpcScriptingManagerSingleton {

  private static NpcScriptingManager instance;

  public static NpcScriptingManager getInstance() {
    if (instance == null) {
      instance = new NpcScriptingManager("dist/scripts");
      instance.setDispatcher(new RealPacketDispatcher());

    }
    return instance;
  }
}
