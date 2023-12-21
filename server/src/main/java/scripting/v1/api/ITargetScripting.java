package scripting.v1.api;

import scripting.v1.base.FieldScripting;
import scripting.v1.base.InventoryScripting;
import server.maps.MapleMap;
import tools.helper.Scripting;

public interface ITargetScripting {
    @Scripting
    int getCharacterID();

    @Scripting
    String getCharacterName();

    @Scripting
    int getGender();

    @Scripting
    int getHair();

    @Scripting
    int getFace();

    @Scripting
    int nLevel();

    @Scripting
    int nJob();

    @Scripting
    boolean changeJob(int job);

    @Scripting
    boolean setJob(int job);

    @Scripting
    int nSTR();

    @Scripting
    int incSTR(int value);

    @Scripting
    int nDEX();

    @Scripting
    int incDEX(int value);

    @Scripting
    int nINT();

    @Scripting
    int incINT(int value);

    @Scripting
    int nLUK();

    @Scripting
    int incLUK(short value);

    @Scripting
    int nHP();

    @Scripting
    int incHP(int value);

    @Scripting
    int nMP();

    @Scripting
    int incMP(int value);

    @Scripting
    int incMHP(int value, int other);

    @Scripting
    int incMMP(int value, int other);

    @Scripting
    int nAP();

    @Scripting
    int incAP(int value);

    @Scripting
    int incAP(int value, int a);

    @Scripting
    int nSP();

    @Scripting
    int incSP(int value);

    @Scripting
    int incSP(int value, int a);

    @Scripting
    boolean isMaster();

    @Scripting
    boolean isSuperGM();

    @Scripting
    void message(String text);

    @Scripting
    void incEXP(int total, boolean show);

    @Scripting
    void incEXP(int total, int show);

    @Scripting
    boolean isPartyBoss();

    @Scripting
    boolean isOnParty();

    @Scripting
    int getPartyMembersCount();

    @Scripting
    int transferParty(int map, String portal, int option);

    @Scripting
    void playPortalSE();

    @Scripting
    void registerTransferField(int map, String portal);

    @Scripting
    FieldScripting field();

    @Scripting
    int id();

    @Scripting
    int nMoney();

    @Scripting
    int incMoney(int meso, int show);

    @Scripting
    int incMoney(int meso, boolean show);

    @Scripting
    int decMoney(int meso, boolean show);

    @Scripting
    void set(String key, String value);

    @Scripting
    String get(String key);

    @Scripting
    void setVar(String key, Object value);

    @Scripting
    Object getVar(String key);

    @Scripting
    void clearTemporaryData();

    @Scripting
    boolean isEvan();

    @Scripting
    boolean isDualBlade();

    @Scripting
    boolean isNightWalker();

    @Scripting
    boolean isAnyKindOfThief();

    @Scripting
    boolean isAran();

    @Scripting
    boolean haveItem(int id);

    MapleMap getWarpMap(int map);

    @Scripting
    FieldScripting getMap(int map);

    @Scripting
    byte getQuestStatus(int id);

    @Scripting
    boolean isQuestActive(int id);

    @Scripting
    boolean isQuestFinished(int id);

    @Scripting
    void completeQuest(int id, int npcId);

    @Scripting
    void forfeitQuest(int id);

    @Scripting
    void forceCompleteQuest(int id, int npcId);

    @Scripting
    void changeMusic(String music);

    QuestRecord questRecord();

    InventoryScripting inventory();
}
