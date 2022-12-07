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

package handling.cashshop;

import handling.MapleServerHandler;
import handling.PacketProcessor;
import handling.channel.PlayerStorage;
import handling.mina.MapleCodecFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import server.ServerProperties;

import java.net.InetSocketAddress;

@lombok.extern.slf4j.Slf4j
public class CashShopServer {

    private final static int PORT = 8799;
    private static String ip;
    private static InetSocketAddress inetSocketAddr;
    private static IoAcceptor acceptor;
    private static PlayerStorage players;
    private static boolean finishedShutdown = false;

    public static final void run_startup_configurations() {
        ip = ServerProperties.getProperty("world.host") + ":" + PORT;

        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        acceptor = new SocketAcceptor();
        final SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getSessionConfig().setTcpNoDelay(true);
        cfg.setDisconnectOnUnbind(true);
        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
        players = new PlayerStorage(-10);

        try {
            inetSocketAddr = new InetSocketAddress(PORT);
            acceptor.bind(inetSocketAddr, new MapleServerHandler(-1, true,
                    PacketProcessor.getProcessor(PacketProcessor.Mode.CASHSHOP)), cfg);
            log.info("Listening on port " + PORT + ".");
        } catch (final Exception e) {
            System.err.println("Binding to port " + PORT + " failed");
            e.printStackTrace();
            throw new RuntimeException("Binding failed.", e);
        }
    }

    public static final String getIP() {
        return ip;
    }

    public static final PlayerStorage getPlayerStorage() {
        return players;
    }

    public static final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        log.info("Saving all connected clients (CS)...");
        players.disconnectAll();
        log.info("Shutting down CS...");
        acceptor.unbindAll();
        finishedShutdown = true;
    }

    public static boolean isShutdown() {
        return finishedShutdown;
    }
}
