/*
 * This file is part of the OdinMS MapleStory Private Server
 * Copyright (C) 2011 Patrick Huy and Matthias Butz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tools.packet;

import client.MapleCharacter;
import handling.SendPacketOpcode;
import handling.channel.handler.utils.PartyHandlerUtils.PartyOperation;
import handling.world.World;
import handling.world.expedition.MapleExpedition;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Store packets for Buddy, Party, Expedition, Guild and Alliance
 */
public class MapleUserPackets {

  public static byte[] partyCreated(final int partyid) {
    final Object[] params = new Object[1];
    params[0] = partyid;
    return partyPacket(0x08, params);
  }

  public static byte[] partyInvite(final MapleCharacter from) {
    final Object[] params = new Object[4];
    params[0] = from.getParty().getId();
    params[1] = from.getName();
    params[2] = from.getLevel();
    params[3] = from.getJob();
    return partyPacket(0x04, params);
  }

  public static byte[] partyPortal(final int townId, final int targetId, final int skillId, final Point position) {
    final Object[] params = new Object[4];
    params[0] = townId;
    params[1] = targetId;
    params[2] = skillId;
    params[3] = position;
    return partyPacket(0x23, params);
  }

  public static byte[] updateParty(final int forChannel, final MapleParty party, final PartyOperation op, final MaplePartyCharacter target) {
    Object[] params = null;
    switch (op) {
      case DISBAND:
      case DISBAND_IN_EXPEDITION:
      case EXPEL:
      case LEAVE:
      case MOVE_MEMBER:
        params = new Object[(op != PartyOperation.DISBAND && op != PartyOperation.DISBAND_IN_EXPEDITION) ? 6 : 3];
        params[0] = (op == PartyOperation.DISBAND || op == PartyOperation.DISBAND_IN_EXPEDITION) ? 0 : ((op == PartyOperation.EXPEL) ? 1 : 2); // Operation
        params[1] = party.getId();
        params[2] = target.getId();
        if (op != PartyOperation.DISBAND && op != PartyOperation.DISBAND_IN_EXPEDITION) {
          params[3] = target.getName();
          params[4] = forChannel;
          params[5] = party;
        }
        return partyPacket(0x0C, params);
      case JOIN:
        params = new Object[4];
        params[0] = party.getId();
        params[1] = target.getName();
        params[2] = forChannel;
        params[3] = party;
        return partyPacket(0x0F, params);
      case SILENT_UPDATE:
      case LOG_ONOFF:
        params = new Object[4];
        params[0] = op == PartyOperation.LOG_ONOFF ? 0 : 1;
        params[1] = party.getId();
        params[2] = forChannel;
        params[3] = party;
        return partyPacket(0x07, params);
      case CHANGE_LEADER:
      case CHANGE_LEADER_DC:
        params = new Object[2];
        params[0] = target.getId();
        params[1] = op == PartyOperation.CHANGE_LEADER_DC ? 1 : 0;
        return partyPacket(0x1F, params);
    }
    return partyPacket(0x01, params); // Your request for a party didn't work due to an unexpected error.
  }

  /**
   * 0x0A : A beginner can't create a party.
   * 0x0D : You have yet to join a party.
   * 0x10 : You have joined the party.
   * 0x11 : Already have joined a party.
   * 0x12 : The party you're trying to join is already in full capacity.
   * 0x16 : You have invited <name> to your party. (Popup)
   * 0x17 : <Name>
   * 0x1D : Cannot kick another user in this map | Expel function is not available in this map.
   * 0x20 : This can only be given to a party member within the vicinity. | The Party Leader can only be handed over to the party member in the same map.
   * 0x21 : Unable to hand over the leadership post; No party member is currently within the vicinity of the party leader | There is no party member in the same field with party leader for the hand over.
   * 0x22 : You may only change with the party member that's on the same channel. | You can only hand over to the party member within the same map.
   * 0x24 : As a GM, you're forbidden from creating a party.
   * 0x25 : Unable to find the character.
   */
  public static byte[] partyStatusMessage(final byte message) {
    final Object[] params = null;
    return partyPacket(message, params);
  }

