package scripting.v1.api;

import tools.Scripting;

public interface INpcScripting {
    @Scripting
    void say(String text);

    @Scripting
    void sayUser(String text);

    @Scripting
    void sayOk(String text);

    @Scripting
    void sayOkUser(String text);

    @Scripting
    void askYesNo(String text);

    @Scripting
    void askYesUser(String text);

    @Scripting
    void askAccept(String text);

    @Scripting
    void askAcceptUser(String text);

    @Scripting
    int askAvatar(String text, int item, int... styles);

    @Scripting
    int makeRandAvatar(int item, int... styles);

    @Scripting
    void askMenu(String text);

    @Scripting
    void askMenuUser(String text);

    @Scripting
    void askText(String text, String def, int col, int line);

    @Scripting
    void askTextUser(String text, String def, int col, int line);

    @Scripting
    void askNumber(String text, int def, int min, int max);

    @Scripting
    void askNumberUser(String text, int def, int min, int max);

    @Scripting
    void setSpecialAction(int npcId, String action);
}
