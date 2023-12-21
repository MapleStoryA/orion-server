package scripting.v1.base;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import handling.world.WorldServer;
import handling.world.party.MaplePartyCharacter;
import java.awt.*;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.api.ITargetScripting;
import scripting.v1.api.QuestRecord;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.helper.Scripting;
import tools.packet.CWVsContextOnMessagePackets;

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
    @Scripting
    public int getCharacterID() {
        return player.getId();
    }

    @Override
    @Scripting
    public String getCharacterName() {
        return player.getName();
    }

    @Override
    @Scripting
    public int getGender() {
        return player.getGender();
    }

    @Override
    @Scripting
    public int getHair() {
        return player.getHair();
    }

    @Override
    @Scripting
    public int getFace() {
        return player.getFace();
    }

    @Override
    @Scripting
    public int nLevel() {
        return player.getLevel();
    }

    @Override
    @Scripting
    public int nJob() {
        return player.getJob().getId();
    }

    @Override
    @Scripting
    public boolean changeJob(int job) {
        player.changeJob(job);
        return true;
    }

    @Override
    @Scripting
    public boolean setJob(int job) {
        player.changeJob(job);
        return true;
    }

    @Override
    @Scripting
    public int nSTR() {
        return player.getStat().getStr();
    }

    @Override
    @Scripting
    public int incSTR(int value) {
        short previousSTR = player.getStat().getStr();
        player.setStat(MapleStat.STR, (short) (previousSTR + value));
        return nSTR();
    }

    @Override
    @Scripting
    public int nDEX() {
        return player.getStat().getDex();
    }

    @Override
    @Scripting
    public int incDEX(int value) {
        short previousDEX = player.getStat().getDex();
        player.setStat(MapleStat.DEX, (short) (previousDEX + value));
        return nDEX();
    }

    @Override
    @Scripting
    public int nINT() {
        return player.getStat().getInt();
    }

    @Override
    @Scripting
    public int incINT(int value) {
        short previousINT = player.getStat().getInt();
        player.setStat(MapleStat.INT, (short) (previousINT + value));
        return nINT();
    }

    @Override
    @Scripting
    public int nLUK() {
        return player.getStat().getLuk();
    }

    @Override
    @Scripting
    public int incLUK(short value) {
        short previousLUK = player.getStat().getLuk();
        player.setStat(MapleStat.LUK, (short) (previousLUK + value));
        return nLUK();
    }

    @Override
    @Scripting
    public int nHP() {
        return player.getStat().getHp();
    }

    @Override
    @Scripting
    public int incHP(int value) {
        int previousHP = player.getStat().getHp();
        player.getStat().setHp(previousHP + value);
        return nHP();
    }

    @Override
    @Scripting
    public int nMP() {
        return player.getStat().getMp();
    }

    @Override
    @Scripting
    public int incMP(int value) {
        int previousMP = player.getStat().getHp();
        player.getStat().setMp(previousMP + value);
        return nMP();
    }

    @Override
    @Scripting
    public int incMHP(int value, int other) {
        int previousMaxHP = player.getStat().getMaxHp();
        player.getStat().setMaxHp(previousMaxHP + value);
        return nHP();
    }

    @Override
    @Scripting
    public int incMMP(int value, int other) {
        int previousMaxMp = player.getStat().getMaxMp();
        player.getStat().setMaxMp(previousMaxMp + value);
        return nMP();
    }

    @Override
    @Scripting
    public int nAP() {
        return player.getRemainingAp();
    }

    @Override
    @Scripting
    public int incAP(int value) {
        player.gainAp(value);
        return nAP();
    }

    @Override
    @Scripting
    public int incAP(int value, int a) {
        player.gainAp(value);
        return nAP();
    }

    @Override
    @Scripting
    public int nSP() {
        return player.getRemainingSp();
    }

    @Override
    @Scripting
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
    @Scripting
    public int incSP(int value, int a) {
        return incSP(value);
    }

    @Override
    @Scripting
    public boolean isMaster() {
        return player.isGameMaster();
    }

    @Override
    @Scripting
    public boolean isSuperGM() {
        return player.isGameMaster();
    }

    @Override
    @Scripting
    public void message(String text) {
        player.dropMessage(5, text);
    }

    @Override
    @Scripting
    public void incEXP(int total, boolean show) {
        player.gainExp(total, show, show, show);
    }

    @Override
    @Scripting
    public void incEXP(int total, int show) {
        this.incEXP(total, show == 0);
    }

    @Override
    @Scripting
    public boolean isPartyBoss() {
        if (player.getParty() == null) {
            return false;
        }
        return player.getParty().getLeader().getId() == player.getId();
    }

    @Override
    @Scripting
    public boolean isOnParty() {
        return player.getParty() != null;
    }

    @Override
    @Scripting
    public int getPartyMembersCount() {
        if (!isOnParty()) {
            return 0;
        }
        return player.getParty().getMembers().size();
    }

    @Override
    @Scripting
    public int transferParty(int map, String portal, int option) {
        for (MaplePartyCharacter mate : player.getParty().getMembers()) {
            MapleCharacter chr =
                    WorldServer.getInstance().getStorage(client.getChannel()).getCharacterById(mate.getId());
            chr.changeMap(map, portal);
        }
        return 1;
    }

    @Override
    @Scripting
    public void playPortalSE() {
        sendPacket(MaplePacketCreator.showOwnBuffEffect(0, 7));
    }

    @Override
    @Scripting
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
    @Scripting
    public FieldScripting field() {
        return client.getPlayer().getMap().getField();
    }

    @Override
    @Scripting
    public int id() {
        return player.getMap().getId();
    }

    @Override
    @Scripting
    public int nMoney() {
        return player.getMeso();
    }

    @Override
    @Scripting
    public int incMoney(int meso, int show) {
        return incMoney(meso, meso == 1);
    }

    @Override
    @Scripting
    public int incMoney(int meso, boolean show) {
        if (meso < 0) {
            return -1;
        }
        player.gainMeso(meso, show);
        return nMoney();
    }

    @Override
    @Scripting
    public int decMoney(int meso, boolean show) {
        if (meso < 0) {
            return -1;
        }
        player.gainMeso(-meso, show);
        return nMoney();
    }

    @Override
    @Scripting
    public void set(String key, String value) {
        player.set(key, value);
    }

    @Override
    @Scripting
    public String get(String key) {
        String value = player.get(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    @Scripting
    public void setVar(String key, Object value) {
        player.addTemporaryData(key, value);
    }

    @Override
    @Scripting
    public Object getVar(String key) {
        Object value = player.getTemporaryData(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    @Scripting
    public void clearTemporaryData() {
        player.clearTemporaryData();
    }

    @Scripting
    public boolean isEvan() {
        return player.getJob().isEvan();
    }

    @Override
    @Scripting
    public boolean isDualBlade() {
        return player.getJob().isDualblade();
    }

    @Override
    @Scripting
    public boolean isNightWalker() {
        int job = nJob();
        return job == MapleJob.NIGHTWALKER1.getId()
                || job == MapleJob.NIGHTWALKER2.getId()
                || job == MapleJob.NIGHTWALKER3.getId()
                || job == MapleJob.NIGHTWALKER4.getId();
    }

    @Override
    @Scripting
    public boolean isAnyKindOfThief() {
        return isNightWalker() || isDualBlade() || (nJob() / 100) == 4;
    }

    @Override
    @Scripting
    public boolean isAran() {
        return player.getJob().isAran();
    }

    @Override
    @Scripting
    public boolean haveItem(int id) {
        return player.haveItem(id);
    }

    @Override
    @Scripting
    public FieldScripting getMap(final int map) {
        return new FieldScripting(getWarpMap(map));
    }

    @Override
    @Scripting
    public final byte getQuestStatus(final int id) {
        return player.getQuestStatus(id);
    }

    @Override
    @Scripting
    public final boolean isQuestActive(final int id) {
        return getQuestStatus(id) == ACTIVE_QUEST;
    }

    @Override
    @Scripting
    public final boolean isQuestFinished(final int id) {
        return getQuestStatus(id) == COMPLETE_QUEST;
    }

    @Override
    @Scripting
    public void completeQuest(int id, int npcId) {
        MapleQuest.getInstance(id).complete(getPlayer(), npcId);
    }

    @Override
    @Scripting
    public void forfeitQuest(int id) {
        MapleQuest.getInstance(id).forfeit(getPlayer());
    }

    @Override
    @Scripting
    public void forceCompleteQuest(final int id, int npcId) {
        MapleQuest.getInstance(id).forceComplete(getPlayer(), npcId);
    }

    @Override
    @Scripting
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
