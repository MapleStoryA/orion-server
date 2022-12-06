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

package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import server.shops.AbstractPlayerStore.BoughtItem;
import tools.Pair;

import java.util.List;

public interface IMaplePlayerShop {

    byte HIRED_MERCHANT = 1;
    byte PLAYER_SHOP = 2;
    byte OMOK = 3;
    byte MATCH_CARD = 4;

    String getOwnerName();

    String getDescription();

    List<Pair<Byte, MapleCharacter>> getVisitors();

    List<MaplePlayerShopItem> getItems();

    boolean isOpen();

    void setOpen(boolean open);

    boolean removeItem(int item);

    boolean isOwner(MapleCharacter chr);

    byte getShopType();

    byte getVisitorSlot(MapleCharacter visitor);

    byte getFreeSlot();

    int getItemId();

    int getMesos();

    void setMesos(int meso);

    int getOwnerId();

    int getOwnerAccId();

    void addItem(MaplePlayerShopItem item);

    void removeFromSlot(int slot);

    void broadcastToVisitors(byte[] packet);

    void addVisitor(MapleCharacter visitor);

    void removeVisitor(MapleCharacter visitor);

    void removeAllVisitors(int error, int type);

    void buy(MapleClient c, int item, short quantity);

    void closeShop(boolean saveItems, boolean remove);

    String getPassword();

    int getMaxSize();

    int getSize();

    int getGameType();

    void update();

    boolean isAvailable();

    void setAvailable(boolean b);

    List<BoughtItem> getBoughtItems();


}
