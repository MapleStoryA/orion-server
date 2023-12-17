package scripting.v1.api;

public interface IFieldScripting {
    int id();

    /**
     * Constructor for Field with a specific ID.
     *
     * @param id The ID of the field.
     */
    void Field(int id);

    /**
     * Gets the count of users in the field.
     *
     * @return The count of users.
     */
    int getUserCount();

    /**
     * Gets the count of a specific mob by its ID.
     *
     * @param mobId The ID of the mob.
     * @return The count of the specified mob.
     */
    int getMobCount(int mobId);

    /**
     * Gets the HP of a specific mob. Only valid when there is only one of the specified mob.
     * If the mob does not exist, returns -1.
     *
     * @param mobId The ID of the mob.
     * @return The HP of the mob, or -1 if the mob does not exist.
     */
    int getMobHP(int mobId);

    /**
     * Counts users in a specific area.
     *
     * @param areaName The name of the area.
     * @return The count of users in the area.
     */
    int countUserInArea(String areaName);

    /**
     * Counts male characters in a specific area.
     *
     * @param areaName The name of the area.
     * @return The count of male characters in the area.
     */
    int countMaleInArea(String areaName);

    /**
     * Counts female characters in a specific area.
     *
     * @param areaName The name of the area.
     * @return The count of female characters in the area.
     */
    int countFemaleInArea(String areaName);

    /**
     * Enables or disables a portal.
     *
     * @param portalName The name of the portal.
     * @param status     Open: 1, Close: 0
     */
    void enablePortal(String portalName, int status);

    // Additional methods follow the same pattern...
    void effectObject(String objName);

    void effectScreen(String name);

    void effectSound(String soundName);

    void effectTremble(int type, int delay);

    void notice(int type, String message, Object... args);

    int isItemInArea(String areaName, int itemId);

    void summonMob(int x, int y, int itemId);

    int transferFieldAll(int mapCode, String portalName);

    void setNpcVar(int npcId, String key, String var);

    String getNpcStrVar(int npcId, String varName);

    int getNpcIntVar(int npcId, String varName);

    void setProtectMobDamagedByMob(int setting);

    void removeAllMob();

    void setMobGen(int onOff);

    void removeMob(int mobId);

    int snowOn(int setting);

    void buffMob(int mobId, int effect, int duration);

    int isUserExist(int userId);

    void startEvent();

    void summonNpc(int templateId, int x, int y);

    void vanishNpc(int templateId);

    // Other methods as per the original structure...
}

