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

package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import constants.PlayerGMRanking;
import constants.ServerConstants;
import handling.SendPacketOpcode;
import handling.login.LoginServer;
import server.Randomizer;
import tools.HexTool;
import tools.Triple;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoginPacket {

  public static final byte[] getHello(final short mapleVersion, final byte[] sendIv, final byte[] recvIv) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

    mplew.writeShort(14); // 13 = MSEA, 14 = GlobalMS, 15 = EMS
    mplew.writeShort(mapleVersion);
    mplew.writeMapleAsciiString(ServerConstants.MAPLE_PATCH);
    mplew.write(recvIv);
    mplew.write(sendIv);
    mplew.write(8); // 7 = MSEA, 8 = GlobalMS, 5 = Test Server

    return mplew.getPacket();
  }

  public static final byte[] getPing() {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

    mplew.writeShort(SendPacketOpcode.PING.getValue());

    return mplew.getPacket();
  }

  public static final byte[] getLoginFailed(final int reason) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

    /*	* 3: ID deleted or blocked
     * 4: Incorrect password
     * 5: Not a registered id
     * 6: System error
     * 7: Already logged in
     * 8: System error
     * 9: System error
     * 10: Cannot process so many connections
     * 11: Only users older than 20 can use this channel
     * 13: Unable to log on as master at this ip
     * 14: Wrong gateway or personal info and weird korean button
     * 15: Processing request with that korean button!
     * 16: Please verify your account through email...
     * 17: Wrong gateway or personal info
     * 21: Please verify your account through email...
     * 23: License agreement
     * 25: Maple Europe notice
     * 27: Some weird full client notice, probably for trial versions
     * 32: IP blocked
     * 84: please revisit website for pass change --> 0x07 recv with response 00/01*/

    mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
    mplew.writeInt(reason);
    mplew.writeShort(0);

    return mplew.getPacket();
  }

  public static final byte[] getPermBan(final byte reason) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

    mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
    mplew.writeShort(2); // Account is banned
    mplew.writeInt(0);
    mplew.writeShort(reason);
    mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));

    return mplew.getPacket();
  }

  public static final byte[] getTempBan(final long timestampTill, final byte reason) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);

    mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
    mplew.write(2);
    mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00"));
    mplew.write(reason);
    mplew.writeLong(timestampTill); // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.

    return mplew.getPacket();
  }

  public static final byte[] getAuthSuccessRequest(final MapleClient client) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
    mplew.write(0);// some error code shit, and nNumOfCharacter
    mplew.write(0);// sMsg + 500, 0 or 1 decodes a bunch of shit
    mplew.writeInt(0);// not read
    mplew.writeInt(client.getAccID()); // user id
    mplew.write(client.getGender());
    //
    PlayerGMRanking rank = PlayerGMRanking.getByLevel(client.getGMLevel());
    byte nSubGradeCode = 0;
    nSubGradeCode |= rank.getSubGrade();
    mplew.writeBool(rank.getLevel() >= PlayerGMRanking.GM.getLevel());// nGradeCode
    mplew.write(nSubGradeCode);// a short in v95
    // v90;
    // Value = (unsigned __int8)CInPacket::Decode1(v5);
    // v118 = ((unsigned int)(unsigned __int8)Value >> 8) & 1; this is for tester account.
    // v118 will only be 1 if nSubGradeCode is 0x100
    mplew.writeBool(false);// nCountryID, admin accounts?
    //
    mplew.writeMapleAsciiString(client.getAccountName());// sNexonClubID
    mplew.write(0);// nPurchaseExp
    mplew.write(0); // isquietbanned, nChatBlockReason
    mplew.writeLong(0);// isquietban time, dtChatUnblockDate
    mplew.writeLong(0); // creation time, dtRegisterDate
    mplew.writeInt(0);// nNumOfCharacter? or just reusing a variable
    mplew.write(2);// pin
    mplew.write(0);
    mplew.writeLong(0);// LABEL_120

    return mplew.getPacket();

  }

  public static final byte[] deleteCharResponse(final int cid, final int state) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
    mplew.writeInt(cid);
    // 6 : Trouble logging in? Try logging in again from maplestory.nexon.net.
    // 9 : Failed due to unknown reason.
    // 10 : Could not be processed due to too many connection requests to the server. Please try again later.
    // 18 : The 8-digit birthday code you have entered is incorrect.
    // 20 : You have entered an incorrect PIC.
    // 22 : Cannot delete Guild Master character.
    // 24 : You may not delete a character that has been engaged or booked for a wedding.
    // 26 : You cannot delete a character that is currently going through the transfer.
    // 29 : You may not delete a character that has a family.
    mplew.write(state);

    return mplew.getPacket();
  }

  public static final byte[] secondPwError(final byte mode) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

    /*
     * 14 - Invalid password
     * 15 - Second password is incorrect
     */
    mplew.writeShort(SendPacketOpcode.SECONDPW_ERROR.getValue());
    mplew.write(mode);

    return mplew.getPacket();
  }

  public static final byte[] getServerList(final int serverId, final String serverName, final Map<Integer, Integer> channelLoad) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
    mplew.write(serverId); // 0 = Aquilla, 1 = bootes, 2 = cass, 3 = delphinus
    final String worldName = serverName.substring(0, serverName.length() - 3); //remove the SEA
    mplew.writeMapleAsciiString(worldName);
    mplew.write(LoginServer.getFlag());
    mplew.writeMapleAsciiString(LoginServer.getEventMessage());
    mplew.writeShort(100);
    mplew.writeShort(100);
    mplew.write(0);

    int lastChannel = 1;
    Set<Integer> channels = channelLoad.keySet();
    for (int i = 30; i > 0; i--) {
      if (channels.contains(i)) {
        lastChannel = i;
        break;
      }
    }
    mplew.write(lastChannel);

    int load;
    for (int i = 1; i <= lastChannel; i++) {
      if (channels.contains(i)) {
        load = channelLoad.get(i);
      } else {
        load = 1200;
      }
      mplew.writeMapleAsciiString(worldName + "-" + i);
      mplew.writeInt(load);
      mplew.write(serverId);
      mplew.writeShort(i - 1);
    }
    mplew.writeShort(1);
    mplew.writePos(new Point(0, 280));
    mplew.writeMapleAsciiString(ServerConstants.WORLD_MESSAGE);

    return mplew.getPacket();
  }

  public static final byte[] getEndOfServerList() {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
    mplew.write(0xFF);

    return mplew.getPacket();
  }

  public static final byte[] getServerStatus(final int status) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    /*	 * 0 - Normal
     * 1 - Highly populated
     * 2 - Full*/
    mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
    mplew.writeShort(status);

    return mplew.getPacket();
  }

  public static final byte[] getCharList(final boolean secondpw, final List<MapleCharacter> chars, int charslots) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
    mplew.write(0);
    mplew.write(chars.size()); // 1

    for (final MapleCharacter chr : chars) {
      boolean isGM = chr.getJob() == 900 || chr.getJob() == 910;
      addCharEntry(mplew, chr, !isGM && chr.getLevel() >= 10);
    }
    mplew.write(2); // second pw request
    mplew.writeLong(charslots);

    return mplew.getPacket();
  }

  public static final byte[] addNewCharEntry(final MapleCharacter chr, final boolean worked) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
    mplew.write(worked ? 0 : 1);
    addCharEntry(mplew, chr, false);

    return mplew.getPacket();
  }

  public static byte[] pinOperation(final byte mode) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

    mplew.writeShort(SendPacketOpcode.PIN_OPERATION.getValue());
    mplew.write(mode);

    return mplew.getPacket();
  }

  public static final byte[] charNameResponse(final String charname, final boolean nameUsed) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
    mplew.writeMapleAsciiString(charname);
    mplew.write(nameUsed ? 1 : 0);

    return mplew.getPacket();
  }

  public static final byte[] getRelogResponse() {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

    mplew.writeShort(SendPacketOpcode.RELOG_RESPONSE.getValue());
    mplew.write(1);

    return mplew.getPacket();
  }

  public static final byte[] blockViewAll() {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
    mplew.write(7);
    mplew.write(0);

    return mplew.getPacket();
  }


  public static final void addCharEntry(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr, boolean ranking) {
    PacketHelper.addCharStats(mplew, chr);
    PacketHelper.addCharLook(mplew, chr, true);
    mplew.write(0); //<-- who knows
    mplew.write(ranking ? 1 : 0);
    if (ranking) {
      mplew.writeInt(chr.getRank());
      mplew.writeInt(chr.getRankMove());
      mplew.writeInt(chr.getJobRank());
      mplew.writeInt(chr.getJobRankMove());
    }
  }


  /**
   * TODO: Fix this..
   */


  public static byte[] changeBackground(List<Triple<String, Integer, Boolean>> backgrounds) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CHANGE_BACKGROUND.getValue());
    mplew.write(backgrounds.size()); //number of bgs


    for (Triple<String, Integer, Boolean> background : backgrounds) {
      mplew.writeMapleAsciiString(background.getLeft());
      mplew.write(background.getRight() ? Randomizer.nextInt(2) : background.getMid());
    }
        /* 
         Map.wz/Obj/login.img/WorldSelect/background/background number
         Backgrounds ids sometime have more than one background anumation
         Background are like layers, backgrounds in the packets are
         removed, so the background which was hiden by the last one
         is shown.
         */
    return mplew.getPacket();
  }


  public static byte[] getRecommendedWorldMessage(int worldID, String message) {
    MaplePacketLittleEndianWriter k = new MaplePacketLittleEndianWriter();
    k.writeShort(SendPacketOpcode.RECOMMENDED_WORLD_MESSAGE.getValue());
    k.write(1);
    k.writeInt(worldID);
    k.writeMapleAsciiString(message);

    return k.getPacket();
  }


}
