package scripting.v1.api;

import scripting.v1.base.FieldScripting;
import scripting.v1.base.InventoryScripting;
import server.maps.MapleMap;
import tools.helper.Api;

public interface ITargetScripting {
    @Api
    int getCharacterID();

    @Api
    String getCharacterName();

    @Api
    int getGender();

    @Api
    int getHair();

    @Api
    int getFace();

    @Api
    int nLevel();

    @Api
    int nJob();

    @Api
    boolean changeJob(int job);

    @Api
    boolean setJob(int job);

    @Api
    int nSTR();

    @Api
    int incSTR(int value);

    @Api
    int nDEX();

    @Api
    int incDEX(int value);

    @Api
    int nINT();

    @Api
    int incINT(int value);

    @Api
    int nLUK();

    @Api
    int incLUK(short value);

    @Api
    int nHP();

    @Api
    int incHP(int value);

    @Api
    int nMP();

    @Api
    int incMP(int value);

    @Api
    int incMHP(int value, int other);

    @Api
    int incMMP(int value, int other);

    @Api
    int nAP();

    @Api
    int incAP(int value);

    @Api
    int incAP(int value, int a);

    @Api
    int nSP();

    @Api
    int incSP(int value);

    @Api
    int incSP(int value, int a);

    @Api
    boolean isMaster();

    @Api
    boolean isSuperGM();

    @Api
    void message(String text);

    @Api
    void incEXP(int total, boolean show);

    @Api
    void incEXP(int total, int show);

    @Api
    boolean isPartyBoss();

    @Api
    boolean isOnParty();

    @Api
    int getPartyMembersCount();

    @Api
    int transferParty(int map, String portal, int option);

    @Api
    void playPortalSE();

    @Api
    void registerTransferField(int map, String portal);

    @Api
    FieldScripting field();

    @Api
    int id();

    @Api
    int nMoney();

    @Api
    int incMoney(int meso, int show);

    @Api
    int incMoney(int meso, boolean show);

    @Api
    int decMoney(int meso, boolean show);

    @Api
    void set(String key, String value);

    @Api
    String get(String key);

    @Api
    void setVar(String key, Object value);

    @Api
    Object getVar(String key);

    @Api
    void clearTemporaryData();

    @Api
    boolean isEvan();

    @Api
    boolean isDualBlade();

    @Api
    boolean isNightWalker();

    @Api
    boolean isAnyKindOfThief();

    @Api
    boolean isAran();

    @Api
    boolean haveItem(int id);

    MapleMap getWarpMap(int map);

    @Api
    FieldScripting getMap(int map);

    @Api
    byte getQuestStatus(int id);

    @Api
    boolean isQuestActive(int id);

    @Api
    boolean isQuestFinished(int id);

    @Api
    void completeQuest(int id, int npcId);

    @Api
    void forfeitQuest(int id);

    @Api
    void forceCompleteQuest(int id, int npcId);

    @Api
    void changeMusic(String music);

    QuestRecord questRecord();

    InventoryScripting inventory();
}
