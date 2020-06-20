package server;

import client.*;
import client.inventory.IItem;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.skills.Evan;
import handling.channel.ChannelServer;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleCarnivalFactory.MCSkill;
import server.Timer.BuffTimer;
import server.life.MapleMonster;
import server.maps.*;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;

import java.awt.*;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class MapleStatEffect implements Serializable {

  private static final long serialVersionUID = 9179541993413738569L;
  private byte mastery, mhpR, mmpR, mobCount, attackCount, bulletCount;
  private short hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, mpCon, hpCon, damage, prop, ehp, emp, ewatk, ewdef, emdef, thaw;
  private double hpR, mpR;
  private int duration, sourceid, moveTo, x, y, z, itemCon, itemConNo, bulletConsume, moneyCon, cooldown, morphId = 0, expinc;
  private boolean overTime, skill, isPotion, partyBuff = true;
  private List<Pair<MapleBuffStat, Integer>> statups;
  private Map<MonsterStatus, Integer> monsterStatus;
  private Point lt, rb;
  private int expBuff, itemup, mesoup, cashup, berserk, illusion, booster, berserk2, cp, nuffSkill;
  private byte level;
  private List<Pair<Integer, Integer>> randomMorph;
  private List<MapleDisease> cureDebuffs;

  public static final MapleStatEffect loadSkillEffectFromData(final MapleData source, final int skillid, final boolean overtime, final byte level) {
    return loadFromData(source, skillid, true, overtime, level);
  }

  public static final MapleStatEffect loadItemEffectFromData(final MapleData source, final int itemid) {
    return loadFromData(source, itemid, false, false, (byte) 1);
  }

  private static final void addBuffStatPairToListIfNotZero(final List<Pair<MapleBuffStat, Integer>> list, final MapleBuffStat buffstat, final Integer val) {
    if (val.intValue() != 0) {
      list.add(new Pair<MapleBuffStat, Integer>(buffstat, val));
    }
  }

  private static MapleStatEffect loadFromData(final MapleData source, final int sourceid, final boolean skill, final boolean overTime, final byte level) {
    final MapleStatEffect ret = new MapleStatEffect();
    ret.sourceid = sourceid;
    ret.skill = skill;
    ret.level = level;
    if (source == null) {
      ret.isPotion = false;
      return ret;
    }
    ret.isPotion = true;
    ret.duration = StringUtil.Solve(MapleDataTool.getString("time", source, "-1"), level);
    ret.hp = (short) StringUtil.Solve(MapleDataTool.getString("hp", source, "0"), level);
    ret.hpR = MapleDataTool.getInt("hpR", source, 0) / 100.0;
    ret.mp = (short) StringUtil.Solve(MapleDataTool.getString("mp", source, "0"), level);
    ret.mpR = MapleDataTool.getInt("mpR", source, 0) / 100.0;
    ret.mhpR = (byte) StringUtil.Solve(MapleDataTool.getString("mhpR", source, "0"), level);
    ret.mmpR = (byte) StringUtil.Solve(MapleDataTool.getString("mmpR", source, "0"), level);
    ret.mpCon = (short) StringUtil.Solve(MapleDataTool.getString("mpCon", source, "0"), level);
    ret.hpCon = (short) StringUtil.Solve(MapleDataTool.getString("hpCon", source, "0"), level);
    ret.prop = (short) StringUtil.Solve(MapleDataTool.getString("prop", source, "100"), level);
    ret.cooldown = StringUtil.Solve(MapleDataTool.getString("cooltime", source, "0"), level);
    ret.expinc = StringUtil.Solve(MapleDataTool.getString("expinc", source, "0"), level);
    ret.morphId = StringUtil.Solve(MapleDataTool.getString("morph", source, "0"), level);
    ret.cp = StringUtil.Solve(MapleDataTool.getString("cp", source, "0"), level);
    ret.nuffSkill = StringUtil.Solve(MapleDataTool.getString("nuffSkill", source, "0"), level);
    ret.mobCount = (byte) StringUtil.Solve(MapleDataTool.getString("mobCount", source, "1"), level);
    ret.thaw = (short) MapleDataTool.getInt("thaw", source, 0);

    if (skill) {
      switch (sourceid) {
        case 1100002:
        case 1100003:
        case 1200002:
        case 1200003:
        case 1300002:
        case 1300003:
        case 3100001:
        case 3200001:
        case 11101002:
        case 13101002:
          ret.mobCount = 6;
          break;
      }
    }

    final MapleData randMorph = source.getChildByPath("morphRandom");
    if (randMorph != null) {
      ret.randomMorph = new ArrayList<>();
      for (MapleData data : randMorph.getChildren()) {
        final String propp = MapleDataTool.getString("prop", data, null);
        ret.randomMorph.add(new Pair<>(MapleDataTool.getInt("morph", data, 0), Integer.parseInt(propp == null ? "100" : propp)));
      }
    }

    if (!ret.skill && ret.duration > -1) {
      ret.overTime = true;
    } else {
      ret.duration *= 1000; // items have their times stored in ms, of course
      ret.overTime = overTime || ret.isMorph() || ret.isPirateMorph() || ret.isFinalAttack();
    }

    final ArrayList<Pair<MapleBuffStat, Integer>> statups = new ArrayList<Pair<MapleBuffStat, Integer>>();

    ret.mastery = (byte) StringUtil.Solve(MapleDataTool.getString("mastery", source, "0"), level);
    ret.watk = (short) StringUtil.Solve(MapleDataTool.getString("pad", source, "0"), level);
    ret.matk = (short) StringUtil.Solve(MapleDataTool.getString("mad", source, "0"), level);
    ret.mdef = (short) StringUtil.Solve(MapleDataTool.getString("mdd", source, "0"), level);
    ret.wdef = (short) StringUtil.Solve(MapleDataTool.getString("pdd", source, "0"), level);
    ret.ehp = (short) StringUtil.Solve(MapleDataTool.getString("emhp", source, "0"), level);
    ret.emp = (short) StringUtil.Solve(MapleDataTool.getString("emmp", source, "0"), level);
    ret.ewatk = (short) StringUtil.Solve(MapleDataTool.getString("epad", source, "0"), level);
    ret.ewdef = (short) StringUtil.Solve(MapleDataTool.getString("epdd", source, "0"), level);
    ret.emdef = (short) StringUtil.Solve(MapleDataTool.getString("emdd", source, "0"), level);
    ret.acc = (short) StringUtil.Solve(MapleDataTool.getString("acc", source, "0"), level);
    ret.avoid = (short) StringUtil.Solve(MapleDataTool.getString("eva", source, "0"), level);
    ret.speed = (short) StringUtil.Solve(MapleDataTool.getString("speed", source, "0"), level);
    ret.jump = (short) StringUtil.Solve(MapleDataTool.getString("jump", source, "0"), level);
    ret.expBuff = StringUtil.Solve(MapleDataTool.getString("expBuff", source, "0"), level);
    ret.cashup = StringUtil.Solve(MapleDataTool.getString("cashBuff", source, "0"), level);
    ret.itemup = StringUtil.Solve(MapleDataTool.getString("itemupbyitem", source, "0"), level);
    ret.mesoup = StringUtil.Solve(MapleDataTool.getString("mesoupbyitem", source, "0"), level);
    ret.berserk = StringUtil.Solve(MapleDataTool.getString("berserk", source, "0"), level);
    ret.berserk2 = StringUtil.Solve(MapleDataTool.getString("berserk2", source, "0"), level);
    ret.booster = StringUtil.Solve(MapleDataTool.getString("booster", source, "0"), level);
    ret.illusion = StringUtil.Solve(MapleDataTool.getString("illusion", source, "0"), level);

    List<MapleDisease> cure = new ArrayList<MapleDisease>(5);
    if (MapleDataTool.getInt("poison", source, 0) > 0) {
      cure.add(MapleDisease.POISON);
    }
    if (MapleDataTool.getInt("seal", source, 0) > 0) {
      cure.add(MapleDisease.SEAL);
    }
    if (MapleDataTool.getInt("darkness", source, 0) > 0) {
      cure.add(MapleDisease.DARKNESS);
    }
    if (MapleDataTool.getInt("weakness", source, 0) > 0) {
      cure.add(MapleDisease.WEAKEN);
    }
    if (MapleDataTool.getInt("curse", source, 0) > 0) {
      cure.add(MapleDisease.CURSE);
    }
    ret.cureDebuffs = cure;

    final MapleData ltd = source.getChildByPath("lt");
    if (ltd != null) {
      ret.lt = (Point) ltd.getData();
      ret.rb = (Point) source.getChildByPath("rb").getData();
    }

    ret.x = StringUtil.Solve(MapleDataTool.getString("x", source, "0"), level);
    ret.y = StringUtil.Solve(MapleDataTool.getString("y", source, "0"), level);
    ret.z = StringUtil.Solve(MapleDataTool.getString("z", source, "0"), level);
    ret.damage = (short) StringUtil.Solve(MapleDataTool.getString("damage", source, "100"), level);
    ret.attackCount = (byte) StringUtil.Solve(MapleDataTool.getString("attackCount", source, "1"), level);
    ret.bulletCount = (byte) StringUtil.Solve(MapleDataTool.getString("bulletCount", source, "1"), level);
    ret.bulletConsume = StringUtil.Solve(MapleDataTool.getString("bulletConsume", source, "0"), level);
    ret.moneyCon = StringUtil.Solve(MapleDataTool.getString("moneyCon", source, "0"), level);
    ret.itemCon = StringUtil.Solve(MapleDataTool.getString("itemCon", source, "0"), level);
    ret.itemConNo = StringUtil.Solve(MapleDataTool.getString("itemConNo", source, "0"), level);
    ret.moveTo = MapleDataTool.getInt("moveTo", source, -1);

    Map<MonsterStatus, Integer> monsterStatus = new EnumMap<MonsterStatus, Integer>(MonsterStatus.class);
    if (ret.overTime && ret.getSummonMovementType() == null) {
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.SPEED, Integer.valueOf(ret.speed));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MAXHP, (int) ret.mhpR);
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MAXMP, (int) ret.mmpR);
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.EXPRATE, Integer.valueOf(ret.expBuff)); // EXP
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACASH_RATE, Integer.valueOf(ret.cashup)); // custom
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.DROP_RATE, Integer.valueOf(ret.itemup * 200)); // defaults to 2x
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MESO_RATE, Integer.valueOf(ret.mesoup * 200)); // defaults to 2x
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.BERSERK_FURY, Integer.valueOf(ret.berserk2));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.PYRAMID_PQ, Integer.valueOf(ret.berserk));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.BOOSTER, Integer.valueOf(ret.booster));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ILLUSION, Integer.valueOf(ret.illusion));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.HP_LOSS_GUARD, Integer.valueOf(ret.thaw));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ENHANCED_WATK, Integer.valueOf(ret.ewatk));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ENHANCED_WDEF, Integer.valueOf(ret.ewdef));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ENHANCED_MDEF, Integer.valueOf(ret.emdef));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ENHANCED_MAXHP, Integer.valueOf(ret.ehp));
      addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ENHANCED_MAXMP, Integer.valueOf(ret.ehp));
    }
    if (skill) { // hack because we can't get from the datafile...
      switch (sourceid) {
        case 2001002: // magic guard
        case 12001001:
        case 22111001:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_GUARD, ret.x));
          break;
        case 2301003: // invincible
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INVINCIBLE, ret.x));
          break;
        case 35120000:
        case 35001002: //TEMP. mech
          ret.duration = 60 * 120 * 1000;
          break;
        case 9101004: // hide
          ret.duration = 60 * 120 * 1000;
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, ret.x));
          break;
        case 13101006: // Wind Walk
          break;
        case 4001003: // darksight
        case 14001003: // cygnus ds
        case 4330001:
        case 30001001: //resist beginner hide
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, ret.x));
          break;
        case 4211003: // pickpocket
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PICKPOCKET, ret.x));
          break;
        case 4211005: // mesoguard
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOGUARD, ret.x));
          break;
        case 4111001: // mesoup
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOUP, ret.x));
          break;
        case 4111002: // shadowpartner
        case 14111000: // cygnus
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, ret.x));
          break;
        case 11101002: // All Final attack
        case 13101002:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINALATTACK, ret.x));
          break;
        case 3101004: // soul arrow
        case 3201004:
        case 2311002: // mystic door - hacked buff icon
        case 13101003:
        case 33101003:
        case 8001:
        case 10008001:
        case 20008001:
        case 20018001:
        case 30008001:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, ret.x));
          break;
        case 1211006: // wk charges
        case 1211003:
        case 1211004:
        case 1211005:
        case 1211008:
        case 1211007:
        case 1221003:
        case 1221004:
        case 11111007:
        case 21111005:
        case 15101006:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WK_CHARGE, ret.x));
          break;
        case 12101005:
        case 22121001: // Elemental Reset
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ELEMENT_RESET, ret.x));
          break;
        case 3121008:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.CONCENTRATE, ret.x));
          break;
        case 5110001: // Energy Charge
        case 15100004:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ENERGY_CHARGE, 0));
          break;
        case 1101005: // booster
        case 1101004:
        case 1201005:
        case 1201004:
        case 1301005:
        case 1301004:
        case 3101002:
        case 3201002:
        case 4101003:
        case 4201002:
        case 2111005: // spell booster, do these work the same?
        case 2211005:
        case 5101006:
        case 5201003:
        case 11101001:
        case 12101004:
        case 13101001:
        case 14101002:
        case 15101002:
        case 21001003: // Aran - Pole Arm Booster
        case 22141002: // Magic Booster
        case 4301002:
        case 32101005:
        case 33001003:
        case 35101006:
        case 35001003: //TEMP.BOOSTER
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BOOSTER, ret.x));
          break;
        case 5121009:
        case 15111005:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPEED_INFUSION, ret.x));
          break;
        case 4321000: //tornado spin uses same buffstats
          ret.duration = 1000;
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_SPEED, 100 + ret.x));
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_JUMP, ret.y)); //always 0 but its there
          break;
        case 5001005: // Dash
        case 15001003:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_SPEED, ret.x));
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_JUMP, ret.y));
          break;
        case 1101007: // pguard
        case 1201007:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.POWERGUARD, ret.x));
          break;
        case 32111004: //conversion
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.CONVERSION, ret.x));
          break;
        case 1301007: // hyper body
        case 9101008:
        case 8003:
        case 10008003:
        case 20008003:
        case 20018003:
        case 30008003:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAXHP, ret.x));
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAXMP, ret.y));
          break;
        case Evan.RECOVERY:
        case 1001: // recovery
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.RECOVERY, ret.x));
          break;
        case 1111002: // combo
        case 11111001: // combo
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
          break;
        case 5211006: // Homing Beacon
        case 5220011: // Bullseye
        case 22151002: //killer wings
          ret.duration = 60 * 120000;
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOMING_BEACON, ret.x));
          break;
        case 1011: // Berserk fury
        case 10001011:
        case 20001011:
        case 20011011:
        case 30001011:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BERSERK_FURY, 1));
          break;
        case 1010:
        case 10001010:// Invincible Barrier
        case 20001010:
        case 20011010:
        case 30001010:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DIVINE_BODY, 1));
          break;
        case 1311006: //dragon roar
          ret.hpR = -ret.x / 100.0;
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DRAGON_ROAR, ret.y));
          break;
        case 4341007:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.THORNS, ret.x << 8 | ret.y));
          break;
        case 4341002:
          //ret.duration = 60 * 1000;
          ret.hpR = (-ret.x / 100.0D);
          statups.add(new Pair<>(MapleBuffStat.FINAL_CUT, Integer.valueOf(ret.y)));
          break;
        case 4331002:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_IMAGE, ret.x));
          break;
        case 4331003:
          statups.add(new Pair<>(MapleBuffStat.OWL_SPIRIT, Integer.valueOf(ret.y)));
          break;
        case 1311008: // dragon blood
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DRAGONBLOOD, ret.x));
          break;
        case 1121000: // maple warrior, all classes
        case 1221000:
        case 1321000:
        case 2121000:
        case 2221000:
        case 2321000:
        case 3121000:
        case 3221000:
        case 4121000:
        case 4221000:
        case 5121000:
        case 5221000:
        case 21121000: // Aran - Maple Warrior
        case 22171000:
        case 4341000:
        case 32121007:
        case 33121007:
        case 35121007:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAPLE_WARRIOR, ret.x));
          break;
        case 15111006: //spark
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPARK, ret.x));
          break;
        case 3121002: // sharp eyes bow master
        case 3221002: // sharp eyes marksmen
        case 33121004:
        case 8002:
        case 10008002:
        case 20008002:
        case 20018002:
        case 30008002:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHARP_EYES, ret.x << 8 | ret.y));
          break;
        case 22151003: //magic resistance
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_RESISTANCE, ret.x));
          break;
        case 21101003: // Body Pressure
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BODY_PRESSURE, ret.x));
          break;
        case 21000000: // Aran Combo
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ARAN_COMBO, 100));
          break;
        case 21100005: // Combo Drain
        case 32101004:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO_DRAIN, ret.x));
          break;
        case 21111001: // Smart Knockback
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SMART_KNOCKBACK, ret.x));
          break;
        case 22131001: //magic shield
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_SHIELD, ret.x));
          break;
        case 22181003: //soul stone
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOUL_STONE, 1));
          break;
        case 4001002: // disorder
        case 14001002: // cygnus disorder
          monsterStatus.put(MonsterStatus.WATK, ret.x);
          monsterStatus.put(MonsterStatus.WDEF, ret.y);
          break;
        case 5221009: // Mind Control
          monsterStatus.put(MonsterStatus.HYPNOTIZE, 1);
          break;
        case 1201006: // threaten
          monsterStatus.put(MonsterStatus.WATK, ret.x);
          monsterStatus.put(MonsterStatus.WDEF, ret.y);
          break;
        case 1211002: // charged blow
        case 1111008: // shout
        case 4211002: // assaulter
        case 3101005: // arrow bomb
        case 1111005: // coma: sword
        case 1111006: // coma: axe
        case 4221007: // boomerang step
        case 5101002: // Backspin Blow
        case 5101003: // Double Uppercut
        case 5121004: // Demolition
        case 5121005: // Snatch
        case 5121007: // Barrage
        case 5201004: // pirate blank shot
        case 4121008: // Ninja Storm
        case 22151001:
        case 4201004: //steal, new
        case 33101001:
        case 33101002:
        case 32111010:
        case 32121004:
        case 33111002:
        case 33121002:
        case 35101003:
        case 35111015:
        case 5111002: //energy blast
        case 15101005:
        case 4331005:
          monsterStatus.put(MonsterStatus.STUN, 1);
          break;
        case 4321002:
          monsterStatus.put(MonsterStatus.DARKNESS, 1);
          break;
        case 4221003:
        case 4121003:
        case 33121005:
          monsterStatus.put(MonsterStatus.SHOWDOWN, ret.x);
          monsterStatus.put(MonsterStatus.MDEF, ret.x);
          monsterStatus.put(MonsterStatus.WDEF, ret.x);
          break;
        case 2201004: // cold beam
        case 2211002: // ice strike
        case 3211003: // blizzard
        case 2211006: // il elemental compo
        case 2221007: // Blizzard
        case 5211005: // Ice Splitter
        case 2121006: // Paralyze
        case 21120006: // Tempest
        case 22121000:
          monsterStatus.put(MonsterStatus.FREEZE, 1);
          ret.duration *= 2; // freezing skills are a little strange
          break;
        case 2101003: // fp slow
        case 2201003: // il slow
        case Evan.SLOW: // Slow
          monsterStatus.put(MonsterStatus.SPEED, -100);
          break;
        case 12101001:
        case 2101005: // poison breath
        case 2111006: // fp elemental compo
        case 2121003: // ice demon
        case 2221003: // fire demon
        case 3111003: //inferno, new
        case 22161002: //phantom imprint
          monsterStatus.put(MonsterStatus.POISON, 1);
          break;
        case 4121004: // Ninja ambush
        case 4221004:
          monsterStatus.put(MonsterStatus.NINJA_AMBUSH, (int) ret.damage);
          break;
        case 2311005:
          monsterStatus.put(MonsterStatus.DOOM, 1);
          break;
        case 4341003:
          monsterStatus.put(MonsterStatus.MONSTER_BOMB, Integer.valueOf(ret.damage));
          break;
        case 32111006:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.REAPER, 1));
          break;
        case 4341006:
        case 3111002: // puppet ranger
        case 3211002: // puppet sniper
        case 13111004: // puppet cygnus
        case 5211001: // Pirate octopus summon
        case 5220002: // wrath of the octopi
        case 33111003:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PUPPET, 1));
          break;
        case 3211005: // golden eagle
        case 3111005: // golden hawk
        case 33111005:
        case 35111002:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, 1));
          monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
          break;
        case 3221005: // frostprey
        case 2121005: // elquines
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, 1));
          monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
          break;
        case 2311006: // summon dragon
        case 3121006: // phoenix
        case 2221005: // ifrit
        case 2321003: // bahamut
        case 1321007: // Beholder
        case 5211002: // Pirate bird summon
        case 11001004:
        case 12001004:
        case 12111004: // Itrit
        case 13001004:
        case 14001005:
        case 15001004:
        case 35111001:
        case 35111010:
        case 35111009:
        case 35111005: //TEMP
        case 35111004: //TEMP
          //case 35111011: //TEMP
        case 35121009:
          //case 35121010: //TEMP
        case 35121011:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, 1));
          break;
        case 2311003: // hs
        case 9101002: // GM hs
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SYMBOL, ret.x));
          break;
        case 2211004: // il seal
        case 2111004: // fp seal
        case 12111002: // cygnus seal
          monsterStatus.put(MonsterStatus.SEAL, 1);
          break;
        case 4111003: // shadow web
        case 14111001:
          monsterStatus.put(MonsterStatus.SHADOW_WEB, 1);
          break;
        case 4121006: // spirit claw
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPIRIT_CLAW, 0));
          break;
        case 2121004:
        case 2221004:
        case 2321004: // Infinity
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INFINITY, ret.x));
          break;
        case 1121002:
        case 1221002:
        case 1321002: // Stance
        case 21121003: // Aran - Freezing Posture
        case 32121005:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.STANCE, (int) ret.prop));
          break;
        case 1005: // Echo of Hero
        case 10001005: // Cygnus Echo
        case 20001005: // Aran
        case 20011005: // Evan
        case 30001005:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ECHO_OF_HERO, ret.x));
          break;
        case 1026: // Soaring
        case 10001026: // Soaring
        case 20001026: // Soaring
        case 20011026: // Soaring
        case 30001026:
          ret.duration = 60 * 120 * 1000; //because it seems to dispel asap.
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOARING, 1));
          break;
        case 2121002: // mana reflection
        case 2221002:
        case 2321002:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MANA_REFLECTION, 1));
          break;
        case 2321005: // holy shield
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SHIELD, ret.x));
          break;
        case 3121007: // Hamstring
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HAMSTRING, ret.x));
          monsterStatus.put(MonsterStatus.SPEED, ret.x);
          break;
        case 3221006: // Blind
        case 33111004:
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLIND, ret.x));
          monsterStatus.put(MonsterStatus.ACC, ret.x);
          break;
        case 33121006: //feline berserk
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAXHP, ret.x));
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WATK, ret.y));//temp
          //statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH_SPEED, ret.z));
          break;
        case 32001003: //dark aura
        case 32120000:
          ret.duration = 60 * 120 * 1000; //because it seems to dispel asap.
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARK_AURA, ret.x));
          break;
        case 32101002: //blue aura
        case 32110000:
          ret.duration = 60 * 120 * 1000; //because it seems to dispel asap.
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLUE_AURA, ret.x));
          break;
        case 32101003: //yellow aura
        case 32120001:
          ret.duration = 60 * 120 * 1000; //because it seems to dispel asap.
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.YELLOW_AURA, ret.x));
          break;
        case 33101004: //it's raining mines
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.RAINING_MINES, ret.x)); //x?
          break;
        case 35101007: //perfect armor
          ret.duration = 60 * 120 * 1000;
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PERFECT_ARMOR, ret.x));
          break;
        case 35121006: //satellite safety
          ret.duration = 60 * 120 * 1000;
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SATELLITESAFE_PROC, ret.x));
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SATELLITESAFE_ABSORB, ret.y));
          break;
        case 35001001: //flame
        case 35101009:
        case 35111007: //TEMP
          //pre-bb = 35111007,
          ret.duration = 8000;
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MECH_CHANGE, (int) level)); //ya wtf
          break;
        case 35121013:
          //case 35111004: //siege
        case 35101002: //TEMP
          ret.duration = 5000;
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MECH_CHANGE, (int) level)); //ya wtf
          break;
        case 35121005: //missile
          ret.duration = 60 * 120 * 1000;
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MECH_CHANGE, (int) level)); //ya wtf
          break;
        default:
          break;
      }
    }
    if (ret.isMonsterRiding()) {
      statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 1));
    }
    if (ret.isMorph() || ret.isPirateMorph()) {
      if (ret.randomMorph == null) {
        statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, ret.getMorph()));
      } else {
        for (Pair<Integer, Integer> rr : ret.randomMorph) {
          statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, rr.getLeft()));
        }
      }
    }

    ret.monsterStatus = monsterStatus;
    statups.trimToSize();
    ret.statups = statups;

    return ret;
  }

  /**
   * @param applyto
   * @param obj
   * @param attack  damage done by the skill
   */
  public final void applyPassive(final MapleCharacter applyto, final MapleMapObject obj) {
    if (makeChanceResult()) {
      switch (sourceid) { // MP eater
        case 2100000:
        case 2200000:
        case 2300000:
          if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
            return;
          }
          final MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
          if (!mob.getStats().isBoss()) {
            final int absorbMp = Math.min((int) (mob.getMobMaxMp() * (getX() / 100.0)), mob.getMp());
            if (absorbMp > 0) {
              mob.setMp(mob.getMp() - absorbMp);
              applyto.getStat().setMp((short) (applyto.getStat().getMp() + absorbMp));
              applyto.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 1));
              applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1), false);
            }
          }
          break;
      }
    }
  }

  public final boolean applyTo(MapleCharacter chr) {
    return applyTo(chr, chr, true, null, duration);
  }

  public final boolean applyTo(MapleCharacter chr, Point pos) {
    return applyTo(chr, chr, true, pos, duration);
  }

  public final boolean applyTo(final MapleCharacter applyfrom, final MapleCharacter applyto, final boolean primary, final Point pos, int newDuration) {
    if (isHeal() && (applyfrom.getMapId() == 749040100 || applyto.getMapId() == 749040100)) {
      return false; //z
      //} else if (isSoaring() && !applyfrom.getMap().canSoar()) {
      //	return false;
    } else if (sourceid == 4341006 && applyfrom.getBuffedValue(MapleBuffStat.MIRROR_IMAGE) == null) {
      applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
      return false; //not working
    } else if (sourceid == 33101004 && applyfrom.getMap().isTown()) {
      applyfrom.dropMessage(5, "You may not use this skill in towns.");
      applyfrom.getClient().getSession().write(MaplePacketCreator.enableActions());
      return false; //not supposed to
    } else if (sourceid == 2210062 || sourceid == 2210063 || sourceid == 2210064 || sourceid == 2210065) {
      for (int i = 1066; i <= 1067; i++) {
        final ISkill skill = SkillFactory.getSkill(GameConstants.getSkillByJob(i, applyfrom.getJob()));
        applyfrom.changeSkillLevel_Skip(skill, (byte) 1, (byte) 1); // apply
      }
    }

    int hpchange = calcHPChange(applyfrom, primary);
    int mpchange = calcMPChange(applyfrom, primary);

    final PlayerStats stat = applyto.getStat();
    if (primary) {
      if (itemConNo != 0) {
        MapleInventoryManipulator.removeById(applyto.getClient(), GameConstants.getInventoryType(itemCon), itemCon, itemConNo, false, true);
      }
    } else if (!primary && isResurrection()) {
      hpchange = stat.getMaxHp();
      applyto.setStance(0); //TODO fix death bug, player doesnt spawn on other screen
    }
    if (isDispel() && makeChanceResult()) {
      applyto.dispelDebuffs();
    } else if (isHeroWill()) {
      applyto.dispelDebuff(MapleDisease.SEDUCE);
    } else if (cureDebuffs.size() > 0) {
      for (final MapleDisease debuff : cureDebuffs) {
        applyfrom.dispelDebuff(debuff);
      }
    } else if (isMPRecovery()) {
      final int toDecreaseHP = ((stat.getMaxHp() / 100) * 10);
      if (stat.getHp() > toDecreaseHP) {
        hpchange += -toDecreaseHP; // -10% of max HP
      } else {
        hpchange = stat.getHp() == 1 ? 0 : stat.getHp() - 1;
      }
      mpchange += ((toDecreaseHP / 100) * getY());
    }
    final List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
    if (hpchange != 0) {
      if (hpchange < 0 && (-hpchange) > stat.getHp() && !applyto.hasDisease(MapleDisease.ZOMBIFY)) {
        return false;
      }
      stat.setHp(stat.getHp() + hpchange);
    }
    if (mpchange != 0) {
      if (mpchange < 0 && (-mpchange) > stat.getMp()) {
        return false;
      }
      //short converting needs math.min cuz of overflow
      stat.setMp(stat.getMp() + mpchange);

      hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(stat.getMp())));
    }
    hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(stat.getHp())));

    applyto.getClient().getSession().write(MaplePacketCreator.updatePlayerStats(hpmpupdate, true, applyto.getJob()));

    if (expinc != 0) {
      applyto.gainExp(expinc, true, true, false);
      applyto.getClient().getSession().write(MaplePacketCreator.showSpecialEffect(17));
    } else if (GameConstants.isMonsterCard(sourceid)) {
      applyto.getMonsterBook().addCard(applyto.getClient(), sourceid);
    } else if (isSpiritClaw()) {
      MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
      IItem item;
      for (int i = 0; i < use.getSlotLimit(); i++) { // impose order...
        item = use.getItem((byte) i);
        if (item != null) {
          if (GameConstants.isThrowingStar(item.getItemId()) && item.getQuantity() >= 200) {
            MapleInventoryManipulator.removeById(applyto.getClient(), MapleInventoryType.USE, item.getItemId(), 200, false, true);
            break;
          }
        }
      }
    } else if (cp != 0 && applyto.getCarnivalParty() != null) {
      applyto.getCarnivalParty().addCP(applyto, cp);
      applyto.CPUpdate(false, applyto.getAvailableCP(), applyto.getTotalCP(), 0);
      for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
        chr.CPUpdate(true, applyto.getCarnivalParty().getAvailableCP(), applyto.getCarnivalParty().getTotalCP(), applyto.getCarnivalParty().getTeam());
      }
    } else if (nuffSkill != 0 && applyto.getParty() != null) {
      final MCSkill skil = MapleCarnivalFactory.getInstance().getSkill(nuffSkill);
      if (skil != null) {
        final MapleDisease dis = skil.getDisease();
        for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
          if (chr.getParty() == null || (chr.getParty().getId() != applyto.getParty().getId())) {
            if (skil.targetsAll || Randomizer.nextBoolean()) {
              if (dis == null) {
                chr.dispel();
              } else if (skil.getSkill() == null) {
                chr.giveDebuff(dis, 1, 30000, MapleDisease.getByDisease(dis), 1);
              } else {
                chr.giveDebuff(dis, skil.getSkill());
              }
              if (!skil.targetsAll) {
                break;
              }
            }
          }
        }
      }
    }
    if (overTime && !isEnergyCharge()) {
      applyBuffEffect(applyfrom, applyto, primary, newDuration);
    }
    if (skill) {
      removeMonsterBuff(applyfrom);
    }
    if (primary) {
      if ((overTime || isHeal()) && !isEnergyCharge()) {
        applyBuff(applyfrom, newDuration);
      }
      if (isMonsterBuff()) {
        applyMonsterBuff(applyfrom);
      }
    }
    final SummonMovementType summonMovementType = getSummonMovementType();
    if (summonMovementType != null) {

      short x = (short) applyfrom.getPosition().x;
      short y = (short) applyfrom.getPosition().y;

      if (sourceid == 3111002) {
        int multiplier = (applyfrom.getFacingDirection() == 0 ? 1 : -1);
        short c = (short) (x + 200 * multiplier);
        MapleFoothold f = applyfrom.getMap().getFootholds().findBelow(new Point(c, y));
        if (f != null && f.getY1() == applyfrom.getPosition().y) {
          x = c;
          y = (short) applyfrom.getPosition().y;
        }

      }

      final MapleSummon tosummon = new MapleSummon(applyfrom, this, new Point(x, y), summonMovementType);


      if (!tosummon.isPuppet()) {
        applyfrom.getCheatTracker().resetSummonAttack();
      }
      applyfrom.getMap().spawnSummon(tosummon);
      applyfrom.getSummons().put(sourceid, tosummon);
      tosummon.addHP((short) x);
      if (isBeholder()) {
        tosummon.addHP((short) 1);
      }
      if (sourceid == 4341006) {
        applyfrom.cancelEffectFromBuffStat(MapleBuffStat.MIRROR_IMAGE);
      }
    } else if (isMagicDoor()) { // Magic Door
      MapleDoor door = new MapleDoor(applyto, new Point(applyto.getPosition()), sourceid); // Current Map door
      if (door.getTownPortal() != null) {

        applyto.getMap().spawnDoor(door);
        applyto.addDoor(door);

        MapleDoor townDoor = new MapleDoor(door); // Town door
        applyto.addDoor(townDoor);
        door.getTown().spawnDoor(townDoor);

        if (applyto.getParty() != null) { // update town doors
          applyto.silentPartyUpdate();
        }
      } else {
        applyto.dropMessage(5, "You may not spawn a door because all doors in the town are taken.");
      }

    } else if (isMist()) {
      final Rectangle bounds = calculateBoundingBox(pos != null ? pos : new Point(applyfrom.getPosition()), applyfrom.isFacingLeft());
      final MapleMist mist = new MapleMist(bounds, applyfrom, this);
      applyfrom.getMap().spawnMist(mist, getDuration(), false);

    } else if (isTimeLeap()) { // Time Leap
      for (MapleCoolDownValueHolder i : applyto.getCooldowns()) {
        if (i.skillId != 5121010) {
          applyto.removeCooldown(i.skillId);
          applyto.getClient().getSession().write(MaplePacketCreator.skillCooldown(i.skillId, 0));
        }
      }
    }
    return true;
  }

  public final boolean applyReturnScroll(final MapleCharacter applyto) {
    if (moveTo != -1) {
      if (applyto.getMap().getReturnMapId() != applyto.getMapId()) {
        MapleMap target;
        if (moveTo == 999999999) {
          target = applyto.getMap().getReturnMap();
        } else {
          target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory().getMap(moveTo);
          if (target.getId() / 10000000 != 60 && applyto.getMapId() / 10000000 != 61) {
            if (target.getId() / 10000000 != 21 && applyto.getMapId() / 10000000 != 20) {
              if (target.getId() / 10000000 != applyto.getMapId() / 10000000) {
                return false;
              }
            }
          }
        }
        applyto.changeMap(target, target.getPortal(0));
        return true;
      }
    }
    return false;
  }

  private final boolean isSoulStone() {
    return skill && sourceid == 22181003;
  }

  private final void applyBuff(final MapleCharacter applyfrom, int newDuration) {
    if (isSoulStone()) {
      if (applyfrom.getParty() != null) {
        int membrs = 0;
        for (MapleCharacter chr : applyfrom.getMap().getCharactersThreadsafe()) {
          if (chr.getParty() != null && chr.getParty().equals(applyfrom.getParty()) && chr.isAlive()) {
            membrs++;
          }
        }
        List<MapleCharacter> awarded = new ArrayList<MapleCharacter>();
        while (awarded.size() < Math.min(membrs, y)) {
          for (MapleCharacter chr : applyfrom.getMap().getCharactersThreadsafe()) {
            if (chr.isAlive() && chr.getParty().equals(applyfrom.getParty()) && !awarded.contains(chr) && Randomizer.nextInt(y) == 0) {
              awarded.add(chr);
            }
          }
        }
        for (MapleCharacter chr : awarded) {
          applyTo(applyfrom, chr, false, null, newDuration);
          chr.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 2));
          chr.getMap().broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr.getId(), sourceid, 2), false);
        }
      }
    } else if (isPartyBuff() && (applyfrom.getParty() != null || isGmBuff())) {
      final Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
      final List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));

      for (final MapleMapObject affectedmo : affecteds) {
        final MapleCharacter affected = (MapleCharacter) affectedmo;

        if (affected != applyfrom && (isGmBuff() || applyfrom.getParty().equals(affected.getParty()))) {
          if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())) {
            applyTo(applyfrom, affected, false, null, newDuration);
            affected.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 2));
            affected.getMap().broadcastMessage(affected, MaplePacketCreator.showBuffeffect(affected.getId(), sourceid, 2), false);
          }
          if (isTimeLeap()) {
            for (MapleCoolDownValueHolder i : affected.getCooldowns()) {
              if (i.skillId != 5121010) {
                affected.removeCooldown(i.skillId);
                affected.getClient().getSession().write(MaplePacketCreator.skillCooldown(i.skillId, 0));
              }
            }
          }
        }
      }
    }
  }

  private final void removeMonsterBuff(final MapleCharacter applyfrom) {
    List<MonsterStatus> cancel = new ArrayList<MonsterStatus>();
    ;
    switch (sourceid) {
      case 1111007:
        cancel.add(MonsterStatus.WDEF);
        cancel.add(MonsterStatus.WEAPON_DEFENSE_UP);
        //cancel.add(MonsterStatus.WEAPON_IMMUNITY);
        break;
      case 1211009:
        cancel.add(MonsterStatus.MDEF);
        cancel.add(MonsterStatus.MAGIC_DEFENSE_UP);
        //cancel.add(MonsterStatus.MAGIC_IMMUNITY);
        break;
      case 1311007:
        cancel.add(MonsterStatus.WATK);
        cancel.add(MonsterStatus.WEAPON_ATTACK_UP);
        cancel.add(MonsterStatus.MATK);
        cancel.add(MonsterStatus.MAGIC_ATTACK_UP);
        break;
      default:
        return;
    }
    final Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
    final List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
    int i = 0;

    for (final MapleMapObject mo : affected) {
      if (makeChanceResult()) {
        for (MonsterStatus stat : cancel) {
          ((MapleMonster) mo).cancelStatus(stat);
        }
      }
      i++;
      if (i >= mobCount) {
        break;
      }
    }
  }

  private final void applyMonsterBuff(final MapleCharacter applyfrom) {

    final Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
    List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
    int i = 0;

    for (final MapleMapObject mo : affected) {
      if (makeChanceResult() || sourceid == Evan.SLOW) {
        for (Map.Entry<MonsterStatus, Integer> stat : getMonsterStati().entrySet()) {
          ((MapleMonster) mo).applyStatus(applyfrom, new MonsterStatusEffect(stat.getKey(), stat.getValue(), sourceid, null, false), isPoison(), getDuration(), false);
        }
      }
      i++;
      if (i >= mobCount) {
        break;
      }
    }
  }

  private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
    Point myrb;
    Point mylt;
    Point uselt;
    Point userb;
    if (sourceid == Evan.SLOW) {//little hack...
      uselt = new Point(-200, -150);
      userb = new Point(200, 150);
    } else {
      if (lt == null || rb == null) {
        return new Rectangle(posFrom.x, posFrom.y, facingLeft ? 1 : -1, 1);
      }
      uselt = this.lt;
      userb = this.rb;
    }


    if (facingLeft) {
      mylt = new Point(uselt.x + posFrom.x, uselt.y + posFrom.y);
      myrb = new Point(userb.x + posFrom.x, userb.y + posFrom.y);

    } else {
      mylt = new Point(-userb.x + posFrom.x, uselt.y + posFrom.y);
      myrb = new Point(-uselt.x + posFrom.x, userb.y + posFrom.y);
    }
    int width = myrb.x - mylt.x;
    int height = myrb.y - mylt.y;
    int statX = mylt.x;
    int statY = mylt.y;
    Rectangle bounds = new Rectangle(statX, statY, width, height);
    return bounds;
  }

  public final void setDuration(int d) {
    this.duration = d;
  }

  public final void silentApplyBuff(final MapleCharacter chr, final long starttime) {
    final int localDuration = alchemistModifyVal(chr, duration, false);
    chr.registerEffect(this, starttime, BuffTimer.getInstance().schedule(new CancelEffectAction(chr, this, starttime),
        ((starttime + localDuration) - System.currentTimeMillis())));

    final SummonMovementType summonMovementType = getSummonMovementType();
    if (summonMovementType != null) {
      final MapleSummon tosummon = new MapleSummon(chr, this, chr.getPosition(), summonMovementType);
      if (!tosummon.isPuppet()) {
        chr.getCheatTracker().resetSummonAttack();
        chr.getMap().spawnSummon(tosummon);
        chr.getSummons().put(sourceid, tosummon);
        tosummon.addHP((short) x);
        if (isBeholder()) {
          tosummon.addHP((short) 1);
        }
      }
    }
  }

  public final void applyComboBuff(final MapleCharacter applyto, short combo) {
    final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ARAN_COMBO, (int) combo));
    applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, 99999, stat, this)); // Hackish timing, todo find out

    final long starttime = System.currentTimeMillis();