  public static byte[] partyStatusMessage(final byte message, final String charname) {
    final Object[] params = new Object[1];
    params[0] = charname;
    return partyPacket(message, params);
  }

  public static byte[] updatePartyMemberHP(final int cid, int curhp, int maxhp) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);
    mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
    mplew.writeInt(cid);
    mplew.writeInt(curhp);
    mplew.writeInt(maxhp);
    return mplew.getPacket();
  }

  private static byte[] partyPacket(final int type, final Object... data) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
    mplew.write(type);
    switch (type) {
      case 0x04: // Invite
        mplew.writeInt((Integer) data[0]); //partyid
        mplew.writeMapleAsciiString((String) data[1]); //name
        mplew.writeInt((Short) data[2]); //level
        mplew.writeInt((Short) data[3]); //jobid
        mplew.write(0);
        break;
      case 0x07: // Silent Update / Log off
        mplew.writeInt((Integer) data[1]);
        addPartyStatus((Integer) data[2], (MapleParty) data[3], mplew, ((Integer) data[0]) == 0);
        break;
      case 0x08: // Create
        mplew.writeInt((Integer) data[0]); //partyid
        mplew.writeInt(999999999);
        mplew.writeInt(999999999);
        mplew.writeLong(0);
        break;
      case 0x0C: // Disband, Expel and Leave
        int operation = (Integer) data[0]; // 0 = disband, 1 = expel, 2 = leave
        mplew.writeInt((Integer) data[1]); //party id
        mplew.writeInt((Integer) data[2]); //target id
        mplew.write(operation != 0 ? 1 : 0); // !disband
        if (operation == 0) { // Disband
          mplew.writeInt((Integer) data[2]); // target id
        } else {
          mplew.write(operation == 1 ? 1 : 0); // Expel
          mplew.writeMapleAsciiString((String) data[3]); // target name
          addPartyStatus((Integer) data[4], (MapleParty) data[5], mplew, operation == 2);
        }
        break;
      case 0x0F: // Join
        mplew.writeInt((Integer) data[0]); //party id
        mplew.writeMapleAsciiString((String) data[1]); // target name
        addPartyStatus((Integer) data[2], (MapleParty) data[3], mplew, false);
        break;
      case 0x16: // Invite Message
        mplew.writeMapleAsciiString((String) data[0]);
        break;
      case 0x1F: // Change leader
        mplew.writeInt((Integer) data[0]); // target id
        mplew.write((Integer) data[1]);
        break;
      case 0x23: // Portal
        mplew.write(0);
        mplew.writeInt((Integer) data[0]); //townId
        mplew.writeInt((Integer) data[1]); //targetId
        mplew.writeInt((Integer) data[2]); //skillId
        mplew.writePos((Point) data[3]); //position
        break;
    }
    return mplew.getPacket();
  }

  private static void addPartyStatus(final int forchannel, final MapleParty party, final MaplePacketLittleEndianWriter mplew, final boolean leaving) {
    addPartyStatus(forchannel, party, mplew, leaving, false);
  }

  private static void addPartyStatus(final int forchannel, final MapleParty party, final MaplePacketLittleEndianWriter mplew, final boolean leaving, final boolean exped) {
    final List<MaplePartyCharacter> partymembers;
    if (party == null) {
      partymembers = new ArrayList<>();
    } else {
      partymembers = new ArrayList<>(party.getMembers());
    }
    while (partymembers.size() < 6) {
      partymembers.add(new MaplePartyCharacter());
    }
    for (final MaplePartyCharacter partychar : partymembers) {
      mplew.writeInt(partychar.getId());
    }
    for (final MaplePartyCharacter partychar : partymembers) {
      mplew.writeAsciiString(partychar.getName(), 13);
    }
    for (final MaplePartyCharacter partychar : partymembers) {
      mplew.writeInt(partychar.getJobId());
    }
    for (final MaplePartyCharacter partychar : partymembers) {
      mplew.writeInt(partychar.getLevel());
    }
    for (final MaplePartyCharacter partychar : partymembers) {
      mplew.writeInt(partychar.isOnline() ? (partychar.getChannel() - 1) : -2);
    }
    mplew.writeInt(party == null ? 0 : party.getLeader().getId());
    if (exped) {
      return;
    }
    for (final MaplePartyCharacter partychar : partymembers) {
      mplew.writeInt(partychar.getChannel() == forchannel ? partychar.getMapid() : 0);
    }
    for (final MaplePartyCharacter partychar : partymembers) {
      if (partychar.getChannel() == forchannel && !leaving) {
        mplew.writeInt(partychar.getDoorTown());
        mplew.writeInt(partychar.getDoorTarget());
        mplew.writeInt(partychar.getDoorSkill());
        mplew.writeInt(partychar.getDoorPosition().x);
        mplew.writeInt(partychar.getDoorPosition().y);
      } else {
        mplew.writeInt(leaving ? 999999999 : 0);
        mplew.writeLong(leaving ? 999999999 : 0);
        mplew.writeLong(leaving ? -1 : 0);
      }
    }
  }

  public static byte[] showExpedition(final MapleExpedition me, final boolean created, final boolean silent) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
    mplew.write(silent ? 53 : (created ? 55 : 57)); // 53, 55(A new expedition has been created), 57("You have joined the expedition)
    mplew.writeInt(me.getType().exped);
    mplew.writeInt(0);
    for (int i = 0; i < 5; i++) {
      if (i < me.getParties().size()) {
        final MapleParty party = World.Party.getParty(me.getParties().get(i));
        if (party != null) {
          addPartyStatus(-1, party, mplew, false, true);
        } else {
          mplew.writeZeroBytes(178);
        }
      } else {
        mplew.writeZeroBytes(178);
      }
    }
    mplew.writeShort(0);
    return mplew.getPacket();
  }

  public static byte[] removeExpedition(final int action) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
    mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
    mplew.write(action); // 54(remove only) , 61 (you have left the expedition), 63(You have been kicked out of the expedition), 64(The Expedition has been disbanded)
    return mplew.getPacket();
  }

  public static byte[] expeditionNotice(final int type, final String name) {
    // 56 : '<Name>' has joined the expedition.
    // 58 : You have joined the expedition.
    // 60 : '<Name>' has left the expedition.
    // 62 : '<Name>' has been kicked out of the expedition.
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
    mplew.write(type);
    if (type != 58) {
      mplew.writeMapleAsciiString(name);
    }
    return mplew.getPacket();
  }

  public static byte[] changeExpeditionLeader(final int newLeader) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
    mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
    mplew.write(65);
    mplew.writeInt(newLeader);
    return mplew.getPacket();
  }

  public static byte[] expeditionUpdate(final int partyIndex, final MapleParty party) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
    mplew.write(66);
    mplew.writeInt(0);
    mplew.writeInt(partyIndex);
    if (party == null) {
      mplew.writeZeroBytes(178);
    } else {
      addPartyStatus(-1, party, mplew, false, true);
    }
    return mplew.getPacket();
  }

  public static byte[] expeditionInvite(final MapleCharacter from, final int exped) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
    mplew.write(68);
    mplew.writeInt(from.getLevel());
    mplew.writeInt(from.getJob());
    mplew.writeMapleAsciiString(from.getName());
    mplew.writeInt(exped);
    return mplew.getPacket();
  }

  public static byte[] expeditionStatusMessage(final int errcode, final String name) {
    // 0 : '<Name>' could not be found in the current server.
    // 1 : Admins can only invite other admins.
    // 2 : '<Name>' is already in a party.
    // 3 : '<Name>' does not meet the level requirement for the expedition.
    // 4 : '<Name>' is currently not accepting any expedition invites.
    // 5 : '<Name>' is taking care of another invitation.
    // 6 : You have already invited '<Name>' to the expedition.
    // 7 : '<Name>' has been invited to the expedition.
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
    mplew.write(69);
    mplew.writeInt(errcode);
    mplew.writeMapleAsciiString(name);
    return mplew.getPacket();
  }

  public static byte[] updatePartyHpForCharacter(final MapleCharacter partyMate) {
    return MapleUserPackets.updatePartyMemberHP(partyMate.getId(), partyMate.getStat().getHp(), partyMate.getStat().getCurrentMaxHp());
  }


}
