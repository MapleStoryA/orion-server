package scripting.v1.event.engine;

import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

@Slf4j
public final class RhinoEngineUtils {

    public static void invokeMethod(String name, Scriptable globalScope, Object... args) {
        try {
            Context context = Context.enter();
            Function f1 = (Function) globalScope.get(name, globalScope);
            f1.call(context, globalScope, globalScope, args);
        } catch (Exception ex) {
            log.info(ex.getMessage());
        } finally {
            Context.exit();
        }
    }
}
