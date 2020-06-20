/*
 * This file is part of the OdinMS MapleStory Private Server
 * Copyright (C) 2011 Patrick Huy and Matthias Butz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package handling.world.expedition;

public enum ExpeditionType {

  Easy_Balrog(6, 2000, 50, 70),
  Normal_Balrog(15, 2001, 50, 200),
  Zakum(30, 2002, 50, 200),
  Horntail(30, 2003, 80, 200),
  Chaos_Zakum(30, 2005, 100, 200),
  ChaosHT(30, 2006, 110, 200),
  Pink_Bean(30, 2004, 140, 200);
  public int maxMembers, maxParty, exped, minLevel, maxLevel;

  private ExpeditionType(int maxMembers, int exped, int minLevel, int maxLevel) {
    this.maxMembers = maxMembers;
    this.exped = exped;
    this.maxParty = (maxMembers / 2 + (maxMembers % 2 > 0 ? 1 : 0));
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
  }

  public static ExpeditionType getById(int id) {
    for (ExpeditionType pst : values()) {
      if (pst.exped == id) {
        return pst;
      }
    }
    return null;
  }
}
