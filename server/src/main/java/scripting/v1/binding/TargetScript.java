package scripting.v1.binding;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.Messages;
import handling.channel.ChannelServer;
import handling.world.World;
import handling.world.party.MaplePartyCharacter;
import scripting.v1.dispatch.PacketDispatcher;
import scripting.v1.event.EventCenter;
import scripting.v1.event.EventInstance;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.packet.CWVsContextOnMessagePackets;

import java.awt.*;

public class TargetScript extends PlayerInteractionScript {

  private static final int COMPLETE_QUEST = 2;
  private static final int ACTIVE_QUEST = 1;

  public TargetScript(MapleClient client, PacketDispatcher dispatcher) {
    super(client, dispatcher);
  }

  public int nCharacterID() {
    return player.getId();
  }

  public String sCharacterName() {
    return player.getName();
  }

  public int nGender() {
    return player.getGender();
  }

  public int nHair() {
    return player.getHair();
  }

  public int nFace() {
    return player.getFace();
  }

  public int nLevel() {
    return player.getLevel();
  }

  public int nJob() {
    return player.getJob();
  }

  public boolean changeJob(int job) {
    player.changeJob(job);
    return true;
  }

  public int nSTR() {
    return player.getStat().getStr();
  }

  public int incSTR(int value) {
    short previousSTR = player.getStat().getStr();
    player.setstat((byte) 1, (short) (previousSTR + value));
    return nSTR();
  }

  public int nDEX() {
    return player.getStat().getDex();
  }

  public int incDEX(int value) {
    short previousDEX = player.getStat().getDex();
    player.setstat((byte) 2, (short) (previousDEX + value));
    return nDEX();
  }

  public int nINT() {
    return player.getStat().getInt();
  }

  public int incINT(int value) {
    short previousINT = player.getStat().getInt();
    player.setstat((byte) 3, (short) (previousINT + value));
    return nINT();
  }

  public int nLUK() {
    return player.getStat().getLuk();
  }

  public int incLUK(short value) {
    short previousLUK = player.getStat().getLuk();
    player.setstat((byte) 4, (short) (previousLUK + value));
    return nLUK();
  }

  public int nHP() {
    return player.getStat().getHp();
  }

  public int incHP(int value) {
    int previousHP = player.getStat().getHp();
    player.getStat().setHp(previousHP + value);
    return nHP();
  }

  public int nMP() {
    return player.getStat().getMp();
  }

  public int incMP(int value) {
    int previousMP = player.getStat().getHp();
    player.getStat().setMp(previousMP + value);
    return nMP();
  }

  public int nAP() {
    return player.getRemainingAp();
  }

  public int incAP(int value) {
    player.gainAp(value);
    return nAP();
  }

  public int incAP(int value, int a) {
    player.gainAp(value);
    return nAP();
  }

  public int nSP() {
    return player.getRemainingSp();
  }

  public int incSP(int value) {
    if (player.isEvan()) {
      player.addEvanSP(value);
    } else {
      player.gainSp(value);
    }
    sendPacket(CWVsContextOnMessagePackets.onIncSpMessage(player.getJobValue(), value));
    return nSP();
  }
  public int incSP(int value, int a) {
    return incSP(value);
  }

  public boolean isMaster() {
    return player.isGM();
  }

  public boolean isSuperGM() {
    return player.isGM();
  }

  public void message(String text) {
    player.dropMessage(5, text);
  }

  public void incEXP(int total, boolean show) {
    player.gainExp(total, show, show, show);
  }

  public void incEXP(int total, int show) {
    this.incEXP(total, show == 0);
  }

  public boolean isPartyBoss() {
    if (player.getParty() == null) {
      return false;
    }
    return player.getParty().getLeader().getId() == player.getId();
  }

  public boolean isOnParty() {
    return player.getParty() != null;
  }

  public int getPartyMembersCount() {
    if (!isOnParty()) {
      return 0;
    }
    return player.getParty().getMembers().size();
  }

  public int transferParty(int map, String portal, int option) {
    for (MaplePartyCharacter mate : player.getParty().getMembers()) {
      MapleCharacter chr = World.getStorage(client.getChannel()).getCharacterById(mate.getId());
      chr.changeMap(map, portal);
    }
    return 1;
  }

  public void playPortalSE() {
    sendPacket(MaplePacketCreator.showOwnBuffEffect(0, 7));
  }

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

  public FieldScript field() {
    return client.getPlayer().getMap().getField();
  }

  public int fieldID() {
    return player.getMap().getId();
  }

  public int nMoney() {
    return player.getMeso();
  }

  public int incMoney(int meso, int show) {
    return incMoney(meso, meso == 1);
  }


  public int incMoney(int meso, boolean show) {
    if (meso < 0) {
      return -1;
    }
    player.gainMeso(meso, show);
    return nMoney();
  }

  public int decMoney(int meso, boolean show) {
    if (meso < 0) {
      return -1;
    }
    player.gainMeso(-meso, show);
    return nMoney();
  }

  public void set(String key, String value) {
    player.set(key, value);
  }

  public String get(String key) {
    String value = player.get(key);
    if (value == null) {
      return "";
    }
    return value;
  }

  public void setVar(String key, Object value) {
    player.addTemporaryData(key, value);
  }

  public Object getVar(String key) {
    Object value = player.getTemporaryData(key);
    if (value == null) {
      return "";
    }
    return value;
  }

  public void clearTemporaryData() {
    player.clearTemporaryData();
  }

  public EventCenter getEventCenter() {
    return getChannelServer().getEventCenter();
  }

  public boolean isEvan() {
    return player.isEvan();
  }

  public boolean isDualBlade() {
    return player.isDualblade();
  }

  public boolean isNightWalker() {
    int job = nJob();
    return job == MapleJob.NIGHTWALKER1.getId() || job == MapleJob.NIGHTWALKER2.getId() || job == MapleJob.NIGHTWALKER3.getId() || job == MapleJob.NIGHTWALKER4.getId();
  }

  public boolean isAnyKindOfThief() {
    return isNightWalker() || isDualBlade() || (nJob() / 100) == 4;
  }

  public boolean isAran() {
    return player.isAran();
  }

  public boolean haveItem(int id) {
    return player.haveItem(id);
  }

  public EventInstance getEvent() {
    return player.getNewEventInstance();
  }

  private MapleMap getWarpMap(final int map) {
    return ChannelServer.getInstance(client.getChannel()).getMapFactory().getMap(map);
  }

  public FieldScript getMap(final int map) {
    return new FieldScript(getWarpMap(map));
  }

  public final byte getQuestStatus(final int id) {
    return player.getQuestStatus(id);
  }

  public final boolean isQuestActive(final int id) {
    return getQuestStatus(id) == ACTIVE_QUEST;
  }

  public final boolean isQuestFinished(final int id) {
    return getQuestStatus(id) == COMPLETE_QUEST;
  }

  public void completeQuest(int id, int npcId) {
    MapleQuest.getInstance(id).complete(getPlayer(), npcId);
  }

  public void forfeitQuest(int id) {
    MapleQuest.getInstance(id).forfeit(getPlayer());
  }

  public void forceCompleteQuest(final int id, int npcId) {
    MapleQuest.getInstance(id).forceComplete(getPlayer(), npcId);
  }

  public void changeMusic(String music) {
    sendPacket(MaplePacketCreator.musicChange(music));
  }

  public Messages messages() {
    return player.getMessages();
  }

}
