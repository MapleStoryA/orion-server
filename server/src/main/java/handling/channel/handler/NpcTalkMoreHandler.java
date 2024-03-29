package handling.channel.handler;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import scripting.NPCConversationManager;
import scripting.NPCScriptManager;
import scripting.v1.NpcTalkHelper;

@Slf4j
public class NpcTalkMoreHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        if (c.getLastNPCTalk() > System.currentTimeMillis() - 400) {
            return;
        }
        if (c.getCurrentNpcScript() != null && c.getCurrentNpcScript().getContinuation() != null) {
            NpcTalkHelper.proceedConversation(packet, c);
            return;
        }
        final byte lastMsg = packet.readByte(); // 00 (last msg type I think) 0F = dimensional mirror, 06 =
        // quiz, 07 = speed quiz
        byte action = 0;
        if (lastMsg != 7) {
            action = packet.readByte(); // 00 = end chat, 01 == follow
        }

        final NPCConversationManager cm = NPCScriptManager.getInstance().getCM(c);

        if (cm == null || c.getPlayer().getConversation() == 0 || cm.getLastMsg() != lastMsg) {
            return;
        }
        cm.setLastMsg((byte) -1);
        if (lastMsg == 3 || lastMsg == 6) {
            if (action != 0) {
                cm.setGetText(packet.readMapleAsciiString());
                if (cm.getType() == 0) {
                    NPCScriptManager.getInstance().startQuest(c, action, lastMsg, -1);
                } else if (cm.getType() == 1) {
                    NPCScriptManager.getInstance().endQuest(c, action, lastMsg, -1);
                } else {
                    NPCScriptManager.getInstance().action(c, action, lastMsg, -1);
                }
            } else {
                cm.dispose();
            }
        } else if (lastMsg == 7) { // Speed Quiz
            if (c.getPlayer().getSpeedQuiz() == null) {
                cm.dispose();
                return;
            }
            c.getPlayer().getSpeedQuiz().nextRound(c, packet.readMapleAsciiString());
        } else {
            int selection = -1;
            if (packet.available() >= 4) {
                selection = packet.readInt();
            } else if (packet.available() > 0) {
                selection = packet.readByte();
            }
            if (lastMsg == 4 && selection <= -1) {
                cm.dispose();
                return; // h4x
            }
            if (selection >= -1 && action != -1) {
                if (cm.getType() == 0) {
                    NPCScriptManager.getInstance().startQuest(c, action, lastMsg, selection);
                } else if (cm.getType() == 1) {
                    NPCScriptManager.getInstance().endQuest(c, action, lastMsg, selection);
                } else {
                    NPCScriptManager.getInstance().action(c, action, lastMsg, selection);
                }
            } else {
                cm.dispose();
            }
        }
    }
}
