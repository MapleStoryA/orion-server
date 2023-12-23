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

package client.inventory;

import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import tools.collection.Pair;

@Slf4j
public enum ItemLoader {
    INVENTORY("inventoryitems", "inventoryequipment", 0, "characterid"),
    STORAGE("inventoryitems", "inventoryequipment", 1, "accountid"),
    CASH_SHOP_EXPLORER("csitems", "csequipment", 2, "accountid"),
    CASH_SHOP_CYGNUS("csitems", "csequipment", 3, "accountid"),
    CASH_SHOP_ARAN("csitems", "csequipment", 4, "accountid"),
    HIRED_MERCHANT("hiredmerchitems", "hiredmerchequipment", 5, "packageid", "accountid", "characterid"),
    CASH_SHOP_EVAN("csitems", "csequipment", 7, "accountid"),
    CASH_SHOP_DB("csitems", "csequipment", 10, "accountid"),
    CASH_SHOP_RESIST("csitems", "csequipment", 11, "accountid");
    private final int value;
    private final String tableInventoryItemsName;
    private final String tableEquipName;
    private final List<String> filterFields;

    ItemLoader(String tableInventoryItemsName, String tableEquipName, int value, String... filterFields) {
        this.tableInventoryItemsName = tableInventoryItemsName;
        this.tableEquipName = tableEquipName;
        this.value = value;
        this.filterFields = Arrays.asList(filterFields);
    }

