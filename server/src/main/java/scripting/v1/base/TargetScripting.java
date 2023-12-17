package scripting.v1.base;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import handling.world.WorldServer;
import handling.world.party.MaplePartyCharacter;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.api.ITargetScripting;
import scripting.v1.api.QuestRecord;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import tools.ApiClass;
import tools.MaplePacketCreator;
import tools.packet.CWVsContextOnMessagePackets;

import java.awt.*;

@Slf4j
public class TargetScripting extends PlayerScripting implements ITargetScripting {

    private static final int COMPLETE_QUEST = 2;
    private static final int ACTIVE_QUEST = 1;

    public TargetScripting(MapleClient client) {
        super(client);
    }

    @Override
    public MapleMap getWarpMap(int map) {
        return WorldServer.getInstance()
                .getChannel(client.getChannel())
                .getMapFactory()
                .getMap(map);
    }

    @Override
    @ApiClass
    public int getCharacterID() {
        return player.getId();
    }

    @Override
    @ApiClass
    public String getCharacterName() {
        return player.getName();
    }

    @Override
    @ApiClass
    public int getGender() {
        return player.getGender();
    }

    @Override
    @ApiClass
    public int getHair() {
        return player.getHair();
    }

    @Override
    @ApiClass
    public int getFace() {
        return player.getFace();
    }

    @Override
    @ApiClass
    public int nLevel() {
        return player.getLevel();
    }

    @Override
    @ApiClass
    public int nJob() {
        return player.getJob().getId();
    }

    @Override
    @ApiClass
    public boolean changeJob(int job) {
        player.changeJob(job);
        return true;
    }

    @Override
    @ApiClass
    public boolean setJob(int job) {
        player.changeJob(job);
        return true;
    }

    @Override
    @ApiClass
    public int nSTR() {
        return player.getStat().getStr();
    }

    @Override
    @ApiClass
    public int incSTR(int value) {
        short previousSTR = player.getStat().getStr();
        player.setStat(MapleStat.STR, (short) (previousSTR + value));
        return nSTR();
    }

    @Override
    @ApiClass
    public int nDEX() {
        return player.getStat().getDex();
    }

    @Override
    @ApiClass
    public int incDEX(int value) {
        short previousDEX = player.getStat().getDex();
        player.setStat(MapleStat.DEX, (short) (previousDEX + value));
        return nDEX();
    }

    @Override
    @ApiClass
    public int nINT() {
        return player.getStat().getInt();
    }

    @Override
    @ApiClass
    public int incINT(int value) {
        short previousINT = player.getStat().getInt();
        player.setStat(MapleStat.INT, (short) (previousINT + value));
        return nINT();
    }

    @Override
    @ApiClass
    public int nLUK() {
        return player.getStat().getLuk();
    }

    @Override
    @ApiClass
    public int incLUK(short value) {
        short previousLUK = player.getStat().getLuk();
        player.setStat(MapleStat.LUK, (short) (previousLUK + value));
        return nLUK();
    }

    @Override
    @ApiClass
    public int nHP() {
        return player.getStat().getHp();
    }

    @Override
    @ApiClass
    public int incHP(int value) {
        int previousHP = player.getStat().getHp();
        player.getStat().setHp(previousHP + value);
        return nHP();
    }

    @Override
    @ApiClass
    public int nMP() {
        return player.getStat().getMp();
    }

    @Override
    @ApiClass
    public int incMP(int value) {
        int previousMP = player.getStat().getHp();
        player.getStat().setMp(previousMP + value);
        return nMP();
    }

    @Override
    @ApiClass
    public int incMHP(int value, int other) {
        int previousMaxHP = player.getStat().getMaxHp();
        player.getStat().setMaxHp(previousMaxHP + value);
        return nHP();
    }

    @Override
    @ApiClass
    public int incMMP(int value, int other) {
        int previousMaxMp = player.getStat().getMaxMp();
        player.getStat().setMaxMp(previousMaxMp + value);
        return nMP();
    }

    @Override
    @ApiClass
    public int nAP() {
        return player.getRemainingAp();
    }

    @Override
    @ApiClass
    public int incAP(int value) {
        player.gainAp(value);
        return nAP();
    }

    @Override
    @ApiClass
    public int incAP(int value, int a) {
        player.gainAp(value);
        return nAP();
    }

    @Override
    @ApiClass
    public int nSP() {
        return player.getRemainingSp();
    }

    @Override
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

    @Override
    @ApiClass
    public int incSP(int value, int a) {
        return incSP(value);
    }

    @Override
    @ApiClass
    public boolean isMaster() {
        return player.isGameMaster();
    }

    @Override
    @ApiClass
    public boolean isSuperGM() {
        return player.isGameMaster();
    }

    @Override
    @ApiClass
    public void message(String text) {
        player.dropMessage(5, text);
    }

    @Override
    @ApiClass
    public void incEXP(int total, boolean show) {
        player.gainExp(total, show, show, show);
    }

    @Override
    @ApiClass
    public void incEXP(int total, int show) {
        this.incEXP(total, show == 0);
    }

