package scripting.v1.binding;

import client.MapleClient;
import scripting.v1.dispatch.PacketDispatcher;
import server.quest.MapleQuest;

public class QuestScript extends PlayerInteractionScript {

  private static final int COMPLETE_QUEST = 2;
  private static final int ACTIVE_QUEST = 1;
  private MapleQuest quest;

  public QuestScript(MapleClient client, MapleQuest quest, PacketDispatcher dispatcher) {
    super(client, dispatcher);
    this.quest = quest;
  }

  public MapleQuest getQuest() {
    return quest;
  }

  public final byte getQuestStatus(final int id) {
    return player.getQuestStatus(id);
  }

  public final boolean isQuestActive(final int id) {
    return getQuestStatus(id) == ACTIVE_QUEST;
  }

  public final boolean isQuestFinished(final int id) {
    return getQuestStatus(id) == COMPLETE_QUEST;
  }

}
