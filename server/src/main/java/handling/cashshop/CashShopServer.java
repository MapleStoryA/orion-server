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

import handling.PacketProcessor;
import handling.channel.PlayerStorage;
import handling.GameServer;
import lombok.extern.slf4j.Slf4j;
import server.ServerProperties;

@Slf4j
public class CashShopServer extends GameServer {

    private final static int PORT = 8799;
    private String ip;
    private PlayerStorage players;
    private boolean finishedShutdown = false;

    private static CashShopServer INSTANCE;

    public CashShopServer() {
        super(-1, 8799, PacketProcessor.Mode.CASHSHOP);
        players = new PlayerStorage(-10);
        ip = ServerProperties.getProperty("world.host") + ":" + PORT;
    }

    public final String getPublicAddress() {
        return ip;
    }

    public final PlayerStorage getPlayerStorage() {
        return players;
    }

    public final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        log.info("Saving all connected clients (CS)...");
        players.disconnectAll();
        log.info("Shutting down CS...");
        unbindAll();
        finishedShutdown = true;
    }

    public boolean isShutdown() {
        return finishedShutdown;
    }

    public static CashShopServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CashShopServer();
        }
        return INSTANCE;
    }
}
