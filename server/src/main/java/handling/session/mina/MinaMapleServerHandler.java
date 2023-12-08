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

package handling.session.mina;

import client.MapleClient;
import constants.ServerConstants;
import handling.PacketProcessor;
import handling.cashshop.CashShopServer;
import handling.session.DefaultPacketHandler;
import handling.session.NetworkSession;
import handling.world.WorldServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import tools.Randomizer;
import tools.packet.LoginPacket;

@Slf4j
public class MinaMapleServerHandler extends IoHandlerAdapter {
    private final PacketProcessor processor;
    private final PacketProcessor.Mode mode;

    private final int channel;

    public MinaMapleServerHandler(int channel, PacketProcessor.Mode mode) {
        this.channel = channel;
        this.mode = mode;
        this.processor = PacketProcessor.getProcessor(mode);
    }

    @Override
    public void messageSent(final IoSession session, final Object message) throws Exception {
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) {}

    @Override
    public void sessionOpened(final IoSession session) {
        if (channel > -1) {
            if (WorldServer.getInstance().getChannel(channel).isShutdown()) {
                session.close();
                return;
            }
        } else if (PacketProcessor.Mode.CASHSHOP.equals(mode)) {
            if (CashShopServer.getInstance().isShutdown()) {
                session.close();
                return;
            }
        }
        NetworkSession minaSession = new MinaNetworkSession(session);
        final byte[] ivSend = new byte[] {82, 48, 120, (byte) Randomizer.nextInt(255)};
        final byte[] ivRecv = new byte[] {70, 114, 122, (byte) Randomizer.nextInt(255)};
        final var client = new MapleClient(ivSend, ivRecv, minaSession);
        client.setChannel(channel);

        MinaMaplePacketDecoder.DecoderState decoderState =
                new MinaMaplePacketDecoder.DecoderState();
        session.setAttribute(MinaMaplePacketDecoder.DECODER_STATE_KEY, decoderState);

        session.write(LoginPacket.getHello(ServerConstants.MAPLE_VERSION, ivSend, ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
        session.setIdleTime(IdleStatus.READER_IDLE, 60);
        session.setIdleTime(IdleStatus.WRITER_IDLE, 60);
    }

    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

        if (client != null) {
            try {
                client.disconnect(true, PacketProcessor.Mode.CASHSHOP.equals(mode));
            } finally {
                session.close();
                session.removeAttribute(MapleClient.CLIENT_KEY);
            }
        }
        super.sessionClosed(session);
    }

    @Override
    public void messageReceived(final IoSession session, final Object message) {
        var client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        DefaultPacketHandler.handlePacket(
                client, processor, PacketProcessor.Mode.CASHSHOP.equals(mode), (byte[]) message);
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
