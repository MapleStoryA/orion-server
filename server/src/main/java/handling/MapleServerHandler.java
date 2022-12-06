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
import handling.cashshop.CashShopOperationHandlers;
import handling.cashshop.CashShopServer;
import handling.channel.handler.InterServerHandler;
import handling.channel.handler.PlayerHandler;
import handling.mina.MaplePacketDecoder;
import handling.world.WorldServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import server.config.ServerEnvironment;
import tools.FileOutputUtil;
import tools.HexTool;
import tools.Randomizer;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

@Slf4j
public class MapleServerHandler extends IoHandlerAdapter {
    private final boolean isCashShop;
    private final PacketProcessor processor;
    private final int channel;

    public MapleServerHandler(int channel, boolean isCashShop, PacketProcessor processor) {
        this.channel = channel;
        this.isCashShop = isCashShop;
        this.processor = processor;
    }

    private void handlePacket(final RecvPacketOpcode header, final SeekableLittleEndianAccessor slea,
                              final MapleClient c) {
        switch (header) {
            case PLAYER_LOGGEDIN:
                final int playerId = slea.readInt();
                if (isCashShop) {
                    CashShopOperationHandlers.enterCashShop(playerId, c);
                } else {
                    InterServerHandler.loggedIn(playerId, c);
                }
                break;
            case CHANGE_MAP:
                if (isCashShop) {
                    CashShopOperationHandlers.leaveCashShop(slea, c, c.getPlayer());
                } else {
                    PlayerHandler.changeMap(slea, c, c.getPlayer());
                }
                break;
            default:
                if (slea.available() >= 0) {
                    FileOutputUtil.logPacket(String.valueOf(header), "[" + header + "] " + slea);
                }
                break;
        }
    }

    @Override
    public void messageSent(final IoSession session, final Object message) throws Exception {
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) {
    }

    @Override
    public void sessionOpened(final IoSession session) {
        final String address = session.getRemoteAddress().toString().split(":")[0];
        if (channel > -1) {
            if (WorldServer.getInstance().getChannel(channel).isShutdown()) {
                session.close();
                return;
            }
        } else if (isCashShop) {
            if (CashShopServer.isShutdown()) {
                session.close();
                return;
            }
        }
        final byte[] ivSend = new byte[]{82, 48, 120, (byte) Randomizer.nextInt(255)};
        final byte[] ivRecv = new byte[]{70, 114, 122, (byte) Randomizer.nextInt(255)};
        final var client = new MapleClient(ivSend, ivRecv, session);
        client.setChannel(channel);

        MaplePacketDecoder.DecoderState decoderState = new MaplePacketDecoder.DecoderState();
        session.setAttribute(MaplePacketDecoder.DECODER_STATE_KEY, decoderState);

        session.write(LoginPacket.getHello(ServerConstants.MAPLE_VERSION, ivSend, ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
        session.setIdleTime(IdleStatus.READER_IDLE, 60);
        session.setIdleTime(IdleStatus.WRITER_IDLE, 60);

        logServer(address);
    }

    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

        if (client != null) {
            try {
                client.disconnect(true, isCashShop);
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
            var slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream((byte[]) message));
            if (slea.available() < 2) {
                return;
            }
            var header_num = slea.readShort();
            var client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
            var packetHandler = processor.getHandler(header_num);
            if (ServerEnvironment.isDebugEnabled()) {
                log.info("Received: " + header_num);
            }
            if (ServerEnvironment.isDebugEnabled() && packetHandler != null) {
                log.info("[" + packetHandler.getClass().getSimpleName() + "]");
            }
            if (packetHandler != null && packetHandler.validateState(client)) {
                packetHandler.handlePacket(slea, client);
                return;
            }
            for (final RecvPacketOpcode recv : RecvPacketOpcode.values()) {
                if (recv.getValue() == header_num) {

                    if (!client.isReceiving()) {
                        return;
                    }
                    if (recv.needsChecking()) {
                        if (!client.isLoggedIn()) {
                            return;
                        }
                    }
                    handlePacket(recv, slea, client);
                    return;
                }
            }
            log.info("Received data: " + HexTool.toString((byte[]) message));
            log.info("Data: " + new String((byte[]) message));

        } catch (Exception e) {
            FileOutputUtil.outputFileError(FileOutputUtil.PacketEx_Log, e);
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

    private void logServer(String address) {
        StringBuilder sb = new StringBuilder();
        if (channel > -1) {
            sb.append("[Channel Server] Channel ").append(channel).append(" : ");
        } else if (isCashShop) {
            sb.append("[Cash Server]");
        } else {
            sb.append("[Login Server]");
        }
        sb.append("IoSession opened ").append(address);
        log.info(sb.toString());
    }
}
