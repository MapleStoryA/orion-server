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

package handling.channel.handler.utils;

import client.MapleCharacter;
import client.inventory.IItem;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import server.MerchItemPackage;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HiredMerchantHandlerUtils {


  public static final byte checkExistance(final int accid, final int charid) {
    Connection con = DatabaseConnection.getConnection();
    try {
      PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where accountid = ? OR characterid = ?");
      ps.setInt(1, accid);
      ps.setInt(2, charid);
      ResultSet rs = ps.executeQuery();


      if (rs.next()) {
        ps.close();
        rs.close();
        return 1;
      }
      rs.close();
      ps.close();
      return 0;
    } catch (SQLException se) {
      return -1;
    }
  }


  public static final boolean check(final MapleCharacter chr, final MerchItemPackage pack) {
    if (chr.getMeso() + pack.getMesos() < 0) {
      return false;
    }
    byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
    for (IItem item : pack.getItems()) {
      final MapleInventoryType invtype = GameConstants.getInventoryType(item.getItemId());
      if (invtype == MapleInventoryType.EQUIP) {
        eq++;
      } else if (invtype == MapleInventoryType.USE) {
        use++;
      } else if (invtype == MapleInventoryType.SETUP) {
        setup++;
      } else if (invtype == MapleInventoryType.ETC) {
        etc++;
      } else if (invtype == MapleInventoryType.CASH) {
        cash++;
      }
    }
    if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq || chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use || chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup || chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc || chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash) {
      return false;
    }
    return true;
  }

  public static final boolean deletePackage(final int charid, final int accid, final int packageid) {
    final Connection con = DatabaseConnection.getConnection();

    try {
      PreparedStatement ps = con.prepareStatement("DELETE from hiredmerch where characterid = ? OR accountid = ? OR packageid = ?");
      ps.setInt(1, charid);
      ps.setInt(2, accid);
      ps.setInt(3, packageid);
      ps.execute();
      ps.close();
      ItemLoader.HIRED_MERCHANT.saveItems(null, packageid, accid, charid);
      return true;
    } catch (SQLException e) {
      System.out.println("Error while deleting the package" + e.getMessage());
      return false;
    }
  }

  public static final MerchItemPackage loadItemFrom_Database(final int charid, final int accountid) {
    final Connection con = DatabaseConnection.getConnection();

    try {
      PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where characterid = ? OR accountid = ?");
      ps.setInt(1, charid);
      ps.setInt(2, accountid);

      ResultSet rs = ps.executeQuery();

      if (!rs.next()) {
        ps.close();
        rs.close();
        return null;
      }
      final int packageid = rs.getInt("PackageId");

      final MerchItemPackage pack = new MerchItemPackage();
      pack.setPackageid(packageid);
      pack.setMesos(rs.getInt("Mesos"));
      pack.setSentTime(rs.getLong("time"));
      ps.close();
      rs.close();

      Map<Integer, Pair<IItem, MapleInventoryType>> items = ItemLoader.HIRED_MERCHANT.loadItems(false, packageid, accountid, charid);
      if (items != null) {
        List<IItem> iters = new ArrayList<IItem>();
        for (Pair<IItem, MapleInventoryType> z : items.values()) {
          iters.add(z.left);
        }
        pack.setItems(iters);
      }


      return pack;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }
}