    @Override
    @ApiClass
    public boolean isPartyBoss() {
        if (player.getParty() == null) {
            return false;
        }
        return player.getParty().getLeader().getId() == player.getId();
    }

    @Override
    @ApiClass
    public boolean isOnParty() {
        return player.getParty() != null;
    }

    @Override
    @ApiClass
    public int getPartyMembersCount() {
        if (!isOnParty()) {
            return 0;
        }
        return player.getParty().getMembers().size();
    }

    @Override
    @ApiClass
    public int transferParty(int map, String portal, int option) {
        for (MaplePartyCharacter mate : player.getParty().getMembers()) {
            MapleCharacter chr =
                    WorldServer.getInstance().getStorage(client.getChannel()).getCharacterById(mate.getId());
            chr.changeMap(map, portal);
        }
        return 1;
    }

    @Override
    @ApiClass
    public void playPortalSE() {
        sendPacket(MaplePacketCreator.showOwnBuffEffect(0, 7));
    }

    @Override
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
                sendPacket(MaplePacketCreator.instantMapWarp(
                        (byte) player.getMap().getPortal(portal).getId()));
                player.getMap()
                        .movePlayer(
                                player,
                                new Point(player.getMap().getPortal(portal).getPosition()));
            } else {
                player.changeMap(mapz, mapz.getPortal(portal));
            }
        } else {
            player.changeMap(mapz, mapz.getPortal(portal));
        }
    }

    @Override
    @ApiClass
    public FieldScripting field() {
        return client.getPlayer().getMap().getField();
    }

    @Override
    @ApiClass
    public int id() {
        return player.getMap().getId();
    }

    @Override
    @ApiClass
    public int nMoney() {
        return player.getMeso();
    }

    @Override
    @ApiClass
    public int incMoney(int meso, int show) {
        return incMoney(meso, meso == 1);
    }

    @Override
    @ApiClass
    public int incMoney(int meso, boolean show) {
        if (meso < 0) {
            return -1;
        }
        player.gainMeso(meso, show);
        return nMoney();
    }

    @Override
    @ApiClass
    public int decMoney(int meso, boolean show) {
        if (meso < 0) {
            return -1;
        }
        player.gainMeso(-meso, show);
        return nMoney();
    }

    @Override
    @ApiClass
    public void set(String key, String value) {
        player.set(key, value);
    }

    @Override
    @ApiClass
    public String get(String key) {
        String value = player.get(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    @ApiClass
    public void setVar(String key, Object value) {
        player.addTemporaryData(key, value);
    }

    @Override
    @ApiClass
    public Object getVar(String key) {
        Object value = player.getTemporaryData(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    @ApiClass
    public void clearTemporaryData() {
        player.clearTemporaryData();
    }

    @ApiClass
    public boolean isEvan() {
        return player.getJob().isEvan();
    }

    @Override
    @ApiClass
    public boolean isDualBlade() {
        return player.getJob().isDualblade();
    }

    @Override
    @ApiClass
    public boolean isNightWalker() {
        int job = nJob();
        return job == MapleJob.NIGHTWALKER1.getId()
                || job == MapleJob.NIGHTWALKER2.getId()
                || job == MapleJob.NIGHTWALKER3.getId()
                || job == MapleJob.NIGHTWALKER4.getId();
    }

    @Override
    @ApiClass
    public boolean isAnyKindOfThief() {
        return isNightWalker() || isDualBlade() || (nJob() / 100) == 4;
    }

    @Override
    @ApiClass
    public boolean isAran() {
        return player.getJob().isAran();
    }

    @Override
    @ApiClass
    public boolean haveItem(int id) {
        return player.haveItem(id);
    }

    @Override
    @ApiClass
    public FieldScripting getMap(final int map) {
        return new FieldScripting(getWarpMap(map));
    }

    @Override
    @ApiClass
    public final byte getQuestStatus(final int id) {
        return player.getQuestStatus(id);
    }

    @Override
    @ApiClass
    public final boolean isQuestActive(final int id) {
        return getQuestStatus(id) == ACTIVE_QUEST;
    }

    @Override
    @ApiClass
    public final boolean isQuestFinished(final int id) {
        return getQuestStatus(id) == COMPLETE_QUEST;
    }

    @Override
    @ApiClass
    public void completeQuest(int id, int npcId) {
        MapleQuest.getInstance(id).complete(getPlayer(), npcId);
    }

    @Override
    @ApiClass
    public void forfeitQuest(int id) {
        MapleQuest.getInstance(id).forfeit(getPlayer());
    }

    @Override
    @ApiClass
    public void forceCompleteQuest(final int id, int npcId) {
        MapleQuest.getInstance(id).forceComplete(getPlayer(), npcId);
    }

    @Override
    @ApiClass
    public void changeMusic(String music) {
        sendPacket(MaplePacketCreator.musicChange(music));
    }

    @Override
    public QuestRecord questRecord() {
        return new QuestScripting(this.client, null);
    }

    @Override
    public InventoryScripting inventory() {
        return new InventoryScripting(client);
    }
}
