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

import handling.MapleServerHandler;
import handling.PacketProcessor;
import handling.mina.MapleCodecFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import server.config.ServerConfig;
import server.config.ServerEnvironment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class LoginServer extends GameServer {

    public static final int PORT = 8484;
    private Map<Integer, Integer> load = new HashMap<>();
    private String serverName, eventMessage;
    private byte flag;
    private int userLimit, usersOn = 0;
    private boolean finishedShutdown = true, adminOnly;

    private static LoginServer INSTANCE;

    public LoginServer(ServerConfig config) {
        super(-1, PORT, PacketProcessor.Mode.LOGINSERVER);
        userLimit = Integer.parseInt(config.getProperty("login.userlimit"));
        serverName = config.getProperty("login.serverName");
        eventMessage = config.getProperty("login.eventMessage");
        flag = Byte.parseByte(config.getProperty("login.flag"));
        adminOnly = Boolean.parseBoolean(config.getProperty("world.admin", "false"));
    }

    public static synchronized LoginServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LoginServer(ServerEnvironment.getConfig());
        }
        return INSTANCE;
    }

    public void addChannel(final int channel) {
        load.put(channel, 0);
    }

    public void removeChannel(final int channel) {
        load.remove(channel);
    }

    public void shutdown() {
        if (finishedShutdown) {
            return;
        }
        System.out.println("Shutting down login...");
        acceptor.unbindAll();
        finishedShutdown = true;
    }

    public final String getServerName() {
        return serverName;
    }

    public final String getServerEventMessage() {
        return eventMessage;
    }

    public final byte getServerFlag() {
        return flag;
    }

    public final Map<Integer, Integer> getServerLoad() {
        return load;
    }

    public void setLoad(final Map<Integer, Integer> load, final int usersOn) {
        this.load = load;
        this.usersOn = usersOn;
    }

    public final void setFlag(final byte flag) {
        this.flag = flag;
    }

    public final int getUserLimit() {
        return userLimit;
    }

    public final int getUsersOn() {
        return usersOn;
    }

    public final boolean isAdminOnly() {
        return adminOnly;
    }

}
