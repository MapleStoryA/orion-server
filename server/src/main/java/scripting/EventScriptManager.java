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

import handling.channel.ChannelServer;
import lombok.extern.slf4j.Slf4j;
import tools.FileOutputUtil;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class EventScriptManager extends AbstractScriptManager {

    private final Map<String, EventEntry> events = new LinkedHashMap<String, EventEntry>();
    private final AtomicInteger runningInstanceMapId = new AtomicInteger(0);

    public EventScriptManager(final ChannelServer channelServer, final String[] scripts) {
        for (final String script : scripts) {
            if (!script.equals("")) {
                final Invocable iv = getInvocable("event", script, null);
                if (iv != null) {
                    events.put(script, new EventEntry(script, iv, new EventManager(channelServer, iv, script)));
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
                FileOutputUtil.log(FileOutputUtil.ScriptEx_Log, "Error initiating event: " + entry.script + ":" + ex);
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
