/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scripting;

import client.*;
import client.inventory.*;
import constants.GameConstants;
import constants.JobConstants;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.channel.handler.utils.HiredMerchantHandlerUtils;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import server.*;
import server.Timer.CloneTimer;
import server.gachapon.GachaponFactory;
import server.gachapon.GachaponLocation;
import server.gachapon.GachaponMachine;
import server.gachapon.GachaponReward;
import server.maps.Event_DojoAgent;
import server.maps.Event_PyramidSubway;
import server.maps.MapleMap;
import server.maps.SpeedRunType;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Triple;
import tools.packet.CWVsContextOnMessagePackets;
import tools.packet.PlayerShopPacket;

import javax.script.Invocable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class NPCConversationManager extends AbstractPlayerInteraction {

  private MapleClient c;
  private int npc, questid;
  private String getText, fileName = null;
  private byte type; // -1 = NPC, 0 = start quest, 1 = end quest
  private byte lastMsg = -1;
  public boolean pendingDisposal = false;
  private Invocable iv;

  private HashMap<String, String> properties = new HashMap<>();

  public NPCConversationManager(MapleClient c, int npc, int questid, byte type, Invocable iv) {
    super(c, npc, questid);
    this.c = c;
    this.npc = npc;
    this.questid = questid;
    this.type = type;
    this.iv = iv;
  }

  public NPCConversationManager(MapleClient c, int npc, int questid, byte type, Invocable iv, String fileName) {
    super(c, npc, questid);
    this.c = c;
    this.npc = npc;
    this.questid = questid;
    this.type = type;
    this.iv = iv;
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  public Invocable getIv() {
    return iv;
  }

  public int getNpc() {
    return npc;
  }

  public int getQuest() {
    return questid;
  }

  public byte getType() {
    return type;
  }

  public void safeDispose() {
    pendingDisposal = true;
  }

  public void dispose() {
    NPCScriptManager.getInstance().dispose(c);
  }

  public void askMapSelection(final String sel) {
    if (lastMsg > -1) {
      return;
    }
    c.getSession().write(MaplePacketCreator.getMapSelection(npc, sel));
    lastMsg = 0xF;
  }

  public void debug(String message) {
    System.out.println(message);
  }

  public void sendNext(String text) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // sendNext will dc otherwise!
      sendSimple(text);
      return;
    }
    getClient().sendPacket(ScriptMan.OnSay(ScriptMan.NpcReplayedByNpc, npc, (byte) 0, text, false, true));
    lastMsg = 0;
  }

  public void sendNextS(String text, byte type) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimpleS(text, type);
      return;
    }
    c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", type));
    lastMsg = 0;
  }

  public void sendPrev(String text) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimple(text);
      return;
    }
    getClient().sendPacket(ScriptMan.OnSay(ScriptMan.NpcReplayedByNpc, npc, (byte) 0, text, true, false));
    lastMsg = 0;
  }

  public void sendPrevS(String text, byte type) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimpleS(text, type);
      return;
    }
    c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", type));
    lastMsg = 0;
  }

  public void sendNextPrev(String text) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimple(text);
      return;
    }
    getClient().sendPacket(ScriptMan.OnSay(ScriptMan.NpcReplayedByNpc, npc, (byte) 0, text, true, true));
    lastMsg = 0;
  }

  public void sendNextNPC(String text, byte type, int OtherNPC) {
    sendNPCIconChat(text, type, OtherNPC, "00 01");
  }

  public void sendPrevNPC(String text, byte type, int OtherNPC) {
    sendNPCIconChat(text, type, OtherNPC, "01 00");
  }

  public void sendNextPrevNPC(String text, byte type, int OtherNPC) {
    sendNPCIconChat(text, type, OtherNPC, "01 01");
  }

  public void sendNPCIconChat(String text, byte type, int OtherNPC, String endBytes) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimple(text);
      return;
    }
    c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, endBytes, type, OtherNPC));
    lastMsg = 0;
  }

  public void PlayerToNpc(String text) {
    sendNextPrevS(text, (byte) 3);
  }

  public void sendNextPrevS(String text) {
    sendNextPrevS(text, (byte) 3);
  }

  public void sendNextPrevS(String text, byte type) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimpleS(text, type);
      return;
    }
    c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", type));
    lastMsg = 0;
  }

  public void sendOk(String text) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimple(text);
      return;
    }
    getClient().sendPacket(ScriptMan.OnSay(ScriptMan.NpcReplayedByNpc, npc, (byte) 0, text, false, false));
    lastMsg = 0;
  }

  public void sendOkS(String text, byte type) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimpleS(text, type);
      return;
    }
    c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", type));
    lastMsg = 0;
  }

  public void sendYesNo(String text) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimple(text);
      return;
    }
    getClient().sendPacket(ScriptMan.OnAskYesNo(ScriptMan.NpcReplayedByNpc, npc, (byte) 0, text));
    lastMsg = 2;
  }

  public void sendYesNoS(String text, byte type) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimpleS(text, type);
      return;
    }
    c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 2, text, "", type));
    lastMsg = 2;
  }

  public void sendAcceptDecline(String text) {
    askAcceptDecline(text);
  }

  public void sendAcceptDeclineNoESC(String text) { // doesn't work..let it
    // fallback..
    askAcceptDecline(text);
  }

  public void askAcceptDecline(String text) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimple(text);
      return;
    }
    getClient().sendPacket(ScriptMan.OnAskAccept(ScriptMan.NpcReplayedByNpc, npc, (byte) 0, text));
    lastMsg = 0x0D;
  }

  public void askAvatar(String text, int... args) {
    if (lastMsg > -1) {
      return;
    }
    c.getSession().write(MaplePacketCreator.getNPCTalkStyle(npc, text, args));
    lastMsg = 8;
  }

  public void sendSimple(String text) {
    if (lastMsg > -1) {
      return;
    }
    if (!text.contains("#L")) { // sendSimple will dc otherwise!
      sendNext(text);
      return;
    }
    getClient().sendPacket(ScriptMan.OnAskMenu(ScriptMan.NpcReplayedByNpc, npc, (byte) 0, text));
    lastMsg = 5;
  }

  public void sendSimpleS(String text, byte type) {
    if (lastMsg > -1) {
      return;
    }
    if (!text.contains("#L")) { // sendSimple will dc otherwise!
      sendNextS(text, type);
      return;
    }
    c.getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 5, text, "", (byte) type));
    lastMsg = 5;
  }

  public void sendStyle(String text, int styles[]) {
    if (lastMsg > -1) {
      return;
    }
    getClient().sendPacket(ScriptMan.OnAskAvatar(ScriptMan.NpcReplayedByNpc, npc, text, styles));
    lastMsg = 8;
  }

  public void sendGetNumber(String text, int def, int min, int max) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimple(text);
      return;
    }
    getClient().sendPacket(ScriptMan.OnAskNumber(ScriptMan.NpcReplayedByNpc, npc, (byte) 0, text, def, min, max));
    lastMsg = 4;
  }

  public void sendGetText(String text) {
    if (lastMsg > -1) {
      return;
    }
    if (text.contains("#L")) { // will dc otherwise!
      sendSimple(text);
      return;
    }
    sendGetText(text, "", 0, 0);
    lastMsg = 3;
  }

  public void setGetText(String text) {
    this.getText = text;
  }

  public String getText() {
    return getText;
  }

  public void setHair(int hair) {
    getPlayer().setHair(hair);
    getPlayer().updateSingleStat(MapleStat.HAIR, hair);
    getPlayer().equipChanged();
  }

  public void setFace(int face) {
    getPlayer().setFace(face);
    getPlayer().updateSingleStat(MapleStat.FACE, face);
    getPlayer().equipChanged();
  }

  public void setSkin(int color) {
    getPlayer().setSkinColor((byte) color);
    getPlayer().updateSingleStat(MapleStat.SKIN, color);
    getPlayer().equipChanged();
  }

  public void createItem(int itemId, short quantity) {// TODO: Remove from
    // here..
    IItem item;
    byte flag = 0;
    flag |= ItemFlag.SPIKES.getValue();
    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
      item = ii.randomizeStats((Equip) ii.getEquipById(itemId));
      item.setFlag(flag);
    } else {
      item = new client.inventory.Item(itemId, (byte) 0, quantity, (byte) 0);
      item.setFlag(flag);
    }
    MapleInventoryManipulator.addbyItem(c, item);
  }

  public int setRandomAvatar(int ticket, int... args_all) {
    if (!haveItem(ticket)) {
      return -1;
    }
    gainItem(ticket, (short) -1);

    int args = args_all[Randomizer.nextInt(args_all.length)];
    if (args < 100) {
      c.getPlayer().setSkinColor((byte) args);
      c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
    } else if (args < 30000) {
      c.getPlayer().setFace(args);
      c.getPlayer().updateSingleStat(MapleStat.FACE, args);
    } else {
      c.getPlayer().setHair(args);
      c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
    }
    c.getPlayer().equipChanged();

    return 1;
  }

  public int setAvatar(int ticket, int args) {
    if (!haveItem(ticket)) {
      return -1;
    }
    gainItem(ticket, (short) -1);

    if (args < 100) {
      c.getPlayer().setSkinColor((byte) args);
      c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
    } else if (args < 30000) {
      c.getPlayer().setFace(args);
      c.getPlayer().updateSingleStat(MapleStat.FACE, args);
    } else {
      c.getPlayer().setHair(args);
      c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
    }
    c.getPlayer().equipChanged();

    return 1;
  }

  public void sendStorage() {
    c.getPlayer().setConversation(4);
    c.getPlayer().getStorage().sendStorage(c, npc);
  }

  public void openShop(int id) {
    MapleShopFactory.getInstance().getShop(id).sendShop(c);
  }

  public int gainGachaponItem(int id, int quantity) {
    return gainGachaponItem(id, quantity, "");
  }

  public int gainGachaponItem(int mapId, boolean isRemote) {
    GachaponMachine gachaponMachine;
    GachaponLocation location = GachaponLocation.valueOf(mapId);
    if (location == null) {
      return -1;
    }
    gachaponMachine = GachaponFactory.getInstance();
    GachaponReward reward = (GachaponReward) gachaponMachine.getReward(location);
    final IItem item = MapleInventoryManipulator.addbyId_Gachapon(c, reward.getId(), (short) reward.getQuantity());
    if (item == null) {
      return -1;
    }
    boolean haveGachaTicket = haveItem(5220000);
    boolean haveRemoteGachaTicket = haveItem(5451000);

    if (isRemote && !haveRemoteGachaTicket) {
      return -1;
    }
    if (!isRemote && !haveGachaTicket) {
      return -1;
    }
    if (isRemote && haveRemoteGachaTicket) {
      gainItem(5451000, (short) -1);
    } else {
      gainItem(5220000, (short) -1);
    }

    c.getSession().write(MaplePacketCreator.getShowItemGain(item.getItemId(), (short) reward.getQuantity(), true));
    if (reward.isRare()) {
      if (isRemote) {
        World.Broadcast.broadcastMessage(MaplePacketCreator
            .getGachaponMega(c.getPlayer().getName(), "Remote", item, (byte) 0));
      } else {
        World.Broadcast.broadcastMessage(
            MaplePacketCreator.getGachaponMega(c.getPlayer().getName(), c.getPlayer().getMap().getMapName(), item, (byte) 0));
      }

    }

    return reward.getId();
  }

  public int gainGachaponItem(int id, int quantity, final String msg) {
    try {
      if (!haveItem(id)) {
        return -1;
      }
      if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
        return -1;
      }
      final IItem item = MapleInventoryManipulator.addbyId_Gachapon(c, id, (short) quantity);

      if (item == null) {
        return -1;
      }
      final byte rareness = GameConstants.gachaponRareItem(item.getItemId());
      if (rareness >= 0) {
        String title = "[" + c.getPlayer().getName() + "] ";
        World.Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega(title, msg, item, rareness));
      }
      return item.getItemId();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1;
  }

  public void changeJob(int job) {
    c.getPlayer().changeJob(job);
  }

  public void changeJobById(int job) {
    c.getPlayer().changeJob(job);
  }

  public void startOriginalQuest(int id) {
    MapleQuest.getInstance(id).start(getPlayer(), npc);
  }

  public void completeQuest(int id) {
    MapleQuest.getInstance(id).complete(getPlayer(), npc);
  }

  public void completeQuest(int id, int npcId) {
    MapleQuest.getInstance(id).complete(getPlayer(), npcId);
  }

  public void forfeitQuest(int id) {
    MapleQuest.getInstance(id).forfeit(getPlayer());
  }

  public void forceStartQuest() {
    MapleQuest.getInstance(questid).forceStart(getPlayer(), getNpc(), null);
  }

  public void startQuest(int id) {
    MapleQuest.getInstance(id).forceStart(getPlayer(), getNpc(), null);
  }

  public void forceStartQuest(int id) {
    MapleQuest.getInstance(id).forceStart(getPlayer(), getNpc(), null);
  }

  public void forceStartQuest(String customData) {
    MapleQuest.getInstance(questid).forceStart(getPlayer(), getNpc(), customData);
  }

  public void forceCompleteQuest() {
    MapleQuest.getInstance(questid).forceComplete(getPlayer(), getNpc());

  }

  public void forceCompleteQuest(final int id) {
    MapleQuest.getInstance(id).forceComplete(getPlayer(), getNpc());
    c.getPlayer().getClient().getSession().write(MaplePacketCreator.showSpecialEffect(9)); // Quest completion
    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showSpecialEffect(getPlayer().getId(), 9), false);
  }

  public void forceCompleteQuest(final int id, int npcId) {
    MapleQuest.getInstance(id).forceComplete(getPlayer(), npcId);
  }

  public String getQuestCustomData() {
    return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(questid)).getCustomData();
  }

  public void setQuestCustomData(String customData) {
    getPlayer().getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(customData);
  }

  public int getMeso() {
    return getPlayer().getMeso();
  }

  public void gainAp(final int amount) {
    c.getPlayer().gainAp((short) amount);
  }

  public void expandInventory(byte type, int amt) {
    c.getPlayer().expandInventory(type, amt);
  }

  public void unequipEverything() {
    MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
    MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
    List<Short> ids = new LinkedList<Short>();
    for (IItem item : equipped.list()) {
      ids.add(item.getPosition());
    }
    for (short id : ids) {
      MapleInventoryManipulator.unequip(getC(), id, equip.getNextFreeSlot());
    }
  }

  public final void clearSkills() {
    Map<ISkill, SkillEntry> skills = getPlayer().getSkills();
    for (Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
      getPlayer().changeSkillLevel(skill.getKey(), (byte) 0, (byte) 0);
    }
  }

  // Map.wz\Effect
  public void showEffect(boolean broadcast, String effect) {
    if (broadcast) {
      c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
    } else {
      c.getSession().write(MaplePacketCreator.showEffect(effect));
    }
  }

  // Sound.wz\Field
  public void playSound(boolean broadcast, String sound) {
    if (broadcast) {
      c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound(sound));
    } else {
      c.getSession().write(MaplePacketCreator.playSound(sound));
    }
  }

  public void environmentChange(boolean broadcast, String env) {
    if (broadcast) {
      c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(env, 2));
    } else {
      c.getSession().write(MaplePacketCreator.environmentChange(env, 2));
    }
  }

  public void updateBuddyCapacity(int capacity) {
    c.getPlayer().setBuddyCapacity((byte) capacity);
  }

  public int getBuddyCapacity() {
    return c.getPlayer().getBuddyCapacity();
  }

  public int partyMembersInMap() {
    int inMap = 0;
    for (MapleCharacter char2 : getPlayer().getMap().getCharactersThreadsafe()) {
      if (char2.getParty() == getPlayer().getParty()) {
        inMap++;
      }
    }
    return inMap;
  }

  public List<MapleCharacter> getPartyMembers() {
    if (getPlayer().getParty() == null) {
      return null;
    }
    List<MapleCharacter> chars = new LinkedList<MapleCharacter>(); // creates
    // an
    // empty
    // array
    // full
    // of
    // shit..
    for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
      for (ChannelServer channel : ChannelServer.getAllInstances()) {
        MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
        if (ch != null) { // double check <3
          chars.add(ch);
        }
      }
    }
    return chars;
  }

  public void warpPartyWithExp(int mapId, int exp) {
    MapleMap target = getMap(mapId);
    for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
      MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
      if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null)
          || curChar.getEventInstance() == getPlayer().getEventInstance()) {
        curChar.changeMap(target, target.getPortal(0));
        curChar.gainExp(exp, true, false, true);
      }
    }
  }

  public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
    MapleMap target = getMap(mapId);
    for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
      MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
      if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null)
          || curChar.getEventInstance() == getPlayer().getEventInstance()) {
        curChar.changeMap(target, target.getPortal(0));
        curChar.gainExp(exp, true, false, true);
        curChar.gainMeso(meso, true);
      }
    }
  }

  public MapleSquad getSquad(String type) {
    MapleSquad squad = c.getChannelServer().getMapleSquad(type);
    return squad;
  }

  public int getSquadAvailability(String type) {
    final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
    if (squad == null) {
      return -1;
    }
    return squad.getStatus();
  }

  public boolean registerSquad(String type, int minutes, String startText) {
    final MapleSquad squad = new MapleSquad(c.getChannel(), type, c.getPlayer(), minutes * 60 * 1000);
    final boolean ret = c.getChannelServer().addMapleSquad(squad, type);
    if (ret) {
      final MapleMap map = c.getPlayer().getMap();

      map.broadcastMessage(MaplePacketCreator.getClock(minutes * 60));
      map.broadcastMessage(MaplePacketCreator.serverNotice(6, c.getPlayer().getName() + startText));
    } else {
      squad.clear();
    }
    return ret;
  }

  public boolean getSquadList(String type, byte type_) {
    final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
    if (squad == null) {
      return false;
    }
    if (type_ == 0 || type_ == 3) { // Normal viewing
      sendNext(squad.getSquadMemberString(type_));
    } else if (type_ == 1) { // Squad Leader banning, Check out banned
      // participant
      sendSimple(squad.getSquadMemberString(type_));
    } else if (type_ == 2) {
      if (squad.getBannedMemberSize() > 0) {
        sendSimple(squad.getSquadMemberString(type_));
      } else {
        sendNext(squad.getSquadMemberString(type_));
      }
    }
    return true;
  }

  public byte isSquadLeader(String type) {
    final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
    if (squad == null) {
      return -1;
    } else {
      if (squad.getLeader() != null && squad.getLeader().getId() == c.getPlayer().getId()) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  public boolean reAdd(String eim, String squad) {
    EventInstanceManager eimz = getDisconnected(eim);
    MapleSquad squadz = getSquad(squad);
    if (eimz != null && squadz != null) {
      squadz.reAddMember(getPlayer());
      eimz.registerPlayer(getPlayer());
      return true;
    }
    return false;
  }

  public void banMember(String type, int pos) {
    final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
    if (squad != null) {
      squad.banMember(pos);
    }
  }

  public void acceptMember(String type, int pos) {
    final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
    if (squad != null) {
      squad.acceptMember(pos);
    }
  }

  public int addMember(String type, boolean join) {
    final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
    if (squad != null) {
      return squad.addMember(c.getPlayer(), join);
    }
    return -1;
  }

  public byte isSquadMember(String type) {
    final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
    if (squad == null) {
      return -1;
    } else {
      if (squad.getMembers().contains(c.getPlayer())) {
        return 1;
      } else if (squad.isBanned(c.getPlayer())) {
        return 2;
      } else {
        return 0;
      }
    }
  }

  public void resetReactors() {
    getPlayer().getMap().resetReactors();
  }

  public void genericGuildMessage(int code) {
    c.getSession().write(MaplePacketCreator.genericGuildMessage((byte) code));
  }

  public void disbandGuild() {
    final int gid = c.getPlayer().getGuildId();
    if (gid <= 0 || c.getPlayer().getGuildRank() != 1) {
      return;
    }
    World.Guild.disbandGuild(gid);
  }

  public void increaseGuildCapacity() {
    if (c.getPlayer().getMeso() < 5000000) {
      c.getSession().write(MaplePacketCreator.serverNotice(1, "You do not have enough mesos."));
      return;
    }
    final int gid = c.getPlayer().getGuildId();
    if (gid <= 0) {
      return;
    }
    World.Guild.increaseGuildCapacity(gid);
    c.getPlayer().gainMeso(-5000000, true, false, true);
  }

  public void displayGuildRanks() {
    c.getSession().write(MaplePacketCreator.showGuildRanks(npc, MapleGuildRanking.getInstance().getRank()));
  }

  public boolean removePlayerFromInstance() {
    if (c.getPlayer().getEventInstance() != null) {
      c.getPlayer().getEventInstance().removePlayer(c.getPlayer());
      return true;
    }
    return false;
  }

  public boolean isPlayerInstance() {
    if (c.getPlayer().getEventInstance() != null) {
      return true;
    }
    return false;
  }

  public void changeStat(byte slot, int type, short amount) {
    Equip sel = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
    switch (type) {
      case 0:
        sel.setStr(amount);
        break;
      case 1:
        sel.setDex(amount);
        break;
      case 2:
        sel.setInt(amount);
        break;
      case 3:
        sel.setLuk(amount);
        break;
      case 4:
        sel.setHp(amount);
        break;
      case 5:
        sel.setMp(amount);
        break;
      case 6:
        sel.setWatk(amount);
        break;
      case 7:
        sel.setMatk(amount);
        break;
      case 8:
        sel.setWdef(amount);
        break;
      case 9:
        sel.setMdef(amount);
        break;
      case 10:
        sel.setAcc(amount);
        break;
      case 11:
        sel.setAvoid(amount);
        break;
      case 12:
        sel.setHands(amount);
        break;
      case 13:
        sel.setSpeed(amount);
        break;
      case 14:
        sel.setJump(amount);
        break;
      case 15:
        sel.setUpgradeSlots((byte) amount);
        break;
      case 16:
        sel.setViciousHammer((byte) amount);
        break;
      case 17:
        sel.setLevel((byte) amount);
        break;
      case 18:
        sel.setEnhance((byte) amount);
        break;
      case 19:
        sel.setPotential1(amount);
        break;
      case 20:
        sel.setPotential2(amount);
        break;
      case 21:
        sel.setPotential3(amount);
        break;
      case 22:
        sel.setOwner(getText());
        break;
      default:
        break;
    }
    c.getPlayer().equipChanged();
  }

  public void giveMerchantMesos() {
    long mesos = 0;
    try {
      Connection con = (Connection) DatabaseConnection.getConnection();
      PreparedStatement ps = (PreparedStatement) con
          .prepareStatement("SELECT * FROM hiredmerchants WHERE merchantid = ?");
      ps.setInt(1, getPlayer().getId());
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
        rs.close();
        ps.close();
      } else {
        mesos = rs.getLong("mesos");
      }
      rs.close();
      ps.close();

      ps = (PreparedStatement) con.prepareStatement("UPDATE hiredmerchants SET mesos = 0 WHERE merchantid = ?");
      ps.setInt(1, getPlayer().getId());
      ps.executeUpdate();
      ps.close();

    } catch (SQLException ex) {
      System.err.println("Error gaining mesos in hired merchant" + ex);
    }
    c.getPlayer().gainMeso((int) mesos, true);
  }

  public long getMerchantMesos() {
    long mesos = 0;
    try {
      Connection con = (Connection) DatabaseConnection.getConnection();
      PreparedStatement ps = (PreparedStatement) con
          .prepareStatement("SELECT * FROM hiredmerch WHERE characterid = ?");
      ps.setInt(1, getPlayer().getId());
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
        rs.close();
        ps.close();
      } else {
        mesos = rs.getLong("mesos");
      }
      rs.close();
      ps.close();
    } catch (SQLException ex) {
      System.err.println("Error gaining mesos in hired merchant" + ex);
    }
    return mesos;
  }

  public boolean hasMerchantExistance() {
    return World.hasMerchant(c.getPlayer().getAccountID());
  }

  public int getCurrentMesos() {
    return this.getPlayer().getMeso();
  }

  private static boolean isFullInventory(MapleInventory mi, List<IItem> list) {
    int total = (mi.getSlotLimit() - mi.getNumFreeSlot()) + list.size();
    boolean v = total > mi.getSlotLimit();
    return v;
  }

  public void openFredrick() {
    final MerchItemPackage pack = HiredMerchantHandlerUtils.loadItemFrom_Database(c.getPlayer().getId(),
        c.getPlayer().getAccountID());
    if (pack == null) {
      getPlayer().dropMessage(1, "You don't have anything to retrieve");
      return;
    }
    c.getSession().write(PlayerShopPacket.merchItemStore_ItemData(pack));
  }

  public int requestHiredItems() {
    final MerchItemPackage pack = HiredMerchantHandlerUtils.loadItemFrom_Database(c.getPlayer().getId(),
        c.getPlayer().getAccountID());

    if (pack == null) {
      return 0;
    }
    for (int i = 1; i < 5; i++) {
      if (isFullInventory(getPlayer().getInventory(MapleInventoryType.getByType((byte) i)), pack.getItems())) {
        getPlayer().dropMessage(1, MapleInventoryType.getByType((byte) i).name() + " inventory is full");
        dispose();
        return 2;
      }
    }

    if (HiredMerchantHandlerUtils.deletePackage(c.getPlayer().getId(), c.getPlayer().getAccountID(),
        pack.getPackageid())) {
      c.getPlayer().gainMeso(pack.getMesos(), false);
      for (IItem item : pack.getItems()) {
        MapleInventoryManipulator.addFromDrop(c, item, false);
      }
      return 1;
    } else {
      return 0;
    }
  }

  public void sendRepairWindow() {
    c.getSession().write(MaplePacketCreator.sendRepairWindow(npc));
  }

  public final int getDojoPoints() {
    return c.getPlayer().getDojo();
  }

  public final int getDojoRecord() {
    return c.getPlayer().getDojoRecord();
  }

  public void setDojoRecord(final boolean reset) {
    c.getPlayer().setDojoRecord(reset);
  }

  public boolean start_DojoAgent(final boolean dojo, final boolean party) {
    if (dojo) {
      return Event_DojoAgent.warpStartDojo(c.getPlayer(), party);
    }
    return Event_DojoAgent.warpStartAgent(c.getPlayer(), party);
  }

  public boolean start_PyramidSubway(final int pyramid) {
    if (pyramid >= 0) {
      return Event_PyramidSubway.warpStartPyramid(c.getPlayer(), pyramid);
    }
    return Event_PyramidSubway.warpStartSubway(c.getPlayer());
  }

  public boolean bonus_PyramidSubway(final int pyramid) {
    if (pyramid >= 0) {
      return Event_PyramidSubway.warpBonusPyramid(c.getPlayer(), pyramid);
    }
    return Event_PyramidSubway.warpBonusSubway(c.getPlayer());
  }

  public final short getSunshines() {
    return this.getChannelServer().getAramiaEvent().getSunsPercentage();
  }

  public void addSunshines(final int kegs) {
    this.getChannelServer().getAramiaEvent().giveSuns(c.getPlayer(), kegs);
  }

  public final MapleInventory getInventory(int type) {
    return c.getPlayer().getInventory(MapleInventoryType.getByType((byte) type));
  }

  public final MapleCarnivalParty getCarnivalParty() {
    return c.getPlayer().getCarnivalParty();
  }

  public final MapleCarnivalChallenge getNextCarnivalRequest() {
    return c.getPlayer().getNextCarnivalRequest();
  }

  public final MapleCarnivalChallenge getCarnivalChallenge(MapleCharacter chr) {
    return new MapleCarnivalChallenge(chr);
  }

  public void maxStats() {
    List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(2);
    c.getPlayer().getStat().setStr((short) 32767);
    c.getPlayer().getStat().setDex((short) 32767);
    c.getPlayer().getStat().setInt((short) 32767);
    c.getPlayer().getStat().setLuk((short) 32767);

    c.getPlayer().getStat().setMaxHp((short) 30000);
    c.getPlayer().getStat().setMaxMp((short) 30000);
    c.getPlayer().getStat().setHp((short) 30000);
    c.getPlayer().getStat().setMp((short) 30000);

    statup.add(new Pair<MapleStat, Integer>(MapleStat.STR, Integer.valueOf(32767)));
    statup.add(new Pair<MapleStat, Integer>(MapleStat.DEX, Integer.valueOf(32767)));
    statup.add(new Pair<MapleStat, Integer>(MapleStat.LUK, Integer.valueOf(32767)));
    statup.add(new Pair<MapleStat, Integer>(MapleStat.INT, Integer.valueOf(32767)));
    statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(30000)));
    statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(30000)));
    statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(30000)));
    statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(30000)));

    c.getSession().write(MaplePacketCreator.updatePlayerStats(statup, c.getPlayer().getJob()));
  }

  public Pair<String, Map<Integer, String>> getSpeedRun(String typ) {
    final SpeedRunType type = SpeedRunType.valueOf(typ);
    if (SpeedRunner.getInstance().getSpeedRunData(type) != null) {
      return SpeedRunner.getInstance().getSpeedRunData(type);
    }
    return new Pair<String, Map<Integer, String>>("", new HashMap<Integer, String>());
  }

  public boolean getSR(Pair<String, Map<Integer, String>> ma, int sel) {
    if (ma.getRight().get(sel) == null || ma.getRight().get(sel).length() <= 0) {
      dispose();
      return false;
    }
    sendOk(ma.getRight().get(sel));
    return true;
  }

  public Equip getEquip(int itemid) {
    return (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemid);
  }

  public void setExpiration(Object statsSel, long expire) {
    if (statsSel instanceof Equip) {
      ((Equip) statsSel).setExpiration(System.currentTimeMillis() + (expire * 24 * 60 * 60 * 1000));
    }
  }

  public void setLock(Object statsSel) {
    if (statsSel instanceof Equip) {
      Equip eq = (Equip) statsSel;
      if (eq.getExpiration() == -1) {
        eq.setFlag((byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
      } else {
        eq.setFlag((byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
      }
    }
  }

  public boolean addFromDrop(Object statsSel) {
    if (statsSel instanceof IItem) {
      final IItem it = (IItem) statsSel;
      return MapleInventoryManipulator.checkSpace(getClient(), it.getItemId(), it.getQuantity(), it.getOwner())
          && MapleInventoryManipulator.addFromDrop(getClient(), it, false);
    }
    return false;
  }

  public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type) {
    return replaceItem(slot, invType, statsSel, offset, type, false);
  }

  public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type, boolean takeSlot) {
    MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
    if (inv == null) {
      return false;
    }
    IItem item = getPlayer().getInventory(inv).getItem((byte) slot);
    if (item == null || statsSel instanceof IItem) {
      item = (IItem) statsSel;
    }
    if (offset > 0) {
      if (inv != MapleInventoryType.EQUIP) {
        return false;
      }
      Equip eq = (Equip) item;
      if (takeSlot) {
        if (eq.getUpgradeSlots() < 1) {
          return false;
        } else {
          eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() - 1));
        }
      }
      if (type.equalsIgnoreCase("Slots")) {
        eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() + offset));
      } else if (type.equalsIgnoreCase("Level")) {
        eq.setLevel((byte) (eq.getLevel() + offset));
      } else if (type.equalsIgnoreCase("Hammer")) {
        eq.setViciousHammer((byte) (eq.getViciousHammer() + offset));
      } else if (type.equalsIgnoreCase("STR")) {
        eq.setStr((short) (eq.getStr() + offset));
      } else if (type.equalsIgnoreCase("DEX")) {
        eq.setDex((short) (eq.getDex() + offset));
      } else if (type.equalsIgnoreCase("INT")) {
        eq.setInt((short) (eq.getInt() + offset));
      } else if (type.equalsIgnoreCase("LUK")) {
        eq.setLuk((short) (eq.getLuk() + offset));
      } else if (type.equalsIgnoreCase("HP")) {
        eq.setHp((short) (eq.getHp() + offset));
      } else if (type.equalsIgnoreCase("MP")) {
        eq.setMp((short) (eq.getMp() + offset));
      } else if (type.equalsIgnoreCase("WATK")) {
        eq.setWatk((short) (eq.getWatk() + offset));
      } else if (type.equalsIgnoreCase("MATK")) {
        eq.setMatk((short) (eq.getMatk() + offset));
      } else if (type.equalsIgnoreCase("WDEF")) {
        eq.setWdef((short) (eq.getWdef() + offset));
      } else if (type.equalsIgnoreCase("MDEF")) {
        eq.setMdef((short) (eq.getMdef() + offset));
      } else if (type.equalsIgnoreCase("ACC")) {
        eq.setAcc((short) (eq.getAcc() + offset));
      } else if (type.equalsIgnoreCase("Avoid")) {
        eq.setAvoid((short) (eq.getAvoid() + offset));
      } else if (type.equalsIgnoreCase("Hands")) {
        eq.setHands((short) (eq.getHands() + offset));
      } else if (type.equalsIgnoreCase("Speed")) {
        eq.setSpeed((short) (eq.getSpeed() + offset));
      } else if (type.equalsIgnoreCase("Jump")) {
        eq.setJump((short) (eq.getJump() + offset));
      } else if (type.equalsIgnoreCase("ItemEXP")) {
        eq.setItemEXP(eq.getItemEXP() + offset);
      } else if (type.equalsIgnoreCase("Expiration")) {
        eq.setExpiration((long) (eq.getExpiration() + offset));
      } else if (type.equalsIgnoreCase("Flag")) {
        eq.setFlag((byte) (eq.getFlag() + offset));
      }
      if (eq.getExpiration() == -1) {
        eq.setFlag((byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
      } else {
        eq.setFlag((byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
      }
      item = eq.copy();
    }
    MapleInventoryManipulator.removeFromSlot(getClient(), inv, (short) slot, item.getQuantity(), false);
    return MapleInventoryManipulator.addFromDrop(getClient(), item, false);
  }

  public boolean replaceItem(int slot, int invType, Object statsSel, int upgradeSlots) {
    return replaceItem(slot, invType, statsSel, upgradeSlots, "Slots");
  }

  public boolean isCash(final int itemId) {
    return MapleItemInformationProvider.getInstance().isCash(itemId);
  }

  public void buffGuild(final int buff, final int duration, final String msg) {
    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    if (ii.getItemEffect(buff) != null && getPlayer().getGuildId() > 0) {
      final MapleStatEffect mse = ii.getItemEffect(buff);
      for (ChannelServer cserv : ChannelServer.getAllInstances()) {
        for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
          if (chr.getGuildId() == getPlayer().getGuildId()) {
            mse.applyTo(chr, chr, true, null, duration);
            chr.dropMessage(5, "Your guild has gotten a " + msg + " buff.");
          }
        }
      }
    }
  }

  public boolean createAlliance(String alliancename) {
    MapleParty pt = c.getPlayer().getParty();
    MapleCharacter otherChar = c.getChannelServer().getPlayerStorage()
        .getCharacterById(pt.getMemberByIndex(1).getId());
    if (otherChar == null || otherChar.getId() == c.getPlayer().getId()) {
      return false;
    }
    try {
      return World.Alliance.createAlliance(alliancename, c.getPlayer().getId(), otherChar.getId(),
          c.getPlayer().getGuildId(), otherChar.getGuildId());
    } catch (Exception re) {
      re.printStackTrace();
      return false;
    }
  }

  public boolean addCapacityToAlliance() {
    try {
      final MapleGuild gs = World.Guild.getGuild(c.getPlayer().getGuildId());
      if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
        if (World.Alliance.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId()
            && World.Alliance.changeAllianceCapacity(gs.getAllianceId())) {
          gainMeso(-MapleGuildAlliance.CHANGE_CAPACITY_COST);
          return true;
        }
      }
    } catch (Exception re) {
      re.printStackTrace();
    }
    return false;
  }

  public boolean disbandAlliance() {
    try {
      final MapleGuild gs = World.Guild.getGuild(c.getPlayer().getGuildId());
      if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
        if (World.Alliance.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId()
            && World.Alliance.disbandAlliance(gs.getAllianceId())) {
          return true;
        }
      }
    } catch (Exception re) {
      re.printStackTrace();
    }
    return false;
  }

  public byte getLastMsg() {
    return lastMsg;
  }

  public final void setLastMsg(final byte last) {
    this.lastMsg = last;
  }

  public final void maxAllSkills() {
    for (ISkill skil : SkillFactory.getAllSkills()) {
      if (GameConstants.isApplicableSkill(skil.getId())) { // no
        // db/additionals/resistance
        // skills
        if (skil.getId() >= 22000000 && (GameConstants.isEvan((skil.getId() / 10000))
            || GameConstants.isResist((skil.getId() / 10000)))) {
          continue;
        } // only beginner skills are not maxed!
        if (skil.getId() < 10000 || ((skil.getId() / 10000) == 1000) || ((skil.getId() / 10000) == 2000)
            || ((skil.getId() / 10000) == 2001)) {
          teachSkill(skil.getId(), skil.getMaxLevel(), skil.getMaxLevel());
        }
      }
    }
  }

  public MapleJob getJobById(int id) {
    return MapleJob.getById(id);
  }

  public final void resetStats(int str, int dex, int z, int luk) {
    c.getPlayer().resetStats(str, dex, z, luk);
  }

  public final boolean dropItem(int slot, int invType, int quantity) {
    MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
    if (inv == null) {
      return false;
    }
    return MapleInventoryManipulator.drop(c, inv, (short) slot, (short) quantity, true);
  }

  public final List<Integer> getAllPotentialInfo() {
    return new ArrayList<Integer>(MapleItemInformationProvider.getInstance().getAllPotentialInfo().keySet());
  }

  public final String getPotentialInfo(final int id) {
    final List<StructPotentialItem> potInfo = MapleItemInformationProvider.getInstance().getPotentialInfo(id);
    final StringBuilder builder = new StringBuilder("#b#ePOTENTIAL INFO FOR ID: ");
    builder.append(id);
    builder.append("#n#k\r\n\r\n");
    int minLevel = 1, maxLevel = 10;
    for (StructPotentialItem item : potInfo) {
      builder.append("#eLevels ");
      builder.append(minLevel);
      builder.append("~");
      builder.append(maxLevel);
      builder.append(": #n");
      builder.append(item.toString());
      minLevel += 10;
      maxLevel += 10;
      builder.append("\r\n");
    }
    return builder.toString();
  }

  public final void sendRPS() {
    c.getSession().write(MaplePacketCreator.getRPSMode((byte) 8, -1, -1, -1));
  }

  public final void setQuestRecord(Object ch, final int questid, final String data) {
    ((MapleCharacter) ch).getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(data);
  }

  public final void doWeddingEffect(final Object ch) {
    final MapleCharacter chr = (MapleCharacter) ch;
    getMap().broadcastMessage(MaplePacketCreator.yellowChat(getPlayer().getName() + ", do you take " + chr.getName()
        + " as your wife and promise to stay beside her through all downtimes, crashes, and lags?"));
    CloneTimer.getInstance().schedule(new Runnable() {

      public void run() {
        if (chr == null || getPlayer() == null) {
          warpMap(680000500, 0);
        } else {
          getMap().broadcastMessage(MaplePacketCreator.yellowChat(chr.getName() + ", do you take "
              + getPlayer().getName()
              + " as your husband and promise to stay beside him through all downtimes, crashes, and lags?"));
        }
      }
    }, 10000);
    CloneTimer.getInstance().schedule(new Runnable() {

      public void run() {
        if (chr == null || getPlayer() == null) {
          if (getPlayer() != null) {
            setQuestRecord(getPlayer(), 160001, "3");
            setQuestRecord(getPlayer(), 160002, "0");
          } else if (chr != null) {
            setQuestRecord(chr, 160001, "3");
            setQuestRecord(chr, 160002, "0");
          }
          warpMap(680000500, 0);
        } else {
          setQuestRecord(getPlayer(), 160001, "2");
          setQuestRecord(chr, 160001, "2");
          sendNPCText(getPlayer().getName() + " and " + chr.getName()
              + ", I wish you two all the best on your journey together!", 9201002);
          getMap().startExtendedMapEffect("You may now kiss the bride, " + getPlayer().getName() + "!",
              5120006);
          if (chr.getGuildId() > 0) {
            World.Guild.guildPacket(chr.getGuildId(),
                MaplePacketCreator.sendMarriage(false, chr.getName()));
          }
          if (getPlayer().getGuildId() > 0) {
            World.Guild.guildPacket(getPlayer().getGuildId(),
                MaplePacketCreator.sendMarriage(false, getPlayer().getName()));
          }
        }
      }
    }, 20000); // 10 sec 10 sec
  }

  public String startSpeedQuiz() {
    if (c.getPlayer().getSpeedQuiz() != null) {
      c.getPlayer().setSpeedQuiz(null);
      return "Ahh..it seemed that something was broken. Please let the admins know about this issue right away!";
    }
    if (!ServerConstants.SPEED_QUIZ) {
      return "Oh dear! You came too late...The event had already ended! But oh well, everything has an ending. Come earlier next time!";
    }
    final String time = "";//getCQInfo(190011);
    final long now = System.currentTimeMillis();
    if (!time.equals("")) { // Contains data
      boolean can = (Long.parseLong(time) + 3600000) < now;
      if (!can) {
        int remaining = (int) ((((Long.parseLong(time) + 3600000) - now) / 1000) / 60);
        return "You've already tried the speed quiz in the past hour. Please come back again in " + remaining
            + " minutes.";
      }
    }
    c.getPlayer().setSpeedQuiz(new SpeedQuiz(c, npc)); // 0 mode
    return null;
  }

  public String populateKeymapValues() {
    if (GameConstants.isEvan(c.getPlayer().getJob())) {
      return "I'm sorry to say that since you're an Evan, you can't keep any skills as other jobs do not have any dragon. Hence, you cannot keep any skills.";
    }
    final Collection<Triple<Byte, Integer, Byte>> keymap = c.getPlayer().getKeymap();
    StringBuilder sb = new StringBuilder(
        "Which of the skills would you like to keep? Please note that you can only select the skills which are on your keymap.\r\n");
    Iterator<Triple<Byte, Integer, Byte>> itr = keymap.iterator();
    int sel = -1;
    while (itr.hasNext()) {
      Triple<Byte, Integer, Byte> key = itr.next();
      if (key.getLeft() == 1 && key.getRight() <= 0) {
        if (!JobConstants.isEvanSkill(key.getMid())) { // not evan
          // skills
          if (key.getMid() >= 30000000 || key.getMid() == 1004 || key.getMid() == 10001004
              || key.getMid() == 20001004 || key.getMid() == 20011004) {
            continue;
          }
          if (((key.getMid() / 10000 == 910) || (key.getMid() / 10000 == 900)) && !c.getPlayer().isGM()) { // is
            // gm
            // skills
            // but
            // person
            // not
            // gm
            continue;
          }
          if (GameConstants.getMountItem(key.getMid()) != 0 && key.getMid() != 5221006) { // special
            // mounts
            // and
            // skill
            // is
            // not
            // battleship
            continue;
          }
          sel++;
          sb.append("#L").append(sel).append("# #s").append(key.getMid()).append("#");
          if (sel % 4 == 0 && sel != 0) {
            sb.append("\r\n");
          } else {
            sb.append("\t");
          }
        }
      }
    }
    if (sel == -1) {
      return "Currently, you do not have any skills which can be kept as a reborn skill on your keymap.";
    }
    return sb.toString();
  }

  public int getSkillIdKey(final int selection) {
    if (GameConstants.isEvan(c.getPlayer().getJob())) {
      return -1;
    }
    final Collection<Triple<Byte, Integer, Byte>> keymap = c.getPlayer().getKeymap();
    Iterator<Triple<Byte, Integer, Byte>> itr = keymap.iterator();
    int sel = -1;
    while (itr.hasNext()) {
      Triple<Byte, Integer, Byte> key = itr.next();
      if (key.getLeft() == 1 && key.getRight() <= 0) {
        if (!JobConstants.isEvanSkill(key.getMid())) { // not evan
          // skills
          if (key.getMid() >= 30000000 || key.getMid() == 1004 || key.getMid() == 10001004
              || key.getMid() == 20001004 || key.getMid() == 20011004) {
            continue;
          }
          if (((key.getMid() / 10000 == 910) || (key.getMid() / 10000 == 900)) && !c.getPlayer().isGM()) { // is
            // gm
            // skills
            // but
            // person
            // not
            // gm
            continue;
          }
          if (GameConstants.getMountItem(key.getMid()) != 0 && key.getMid() != 5221006) { // special
            // mounts
            // and
            // skill
            // is
            // not
            // battleship
            continue;
          }
          sel++;
          if (sel == selection) {
            return key.getMid();
          }
        }
      }
    }
    return 0;
  }

  public void changeGuildName(final String name) {
    World.Guild.setGuildName(getPlayer().getGuildId(), name);
    if (getPlayer().getMap() == null) {
      return;
    }
    getPlayer().getMap().broadcastMessage(MaplePacketCreator.loadGuildName(getPlayer()));
    getPlayer().getMap().broadcastMessage(MaplePacketCreator.loadGuildIcon(getPlayer()));
  }

  public String EquipList() {
    final StringBuilder str = new StringBuilder();
    final MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
    int sel = 0;
    for (final IItem item : equip.list()) {
      str.append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l");
      sel++;
      if (sel % 4 == 0) {
        str.append("\r\n");
      } else {
        str.append("\t");
      }
    }
    return (sel == 0 ? null : str.toString());
  }

  public boolean randomMaxStatItem(final byte slot) {
    final IItem eu = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
    if (eu == null) {
      return false;
    }
    final Equip eq = (Equip) eu.copy();
    eq.setStr((short) (Randomizer.nextInt(15001) + 15000));
    eq.setDex((short) (Randomizer.nextInt(15001) + 15000));
    eq.setInt((short) (Randomizer.nextInt(15001) + 15000));
    eq.setLuk((short) (Randomizer.nextInt(15001) + 15000));
    if (eq.getWatk() > 0) {
      eq.setWatk((short) (Randomizer.nextInt(eq.getWatk() + 1)));
    }
    if (eq.getMatk() > 0) {
      eq.setMatk((short) (Randomizer.nextInt(eq.getMatk() + 1)));
    }
    if (eq.getUpgradeSlots() > 7) {
      eq.setUpgradeSlots((byte) 7); // Max is 7
    }
    eq.setItemEXP((short) 0);
    eq.setEnhance((byte) 0);
    eq.setPotential1((short) 0);
    eq.setPotential2((short) 0);
    eq.setPotential3((short) 0);
    eq.setOwner(getPlayer().getName());
    if (MapleInventoryManipulator.addFromDrop(c, eq, false)) {
      final List<Pair<MapleStat, Integer>> stat = new ArrayList<>(4);
      getPlayer().getStat().setStr((short) 4);
      getPlayer().getStat().setDex((short) 4);
      getPlayer().getStat().setInt((short) 4);
      getPlayer().getStat().setLuk((short) 4);
      stat.add(new Pair<>(MapleStat.STR, 4));
      stat.add(new Pair<>(MapleStat.DEX, 4));
      stat.add(new Pair<>(MapleStat.INT, 4));
      stat.add(new Pair<>(MapleStat.LUK, 4));
      getClient().getSession().write(MaplePacketCreator.updatePlayerStats(stat, false, getJob()));
      return true;
    }
    return false;
  }


  public void upgradeWepAtk(short amount) {
    final Equip nEquip = (Equip) getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
    final short watk = nEquip.getWatk();
    nEquip.setWatk((short) (watk + amount));
    getPlayer().forceReAddItem(nEquip, MapleInventoryType.EQUIPPED);
    // getPlayer().forceUpdateItem(MapleInventoryType.EQUIPPED, nEquip);
    if (watk >= 32767) {
      nEquip.setWatk((short) (32767));
    }
  }

  public String EquipList(MapleClient c) {
    StringBuilder str = new StringBuilder();
    MapleInventory equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP);
    List<String> stra = new LinkedList<String>();
    for (IItem item : equip.list()) {
      stra.add("#L" + item.getPosition() + "##v" + item.getItemId() + "##l");
    }
    for (String strb : stra) {
      str.append(strb);
    }
    return str.toString();
  }

  public void reloadChar() {
    getPlayer().getClient().getSession().write(MaplePacketCreator.getCharInfo(getPlayer()));
    getPlayer().getMap().removePlayer(getPlayer());
    getPlayer().getMap().addPlayer(getPlayer());
  }

  public void worldNotice(int type, String Header, String msg) {
    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(type, "[" + Header + "] " + msg + ""));
  }

  public short getJobId() {
    return getPlayer().getJob();
  }

  public short getLevel() {
    return getPlayer().getLevel();
  }

  public String getJobName(short jobid) {
    switch (jobid) {
      case 0:
        return "Beginner";
      case 100:
        return "Warrior";
      case 110:
        return "Fighter";
      case 111:
        return "Crusader";
      case 112:
        return "Hero";
      case 120:
        return "Page";
      case 121:
        return "White Knight";
      case 122:
        return "Paladin";
      case 130:
        return "Spear Man";
      case 131:
        return "Dragon Knight";
      case 132:
        return "Dark Knight";
      case 200:
        return "Magician";
      case 210:
        return "Fire/Poison Wizard";
      case 211:
        return "Fire/Poison Mage";
      case 212:
        return "Fire/Poison ArchMage";
      case 220:
        return "Ice/Lightning Wizard";
      case 221:
        return "Ice/Lightning Mage";
      case 222:
        return "Ice/Lightning ArchMage";
      case 230:
        return "Cleric";
      case 231:
        return "Priest";
      case 232:
        return "Bishop";
      case 300:
        return "Bowman";
      case 310:
        return "Hunter";
      case 311:
        return "Ranger";
      case 312:
        return "Bowmaster";
      case 320:
        return "Sniper";
      case 321:
        return "Crossbow Master";
      case 400:
        return "Thief";
      case 410:
        return "Assassin";
      case 411:
        return "Hermit";
      case 412:
        return "Night Lord";
      case 420:
        return "Bandit";
      case 421:
        return "Chief Bandit";
      case 422:
        return "Shadower";
      case 430:
        return "Blade Recruit";
      case 431:
        return "Blade Acolyte";
      case 432:
        return "Blade Specialist";
      case 433:
        return "Blade Lord";
      case 434:
        return "Blade Master";
      case 500:
        return "Pirate";
      case 510:
        return "Brawler";
      case 520:
        return "GunSlinger";
      case 511:
        return "Marauder";
      case 521:
        return "Outlaw";
      case 512:
        return "Buccaner";
      case 522:
        return "Corsair";
      case 1000:
        return "Noblesse";
      case 1100:
        return "Dawn Warrior Job 1";
      case 1110:
        return "Dawn Warrior Job 2";
      case 1111:
        return "Dawn Warrior Job 3";
      case 1112:
        return "Dawn Warrior Job 4";
      case 1200:
        return "Blaze Wizard Job 1";
      case 1210:
        return "Blaze Wizard Job 2";
      case 1211:
        return "Blaze Wizard Job 3";
      case 1212:
        return "Blaze Wizard Job 4";
      case 1300:
        return "Wind Archer Job 1";
      case 1310:
        return "Wind Archer Job 2";
      case 1311:
        return "Wind Archer Job 3";
      case 1312:
        return "Wind Archer Job 4";
      case 1400:
        return "Night Walker Job 1";
      case 1410:
        return "Night Walker Job 2";
      case 1411:
        return "Night Walker Job 3";
      case 1412:
        return "Night Walker Job 4";
      case 1500:
        return "Thunder Breaker Job 1";
      case 1510:
        return "Thunder breaker Job 2";
      case 1511:
        return "Thunder Breaker Job 3";
      case 1512:
        return "Thunder Breaker Job 4";
      case 2000:
        return "Legend";
      case 2100:
        return "Aran Job 1";
      case 2110:
        return "Aran Job 2";
      case 2111:
        return "Aran Job 3";
      case 2112:
        return "Aran Job 4";
      case 2200:
        return "Evan Job 1";
      case 2210:
        return "Evan Job 2";
      case 2211:
        return "Evan Job 3";
      case 2212:
        return "Evan Job 4";
      case 2213:
        return "Evan Job 5";
      case 2214:
        return "Evan Job 6";
      case 2215:
        return "Evan Job 7";
      case 2216:
        return "Evan Job 8";
      case 2217:
        return "Evan Job 9";
      case 2218:
        return "Evan Job 10";
    }
    return "invalid job name";
  }

  public void dropMessage(String text) {
    World.Broadcast.broadcastMessage(MaplePacketCreator.getGameMessage(5, text));
  }

  public void dropColorMessage(int id, String text) {
    World.Broadcast.broadcastMessage(MaplePacketCreator.getGameMessage(id, text));
  }

  public void showEffect(int effect) {
    World.Broadcast.broadcastMessage(MaplePacketCreator.showSpecialEffect_(effect));
  }

  public void setstat(byte stats, short newval) {
    getPlayer().setstat(stats, newval);
  }

  public void gainNx(int amount) {
    c.getPlayer().modifyCSPoints(1, amount);
  }

  public void sendFeedback(String msg) {
    PreparedStatement ps;
    try {
      Connection con = DatabaseConnection.getConnection();
      ps = con.prepareStatement("INSERT INTO feedback VALUES(?, ?, ?, ?, ?)");
      ps.setInt(1, getClient().getAccID());
      ps.setInt(2, getPlayer().getId());
      ps.setString(3, getClient().getAccountName());
      ps.setString(4, getPlayer().getName());
      ps.setString(5, msg);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      sendOk("#eSorry, you have already sent a message before. You are limited to 1 message per person.");
    }
  }

  public void worldmessage(byte type, String message) {
    for (@SuppressWarnings("unused")
        MapleCharacter chr : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
      World.Broadcast.broadcastMessage(MaplePacketCreator.getGameMessage(type, message));
    }
  }

  public String getDailyMissionInfo(int id) {
    switch (id) {
      case 160000:
        return "Kill 1000 cows";
      case 160001:
        return "Kill 1000 roosters";
      case 160002:
        return "Kill 500 cows";
      case 160003:
        return "Kill 500 roosters";
      case 160004:
        return "Kill 200 Ninja from Ninja 1 map";
      case 160005:
        return "Kill 500 Ninja from Ninja 1 map";
      case 160006:
        return "Hunt 100 oranges mushroom caps";
      default:
        return "invalid quest id. please report this to the owner.";
    }
  }

  public void expfix() {
    getPlayer().setExp(0);
    getPlayer().updateSingleStat(MapleStat.EXP, 0);
  }

  public String generateFeedbackList() {
    Connection con = DatabaseConnection.getConnection();
    try {
      StringBuilder sb = new StringBuilder();
      PreparedStatement ps = con.prepareStatement("SELECT charname, feedback FROM feedback");
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        sb.append("#e").append("Player name: ").append(rs.getString("charname")).append(":").append(("\r\n#r"))
            .append(rs.getString("feedback")).append("#k").append("\r\n\r\n");
      }
      sb.append("\r\n\r\n Reached end of table");
      return sb.toString();
    } catch (SQLException e) {
      System.out.println(e);
    }
    return "failed to generate feedback list";
  }

  public void sendBrowser(String site) {
    World.Broadcast.broadcastMessage(MaplePacketCreator.sendBrowser(site));
  }

  public void addNote(int songid, byte note) {

  }

  public void openVoteWebpage() {
    c.getPlayer().getClient().getSession().write(MaplePacketCreator.openVoteWebpage());
  }

  public void getShopForNPC(int id) {
    MapleShopFactory.getInstance().getShopForNPC(id).sendShop(c);
  }

  public void openMerchantItemStore() {
    c.getPlayer().setConversation(3);
    c.getSession().write(PlayerShopPacket.merchItemStore((byte) 0x22));
  }

  public void setProperty(String key, String value) {
    this.properties.put(key, value);
  }

  public String getProperty(String key) {
    return this.properties.get(key);
  }

  public void gainSp(int amount) {
    int newSp = (getPlayer().getRemainingSp() + amount);
    getPlayer().setRemainingSp(newSp);
    getPlayer().updateSingleStat(MapleStat.AVAILABLESP, newSp);
    byte[] packet = CWVsContextOnMessagePackets.onIncSpMessage(this.getPlayer().getJobValue(), amount);
    getClient().sendPacket(packet);
  }


  public boolean isQuestComplete(int id) {
    return getQuestStatus(id) == 2;
  }

  public void sendGetText(String text, String def, int col, int line) {
    getClient().sendPacket(ScriptMan.OnAskText(ScriptMan.NpcReplayedByNpc, npc, (byte) 0, text, def, col, line));
  }

  public void changeMusic(String music, boolean broadcast) {
    if (broadcast) {
      c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(music));
    } else {
      c.getSession().write(MaplePacketCreator.musicChange(music));
    }
  }

}