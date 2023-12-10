package scripting.v1.game;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import server.quest.MapleQuest;
import tools.ApiClass;

@Slf4j
public class QuestScripting extends PlayerScripting {

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
}
