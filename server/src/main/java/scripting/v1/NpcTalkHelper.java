package scripting.v1;

import client.MapleClient;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import org.mozilla.javascript.ContinuationPending;
import scripting.v1.base.AskAvatarHelper;
import scripting.v1.base.NpcScripting;
import server.base.config.ServerConfig;

@Slf4j
public class NpcTalkHelper {

    class ChatAction {
        public static final int ACTION_END_CHAT = -1;
        public static final int ACTION_BACK_OR_NO = 0;
        public static final int ACTION_NEXT = 1;
    }

    public static boolean isNewNpcScriptAvailable(int npc, String script) {
        log.info("Loading script npc id: {} script: {}", npc, script);
        if (script == null) {
            var file = new File(ServerConfig.serverConfig().getScriptsPath() + "/npcNew/" + npc + ".js");
            return file.exists();
        }
        var file = new File(ServerConfig.serverConfig().getScriptsPath() + "/npcNew/" + script + ".js");
        if (!file.exists()) {
            file = new File(ServerConfig.serverConfig().getScriptsPath() + "/npcNew/" + script + ".ts");
        }
        return file.exists();
    }

    public static boolean isNewQuestScriptAvailable(int npc) {
        var file = new File(ServerConfig.serverConfig().getScriptsPath() + "/" + "questNew/" + npc + ".js");
        return file.exists();
    }

    public static void startConversation(int npc, MapleClient client, String script) {
        var manager = NpcScriptingManager.getInstance();
        try {
            manager.runScript(npc, script, client);
        } catch (ContinuationPending pending) {
            client.getCurrentNpcScript().setContinuation(pending.getContinuation());
        }
    }

    public static void startQuestConversation(int npc, int quest, MapleClient client) {
        var manager = NpcScriptingManager.getInstance();
        try {
            manager.runQuestScript(npc, quest, client);
        } catch (ContinuationPending pending) {
            client.getCurrentNpcScript().setContinuation(pending.getContinuation());
        }
    }

    public static void proceedConversation(InPacket packet, MapleClient client) {
        int talk = packet.readByte();
        Talk type = Talk.from(talk);
        int action = packet.readByte();
        NpcScripting script = client.getCurrentNpcScript();
        if (script == null) {
            client.enableActions();
            return;
        }
        switch (type) {
            case SAY: {
                switch (action) {
                    case ChatAction.ACTION_END_CHAT:
                    case ChatAction.ACTION_BACK_OR_NO:
                        break;
                    case ChatAction.ACTION_NEXT:
                        script.resume(1);
                }
                break;
            }
            case ASK_ACCEPT_NO_ESC:
            case ASK_ACCEPT:
            case ASK_YES_NO:
                switch (action) {
                    case ChatAction.ACTION_BACK_OR_NO:
                    case ChatAction.ACTION_NEXT:
                        script.resume(action);
                        break;
                    case ChatAction.ACTION_END_CHAT:
                }
                break;
            case ASK_AVATAR:
                switch (action) {
                    case ChatAction.ACTION_BACK_OR_NO:
                    case ChatAction.ACTION_NEXT:
                        int result = AskAvatarHelper.processAskAvatar(client.getPlayer(), packet.readByte(), false);
                        script.resume(result);
                }
                break;
            case ASK_MENU:
            case ASK_QUESTION:
                switch (action) {
                    case ChatAction.ACTION_NEXT:
                        script.resume(Integer.valueOf(packet.readInt()));
                    case ChatAction.ACTION_BACK_OR_NO:
                        break;
                }
                break;
            case ASK_NUMBER: {
                switch (action) {
                    case ChatAction.ACTION_NEXT:
                        script.resume(packet.readInt());
                    case ChatAction.ACTION_BACK_OR_NO:
                        break;
                }
                break;
            }
            case ASK_TEXT: {
                switch (action) {
                    case ChatAction.ACTION_NEXT:
                        script.resume(packet.readMapleAsciiString());
                    case ChatAction.ACTION_BACK_OR_NO:
                        break;
                }
                break;
            }
            case UNDEFINED:
            default:
                break;
        }
    }

    enum Talk {
        SAY(0x00),
        ASK_YES_NO(0x02),
        ASK_TEXT(0x03),
        ASK_NUMBER(0x01),
        ASK_MENU(0x04),
        ASK_QUESTION(0x05),
        ASK_QUIZ(0x06),
        ASK_AVATAR(0x08),
        ASK_PET(0x09),
        ASK_ACCEPT(0x0C),
        ASK_ACCEPT_NO_ESC(0x0D),
        ASK_BOX_TEXT(0x0E),
        UNDEFINED(-0xF);

        public int value;

        Talk(int value) {
            this.value = value;
        }

        public static Talk from(int value) {
            for (Talk talk : Talk.values()) {
                if (talk.value == value) {
                    return talk;
                }
            }
            return UNDEFINED;
        }
    }
}
