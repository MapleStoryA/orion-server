package scripting.v1.api;

import tools.ApiClass;

public interface INpcScripting {
    @ApiClass
    void say(String text);

    @ApiClass
    void sayUser(String text);

    @ApiClass
    void sayOk(String text);

    @ApiClass
    void sayOkUser(String text);

    @ApiClass
    void askYesNo(String text);

    @ApiClass
    void askYesUser(String text);

    @ApiClass
    void askAccept(String text);

    @ApiClass
    void askAcceptUser(String text);

    @ApiClass
    int askAvatar(String text, int item, int... styles);

    @ApiClass
    int makeRandAvatar(int item, int... styles);

    @ApiClass
    void askMenu(String text);

    @ApiClass
    void askMenuUser(String text);

    @ApiClass
    void askText(String text, String def, int col, int line);

    @ApiClass
    void askTextUser(String text, String def, int col, int line);

    @ApiClass
    void askNumber(String text, int def, int min, int max);

    @ApiClass
    void askNumberUser(String text, int def, int min, int max);

    @ApiClass
    void setSpecialAction(int npcId, String action);


}