//	final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
//	final ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + 99999) - System.currentTimeMillis()));
    applyto.registerEffect(this, starttime, null);
  }

  public final void applyEnergyBuff(final MapleCharacter applyto, final boolean infinity) {
    final List<Pair<MapleBuffStat, Integer>> stat = this.statups;

    final long starttime = System.currentTimeMillis();
    if (infinity) {
      applyto.getClient().getSession().write(MaplePacketCreator.giveEnergyChargeTest(0, duration / 1000));
      applyto.registerEffect(this, starttime, null);
    } else {
      applyto.cancelEffect(this, true, -1);
      applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveEnergyChargeTest(applyto.getId(), 10000, duration / 1000), false);
      final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
      final ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, ((starttime + duration) - System.currentTimeMillis()));
      this.statups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ENERGY_CHARGE, 10000));
      applyto.registerEffect(this, starttime, schedule);
      this.statups = stat;
    }
  }

  private final void applyBuffEffect(final MapleCharacter applyfrom, final MapleCharacter applyto, final boolean primary, final int newDuration) {
    int localDuration = newDuration;
    if (primary) {
      localDuration = alchemistModifyVal(applyfrom, localDuration, false);
      applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1), false);
    }
    List<Pair<MapleBuffStat, Integer>> localstatups = statups;
    boolean normal = true;
    switch (sourceid) {
      case 5121009: // Speed Infusion
      case 15111005:
      case 5001005: // Dash
      case 4321000: //tornado spin
      case 15001003: {
        applyto.getClient().getSession().write(MaplePacketCreator.givePirate(statups, localDuration / 1000, sourceid));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignPirate(statups, localDuration / 1000, applyto.getId(), sourceid), false);
        normal = false;
        break;
      }
      case 5211006: // Homing Beacon
      case 22151002: //killer wings
      case 5220011: {// Bullseye
        if (applyto.getFirstLinkMid() > 0) {
          applyto.getClient().getSession().write(MaplePacketCreator.cancelHoming());
          applyto.getClient().getSession().write(MaplePacketCreator.giveHoming(sourceid, applyto.getFirstLinkMid()));
        } else {
          return;
        }
        normal = false;
        break;
      }
      case 13101006:
      case 4330001:
      case 4001003:
      case 14001003: { // Dark Sight
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, 0));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        break;
      }
      //case 22131001: {//magic shield
      //final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_SHIELD, x));
      //applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
      //break;
      //}
      case 32001003: //dark aura
      case 32120000: {
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARK_AURA, 1));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA);
        applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA);
        break;
      }
      case 32101002: //blue aura
      case 32110000: {
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLUE_AURA, 1));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA);
        applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA);
        break;
      }
      case 32101003: //yellow aura
      case 32120001: {
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.YELLOW_AURA, 1));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA);
        applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA);
        break;
      }
      case 1211008:
      case 1211007: { //lightning
        if (applyto.getBuffedValue(MapleBuffStat.WK_CHARGE) != null && applyto.getBuffSource(MapleBuffStat.WK_CHARGE) != sourceid) {
          localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.LIGHTNING_CHARGE, 1));
        }
        applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
        normal = false;
        break;
      }

      case 35001001: //flame
      case 35101009:
      case 35111007: //TEMP
      case 35101002: //TEMP
      case 35121013:
        //  case 35111004: siege
      case 35121005: { //missile
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MECH_CHANGE, 1));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        break;
      }
      case 1111002:
      case 11111001: { // Combo
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        break;
      }
      case 3101004:
      case 3201004:
      case 13101003: { // Soul Arrow
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, 0));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        break;
      }
      case 4111002:
      case 14111000: { // Shadow Partne
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, 0));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        break;
      }
      case 15111006: { // Spark
        localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPARK, x));
        applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
        normal = false;
        break;
      }
      case 4341002: { // Final Cut
        localstatups = Arrays.asList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WATK, y), new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINAL_CUT, y));
        applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
        normal = false;
        break;
      }
      case 4331003: { // Owl Spirit
        localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.OWL_SPIRIT, y));
        applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
        normal = false;
        break;
      }
      case 4331002: { // Mirror Image
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_IMAGE, 0));
        applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        break;
      }
      case 1121010: // Enrage
        applyto.handleOrbconsume();
        break;
      default:
        if (isRandomMorph()) {
          final int random = getMorph();
          final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, Integer.valueOf(random)));
          applyto.setMorphId((byte) random);
          applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
          applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(-sourceid, localDuration, stat, this));
          normal = false;
        } else if (isMorph() || isPirateMorph()) {
          final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, Integer.valueOf(getMorph(applyto))));
          applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        } else if (isMonsterRiding()) {
          final int mountid = parseMountInfo(applyto, sourceid);
          if (mountid != 0) {
            final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 0));
            applyto.getClient().getSession().write(MaplePacketCreator.cancelBuff(null));
            applyto.getClient().getSession().write(MaplePacketCreator.giveMount(mountid, sourceid, stat));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showMonsterRiding(applyto.getId(), stat, mountid, sourceid), false);
          } else {
            return;
          }
          normal = false;
        } else if (isSoaring()) {
          localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOARING, 1));
          applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), localstatups, this), false);
          applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, localDuration, localstatups, this));
          normal = false;
          //} else if (berserk > 0) {
          //    final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PYRAMID_PQ, berserk));
          //    applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        } else if (isBerserkFury() || berserk2 > 0) {
          final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BERSERK_FURY, 1));
          applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        } else if (isDivineBody()) {
          final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DIVINE_BODY, 1));
          applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), stat, this), false);
        }
        break;
    }
    if (!isMonsterRiding()) {
      applyto.cancelEffect(this, true, -1, localstatups);
    }
    // Broadcast effect to self
    if (normal && statups.size() > 0) {
      applyto.getClient().getSession().write(MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, statups, this));
    }
    final long starttime = System.currentTimeMillis();
    final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
    //System.out.println("Started effect " + sourceid + ". Duration: " + localDuration + ", Actual Duration: " + (((starttime + localDuration) - System.currentTimeMillis())));
    final ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, ((starttime + localDuration) - System.currentTimeMillis()));
    applyto.registerEffect(this, starttime, schedule, localstatups);
  }

  public static final int parseMountInfo(final MapleCharacter player, final int skillid) {
    switch (skillid) {
      case 1004: // Monster riding
      case 10001004:
      case 20001004:
      case 20011004:
      case 30001004:
        //if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -118) != null) {
        //    return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -118).getItemId();
        //}
        if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
          return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId();
        }
        return 0;
      default:
        return GameConstants.getMountItem(skillid);
    }
  }

  private final int calcHPChange(final MapleCharacter applyfrom, final boolean primary) {
    int hpchange = 0;
    if (hp != 0) {
      if (!skill) {
        if (primary) {
          hpchange += alchemistModifyVal(applyfrom, hp, true);
        } else {
          hpchange += hp;
        }
        if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
          hpchange /= 2;
        }
      } else { // assumption: this is heal
        hpchange += makeHealHP(hp / 100.0, applyfrom.getStat().getTotalMagic(), 3, 5);
        if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
          hpchange = -hpchange;
        }
      }
    }
    if (hpR != 0) {
      hpchange += (int) (applyfrom.getStat().getCurrentMaxHp() * hpR) / (applyfrom.hasDisease(MapleDisease.ZOMBIFY) ? 2 : 1);
    }
    // actually receivers probably never get any hp when it's not heal but whatever
    if (primary) {
      if (hpCon != 0) {
        hpchange -= hpCon;
      }
    }
    switch (this.sourceid) {
      case 4211001: // Chakra
        final PlayerStats stat = applyfrom.getStat();
        int v42 = getY() + 100;
        int v38 = Randomizer.rand(1, 100) + 100;
        hpchange = (int) ((v38 * stat.getLuk() * 0.033 + stat.getDex()) * v42 * 0.002);
        hpchange += makeHealHP(getY() / 100.0, applyfrom.getStat().getTotalLuk(), 2.3, 3.5);
        break;
    }
    return hpchange;
  }

  private static final int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
    return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
  }

  private static final int getElementalAmp(final int job) {
    switch (job) {
      case 211:
      case 212:
        return 2110001;
      case 221:
      case 222:
        return 2210001;
      case 1211:
      case 1212:
        return 12110001;
      case 2215:
      case 2216:
      case 2217:
      case 2218:
        return 22150000;
    }
    return -1;
  }

  private final int calcMPChange(final MapleCharacter applyfrom, final boolean primary) {
    int mpchange = 0;
    if (mp != 0) {
      if (primary) {
        mpchange += alchemistModifyVal(applyfrom, mp, true);
      } else {
        mpchange += mp;
      }
    }
    if (mpR != 0) {
      mpchange += (int) (applyfrom.getStat().getCurrentMaxMp() * mpR);
    }
    if (primary) {
      if (mpCon != 0) {
        double mod = 1.0;

        final int ElemSkillId = getElementalAmp(applyfrom.getJob());
        if (ElemSkillId != -1) {
          final ISkill amp = SkillFactory.getSkill(ElemSkillId);
          final int ampLevel = applyfrom.getSkillLevel(amp);
          if (ampLevel > 0) {
            MapleStatEffect ampStat = amp.getEffect(ampLevel);
            mod = ampStat.getX() / 100.0;
          }
        }
        final Integer Concentrate = applyfrom.getBuffedSkill_X(MapleBuffStat.CONCENTRATE);
        final int percent_off = applyfrom.getStat().mpconReduce + (Concentrate == null ? 0 : Concentrate);
        if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null) {
          mpchange = 0;
        } else {
          mpchange -= (mpCon - (mpCon * percent_off / 100)) * mod;
        }
      }
    }
    return mpchange;
  }

  private final int alchemistModifyVal(final MapleCharacter chr, final int val, final boolean withX) {
    if (!skill) {
      int offset = chr.getStat().RecoveryUP;
      final MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
      if (alchemistEffect != null) {
        offset += (withX ? alchemistEffect.getX() : alchemistEffect.getY());
      } else {
        offset += 100;
      }
      return (val * offset / 100);
    }
    return val;
  }

  private final MapleStatEffect getAlchemistEffect(final MapleCharacter chr) {
    ISkill al;
    switch (chr.getJob()) {
      case 411:
      case 412:
        al = SkillFactory.getSkill(4110000);
        if (chr.getSkillLevel(al) <= 0) {
          return null;
        }
        return al.getEffect(chr.getSkillLevel(al));
      case 1411:
      case 1412:
        al = SkillFactory.getSkill(14110003);
        if (chr.getSkillLevel(al) <= 0) {
          return null;
        }
        return al.getEffect(chr.getSkillLevel(al));
    }
    if (GameConstants.isResist(chr.getJob())) {
      al = SkillFactory.getSkill(30000002);
      if (chr.getSkillLevel(al) <= 0) {
        return null;
      }
      return al.getEffect(chr.getSkillLevel(al));
    }
    return null;
  }

  public final void setSourceId(final int newid) {
    sourceid = newid;
  }

  private final boolean isGmBuff() {
    switch (sourceid) {
      case 1005: // echo of hero acts like a gm buff
      case 10001005: // cygnus Echo
      case 20001005: // Echo
      case 20011005:
      case 30001005:
      case 9101000: // GM dispel
      case 9101001: // GM haste
      case 9101002: // GM Holy Symbol
      case 9101003: // GM Bless
      case 9101005: // GM resurrection
      case 9101008: // GM Hyper body
        return true;
      default:
        return false;
    }
  }

  private final boolean isEnergyCharge() {
    return skill && (sourceid == 5110001 || sourceid == 15100004);
  }

  private final boolean isMonsterBuff() {
    switch (sourceid) {
      case 1201006: // threaten
      case 2101003: // fp slow
      case 2201003: // il slow
      case 12101001: // cygnus slow
      case 2211004: // il seal
      case 2111004: // fp seal
      case 12111002: // cygnus seal
      case 2311005: // doom
      case 4111003: // shadow web
      case 14111001: // cygnus web
      case 4121004: // Ninja ambush
      case 4221004: // Ninja ambush
      case 22151001:
        return skill;
      case Evan.SLOW:
        return true;
      case 22121000:
      case 22161002:
      case 4321002:
      case 4341003://Monster bomb
        return skill;
    }
    return false;
  }

  public final void setPartyBuff(boolean pb) {
    this.partyBuff = pb;
  }

  private final boolean isPartyBuff() {
    if (lt == null || rb == null || !partyBuff) {
      return isSoulStone();
    }
    switch (sourceid) {
      case 1211003:
      case 1211004:
      case 1211005:
      case 1211006:
      case 1211007:
      case 1211008:
      case 1221003:
      case 1221004:
      case 11111007:
      case 12101005:
      case 4311001:
        return false;
    }
    return true;
  }

  public final boolean isHeal() {
    return sourceid == 2301002 || sourceid == 9101000;
  }

  public final boolean isResurrection() {
    return sourceid == 9101005 || sourceid == 2321006;
  }

  public final boolean isTimeLeap() {
    return sourceid == 5121010;
  }

  public final short getHp() {
    return hp;
  }

  public final short getMp() {
    return mp;
  }

  public final byte getMastery() {
    return mastery;
  }

  public final short getWatk() {
    return watk;
  }

  public final short getMatk() {
    return matk;
  }

  public final short getWdef() {
    return wdef;
  }

  public final short getMdef() {
    return mdef;
  }

  public final short getAcc() {
    return acc;
  }

  public final short getAvoid() {
    return avoid;
  }

  public final short getHands() {
    return hands;
  }

  public final short getSpeed() {
    return speed;
  }

  public final short getJump() {
    return jump;
  }

  public final int getDuration() {
    return duration;
  }

  public final boolean isOverTime() {
    return overTime;
  }

  public final List<Pair<MapleBuffStat, Integer>> getStatups() {
    return statups;
  }

  public final boolean sameSource(final MapleStatEffect effect) {
    return effect != null && this.sourceid == effect.sourceid && this.skill == effect.skill;
  }

  public final int getX() {
    return x;
  }

  public final int getY() {
    return y;
  }

  public final int getZ() {
    return z;
  }

  public final short getDamage() {
    return damage;
  }

  public final byte getAttackCount() {
    return attackCount;
  }

  public final byte getBulletCount() {
    return bulletCount;
  }

  public final int getBulletConsume() {
    return bulletConsume;
  }

  public final byte getMobCount() {
    return mobCount;
  }

  public final int getMoneyCon() {
    return moneyCon;
  }

  public final int getCooldown() {
    return cooldown;
  }

  public final Map<MonsterStatus, Integer> getMonsterStati() {
    return monsterStatus;
  }

  public final int getBerserk() {
    return berserk;
  }

  public final boolean isHide() {
    return skill && sourceid == 9101004;
  }

  public final boolean isDragonBlood() {
    return skill && sourceid == 1311008;
  }

  public final boolean isRecovery() {
    return skill && (sourceid == 1001 || sourceid == 10001001 || sourceid == 20001001 || sourceid == 20011001 || sourceid == 20021001 || sourceid == 11001 || sourceid == 35121005);
  }

  public final boolean isBerserk() {
    return skill && sourceid == 1320006;
  }

  public final boolean isBeholder() {
    return skill && sourceid == 1321007;
  }

  public final boolean isMPRecovery() {
    return skill && sourceid == 5101005;
  }

  public final boolean isMonsterRiding_() {
    return skill && (sourceid == 1004 || sourceid == 10001004 || sourceid == 20001004 || sourceid == 20011004 || sourceid == 30001004);
  }

  public final boolean isMonsterRiding() {
    return skill && (isMonsterRiding_() || GameConstants.getMountItem(sourceid) != 0);
  }

  public final boolean isMagicDoor() {
    return skill && (sourceid == 2311002 || sourceid == 8001 || sourceid == 10008001 || sourceid == 20008001 || sourceid == 20018001 || sourceid == 30008001);
  }

  public final boolean isMesoGuard() {
    return skill && sourceid == 4211005;
  }

  public final boolean isCharge() {
    switch (sourceid) {
      case 1211003:
      case 1211008:
      case 11111007:
      case 12101005:
      case 15101006:
      case 21111005:
        return skill;
    }
    return false;
  }

  public final boolean isPoison() {
    switch (sourceid) {
      case 2111003:
      case 2101005:
      case 2111006:
      case 2121003:
      case 2221003:
      case 12111005: // Flame gear
      case 3111003: //inferno, new
      case 22161002: //phantom imprint
        return skill;
    }
    return false;
  }

  private final boolean isMist() {
    return skill && (sourceid == 2111003 || sourceid == 4221006 || sourceid == 12111005 || sourceid == 22161003); // poison mist, smokescreen and flame gear, recovery aura
  }

  private final boolean isSpiritClaw() {
    return skill && sourceid == 4121006;
  }

  private final boolean isDispel() {
    return skill && (sourceid == 2311001 || sourceid == 9101000);
  }

  private final boolean isHeroWill() {
    switch (sourceid) {
      case 1121011:
      case 1221012:
      case 1321010:
      case 2121008:
      case 2221008:
      case 2321009:
      case 3121009:
      case 3221008:
      case 4121009:
      case 4221008:
      case 5121008:
      case 5221010:
      case 21121008:
      case 22171004:
      case 4341008:
      case 32121008:
      case 33121008:
      case 35121008:
        return skill;
    }
    return false;
  }

  public final boolean isAranCombo() {
    return sourceid == 21000000;
  }

  public final boolean isCombo() {
    switch (sourceid) {
      case 1111002:
      case 11111001: // Combo
        return skill;
    }
    return false;
  }

  public final boolean isPirateMorph() {
    switch (sourceid) {
      case 15111002:
      case 5111005:
      case 5121003:
        return skill;
    }
    return false;
  }

  public final boolean isMorph() {
    return morphId > 0 || isRandomMorph();
  }

  public final boolean isRandomMorph() {
    return randomMorph != null && !randomMorph.isEmpty();
  }

  private int getMorph() {
    switch (sourceid) {
      case 15111002:
      case 5111005:
        return 1000;
      case 5121003:
        return 1001;
      case 5101007:
        return 1002;
      case 13111005:
        return 1003;
    }
    if (randomMorph != null) { // we must return a value instead of 0
      boolean morphed = false;
      while (!morphed) {
        for (Pair<Integer, Integer> rm : randomMorph) { // this is only used in 2 items =.= 02211000 and 02212000
          if (rm.getRight() == 100) { // we take the 1st 100
            morphed = true;
            return rm.getLeft();
          } else if (Randomizer.nextInt(99) < rm.getRight()) {
            morphed = true;
            return rm.getLeft();
          }
        }
      }
    }
    return morphId;
  }

  public final boolean isDivineBody() {
    switch (sourceid) {
      case 1010:
      case 10001010:// Invincible Barrier
      case 20001010:
      case 20011010:
      case 30001010:
        return skill;
    }
    return false;
  }

  public final boolean isBerserkFury() {
    switch (sourceid) {
      case 1011: // Berserk fury
      case 10001011:
      case 20001011:
      case 20011011:
      case 30001011:
        return skill;
    }
    return false;
  }

  private final int getMorph(final MapleCharacter chr) {
    final int morph = getMorph();
    switch (morph) {
      case 1000:
      case 1001:
      case 1003:
        return morph + (chr.getGender() == 1 ? 100 : 0);
    }
    return morph;
  }

  public final byte getLevel() {
    return level;
  }

  public final SummonMovementType getSummonMovementType() {
    if (!skill) {
      return null;
    }
    switch (sourceid) {
      case 3211002: // puppet sniper
      case 3111002: // puppet ranger
      case 33111003:
      case 13111004: // puppet cygnus
      case 5211001: // octopus - pirate
      case 5220002: // advanced octopus - pirate
      case 4341006:
      case 35111002:
      case 35111005: //TEMP
      case 35111004: //TEMP
        //case 35111011: //TEMP
      case 35121009:
        //case 35121010: //TEMP
      case 35121011:
        //case 4111007: //TEMP
        return SummonMovementType.STATIONARY;
      case 3211005: // golden eagle
      case 3111005: // golden hawk
      case 33111005:
      case 2311006: // summon dragon
      case 3221005: // frostprey
      case 3121006: // phoenix
        return SummonMovementType.CIRCLE_FOLLOW;
      case 5211002: // bird - pirate
        return SummonMovementType.CIRCLE_STATIONARY;
      case 32111006: //reaper
        return SummonMovementType.WALK_STATIONARY;
      case 1321007: // beholder
      case 2121005: // elquines
      case 2221005: // ifrit
      case 2321003: // bahamut
      case 12111004: // Ifrit
      case 11001004: // soul
      case 12001004: // flame
      case 13001004: // storm
      case 14001005: // darkness
      case 15001004: // lightning
      case 35111001:
      case 35111010:
      case 35111009:
        return SummonMovementType.FOLLOW;
    }
    return null;
  }

  public final boolean isSkill() {
    return skill;
  }

  public final boolean isPotion() {
    return isPotion;
  }

  public final int getSourceId() {
    return sourceid;
  }

  public final boolean isSoaring() {
    switch (sourceid) {
      case 1026: // Soaring
      case 10001026: // Soaring
      case 20001026: // Soaring
      case 20011026: // Soaring
      case 30001026:
        return skill;
    }
    return false;
  }

  public final boolean isFinalAttack() {
    switch (sourceid) {
      case 13101002:
      case 11101002:
        return skill;
    }
    return false;
  }

  /**
   * @return true if the effect should happen based on it's probablity, false otherwise
   */
  public final boolean makeChanceResult() {
    return (this.prop == 1.0D) || (Math.random() < this.prop);
  }

  public final short getProb() {
    return prop;
  }

  public static class CancelEffectAction implements Runnable {

    private final MapleStatEffect effect;
    private final WeakReference<MapleCharacter> target;
    private final long startTime;

    public CancelEffectAction(final MapleCharacter target, final MapleStatEffect effect, final long startTime) {
      this.effect = effect;
      this.target = new WeakReference<MapleCharacter>(target);
      this.startTime = startTime;
    }

    @Override
    public void run() {
      final MapleCharacter realTarget = target.get();
      if (realTarget != null) {
        realTarget.cancelEffect(effect, false, startTime);
      }
    }
  }
}
