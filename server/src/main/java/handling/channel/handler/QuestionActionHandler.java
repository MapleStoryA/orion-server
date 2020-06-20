package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import scripting.NPCScriptManager;
import scripting.v1.NewNpcTalkHandler;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class QuestionActionHandler extends AbstractMaplePacketHandler {

  public static final int RESTORE_LOST_ITEM = 0;
  public static final int START_QUEST = 1;
  public static final int COMPLETE_QUEST = 2;
  public static final int FOREFIT_QUEST = 3;
  public static final int START_SCRIPTED_QUEST = 4;
  public static final int END_SCRIPTED_QUEST = 5;

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final byte action = slea.readByte();
    int quest = slea.readShort();
    if (quest < 0) { //questid 50000 and above, WILL cast to negative, this was tested.
      quest += 65536; //probably not the best fix, but whatever
    }
    if (chr == null) {
      return;
    }
    if (quest <= 29909 && quest >= 29906) {
      return;
    }
    final MapleQuest q = MapleQuest.getInstance(quest);
    switch (action) {
      case RESTORE_LOST_ITEM: {
        chr.updateTick(slea.readInt());
        final int itemid = slea.readInt();
        MapleQuest.getInstance(quest).RestoreLostItem(chr, itemid);
        break;
      }
      case START_QUEST: {
        final int npc = slea.readInt();
        q.start(chr, npc);
        break;
      }
      case COMPLETE_QUEST: {
        final int npc = slea.readInt();
        int tick = slea.readInt();
        if(q.getId() >= 1009 && q.getId() <= 1015){ // Maple quiz in map 1000000
          tick = tick + q.getId();
        }
        chr.updateTick(tick);

        if (slea.available() >= 4) {
          q.complete(chr, npc, slea.readInt());
        } else {
          q.complete(chr, npc);
        }
        break;
      }
      case FOREFIT_QUEST: {
        if (GameConstants.canForfeit(q.getId())) {
          q.forfeit(chr);
        } else {
          chr.dropMessage(1, "You may not forfeit this quest.");
        }
        break;
      }
      case START_SCRIPTED_QUEST: { // Scripted Start Quest
        final int npc = slea.readInt();
        if (NewNpcTalkHandler.isNewQuestScriptAvailable(quest)) {
          NewNpcTalkHandler.startQuestConversation(npc, quest, c);
          break;
        }
        c.setNpcScript(null);
        NPCScriptManager.getInstance().startQuest(c, npc, quest);
        break;
      }
      case END_SCRIPTED_QUEST: {
        final int npc = slea.readInt();
        NPCScriptManager.getInstance().endQuest(c, npc, quest, false);
        c.getSession().write(MaplePacketCreator.showSpecialEffect(9)); // Quest completion
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showSpecialEffect(chr.getId(), 9), false);
        break;
      }
    }

  }

}
