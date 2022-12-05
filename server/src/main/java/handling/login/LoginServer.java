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

package handling.login;

import com.mysql.jdbc.log.Log;
import handling.MapleServerHandler;
import handling.PacketProcessor;
import handling.mina.MapleCodecFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import server.ServerProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class LoginServer {

    public static final int PORT = 8484;
    private static InetSocketAddress InetSocketadd;
    private static IoAcceptor acceptor;
    private static Map<Integer, Integer> load = new HashMap<Integer, Integer>();
    private static String serverName, eventMessage;
    private static byte flag;
    private static int maxCharacters, userLimit, usersOn = 0;
    private static boolean finishedShutdown = true, adminOnly = false;

    private static LoginServer INSTANCE;

    public LoginServer(){
        userLimit = Integer.parseInt(ServerProperties.getProperty("login.userlimit"));
        serverName = ServerProperties.getProperty("login.serverName");
        eventMessage = ServerProperties.getProperty("login.eventMessage");
        flag = Byte.parseByte(ServerProperties.getProperty("login.flag"));
        adminOnly = Boolean.parseBoolean(ServerProperties.getProperty("world.admin", "false"));
        maxCharacters = Integer.parseInt(ServerProperties.getProperty("login.maxCharacters"));

        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        acceptor = new SocketAcceptor();
        final SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getSessionConfig().setTcpNoDelay(true);
        cfg.setDisconnectOnUnbind(true);
        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));

        try {
            InetSocketadd = new InetSocketAddress(PORT);
            acceptor.bind(InetSocketadd, new MapleServerHandler(-1, false, PacketProcessor.getProcessor(PacketProcessor.Mode.LOGINSERVER)), cfg);
            System.out.println("Listening on port " + PORT + ".");
        } catch (IOException e) {
            System.err.println("Binding to port " + PORT + " failed" + e);
        }
    }

    public static synchronized LoginServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LoginServer();
        }
        return INSTANCE;
    }


    public final void addChannel(final int channel) {
        load.put(channel, 0);
    }

    public final void removeChannel(final int channel) {
        load.remove(channel);
    }

    public void shutdown() {
        if (finishedShutdown) {
            return;
        }
        System.out.println("Shutting down login...");
        acceptor.unbindAll();
        finishedShutdown = true; //nothing. lol
    }

    public final String getServerName() {
        return serverName;
    }

    public final String getEventMessage() {
        return eventMessage;
    }

    public final byte getFlag() {
        return flag;
    }

    public final int getMaxCharacters() {
        return maxCharacters;
    }

    public final Map<Integer, Integer> getLoad() {
        return load;
    }

    public void setLoad(final Map<Integer, Integer> load_, final int usersOn_) {
        load = load_;
        usersOn = usersOn_;
    }

    public final void setEventMessage(final String newMessage) {
        eventMessage = newMessage;
    }

    public final void setFlag(final byte newflag) {
        flag = newflag;
    }

    public final int getUserLimit() {
        return userLimit;
    }

    public final int getUsersOn() {
        return usersOn;
    }

    public final void setUserLimit(final int newLimit) {
        userLimit = newLimit;
    }

    public final int getNumberOfSessions() {
        return acceptor.getManagedSessions(InetSocketadd).size();
    }

    public final boolean isAdminOnly() {
        return adminOnly;
    }

}
