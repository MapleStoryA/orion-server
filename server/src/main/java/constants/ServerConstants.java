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

package constants;

public class ServerConstants {

  public static final int ONE_DAY_ITEM = 5062000; //cube

  /*
   * Specifics which job gives an additional EXP to party
   * returns the percentage of EXP to increase
   */
  public static byte Class_Bonus_EXP(final int job) {
    switch (job) {
      case 3000: //whenever these arrive, they'll give bonus
      case 3200:
      case 3210:
      case 3211:
      case 3212:
      case 3300:
      case 3310:
      case 3311:
      case 3312:
      case 3500:
      case 3510:
      case 3511:
      case 3512:
        return 10;
    }
    return 0;
  }

  // Event Constants
  // Allows all mobs to drop EXP Item Card
  public static final boolean EXPItemDrop = false;
  // Bonus EXP every 3rd mob killed
  public static final boolean TRIPLE_TRIO = true;
  // Shop discount for potions
  public static final boolean SHOP_DISCOUNT = false;
  public static final float SHOP_DISCOUNT_PERCENT = 5f; // float = round up.
  //
  public static final boolean SPEED_QUIZ = true;
  // Default is 500. If the map contains > this amount, it will automatically clear drops
  public static final int MAX_ITEMS = 600;
  // End of Poll
  public static final short MAPLE_VERSION = 90;
  public static final String MAPLE_PATCH = "3";
  public static final boolean Use_Fixed_IV = false;
  public static final String WORLD_MESSAGE = "Welcome to Maple Story Global ";
  public static final String RECOMMENDED_MESSAGE = "We are still in Tespia testing! Report bugs on our forums.";
  //Faction Stuff
  public static final float FP_MULTIPLIER = 1.3f; // float = rounding the int


  public static int getRespawnRate(final int mapid) {
    return 1;
  }

  public static enum PlayerGMRank {

    NORMAL('@', 0),
    DONOR('!', 1),
    GM('!', 2),
    ADMIN('!', 3);
    private char commandPrefix;
    private int level;

    PlayerGMRank(char ch, int level) {
      commandPrefix = ch;
      this.level = level;
    }

    public char getCommandPrefix() {
      return commandPrefix;
    }

    public int getLevel() {
      return level;
    }
  }

  public static enum CommandType {

    NORMAL(0),
    TRADE(1);
    private int level;

    CommandType(int level) {
      this.level = level;
    }

    public int getType() {
      return level;
    }
  }


}
