package scripting.v1.game.api;

import scripting.v1.event.EventCenter;
import scripting.v1.event.EventInstance;
import scripting.v1.game.FieldScripting;
import scripting.v1.game.InventoryScripting;
import scripting.v1.game.QuestRecord;
import server.maps.MapleMap;
import tools.ApiClass;

public interface ITargetScripting {
    @ApiClass
    int getCharacterID();

    @ApiClass
    String getCharacterName();

    @ApiClass
    int getGender();

    @ApiClass
    int getHair();

    @ApiClass
    int getFace();

    @ApiClass
    int nLevel();

    @ApiClass
    int nJob();

    @ApiClass
    boolean changeJob(int job);

    @ApiClass
    boolean setJob(int job);

    @ApiClass
    int nSTR();

    @ApiClass
    int incSTR(int value);

    @ApiClass
    int nDEX();

    @ApiClass
    int incDEX(int value);

    @ApiClass
    int nINT();

    @ApiClass
    int incINT(int value);

    @ApiClass
    int nLUK();

    @ApiClass
    int incLUK(short value);

    @ApiClass
    int nHP();

    @ApiClass
    int incHP(int value);

    @ApiClass
    int nMP();

    @ApiClass
    int incMP(int value);

    @ApiClass
    int incMHP(int value, int other);

    @ApiClass
    int incMMP(int value, int other);

    @ApiClass
    int nAP();

    @ApiClass
    int incAP(int value);

    @ApiClass
    int incAP(int value, int a);

    @ApiClass
    int nSP();

    @ApiClass
    int incSP(int value);

    @ApiClass
    int incSP(int value, int a);

    @ApiClass
    boolean isMaster();

    @ApiClass
    boolean isSuperGM();

    @ApiClass
    void message(String text);

    @ApiClass
    void incEXP(int total, boolean show);

    @ApiClass
    void incEXP(int total, int show);

    @ApiClass
    boolean isPartyBoss();

    @ApiClass
    boolean isOnParty();

    @ApiClass
    int getPartyMembersCount();

    @ApiClass
    int transferParty(int map, String portal, int option);

    @ApiClass
    void playPortalSE();

    @ApiClass
    void registerTransferField(int map, String portal);

    @ApiClass
    FieldScripting field();

    @ApiClass
    int id();

    @ApiClass
    int nMoney();

    @ApiClass
    int incMoney(int meso, int show);

    @ApiClass
    int incMoney(int meso, boolean show);

    @ApiClass
    int decMoney(int meso, boolean show);

    @ApiClass
    void set(String key, String value);

    @ApiClass
    String get(String key);

    @ApiClass
    void setVar(String key, Object value);

    @ApiClass
    Object getVar(String key);

    @ApiClass
    void clearTemporaryData();

    @ApiClass
    EventCenter getEventCenter();

    @ApiClass
    boolean isEvan();

    @ApiClass
    boolean isDualBlade();

    @ApiClass
    boolean isNightWalker();

    @ApiClass
    boolean isAnyKindOfThief();

    @ApiClass
    boolean isAran();

    @ApiClass
    boolean haveItem(int id);

    @ApiClass
    EventInstance getEvent();

    MapleMap getWarpMap(int map);

    @ApiClass
    FieldScripting getMap(int map);

    @ApiClass
    byte getQuestStatus(int id);

    @ApiClass
    boolean isQuestActive(int id);

    @ApiClass
    boolean isQuestFinished(int id);

    @ApiClass
    void completeQuest(int id, int npcId);

    @ApiClass
    void forfeitQuest(int id);

    @ApiClass
    void forceCompleteQuest(int id, int npcId);

    @ApiClass
    void changeMusic(String music);

    QuestRecord questRecord();

    InventoryScripting inventory();
}
