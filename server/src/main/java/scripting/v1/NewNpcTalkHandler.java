package scripting.v1;

import client.MapleClient;
import org.mozilla.javascript.ContinuationPending;
import scripting.v1.binding.AskAvatarOperations;
import scripting.v1.binding.NpcScript;
import tools.data.input.SeekableLittleEndianAccessor;

import java.io.File;

public class NewNpcTalkHandler {

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

    public static boolean isNewNpcScriptAvailable(int npc) {
        File file = new File("dist/scripts/npcNew/" + npc + ".js");
        return file.exists();
    }

    public static boolean isNewQuestScriptAvailable(int npc) {
        File file = new File("dist/scripts/questNew/" + npc + ".js");
        return file.exists();
    }


    public static void startConversation(int npc, MapleClient client) {
        NpcScriptingManager manager = NpcScriptingManagerSingleton.getInstance();
        try {
            manager.runScript(npc, client);
        } catch (ContinuationPending pending) {
            client.getNpcScript().setContinuation(pending.getContinuation());
        }
    }

    public static void startQuestConversation(int npc, int quest, MapleClient client) {
        NpcScriptingManager manager = NpcScriptingManagerSingleton.getInstance();
        try {
            manager.runQuestScript(npc, quest, client);
        } catch (ContinuationPending pending) {
            client.getNpcScript().setContinuation(pending.getContinuation());
        }
    }


    public static void proceedConversation(SeekableLittleEndianAccessor slea, MapleClient client) {
        int talk = slea.readByte();
        Talk type = Talk.from(talk);
        int action = slea.readByte();
        NpcScript script = client.getNpcScript();
        if (script == null) {
            client.enableActions();
            return;
        }
        switch (type) {
            case SAY: {
                switch (action) {
                    case -1:// end_chat
                        break;
                    case 0:// back
                        break;
                    case 1:// next
                        script.resume(1);
                }
                break;
            }
            case ASK_ACCEPT_NO_ESC:
            case ASK_ACCEPT:
            case ASK_YES_NO:
                switch (action) {
                    case 0:// No
                    case 1:// Yes
                        script.resume(action);
                        break;
                    case -1:// end chat
                }
                break;
            case ASK_AVATAR:
                switch (action) {
                    case 0:
                    case 1:
                        int result = AskAvatarOperations.processAskAvatar(client.getPlayer(), slea.readByte(), false);
                        script.resume(result);
                }
                break;
            case ASK_MENU:
            case ASK_QUESTION:
                switch (action) {
                    case 1:
                        script.resume(Integer.valueOf(slea.readInt()));
                    case 0:
                        break;
                }
                break;
            case ASK_NUMBER: {
                switch (action) {
                    case 1:
                        script.resume(slea.readInt());
                    case 0:
                        break;
                }
                break;
            }
            case ASK_TEXT: {
                switch (action) {
                    case 1:
                        script.resume(slea.readMapleAsciiString());
                    case 0:
                        break;
                }
                break;
            }
            case UNDEFINED:
            default:
                break;
        }
    }

}
