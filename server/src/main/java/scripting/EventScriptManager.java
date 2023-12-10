package scripting;

import handling.channel.ChannelServer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventScriptManager extends AbstractScriptManager {

    private final Map<String, EventEntry> events = new LinkedHashMap<String, EventEntry>();
    private final AtomicInteger runningInstanceMapId = new AtomicInteger(0);

    public EventScriptManager(final ChannelServer channelServer, final List<String> scripts) {
        for (final String script : scripts) {
            if (!script.equals("")) {
                final Invocable iv = getInvocable("event", script, null);
                if (iv != null) {
                    events.put(
                            script,
                            new EventEntry(
                                    script, iv, new EventManager(channelServer, iv, script)));
                }
            }
        }
    }

    public final int getNewInstanceMapId() {
        return runningInstanceMapId.addAndGet(1);
    }

    public final EventManager getEventManager(final String event) {
        final EventEntry entry = events.get(event);
        if (entry == null) {
            return null;
        }
        return entry.em;
    }

    public final void init() {
        for (final EventEntry entry : events.values()) {
            try {
                ((ScriptEngine) entry.iv).put("em", entry.em);
                entry.iv.invokeFunction("init", (Object) null);
            } catch (final Exception ex) {
                log.info("Error initiating event: " + entry.script + ":" + ex);
                log.info(
                        "Log_Script_Except.rtf"
                                + " : "
                                + ("Error initiating event: " + entry.script + ":" + ex));
            }
        }
    }

    public final void cancel() {
        for (final EventEntry entry : events.values()) {
            entry.em.cancel();
        }
    }

    private static class EventEntry {

        public String script;
        public Invocable iv;
        public EventManager em;

        public EventEntry(final String script, final Invocable iv, final EventManager em) {
            this.script = script;
            this.iv = iv;
            this.em = em;
        }
    }
}
