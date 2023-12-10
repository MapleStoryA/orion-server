package scripting.v1.game;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import handling.world.WorldServer;
import handling.world.party.MaplePartyCharacter;
import java.awt.*;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.event.EventCenter;
import scripting.v1.event.EventInstance;
import tools.ApiClass;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.packet.CWVsContextOnMessagePackets;

@Slf4j
public class TargetScripting extends PlayerScripting {

    private static final int COMPLETE_QUEST = 2;
    private static final int ACTIVE_QUEST = 1;

    public TargetScripting(MapleClient client) {
        super(client);
    }

    @ApiClass
    public int getCharacterID() {
        return player.getId();
    }

    @ApiClass
    public String getCharacterName() {
        return player.getName();
    }

    @ApiClass
    public int getGender() {
        return player.getGender();
    }

    @ApiClass
    public int getHair() {
        return player.getHair();
    }

    @ApiClass
    public int getFace() {
        return player.getFace();
    }

    @ApiClass
    public int nLevel() {
        return player.getLevel();
    }

    @ApiClass
    public int nJob() {
        return player.getJob().getId();
    }

    @ApiClass
    public boolean changeJob(int job) {
        player.changeJob(job);
        return true;
    }

    @ApiClass
    public int nSTR() {
        return player.getStat().getStr();
    }

    @ApiClass
    public int incSTR(int value) {
        short previousSTR = player.getStat().getStr();
        player.setStat(MapleStat.STR, (short) (previousSTR + value));
        return nSTR();
    }

    @ApiClass
    public int nDEX() {
        return player.getStat().getDex();
    }

    @ApiClass
    public int incDEX(int value) {
        short previousDEX = player.getStat().getDex();
        player.setStat(MapleStat.DEX, (short) (previousDEX + value));
        return nDEX();
    }

    @ApiClass
    public int nINT() {
        return player.getStat().getInt();
    }

    @ApiClass
    public int incINT(int value) {
        short previousINT = player.getStat().getInt();
        player.setStat(MapleStat.INT, (short) (previousINT + value));
        return nINT();
    }

    @ApiClass
    public int nLUK() {
        return player.getStat().getLuk();
    }

    @ApiClass
    public int incLUK(short value) {
        short previousLUK = player.getStat().getLuk();
        player.setStat(MapleStat.LUK, (short) (previousLUK + value));
        return nLUK();
    }

    @ApiClass
    public int nHP() {
        return player.getStat().getHp();
    }

    @ApiClass
    public int incHP(int value) {
        int previousHP = player.getStat().getHp();
        player.getStat().setHp(previousHP + value);
        return nHP();
    }

    @ApiClass
    public int nMP() {
        return player.getStat().getMp();
    }

    @ApiClass
    public int incMP(int value) {
        int previousMP = player.getStat().getHp();
        player.getStat().setMp(previousMP + value);
        return nMP();
    }

    @ApiClass
    public int nAP() {
        return player.getRemainingAp();
    }

    @ApiClass
    public int incAP(int value) {
        player.gainAp(value);
        return nAP();
    }

    @ApiClass
    public int incAP(int value, int a) {
        player.gainAp(value);
        return nAP();
    }

    @ApiClass
    public int nSP() {
        return player.getRemainingSp();
    }

    @ApiClass
    public int incSP(int value) {
        if (player.getJob().isEvan()) {
            player.addEvanSP(value);
        } else {
            player.gainSp(value);
        }
        sendPacket(CWVsContextOnMessagePackets.onIncSpMessage(player.getJob(), value));
        return nSP();
    }

    @ApiClass
    public int incSP(int value, int a) {
        return incSP(value);
    }

    @ApiClass
    public boolean isMaster() {
        return player.isGameMaster();
    }

    @ApiClass
    public boolean isSuperGM() {
        return player.isGameMaster();
    }

    @ApiClass
    public void message(String text) {
        player.dropMessage(5, text);
    }

    @ApiClass
    public void incEXP(int total, boolean show) {
        player.gainExp(total, show, show, show);
    }

    @ApiClass
    public void incEXP(int total, int show) {
        this.incEXP(total, show == 0);
    }

    @ApiClass
    public boolean isPartyBoss() {
        if (player.getParty() == null) {
            return false;
        }
        return player.getParty().getLeader().getId() == player.getId();
    }

    @ApiClass
    public boolean isOnParty() {
        return player.getParty() != null;
    }

    @ApiClass
    public int getPartyMembersCount() {
        if (!isOnParty()) {
            return 0;
        }
        return player.getParty().getMembers().size();
    }

    @ApiClass
    public int transferParty(int map, String portal, int option) {
        for (MaplePartyCharacter mate : player.getParty().getMembers()) {
            MapleCharacter chr =
                    WorldServer.getInstance()
                            .getStorage(client.getChannel())
                            .getCharacterById(mate.getId());
            chr.changeMap(map, portal);
        }
        return 1;
    }

