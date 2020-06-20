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

package server.maps;

import client.MapleCharacter;
import client.MapleQuestStatus;
import handling.channel.ChannelServer;
import handling.world.party.MaplePartyCharacter;
import server.Randomizer;
import server.Timer.MapTimer;
import server.life.MapleLifeFactory;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

public class Event_PyramidSubway {

  private int kill = 0, cool = 0, miss = 0, skill = 0, energybar = 100;
  private boolean broaded = false;
  private ScheduledFuture<?> energyBarDecrease, timerSchedule, yetiSchedule;
  //type: -1 = subway, 0-3 = difficulty of nett's pyramid.
  private Difficulty type;
  private long startTime;

  public enum Difficulty {
    EASY(0),
    NORMAL(1),
    HARD(2),
    HELL(3),
    OTHER(-1);

    private int type;

    Difficulty(int type) {
      this.type = type;
    }

    public int getType() {
      return type;
    }


    public static Difficulty fromInt(int i) {
      for (Difficulty d : Difficulty.values()) {
        if (d.type == i) {
          return d;
        }
      }
      System.out.println("Not expected difficulty value");
      return Difficulty.EASY;
    }

  }


  public Event_PyramidSubway(final MapleCharacter c) {
    resetStartTime();
    final int mapid = c.getMapId();
    if (mapid / 10000 == 91032) {
      type = Difficulty.OTHER;
    } else {
      type = Difficulty.fromInt(mapid % 10000 / 1000);
    }
    System.out.println("Type: " + type);
    if (c.getParty() == null || c.getParty().getLeader().equals(new MaplePartyCharacter(c))) {
      commenceTimerNextMap(c, 1);
      energyBarDecrease = MapTimer.getInstance().register(new Runnable() {

        public void run() {
          energybar -= (c.getParty() != null && c.getParty().getMembers().size() > 1 ? 10 : 5) * calculateTimingVariable();
          broadcastUpdate(c);
          if (broaded) {
            c.getMap().respawn(true);
          } else {
            broaded = true;
          }
          if (energybar <= 0) { //why
            fail(c);
          }
        }
      }, 1000);
    }
  }

  public final void fullUpdate(final MapleCharacter c, final int stage) {
    broadcastEnergy(c, "massacre_party", c.getParty() == null ? 0 : c.getParty().getMembers().size()); //huh
    broadcastEnergy(c, "massacre_miss", miss);
    broadcastEnergy(c, "massacre_cool", cool);
    broadcastEnergy(c, "massacre_skill", skill);
    broadcastEnergy(c, "massacre_laststage", stage - 1);
    broadcastEnergy(c, "massacre_hit", kill);
    broadcastUpdate(c);
  }

