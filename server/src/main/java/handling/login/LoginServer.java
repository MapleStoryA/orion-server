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

import client.MapleClient;
import handling.GameServer;
import handling.PacketProcessor;
import handling.world.WorldServer;
import server.ClientStorage;
import server.Timer;
import server.config.ServerConfig;
import server.config.ServerEnvironment;
import tools.MaplePacketCreator;
import tools.packet.LoginPacket;

import java.util.HashMap;
import java.util.Map;

@lombok.extern.slf4j.Slf4j
public class LoginServer extends GameServer {

    public static final int PORT = 8484;
    private static LoginServer INSTANCE;
    private final String serverName;
    private final String eventMessage;
    private final int userLimit;
    private final boolean adminOnly;
    private Map<Integer, Integer> load = new HashMap<>();
    private byte flag;
    private int usersOn = 0;
    private boolean finishedShutdown = true;
    private long lastUpdate = 0;

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
        log.info("Shutting down login...");
        unbindAll();
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

    public void registerClient(final MapleClient c) {
        if (LoginServer.getInstance().isAdminOnly() && !c.isGm()) {
            c.getSession().write(MaplePacketCreator.serverNotice(1, "The server is currently set to Admin login only.\r\nWe are currently testing some issues.\r\nPlease try again later."));
            c.getSession().write(LoginPacket.getLoginFailed(7));
            return;
        }

        if (System.currentTimeMillis() - lastUpdate > 600000) { // Update once every 10 minutes
            lastUpdate = System.currentTimeMillis();
            final Map<Integer, Integer> load = WorldServer.getInstance().getChannelLoad();
            int usersOn = 0;
            if (load == null || load.size() == 0) { // In an unfortunate event that client logged in before load
                lastUpdate = 0;
                c.getSession().write(LoginPacket.getLoginFailed(7));
                return;
            }
            final double loadFactor = 1200 / ((double) LoginServer.getInstance().getUserLimit() / load.size());
            for (Map.Entry<Integer, Integer> entry : load.entrySet()) {
                usersOn += entry.getValue();
                load.put(entry.getKey(), Math.min(1200, (int) (entry.getValue() * loadFactor)));
            }
            LoginServer.getInstance().setLoad(load, usersOn);
            lastUpdate = System.currentTimeMillis();
        }

        if (c.finishLogin() == 0) {
            c.getSession().write(LoginPacket.getAuthSuccessRequest(c));
            ClientStorage.addClient(c);
            c.setIdleTask(Timer.PingTimer.getInstance().schedule(() -> c.getSession().close(), 10 * 60 * 10000));
        } else {
            c.getSession().write(LoginPacket.getLoginFailed(7));
        }
    }

}
