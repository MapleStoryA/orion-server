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

package handling;

import client.MapleClient;
import constants.ServerConstants;
import handling.cashshop.CashShopServer;
import handling.cashshop.handler.CashShopOperationUtils;
import handling.channel.handler.InterServerHandler;
import handling.channel.handler.PlayerHandler;
import handling.mina.MaplePacketDecoder;
import handling.world.WorldServer;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import server.Randomizer;
import server.config.ServerEnvironment;
import tools.FileOutputUtil;
import tools.HexTool;
import tools.MapleAESOFB;
import tools.Pair;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@lombok.extern.slf4j.Slf4j
public class MapleServerHandler extends IoHandlerAdapter {

    public static final boolean Log_Packets = true;
    private final boolean cs;
    private final List<String> BlockedIP = new ArrayList<String>();
    private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap<String, Pair<Long, Byte>>();
    private final PacketProcessor processor;
    private int channel = -1;
    private boolean login;

    public MapleServerHandler(final int channel, final boolean cs, PacketProcessor processor) {
        this.channel = channel;
        this.cs = cs;
        this.processor = processor;
    }

    public static final void handlePacket(final RecvPacketOpcode header, final SeekableLittleEndianAccessor slea,
                                          final MapleClient c, final boolean cs) throws Exception {
        switch (header) {
            case PLAYER_LOGGEDIN:
                final int playerid = slea.readInt();
                if (cs) {
                    CashShopOperationUtils.EnterCS(playerid, c);
                } else {
                    InterServerHandler.Loggedin(playerid, c);
                }
                break;
            case CHANGE_MAP:
                if (cs) {
                    CashShopOperationUtils.LeaveCS(slea, c, c.getPlayer());
                } else {
                    PlayerHandler.ChangeMap(slea, c, c.getPlayer());
                }
                break;
            default:
                if (slea.available() >= 0) { // we don't want to log headers only
                    FileOutputUtil.logPacket(String.valueOf(header), "[" + header + "] " + slea);
                    // log.info("[UNHANDLED] Recv [" + header.toString() +
                    // "] found");
                }
                break;
        }
    }

    @Override
    public void messageSent(final IoSession session, final Object message) throws Exception {
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
    }

    @Override
    public void sessionOpened(final IoSession session) throws Exception {
        // Start of IP checking
        final String address = session.getRemoteAddress().toString().split(":")[0];

        if (BlockedIP.contains(address)) {
            session.close();
            return;
        }
        final Pair<Long, Byte> track = tracker.get(address);

        byte count;
        if (track == null) {
            count = 1;
        } else {
            count = track.right;

            final long difference = System.currentTimeMillis() - track.left;
            if (difference < 2000) { // Less than 2 sec
                count++;
            } else if (difference > 20000) { // Over 20 sec
                count = 1;
            }
            if (count >= 10) {
                BlockedIP.add(address);
                tracker.remove(address); // Cleanup
                session.close();
                return;
            }
        }
        tracker.put(address, new Pair<Long, Byte>(System.currentTimeMillis(), count));
        // End of IP checking.

        if (channel > -1) {
            if (WorldServer.getInstance().getChannel(channel).isShutdown()) {
                session.close();
                return;
            }
        } else if (cs) {
            if (CashShopServer.isShutdown()) {
                session.close();
                return;
            }
        }
        final byte[] serverRecv = new byte[]{70, 114, 122, (byte) Randomizer.nextInt(255)};
        final byte[] serverSend = new byte[]{82, 48, 120, (byte) Randomizer.nextInt(255)};
        final byte[] ivRecv = ServerConstants.Use_Fixed_IV ? new byte[]{9, 0, 0x5, 0x5F} : serverRecv;
        final byte[] ivSend = ServerConstants.Use_Fixed_IV ? new byte[]{1, 0x5F, 4, 0x3F} : serverSend;

        final MapleClient client = new MapleClient(
                new MapleAESOFB(ivSend, (short) (0xFFFF - ServerConstants.MAPLE_VERSION)), // Sent
                // Cypher
                new MapleAESOFB(ivRecv, ServerConstants.MAPLE_VERSION), // Recv
                // Cypher
                session);
        client.setChannel(channel);

        MaplePacketDecoder.DecoderState decoderState = new MaplePacketDecoder.DecoderState();
        session.setAttribute(MaplePacketDecoder.DECODER_STATE_KEY, decoderState);

        session.write(
                LoginPacket.getHello(ServerConstants.MAPLE_VERSION, ServerConstants.Use_Fixed_IV ? serverSend : ivSend,
                        ServerConstants.Use_Fixed_IV ? serverRecv : ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
        session.setIdleTime(IdleStatus.READER_IDLE, 60);
        session.setIdleTime(IdleStatus.WRITER_IDLE, 60);

        StringBuilder sb = new StringBuilder();
        if (channel > -1) {
            sb.append("[Channel Server] Channel ").append(channel).append(" : ");
        } else if (cs) {
            sb.append("[Cash Server]");
        } else if (login) {
            sb.append("[Login Server]");
        } else {
            return;
        }
        sb.append("IoSession opened ").append(address);
        log.info(sb.toString());
    }

    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

        if (client != null) {
            try {
                client.disconnect(true, cs);
            } finally {
                session.close();
                session.removeAttribute(MapleClient.CLIENT_KEY);

            }
        }
        super.sessionClosed(session);
    }

    @Override
    public void messageReceived(final IoSession session, final Object message) {
        try {
            final SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(
                    new ByteArrayByteStream((byte[]) message));
            if (slea.available() < 2) {
                return;
            }
            final short header_num = slea.readShort();
            final MapleClient c = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
            MaplePacketHandler packetHandler = processor.getHandler(header_num);
            if (ServerEnvironment.isDebugEnabled()) {
                //log.info("Received: " + header_num);
            }
            if (ServerEnvironment.isDebugEnabled() && packetHandler != null) {
                // log.info("[" + packetHandler.getClass().getSimpleName() + "]");
            }
            if (packetHandler != null && packetHandler.validateState(c)) {
                packetHandler.handlePacket(slea, c);
                return;
            }
            for (final RecvPacketOpcode recv : RecvPacketOpcode.values()) {
                if (recv.getValue() == header_num) {

                    if (!c.isReceiving()) {
                        return;
                    }
                    if (recv.NeedsChecking()) {
                        if (!c.isLoggedIn()) {
                            return;
                        }
                    }
                    handlePacket(recv, slea, c, cs);
                    return;
                }
            }
            log.info("Received data: " + HexTool.toString((byte[]) message));
            log.info("Data: " + new String((byte[]) message));

            if (slea.available() == 0) { // we don't want to log headers only
            }

        } catch (Exception e) {
            FileOutputUtil.outputFileError(FileOutputUtil.PacketEx_Log, e);
            e.printStackTrace();
        }

    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

        if (client != null) {
            client.sendPing();
        }
        super.sessionIdle(session, status);
    }
}
