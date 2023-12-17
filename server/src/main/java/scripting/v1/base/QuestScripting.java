package scripting.v1.base;

import client.MapleClient;
import client.MapleQuestStatus;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.api.QuestRecord;
import server.quest.MapleQuest;
import tools.ApiClass;

@Slf4j
public class QuestScripting extends PlayerScripting implements QuestRecord {

    private static final int COMPLETE_QUEST = 2;
    private static final int ACTIVE_QUEST = 1;
    private final MapleQuest quest;

    @ApiClass
    public QuestScripting(MapleClient client, MapleQuest quest) {
        super(client);
        this.quest = quest;
    }

    @ApiClass
    public MapleQuest getQuest() {
        return quest;
    }

    @ApiClass
    public final byte getQuestStatus(final int id) {
        return player.getQuestStatus(id);
    }

    @ApiClass
    public final boolean isQuestActive(final int id) {
        return getQuestStatus(id) == ACTIVE_QUEST;
    }

    @ApiClass
    public final boolean isQuestFinished(final int id) {
        return getQuestStatus(id) == COMPLETE_QUEST;
    }

    @Override
    public void set(int key, String value) {
        MapleQuestStatus quest = player.getQuest(MapleQuest.getInstance(key));
        quest.setCustomData(value);
    }

    @Override
    public void setComplete(int key) {
        MapleQuest quest = MapleQuest.getInstance(key);
        quest.forceComplete(player);
    }

    @Override
    public void setState(int key, byte state) {
        MapleQuestStatus quest = player.getQuest(MapleQuest.getInstance(key));
        quest.setStatus(state);
    }

    @Override
    public String get(int key) {
        MapleQuestStatus quest = player.getQuest(MapleQuest.getInstance(key));
        return quest.getCustomData();
    }

    @Override
    public int getState(int key) {
        return getQuestStatus(key);
    }

    @Override
    public int canComplete(int key) {
        MapleQuest quest = MapleQuest.getInstance(key);
        if (quest.canComplete(player, null)) {
            return 1;
        }
        return 0;
    }

    @Override
    public void remove(int key) {
        MapleQuest quest = MapleQuest.getInstance(key);
        quest.forfeit(player);
    }

    @Override
    public void selectedMob(int questId, int mobId, int locationType, int encounterLocation) {

    }
}
