package scripting.v1.game.helper;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.ContinuationPending;
import scripting.v1.NpcScriptingManager;
import scripting.v1.game.NpcScripting;
import server.config.ServerConfig;
import tools.data.input.InPacket;

import java.io.File;

@Slf4j
public class NpcTalkHelper {

    class ChatAction {
        public static final int ACTION_END_CHAT = -1;
        public static final int ACTION_BACK_OR_NO = 0;
        public static final int ACTION_NEXT = 1;
    }

    public static boolean isNewNpcScriptAvailable(int npc, String script) {
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

    public static void proceedConversation(InPacket slea, MapleClient client) {
        int talk = slea.readByte();
        Talk type = Talk.from(talk);
        int action = slea.readByte();
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
                        int result = AskAvatarHelper.processAskAvatar(client.getPlayer(), slea.readByte(), false);
                        script.resume(result);
                }
                break;
            case ASK_MENU:
            case ASK_QUESTION:
                switch (action) {
                    case ChatAction.ACTION_NEXT:
                        script.resume(Integer.valueOf(slea.readInt()));
                    case ChatAction.ACTION_BACK_OR_NO:
                        break;
                }
                break;
            case ASK_NUMBER: {
                switch (action) {
                    case ChatAction.ACTION_NEXT:
                        script.resume(slea.readInt());
                    case ChatAction.ACTION_BACK_OR_NO:
                        break;
                }
                break;
            }
            case ASK_TEXT: {
                switch (action) {
                    case ChatAction.ACTION_NEXT:
                        script.resume(slea.readMapleAsciiString());
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
