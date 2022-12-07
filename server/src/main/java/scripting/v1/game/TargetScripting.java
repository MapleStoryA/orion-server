package scripting.v1.game;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import handling.world.World;
import handling.world.WorldServer;
import handling.world.party.MaplePartyCharacter;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.event.EventCenter;
import scripting.v1.event.EventInstance;
import scripting.v1.game.helper.ScriptingApi;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.packet.CWVsContextOnMessagePackets;

import java.awt.*;


@Slf4j
public class TargetScripting extends PlayerScripting {

    private static final int COMPLETE_QUEST = 2;
    private static final int ACTIVE_QUEST = 1;

    public TargetScripting(MapleClient client) {
        super(client);
    }

    @ScriptingApi
    public int getCharacterID() {
        return player.getId();
    }

    @ScriptingApi
    public String getCharacterName() {
        return player.getName();
    }

    @ScriptingApi
    public int getGender() {
        return player.getGender();
    }

    @ScriptingApi
    public int getHair() {
        return player.getHair();
    }

    @ScriptingApi
    public int getFace() {
        return player.getFace();
    }

    @ScriptingApi
    public int nLevel() {
        return player.getLevel();
    }

    @ScriptingApi
    public int nJob() {
        return player.getJob();
    }

    @ScriptingApi
    public boolean changeJob(int job) {
        player.changeJob(job);
        return true;
    }

    @ScriptingApi
    public int nSTR() {
        return player.getStat().getStr();
    }

    @ScriptingApi
    public int incSTR(int value) {
        short previousSTR = player.getStat().getStr();
        player.setstat((byte) 1, (short) (previousSTR + value));
        return nSTR();
    }

    @ScriptingApi
    public int nDEX() {
        return player.getStat().getDex();
    }

    @ScriptingApi
    public int incDEX(int value) {
        short previousDEX = player.getStat().getDex();
        player.setstat((byte) 2, (short) (previousDEX + value));
        return nDEX();
    }

    @ScriptingApi
    public int nINT() {
        return player.getStat().getInt();
    }

    @ScriptingApi
    public int incINT(int value) {
        short previousINT = player.getStat().getInt();
        player.setstat((byte) 3, (short) (previousINT + value));
        return nINT();
    }

    @ScriptingApi
    public int nLUK() {
        return player.getStat().getLuk();
    }

    @ScriptingApi
    public int incLUK(short value) {
        short previousLUK = player.getStat().getLuk();
        player.setstat((byte) 4, (short) (previousLUK + value));
        return nLUK();
    }

    @ScriptingApi
    public int nHP() {
        return player.getStat().getHp();
    }

    @ScriptingApi
    public int incHP(int value) {
        int previousHP = player.getStat().getHp();
        player.getStat().setHp(previousHP + value);
        return nHP();
    }

    @ScriptingApi
    public int nMP() {
        return player.getStat().getMp();
    }

    @ScriptingApi
    public int incMP(int value) {
        int previousMP = player.getStat().getHp();
        player.getStat().setMp(previousMP + value);
        return nMP();
    }

    @ScriptingApi
    public int nAP() {
        return player.getRemainingAp();
    }

    @ScriptingApi
    public int incAP(int value) {
        player.gainAp(value);
        return nAP();
    }


    @ScriptingApi
    public int incAP(int value, int a) {
        player.gainAp(value);
        return nAP();
    }

    @ScriptingApi
    public int nSP() {
        return player.getRemainingSp();
    }

    @ScriptingApi
    public int incSP(int value) {
        if (player.isEvan()) {
            player.addEvanSP(value);
        } else {
            player.gainSp(value);
        }
        sendPacket(CWVsContextOnMessagePackets.onIncSpMessage(player.getJobValue(), value));
        return nSP();
    }

    @ScriptingApi
    public int incSP(int value, int a) {
        return incSP(value);
    }

    @ScriptingApi
    public boolean isMaster() {
        return player.isGM();
    }

    @ScriptingApi
    public boolean isSuperGM() {
        return player.isGM();
    }

    @ScriptingApi
    public void message(String text) {
        player.dropMessage(5, text);
    }

    @ScriptingApi
    public void incEXP(int total, boolean show) {
        player.gainExp(total, show, show, show);
    }

    @ScriptingApi
    public void incEXP(int total, int show) {
        this.incEXP(total, show == 0);
    }

    @ScriptingApi
    public boolean isPartyBoss() {
        if (player.getParty() == null) {
            return false;
        }
        return player.getParty().getLeader().getId() == player.getId();
    }

    @ScriptingApi
    public boolean isOnParty() {
        return player.getParty() != null;
    }

    @ScriptingApi
    public int getPartyMembersCount() {
        if (!isOnParty()) {
            return 0;
        }
        return player.getParty().getMembers().size();
    }