    @ApiClass
    public void playPortalSE() {
        sendPacket(MaplePacketCreator.showOwnBuffEffect(0, 7));
    }

    @ApiClass
    public void registerTransferField(int map, String portal) {
        final MapleMap mapz = getWarpMap(map);
        if (map == 109060000 || map == 109060002 || map == 109060004) {
            portal = mapz.getSnowballPortal();
        }
        if (map == player.getMapId()) { // test
            final Point portalPos = new Point(player.getMap().getPortal(portal).getPosition());
            if (portalPos.distanceSq(player.getPosition()) < 90000.0) { // estimation
                player.checkFollow();
                sendPacket(
                        MaplePacketCreator.instantMapWarp(
                                (byte) player.getMap().getPortal(portal).getId()));
                player.getMap()
                        .movePlayer(
                                player, new Point(player.getMap().getPortal(portal).getPosition()));
            } else {
                player.changeMap(mapz, mapz.getPortal(portal));
            }
        } else {
            player.changeMap(mapz, mapz.getPortal(portal));
        }
    }

    @ApiClass
    public FieldScripting field() {
        return client.getPlayer().getMap().getField();
    }

    @ApiClass
    public int fieldID() {
        return player.getMap().getId();
    }

    @ApiClass
    public int nMoney() {
        return player.getMeso();
    }

    @ApiClass
    public int incMoney(int meso, int show) {
        return incMoney(meso, meso == 1);
    }

    @ApiClass
    public int incMoney(int meso, boolean show) {
        if (meso < 0) {
            return -1;
        }
        player.gainMeso(meso, show);
        return nMoney();
    }

    @ApiClass
    public int decMoney(int meso, boolean show) {
        if (meso < 0) {
            return -1;
        }
        player.gainMeso(-meso, show);
        return nMoney();
    }

    @ApiClass
    public void set(String key, String value) {
        player.set(key, value);
    }

    @ApiClass
    public String get(String key) {
        String value = player.get(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @ApiClass
    public void setVar(String key, Object value) {
        player.addTemporaryData(key, value);
    }

    @ApiClass
    public Object getVar(String key) {
        Object value = player.getTemporaryData(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @ApiClass
    public void clearTemporaryData() {
        player.clearTemporaryData();
    }

    @ApiClass
    public EventCenter getEventCenter() {
        return getChannelServer().getEventCenter();
    }

    @ApiClass
    public boolean isEvan() {
        return player.getJob().isEvan();
    }

    @ApiClass
    public boolean isDualBlade() {
        return player.getJob().isDualblade();
    }

    @ApiClass
    public boolean isNightWalker() {
        int job = nJob();
        return job == MapleJob.NIGHTWALKER1.getId()
                || job == MapleJob.NIGHTWALKER2.getId()
                || job == MapleJob.NIGHTWALKER3.getId()
                || job == MapleJob.NIGHTWALKER4.getId();
    }

    @ApiClass
    public boolean isAnyKindOfThief() {
        return isNightWalker() || isDualBlade() || (nJob() / 100) == 4;
    }

    @ApiClass
    public boolean isAran() {
        return player.getJob().isAran();
    }

    @ApiClass
    public boolean haveItem(int id) {
        return player.haveItem(id);
    }

    @ApiClass
    public EventInstance getEvent() {
        return player.getNewEventInstance();
    }

    private MapleMap getWarpMap(final int map) {
        return WorldServer.getInstance()
                .getChannel(client.getChannel())
                .getMapFactory()
                .getMap(map);
    }

    @ApiClass
    public FieldScripting getMap(final int map) {
        return new FieldScripting(getWarpMap(map));
    }

    @ApiClass
    public final byte getQuestStatus(final int id) {
        return player.getQuestStatus(id);
    }

    @ApiClass
    public final boolean isQuestActive(final int id) {
        return getQuestStatus(id) == ACTIVE_QUEST;
    }

    @ApiClass
    public final boolean isQuestFinished(final int id) {
        return getQuestStatus(id) == COMPLETE_QUEST;
    }

    @ApiClass
    public void completeQuest(int id, int npcId) {
        MapleQuest.getInstance(id).complete(getPlayer(), npcId);
    }

    @ApiClass
    public void forfeitQuest(int id) {
        MapleQuest.getInstance(id).forfeit(getPlayer());
    }

    @ApiClass
    public void forceCompleteQuest(final int id, int npcId) {
        MapleQuest.getInstance(id).forceComplete(getPlayer(), npcId);
    }

    @ApiClass
    public void changeMusic(String music) {
        sendPacket(MaplePacketCreator.musicChange(music));
    }
}
