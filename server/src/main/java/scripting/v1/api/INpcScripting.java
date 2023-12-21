package scripting.v1.api;

import tools.helper.Api;

public interface INpcScripting {
    @Api
    void say(String text);

    @Api
    void sayUser(String text);

    @Api
    void sayOk(String text);

    @Api
    void sayOkUser(String text);

    @Api
    void askYesNo(String text);

    @Api
    void askYesUser(String text);

    @Api
    void askAccept(String text);

    @Api
    void askAcceptUser(String text);

    @Api
    int askAvatar(String text, int item, int... styles);

    @Api
    int makeRandAvatar(int item, int... styles);

    @Api
    void askMenu(String text);

    @Api
    void askMenuUser(String text);

    @Api
    void askText(String text, String def, int col, int line);

    @Api
    void askTextUser(String text, String def, int col, int line);

    @Api
    void askNumber(String text, int def, int min, int max);

    @Api
    void askNumberUser(String text, int def, int min, int max);

    @Api
    void setSpecialAction(int npcId, String action);
}
