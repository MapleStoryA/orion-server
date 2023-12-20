/*
This file is part of the ZeroFusion MapleStory Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>
ZeroFusion organized by "RMZero213" <RMZero213@hotmail.com>

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

package server.cashshop;

import client.MapleClient;
import client.MapleJob;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.GameConstants;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import provider.MapleData;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.Pair;
import tools.packet.MTSCSPacket;

@lombok.extern.slf4j.Slf4j
public class CashShop implements Serializable {

    private static final long serialVersionUID = 231541893513373579L;
    private final int accountId;
    private final int characterId;
    private final ItemLoader factory;
    private final List<IItem> inventory = new ArrayList<IItem>();
    private final List<Integer> uniqueids = new ArrayList<Integer>();

    public CashShop(int accountId, int characterId, MapleJob mapleJob) throws SQLException {
        this.accountId = accountId;
        this.characterId = characterId;
        int job_id = mapleJob.getId();
        if (job_id / 1000 == 1) {
            factory = ItemLoader.CASHSHOP_CYGNUS;
        } else if ((job_id / 100 == 21 || job_id / 100 == 20) && job_id != 2001) {
            factory = ItemLoader.CASHSHOP_ARAN;
        } else if (job_id == 2001 || job_id / 100 == 22) {
            factory = ItemLoader.CASHSHOP_EVAN;
        } else if (job_id >= 3000) {
            factory = ItemLoader.CASHSHOP_RESIST;
        } else if (job_id / 10 == 43) {
            factory = ItemLoader.CASHSHOP_DB;
        } else {
            factory = ItemLoader.CASHSHOP_EXPLORER;
        }

        for (Pair<IItem, MapleInventoryType> item :
                factory.loadItems(false, accountId).values()) {
            inventory.add(item.getLeft());
        }
    }

    public int getItemsSize() {
        return inventory.size();
    }

    public List<IItem> getInventory() {
        return inventory;
    }

    public IItem findByCashId(int cashId) {
        for (IItem item : inventory) {
            if (item.getSN() == cashId) {
                return item;
            }
        }

        return null;
    }

    public void checkExpire(MapleClient c) {
        List<IItem> toberemove = new ArrayList<IItem>();
        for (IItem item : inventory) {
            if (item != null
                    && !GameConstants.isPet(item.getItemId())
                    && item.getExpiration() > 0
                    && item.getExpiration() < System.currentTimeMillis()) {
                toberemove.add(item);
            }
        }
        if (toberemove.size() > 0) {
            for (IItem item : toberemove) {
                removeFromInventory(item);
                c.getSession().write(MTSCSPacket.cashItemExpired(item.getSN()));
            }
            toberemove.clear();
        }
    }

    public IItem toItemWithQuantity(CashItemInfo cItem, int quantity, String gift) {
        return toItem(cItem, MapleInventoryManipulator.getUniqueId(cItem.getId(), null), gift, quantity);
    }

    public IItem toItem(CashItemInfo cItem) {
        return toItem(cItem, MapleInventoryManipulator.getUniqueId(cItem.getId(), null), "", 0);
    }

    public IItem toItem(CashItemInfo cItem, String gift) {
        return toItem(cItem, MapleInventoryManipulator.getUniqueId(cItem.getId(), null), gift, 0);
    }

    public IItem toItem(CashItemInfo cItem, int uniqueid) {
        return toItem(cItem, uniqueid, "", 0);
    }

    public IItem toItem(CashItemInfo cItem, int uniqueid, String gift, int quantity) {
        if (uniqueid <= 0) {
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        long period = cItem.getPeriod();
        if (GameConstants.isPet(cItem.getId())) {
            period = 45;
        }
        IItem ret = null;
        if (GameConstants.getInventoryType(cItem.getId()) == MapleInventoryType.EQUIP) {
            Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(cItem.getId());
            eq.setSN(uniqueid);
            if (period > 0) {
                eq.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
            }
            eq.setGiftFrom(gift);
            if (GameConstants.isEffectRing(cItem.getId()) && uniqueid > 0) {
                MapleRing ring = MapleRing.loadFromDb(uniqueid);
                if (ring != null) {
                    eq.setRing(ring);
                }
            }
            ret = eq.copy();
        } else {
            Item item = new Item(
                    cItem.getId(), (byte) 0, (short) (quantity > 0 ? quantity : cItem.getCount()), (byte) 0, uniqueid);
            if (period > 0) {
                item.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
            }
            item.setGiftFrom(gift);
            if (GameConstants.isPet(cItem.getId())) {
                final MaplePet pet = MaplePet.createPet(cItem.getId(), uniqueid);
                if (pet != null) {
                    item.setPet(pet);
                }
            }
            ret = item.copy();
        }
        return ret;
    }

    public void addToInventory(IItem item) {
        inventory.add(item);
    }

    public void removeFromInventory(IItem item) {
        inventory.remove(item);
    }

    public void gift(int recipient, String from, String message, int sn) {
        gift(recipient, from, message, sn, 0);
    }

    public void gift(int recipient, String from, String message, int sn, int uniqueid) {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO `gifts` VALUES (DEFAULT, ?, ?, ?, ?, ?)");
            ps.setInt(1, recipient);
            ps.setString(2, from);
            ps.setString(3, message);
            ps.setInt(4, sn);
            ps.setInt(5, uniqueid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public List<Pair<IItem, String>> loadGifts() {
        List<Pair<IItem, String>> gifts = new ArrayList<Pair<IItem, String>>();
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `gifts` WHERE `recipient` = ?");
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();
            // TODO: Remove from here.
            final MapleData rootNode = CashItemFactory.data.getData("CashPackage.img");
            List<MapleData> children = rootNode.getChildren();
            while (rs.next()) {
                CashItemInfo cItem = CashItemFactory.getInstance().getItem(rs.getInt("sn"));
                IItem item = toItem(cItem, rs.getInt("uniqueid"), rs.getString("from"), 0);
                gifts.add(new Pair<IItem, String>(item, rs.getString("message")));
                uniqueids.add(item.getSN());
                List<CashItemInfo> packages = CashItemFactory.getInstance().getPackageItems(cItem.getId(), children);
                if (packages != null && packages.size() > 0) {
                    for (CashItemInfo packageItem : packages) {
                        addToInventory(toItem(packageItem, rs.getString("from")));
                    }
                } else {
                    addToInventory(item);
                }
            }

            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM `gifts` WHERE `recipient` = ?");
            ps.setInt(1, characterId);
            ps.executeUpdate();
            ps.close();
            save();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return gifts;
    }

    public boolean canSendNote(int uniqueid) {
        return uniqueids.contains(uniqueid);
    }

    public void sendedNote(int uniqueid) {
        for (int i = 0; i < uniqueids.size(); i++) {
            if (uniqueids.get(i).intValue() == uniqueid) {
                uniqueids.remove(i);
            }
        }
    }

    public void save() throws SQLException {
        List<Pair<IItem, MapleInventoryType>> itemsWithType = new ArrayList<Pair<IItem, MapleInventoryType>>();

        for (IItem item : inventory) {
            itemsWithType.add(
                    new Pair<IItem, MapleInventoryType>(item, GameConstants.getInventoryType(item.getItemId())));
        }

        factory.saveItems(itemsWithType, accountId);
    }
}
