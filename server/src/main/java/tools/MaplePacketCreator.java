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

package tools;

import client.*;
import client.inventory.IEquip.ScrollResult;
import client.inventory.*;
import constants.GameConstants;
import handling.SendPacketOpcode;
import handling.channel.MapleGuildRanking.GuildRankingInfo;
import handling.channel.handler.PlayerInteractionHandler;
import handling.world.World;
import handling.world.buddy.BuddyListEntry;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleBBSThread.MapleBBSReply;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import handling.world.guild.MapleGuildCharacter;
import server.*;
import server.events.MapleSnowball.MapleSnowballs;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.PlayerNPC;
import server.life.SummonAttackEntry;
import server.maps.*;
import server.maps.MapleNodes.MapleNodeInfo;
import server.maps.MapleNodes.MaplePlatform;
import server.movement.MovePath;
import server.shops.HiredMerchant;
import server.shops.MaplePlayerShopItem;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.PacketHelper;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

public class MaplePacketCreator {

  public final static List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();

  public static final byte[] getServerIP(final int port, final int clientId) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
    mplew.writeShort(0);
    try {
      mplew.write(InetAddress.getByName(ServerProperties.getProperty("net.sf.odinms.channel.net.interface"))
          .getAddress());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    mplew.writeShort(port);
    mplew.writeInt(clientId);
    mplew.writeZeroBytes(5);

    return mplew.getPacket();
  }

  public static final byte[] getChannelChange(final int port) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
    mplew.write(1);
    try {
      mplew.write(InetAddress.getByName(ServerProperties.getProperty("net.sf.odinms.channel.net.interface"))
          .getAddress());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    mplew.writeShort(port);

    return mplew.getPacket();
  }

  public static final byte[] getCharInfo(final MapleCharacter chr) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
    mplew.writeShort(2);
    mplew.writeLong(1);
    mplew.writeLong(2);
    mplew.writeLong(chr.getClient().getChannel() - 1);
    mplew.write(chr.getPortalCount(true));
    mplew.write(1);
    mplew.writeShort(0);

    chr.CRand().connectData(mplew); // Random number generator

