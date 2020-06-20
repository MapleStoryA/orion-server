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

package server.life;

import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConstants;
import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapleMonsterInformationProvider {

  private static final MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();
  private final Map<Integer, List<MonsterDropEntry>> drops = new HashMap<>();
  private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<>();

  public static MapleMonsterInformationProvider getInstance() {
    return instance;
  }

  public final List<MonsterGlobalDropEntry> getGlobalDrop() {
    return globaldrops;
  }

  public final List<MonsterDropEntry> retrieveDrop(final int monsterId) {
    List<MonsterDropEntry> entries = drops.get(monsterId);
    if (entries == null) {
      return new ArrayList<>();
    }
    return entries;
  }


  public void load() {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      Connection con = DatabaseConnection.getConnection();

      ps = con.prepareStatement("SELECT * FROM `drop_data_global` WHERE chance > 0");
      rs = ps.executeQuery();
      while (rs.next()) {
        globaldrops.add(new MonsterGlobalDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("continent"), rs.getByte("dropType"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getInt("questid")));
      }
      rs.close();
      ps.close();
      if (ServerConstants.EXPItemDrop) {
        for (int i = 2022450; i <= 2022452; i++) {
          globaldrops.add(new MonsterGlobalDropEntry(i, 15000, -1, (byte) 0, 1, 1, (short) 0));
        }
      }

      ps = con.prepareStatement("SELECT `dropperid` FROM `drop_data`");
      List<Integer> mobIds = new ArrayList<>();
      rs = ps.executeQuery();
      while (rs.next()) {
        if (!mobIds.contains(Integer.valueOf(rs.getInt("dropperid")))) {
          try {
            loadDrop(rs.getInt("dropperid"));
            mobIds.add(Integer.valueOf(rs.getInt("dropperid")));
          } catch (Exception e) {
            //ignore since i'm using bb drops
          }
        }
      }
    } catch (SQLException ignore) {
      System.err.println("Error retrieving drop" + ignore);
    } finally {
      try {
        if (ps != null) {
          ps.close();
        }
        if (rs != null) {
          rs.close();
        }
      } catch (SQLException ignore) {
      }
    }
  }

  private void loadDrop(int monsterId) {
    ArrayList<MonsterDropEntry> ret = new ArrayList<>();

    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      MapleMonsterStats mons = MapleLifeFactory.getMonsterStats(monsterId);
      if (mons == null) {
        return;
      }
      ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM drop_data WHERE dropperid = ?");
      ps.setInt(1, monsterId);
      rs = ps.executeQuery();

      boolean doneMesos = false;
      while (rs.next()) {
        int itemid = rs.getInt("itemid");
        int chance = rs.getInt("chance");
        if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
          chance *= 10; //in GMS/SEA it was raised
        }
        ret.add(new MonsterDropEntry(itemid, chance,
                                     rs.getInt("minimum_quantity"),
                                     rs.getInt("maximum_quantity"),
                                     rs.getInt("questid"),
                                     rs.getInt("holdMaximum")));
        if (itemid == 0) {
          doneMesos = true;
        }
      }
      if (!doneMesos) {
        addMeso(mons, ret);
      }
    } catch (SQLException ignore) {
      System.err.println(monsterId + " is Id. Error retrieving normal drop" + ignore);
    } finally {
      try {
        if (ps != null) {
          ps.close();
        }
        if (rs != null) {
          rs.close();
        }
      } catch (SQLException ignore) {
      }
    }
    drops.put(monsterId, ret);
  }

  public void addMeso(MapleMonsterStats mons, ArrayList<MonsterDropEntry> ret) {
    double divided = mons.getLevel() < 100 ? 10.0D : mons.getLevel() < 10 ? mons.getLevel() : mons.getLevel() / 10.0D;
    int max = mons.isBoss() ? (mons.getLevel() * mons.getLevel()) : (mons.getLevel() * (int) Math.ceil(mons.getLevel() / divided));
    for (int i = 0; i < mons.dropsMeso(); i++) {
      ret.add(new MonsterDropEntry(0, mons.isBoss() ? 1000000 : 200000, (int) Math.floor(0.66D * max), max, (short) 0, -1));
    }
  }

  public final void clearDrops() {
    drops.clear();
    globaldrops.clear();
    load();
  }
}
