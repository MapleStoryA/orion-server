package client;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import server.life.MapleLifeFactory;
import server.quest.MapleQuest;

@lombok.extern.slf4j.Slf4j
public class MapleQuestStatus implements Serializable {

    private static final long serialVersionUID = 91795419934134L;
    private final transient MapleQuest quest;
    private byte status;
    private Map<Integer, Integer> killedMobs = null;
    private int npc;
    private long completionTime;
    private int forfeited = 0;
    private String customData;

    /** Creates a new instance of MapleQuestStatus */
    public MapleQuestStatus(final MapleQuest quest, final byte status) {
        this.quest = quest;
        this.setStatus(status);
        this.completionTime = System.currentTimeMillis();
        if (status == 1) { // Started
            if (!quest.getRelevantMobs().isEmpty()) {
                registerMobs();
            }
        }
    }

    public MapleQuestStatus(final MapleQuest quest, final byte status, final int npc) {
        this.quest = quest;
        this.setStatus(status);
        this.setNpc(npc);
        this.completionTime = System.currentTimeMillis();
        if (status == 1) { // Started
            if (!quest.getRelevantMobs().isEmpty()) {
                registerMobs();
            }
        }
    }

    public final MapleQuest getQuest() {
        return quest;
    }

    public final byte getStatus() {
        return status;
    }

    public final void setStatus(final byte status) {
        this.status = status;
    }

    public final int getNpc() {
        return npc;
    }

    public final void setNpc(final int npc) {
        this.npc = npc;
    }

    private final void registerMobs() {
        killedMobs = new LinkedHashMap<Integer, Integer>();
        for (final int i : quest.getRelevantMobs().keySet()) {
            killedMobs.put(i, 0);
        }
    }

    private final int maxMob(final int mobid) {
        for (final Map.Entry<Integer, Integer> qs : quest.getRelevantMobs().entrySet()) {
            if (qs.getKey() == mobid) {
                return qs.getValue();
            }
        }
        return 0;
    }

    public final boolean mobKilled(final int id, final int skillID) {
        if (quest != null && quest.getSkillID() > 0) {
            if (quest.getSkillID() != skillID) {
                return false;
            }
        }
        final Integer mob = killedMobs.get(id);
        if (mob != null) {
            final int mo = maxMob(id);
            if (mob >= mo) {
                return false; // nothing happened
            }
            killedMobs.put(id, Math.min(mob + 1, mo));
            return true;
        }
        for (Entry<Integer, Integer> mo : killedMobs.entrySet()) {
            if (questCount(mo.getKey(), id)) {
                final int mobb = maxMob(mo.getKey());
                if (mo.getValue() >= mobb) {
                    return false; // nothing
                }
                killedMobs.put(mo.getKey(), Math.min(mo.getValue() + 1, mobb));
                return true;
            }
        } // i doubt this
        return false;
    }

    private final boolean questCount(final int mo, final int id) {
        if (MapleLifeFactory.getQuestCount(mo) != null) {
            for (int i : MapleLifeFactory.getQuestCount(mo)) {
                if (i == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public final void setMobKills(final int id, final int count) {
        if (killedMobs == null) {
            registerMobs(); // lol
        }
        killedMobs.put(id, count);
    }

    public final boolean hasMobKills() {
        if (killedMobs == null) {
            return false;
        }
        return killedMobs.size() > 0;
    }

    public final int getMobKills(final int id) {
        final Integer mob = killedMobs.get(id);
        if (mob == null) {
            return 0;
        }
        return mob;
    }

    public final Map<Integer, Integer> getMobKills() {
        return killedMobs;
    }

    public final long getCompletionTime() {
        return completionTime;
    }

    public final void setCompletionTime(final long completionTime) {
        this.completionTime = completionTime;
    }

    public final int getForfeited() {
        return forfeited;
    }

    public final void setForfeited(final int forfeited) {
        if (forfeited >= this.forfeited) {
            this.forfeited = forfeited;
        } else {
            throw new IllegalArgumentException(
                    "Can't set forfeits to something lower than before.");
        }
    }

    public final String getCustomData() {
        return customData;
    }

    public final void setCustomData(final String customData) {
        this.customData = customData;
    }
}
