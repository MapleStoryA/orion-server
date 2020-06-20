package maplebr2.scripting;

import client.MapleClient;
import org.junit.Test;
import org.mozilla.javascript.ContinuationPending;
import scripting.v1.NpcScriptingManager;
import scripting.v1.NpcScriptingManagerSingleton;

public class ScriptingNpcTests {

  @Test
  public void testSay() {
    NpcScriptingManager manager = NpcScriptingManagerSingleton.getInstance();
    MapleClient client = new MapleClient(null, null, null);
    try {
      manager.runScript(1, client);
    } catch (ContinuationPending pending) {
      client.getNpcScript().setContinuation(pending.getContinuation());
      client.getNpcScript().resume(1);
    }
  }


}
