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

package client;

import constants.GameConstants;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleStatEffect;
import server.life.Element;

import java.util.ArrayList;
import java.util.List;

public class Skill implements ISkill {

  //public static final int[] skills = new int[]{4311003, 4321000, 4331002, 4331005, 4341004, 4341007};
  private String name = "";
  private final List<MapleStatEffect> effects = new ArrayList<MapleStatEffect>();
  private Element element;
  private byte level;
  private int id, animationTime, requiredSkill, masterLevel;
  private boolean action, invisible, chargeskill, timeLimited;

  public Skill(final int id) {
    super();
    this.id = id;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  public static final Skill loadFromData(final int id, final MapleData data) {
    Skill ret = new Skill(id);

    boolean isBuff = false;
    final int skillType = MapleDataTool.getInt("skillType", data, -1);
    final String elem = MapleDataTool.getString("elemAttr", data, null);
    if (elem != null) {
      ret.element = Element.getFromChar(elem.charAt(0));
    } else {
      ret.element = Element.NEUTRAL;
    }
    ret.invisible = MapleDataTool.getInt("invisible", data, 0) > 0;
    ret.timeLimited = MapleDataTool.getInt("timeLimited", data, 0) > 0;
    ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);
    final MapleData effect = data.getChildByPath("effect");
    if (skillType != -1) {
      if (skillType == 2) {
        isBuff = true;
      }
    } else {
      final MapleData action_ = data.getChildByPath("action");
      final MapleData hit = data.getChildByPath("hit");
      final MapleData ball = data.getChildByPath("ball");

      boolean action = false;
      if (action_ == null) {
        if (data.getChildByPath("prepare/action") != null) {
          action = true;
        } else {
          switch (id) {
            case 5201001:
            case 5221009:
            case 4221001:
            case 4321001:
            case 4321000:
            case 4331001: //o_o
            case 3101005: //or is this really hack
              action = true;
              break;
          }
        }
      } else {
        action = true;
      }
      ret.action = action;
      isBuff = effect != null && hit == null && ball == null;
      isBuff |= action_ != null && MapleDataTool.getString("0", action_, "").equals("alert2");
      switch (id) {
        case 2301002: // heal is alert2 but not overtime...
        case 2111003: // poison mist
        case 12111005: // Flame Gear
        case 2111002: // explosion
        case 4211001: // chakra
        case 2121001: // Big bang
        case 2221001: // Big bang
        case 2321001: // Big bang
          isBuff = false;
          break;
        case 1004: // monster riding
        case 10001004:
        case 20001004:
        case 20011004:
        case 30001004:
        case 1026: //Soaring
        case 10001026:
        case 20001026:
        case 20011026:
        case 30001026:
        case 1111002: // combo
        case 4211003: // pickpocket
        case 4111001: // mesoup
        case 15111002: // Super Transformation
        case 5111005: // Transformation
        case 5121003: // Super Transformation
        case 13111005: // Alabtross
        case 21000000: // Aran Combo
        case 21101003: // Body Pressure
        case 5211001: // Pirate octopus summon
        case 5211002:
        case 5220002: // wrath of the octopi
        case 5001005: //dash
        case 15001003:
        case 5211006: //homing beacon
        case 5220011: //bullseye
        case 5110001: //energy charge
        case 15100004:
        case 5121009: //speed infusion
        case 15111005:

        case 22121001: //element reset
        case 22131001: //magic shield
        case 22141002: //magic booster
        case 22151002: //killer wing
        case 22151003: //magic resist
        case 22171000: //maple warrior
        case 22171004: //hero will
        case 22181000: //onyx blessing
        case 22181003: //soul stone
          //case 22121000:
        case 22141003:
          //case 22151001:
          //case 22161002:
        case 4331003: //owl spirit
        case 15101006: //spark
        case 15111006: //spark
        case 4321000: //tornado spin
        case 1320009: //beholder's buff.. passive
        case 35120000:
        case 35001002: //TEMP. mech
        case 9001004: // hide
        case 9101001:
        case 9101002:
        case 9101003:
        case 9101004:
        case 9101006:
        case 9101008:
        case 4341002:

        case 32001003: //dark aura
        case 32120000:
        case 32101002: //blue aura
        case 32110000:
        case 32101003: //yellow aura
        case 32120001:
        case 35101007: //perfect armor
        case 35121006: //satellite safety
        case 35001001: //flame
        case 35101009:
        case 35111007: //TEMP
        case 35121005: //missile
        case 35121013:
          //case 35111004: //siege
        case 35101002: //TEMP
        case 33111003: //puppet ?
        case 1211009:
        case 1111007:
        case 1311007: //magic,armor,atk crash
          isBuff = true;
          break;
      }
    }
    ret.chargeskill = data.getChildByPath("keydown") != null;


    if (data.getChildByPath("level") != null) {
      for (final MapleData level : data.getChildByPath("level")) {
        ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff, Byte.parseByte(level.getName())));
      }
    } else {
      final MapleData c = data.getChildByPath("common");
      if (c != null) {
        int mlevel = MapleDataTool.getIntConvert("maxLevel", c);
        for (byte i = 1; i <= mlevel; i++) {
          ret.effects.add(MapleStatEffect.loadSkillEffectFromData(c, id, isBuff, i));
        }
      }
    }

    final MapleData reqDataRoot = data.getChildByPath("req");
    if (reqDataRoot != null) {
      for (final MapleData reqData : reqDataRoot.getChildren()) {
        ret.requiredSkill = Integer.parseInt(reqData.getName());
        ret.level = (byte) MapleDataTool.getInt(reqData, 1);
      }
    }
    ret.animationTime = 0;
    if (effect != null) {
      for (final MapleData effectEntry : effect) {
        ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
      }
    }
    return ret;
  }

  @Override
  public MapleStatEffect getEffect(final int level) {
    if (effects.size() < level) {
      if (effects.size() > 0) { //incAllskill
        return effects.get(effects.size() - 1);
      }
      return null;
    } else if (level <= 0) {
      return effects.get(0);
    }
    return effects.get(level - 1);
  }

  @Override
  public boolean getAction() {
    return action;
  }

  @Override
  public boolean isChargeSkill() {
    return chargeskill;
  }

  @Override
  public boolean isInvisible() {
    return invisible;
  }

  @Override
  public boolean hasRequiredSkill() {
    return level > 0;
  }

  @Override
  public int getRequiredSkillLevel() {
    return level;
  }

  @Override
  public int getRequiredSkillId() {
    return requiredSkill;
  }

  @Override
  public byte getMaxLevel() {
    return (byte) effects.size();
  }

  @Override
  public boolean canBeLearnedBy(int job) {
    int jid = job;
    int skillForJob = id / 10000;
    if (skillForJob == 2000) {
      return true;
    }
    if (skillForJob == 2001 && GameConstants.isEvan(job)) {
      return true; //special exception for evan -.-
    } else if (jid / 100 != skillForJob / 100) { // wrong job
      return false;
    } else if (jid / 1000 != skillForJob / 1000) { // wrong job
      return false;
    } else if (GameConstants.isAdventurer(skillForJob) && !GameConstants.isAdventurer(job)) {
      return false;
    } else if (GameConstants.isKOC(skillForJob) && !GameConstants.isKOC(job)) {
      return false;
    } else if (GameConstants.isAran(skillForJob) && !GameConstants.isAran(job)) {
      return false;
    } else if (GameConstants.isEvan(skillForJob) && !GameConstants.isEvan(job)) {
      return false;
    } else if (GameConstants.isResist(skillForJob) && !GameConstants.isResist(job)) {
      return false;
    } else if ((skillForJob / 10) % 10 > (jid / 10) % 10) { // wrong 2nd job
      return false;
    } else if (skillForJob % 10 > jid % 10) { // wrong 3rd/4th job
      return false;
    }
    return true;
  }

  @Override
  public boolean isTimeLimited() {
    return timeLimited;
  }

  @Override
  public boolean isFourthJob() {
    if (id / 10000 == 2312) {
      return true;
    }
    if ((getMaxLevel() <= 15 && !invisible && getMasterLevel() <= 0) || id == 3220010 || id == 3120011 || id == 33120010 || id == 32120009 || id == 5321006 || id == 21120011 || id == 22181004 || id == 4340010) {
      return false;
    }
    if (id / 10000 >= 2212 && id / 10000 < 3000) {
      return id / 10000 % 10 >= 7;
    }
    if (id / 10000 >= 430 && id / 10000 <= 434) {
      return (id / 10000 % 10 == 4) || (getMasterLevel() > 0);
    }
    return (id / 10000 % 10 == 2) && (id < 90000000) && (!isBeginnerSkill());
  }

  @Override
  public Element getElement() {
    return element;
  }

  @Override
  public int getAnimationTime() {
    return animationTime;
  }

  @Override
  public int getMasterLevel() {
    return masterLevel;
  }

  @Override
  public boolean isBeginnerSkill() {
    int jobId = id / 10000;
    return jobId == 0 || jobId == 1000 || jobId == 2000 || jobId == 2001 || jobId == 3000;
  }

  @Override
  public boolean hasMastery() {
    if (masterLevel > 0) {
      return true;
    }
    final int jobId = id / 10000;
    if (jobId < 430) {
      if (jobId % 10 == 2) {
        return true;
      }
    } else if (jobId > 440 && jobId < 2200) {
      if (jobId % 10 == 2) {
        return true;
      }
    } else if (jobId == 434) {
      return true;
    } else if (id == 22170001 || id == 22171003 || id == 22171004 || id == 22181002 || id == 22181003) {
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return "Skill [name=" + name + ", effects=" + effects + ", element=" + element + ", level=" + level + ", id="
        + id + ", animationTime=" + animationTime + ", requiredSkill=" + requiredSkill + ", masterLevel="
        + masterLevel + ", action=" + action + ", invisible=" + invisible + ", chargeskill=" + chargeskill
        + ", timeLimited=" + timeLimited + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Skill other = (Skill) obj;
    if (id != other.id) {
      return false;
    }
    return true;
  }

}