    PacketHelper.addCharacterInfo(mplew, chr);
    mplew.writeInt(0); // Lucky Logout Gift packet. Received/do not show =
    // 1; not received/show = 0
    mplew.writeInt(0); // SN 1
    mplew.writeInt(0); // SN 2
    mplew.writeInt(0); // SN 3
    mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));

    return mplew.getPacket();
  }

  public static final byte[] getWarpToMap(final MapleMap to, final int spawnPoint, final MapleCharacter chr) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
    mplew.writeShort(2);
    mplew.writeLong(1);
    mplew.writeLong(2);
    mplew.writeLong(chr.getClient().getChannel() - 1);
    mplew.write(chr.getPortalCount(true));
    mplew.write(0); // not connect packet
    mplew.writeShort(0); // Messages
    mplew.write(0); // revive stuffs?..
    mplew.writeInt(to.getId());
    mplew.write(spawnPoint);
    mplew.writeShort(chr.getStat().getHp());
    mplew.write(0); // if 1, then 2 more int
    mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));

    return mplew.getPacket();
  }

  public static final byte[] enableActions() {
    return updatePlayerStats(EMPTY_STATUPDATE, true, 0);
  }

  public static final byte[] updatePlayerStats(final List<Pair<MapleStat, Integer>> stats, final int evan) {
    return updatePlayerStats(stats, false, evan);
  }

  public static final byte[] updatePlayerStats(final List<Pair<MapleStat, Integer>> stats, final boolean itemReaction,
                                               final int evan) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
    mplew.write(itemReaction ? 1 : 0);
    int updateMask = 0;
    for (final Pair<MapleStat, Integer> statupdate : stats) {
      updateMask |= statupdate.getLeft().getValue();
    }
    List<Pair<MapleStat, Integer>> mystats = stats;
    if (mystats.size() > 1) {
      Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {

        @Override
        public int compare(final Pair<MapleStat, Integer> o1, final Pair<MapleStat, Integer> o2) {
          int val1 = o1.getLeft().getValue();
          int val2 = o2.getLeft().getValue();
          return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
        }
      });
    }
    mplew.writeInt(updateMask);
    Integer value;

    for (final Pair<MapleStat, Integer> statupdate : mystats) {
      value = statupdate.getLeft().getValue();

      if (value >= 1) {
        if (value == MapleStat.SKIN.getValue()) {
          mplew.writeShort(statupdate.getRight().shortValue());
        } else if (value <= MapleStat.HAIR.getValue()) {
          mplew.writeInt(statupdate.getRight());
        } else if (value < MapleStat.JOB.getValue()) {
          mplew.write(statupdate.getRight().byteValue());
        } else if (value == MapleStat.AVAILABLESP.getValue()) {
          if (GameConstants.isEvan(evan) || GameConstants.isResist(evan)) {
            mplew.writeShort(0);
          } else {
            mplew.writeShort(statupdate.getRight().shortValue());
          }
        } else if (value < 0xFFFF) {
          mplew.writeShort(statupdate.getRight().shortValue());
        } else {
          mplew.writeInt(statupdate.getRight().intValue());
        }
      }
    }
    mplew.write(0); // v88

    return mplew.getPacket();
  }

  public static final byte[] updateSp(MapleCharacter chr, final boolean itemReaction) { // this
    // will
    // do..
    return updateSp(chr, itemReaction, false);
  }

  public static final byte[] updateSp(MapleCharacter chr, final boolean itemReaction, final boolean overrideJob) { // this
    // will
    // do..
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
    mplew.write(itemReaction ? 1 : 0);
    mplew.writeInt(MapleStat.AVAILABLESP.getValue());
    if (overrideJob || GameConstants.isEvan(chr.getJob()) || GameConstants.isResist(chr.getJob())) {
      mplew.write(0);
    } else {
      mplew.writeShort(chr.getRemainingSp());
    }
    mplew.write(0); // v88

    return mplew.getPacket();
  }

  public static final byte[] instantMapWarp(final byte portal) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
    mplew.write(0);
    mplew.write(portal); // 6

    return mplew.getPacket();
  }

  public static final byte[] spawnPortal(final int townId, final int targetId, final int skillId, final Point pos) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PORTAL_TOWN.getValue());
    mplew.writeInt(townId);
    mplew.writeInt(targetId);
    mplew.writeInt(skillId);
    if (pos != null) {
      mplew.writePos(pos);
    }

    return mplew.getPacket();
  }

  public static final byte[] spawnDoor(final int oid, final Point pos, final boolean town) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
    mplew.write(town ? 1 : 0);
    mplew.writeInt(oid);
    mplew.writePos(pos);

    return mplew.getPacket();
  }

  public static byte[] removeDoor(int oid, boolean town) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    if (town) {
      mplew.writeShort(SendPacketOpcode.PORTAL_TOWN.getValue());
      mplew.writeInt(999999999);
      mplew.writeLong(999999999);
    } else {
      mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
      mplew.write(/* town ? 1 : */0);
      mplew.writeLong(oid);
    }

    return mplew.getPacket();
  }

  public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
    mplew.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());
    mplew.writeInt(summon.getOwner().getId());
    mplew.writeInt(summon.getObjectId());
    mplew.writeInt(summon.getSkill());
    mplew.write(summon.getOwner().getLevel());
    mplew.write(summon.getSkillLevel());
    mplew.writeShort(summon.getPosition().x);
    mplew.writeShort(summon.getPosition().y);
    mplew.write(4);
    mplew.write(summon.getStance());
    mplew.write(0);
    mplew.write(summon.getMovementType().getValue());
    mplew.write(summon.isMirrorTarget() ? 0 : 1);
    mplew.write((animated) || (summon.isMirrorTarget()) ? 0 : 1);
    if (!summon.isMirrorTarget()) {
      mplew.writeShort(0);
    } else {
      mplew.write(1);
      PacketHelper.addCharLook(mplew, summon.getOwner(), true);
    }
    return mplew.getPacket();
  }

  public static byte[] removeSummon(MapleSummon summon, boolean animated) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
    mplew.writeInt(summon.getOwnerId());
    mplew.writeInt(summon.getObjectId());
    mplew.write(animated ? 4 : 1);

    return mplew.getPacket();
  }

  public static byte[] getRelogResponse() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

    mplew.writeShort(SendPacketOpcode.RELOG_RESPONSE.getValue());
    mplew.write(1);

    return mplew.getPacket();
  }

  /**
   * Possible values for <code>type</code>:<br>
   * 1: You cannot move that channel. Please try again later.<br>
   * 2: You cannot go into the cash shop. Please try again later.<br>
   * 3: The Item-Trading shop is currently unavailable, please try again
   * later.<br>
   * 4: You cannot go into the trade shop, due to the limitation of user
   * count.<br>
   * 5: You do not meet the minimum level requirement to access the Trade
   * Shop.<br>
   *
   * @param type The type
   * @return The "block" packet.
   */
  public static byte[] serverBlocked(int type) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SERVER_BLOCKED.getValue());
    mplew.write(type);

    return mplew.getPacket();
  }

  public static byte[] mapBlocked(int type) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    // 1: The portal is closed for now.
    // 2: You cannot go to that place.
    // 3: Unable to approach due to the force of the ground.
    // 4; You cannot teleport to or on this map.
    // 5; Unable to approach due to the force of the ground.
    // 6: This map can only be entered by party members.
    // 7: Only members of an expedition can enter this map.
    // 8: The cash shop is currently not available. Stay tuned...
    mplew.writeShort(SendPacketOpcode.MAP_BLOCKED.getValue());
    mplew.write(type);
    return mplew.getPacket();
  }

  public static byte[] serverMessage(String message) {
    return serverMessage(4, 0, message, false);
  }

  public static byte[] serverNotice(int type, String message) {
    return serverMessage(type, 0, message, false);
  }

  public static byte[] serverNotice(int type, int channel, String message) {
    return serverMessage(type, channel, message, false);
  }

  public static byte[] serverNotice(int type, int channel, String message, boolean smegaEar) {
    return serverMessage(type, channel, message, smegaEar);
  }

  public static byte[] serverMessage(int type, int channel, String message, boolean whisper) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    // 0: [Notice] <Msg>
    // 1: Popup <Msg>
    // 2: Megaphone
    // 3: Super Megaphone
    // 4: Server Message
    // 5: Pink Text
    // 6: LightBlue Text ({} as Item)
    // 7: [int] -> Keep Wz Error
    // 8: Item Megaphone
    // 9: Item Megaphone
    // 10: Three Line Megaphone
    // 11: Weather Effect
    // 12: Green Gachapon Message
    // 13: Yellow Twin Dragon's Egg
    // 14: Green Twin Dragon's Egg
    // 15: Lightblue Text
    // 16: Lightblue Text
    // 18: LightBlue Text ({} as Item)
    // 20: (Red Message) : Skull?

    mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
    mplew.write(type);
    if (type == 4) { // Server Message
      mplew.write(1);
    }
    mplew.writeMapleAsciiString(message);
    switch (type) {
      case 3: // Super Megaphone
      case 20: // Skull Megaphone
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);
        break;
      case 9: // Like Item Megaphone (Without Item)
        mplew.write(channel - 1);
        break;
      case 11: // Weather Effect
        mplew.writeInt(channel); // item id
        break;
      case 13: // Yellow Twin Dragon's Egg
      case 14: // Green Twin Dragon's Egg
        mplew.writeMapleAsciiString("NULL"); // Name
        PacketHelper.addItemInfo(mplew, null, true, true);
        break;
      case 6:
      case 18:
        mplew.writeInt(channel >= 1000000 && channel < 6000000 ? channel : 0); // Item
        // Id
        // E.G. All new EXP coupon {Ruby EXP Coupon} is now available in the
        // Cash Shop!
        break;
    }
    return mplew.getPacket();
  }

  public static byte[] getGachaponMega(final String name, final String message, final IItem item,
                                       final byte rareness) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
    mplew.write(12);
    mplew.writeMapleAsciiString(name + " : got a(n) ");
    mplew.writeInt(1); // 0~3 i think
    mplew.writeMapleAsciiString(message);
    PacketHelper.addItemInfo(mplew, item, true, true);

    return mplew.getPacket();
  }

  public static byte[] tripleSmega(List<String> message, boolean ear, int channel) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
    mplew.write(10);
    if (message.get(0) != null) {
      mplew.writeMapleAsciiString(message.get(0));
    }
    mplew.write(message.size());
    for (int i = 1; i < message.size(); i++) {
      if (message.get(i) != null) {
        mplew.writeMapleAsciiString(message.get(i));
      }
    }
    mplew.write(channel - 1);
    mplew.write(ear ? 1 : 0);

    return mplew.getPacket();
  }

  public static byte[] getAvatarMega(MapleCharacter chr, int channel, int itemId, final List<String> text,
                                     boolean ear) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
    mplew.writeInt(itemId);
    mplew.writeMapleAsciiString(chr.getName());
    for (String i : text) {
      mplew.writeMapleAsciiString(i);
    }
    mplew.writeInt(channel - 1); // channel
    mplew.write(ear ? 1 : 0);
    PacketHelper.addCharLook(mplew, chr, true);

    return mplew.getPacket();
  }

  public static byte[] itemMegaphone(String msg, boolean whisper, int channel, IItem item) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
    mplew.write(8);
    mplew.writeMapleAsciiString(msg);
    mplew.write(channel - 1);
    mplew.write(whisper ? 1 : 0);

    if (item == null) {
      mplew.write(0);
    } else {
      PacketHelper.addItemInfo(mplew, item, false, false, true);
    }
    return mplew.getPacket();
  }

  public static byte[] spawnNPC(MapleNPC life, boolean show) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
    mplew.writeInt(life.getObjectId());
    mplew.writeInt(life.getId());
    mplew.writeShort(life.getPosition().x);
    mplew.writeShort(life.getCy());
    mplew.write(life.getF() == 1 ? 0 : 1);
    mplew.writeShort(life.getFh());
    mplew.writeShort(life.getRx0());
    mplew.writeShort(life.getRx1());
    mplew.write((show && !life.isHidden()) ? 1 : 0);

    return mplew.getPacket();
  }

  public static byte[] removeNPC(final int objectid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
    mplew.writeInt(objectid);// TODO: is this correct?

    return mplew.getPacket();
  }

  public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
    mplew.write(1);
    mplew.writeInt(life.getObjectId());
    mplew.writeInt(life.getId());
    mplew.writeShort(life.getPosition().x);
    mplew.writeShort(life.getCy());
    mplew.write(life.getF() == 1 ? 0 : 1);
    mplew.writeShort(life.getFh());
    mplew.writeShort(life.getRx0());
    mplew.writeShort(life.getRx1());
    mplew.write(MiniMap ? 1 : 0);

    return mplew.getPacket();
  }

  public static byte[] spawnPlayerNPC(PlayerNPC npc) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
    mplew.write(1); // 0 = hide, 1 = show
    mplew.writeInt(npc.getId());
    mplew.writeMapleAsciiString(npc.getName());
    mplew.write(npc.getGender());
    mplew.write(npc.getSkin());
    mplew.writeInt(npc.getFace());
    mplew.write(0);
    mplew.writeInt(npc.getHair());
    Map<Byte, Integer> equip = npc.getEquips();
    Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
    Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
    for (Entry<Byte, Integer> position : equip.entrySet()) {
      byte pos = (byte) (position.getKey() * -1);
      if (pos < 100 && myEquip.get(pos) == null) {
        myEquip.put(pos, position.getValue());
      } else if ((pos > 100 || pos == -128) && pos != 111) { // don't ask.
        // o.o
        pos = (byte) (pos == -128 ? 28 : pos - 100);
        if (myEquip.get(pos) != null) {
          maskedEquip.put(pos, myEquip.get(pos));
        }
        myEquip.put(pos, position.getValue());
      } else if (myEquip.get(pos) != null) {
        maskedEquip.put(pos, position.getValue());
      }
    }
    for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
      mplew.write(entry.getKey());
      mplew.writeInt(entry.getValue());
    }
    mplew.write(0xFF);
    for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
      mplew.write(entry.getKey());
      mplew.writeInt(entry.getValue());
    }
    mplew.write(0xFF);
    Integer cWeapon = equip.get((byte) -111);
    if (cWeapon != null) {
      mplew.writeInt(cWeapon);
    } else {
      mplew.writeInt(0);
    }
    for (int i = 0; i < 3; i++) {
      mplew.writeInt(npc.getPet(i));
    }

    return mplew.getPacket();
  }

  public static byte[] getChatText(int cidfrom, String text, boolean whiteBG, int show) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CHATTEXT.getValue());
    mplew.writeInt(cidfrom);
    mplew.write(whiteBG ? 1 : 0);
    mplew.writeMapleAsciiString(text);
    mplew.write(show);

    return mplew.getPacket();
  }

  public static byte[] GameMaster_Func(int value, int mode) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GM_EFFECT.getValue());
    mplew.write(value);
    mplew.write(mode);

    return mplew.getPacket();
  }

  public static byte[] testCombo(int value) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
    mplew.writeInt(value);

    return mplew.getPacket();
  }

  public static byte[] getPacketFromHexString(String hex) {
    return HexTool.getByteArrayFromHexString(hex);
  }

  public static final byte[] GainEXP_Monster(int gain, boolean white, int partyinc, int Class_Bonus_EXP,
                                             int Equipment_Bonus_EXP, int Premium_Bonus_EXP, byte percentage, double hoursFromLogin) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
    mplew.write(white ? 1 : 0);
    mplew.writeInt(gain);
    mplew.write(0); // Not in chat
    mplew.writeInt(0); // Bonus Event EXP
    mplew.write(percentage);
    mplew.write(0);
    mplew.writeInt(0); // Bonus Wedding EXP
    // A bonus EXP <percentage>% is awarded for every 3rd monster defeated.
    // Bonus EXP for hunting over <> hours
    mplew.write((byte) hoursFromLogin);
    if (percentage > 0) {
      mplew.write(0); // Party bonus rate. x 0.01
    }
    mplew.writeInt(partyinc); // Bonus EXP for PARTY
    mplew.writeInt(Equipment_Bonus_EXP); // Equip Item Bonus EXP
    mplew.writeInt(0); // Internet Cafe EXP Bonus
    mplew.writeInt(0); // Rainbow Week Bonus EXP
    mplew.writeInt(Premium_Bonus_EXP); // Party Ring Bonus EXP
    mplew.writeInt(0); // Cake vs Pie Bonus EXP

    return mplew.getPacket();
  }

  public static final byte[] GainEXP_Others(final int gain, final boolean inChat, final boolean white) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
    mplew.write(white ? 1 : 0);
    mplew.writeInt(gain);
    mplew.write(inChat ? 1 : 0);
    mplew.writeInt(0); // Bonus Event EXP
    mplew.write(0);
    mplew.write(0);
    mplew.writeInt(0); // Bonus Wedding EXP
    if (inChat) {
      int bonus = 0, applied = 0; // will code this later on.
      // Earned 'Spirit Week Event' bonus EXP.
      mplew.write(bonus);
      if (bonus > 0) {
        // The next <applied> completed quests will include additional
        // Event bonus EXP
        mplew.write(applied);
      }
    }
    mplew.write(0); // Party bonus rate. x 0.01
    mplew.writeInt(0); // Bonus EXP for PARTY
    mplew.writeInt(0); // Equip Item Bonus EXP
    mplew.writeInt(0); // Internet Cafe EXP Bonus
    mplew.writeInt(0); // Rainbow Week Bonus EXP
    mplew.writeInt(0); // Party Ring Bonus EXP
    mplew.writeInt(0); // Cake vs Pie Bonus EXP

    return mplew.getPacket();
  }

  public static final byte[] getShowFameGain(final int gain) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(5);
    mplew.writeInt(gain);

    return mplew.getPacket();
  }

  public static final byte[] showMesoGain(final int gain, final boolean inChat) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    if (!inChat) {
      mplew.write(0);
      mplew.write(1);
      mplew.write(0);
      mplew.writeInt(gain);
      mplew.writeShort(0); // inet cafe meso gain ?.o
    } else {
      mplew.write(6);
      mplew.writeInt(gain);
    }

    return mplew.getPacket();
  }

  public static byte[] getShowItemGain(int itemId, short quantity) {
    return getShowItemGain(itemId, quantity, false);
  }

  public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    if (inChat) {
      mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
      mplew.write(3);
      mplew.write(1); // item count
      mplew.writeInt(itemId);
      mplew.writeInt(quantity);
      /*
       * for (int i = 0; i < count; i++) { // if ItemCount is handled.
       * mplew.writeInt(itemId); mplew.writeInt(quantity); }
       */
    } else {
      mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
      mplew.writeShort(0);
      mplew.writeInt(itemId);
      mplew.writeInt(quantity);
    }
    return mplew.getPacket();
  }

  public static byte[] showRewardItemAnimation(int itemId, String effect) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    mplew.write(14);
    mplew.writeInt(itemId);
    mplew.write(effect != null && effect.length() > 0 ? 1 : 0);
    if (effect != null && effect.length() > 0) {
      mplew.writeMapleAsciiString(effect);
    }

    return mplew.getPacket();
  }

  public static byte[] showRewardItemAnimation(int itemId, String effect, int from_playerid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
    mplew.writeInt(from_playerid);
    mplew.write(14);
    mplew.writeInt(itemId);
    mplew.write(effect != null && effect.length() > 0 ? 1 : 0);
    if (effect != null && effect.length() > 0) {
      mplew.writeMapleAsciiString(effect);
    }

    return mplew.getPacket();
  }

  public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
    mplew.write(mod); // 1 animation, 2 no animation, 3 spawn disappearing
    // item [Fade], 4 spawn disappearing item
    mplew.writeInt(drop.getObjectId()); // item owner id
    mplew.write(drop.getMeso() > 0 ? 1 : 0); // 1 mesos, 0 item, 2 and above
    // all item meso bag,
    mplew.writeInt(drop.getItemId()); // drop object ID
    mplew.writeInt(drop.getOwner()); // owner charid
    mplew.write(drop.getDropType()); // 0 = timeout for non-owner, 1 =
    // timeout for non-owner's party, 2
    // = FFA, 3 = explosive/FFA
    mplew.writePos(dropto);
    mplew.writeInt(0);

    if (mod != 2) {
      mplew.writePos(dropfrom);
      mplew.writeShort(0);
    }
    if (drop.getMeso() == 0) {
      PacketHelper.addExpirationTime(mplew, drop.getItem().getExpiration());
    }
    mplew.writeShort(drop.isPlayerDrop() ? 0 : 1); // pet EQP pickup

    return mplew.getPacket();
  }

  public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
    mplew.writeInt(chr.getId());
    mplew.write(chr.getLevel());
    mplew.writeMapleAsciiString(chr.getName());

    if (chr.getGuildId() <= 0) {
      mplew.writeInt(0);
      mplew.writeInt(0);
    } else {
      final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
      if (gs != null) {
        mplew.writeMapleAsciiString(gs.getName());
        mplew.writeShort(gs.getLogoBG());
        mplew.write(gs.getLogoBGColor());
        mplew.writeShort(gs.getLogo());
        mplew.write(gs.getLogoColor());
      } else {
        mplew.writeMapleAsciiString("");
        mplew.write(new byte[6]);
      }
    }
    // mplew.writeInt(3); after aftershock
    List<Pair<Integer, Integer>> buffvalue = new ArrayList<Pair<Integer, Integer>>();
    long fbuffmask = 0x3F80000L; // becomes F8000000 after bb?
    /**
     * TODO: Removed for now, it d/c's other people
     *
     */
    if (chr.getBuffedValue(MapleBuffStat.FINAL_CUT) != null) {
      fbuffmask |= MapleBuffStat.FINAL_CUT.getValue();
      buffvalue.add(new Pair<>(
          Integer.valueOf(chr.getBuffedValue(MapleBuffStat.FINAL_CUT).intValue()), 3));
    }
    if (chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT) != null) {
      fbuffmask |= MapleBuffStat.OWL_SPIRIT.getValue();
      buffvalue.add(new Pair<>(
          Integer.valueOf(chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT).intValue()), 3));
    }

    if (chr.getBuffedValue(MapleBuffStat.SOARING) != null) {
      fbuffmask |= MapleBuffStat.SOARING.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.MIRROR_IMAGE) != null) {
      fbuffmask |= MapleBuffStat.MIRROR_IMAGE.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.DARK_AURA) != null) {
      fbuffmask |= MapleBuffStat.DARK_AURA.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.BLUE_AURA) != null) {
      fbuffmask |= MapleBuffStat.BLUE_AURA.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.YELLOW_AURA) != null) {
      fbuffmask |= MapleBuffStat.YELLOW_AURA.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ) != null) {
      fbuffmask |= MapleBuffStat.PYRAMID_PQ.getValue();
      buffvalue.add(new Pair<>(
          Integer.valueOf(chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ).intValue()), 1)); // idk
    }
    if (chr.getBuffedValue(MapleBuffStat.MAGIC_SHIELD) != null) {
      fbuffmask |= MapleBuffStat.MAGIC_SHIELD.getValue();
      buffvalue.add(new Pair<>(
          Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MAGIC_SHIELD).intValue()), 1)); // idk
    }
    mplew.writeLong(fbuffmask);
    long buffmask = 0;

    if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && !chr.isHidden()) {
      buffmask |= MapleBuffStat.DARKSIGHT.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
      buffmask |= MapleBuffStat.COMBO.getValue();
      buffvalue
          .add(new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue()),
              MapleBuffStat.COMBO.isFirst()));
    }
    if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
      buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
      buffmask |= MapleBuffStat.SOULARROW.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.DIVINE_BODY) != null) {
      buffmask |= MapleBuffStat.DIVINE_BODY.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.BERSERK_FURY) != null) {
      buffmask |= MapleBuffStat.BERSERK_FURY.getValue();
    }
    if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
      buffmask |= MapleBuffStat.MORPH.getValue();
      buffvalue.add(
          new Pair<Integer, Integer>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue()), 2));
    }

    mplew.writeLong(buffmask);
    for (Pair<Integer, Integer> i : buffvalue) {
      if (i.right == 2) {
        mplew.writeShort(i.left.shortValue());
      } else if (i.right == 3) {
        mplew.writeInt(i.left.shortValue());
      } else {
        mplew.write(i.left.byteValue());
      }
    }
    final int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
    // CHAR_MAGIC_SPAWN is really just tickCount
    // this is here as it explains the 7 "dummy" buffstats which are placed
    // into every character
    // these 7 buffstats are placed because they have irregular packet
    // structure.
    // they ALL have writeShort(0); first, then a long as their variables,
    // then server tick count
    // 0x80000, 0x100000, 0x200000, 0x400000, 0x800000, 0x1000000, 0x2000000

    mplew.writeShort(0); // start of energy charge
    mplew.writeLong(0);
    mplew.write(1);
    mplew.writeInt(CHAR_MAGIC_SPAWN);
    mplew.writeShort(0); // start of dash_speed
    mplew.writeLong(0);
    mplew.write(1);
    mplew.writeInt(CHAR_MAGIC_SPAWN);
    mplew.writeShort(0); // start of dash_jump
    mplew.writeLong(0);
    mplew.write(1);
    mplew.writeInt(CHAR_MAGIC_SPAWN);
    mplew.writeShort(0); // start of Monster Riding
    int buffSrc = chr.getBuffSource(MapleBuffStat.MONSTER_RIDING);
    if (buffSrc > 0) {
      final IItem c_mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -118);
      final IItem mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
      if (GameConstants.getMountItem(buffSrc) == 0 && c_mount != null) {
        mplew.writeInt(c_mount.getItemId());
      } else if (GameConstants.getMountItem(buffSrc) == 0 && mount != null) {
        mplew.writeInt(mount.getItemId());
      } else {
        mplew.writeInt(GameConstants.getMountItem(buffSrc));
      }
      mplew.writeInt(buffSrc);
    } else {
      mplew.writeLong(0);
    }
    mplew.write(1);
    mplew.writeInt(CHAR_MAGIC_SPAWN);
    mplew.writeLong(0); // speed infusion behaves differently here
    mplew.write(1);
    mplew.writeInt(CHAR_MAGIC_SPAWN);
    mplew.writeInt(1);
    mplew.writeLong(0); // homing beacon
    mplew.write(0);
    mplew.writeShort(0);
    mplew.write(1);
    mplew.writeInt(CHAR_MAGIC_SPAWN);
    mplew.writeInt(0); // and finally, something ive no idea
    mplew.writeLong(0);
    mplew.write(1);
    mplew.writeInt(CHAR_MAGIC_SPAWN);
    mplew.writeShort(0);
    mplew.writeShort(chr.getJob());
    PacketHelper.addCharLook(mplew, chr, false);
    mplew.writeInt(0);// this is CHARID to follow
    mplew.writeInt(0); // probably charid following
    mplew.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000))); // max
    // is
    // like
    // 100.
    // but
    // w/e
    mplew.writeInt(chr.getItemEffect());
    mplew.writeInt(0);
    mplew.writeInt(GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);
    mplew.writePos(chr.getPosition());
    mplew.write(chr.getStance());
    mplew.writeShort(0); // FH
    mplew.write(0);
    mplew.write(0); // end of pets
    mplew.writeInt(chr.getMount().getLevel()); // mount lvl
    mplew.writeInt(chr.getMount().getExp()); // exp
    mplew.writeInt(chr.getMount().getFatigue()); // tiredness
    PacketHelper.addAnnounceBox(mplew, chr);
    mplew.write(chr.getChalkboard() != null && chr.getChalkboard().length() > 0 ? 1 : 0);
    if (chr.getChalkboard() != null && chr.getChalkboard().length() > 0) {
      mplew.writeMapleAsciiString(chr.getChalkboard());
    }
    Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
    addRingInfo(mplew, rings.getLeft());
    addRingInfo(mplew, rings.getMid());
    addMRingInfo(mplew, rings.getRight(), chr);
    // mplew.write(0); // 3 ints
    mplew.write(chr.getStat().Berserk ? 1 : 0);
    mplew.write(0); // if this is 1, then 1 int(size), each size = another
    // int.
    mplew.writeInt(0);
    if (chr.getCarnivalParty() != null) {
      mplew.write(chr.getCarnivalParty().getTeam());
    } else {
      mplew.write(0);
    }
    return mplew.getPacket();
  }

  public static byte[] removePlayerFromMap(int cid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
    mplew.writeInt(cid);

    return mplew.getPacket();
  }

  public static byte[] facialExpression(MapleCharacter from, int expression) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
    mplew.writeInt(from.getId());
    mplew.writeInt(expression);
    mplew.writeInt(-1); // itemid of expression use
    mplew.write(0);

    return mplew.getPacket();
  }


  public static byte[] movePlayer(int cid, MovePath moves){// CUserRemote::OnMove
    final MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
    packet.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
    packet.writeInt(cid);
    moves.encode(packet);
    return packet.getPacket();
  }



  public static byte[] moveSummon(int cid, int oid, MovePath path) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
    mplew.writeInt(cid);
    mplew.writeInt(oid);
    path.encode(mplew);

    return mplew.getPacket();
  }

  public static byte[] summonAttack(final int cid, final int summonSkillId, final byte animation,
                                    final List<SummonAttackEntry> allDamage, final int level) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
    mplew.writeInt(cid);
    mplew.writeInt(summonSkillId);
    mplew.write(level - 1); // ? guess
    mplew.write(animation);
    mplew.write(allDamage.size());

    for (final SummonAttackEntry attackEntry : allDamage) {
      mplew.writeInt(attackEntry.getMonster().getObjectId()); // oid
      mplew.write(7); // who knows
      mplew.writeInt(attackEntry.getDamage()); // damage
    }
    return mplew.getPacket();
  }

  public static byte[] closeRangeAttack(int cid, int tbyte, int skill, int level, byte display, byte animation,
                                        byte speed, List<AttackPair> damage, final boolean energy, int lvl, byte mastery, byte unk, int charge) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(
        energy ? SendPacketOpcode.ENERGY_ATTACK.getValue() : SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
    mplew.writeInt(cid);
    mplew.write(tbyte);
    mplew.write(lvl); // ?
    if (skill > 0) {
      mplew.write(level);
      mplew.writeInt(skill);
    } else {
      mplew.write(0);
    }
    mplew.write(unk); // Added on v.82
    // a short actually
    mplew.write(display);
    mplew.write(animation);

    mplew.write(speed);
    mplew.write(mastery); // Mastery
    mplew.writeInt(0); // E9 03 BE FC

    if (skill == 4211006) {
      for (AttackPair oned : damage) {
        if (oned.attack != null) {
          mplew.writeInt(oned.objectid);
          mplew.write(0x07);
          mplew.write(oned.attack.size());
          for (Pair<Integer, Boolean> eachd : oned.attack) {
            mplew.write(eachd.right ? 1 : 0);
            mplew.writeInt(eachd.left); // m.e. is never crit
          }
        }
      }
    } else {
      for (AttackPair oned : damage) {
        if (oned.attack != null) {
          mplew.writeInt(oned.objectid);
          mplew.write(0x07);
          for (Pair<Integer, Boolean> eachd : oned.attack) {
            mplew.write(eachd.right ? 1 : 0);
            mplew.writeInt(eachd.left.intValue());
          }
        }
      }
    }
    // if (charge > 0) {
    // mplew.writeInt(charge); //is it supposed to be here
    // }
    return mplew.getPacket();
  }

  public static byte[] rangedAttack(int cid, byte tbyte, int skill, int level, byte display, byte animation,
                                    byte speed, int itemid, List<AttackPair> damage, final Point pos, int lvl, byte mastery, byte unk) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
    mplew.writeInt(cid);
    mplew.write(tbyte);
    mplew.write(lvl); // ?
    if (skill > 0) {
      mplew.write(level);
      mplew.writeInt(skill);
    } else {
      mplew.write(0);
    }
    mplew.write(unk); // Added on v.82
    mplew.write(display);
    mplew.write(animation);
    mplew.write(speed);
    mplew.write(mastery); // Mastery level, who cares
    mplew.writeInt(itemid);

    for (AttackPair oned : damage) {
      if (oned.attack != null) {
        mplew.writeInt(oned.objectid);
        mplew.write(0x07);
        for (Pair<Integer, Boolean> eachd : oned.attack) {
          mplew.write(eachd.right ? 1 : 0);
          mplew.writeInt(eachd.left.intValue());
        }
      }
    }
    mplew.writePos(pos); // Position

    return mplew.getPacket();
  }

  public static byte[] magicAttack(int cid, int tbyte, int skill, int level, byte display, byte animation, byte speed,
                                   List<AttackPair> damage, int charge, int lvl, byte unk) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
    mplew.writeInt(cid);
    mplew.write(tbyte);
    mplew.write(lvl); // ?
    mplew.write(level);
    mplew.writeInt(skill);

    mplew.write(unk); // Added on v.82
    mplew.write(display);
    mplew.write(animation);
    mplew.write(speed);
    mplew.write(0); // Mastery byte is always 0 because spells don't have a
    // swoosh
    mplew.writeInt(0);

    for (AttackPair oned : damage) {
      if (oned.attack != null) {
        mplew.writeInt(oned.objectid);
        mplew.write(7);
        for (Pair<Integer, Boolean> eachd : oned.attack) {
          mplew.write(eachd.right ? 1 : 0);
          mplew.writeInt(eachd.left.intValue());
        }
      }
    }
    if (charge > 0) {
      mplew.writeInt(charge);
    }
    return mplew.getPacket();
  }

  public static byte[] getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {

    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
    mplew.writeInt(sid);
    mplew.writeShort(items.size()); // item count
    for (MapleShopItem item : items) {
      mplew.writeInt(item.getItemId());
      mplew.writeInt(item.getPrice());
      mplew.write(0); // ??
      mplew.writeInt(item.getReqItem());
      mplew.writeInt(item.getReqItemQ());
      mplew.writeInt(GameConstants.isPet(item.getItemId()) ? 0 : item.getExpiration()); // Can
      // be
      // used
      // x
      // minutes
      // after
      // purchase
      mplew.writeInt(item.getReqLevel()); // minimum level to purchase
      // item ("Your level must be
      // over lv.X to purchase this
      // item"), the level stated here
      // = can buy already.
      if (GameConstants.isRechargable(item.getItemId())) {
        mplew.writeShort(0);
        mplew.writeInt(0);
      }
      mplew.writeShort(GameConstants.isRechargable(item.getItemId()) ? ii.getSlotMax(c, item.getItemId())
          : item.getQuantity()); // Quantity sold
      mplew.writeShort(ii.getSlotMax(c, item.getItemId())); // Maximum
      // quantity
      // which can
      // be bought
      // at a time
      // / Max
      // recharged
    }
    return mplew.getPacket();
  }

  public static byte[] confirmShopTransaction(byte code) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
    mplew.write(code); // 8 = sell, 0 = buy, 0x20 = due to an error

    return mplew.getPacket();
  }

  public static byte[] addInventorySlot(MapleInventoryType type, IItem item) {
    return addInventorySlot(type, item, false);
  }

  public static byte[] addInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(fromDrop ? 1 : 0);
    mplew.writeShort(1); // add mode
    mplew.write(type.getType()); // iv type
    mplew.write(item.getPosition()); // slot id
    PacketHelper.addItemInfo(mplew, item, true, false);

    return mplew.getPacket();
  }

  public static byte[] updateInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(fromDrop ? 1 : 0);
    // mplew.write((slot2 > 0 ? 1 : 0) + 1);
    mplew.write(1);
    mplew.write(1);
    mplew.write(type.getType()); // iv type
    mplew.writeShort(item.getPosition()); // slot id
    mplew.writeShort(item.getQuantity());
    /*
     * if (slot2 > 0) { mplew.write(1); mplew.write(type.getType());
     * mplew.writeShort(slot2); mplew.writeShort(amt2); }
     */
    return mplew.getPacket();
  }

  public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst) {
    return moveInventoryItem(type, src, dst, (byte) -1);
  }

  public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst, short equipIndicator) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(HexTool.getByteArrayFromHexString("01 01 02"));
    mplew.write(type.getType());
    mplew.writeShort(src);
    mplew.writeShort(dst);
    if (equipIndicator != -1) {
      mplew.write(equipIndicator);
    }
    return mplew.getPacket();
  }

  public static byte[] moveAndMergeInventoryItem(MapleInventoryType type, short src, short dst, short total) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(HexTool.getByteArrayFromHexString("01 02 03"));
    mplew.write(type.getType());
    mplew.writeShort(src);
    mplew.write(1); // merge mode?
    mplew.write(type.getType());
    mplew.writeShort(dst);
    mplew.writeShort(total);

    return mplew.getPacket();
  }

  public static byte[] moveAndMergeWithRestInventoryItem(MapleInventoryType type, short src, short dst, short srcQ,
                                                         short dstQ) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(HexTool.getByteArrayFromHexString("01 02 01"));
    mplew.write(type.getType());
    mplew.writeShort(src);
    mplew.writeShort(srcQ);
    mplew.write(HexTool.getByteArrayFromHexString("01"));
    mplew.write(type.getType());
    mplew.writeShort(dst);
    mplew.writeShort(dstQ);

    return mplew.getPacket();
  }

  public static byte[] clearInventoryItem(MapleInventoryType type, short slot, boolean fromDrop) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(fromDrop ? 1 : 0);
    mplew.write(HexTool.getByteArrayFromHexString("01 03"));
    mplew.write(type.getType());
    mplew.writeShort(slot);

    return mplew.getPacket();
  }

  public static byte[] updateSpecialItemUse(IItem item, byte invType) {
    return updateSpecialItemUse(item, invType, item.getPosition());
  }

  public static byte[] updateSpecialItemUse(IItem item, byte invType, short pos) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(0); // could be from drop
    mplew.write(2); // always 2
    mplew.write(3); // quantity > 0 (?)
    mplew.write(invType); // Inventory type
    mplew.writeShort(pos); // item slot
    mplew.write(0);
    mplew.write(invType);
    if (item.getType() == 1) {
      mplew.writeShort(pos);
    } else {
      mplew.write(pos);
    }
    PacketHelper.addItemInfo(mplew, item, true, true);
    if (item.getPosition() < 0) {
      mplew.write(2); // ?
    }

    return mplew.getPacket();
  }

  public static byte[] updateSpecialItemUse_(IItem item, byte invType) {
    return updateSpecialItemUse_(item, invType, item.getPosition());
  }

  public static byte[] updateSpecialItemUse_(IItem item, byte invType, short pos) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(0); // could be from drop
    mplew.write(1); // always 2
    mplew.write(0); // quantity > 0 (?)
    mplew.write(invType); // Inventory type
    if (item.getType() == 1) {
      mplew.writeShort(pos);
    } else {
      mplew.write(pos);
    }
    PacketHelper.addItemInfo(mplew, item, true, true);
    if (item.getPosition() < 0) {
      mplew.write(1); // ?
    }

    return mplew.getPacket();
  }

  public static byte[] scrolledItem(IItem scroll, IItem item, boolean destroyed, boolean potential) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(1); // fromdrop always true
    mplew.write(destroyed ? 2 : 3);
    mplew.write(scroll.getQuantity() > 0 ? 1 : 3);
    mplew.write(GameConstants.getInventoryType(scroll.getItemId()).getType()); // can
    // be
    // cash
    mplew.writeShort(scroll.getPosition());

    if (scroll.getQuantity() > 0) {
      mplew.writeShort(scroll.getQuantity());
    }
    mplew.write(3);
    if (!destroyed) {
      mplew.write(MapleInventoryType.EQUIP.getType());
      mplew.writeShort(item.getPosition());
      mplew.write(0);
    }
    mplew.write(MapleInventoryType.EQUIP.getType());
    mplew.writeShort(item.getPosition());
    if (!destroyed) {
      PacketHelper.addItemInfo(mplew, item, true, true);
    }
    mplew.write(potential ? 2 : 1);

    return mplew.getPacket();
  }

  public static byte[] getNormalScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit,
                                             boolean whiteScroll) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_UPGRADE_EFFECT.getValue());
    mplew.writeInt(chr);

    switch (scrollSuccess) {
      case SUCCESS:
        mplew.write(1);
        mplew.write(0);
        break;
      case FAIL:
        mplew.write(0);
        mplew.write(0);
        break;
      case CURSE:
        mplew.write(0);
        mplew.write(1);
        break;
    }
    mplew.write(legendarySpirit ? 1 : 0);
    mplew.writeInt(0);// if this is 2, then below 2 bytes are not used.
    // 1 + 0 : You are successful in upgrading the equipment.
    // 0 + 0 : You fail to upgrade the equipment.
    // 0 + 1 : Your equipment is destroyed since you failed to upgrade.
    mplew.write(legendarySpirit ? 0 : (whiteScroll ? 1 : 0)); // this is not
    // used when
    // got
    // legendaryspirit
    mplew.write(/* legendarySpirit ? (whiteScroll ? 1 : 0) : */0); // pams
    // song?

    return mplew.getPacket();
  }

  public static byte[] getPotentialScrollEffect(boolean isPotential, int chr, ScrollResult scrollSuccess,
                                                boolean legendarySpirit) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(isPotential ? SendPacketOpcode.SHOW_ITEM_OPTION_UPGRADE_EFFECT.getValue()
        : SendPacketOpcode.SHOW_ITEM_HYPER_UPGRADE_EFFECT.getValue());
    mplew.writeInt(chr);
    mplew.write(scrollSuccess == ScrollResult.SUCCESS ? 1 : 0);
    mplew.write(scrollSuccess == ScrollResult.CURSE ? 1 : 0);
    mplew.write(legendarySpirit ? 1 : 0);
    mplew.writeInt(0); // ??

    return mplew.getPacket();
  }

  public static byte[] getMagnifyingEffect(final int chr, final short pos) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_RELEASE_EFFECT.getValue());
    mplew.writeInt(chr);
    mplew.writeShort(pos);

    return mplew.getPacket();
  }

  public static byte[] getMiracleCubeEffect(final int chr, final boolean pass) {
    // 0x00 : Resetting Potential has failed due to insufficient space in
    // the Use item.
    // 0x01 : Potential successfully reset.
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_UNRELEASE_EFFECT.getValue());
    mplew.writeInt(chr);
    mplew.write(pass ? 1 : 0);

    return mplew.getPacket();
  }

  public static final byte[] ItemMaker_Success(final boolean pass) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    mplew.write(16);
    mplew.writeInt(pass ? 0 : 1); // 0 = pass, 1 = fail

    return mplew.getPacket();
  }

  public static final byte[] ItemMaker_Success_3rdParty(final int from_playerid, final boolean pass) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
    mplew.writeInt(from_playerid);
    mplew.write(16);
    mplew.writeInt(pass ? 0 : 1); // 0 = pass, 1 = fail

    return mplew.getPacket();
  }

  public static byte[] explodeDrop(int oid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
    mplew.write(4); // 4 = Explode
    mplew.writeInt(oid);
    mplew.writeShort(655);

    return mplew.getPacket();
  }

  public static byte[] removeItemFromMap(int oid, int animation, int cid) {
    return removeItemFromMap(oid, animation, cid, 0);
  }

  public static byte[] removeItemFromMap(int oid, int animation, int cid, int slot) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
    mplew.write(animation); // 0 = Expire, 1 = without animation, 2 =
    // pickup, 4 = explode, 5 = pet pickup
    mplew.writeInt(oid);
    if (animation >= 2) {
      mplew.writeInt(cid);
      if (animation == 5) { // allow pet pickup?
        mplew.writeInt(slot);
      }
    }
    return mplew.getPacket();
  }

  public static byte[] updateCharLook(MapleCharacter chr) {
    return updateCharLook(chr, (byte) 1);
  }

  public static byte[] updateCharLook(MapleCharacter chr, byte mode) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
    mplew.writeInt(chr.getId());
    mplew.write(mode); // flags actually |
    switch (mode) {
      case 1:
        PacketHelper.addCharLook(mplew, chr, false);
        break;
      case 2:
        mplew.write(0); // ?
        break;
      case 4: // Carry Item effect
        mplew.write(0);
        break;
    }
    Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
    addRingInfo(mplew, rings.getLeft());
    addRingInfo(mplew, rings.getMid());
    addMRingInfo(mplew, rings.getRight(), chr);
    mplew.writeInt(0); // charid to follow

    return mplew.getPacket();
  }

  public static void addRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings) {
    mplew.write(rings.size());
    for (MapleRing ring : rings) {
      mplew.writeLong(ring.getRingId());
      mplew.writeLong(ring.getPartnerRingId());
      mplew.writeInt(ring.getItemId());
    }
  }

  public static void addMRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings, MapleCharacter chr) {
    mplew.write(rings.size());
    for (MapleRing ring : rings) {
      mplew.writeInt(chr.getId());
      mplew.writeInt(ring.getPartnerChrId());
      mplew.writeInt(ring.getItemId());
    }
  }

  public static byte[] dropInventoryItem(MapleInventoryType type, short src) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(HexTool.getByteArrayFromHexString("01 01 03"));
    mplew.write(type.getType());
    mplew.writeShort(src);
    if (src < 0) {
      mplew.write(1);
    }
    return mplew.getPacket();
  }

  public static byte[] dropInventoryItemUpdate(MapleInventoryType type, IItem item) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(HexTool.getByteArrayFromHexString("01 01 01"));
    mplew.write(type.getType());
    mplew.writeShort(item.getPosition());
    mplew.writeShort(item.getQuantity());

    return mplew.getPacket();
  }

  public static byte[] damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, byte direction,
                                    int reflect, boolean is_pg, int oid, int pos_x, int pos_y) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
    mplew.writeInt(cid);
    mplew.write(skill);
    mplew.writeInt(damage);
    mplew.writeInt(monsteridfrom);
    mplew.write(direction);

    if (reflect > 0) {
      mplew.write(reflect);
      mplew.write(is_pg ? 1 : 0);
      mplew.writeInt(oid);
      mplew.write(6);
      mplew.writeShort(pos_x);
      mplew.writeShort(pos_y);
      mplew.write(0);
    } else {
      mplew.writeShort(0);
    }
    mplew.writeInt(damage);
    if (fake > 0) {
      mplew.writeInt(fake);
    }
    return mplew.getPacket();
  }


  public static final byte[] updateInfoQuest(final int quest, final String data) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(11);
    mplew.writeShort(quest);
    mplew.writeMapleAsciiString(data);

    return mplew.getPacket();
  }

  public static byte[] updateQuestInfo(MapleCharacter c, int quest, int npc, byte progress) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
    mplew.write(progress);
    mplew.writeShort(quest);
    mplew.writeInt(npc);
    mplew.writeInt(0);

    return mplew.getPacket();
  }

  public static byte[] updateQuestFinish(int quest, int npc, int nextquest) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
    mplew.write(8);
    mplew.writeShort(quest);
    mplew.writeInt(npc);
    mplew.writeInt(nextquest);
    return mplew.getPacket();
  }

  public static final byte[] charInfo(final MapleCharacter chr, final boolean isSelf) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CHARACTER_INFO.getValue());
    mplew.writeInt(chr.getId());
    mplew.write(chr.getLevel());
    mplew.writeShort(chr.getJob());
    mplew.writeShort(chr.getFame());
    mplew.write(chr.getMarriageId() > 0 ? 1 : 0); // heart red or gray

    if (chr.getGuildId() <= 0) {
      mplew.writeMapleAsciiString("-");
      mplew.writeMapleAsciiString("");
    } else {
      final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
      if (gs != null) {
        mplew.writeMapleAsciiString(gs.getName());
        if (gs.getAllianceId() > 0) {
          final MapleGuildAlliance allianceName = World.Alliance.getAlliance(gs.getAllianceId());
          if (allianceName != null) {
            mplew.writeMapleAsciiString(allianceName.getName());
          } else {
            mplew.writeMapleAsciiString("");
          }
        } else {
          mplew.writeMapleAsciiString("");
        }
      } else {
        mplew.writeMapleAsciiString("-");
        mplew.writeMapleAsciiString("");
      }

    }
    mplew.write(isSelf ? 1 : 0);

    final IItem inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -114);
    final int peteqid = inv != null ? inv.getItemId() : 0;

    for (final MaplePet pet : chr.getPets()) {
      if (pet.getSummoned()) {
        mplew.write(pet.getUniqueId()); // o-o byte ?
        mplew.writeInt(pet.getPetItemId()); // petid
        mplew.writeMapleAsciiString(pet.getName());
        mplew.write(pet.getLevel()); // pet level
        mplew.writeShort(pet.getCloseness()); // pet closeness
        mplew.write(pet.getFullness()); // pet fullness
        mplew.writeShort(0);
        mplew.writeInt(peteqid);
      }
    }
    mplew.write(0); // End of pet

    if (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -22) != null) {
      final int itemid = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -22).getItemId();
      final MapleMount mount = chr.getMount();
      final boolean canwear = MapleItemInformationProvider.getInstance().getReqLevel(itemid) <= chr.getLevel();
      mplew.write(canwear ? 1 : 0);
      if (canwear) {
        mplew.writeInt(mount.getLevel());
        mplew.writeInt(mount.getExp());
        mplew.writeInt(mount.getFatigue());
      }
    } else {
      mplew.write(0);
    }

    final int wishlistSize = chr.getWishlistSize();
    mplew.write(wishlistSize);
    if (wishlistSize > 0) {
      final int[] wishlist = chr.getWishlist();
      for (int x = 0; x < wishlistSize; x++) {
        mplew.writeInt(wishlist[x]);
      }
    }
    chr.getMonsterBook().addCharInfoPacket(chr.getMonsterBookCover(), mplew);

    IItem medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -46);
    mplew.writeInt(medal == null ? 0 : medal.getItemId());
    List<Integer> medalQuests = new ArrayList<Integer>();
    List<MapleQuestStatus> completed = chr.getCompletedQuests();
    for (MapleQuestStatus q : completed) {
      if (q.getQuest().getMedalItem() > 0
          && GameConstants.getInventoryType(q.getQuest().getMedalItem()) == MapleInventoryType.EQUIP) { // chair
        // kind
        // medal
        // viewmedal
        // is
        // weird
        medalQuests.add(q.getQuest().getId());
      }
    }
    mplew.writeShort(medalQuests.size());
    for (int x : medalQuests) {
      mplew.writeShort(x);
    }
    // v90 New
    List<Integer> chairs = new ArrayList<Integer>();
    for (IItem item : chr.getInventory(MapleInventoryType.SETUP)) {
      if (item.getItemId() / 10000 == 301 && !chairs.contains(item.getItemId())) {
        chairs.add(item.getItemId());
      }
    }
    mplew.writeInt(chairs.size());
    for (Integer ch : chairs) {
      mplew.writeInt(ch);
    }
    return mplew.getPacket();
  }

  private static void writeLongMask(MaplePacketLittleEndianWriter mplew, List<Pair<MapleBuffStat, Integer>> statups) {
    long firstmask = 0;
    long secondmask = 0;
    for (Pair<MapleBuffStat, Integer> statup : statups) {// TODO: implement
      // isFirst
      if (statup.getLeft().isFirst() == 2) {
        firstmask |= statup.getLeft().getValue();
      } else {
        secondmask |= statup.getLeft().getValue();
      }
    }
    mplew.writeLong(firstmask);
    mplew.writeLong(secondmask);
  }

  // List<Pair<MapleDisease, Integer>>
  private static void writeLongDiseaseMask(MaplePacketLittleEndianWriter mplew,
                                           List<Pair<MapleDisease, Integer>> statups) {
    long firstmask = 0;
    long secondmask = 0;
    for (Pair<MapleDisease, Integer> statup : statups) {
      if (statup.getLeft().isFirst()) {
        firstmask |= statup.getLeft().getValue();
      } else {
        secondmask |= statup.getLeft().getValue();
      }
    }
    mplew.writeLong(firstmask);
    mplew.writeLong(secondmask);
  }

  private static void writeLongMaskFromList(MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups) {
    long firstmask = 0;
    long secondmask = 0;
    for (MapleBuffStat statup : statups) {
      if (statup.isFirst() == 2) {// TODO: implement isFirst
        firstmask |= statup.getValue();
      } else {
        secondmask |= statup.getValue();
      }
    }
    mplew.writeLong(firstmask);
    mplew.writeLong(secondmask);
  }

  public static byte[] giveMount(int buffid, int skillid, List<Pair<MapleBuffStat, Integer>> statups) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
    writeLongMask(mplew, statups);

    mplew.writeShort(0);
    mplew.writeInt(buffid); // 1902000 saddle
    mplew.writeInt(skillid); // skillid
    mplew.writeInt(0); // Server tick value
    mplew.writeShort(0);
    mplew.write(0);
    mplew.write(2); // Total buffed times

    return mplew.getPacket();
  }

  public static byte[] givePirate(List<Pair<MapleBuffStat, Integer>> statups, int duration, int skillid) {
    final boolean infusion = skillid == 5121009 || skillid == 15111005;
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
    writeLongMask(mplew, statups);

    mplew.writeShort(0);
    for (Pair<MapleBuffStat, Integer> stat : statups) {
      mplew.writeInt(stat.getRight().intValue());
      mplew.writeLong(skillid);
      mplew.writeZeroBytes(infusion ? 6 : 1);
      mplew.writeShort(duration);
    }
    mplew.writeShort(infusion ? 600 : 0);
    if (!infusion) {
      mplew.write(1); // does this only come in dash?
    }
    return mplew.getPacket();
  }

  public static byte[] giveForeignPirate(List<Pair<MapleBuffStat, Integer>> statups, int duration, int cid,
                                         int skillid) {
    final boolean infusion = skillid == 5121009 || skillid == 15111005;
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
    mplew.writeInt(cid);
    writeLongMask(mplew, statups);
    mplew.writeShort(0);
    for (Pair<MapleBuffStat, Integer> stat : statups) {
      mplew.writeInt(stat.getRight().intValue());
      mplew.writeLong(skillid);
      mplew.writeZeroBytes(infusion ? 7 : 1);
      mplew.writeShort(duration);// duration... seconds
    }
    mplew.writeShort(infusion ? 600 : 0);
    return mplew.getPacket();
  }

  public static byte[] giveHoming(int skillid, int mobid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
    mplew.writeLong(MapleBuffStat.HOMING_BEACON.getValue());
    mplew.writeLong(0);

    mplew.writeShort(0);
    mplew.writeInt(1);
    mplew.writeLong(skillid);
    mplew.write(0);
    mplew.writeInt(mobid);
    mplew.writeShort(0);
    return mplew.getPacket();
  }

  public static byte[] giveEnergyChargeTest(int bar, int bufflength) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
    mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
    mplew.writeLong(0);
    mplew.writeShort(0);
    mplew.writeInt(0);
    mplew.writeInt(1555445060); // ?
    mplew.writeShort(0);
    mplew.writeInt(Math.min(bar, 10000)); // 0 = no bar, 10000 = full bar
    mplew.writeLong(0); // skillid, but its 0 here
    mplew.write(0);
    mplew.writeInt(bar >= 10000 ? bufflength : 0);// short - bufflength...50
    return mplew.getPacket();
  }

  public static byte[] giveEnergyChargeTest(int cid, int bar, int bufflength) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
    mplew.writeInt(cid);
    mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
    mplew.writeLong(0);
    mplew.writeShort(0);
    mplew.writeInt(0);
    mplew.writeInt(1555445060); // ?
    mplew.writeShort(0);
    mplew.writeInt(Math.min(bar, 10000)); // 0 = no bar, 10000 = full bar
    mplew.writeLong(0); // skillid, but its 0 here
    mplew.write(0);
    mplew.writeInt(bar >= 10000 ? bufflength : 0);// short - bufflength...50
    return mplew.getPacket();
  }

  public static byte[] giveEnergyCharge(int barammount) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
    mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
    mplew.writeZeroBytes(10);
    mplew.writeShort(barammount);
    mplew.writeZeroBytes(11);
    mplew.writeInt(50);
    return mplew.getPacket();
  }

  public static byte[] giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups,
                                MapleStatEffect effect) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
    writeLongMask(mplew, statups);

    for (Pair<MapleBuffStat, Integer> statup : statups) {
      mplew.writeShort(statup.getRight().shortValue());
      mplew.writeInt(buffid);
      mplew.writeInt(bufflength);
    }
    mplew.writeShort(0); // delay, wk charges have 600 here o.o
    mplew.writeShort(0); // combo 600, too
    if (effect == null || (!effect.isCombo() && !effect.isFinalAttack())) {
      mplew.write(0); // Test
    }

    return mplew.getPacket();
  }

  public static byte[] giveDebuff(final List<Pair<MapleDisease, Integer>> statups, int skillid, int level,
                                  int duration) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());

    writeLongDiseaseMask(mplew, statups);

    for (Pair<MapleDisease, Integer> statup : statups) {
      mplew.writeShort(statup.getRight().shortValue());
      mplew.writeShort(skillid);
      mplew.writeShort(level);
      mplew.writeInt(duration);
    }
    mplew.writeShort(0); // ??? wk charges have 600 here o.o
    mplew.writeShort(900); // Delay
    mplew.write(1);

    return mplew.getPacket();
  }

  public static byte[] giveForeignDebuff(int cid, final List<Pair<MapleDisease, Integer>> statups, int skillid,
                                         int level) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
    mplew.writeInt(cid);

    writeLongDiseaseMask(mplew, statups);

    if (skillid == 125) {
      mplew.writeShort(0);
    }
    mplew.writeShort(skillid);
    mplew.writeShort(level);
    mplew.writeShort(0); // same as give_buff
    mplew.writeShort(900); // Delay

    return mplew.getPacket();
  }

  public static byte[] cancelForeignDebuff(int cid, long mask, boolean first) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
    mplew.writeInt(cid);
    mplew.writeLong(first ? mask : 0);
    mplew.writeLong(first ? 0 : mask);

    return mplew.getPacket();
  }

  public static byte[] showMonsterRiding(int cid, List<Pair<MapleBuffStat, Integer>> statups, int itemId,
                                         int skillId) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
    mplew.writeInt(cid);

    writeLongMask(mplew, statups);

    mplew.writeShort(0);
    mplew.writeInt(itemId);
    mplew.writeInt(skillId);
    mplew.writeInt(0);
    mplew.writeShort(0);
    mplew.write(0);
    mplew.write(0);

    return mplew.getPacket();
  }

  public static byte[] giveForeignBuff(int cid, List<Pair<MapleBuffStat, Integer>> statups, MapleStatEffect effect) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
    mplew.writeInt(cid);

    writeLongMask(mplew, statups);

    for (Pair<MapleBuffStat, Integer> statup : statups) {
      mplew.writeShort(statup.getRight().shortValue());
    }
    mplew.writeShort(0); // same as give_buff
    if (effect.isMorph()) {
      mplew.write(0);
    }
    mplew.write(0);

    return mplew.getPacket();
  }

  public static byte[] cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
    mplew.writeInt(cid);

    writeLongMaskFromList(mplew, statups);

    return mplew.getPacket();
  }

  public static byte[] cancelBuff(List<MapleBuffStat> statups) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
    if (statups != null) {
      writeLongMaskFromList(mplew, statups);
      mplew.write(3);
    } else {
      mplew.writeLong(0);
      mplew.writeInt(0x40);
      mplew.writeInt(0x1000);
    }

    return mplew.getPacket();
  }

  public static byte[] cancelHoming() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
    mplew.writeLong(MapleBuffStat.HOMING_BEACON.getValue());
    mplew.writeLong(0);

    return mplew.getPacket();
  }

  public static byte[] cancelDebuff(long mask, boolean first) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
    mplew.writeLong(first ? mask : 0);
    mplew.writeLong(first ? 0 : mask);
    mplew.write(1);

    return mplew.getPacket();
  }

  public static byte[] updateMount(MapleCharacter chr, boolean levelup) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
    mplew.writeInt(chr.getId());
    mplew.writeInt(chr.getMount().getLevel());
    mplew.writeInt(chr.getMount().getExp());
    mplew.writeInt(chr.getMount().getFatigue());
    mplew.write(levelup ? 1 : 0);

    return mplew.getPacket();
  }

  public static byte[] getPlayerShopNewVisitor(MapleCharacter c, int slot) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
    PacketHelper.addCharLook(mplew, c, false);
    mplew.writeMapleAsciiString(c.getName());
    mplew.writeShort(c.getJob());

    return mplew.getPacket();
  }

  public static byte[] getPlayerShopRemoveVisitor(int slot) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(PlayerInteractionHandler.EXIT);
    if (slot > 0) {
      mplew.writeShort(slot);
    }

    return mplew.getPacket();
  }

  public static byte[] getTradePartnerAdd(MapleCharacter c) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(4);
    mplew.write(1);
    PacketHelper.addCharLook(mplew, c, false);
    mplew.writeMapleAsciiString(c.getName());
    mplew.writeShort(c.getJob());

    return mplew.getPacket();
  }

  public static byte[] getTradeInvite(MapleCharacter c) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(2);
    mplew.write(3);
    mplew.writeMapleAsciiString(c.getName());
    mplew.writeInt(0); // Trade ID

    return mplew.getPacket();
  }

  public static byte[] getTradeMesoSet(byte number, int meso) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x10);
    mplew.write(number);
    mplew.writeInt(meso);

    return mplew.getPacket();
  }

  public static byte[] getTradeItemAdd(byte number, IItem item) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0xF);
    mplew.write(number);
    PacketHelper.addItemInfo(mplew, item, false, false, true);

    return mplew.getPacket();
  }

  public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(5);
    mplew.write(3);
    mplew.write(2);
    mplew.write(number);

    if (number == 1) {
      mplew.write(0);
      PacketHelper.addCharLook(mplew, trade.getPartner().getChr(), false);
      mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
      mplew.writeShort(trade.getPartner().getChr().getJob());
    }
    mplew.write(number);
    PacketHelper.addCharLook(mplew, c.getPlayer(), false);
    mplew.writeMapleAsciiString(c.getPlayer().getName());
    mplew.writeShort(c.getPlayer().getJob());
    mplew.write(0xFF);

    return mplew.getPacket();
  }

  public static byte[] getTradeConfirmation() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x11); // or 7? what

    return mplew.getPacket();
  }

  public static byte[] TradeMessage(final byte UserSlot, final byte message) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0xA);
    mplew.write(UserSlot);
    mplew.write(message);
    // 0x02 = cancelled
    // 0x07 = success [tax is automated]
    // 0x08 = unsuccessful
    // 0x09 = "You cannot make the trade because there are some items which
    // you cannot carry more than one."
    // 0x0A = "You cannot make the trade because the other person's on a
    // different map."

    return mplew.getPacket();
  }

  public static byte[] getTradeCancel(final byte UserSlot, final int unsuccessful) { // 0
    // =
    // canceled
    // 1
    // =
    // invent
    // space
    // 2
    // =
    // pickuprestricted
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0xA);
    mplew.write(UserSlot);
    mplew.write(unsuccessful == 0 ? 2 : (unsuccessful == 1 ? 8 : 9));

    return mplew.getPacket();
  }

  public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type) {
    return getNPCTalk(npc, msgType, talk, endBytes, type, 0);
  }

  public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type, int OtherNPC) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
    mplew.write(4);
    mplew.writeInt(npc);
    mplew.write(msgType);
    mplew.write(type); // 1 = No ESC, 3 = show character + no sec
    if (type >= 4 && type <= 5) {
      mplew.writeInt(OtherNPC);
    }
    mplew.writeMapleAsciiString(talk);
    mplew.write(HexTool.getByteArrayFromHexString(endBytes));

    return mplew.getPacket();
  }

  public static final byte[] getMapSelection(final int npcid, final String sel) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
    mplew.write(4);
    mplew.writeInt(npcid);
    mplew.writeShort(15);

    mplew.writeInt(0); // type, usually is 0, or 1 // If type is 1, then sel
    // is 0-6, else is 0-7 and 99
    mplew.writeInt(5);
    mplew.writeMapleAsciiString(sel);

    return mplew.getPacket();
  }

  public static final byte[] getSpeedQuiz(int npc, byte type, int oid, int points, int questionNo, int time) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
    mplew.write(4);
    mplew.writeInt(npc);
    mplew.writeShort(7); // Speed quiz
    mplew.write(0); // 1 = close
    mplew.writeInt(type); // Type: 0 = NPC, 1 = Mob, 2 = Item
    mplew.writeInt(oid); // Object id
    mplew.writeInt(points); // points
    mplew.writeInt(questionNo); // questions
    mplew.writeInt(time); // time in seconds

    return mplew.getPacket();
  }

  public static final byte[] getQuiz() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
    mplew.write(4);
    mplew.writeInt(9010010);
    mplew.write(6); // quiz
    mplew.write(0);

    mplew.write(0); // 1 = close
    mplew.writeMapleAsciiString("123"); // Main topic
    mplew.writeMapleAsciiString("     123"); // Question
    mplew.writeMapleAsciiString(" none"); // Clue
    mplew.writeInt(10); // min characters
    mplew.writeInt(20); // max characters
    mplew.writeInt(30); // time in seconds

    return mplew.getPacket();
  }

  public static byte[] getNPCTalkStyle(int npc, String talk, int... args) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
    mplew.write(4);
    mplew.writeInt(npc);
    mplew.writeShort(8);
    mplew.writeMapleAsciiString(talk);
    mplew.write(args.length);

    for (int i = 0; i < args.length; i++) {
      mplew.writeInt(args[i]);
    }
    return mplew.getPacket();
  }

  public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
    mplew.write(4);
    mplew.writeInt(npc);
    mplew.writeShort(4);
    mplew.writeMapleAsciiString(talk);
    mplew.writeInt(def);
    mplew.writeInt(min);
    mplew.writeInt(max);
    mplew.writeInt(0);

    return mplew.getPacket();
  }

  public static byte[] getNPCTalkText(int npc, String talk) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
    mplew.write(4);
    mplew.writeInt(npc);
    mplew.writeShort(3);
    mplew.writeMapleAsciiString(talk);
    mplew.writeInt(0);
    mplew.writeInt(0);

    return mplew.getPacket();
  }

  public static byte[] showForeignEffect(int cid, int effect) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
    mplew.writeInt(cid);
    mplew.write(effect); // 0 = Level up, 8 = job change

    return mplew.getPacket();
  }

  public static byte[] showBuffeffect(int cid, int skillid, int effectid) {
    return showBuffeffect(cid, skillid, effectid, (byte) 3);
  }

  public static byte[] showBuffeffect(int cid, int skillid, int effectid, byte direction) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
    mplew.writeInt(cid);
    mplew.write(effectid); // ehh?
    mplew.writeInt(skillid);
    mplew.write(1); // skill level = 1 for the lulz
    mplew.write(1); // actually skill level ? 0 = dosnt show
    if (direction != (byte) 3) {
      mplew.write(direction);
    }
    return mplew.getPacket();
  }

  public static byte[] showOwnBuffEffect(int skillid, int effectid) {
    return showOwnBuffEffect(skillid, effectid, (byte) 3);
  }

  public static byte[] showOwnBuffEffect(int skillid, int effectid, byte direction) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    mplew.write(effectid);
    mplew.writeInt(skillid);
    mplew.write(1); // skill level = 1 for the lulz
    mplew.write(1); // 0 = doesnt show? or is this even here
    if (direction != (byte) 3) {
      mplew.write(direction);
    }

    return mplew.getPacket();
  }

  public static byte[] showItemLevelupEffect() {
    return showSpecialEffect(15);
  }

  public static byte[] showForeignItemLevelupEffect(int cid) {
    return showSpecialEffect(cid, 15);
  }

  public static byte[] showSpecialEffect(int effect) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    mplew.write(effect);

    return mplew.getPacket();
  }

  public static byte[] showSpecialEffect(int cid, int effect) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
    mplew.writeInt(cid);
    mplew.write(effect);

    return mplew.getPacket();
  }

  public static byte[] updateSkill(int skillid, int level, int masterlevel, long expiration) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SKILLS_UPDATE.getValue());
    mplew.write(1);
    mplew.writeShort(1);
    mplew.writeInt(skillid);
    mplew.writeInt(level);
    mplew.writeInt(masterlevel);
    PacketHelper.addExpirationTime(mplew, expiration);
    mplew.write(4);

    return mplew.getPacket();
  }

  public static byte[] updateSkill(final Map<ISkill, SkillEntry> skills) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SKILLS_UPDATE.getValue());
    mplew.write(1);
    mplew.writeShort(skills.size());
    for (final Entry<ISkill, SkillEntry> sk : skills.entrySet()) {
      mplew.writeInt(sk.getKey().getId());
      mplew.writeInt(sk.getValue().skillevel);
      mplew.writeInt(sk.getValue().masterlevel);
      PacketHelper.addExpirationTime(mplew, sk.getValue().expiration);
    }
    mplew.write(4);

    return mplew.getPacket();
  }

  public static final byte[] updateQuestMobKills(final MapleQuestStatus status) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(1);
    mplew.writeShort(status.getQuest().getId());
    mplew.write(1);

    final StringBuilder sb = new StringBuilder();
    for (final int kills : status.getMobKills().values()) {
      sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
    }
    mplew.writeMapleAsciiString(sb.toString());
    mplew.writeZeroBytes(8);

    return mplew.getPacket();
  }

  public static byte[] getShowQuestCompletion(int id) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
    mplew.writeShort(id);

    return mplew.getPacket();
  }

  public static byte[] getKeymap(MapleKeyLayout layout) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());
    mplew.write(0);
    layout.writeData(mplew);

    return mplew.getPacket();
  }

  public static byte[] getQuickSlot(final String qs) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.QUICK_SLOT.getValue());
    mplew.write((qs == null || qs.equals("")) ? 0 : 1);
    if (qs != null && !qs.equals("")) {
      final String[] slots = qs.split(",");
      for (int i = 0; i < 8; i++) {
        mplew.writeInt(Integer.parseInt(slots[i]));
      }
    }

    return mplew.getPacket();
  }

  public static byte[] getWhisper(String sender, int channel, String text) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
    mplew.write(0x12);
    mplew.writeMapleAsciiString(sender);
    mplew.writeShort(channel - 1);
    mplew.writeMapleAsciiString(text);

    return mplew.getPacket();
  }

  public static byte[] getWhisperReply(String target, byte reply) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
    mplew.write(0x0A); // whisper?
    mplew.writeMapleAsciiString(target);
    mplew.write(reply);// 0x0 = cannot find char, 0x1 = success

    return mplew.getPacket();
  }

  public static byte[] getFindReplyWithMap(String target, int mapid, final boolean buddy) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
    mplew.write(buddy ? 72 : 9);
    mplew.writeMapleAsciiString(target);
    mplew.write(1);
    mplew.writeInt(mapid);
    mplew.writeZeroBytes(8); // ?? official doesn't send zeros here but
    // whatever

    return mplew.getPacket();
  }

  public static byte[] getFindReply(String target, int channel, final boolean buddy) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
    mplew.write(buddy ? 72 : 9);
    mplew.writeMapleAsciiString(target);
    mplew.write(3);
    mplew.writeInt(channel - 1);

    return mplew.getPacket();
  }

  public static byte[] getInventoryFull() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(1);
    mplew.write(0);

    return mplew.getPacket();
  }

  public static byte[] getShowInventoryFull() {
    return getShowInventoryStatus(0xff);
  }

  public static byte[] showItemUnavailable() {
    return getShowInventoryStatus(0xfe);
  }

  public static byte[] getShowInventoryStatus(int mode) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(0);
    mplew.write(mode);
    mplew.writeInt(0);
    mplew.writeInt(0);

    return mplew.getPacket();
  }

  public static byte[] getStorage(int npcId, byte slots, Collection<IItem> items, int meso) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
    mplew.write(0x16);
    mplew.writeInt(npcId);
    mplew.write(slots);
    mplew.writeShort(0x7E);
    mplew.writeShort(0);
    mplew.writeInt(0);
    mplew.writeInt(meso);
    mplew.writeShort(0);
    mplew.write((byte) items.size());
    for (IItem item : items) {
      PacketHelper.addItemInfo(mplew, item, true, true);
    }
    mplew.writeShort(0);
    mplew.write(0);

    return mplew.getPacket();
  }

  public static byte[] getStorageFull() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
    mplew.write(0x11);

    return mplew.getPacket();
  }

  public static byte[] mesoStorage(byte slots, int meso) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
    mplew.write(0x13);
    mplew.write(slots);
    mplew.writeShort(2);
    mplew.writeShort(0);
    mplew.writeInt(0);
    mplew.writeInt(meso);

    return mplew.getPacket();
  }

  public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
    mplew.write(0x0D);
    mplew.write(slots);
    mplew.writeShort(type.getBitfieldEncoding());
    mplew.writeShort(0);
    mplew.writeInt(0);
    mplew.write(items.size());
    for (IItem item : items) {
      PacketHelper.addItemInfo(mplew, item, true, true);
    }
    return mplew.getPacket();
  }

  public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
    mplew.write(0x9);
    mplew.write(slots);
    mplew.writeShort(type.getBitfieldEncoding());
    mplew.writeShort(0);
    mplew.writeInt(0);
    mplew.write(items.size());
    for (IItem item : items) {
      PacketHelper.addItemInfo(mplew, item, true, true);
    }
    return mplew.getPacket();
  }

  public static byte[] fairyPendantMessage(int percent) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BONUS_EXP_CHANGED.getValue());
    mplew.writeInt(0x11); // 0x11 = pendant, 0x31 = evan medal
    mplew.writeInt(0); // GMS doens't send hour here.
    mplew.writeInt(percent);

    return mplew.getPacket();
  }

  public static byte[] giveFameResponse(int mode, String charname, int newfame) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
    mplew.write(0);
    mplew.writeMapleAsciiString(charname);
    mplew.write(mode); // 1 or 0
    mplew.writeInt(newfame);

    return mplew.getPacket();
  }

  public static byte[] giveFameErrorResponse(int status) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    // 1: The user name is incorrectly entered.
    // 2: Users under leve l5 are unable to toggle with fame.
    // 3: You can't raise or drop a level anymore for today.
    // 4: You can't raise or drop a level of fame of that character anymore
    // for this month.
    // 6: The level of fame has neither been raise or dropped due to an
    // unexpected error.
    mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
    mplew.write(status);

    return mplew.getPacket();
  }

  public static byte[] receiveFame(int mode, String charnameFrom) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
    mplew.write(5);
    mplew.writeMapleAsciiString(charnameFrom);
    mplew.write(mode); // 1 : Raised, 0 : Dropped

    return mplew.getPacket();
  }

  public static byte[] multiChat(String name, String chattext, int mode) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue());
    mplew.write(mode); // 0 buddychat; 1 partychat; 2 guildchat
    mplew.writeMapleAsciiString(name);
    mplew.writeMapleAsciiString(chattext);

    return mplew.getPacket();
  }

  public static byte[] getClock(int time) { // time in seconds
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
    mplew.write(2); // clock type. if you send 3 here you have to send
    // another byte (which does not matter at all) before
    // the timestamp
    mplew.writeInt(time);

    return mplew.getPacket();
  }

  public static byte[] getClockTime(int hour, int min, int sec) { // Current
    // Time
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
    mplew.write(1); // Clock-Type
    mplew.write(hour);
    mplew.write(min);
    mplew.write(sec);

    return mplew.getPacket();
  }

  public static byte[] spawnMist(final MapleMist mist) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
    mplew.writeInt(mist.getObjectId());
    mplew.writeInt(mist.isMobMist() ? 0 : (mist.isPoisonMist() != 0 ? 1 : 2));
    mplew.writeInt(mist.getOwnerId());
    if (mist.getMobSkill() == null) {
      mplew.writeInt(mist.getSourceSkill().getId());
    } else {
      mplew.writeInt(mist.getMobSkill().getSkillId());
    }
    mplew.write(mist.getSkillLevel());
    mplew.writeShort(mist.getSkillDelay());
    mplew.writeInt(mist.getBox().x);
    mplew.writeInt(mist.getBox().y);
    mplew.writeInt(mist.getBox().x + mist.getBox().width);
    mplew.writeInt(mist.getBox().y + mist.getBox().height);
    mplew.writeInt(0);
    mplew.writeInt(0);

    return mplew.getPacket();
  }

  public static byte[] removeMist(final int oid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
    mplew.writeInt(oid);

    return mplew.getPacket();
  }

  public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
    mplew.writeInt(cid);
    mplew.writeInt(summonSkillId);
    mplew.write(unkByte);
    mplew.writeInt(damage);
    mplew.writeInt(monsterIdFrom);
    mplew.write(0);

    return mplew.getPacket();
  }

  public static byte[] buddylistMessage(byte message) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
    mplew.write(message);

    return mplew.getPacket();
  }

  public static byte[] updateBuddylist(byte action, Collection<BuddyListEntry> buddylist) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
    mplew.write(action);
    mplew.write(buddylist.size());

    for (BuddyListEntry buddy : buddylist) {
      mplew.writeInt(buddy.getCharacterId());
      mplew.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getName(), '\0', 13));
      mplew.write(0);
      mplew.writeInt(buddy.getChannel() == -1 ? -1 : buddy.getChannel() - 1);
      mplew.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getGroup(), '\0', 17));
    }
    for (int x = 0; x < buddylist.size(); x++) {
      mplew.writeInt(0);
    }
    return mplew.getPacket();
  }

  public static byte[] requestBuddylistAdd(int cidFrom, String nameFrom, int levelFrom, int jobFrom) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
    mplew.write(9);
    mplew.writeInt(cidFrom);
    mplew.writeMapleAsciiString(nameFrom);
    mplew.writeInt(levelFrom);
    mplew.writeInt(jobFrom);
    mplew.writeInt(cidFrom);
    mplew.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\0', 13));
    mplew.write(1);
    mplew.writeInt(0);
    mplew.writeAsciiString(StringUtil.getRightPaddedStr("Default Group", '\0', 16));
    mplew.writeShort(1);

    return mplew.getPacket();
  }

  public static byte[] updateBuddyChannel(int characterid, int channel) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
    mplew.write(0x14);
    mplew.writeInt(characterid);
    mplew.write(0);
    mplew.writeInt(channel);

    return mplew.getPacket();
  }

  public static byte[] itemEffect(int characterid, int itemid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
    mplew.writeInt(characterid);
    mplew.writeInt(itemid);

    return mplew.getPacket();
  }

  public static byte[] updateBuddyCapacity(int capacity) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
    mplew.write(0x15);
    mplew.write(capacity);

    return mplew.getPacket();
  }

  public static byte[] showChair(int characterid, int itemid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
    mplew.writeInt(characterid);
    mplew.writeInt(itemid);

    return mplew.getPacket();
  }

  public static byte[] cancelChair(int id) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
    if (id == -1) {
      mplew.write(0);
    } else {
      mplew.write(1);
      mplew.writeShort(id);
    }
    return mplew.getPacket();
  }

  public static byte[] spawnReactor(MapleReactor reactor) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
    mplew.writeInt(reactor.getObjectId());
    mplew.writeInt(reactor.getReactorId());
    mplew.write(reactor.getState());
    mplew.writePos(reactor.getPosition());
    mplew.write(reactor.getFacingDirection()); // stance
    mplew.writeMapleAsciiString(reactor.getName());

    return mplew.getPacket();
  }

  public static byte[] triggerReactor(MapleReactor reactor, int stance) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
    mplew.writeInt(reactor.getObjectId());
    mplew.write(reactor.getState());
    mplew.writePos(reactor.getPosition());
    mplew.writeShort(stance);
    mplew.write(0);
    mplew.write(4); // frame delay, set to 5 since there doesn't appear to
    // be a fixed formula for it

    return mplew.getPacket();
  }

  public static byte[] destroyReactor(MapleReactor reactor) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
    mplew.writeInt(reactor.getObjectId());
    mplew.write(reactor.getState());
    mplew.writePos(reactor.getPosition());

    return mplew.getPacket();
  }

  public static byte[] musicChange(String song) {
    return environmentChange(song, 6);
  }

  public static byte[] showEffect(String effect) {
    return environmentChange(effect, 3);
  }

  public static byte[] playSound(String sound) {
    return environmentChange(sound, 4);
  }

  public static byte[] environmentChange(String env, int mode) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
    mplew.write(mode);
    mplew.writeMapleAsciiString(env);

    return mplew.getPacket();
  }

  public static byte[] environmentMove(String env, int mode) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MOVE_ENV.getValue());
    mplew.writeMapleAsciiString(env);
    mplew.writeInt(mode);

    return mplew.getPacket();
  }

  public static byte[] startMapEffect(String msg, int itemid, boolean active) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
    mplew.write(active ? 0 : 1);

    mplew.writeInt(itemid);
    if (active) {
      mplew.writeMapleAsciiString(msg);
    }
    return mplew.getPacket();
  }

  public static byte[] removeMapEffect() {
    return startMapEffect(null, 0, false);
  }

  public static byte[] showGuildInfo(MapleCharacter c) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x1A); // signature for showing guild info

    if (c == null || c.getMGC() == null) { // show empty guild (used for
      // leaving, expelled)
      mplew.write(0);
      return mplew.getPacket();
    }
    MapleGuild g = World.Guild.getGuild(c.getGuildId());
    if (g == null) { // failed to read from DB - don't show a guild
      mplew.write(0);
      return mplew.getPacket();
    }
    mplew.write(1); // bInGuild
    getGuildInfo(mplew, g);

    return mplew.getPacket();
  }

  private static void getGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
    mplew.writeInt(guild.getId());
    mplew.writeMapleAsciiString(guild.getName());
    for (int i = 1; i <= 5; i++) {
      mplew.writeMapleAsciiString(guild.getRankTitle(i));
    }
    guild.addMemberData(mplew);
    mplew.writeInt(guild.getCapacity());
    mplew.writeShort(guild.getLogoBG());
    mplew.write(guild.getLogoBGColor());
    mplew.writeShort(guild.getLogo());
    mplew.write(guild.getLogoColor());
    mplew.writeMapleAsciiString(guild.getNotice());
    mplew.writeInt(guild.getGP());
    mplew.writeInt(guild.getAllianceId() > 0 ? guild.getAllianceId() : 0);
  }

  public static byte[] guildMemberOnline(int gid, int cid, boolean bOnline) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x3d);
    mplew.writeInt(gid);
    mplew.writeInt(cid);
    mplew.write(bOnline ? 1 : 0);

    return mplew.getPacket();
  }

  public static byte[] guildInvite(int gid, String charName, int levelFrom, int jobFrom) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x05);
    mplew.writeInt(gid);
    mplew.writeMapleAsciiString(charName);
    mplew.writeInt(levelFrom);
    mplew.writeInt(jobFrom);

    return mplew.getPacket();
  }

  public static byte[] denyGuildInvitation(String charname) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x37);
    mplew.writeMapleAsciiString(charname);

    return mplew.getPacket();
  }

  public static byte[] genericGuildMessage(byte code) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(code);

    return mplew.getPacket();
  }

  public static byte[] newGuildMember(MapleGuildCharacter mgc) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x27);
    mplew.writeInt(mgc.getGuildId());
    mplew.writeInt(mgc.getId());
    mplew.writeAsciiString(StringUtil.getRightPaddedStr(mgc.getName(), '\0', 13));
    mplew.writeInt(mgc.getJobId());
    mplew.writeInt(mgc.getLevel());
    mplew.writeInt(mgc.getGuildRank()); // should be always 5 but whatevs
    mplew.writeInt(mgc.isOnline() ? 1 : 0); // should always be 1 too
    mplew.writeInt(1); // ? could be guild signature, but doesn't seem to
    // matter
    mplew.writeInt(mgc.getAllianceRank()); // should always 3

    return mplew.getPacket();
  }

  // someone leaving, mode == 0x2c for leaving, 0x2f for expelled
  public static byte[] memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(bExpelled ? 0x2f : 0x2c);

    mplew.writeInt(mgc.getGuildId());
    mplew.writeInt(mgc.getId());
    mplew.writeMapleAsciiString(mgc.getName());

    return mplew.getPacket();
  }

  public static byte[] changeRank(MapleGuildCharacter mgc) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x40);
    mplew.writeInt(mgc.getGuildId());
    mplew.writeInt(mgc.getId());
    mplew.write(mgc.getGuildRank());

    return mplew.getPacket();
  }

  public static byte[] guildNotice(int gid, String notice) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x44);
    mplew.writeInt(gid);
    mplew.writeMapleAsciiString(notice);

    return mplew.getPacket();
  }

  public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x3C);
    mplew.writeInt(mgc.getGuildId());
    mplew.writeInt(mgc.getId());
    mplew.writeInt(mgc.getLevel());
    mplew.writeInt(mgc.getJobId());

    return mplew.getPacket();
  }

  public static byte[] rankTitleChange(int gid, String[] ranks) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x3e);
    mplew.writeInt(gid);

    for (String r : ranks) {
      mplew.writeMapleAsciiString(r);
    }
    return mplew.getPacket();
  }

  public static byte[] guildDisband(int gid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x32);
    mplew.writeInt(gid);
    mplew.write(1);

    return mplew.getPacket();
  }

  public static byte[] guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x42);
    mplew.writeInt(gid);
    mplew.writeShort(bg);
    mplew.write(bgcolor);
    mplew.writeShort(logo);
    mplew.write(logocolor);

    return mplew.getPacket();
  }

  public static byte[] guildCapacityChange(int gid, int capacity) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x3a);
    mplew.writeInt(gid);
    mplew.write(capacity);

    return mplew.getPacket();
  }

  public static byte[] removeGuildFromAlliance(MapleGuildAlliance alliance, MapleGuild expelledGuild,
                                               boolean expelled) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x10);
    addAllianceInfo(mplew, alliance);
    getGuildInfo(mplew, expelledGuild);
    mplew.write(expelled ? 1 : 0); // 1 = expelled, 0 = left
    return mplew.getPacket();
  }

  public static byte[] changeAlliance(MapleGuildAlliance alliance, final boolean in) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x01);
    mplew.write(in ? 1 : 0);
    mplew.writeInt(in ? alliance.getId() : 0);
    final int noGuilds = alliance.getNoGuilds();
    MapleGuild[] g = new MapleGuild[noGuilds];
    for (int i = 0; i < noGuilds; i++) {
      g[i] = World.Guild.getGuild(alliance.getGuildId(i));
      if (g[i] == null) {
        return enableActions();
      }
    }
    mplew.write(noGuilds);
    for (int i = 0; i < noGuilds; i++) {
      mplew.writeInt(g[i].getId());
      // must be world
      Collection<MapleGuildCharacter> members = g[i].getMembers();
      mplew.writeInt(members.size());
      for (MapleGuildCharacter mgc : members) {
        mplew.writeInt(mgc.getId());
        mplew.write(in ? mgc.getAllianceRank() : 0);
      }
    }
    return mplew.getPacket();
  }

  public static byte[] changeAllianceLeader(int allianceid, int newLeader, int oldLeader) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x02);
    mplew.writeInt(allianceid);
    mplew.writeInt(oldLeader);
    mplew.writeInt(newLeader);
    return mplew.getPacket();
  }

  public static byte[] updateAllianceLeader(int allianceid, int newLeader, int oldLeader) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x19);
    mplew.writeInt(allianceid);
    mplew.writeInt(oldLeader);
    mplew.writeInt(newLeader);
    return mplew.getPacket();
  }

  public static byte[] sendAllianceInvite(String allianceName, MapleCharacter inviter) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x03);
    mplew.writeInt(inviter.getGuildId());
    mplew.writeMapleAsciiString(inviter.getName());
    // alliance invite did NOT change
    mplew.writeMapleAsciiString(allianceName);
    return mplew.getPacket();
  }

  public static byte[] changeGuildInAlliance(MapleGuildAlliance alliance, MapleGuild guild, final boolean add) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x04);
    mplew.writeInt(add ? alliance.getId() : 0);
    mplew.writeInt(guild.getId());
    Collection<MapleGuildCharacter> members = guild.getMembers();
    mplew.writeInt(members.size());
    for (MapleGuildCharacter mgc : members) {
      mplew.writeInt(mgc.getId());
      mplew.write(add ? mgc.getAllianceRank() : 0);
    }
    return mplew.getPacket();
  }

  public static byte[] changeAllianceRank(int allianceid, MapleGuildCharacter player) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x05);
    mplew.writeInt(allianceid);
    mplew.writeInt(player.getId());
    mplew.writeInt(player.getAllianceRank());
    return mplew.getPacket();
  }

  public static byte[] createGuildAlliance(MapleGuildAlliance alliance) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x0F);
    addAllianceInfo(mplew, alliance);
    final int noGuilds = alliance.getNoGuilds();
    MapleGuild[] g = new MapleGuild[noGuilds];
    for (int i = 0; i < alliance.getNoGuilds(); i++) {
      g[i] = World.Guild.getGuild(alliance.getGuildId(i));
      if (g[i] == null) {
        return enableActions();
      }
    }
    for (MapleGuild gg : g) {
      getGuildInfo(mplew, gg);
    }
    return mplew.getPacket();
  }

  public static byte[] getAllianceInfo(MapleGuildAlliance alliance) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x0C);
    mplew.write(alliance == null ? 0 : 1); // in an alliance
    if (alliance != null) {
      addAllianceInfo(mplew, alliance);
    }
    return mplew.getPacket();
  }

  public static byte[] getAllianceUpdate(MapleGuildAlliance alliance) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x17);
    addAllianceInfo(mplew, alliance);
    return mplew.getPacket();
  }

  public static byte[] getGuildAlliance(MapleGuildAlliance alliance) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x0D);
    if (alliance == null) {
      mplew.writeInt(0);
      return mplew.getPacket();
    }
    final int noGuilds = alliance.getNoGuilds();
    MapleGuild[] g = new MapleGuild[noGuilds];
    for (int i = 0; i < alliance.getNoGuilds(); i++) {
      g[i] = World.Guild.getGuild(alliance.getGuildId(i));
      if (g[i] == null) {
        return enableActions();
      }
    }
    mplew.writeInt(noGuilds);
    for (MapleGuild gg : g) {
      getGuildInfo(mplew, gg);
    }
    return mplew.getPacket();
  }

  public static byte[] addGuildToAlliance(MapleGuildAlliance alliance, MapleGuild newGuild) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x12);
    addAllianceInfo(mplew, alliance);
    mplew.writeInt(newGuild.getId()); // ???
    getGuildInfo(mplew, newGuild);
    mplew.write(0); // ???
    return mplew.getPacket();
  }

  private static void addAllianceInfo(MaplePacketLittleEndianWriter mplew, MapleGuildAlliance alliance) {
    mplew.writeInt(alliance.getId());
    mplew.writeMapleAsciiString(alliance.getName());
    for (int i = 1; i <= 5; i++) {
      mplew.writeMapleAsciiString(alliance.getRank(i));
    }
    mplew.write(alliance.getNoGuilds());
    for (int i = 0; i < alliance.getNoGuilds(); i++) {
      mplew.writeInt(alliance.getGuildId(i));
    }
    mplew.writeInt(alliance.getCapacity()); // ????
    mplew.writeMapleAsciiString(alliance.getNotice());
  }

  public static byte[] allianceMemberOnline(int alliance, int gid, int id, boolean online) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x0E);
    mplew.writeInt(alliance);
    mplew.writeInt(gid);
    mplew.writeInt(id);
    mplew.write(online ? 1 : 0);

    return mplew.getPacket();
  }

  public static byte[] updateAlliance(MapleGuildCharacter mgc, int allianceid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x18);
    mplew.writeInt(allianceid);
    mplew.writeInt(mgc.getGuildId());
    mplew.writeInt(mgc.getId());
    mplew.writeInt(mgc.getLevel());
    mplew.writeInt(mgc.getJobId());

    return mplew.getPacket();
  }

  public static byte[] updateAllianceRank(int allianceid, MapleGuildCharacter mgc) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x1B);
    mplew.writeInt(allianceid);
    mplew.writeInt(mgc.getId());
    mplew.writeInt(mgc.getAllianceRank());

    return mplew.getPacket();
  }

  public static byte[] disbandAlliance(int alliance) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
    mplew.write(0x1D);
    mplew.writeInt(alliance);

    return mplew.getPacket();
  }

  public static byte[] BBSThreadList(final List<MapleBBSThread> bbs, int start) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
    mplew.write(6);

    if (bbs == null) {
      mplew.write(0);
      mplew.writeLong(0);
      return mplew.getPacket();
    }
    int threadCount = bbs.size();
    MapleBBSThread notice = null;
    for (MapleBBSThread b : bbs) {
      if (b.isNotice()) { // notice
        notice = b;
        break;
      }
    }
    final int ret = (notice == null ? 0 : 1);
    mplew.write(ret);
    if (notice != null) { // has a notice
      addThread(mplew, notice);
      threadCount--; // one thread didn't count (because it's a notice)
    }
    if (threadCount < start) { // seek to the thread before where we start
      // uh, we're trying to start at a place past possible
      start = 0;
    }
    // each page has 10 threads, start = page # in packet but not here
    mplew.writeInt(threadCount);
    final int pages = Math.min(10, threadCount - start);
    mplew.writeInt(pages);

    for (int i = 0; i < pages; i++) {
      addThread(mplew, bbs.get(start + i + ret)); // because 0 = notice
    }
    return mplew.getPacket();
  }

  private static void addThread(MaplePacketLittleEndianWriter mplew, MapleBBSThread rs) {
    mplew.writeInt(rs.localthreadID);
    mplew.writeInt(rs.ownerID);
    mplew.writeMapleAsciiString(rs.name);
    mplew.writeLong(PacketHelper.getKoreanTimestamp(rs.timestamp));
    mplew.writeInt(rs.icon);
    mplew.writeInt(rs.getReplyCount());
  }

  public static byte[] showThread(MapleBBSThread thread) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
    mplew.write(7);

    mplew.writeInt(thread.localthreadID);
    mplew.writeInt(thread.ownerID);
    mplew.writeLong(PacketHelper.getKoreanTimestamp(thread.timestamp));
    mplew.writeMapleAsciiString(thread.name);
    mplew.writeMapleAsciiString(thread.text);
    mplew.writeInt(thread.icon);
    mplew.writeInt(thread.getReplyCount());
    for (MapleBBSReply reply : thread.replies.values()) {
      mplew.writeInt(reply.replyid);
      mplew.writeInt(reply.ownerID);
      mplew.writeLong(PacketHelper.getKoreanTimestamp(reply.timestamp));
      mplew.writeMapleAsciiString(reply.content);
    }
    return mplew.getPacket();
  }

  public static byte[] showGuildRanks(int npcid, List<GuildRankingInfo> all) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x49);
    mplew.writeInt(npcid);
    mplew.writeInt(all.size());

    for (GuildRankingInfo info : all) {
      mplew.writeMapleAsciiString(info.getName());
      mplew.writeInt(info.getGP());
      mplew.writeInt(info.getLogo());
      mplew.writeInt(info.getLogoColor());
      mplew.writeInt(info.getLogoBg());
      mplew.writeInt(info.getLogoBgColor());
    }

    return mplew.getPacket();
  }

  public static byte[] updateGP(int gid, int GP) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
    mplew.write(0x48);
    mplew.writeInt(gid);
    mplew.writeInt(GP);

    return mplew.getPacket();
  }

  public static byte[] skillEffect(MapleCharacter from, int skillId, byte level, byte direction, byte speed, byte unk) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
    mplew.writeInt(from.getId());
    mplew.writeInt(skillId);
    mplew.write(level);
    mplew.write(direction);
    mplew.write(speed);
    mplew.writeZeroBytes(5); // Direction ??

    return mplew.getPacket();
  }

  public static byte[] skillCancel(MapleCharacter from, int skillId) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
    mplew.writeInt(from.getId());
    mplew.writeInt(skillId);

    return mplew.getPacket();
  }

  public static byte[] showMagnet(int mobid, byte success) { // Monster Magnet
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
    mplew.writeInt(mobid);
    mplew.write(success);

    return mplew.getPacket();
  }

  public static byte[] sendHint(String hint, int width, int height) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    if (width < 1) {
      width = hint.length() * 10;
      if (width < 40) {
        width = 40;
      }
    }
    if (height < 5) {
      height = 5;
    }
    mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
    mplew.writeMapleAsciiString(hint);
    mplew.writeShort(width);
    mplew.writeShort(height);
    mplew.write(1);

    return mplew.getPacket();
  }

  public static byte[] messengerInvite(String from, int messengerid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
    mplew.write(0x03);
    mplew.writeMapleAsciiString(from);
    mplew.write(0x00);
    mplew.writeInt(messengerid);
    mplew.write(0x00);

    return mplew.getPacket();
  }

  public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
    mplew.write(0x00);
    mplew.write(position);
    PacketHelper.addCharLook(mplew, chr, true);
    mplew.writeMapleAsciiString(from);
    mplew.writeShort(channel);

    return mplew.getPacket();
  }

  public static byte[] removeMessengerPlayer(int position) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
    mplew.write(0x02);
    mplew.write(position);

    return mplew.getPacket();
  }

  public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
    mplew.write(0x07);
    mplew.write(position);
    PacketHelper.addCharLook(mplew, chr, true);
    mplew.writeMapleAsciiString(from);
    mplew.writeShort(channel);

    return mplew.getPacket();
  }

  public static byte[] joinMessenger(int position) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
    mplew.write(0x01);
    mplew.write(position);

    return mplew.getPacket();
  }

  public static byte[] messengerChat(String text) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
    mplew.write(0x06);
    mplew.writeMapleAsciiString(text);

    return mplew.getPacket();
  }

  public static byte[] messengerNote(String text, int mode, int mode2) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
    mplew.write(mode);
    mplew.writeMapleAsciiString(text);
    mplew.write(mode2);

    return mplew.getPacket();
  }

  public static byte[] getFindReplyWithCS(String target, final boolean buddy) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
    mplew.write(buddy ? 72 : 9);
    mplew.writeMapleAsciiString(target);
    mplew.write(2);
    mplew.writeInt(-1);

    return mplew.getPacket();
  }

  public static byte[] getFindReplyWithMTS(String target, final boolean buddy) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
    mplew.write(buddy ? 72 : 9);
    mplew.writeMapleAsciiString(target);
    mplew.write(0);
    mplew.writeInt(-1);

    return mplew.getPacket();
  }

  public static byte[] showEquipEffect() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());

    return mplew.getPacket();
  }

  public static byte[] showEquipEffect(int team) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
    mplew.writeShort(team);
    return mplew.getPacket();
  }

  public static byte[] summonSkill(int cid, int summonSkillId, int newStance) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
    mplew.writeInt(cid);
    mplew.writeInt(summonSkillId);
    mplew.write(newStance);

    return mplew.getPacket();
  }

  public static byte[] skillCooldown(int sid, int time) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
    mplew.writeInt(sid);
    mplew.writeShort(time);

    return mplew.getPacket();
  }

  public static byte[] useSkillBook(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
    mplew.write(0); // ?
    mplew.writeInt(chr.getId());
    mplew.write(1);
    mplew.writeInt(skillid);
    mplew.writeInt(maxlevel);
    mplew.write(canuse ? 1 : 0);
    mplew.write(success ? 1 : 0);

    return mplew.getPacket();
  }

  public static byte[] getMacros(SkillMacro[] macros) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
    int count = 0;
    for (int i = 0; i < 5; i++) {
      if (macros[i] != null) {
        count++;
      }
    }
    mplew.write(count); // number of macros
    for (int i = 0; i < 5; i++) {
      SkillMacro macro = macros[i];
      if (macro != null) {
        mplew.writeMapleAsciiString(macro.getName());
        mplew.write(macro.getShout());
        mplew.writeInt(macro.getSkill1());
        mplew.writeInt(macro.getSkill2());
        mplew.writeInt(macro.getSkill3());
      }
    }
    return mplew.getPacket();
  }

  public static byte[] updateAriantPQRanking(String name, int score, boolean empty) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ARIANT_PQ_START.getValue());
    mplew.write(empty ? 0 : 1);
    if (!empty) {
      mplew.writeMapleAsciiString(name);
      mplew.writeInt(score);
    }
    return mplew.getPacket();
  }

  public static byte[] catchMonster(int mobid, int itemid, byte success) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CATCH_MONSTER.getValue());
    mplew.writeInt(mobid);
    mplew.writeInt(itemid);
    mplew.write(success);

    return mplew.getPacket();
  }

  public static byte[] showAriantScoreBoard() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());

    return mplew.getPacket();
  }

  public static byte[] boatPacket(int effect) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    // 1034: balrog boat comes, 1548: boat comes, 3: boat leaves
    mplew.writeShort(SendPacketOpcode.BOAT_EFFECT.getValue());
    mplew.writeShort(effect); // 0A 04 balrog
    // this packet had 3: boat leaves

    return mplew.getPacket();
  }

  public static byte[] Mulung_DojoUp2() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    mplew.write(7);

    return mplew.getPacket();
  }

  public static byte[] showQuestMsg(final String msg) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(10);
    mplew.writeMapleAsciiString(msg);
    return mplew.getPacket();
  }

  public static byte[] Mulung_Pts(int recv, int total) {
    return showQuestMsg("You have received " + recv + " training points, for the accumulated total of " + total
        + " training points.");
  }

  public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.OX_QUIZ.getValue());
    mplew.write(askQuestion ? 1 : 0);
    mplew.write(questionSet);
    mplew.writeShort(questionId);
    return mplew.getPacket();
  }

  public static byte[] leftKnockBack() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.LEFT_KNOCK_BACK.getValue());
    return mplew.getPacket();
  }

  public static byte[] rollSnowball(int type, MapleSnowballs ball1, MapleSnowballs ball2) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.ROLL_SNOWBALL.getValue());
    mplew.write(type); // 0 = normal, 1 = rolls from start to end, 2 = down
    // disappear, 3 = up disappear, 4 = move
    mplew.writeInt(ball1 == null ? 0 : (ball1.getSnowmanHP() / 75));
    mplew.writeInt(ball2 == null ? 0 : (ball2.getSnowmanHP() / 75));
    mplew.writeShort(ball1 == null ? 0 : ball1.getPosition());
    mplew.write(0);
    mplew.writeShort(ball2 == null ? 0 : ball2.getPosition());
    mplew.writeZeroBytes(11);
    return mplew.getPacket();
  }

  public static byte[] enterSnowBall() {
    return rollSnowball(0, null, null);
  }

  public static byte[] hitSnowBall(int team, int damage, int distance, int delay) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.HIT_SNOWBALL.getValue());
    mplew.write(team);// 0 is down, 1 is up
    mplew.writeShort(damage);
    mplew.write(distance);
    mplew.write(delay);
    return mplew.getPacket();
  }

  public static byte[] snowballMessage(int team, int message) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.SNOWBALL_MESSAGE.getValue());
    mplew.write(team);// 0 is down, 1 is up
    mplew.writeInt(message);
    return mplew.getPacket();
  }

  public static byte[] finishedSort(int type) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.FINISH_SORT.getValue());
    mplew.write(1);
    mplew.write(type);
    return mplew.getPacket();
  }

  // 00 01 00 00 00 00
  public static byte[] coconutScore(int[] coconutscore) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.COCONUT_SCORE.getValue());
    mplew.writeShort(coconutscore[0]);
    mplew.writeShort(coconutscore[1]);
    return mplew.getPacket();
  }

  public static byte[] hitCoconut(boolean spawn, int id, int type) {
    // FF 00 00 00 00 00 00
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.HIT_COCONUT.getValue());
    if (spawn) {
      mplew.write(0);
      mplew.writeInt(0x80);
    } else {
      mplew.writeInt(id);
      mplew.write(type); // What action to do for the coconut.
    }
    return mplew.getPacket();
  }

  public static byte[] finishedGather(int type) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.FINISH_GATHER.getValue());
    mplew.write(1);
    mplew.write(type);
    return mplew.getPacket();
  }

  public static byte[] yellowChat(String msg) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.YELLOW_CHAT.getValue());
    mplew.write(-1); // could be something like mob displaying message.
    mplew.writeMapleAsciiString(msg);
    return mplew.getPacket();
  }

  public static byte[] getPeanutResult(int itemId, short quantity, int itemId2, short quantity2) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PIGMI_REWARD.getValue());
    mplew.writeInt(itemId);
    mplew.writeShort(quantity);
    mplew.writeInt(5060003);
    mplew.writeInt(itemId2);
    mplew.writeInt(quantity2);

    return mplew.getPacket();
  }

  public static byte[] sendLevelup(boolean family, int level, String name) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.LEVEL_UPDATE.getValue());
    mplew.write(family ? 1 : 2);
    mplew.writeInt(level);
    mplew.writeMapleAsciiString(name);

    return mplew.getPacket();
  }

  public static byte[] sendMarriage(boolean family, String name) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MARRIAGE_UPDATE.getValue());
    mplew.write(family ? 1 : 0);
    mplew.writeMapleAsciiString(name);

    return mplew.getPacket();
  }

  public static byte[] sendJobup(boolean family, int jobid, String name) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.JOB_UPDATE.getValue());
    mplew.write(family ? 1 : 0);
    mplew.writeInt(jobid); // or is this a short
    mplew.writeMapleAsciiString(name);

    return mplew.getPacket();
  }

  public static byte[] showZakumShrine(boolean spawned, int time) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.ZAKUM_SHRINE.getValue());
    mplew.write(spawned ? 1 : 0);
    mplew.writeInt(time);
    return mplew.getPacket();
  }

  public static byte[] showHorntailShrine(boolean spawned, int time) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
    mplew.write(spawned ? 1 : 0);
    mplew.writeInt(time);
    return mplew.getPacket();
  }

  public static byte[] showChaosZakumShrine(boolean spawned, int time) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
    mplew.write(spawned ? 1 : 0);
    mplew.writeInt(time);
    return mplew.getPacket();
  }

  public static byte[] showChaosHorntailShrine(boolean spawned, int time) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.CHAOS_HORNTAIL_SHRINE.getValue());
    mplew.write(spawned ? 1 : 0);
    mplew.writeInt(time);
    return mplew.getPacket();
  }

  public static byte[] stopClock() {
    return getPacketFromHexString("A9 00"); // does the header not work?
  }

  public static byte[] spawnDragon(MapleDragon d) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.DRAGON_SPAWN.getValue());
    mplew.writeInt(d.getOwner());
    mplew.writeInt(d.getPosition().x);
    mplew.writeInt(d.getPosition().y);
    mplew.write(d.getStance()); // stance?
    mplew.writeShort(0);
    mplew.writeShort(d.getJobId());
    return mplew.getPacket();
  }

  public static byte[] removeDragon(int chrid) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.DRAGON_REMOVE.getValue());
    mplew.writeInt(chrid);
    return mplew.getPacket();
  }

  public static byte[] moveDragon(MapleDragon d, MovePath path) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.DRAGON_MOVE.getValue()); // not sure
    mplew.writeInt(d.getOwner());
    path.encode(mplew);

    return mplew.getPacket();
  }

  public static final byte[] temporaryStats_Aran() {
    final List<Pair<MapleStat.Temp, Integer>> stats = new ArrayList<Pair<MapleStat.Temp, Integer>>();
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.STR, 999));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.DEX, 999));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.INT, 999));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.LUK, 999));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.WATK, 255));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.ACC, 999));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.AVOID, 999));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.SPEED, 140));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.JUMP, 120));
    return temporaryStats(stats);
  }

  public static final byte[] temporaryStats_Balrog(final MapleCharacter chr) {
    final List<Pair<MapleStat.Temp, Integer>> stats = new ArrayList<Pair<MapleStat.Temp, Integer>>();
    int offset = 1 + (chr.getLevel() - 90) / 20;
    // every 20 levels above 90, +1
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.STR, chr.getStat().getTotalStr() / offset));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.DEX, chr.getStat().getTotalDex() / offset));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.INT, chr.getStat().getTotalInt() / offset));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.LUK, chr.getStat().getTotalLuk() / offset));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.WATK, chr.getStat().getTotalWatk() / offset));
    stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.MATK, chr.getStat().getTotalMagic() / offset));
    return temporaryStats(stats);
  }

  public static final byte[] temporaryStats(final List<Pair<MapleStat.Temp, Integer>> stats) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.FORCED_STATS.getValue());
    // str 0x1, dex 0x2, int 0x4, luk 0x8
    // level 0x10 = 255
    // 0x100 = 999
    // 0x200 = 999
    // 0x400 = 120
    // 0x800 = 140
    int updateMask = 0;
    for (final Pair<MapleStat.Temp, Integer> statupdate : stats) {
      updateMask |= statupdate.getLeft().getValue();
    }
    List<Pair<MapleStat.Temp, Integer>> mystats = stats;
    if (mystats.size() > 1) {
      Collections.sort(mystats, new Comparator<Pair<MapleStat.Temp, Integer>>() {

        @Override
        public int compare(final Pair<MapleStat.Temp, Integer> o1, final Pair<MapleStat.Temp, Integer> o2) {
          int val1 = o1.getLeft().getValue();
          int val2 = o2.getLeft().getValue();
          return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
        }
      });
    }
    mplew.writeInt(updateMask);
    Integer value;

    for (final Pair<MapleStat.Temp, Integer> statupdate : mystats) {
      value = statupdate.getLeft().getValue();

      if (value >= 1) {
        if (value <= 0x200) { // level 0x10 - is this really short or
          // some other? (FF 00)
          mplew.writeShort(statupdate.getRight().shortValue());
        } else {
          mplew.write(statupdate.getRight().byteValue());
        }
      }
    }
    return mplew.getPacket();
  }

  public static final byte[] temporaryStats_Reset() {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.FORCED_STATS_RESET.getValue());
    return mplew.getPacket();
  }

  public static final byte[] showHpHealed(final int cid, final int amount) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
    mplew.writeInt(cid);
    mplew.write(10);
    mplew.writeInt(amount);

    return mplew.getPacket();
  }

  public static final byte[] showOwnHpHealed(final int amount) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    mplew.write(10);
    mplew.writeInt(amount);

    return mplew.getPacket();
  }

  public static final byte[] sendRepairWindow(int npc) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.REPAIR_WINDOW.getValue());
    mplew.writeInt(GameUI.REPAIR_WINDOW);
    mplew.writeInt(npc);
    return mplew.getPacket();
  }

  public static final byte[] sendPyramidUpdate(final int amount) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PYRAMID_UPDATE.getValue());
    mplew.writeInt(amount); // 1-132 ?
    return mplew.getPacket();
  }

  public static final byte[] sendPyramidResult(final byte rank, final int amount) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PYRAMID_RESULT.getValue());
    mplew.write(rank);
    mplew.writeInt(amount); // 1-132 ?
    return mplew.getPacket();
  }

  // show_status_info - 01 53 1E 01
  // 10/08/14/19/11
  // update_quest_info - 08 53 1E 00 00 00 00 00 00 00 00
  // show_status_info - 01 51 1E 01 01 00 30
  // update_quest_info - 08 51 1E 00 00 00 00 00 00 00 00
  public static final byte[] sendPyramidEnergy(final String type, final String amount) {
    return sendString(1, type, amount);
  }

  public static final byte[] sendString(final int type, final String object, final String amount) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    switch (type) {
      case 1:
        mplew.writeShort(SendPacketOpcode.ENERGY.getValue());
        break;
      case 2:
        mplew.writeShort(SendPacketOpcode.GHOST_POINT.getValue());
        break;
      case 3:
        mplew.writeShort(SendPacketOpcode.GHOST_STATUS.getValue());
        break;
    }
    mplew.writeMapleAsciiString(object); // massacre_hit, massacre_cool,
    // massacre_miss,
    // massacre_party,
    // massacre_laststage,
    // massacre_skill
    mplew.writeMapleAsciiString(amount);
    return mplew.getPacket();
  }

  public static final byte[] sendGhostPoint(final String type, final String amount) {
    return sendString(2, type, amount); // PRaid_Point (0-1500???)
  }

  public static final byte[] sendGhostStatus(final String type, final String amount) {
    return sendString(3, type, amount); // Red_Stage(1-5), Blue_Stage,
    // blueTeamDamage, redTeamDamage
  }

  public static byte[] MulungEnergy(int energy) {
    return sendPyramidEnergy("energy", String.valueOf(energy));
  }

  public static byte[] getEvanTutorial(String data) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());

    mplew.writeInt(8);
    mplew.write(0);
    mplew.write(1);
    mplew.write(1);
    mplew.write(1);
    mplew.writeMapleAsciiString(data);

    return mplew.getPacket();
  }

  public static byte[] showEventInstructions() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
    mplew.write(0);
    return mplew.getPacket();
  }

  public static byte[] getOwlOpen() { // best items! hardcoded
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
    mplew.write(7);
    mplew.write(GameConstants.owlItems.length);
    for (int i : GameConstants.owlItems) {
      mplew.writeInt(i);
    } // these are the most searched items. too lazy to actually make
    return mplew.getPacket();
  }

  public static byte[] getOwlSearched(final int itemSearch, final List<HiredMerchant> hms) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
    mplew.write(6);
    mplew.writeInt(0);
    mplew.writeInt(itemSearch);
    int size = 0;

    for (HiredMerchant hm : hms) {
      size += hm.searchItem(itemSearch).size();
    }
    mplew.writeInt(size);
    for (HiredMerchant hm : hms) {
      final List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
      for (MaplePlayerShopItem item : items) {
        mplew.writeMapleAsciiString(hm.getOwnerName());
        mplew.writeInt(hm.getMap().getId());
        mplew.writeMapleAsciiString(hm.getDescription());
        mplew.writeInt(item.item.getQuantity()); // I THINK.
        mplew.writeInt(item.bundles); // I THINK.
        mplew.writeInt(item.price);
        mplew.writeInt(hm.getOwnerId());
        mplew.write(hm.getFreeSlot() == -1 ? 1 : 0);
        if (item.item.getItemId() / 1000000 == 1) {
          mplew.write(1);
          PacketHelper.addItemInfo(mplew, item.item, true, true);
        } else {
          mplew.write(2);
        }
      }
    }
    return mplew.getPacket();
  }

  public static byte[] getRPSMode(byte mode, int mesos, int selection, int answer) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.RPS_GAME.getValue());
    mplew.write(mode);
    switch (mode) {
      case 6: { // not enough mesos
        if (mesos != -1) {
          mplew.writeInt(mesos);
        }
        break;
      }
      case 8: { // open (npc)
        mplew.writeInt(9000019);
        break;
      }
      case 11: { // selection vs answer
        mplew.write(selection);
        mplew.write(answer); // FF = lose, or if selection = answer then
        // lose ???
        break;
      }
    }
    return mplew.getPacket();
  }

  public static final byte[] getSlotUpdate(byte invType, byte newSlots) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_GROW.getValue());
    mplew.write(invType);
    mplew.write(newSlots);
    return mplew.getPacket();
  }

  public static byte[] followRequest(int chrid) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.FOLLOW_REQUEST.getValue());
    mplew.writeInt(chrid);
    return mplew.getPacket();
  }

  public static byte[] followEffect(int initiator, int replier, Point toMap) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.FOLLOW_EFFECT.getValue());
    mplew.writeInt(initiator);
    mplew.writeInt(replier);
    if (replier == 0) { // cancel
      mplew.write(toMap == null ? 0 : 1); // 1 -> x (int) y (int) to
      // change map
      if (toMap != null) {
        mplew.writeInt(toMap.x);
        mplew.writeInt(toMap.y);
      }
    }
    return mplew.getPacket();
  }

  public static byte[] getFollowMsg(int opcode) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.FOLLOW_MSG.getValue());
    mplew.writeLong(opcode); // 5 = canceled request.
    return mplew.getPacket();
  }
  // TODO FIX follow
  public static byte[] moveFollow(Point otherStart, Point myStart, Point otherEnd, MovePath path) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.FOLLOW_MOVE.getValue());
    mplew.writePos(otherStart);
    mplew.writePos(myStart);
    path.encode(mplew);
    mplew.write(17);
    for (int i = 0; i < 8; i++) {
      mplew.write(0);
    }
    mplew.write(0);
    mplew.writePos(otherEnd);
    mplew.writePos(otherStart);

    return mplew.getPacket();
  }

  public static final byte[] getFollowMessage(final String msg) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.FOLLOW_MESSAGE.getValue());
    mplew.writeShort(0x0B); // ?
    mplew.writeMapleAsciiString(msg); // white in gms, but msea just makes
    // it pink.. waste
    return mplew.getPacket();
  }

  public static final byte[] getNodeProperties(final MapleMonster objectid, final MapleMap map) {
    // idk.
    if (objectid.getNodePacket() != null) {
      return objectid.getNodePacket();
    }
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MONSTER_PROPERTIES.getValue());
    mplew.writeInt(objectid.getObjectId()); // ?
    mplew.writeInt(map.getNodes().size());
    mplew.writeInt(objectid.getPosition().x);
    mplew.writeInt(objectid.getPosition().y);
    for (MapleNodeInfo mni : map.getNodes()) {
      mplew.writeInt(mni.x);
      mplew.writeInt(mni.y);
      mplew.writeInt(mni.attr);
      if (mni.attr == 2) { // msg
        mplew.writeInt(500); // ? talkMonster
      }
    }
    mplew.writeZeroBytes(6);
    objectid.setNodePacket(mplew.getPacket());
    return objectid.getNodePacket();
  }

  public static final byte[] getMovingPlatforms(final MapleMap map) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MOVE_PLATFORM.getValue());
    mplew.writeInt(map.getPlatforms().size());
    for (MaplePlatform mp : map.getPlatforms()) {
      mplew.writeMapleAsciiString(mp.name);
      mplew.writeInt(mp.start);
      mplew.writeInt(mp.SN.size());
      for (int x = 0; x < mp.SN.size(); x++) {
        mplew.writeInt(mp.SN.get(x));
      }
      mplew.writeInt(mp.speed);
      mplew.writeInt(mp.x1);
      mplew.writeInt(mp.x2);
      mplew.writeInt(mp.y1);
      mplew.writeInt(mp.y2);
      mplew.writeInt(mp.x1);// ?
      mplew.writeInt(mp.y1);
      mplew.writeShort(mp.r);
    }
    return mplew.getPacket();
  }

  public static final byte[] getUpdateEnvironment(final MapleMap map) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_ENV.getValue());
    mplew.writeInt(map.getEnvironment().size());
    for (Entry<String, Integer> mp : map.getEnvironment().entrySet()) {
      mplew.writeMapleAsciiString(mp.getKey());
      mplew.writeInt(mp.getValue());
    }
    return mplew.getPacket();
  }

  public static byte[] sendEngagementRequest(String name, int cid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.ENGAGE_REQUEST.getValue());
    mplew.write(0); // mode, 0 = engage, 1 = cancel, 2 = answer.. etc
    mplew.writeMapleAsciiString(name); // name
    mplew.writeInt(cid); // playerid
    return mplew.getPacket();
  }

  /**
   * @param type  - (0:Light&Long 1:Heavy&Short)
   * @param delay - seconds
   * @return
   */
  public static byte[] trembleEffect(int type, int delay) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
    mplew.write(1);
    mplew.write(type);
    mplew.writeInt(delay);
    return mplew.getPacket();
  }

  public static byte[] sendEngagement(final byte msg, final int item, final MapleCharacter male,
                                      final MapleCharacter female) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    // 0B = Engagement has been concluded.
    // 0D = The engagement is cancelled.
    // 0E = The divorce is concluded.
    // 10 = The marriage reservation has been successsfully made.
    // 12 = Wrong character name
    // 13 = The party in not in the same map.
    // 14 = Your inventory is full. Please empty your E.T.C window.
    // 15 = The person's inventory is full.
    // 16 = The person cannot be of the same gender.
    // 17 = You are already engaged.
    // 18 = The person is already engaged.
    // 19 = You are already married.
    // 1A = The person is already married.
    // 1B = You are not allowed to propose.
    // 1C = The person is not allowed to be proposed to.
    // 1D = Unfortunately, the one who proposed to you has cancelled his
    // proprosal.
    // 1E = The person had declined the proposal with thanks.
    // 1F = The reservation has been cancelled. Try again later.
    // 20 = You cannot cancel the wedding after reservation.
    // 22 = The invitation card is ineffective.
    mplew.writeShort(SendPacketOpcode.ENGAGE_RESULT.getValue());
    mplew.write(msg); // 1103 custom quest
    switch (msg) {
      case 11: {
        mplew.writeInt(0); // ringid or uniqueid
        mplew.writeInt(male.getId());
        mplew.writeInt(female.getId());
        mplew.writeShort(1); // always
        mplew.writeInt(item);
        mplew.writeInt(item); // wtf?repeat?
        mplew.writeAsciiString(male.getName(), 13);
        mplew.writeAsciiString(female.getName(), 13);
        break;
      }
    }
    return mplew.getPacket();
  }

  public static byte[] iDontKnow(final MapleCharacter chr, int type) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(396);
    mplew.write(type);
    mplew.write(1);
    PacketHelper.addCharLook(mplew, chr, false);
    mplew.writeMapleAsciiString("1");
    mplew.writeMapleAsciiString("2");
    mplew.writeMapleAsciiString("3");
    mplew.writeMapleAsciiString("4");
    mplew.writeMapleAsciiString("5");
    mplew.writeMapleAsciiString("6");
    mplew.writeMapleAsciiString("7");
    mplew.writeInt(0); // other char id?
    if (type == 2) {
      PacketHelper.addCharLook(mplew, chr, false);
    }
    return mplew.getPacket();
  }

  // [29 00] [01] [1A 6F]-28442 questid [01] [08 00 37 30 30 30 30 33 39 33]
  public static byte[] luckyLogoutGift(byte progress_mode, String progress_info) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(1);
    mplew.writeShort(28442); // questid
    mplew.write(progress_mode);
    mplew.writeMapleAsciiString(progress_info); // serial number
    return mplew.getPacket();
  }

  public static byte[] enableShopDiscount(byte percent) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOP_DISCOUNT.getValue());
    mplew.write(percent);

    return mplew.getPacket();
  }

  public static byte[] showVisitorEffect() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.VISITOR.getValue());
    mplew.writeMapleAsciiString("Visitor");
    mplew.write(1); // 0, 1, 2, 3

    return mplew.getPacket();
  }

  public static byte[] itemExpired(int itemid) {
    return expiredMessage((byte) 2, itemid, null, null);
  }

  public static byte[] sealExpired(List<Integer> expire) {
    return expiredMessage((byte) 12, 0, expire, null);
  }

  public static byte[] itemReplaced(List<String> replaceMsg) {
    return expiredMessage((byte) 13, 0, null, replaceMsg);
  }

  public static byte[] skillExpired(List<Integer> expire) {
    return expiredMessage((byte) 14, 0, expire, null);
  }

  public static byte[] expiredMessage(byte type, int itemid, List<Integer> expire, List<String> replaceMsg) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(type);
    switch (type) {
      case 2: // OnCashItemExpireMessage
        // [<item name>] has passed its expiration date and will be removed
        // from your inventory
        mplew.writeInt(itemid);
        break;
      case 12: // OnItemProtectExpireMessage
        // <Item name>'s seal has expired.
        mplew.write(expire.size()); // Size
        for (Integer it : expire) {
          mplew.writeInt(it.intValue());
        }
        break;
      case 13: // OnItemExpireReplaceMessage
        mplew.write(replaceMsg.size()); // Size
        for (String x : replaceMsg) {
          mplew.writeMapleAsciiString(x);
        }
        break;
      case 14: // OnSkillExpireMessage
        // <Skill Name> has disappeared as the time limit has passed.
        mplew.write(expire.size());
        for (Integer i : expire) {
          mplew.writeInt(i); // Skill Id
        }
        break;
    }

    return mplew.getPacket();
  }

  public static byte[] showMonsterBombEffect(int x, int y, int level) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(23);

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    mplew.write(5);
    mplew.writeInt(4341003);
    mplew.writeInt(x);
    mplew.writeInt(y);
    mplew.writeInt(1);
    mplew.writeInt(level);

    return mplew.getPacket();
  }

  public static byte[] setNPCScriptable(List<Integer> npcId) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());
    mplew.write(npcId.size());
    for (Integer i : npcId) {
      mplew.writeInt(i);
      mplew.writeMapleAsciiString(".");
      mplew.writeInt(0); // start time
      mplew.writeInt(Integer.MAX_VALUE); // end time
    }

    return mplew.getPacket();
  }

  public static byte[] setNPCScriptable(int npcId, String message) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());
    mplew.write(1);
    mplew.writeInt(npcId);
    mplew.writeMapleAsciiString(message);
    mplew.writeInt(0); // start time
    mplew.writeInt(Integer.MAX_VALUE); // end time

    return mplew.getPacket();
  }

  public static byte[] loadGuildName(MapleCharacter chr) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.LOAD_GUILD_NAME.getValue());
    mplew.writeInt(chr.getId());

    if (chr.getGuildId() <= 0) {
      mplew.writeShort(0);
    } else {
      final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
      mplew.writeMapleAsciiString(gs != null ? gs.getName() : "");
    }

    return mplew.getPacket();
  }

  public static byte[] loadGuildIcon(MapleCharacter chr) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.LOAD_GUILD_ICON.getValue());
    mplew.writeInt(chr.getId());

    if (chr.getGuildId() <= 0) {
      mplew.writeZeroBytes(6);
    } else {
      final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
      if (gs != null) {
        mplew.writeShort(gs.getLogoBG());
        mplew.write(gs.getLogoBGColor());
        mplew.writeShort(gs.getLogo());
        mplew.write(gs.getLogoColor());
      } else {
        mplew.writeZeroBytes(6);
      }
    }

    return mplew.getPacket();
  }

  /**
   * 0 : Level up Effect 7 : Enter portal 8 : Job Change 9 : Complete quest 13
   * : Monster Book 15 : Equipment level up 17 : EXP Card gain 26 : Soul stone
   * effect i think..(You have revived on the current map through the effect
   * of the Spirit Stone.)
   */
  public static byte[] showSpecialEffect_(int effect) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    mplew.write(effect);
    switch (effect) {
      case 2:
        break;
      case 10:
        mplew.write(0); // 0 = Miss, 1 and above is heal (blue colour hp)
        break;
      case 11: // some kind of shine effect
        mplew.writeInt(0); // ??
        break;
      case 12: // show path
        mplew.writeMapleAsciiString("");
        break;
      case 16: // maker skill
        mplew.writeInt(0); // 0 = pass, 1 = fail
        break;
      case 21: // Wheel of Destiny
        // You have used 1 Wheel of Destiny in order to revive at the
        // current map. (<left> left)
        mplew.write(0); // left
        break;
    }

    return mplew.getPacket();
  }

  public static final byte[] getGameMessage(final int code, final String msg) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.FOLLOW_MESSAGE.getValue());
    mplew.writeShort(code);
    mplew.writeMapleAsciiString(msg);

    return mplew.getPacket();
  }

  public static final byte[] sendBrowser(final String site) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MAPLE_ADMIN.getValue());
    mplew.writeAsciiString(site);

    return mplew.getPacket();
  }

  public static byte[] damageMonster(int oid, int damage) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
    mplew.writeInt(oid);
    mplew.write(0);
    mplew.writeInt(damage);
    mplew.write(0);
    mplew.write(0);
    mplew.write(0);
    return mplew.getPacket();
  }

  public static byte[] damageMonster(int skill, int x, int y) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(19);
    mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
    mplew.write(skill);
    mplew.writeInt(x);
    mplew.writeInt(y);
    mplew.writeLong(0L);
    return mplew.getPacket();
  }

  public static byte[] updateExtendedSP(EvanSkillPoints esp) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
    mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
    mplew.write(0);
    mplew.write(0);
    mplew.write(128);
    mplew.writeShort(0);
    mplew.write(esp.getSkillPoints().keySet().size());
    for (Iterator<?> i = esp.getSkillPoints().keySet().iterator(); i.hasNext(); ) {
      int val = ((Integer) i.next()).intValue();
      mplew.write(val == 2200 ? 1 : val - 2208);
      mplew.write(esp.getSkillPoints(val));
    }
    mplew.write(0);
    return mplew.getPacket();
  }

  public static byte[] updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemReaction,
                                         boolean extendSPJob, ExtendedSPTable table) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
    mplew.write(itemReaction ? 1 : 0);
    int updateMask = 0;
    for (Pair<MapleStat, Integer> statupdate : stats) {
      updateMask |= statupdate.getLeft().getValue();
    }
    List<Pair<MapleStat, Integer>> mystats = stats;
    if (mystats.size() > 1) {
      Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {
        @Override
        public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
          int val1 = o1.getLeft().getValue();
          int val2 = o2.getLeft().getValue();
          return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
        }
      });
    }
    mplew.writeInt(updateMask);
    for (Pair<MapleStat, Integer> statupdate : mystats) {
      if (statupdate.getLeft().getValue() >= 1) {
        if (statupdate.getLeft().getValue() == 0x1) {
          mplew.writeShort(statupdate.getRight().shortValue());
        } else if (statupdate.getLeft() == MapleStat.AVAILABLESP) {
          if (extendSPJob) {
            table.addSPData(mplew);
          } else {
            mplew.writeShort(statupdate.getRight().shortValue());
          }
        } else if (statupdate.getLeft().getValue() <= 0x4) {
          mplew.writeInt(statupdate.getRight());
        } else if (statupdate.getLeft().getValue() < 0x20) {
          mplew.write(statupdate.getRight().shortValue());
        } else if (statupdate.getLeft().getValue() < 0xFFFF) {
          mplew.writeShort(statupdate.getRight().shortValue());
        } else {
          mplew.writeInt(statupdate.getRight().intValue());
        }
      }
    }
    mplew.write(0);// v88 new
    return mplew.getPacket();
  }

  public static byte[] viewAllCharCustomMessage(String message) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
    mplew.write(6); // 2: Already connected
    mplew.write(1);
    mplew.writeMapleAsciiString(message);
    return mplew.getPacket();
  }

  /*
   * header: 8 se
   *
   */
  public static byte[] viewAllChar(int serverCount, int numCharacter) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
    mplew.write(1); // 2: Already connected
    mplew.writeInt(serverCount);// m_nCountRelatedSvrs
    mplew.writeInt(numCharacter);// m_nCountCharacters
    return mplew.getPacket();
  }

  public static byte[] viewAllCharShowChars(int nWorldID, List<MapleCharacter> chars) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
    mplew.write(0);// nType
    mplew.write(nWorldID);
    mplew.write(chars.size());
    for (final MapleCharacter chr : chars) {
      PacketHelper.addCharStats(mplew, chr);
      PacketHelper.addCharLook(mplew, chr, true);
      boolean ranking = !chr.isGM();
      mplew.write(ranking ? 1 : 0);
      if (ranking) {
        mplew.writeInt(chr.getRank());
        mplew.writeInt(chr.getRankMove());
        mplew.writeInt(chr.getJobRank());
        mplew.writeInt(chr.getJobRankMove());
      }
    }
    mplew.write(2); // second pw request

    return mplew.getPacket();
  }

  public static byte[] openVoteWebpage() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(41);
    return mplew.getPacket();
  }

  public static byte[] openGMWindowBoard(String url) {
    MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
    packet.writeShort(131);
    packet.writeInt(Integer.MAX_VALUE);
    packet.writeMapleAsciiString(url);
    return packet.getPacket();
  }

  enum GMResulMessages {
    SUCCESSFULLY_BLOCKED_ACCESS(4),
    UNBLOCKING_SUCCESSFULL(5),
    INVALID_CHAR_NAME(6),
    REMOVED_FROM_RANKS(6),
    INVISIVLE_GM_MESSAGE_RED(0x3A),
    INVISIVLE_GM_MESSAGE_LIGHT(0x39);

    int type;

    GMResulMessages(int type) {
      this.type = type;
    }

    public int getType() {
      return type;
    }

  }

  public byte[] blockedAccessGMMessage(boolean blockAccess) {
    MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
    packet.writeShort(159);
    int type = GMResulMessages.SUCCESSFULLY_BLOCKED_ACCESS.getType();
    if (!blockAccess) {
      type = GMResulMessages.UNBLOCKING_SUCCESSFULL.getType();
      ;
    }
    packet.write(type);
    packet.write(4);
    return packet.getPacket();
  }

  public byte[] channelAndServerMessage(String channel, String world, String msg) {
    MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
    packet.writeShort(159);
    packet.write(0xB);
    packet.writeMapleAsciiString(channel);
    packet.writeMapleAsciiString(world);
    packet.writeMapleAsciiString(msg);

    return packet.getPacket();
  }

  public byte[] writeGMMessageWithoutName(String msg, boolean isRed) {
    MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
    packet.writeShort(159);
    if (isRed) {
      packet.write(GMResulMessages.INVISIVLE_GM_MESSAGE_RED.getType());
    } else {
      packet.write(GMResulMessages.INVISIVLE_GM_MESSAGE_LIGHT.getType());
    }

    packet.writeMapleAsciiString(msg);
    return packet.getPacket();
  }

}
