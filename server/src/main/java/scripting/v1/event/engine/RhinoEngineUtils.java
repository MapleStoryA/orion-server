package scripting.v1.event.engine;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public final class RhinoEngineUtils {

  public static void invokeMethod(String name, Scriptable globalScope, Object... args) {
    try {
      Context context = Context.enter();
      Function f1 = (Function) globalScope.get(name, globalScope);
      f1.call(context, globalScope, globalScope, args);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    } finally {
      Context.exit();
    }
  }
}
