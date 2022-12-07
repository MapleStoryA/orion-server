package scripting.v1.game;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.game.helper.ScriptingApi;
import server.quest.MapleQuest;

@Slf4j
public class QuestScripting extends PlayerScripting {

    private static final int COMPLETE_QUEST = 2;
    private static final int ACTIVE_QUEST = 1;
    private final MapleQuest quest;

    @ScriptingApi
    public QuestScripting(MapleClient client, MapleQuest quest) {
        super(client);
        this.quest = quest;
    }

    @ScriptingApi
    public MapleQuest getQuest() {
        return quest;
    }

    @ScriptingApi
    public final byte getQuestStatus(final int id) {
        return player.getQuestStatus(id);
    }

    @ScriptingApi
    public final boolean isQuestActive(final int id) {
        return getQuestStatus(id) == ACTIVE_QUEST;
    }

    @ScriptingApi
    public final boolean isQuestFinished(final int id) {
        return getQuestStatus(id) == COMPLETE_QUEST;
    }

}