    public Map<Integer, Pair<IItem, MapleInventoryType>> loadInventoryItems(Integer... id) throws SQLException {
        List<Integer> ids = Arrays.asList(id);
        Map<Integer, Pair<IItem, MapleInventoryType>> items = new LinkedHashMap<>();
        if (ids.size() != filterFields.size()) {
            return items;
        }
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(createSelectAllQuery());
            ps.setInt(1, value);
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 2, ids.get(i));
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MapleInventoryType mapleInventoryType = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                Item item;
                if (isEquipInventory(mapleInventoryType)) {
                    item = mapEquipFromResultSet(rs, mapleInventoryType);
                } else {
                    item = mapItemFromResultSet(rs);
                }
                items.put(rs.getInt("inventoryitemid"), new Pair<>(item.copy(), mapleInventoryType));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("Could not loadItems", ex);
        }
        return items;
    }

    public void saveItems(List<Pair<IItem, MapleInventoryType>> items, Integer... id) {
        List<Integer> ids = Arrays.asList(id);
        if (ids.size() != filterFields.size()) {
            return;
        }

        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(createDeleteFromQuery());
            ps.setInt(1, value);
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 2, ids.get(i));
            }
            ps.executeUpdate();
            ps.close();

            StringBuilder insertIntoQuery = createInsertInventoryItemsQuery();
            ps = con.prepareStatement(insertIntoQuery.toString(), Statement.RETURN_GENERATED_KEYS);
            PreparedStatement pse = con.prepareStatement(createInsertIntoTableEquipQuery());
            final Iterator<Pair<IItem, MapleInventoryType>> it = items.iterator();
            Pair<IItem, MapleInventoryType> pair;
            while (it.hasNext()) {
                pair = it.next();
                IItem item = pair.getLeft();
                MapleInventoryType mapleInventoryType = pair.getRight();
                int i = 1;
                for (int current = 0; current < ids.size(); current++) {
                    ps.setInt(i, ids.get(current));
                    i++;
                }
                ps.setInt(i, item.getItemId());
                ps.setInt(i + 1, mapleInventoryType.getType());
                ps.setInt(i + 2, item.getPosition());
                ps.setInt(i + 3, item.getQuantity());
                ps.setString(i + 4, item.getOwner());
                ps.setInt(i + 5, item.getSN());
                ps.setLong(i + 6, item.getExpiration());
                ps.setByte(i + 7, item.getFlag());
                ps.setByte(i + 8, (byte) value);
                ps.setString(i + 9, item.getGiftFrom());
                ps.executeUpdate();

                if (isEquipInventory(mapleInventoryType)) {
                    ResultSet rs = ps.getGeneratedKeys();

                    if (!rs.next()) {
                        throw new RuntimeException("Inserting item failed.");
                    }

                    pse.setInt(1, rs.getInt(1));
                    rs.close();

                    setEquipInsertParameters(pse, (IEquip) item);
                    pse.executeUpdate();
                }
            }
            pse.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("Error while saving items", ex);
        }
    }

    private static boolean isEquipInventory(MapleInventoryType mapleInventoryType) {
        return mapleInventoryType.equals(MapleInventoryType.EQUIP)
                || mapleInventoryType.equals(MapleInventoryType.EQUIPPED);
    }

    private static void setEquipInsertParameters(PreparedStatement pse, IEquip equip) throws SQLException {
        pse.setInt(2, equip.getUpgradeSlots());
        pse.setInt(3, equip.getLevel());
        pse.setInt(4, equip.getStr());
        pse.setInt(5, equip.getDex());
        pse.setInt(6, equip.getInt());
        pse.setInt(7, equip.getLuk());
        pse.setInt(8, equip.getHp());
        pse.setInt(9, equip.getMp());
        pse.setInt(10, equip.getWatk());
        pse.setInt(11, equip.getMatk());
        pse.setInt(12, equip.getWdef());
        pse.setInt(13, equip.getMdef());
        pse.setInt(14, equip.getAcc());
        pse.setInt(15, equip.getAvoid());
        pse.setInt(16, equip.getHands());
        pse.setInt(17, equip.getSpeed());
        pse.setInt(18, equip.getJump());
        pse.setInt(19, equip.getViciousHammer());
        pse.setInt(20, equip.getItemEXP());
        pse.setInt(21, equip.getDurability());
        pse.setByte(22, equip.getEnhance());
        pse.setInt(23, equip.getPotential1());
        pse.setInt(24, equip.getPotential2());
        pse.setInt(25, equip.getPotential3());
        pse.setInt(26, equip.getHpR());
        pse.setInt(27, equip.getMpR());
    }

    private String createDeleteFromQuery() {
        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM `");
        query.append(tableInventoryItemsName);
        query.append("` WHERE `type` = ? AND (`");
        query.append(filterFields.get(0));
        query.append("` = ?");
        if (filterFields.size() > 1) {
            for (int i = 1; i < filterFields.size(); i++) {
                query.append(" OR `");
                query.append(filterFields.get(i));
                query.append("` = ?");
            }
        }
        query.append(")");
        return query.toString();
    }

    private String createInsertIntoTableEquipQuery() {
        return "INSERT INTO "
                + tableEquipName
                + " VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private StringBuilder createInsertInventoryItemsQuery() {
        StringBuilder insertIntoQuery = new StringBuilder("INSERT INTO `");
        insertIntoQuery.append(tableInventoryItemsName);
        insertIntoQuery.append("` (");
        for (String g : filterFields) {
            insertIntoQuery.append(g);
            insertIntoQuery.append(", ");
        }
        insertIntoQuery.append("itemid, inventorytype, position, quantity, owner, uniqueid, expiredate, flag,"
                + " `type`, sender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
        for (String g : filterFields) {
            insertIntoQuery.append(", ?");
        }
        insertIntoQuery.append(")");
        return insertIntoQuery;
    }

    private String createSelectAllQuery() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `");
        query.append(tableInventoryItemsName);
        query.append("` LEFT JOIN `");
        query.append(tableEquipName);
        query.append("` USING(`inventoryitemid`) WHERE `type` = ?");
        for (String g : filterFields) {
            query.append(" AND `");
            query.append(g);
            query.append("` = ?");
        }

        return query.toString();
    }

    private static Item mapItemFromResultSet(ResultSet rs) throws SQLException {
        Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getByte("flag"));
        item.setSN(rs.getInt("uniqueid"));
        item.setOwner(rs.getString("owner"));
        item.setInventoryId(rs.getLong("inventoryitemid"));
        item.setExpiration(rs.getLong("expiredate"));
        item.setGiftFrom(rs.getString("sender"));
        if (GameConstants.isPet(item.getItemId())) {
            if (item.getSN() > -1) {
                MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getSN(), item.getPosition());
                if (pet != null) {
                    item.setPet(pet);
                }
            } else {
                final int new_unique = MapleInventoryIdentifier.getInstance();
                item.setSN(new_unique);
                item.setPet(MaplePet.createPet(item.getItemId(), new_unique));
            }
        }
        return item;
    }

    private static Equip mapEquipFromResultSet(ResultSet rs, MapleInventoryType mit) throws SQLException {
        Equip equip =
                new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getByte("flag"));

        equip.setQuantity((short) 1);
        equip.setInventoryId(rs.getLong("inventoryitemid"));
        equip.setOwner(rs.getString("owner"));
        equip.setExpiration(rs.getLong("expiredate"));
        equip.setUpgradeSlots(rs.getByte("upgradeslots"));
        equip.setLevel(rs.getByte("level"));
        equip.setStr(rs.getShort("str"));
        equip.setDex(rs.getShort("dex"));
        equip.setInt(rs.getShort("int"));
        equip.setLuk(rs.getShort("luk"));
        equip.setHp(rs.getShort("hp"));
        equip.setMp(rs.getShort("mp"));
        equip.setWatk(rs.getShort("watk"));
        equip.setMatk(rs.getShort("matk"));
        equip.setWdef(rs.getShort("wdef"));
        equip.setMdef(rs.getShort("mdef"));
        equip.setAcc(rs.getShort("acc"));
        equip.setAvoid(rs.getShort("avoid"));
        equip.setHands(rs.getShort("hands"));
        equip.setSpeed(rs.getShort("speed"));
        equip.setJump(rs.getShort("jump"));
        equip.setViciousHammer(rs.getByte("ViciousHammer"));
        equip.setItemEXP(rs.getInt("itemEXP"));
        equip.setDurability(rs.getInt("durability"));
        equip.setEnhance(rs.getByte("enhance"));
        equip.setPotential1(rs.getShort("potential1"));
        equip.setPotential2(rs.getShort("potential2"));
        equip.setPotential3(rs.getShort("potential3"));
        equip.setHpR(rs.getShort("hpR"));
        equip.setMpR(rs.getShort("mpR"));
        equip.setGiftFrom(rs.getString("sender"));
        if (equip.getSN() > -1) {
            if (GameConstants.isEffectRing(rs.getInt("itemid"))) {
                MapleRing ring = MapleRing.loadFromDb(equip.getSN(), mit.equals(MapleInventoryType.EQUIPPED));
                if (ring != null) {
                    equip.setRing(ring);
                }
            }
        }
        return equip;
    }
}