    @ScriptingApi
    public int transferParty(int map, String portal, int option) {
        for (MaplePartyCharacter mate : player.getParty().getMembers()) {
            MapleCharacter chr = World.getStorage(client.getChannel()).getCharacterById(mate.getId());
            chr.changeMap(map, portal);
        }
        return 1;
    }

    @ScriptingApi
    public void playPortalSE() {
        sendPacket(MaplePacketCreator.showOwnBuffEffect(0, 7));
    }

    @ScriptingApi
    public void registerTransferField(int map, String portal) {
        final MapleMap mapz = getWarpMap(map);
        if (map == 109060000 || map == 109060002 || map == 109060004) {
            portal = mapz.getSnowballPortal();
        }
        if (map == player.getMapId()) { // test
            final Point portalPos = new Point(player.getMap().getPortal(portal).getPosition());
            if (portalPos.distanceSq(player.getPosition()) < 90000.0) { // estimation
                player.checkFollow();
                sendPacket(MaplePacketCreator.instantMapWarp((byte) player.getMap().getPortal(portal).getId()));
                player.getMap().movePlayer(player, new Point(player.getMap().getPortal(portal).getPosition()));
            } else {
                player.changeMap(mapz, mapz.getPortal(portal));
            }
        } else {
            player.changeMap(mapz, mapz.getPortal(portal));
        }
    }

    @ScriptingApi
    public FieldScripting field() {
        return client.getPlayer().getMap().getField();
    }

    @ScriptingApi
    public int fieldID() {
        return player.getMap().getId();
    }

    @ScriptingApi
    public int nMoney() {
        return player.getMeso();
    }

    @ScriptingApi
    public int incMoney(int meso, int show) {
        return incMoney(meso, meso == 1);
    }


    @ScriptingApi
    public int incMoney(int meso, boolean show) {
        if (meso < 0) {
            return -1;
        }
        player.gainMeso(meso, show);
        return nMoney();
    }

    @ScriptingApi
    public int decMoney(int meso, boolean show) {
        if (meso < 0) {
            return -1;
        }
        player.gainMeso(-meso, show);
        return nMoney();
    }

    @ScriptingApi
    public void set(String key, String value) {
        player.set(key, value);
    }

    @ScriptingApi
    public String get(String key) {
        String value = player.get(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @ScriptingApi
    public void setVar(String key, Object value) {
        player.addTemporaryData(key, value);
    }

    @ScriptingApi
    public Object getVar(String key) {
        Object value = player.getTemporaryData(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @ScriptingApi
    public void clearTemporaryData() {
        player.clearTemporaryData();
    }

    @ScriptingApi
    public EventCenter getEventCenter() {
        return getChannelServer().getEventCenter();
    }

    @ScriptingApi
    public boolean isEvan() {
        return player.isEvan();
    }

    @ScriptingApi
    public boolean isDualBlade() {
        return player.isDualblade();
    }

    @ScriptingApi
    public boolean isNightWalker() {
        int job = nJob();
        return job == MapleJob.NIGHTWALKER1.getId() || job == MapleJob.NIGHTWALKER2.getId() || job == MapleJob.NIGHTWALKER3.getId() || job == MapleJob.NIGHTWALKER4.getId();
    }

    @ScriptingApi
    public boolean isAnyKindOfThief() {
        return isNightWalker() || isDualBlade() || (nJob() / 100) == 4;
    }

    @ScriptingApi
    public boolean isAran() {
        return player.isAran();
    }

    @ScriptingApi
    public boolean haveItem(int id) {
        return player.haveItem(id);
    }

    @ScriptingApi
    public EventInstance getEvent() {
        return player.getNewEventInstance();
    }

    private MapleMap getWarpMap(final int map) {
        return WorldServer.getInstance().getChannel(client.getChannel()).getMapFactory().getMap(map);
    }

    @ScriptingApi
    public FieldScripting getMap(final int map) {
        return new FieldScripting(getWarpMap(map));
    }

    @ScriptingApi
    public final byte getQuestStatus(final int id) {
        return player.getQuestStatus(id);
    }

    @ScriptingApi
    public final boolean isQuestActive(final int id) {
        return getQuestStatus(id) == ACTIVE_QUEST;
    }

    @ScriptingApi
    public final boolean isQuestFinished(final int id) {
        return getQuestStatus(id) == COMPLETE_QUEST;
    }

    @ScriptingApi
    public void completeQuest(int id, int npcId) {
        MapleQuest.getInstance(id).complete(getPlayer(), npcId);
    }

    @ScriptingApi
    public void forfeitQuest(int id) {
        MapleQuest.getInstance(id).forfeit(getPlayer());
    }

    @ScriptingApi
    public void forceCompleteQuest(final int id, int npcId) {
        MapleQuest.getInstance(id).forceComplete(getPlayer(), npcId);
    }

    @ScriptingApi
    public void changeMusic(String music) {
        sendPacket(MaplePacketCreator.musicChange(music));
    }


}