  public final void commenceTimerNextMap(final MapleCharacter c, final int stage) {
    if (timerSchedule != null) {
      timerSchedule.cancel(false);
      timerSchedule = null;
    }
    if (yetiSchedule != null) {
      yetiSchedule.cancel(false);
      yetiSchedule = null;
    }
    final MapleMap ourMap = c.getMap();
    final int time = (type == Difficulty.OTHER ? 180 : (stage == 1 ? 240 : 300)) - 1;
    if (c.getParty() != null && c.getParty().getMembers().size() > 1) {
      for (MaplePartyCharacter mpc : c.getParty().getMembers()) {
        final MapleCharacter chr = ourMap.getCharacterById(mpc.getId());
        if (chr != null) {
          chr.getClient().getSession().write(MaplePacketCreator.getClock(time));
          chr.getClient().getSession().write(MaplePacketCreator.showEffect("killing/first/number/" + stage));
          chr.getClient().getSession().write(MaplePacketCreator.showEffect("killing/first/stage"));
          chr.getClient().getSession().write(MaplePacketCreator.showEffect("killing/first/start"));
          fullUpdate(chr, stage);
        }
      }
    } else {
      c.getClient().getSession().write(MaplePacketCreator.getClock(time));
      c.getClient().getSession().write(MaplePacketCreator.showEffect("killing/first/number/" + stage));
      c.getClient().getSession().write(MaplePacketCreator.showEffect("killing/first/stage"));
      c.getClient().getSession().write(MaplePacketCreator.showEffect("killing/first/start"));
      fullUpdate(c, stage);
    }
    if (type != Difficulty.OTHER && (stage == 4 || stage == 5)) { //yetis. temporary
      final Point pos = c.getPosition();
      final MapleMap map = c.getMap();
      yetiSchedule = MapTimer.getInstance().register(new Runnable() {

        public void run() {
          if (map.countMonsterById(9300021) <= (stage == 4 ? 1 : 2)) {
            map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300021), new Point(pos));
          }
        }
      }, 10000L);
    }
    timerSchedule = MapTimer.getInstance().schedule(new Runnable() {

      public void run() {
        boolean ret = false;
        if (type == Difficulty.OTHER) {
          ret = warpNextMap_Subway(c);
        } else {
          ret = warpNextMap_Pyramid(c, type);
        }
        if (!ret) {
          fail(c);
        }
      }
    }, time * 1000L);
  }

  public double calculateTimingVariable() {
    long now = System.currentTimeMillis();
    long numberOfPassedSeconds = (now - startTime) / 1000;
    if (numberOfPassedSeconds < 60) {
      return 1;
    } else if (numberOfPassedSeconds < 120) {
      return 1.5;
    } else if (numberOfPassedSeconds < 180) {
      return 2.5;
    } else {
      return 3.5;
    }
  }

  public int getMaxEneryBar() {
    return 200 * Math.max(type.getType(), 1);
  }

  public final void onKill(final MapleCharacter c) {
    kill++;
    if (Randomizer.nextInt(100) < 5) { //monster properties coolDamage and coolDamageProb determine this, will code later
      cool++;
      broadcastEnergy(c, "massacre_cool", cool);
    }
    energybar += 5;
    System.out.println("Energy bar: " + energybar);
    if (energybar > getMaxEneryBar()) {
      energybar = getMaxEneryBar(); //rofl
    }
    if (type != Difficulty.OTHER) {
      for (int i = 5; i >= 1; i--) {
        if ((kill + cool) % (i * 100) == 0 && Randomizer.nextInt(100) < 50) {
          broadcastEffect(c, "killing/yeti" + (i - 1));
          break;
        }
      }
      //i dont want to give buffs as they could smuggle it
      if ((kill + cool) % 500 == 0) {
        skill++;
        broadcastEnergy(c, "massacre_skill", skill);
      }
      if (new Random().nextInt(100) == 5) {
        onMiss(c);
      }
    }

    broadcastUpdate(c);
    broadcastEnergy(c, "massacre_hit", kill);
  }

  public final void onMiss(final MapleCharacter c) {
    miss++;
    energybar -= 5;
    broadcastUpdate(c);
    broadcastEnergy(c, "massacre_miss", miss);
  }

  public final boolean onSkillUse(final MapleCharacter c) {
    if (skill > 0 && type != Difficulty.OTHER) {
      skill--;
      broadcastEnergy(c, "massacre_skill", skill);
      return true;
    }
    return false;
  }

  public final void onChangeMap(final MapleCharacter c, final int newmapid) {
    resetStartTime();
    if ((newmapid == 910330001 && type == Difficulty.OTHER) || (newmapid == 926020001 + type.getType() && type != Difficulty.OTHER)) {
      succeed(c);
    } else {
      if (type == Difficulty.OTHER && (newmapid < 910320100 || newmapid > 910320304)) {
        dispose(c);
        return;
      } else if (type != Difficulty.OTHER && (newmapid < 926010100 || newmapid > 926013504)) {
        dispose(c);
        return;
      } else if (c.getParty() == null || c.getParty().getLeader().equals(new MaplePartyCharacter(c))) {
        energybar = 100;
        commenceTimerNextMap(c, newmapid % 1000 / 100);
      }
    }
  }

  private void resetStartTime() {
    startTime = System.currentTimeMillis();
  }

  public final void succeed(final MapleCharacter c) {
    final MapleQuestStatus record = c.getQuestNAdd(MapleQuest.getInstance(type == Difficulty.OTHER ? 7662 : 7760));
    String data = record.getCustomData();
    if (data == null) {
      record.setCustomData("0");
      data = record.getCustomData();
    }
    final int mons = Integer.parseInt(data);
    final int tk = kill + cool;
    record.setCustomData(String.valueOf(mons + tk));
    byte rank = 4;
    if (type == Difficulty.OTHER) {
      if (tk >= 2000) {
        rank = 0;
      } else if (tk >= 1500 && tk <= 1999) {
        rank = 1;
      } else if (tk >= 1000 && tk <= 1499) {
        rank = 2;
      } else if (tk >= 500 && tk <= 999) {
        rank = 3;
      }
    } else {
      if (tk >= 3000) {
        rank = 0;
      } else if (tk >= 2000 && tk <= 2999) {
        rank = 1;
      } else if (tk >= 1500 && tk <= 1999) {
        rank = 2;
      } else if (tk >= 500 && tk <= 1499) {
        rank = 3;
      }
    }


    int pt = 0;
    switch (type) {
      case EASY:
        switch (rank) {
          case 0:
            pt = 60500;
            break;
          case 1:
            pt = 55000;
            break;
          case 2:
            pt = 46750;
            break;
          case 3:
            pt = 22000;
            break;
        }
        break;
      case NORMAL:
        switch (rank) {
          case 0:
            pt = 66000;
            break;
          case 1:
            pt = 60000;
            break;
          case 2:
            pt = 51750;
            break;
          case 3:
            pt = 24000;
            break;
        }
        break;
      case HARD:
        switch (rank) {
          case 0:
            pt = 71500;
            break;
          case 1:
            pt = 65000;
            break;
          case 2:
            pt = 55250;
            break;
          case 3:
            pt = 26000;
            break;
        }
        break;
      case HELL:
        switch (rank) {
          case 0:
            pt = 77000;
            break;
          case 1:
            pt = 70000;
            break;
          case 2:
            pt = 59500;
            break;
          case 3:
            pt = 28000;
            break;
        }
        break;
      default:
        switch (rank) {
          case 0:
            pt = 22000;
            break;
          case 1:
            pt = 17000;
            break;
          case 2:
            pt = 10750;
            break;
          case 3:
            pt = 7000;
            break;
        }
        break;
    }
    int exp = 0;
    if (rank < 4) {
      exp = (((kill * 2) + (cool * 10)) + pt) * c.getClient().getChannelServer().getExpRate();
      c.gainExp(exp, true, false, false);
    }
    c.getClient().getSession().write(MaplePacketCreator.showEffect("killing/clear"));
    c.getClient().getSession().write(MaplePacketCreator.sendPyramidResult(rank, exp));
    dispose(c);
  }

  public final void fail(final MapleCharacter c) {
    final MapleMap map;
    if (type == Difficulty.OTHER) {
      map = c.getClient().getChannelServer().getMapFactory().getMap(910320001);
    } else {
      map = c.getClient().getChannelServer().getMapFactory().getMap(926010001 + type.getType());
    }
    changeMap(c, map, 1, 200, 2);
    dispose(c);
  }

  public final void dispose(final MapleCharacter c) {
    final boolean lead = energyBarDecrease != null && timerSchedule != null;
    if (energyBarDecrease != null) {
      energyBarDecrease.cancel(false);
      energyBarDecrease = null;
    }
    if (timerSchedule != null) {
      timerSchedule.cancel(false);
      timerSchedule = null;
    }
    if (yetiSchedule != null) {
      yetiSchedule.cancel(false);
      yetiSchedule = null;
    }
    if (c.getParty() != null && lead && c.getParty().getMembers().size() > 1) {
      fail(c);
      return;
    }
    c.setPyramidSubway(null);
  }

  public final void broadcastUpdate(final MapleCharacter c) {
    final MapleMap map = c.getMap();
    if (c.getParty() != null && c.getParty().getMembers().size() > 1) {
      for (MaplePartyCharacter mpc : c.getParty().getMembers()) {
        final MapleCharacter chr = map.getCharacterById(mpc.getId());
        if (chr != null) {
          chr.getClient().getSession().write(MaplePacketCreator.sendPyramidUpdate(energybar));
        }
      }
    } else {
      c.getClient().getSession().write(MaplePacketCreator.sendPyramidUpdate(energybar));
    }
  }

  public final void broadcastEffect(final MapleCharacter c, final String effect) {
    c.getClient().getSession().write(MaplePacketCreator.showEffect(effect));
  }

  public final void broadcastEnergy(final MapleCharacter c, final String type, final int amount) {
    c.getClient().getSession().write(MaplePacketCreator.sendPyramidEnergy(type, String.valueOf(amount)));
  }

  public static boolean warpStartSubway(final MapleCharacter c) {
    final int mapid = 910320100;

    final ChannelServer ch = c.getClient().getChannelServer();
    for (int i = 0; i < 5; i++) {
      final MapleMap map = ch.getMapFactory().getMap(mapid + i);
      if (map.getCharactersSize() == 0) {
        clearMap(map, false);
        changeMap(c, map, 25, 30);
        return true;
      }
    }
    return false;
  }

  public static boolean warpBonusSubway(final MapleCharacter c) {
    final int mapid = 910320010;

    final ChannelServer ch = c.getClient().getChannelServer();
    for (int i = 0; i < 20; i++) {
      final MapleMap map = ch.getMapFactory().getMap(mapid + i);
      if (map.getCharactersSize() == 0) {
        clearMap(map, false);
        c.changeMap(map, map.getPortal(0));//solo
        return true;
      }
    }
    return false;
  }

  public static boolean warpNextMap_Subway(final MapleCharacter c) {
    final int currentmap = c.getMapId();
    final int thisStage = (currentmap - 910320100) / 100;

    MapleMap map = c.getMap();
    clearMap(map, true);
    final ChannelServer ch = c.getClient().getChannelServer();
    if (thisStage >= 2) {
      map = ch.getMapFactory().getMap(910330001);
      changeMap(c, map, 1, 200, 1);
      return true;
    }
    final int nextmapid = 910320100 + ((thisStage + 1) * 100);
    for (int i = 0; i < 5; i++) {
      map = ch.getMapFactory().getMap(nextmapid + i);
      if (map.getCharactersSize() == 0) {
        clearMap(map, false);
        changeMap(c, map, 1, 200, 1); //any level because they could level
        return true;
      }
    }
    return false;
  }

  public static boolean warpStartPyramid(final MapleCharacter c, final int difficulty) {
    final int mapid = 926010100 + (difficulty * 1000);
    int minLevel = 40, maxLevel = 60;
    switch (difficulty) {
      case 1:
        minLevel = 45;
        break;
      case 2:
        minLevel = 50;
        break;
      case 3:
        minLevel = 61;
        maxLevel = 200;
        break;
    }
    final ChannelServer ch = c.getClient().getChannelServer();
    for (int i = 0; i < 5; i++) {
      final MapleMap map = ch.getMapFactory().getMap(mapid + i);
      if (map.getCharactersSize() == 0) {
        clearMap(map, false);
        changeMap(c, map, minLevel, maxLevel);
        return true;
      }
    }
    return false;
  }

  public static boolean warpBonusPyramid(final MapleCharacter c, final int difficulty) {
    final int mapid = 926010010 + (difficulty * 20);

    final ChannelServer ch = c.getClient().getChannelServer();
    for (int i = 0; i < 20; i++) {
      final MapleMap map = ch.getMapFactory().getMap(mapid + i);
      if (map.getCharactersSize() == 0) {
        clearMap(map, false);
        c.changeMap(map, map.getPortal(0));//solo
        return true;
      }
    }
    return false;
  }

  public static boolean warpNextMap_Pyramid(final MapleCharacter c, Difficulty difficulty) {
    final int currentmap = c.getMapId();
    final int thisStage = (currentmap - (926010100 + (difficulty.getType() * 1000))) / 100;

    MapleMap map = c.getMap();
    clearMap(map, true);
    final ChannelServer ch = c.getClient().getChannelServer();
    if (thisStage >= 4) {
      map = ch.getMapFactory().getMap(926020001 + difficulty.getType());
      changeMap(c, map, 1, 200, 1);
      return true;
    }
    final int nextmapid = 926010100 + ((thisStage + 1) * 100) + (difficulty.getType() * 1000);
    for (int i = 0; i < 5; i++) {
      map = ch.getMapFactory().getMap(nextmapid + i);
      if (map.getCharactersSize() == 0) {
        clearMap(map, false);
        changeMap(c, map, 1, 200, 1); //any level because they could level
        return true;
      }
    }
    return false;
  }

  private static final void changeMap(final MapleCharacter c, final MapleMap map, final int minLevel, final int maxLevel) {
    changeMap(c, map, minLevel, maxLevel, 0);
  }

  private static final void changeMap(final MapleCharacter c, final MapleMap map, final int minLevel, final int maxLevel, final int clear) {
    final MapleMap oldMap = c.getMap();
    if (c.getParty() != null && c.getParty().getMembers().size() > 1) {
      for (MaplePartyCharacter mpc : c.getParty().getMembers()) {
        final MapleCharacter chr = oldMap.getCharacterById(mpc.getId());
        if (chr != null && chr.getId() != c.getId() && chr.getLevel() >= minLevel && chr.getLevel() <= maxLevel) {
          if (clear == 1) {
            chr.getClient().getSession().write(MaplePacketCreator.showEffect("killing/clear"));
          } else if (clear == 2) {
            chr.getClient().getSession().write(MaplePacketCreator.showEffect("killing/fail"));
          }
          chr.changeMap(map, map.getPortal(0));
        }
      }
    }
    if (clear == 1) {
      c.getClient().getSession().write(MaplePacketCreator.showEffect("killing/clear"));
    } else if (clear == 2) {
      c.getClient().getSession().write(MaplePacketCreator.showEffect("killing/fail"));
    }
    c.changeMap(map, map.getPortal(0));
  }

  private static final void clearMap(final MapleMap map, final boolean check) {
    if (check && map.getCharactersSize() > 0) {
      return;
    }
    map.resetFully(false);
  }
}
