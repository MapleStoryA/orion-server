package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import constants.PlayerGMRanking;
import constants.ServerConstants;
import handling.login.LoginServer;
import java.awt.*;
import java.util.Map;
import java.util.Set;
import networking.packet.SendPacketOpcode;
import tools.HexTool;
import tools.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class LoginPacket {

    public static final byte[] getHello(final short mapleVersion, final byte[] sendIv, final byte[] recvIv) {
        final OutPacket packet = new OutPacket(16);

        packet.writeShort(14); // 13 = MSEA, 14 = GlobalMS, 15 = EMS
        packet.writeShort(mapleVersion);
        packet.writeMapleAsciiString(ServerConstants.MAPLE_PATCH);
        packet.write(recvIv);
        packet.write(sendIv);
        packet.write(8); // 7 = MSEA, 8 = GlobalMS, 5 = Test Server

        return packet.getPacket();
    }

    public static final byte[] getPing() {
        final OutPacket packet = new OutPacket(16);

        packet.writeShort(SendPacketOpcode.PING.getValue());

        return packet.getPacket();
    }

    public static final byte[] getLoginFailed(final int reason) {
        final OutPacket packet = new OutPacket(16);

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

        packet.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        packet.writeInt(reason);
        packet.writeShort(0);

        return packet.getPacket();
    }

    public static final byte[] getPermBan(final int reason) {
        final OutPacket packet = new OutPacket(16);

        packet.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        packet.write(2);
        packet.write(HexTool.getByteArrayFromHexString("00 00 00 00 00"));
        packet.write(reason);
        packet.writeLong(150841440000000000L);

        return packet.getPacket();
    }

    public static final byte[] getTempBan(final long timestampTill, final byte reason) {
        final OutPacket packet = new OutPacket(17);

        packet.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        packet.write(2);
        packet.write(HexTool.getByteArrayFromHexString("00 00 00 00 00"));
        packet.write(reason);
        packet.writeLong(timestampTill); // Tempban date is handled as a 64-bit long, number of 100NS
        // intervals since 1/1/1601. Lulz.

        return packet.getPacket();
    }

    public static final byte[] getAuthSuccessRequest(final MapleClient client) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        packet.write(0); // some error code shit, and nNumOfCharacter
        packet.write(0); // sMsg + 500, 0 or 1 decodes a bunch of shit
        packet.writeInt(0); // not read
        packet.writeInt(client.getAccountData().getId()); // user id
        packet.write(client.getAccountData().getGender());
        //
        PlayerGMRanking rank =
                PlayerGMRanking.getByLevel(client.getAccountData().getGMLevel());
        byte nSubGradeCode = 0;
        nSubGradeCode |= rank.getSubGrade();
        packet.writeBool(rank.getLevel() >= PlayerGMRanking.GM.getLevel()); // nGradeCode
        packet.write(nSubGradeCode); // a short in v95
        // v90;
        // Value = (unsigned __int8)CInPacket::Decode1(v5);
        // v118 = ((unsigned int)(unsigned __int8)Value >> 8) & 1; this is for tester account.
        // v118 will only be 1 if nSubGradeCode is 0x100
        packet.writeBool(true); // nCountryID, admin accounts?
        //
        packet.writeMapleAsciiString(client.getAccountData().getName()); // sNexonClubID
        packet.write(0); // nPurchaseExp
        packet.write(0); // isquietbanned, nChatBlockReason
        packet.writeLong(0); // isquietban time, dtChatUnblockDate
        packet.writeLong(0); // creation time, dtRegisterDate
        packet.writeInt(0); // nNumOfCharacter? or just reusing a variable
        packet.write(2); // pin
        packet.write(0);
        packet.writeLong(0); // LABEL_120

        return packet.getPacket();
    }

    public static final byte[] deleteCharResponse(final int cid, final int state) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
        packet.writeInt(cid);
        // 6 : Trouble logging in? Try logging in again from maplestory.nexon.net.
        // 9 : Failed due to unknown reason.
        // 10 : Could not be processed due to too many connection requests to the server. Please try
        // again later.
        // 18 : The 8-digit birthday code you have entered is incorrect.
        // 20 : You have entered an incorrect PIC.
        // 22 : Cannot delete Guild Master character.
        // 24 : You may not delete a character that has been engaged or booked for a wedding.
        // 26 : You cannot delete a character that is currently going through the transfer.
        // 29 : You may not delete a character that has a family.
        packet.write(state);

        return packet.getPacket();
    }

    public static final byte[] secondPwError(final byte mode) {
        final OutPacket packet = new OutPacket(3);

        /*
         * 14 - Invalid password
         * 15 - Second password is incorrect
         */
        packet.writeShort(SendPacketOpcode.SECONDPW_ERROR.getValue());
        packet.write(mode);

        return packet.getPacket();
    }

    public static final byte[] getServerList(
            final int serverId, final String serverName, final Map<Integer, Integer> channelLoad) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        packet.write(serverId); // 0 = Aquilla, 1 = bootes, 2 = cass, 3 = delphinus
        final String worldName = serverName.substring(0, serverName.length() - 3); // remove the SEA
        packet.writeMapleAsciiString(worldName);
        packet.write(LoginServer.getInstance().getServerFlag());
        packet.writeMapleAsciiString(LoginServer.getInstance().getServerEventMessage());
        packet.writeShort(100);
        packet.writeShort(100);
        packet.write(0);

        int lastChannel = 1;
        Set<Integer> channels = channelLoad.keySet();
        for (int i = 30; i > 0; i--) {
            if (channels.contains(i)) {
                lastChannel = i;
                break;
            }
        }
        packet.write(lastChannel);

        int load;
        for (int i = 1; i <= lastChannel; i++) {
            if (channels.contains(i)) {
                load = channelLoad.get(i);
            } else {
                load = 1200;
            }
            packet.writeMapleAsciiString(worldName + "-" + i);
            packet.writeInt(load);
            packet.write(serverId);
            packet.writeShort(i - 1);
        }
        packet.writeShort(1);
        packet.writePos(new Point(0, 280));
        packet.writeMapleAsciiString(ServerConstants.WORLD_MESSAGE);

        return packet.getPacket();
    }

    public static final byte[] getEndOfServerList() {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        packet.write(0xFF);

        return packet.getPacket();
    }

    public static final byte[] getServerStatus(final int status) {
        final OutPacket packet = new OutPacket();

        /*	 * 0 - Normal
         * 1 - Highly populated
         * 2 - Full*/
        packet.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
        packet.writeShort(status);

        return packet.getPacket();
    }

    public static final byte[] addNewCharEntry(final MapleCharacter chr, final boolean worked) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        packet.write(worked ? 0 : 1);
        addCharEntry(packet, chr, false);

        return packet.getPacket();
    }

    public static byte[] pinOperation(final byte mode) {
        final OutPacket packet = new OutPacket(3);

        packet.writeShort(SendPacketOpcode.PIN_OPERATION.getValue());
        packet.write(mode);

        return packet.getPacket();
    }

    public static final byte[] charNameResponse(final String charname, final boolean nameUsed) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
        packet.writeMapleAsciiString(charname);
        packet.write(nameUsed ? 1 : 0);

        return packet.getPacket();
    }

    public static final byte[] getRelogResponse() {
        final OutPacket packet = new OutPacket(3);

        packet.writeShort(SendPacketOpcode.RELOG_RESPONSE.getValue());
        packet.write(1);

        return packet.getPacket();
    }

    public static final byte[] blockViewAll() {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        packet.write(7);
        packet.write(0);

        return packet.getPacket();
    }

    public static final void addCharEntry(final OutPacket packet, final MapleCharacter chr, boolean ranking) {
        PacketHelper.addCharStats(packet, chr);
        PacketHelper.addCharLook(packet, chr, true);
        packet.write(0);
        packet.write(ranking ? 1 : 0);
        if (ranking) {
            packet.writeInt(0);
            packet.writeInt(0);
            packet.writeInt(0);
            packet.writeInt(0);
        }
    }

    /*
     * CLogin::OnPacket
     * CMapLoadable::OnPacket(int a1, int a2)
     * CMapLoadable::OnSetBackEffect(a2);
     * */
    public static byte[] sendCMapLoadable__OnSetBackEffect(int nEffect, int nField, int nPageID, int duration) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MapLoadable__OnSetBackEffect.getValue());
        packet.write(nEffect); // nEffect
        packet.writeInt(nField); // nField
        packet.write(nPageID); // nPageID
        packet.writeInt(duration); // Duration

        return packet.getPacket();
    }

    /*
     * CLogin::OnPacket
     * CMapLoadable::OnPacket(int a1, int a2)
     * CMapLoadable::OnSetMapObjectVisible(a2);
     * */
    public static byte[] sendCMapLoadable__OnSetMapObjectVisible() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CMapLoadable__OnSetMapObjectVisible.getValue());
        packet.write(2);
        packet.writeMapleAsciiString("dual/0");
        packet.write(0);
        packet.writeMapleAsciiString("visitors/0");
        packet.write(1);

        /*
        Map.wz/Obj/login.img/WorldSelect/background/background number
        Backgrounds ids sometime have more than one background anumation
        Background are like layers, backgrounds in the packets are
        removed, so the background which was hiden by the last one
        is shown.
        */
        return packet.getPacket();
    }

    public static byte[] getRecommendedWorldMessage(int worldID, String message) {
        OutPacket k = new OutPacket();
        k.writeShort(SendPacketOpcode.RECOMMENDED_WORLD_MESSAGE.getValue());
        k.write(1);
        k.writeInt(worldID);
        k.writeMapleAsciiString(message);

        return k.getPacket();
    }
}
