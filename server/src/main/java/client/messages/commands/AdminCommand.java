package client.messages.commands;

import client.*;
import client.anticheat.CheatingOffense;
import client.anticheat.ReportType;
import client.information.drop.DropDataProvider;
import client.information.drop.MapleDropData;
import client.information.drop.MapleDropProvider;
import client.inventory.*;
import client.messages.CommandProcessorUtil;
import constants.GameConstants;
import constants.MapConstants;
import constants.ServerConstants.PlayerGMRank;
import database.DatabaseConnection;
import handling.RecvPacketOpcode;
import handling.channel.ChannelServer;
import handling.world.CheaterData;
import handling.world.World;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.EventInstanceManager;
import scripting.EventManager;
import scripting.PortalScriptManager;
import scripting.ReactorScriptManager;
import server.*;
import server.Timer;
import server.Timer.*;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.*;
import server.maps.*;
import server.quest.MapleQuest;
import tools.*;
import tools.packet.MobPacket;
import tools.packet.PlayerShopPacket;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Emilyx3
 */

public class AdminCommand {

  public static PlayerGMRank getPlayerLevelRequired() {
    return PlayerGMRank.ADMIN;
  }

  public static class LowHP extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getStat().setHp((short) 1);
      c.getPlayer().getStat().setMp((short) 1);
      c.getPlayer().updateSingleStat(MapleStat.HP, 1);
      c.getPlayer().updateSingleStat(MapleStat.MP, 1);
      return 0;
    }
  }

  public static class Heal extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getStat().setHp(c.getPlayer().getStat().getCurrentMaxHp());
      c.getPlayer().getStat().setMp(c.getPlayer().getStat().getCurrentMaxMp());
      c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getStat().getCurrentMaxHp());
      c.getPlayer().updateSingleStat(MapleStat.MP, c.getPlayer().getStat().getCurrentMaxMp());
      return 0;
    }
  }

  public static class HellBan extends Ban {

    public HellBan() {
      hellban = true;
    }
  }

  public static class Ban extends CommandExecute {

    protected boolean hellban = false;

    private String getCommand() {
      if (hellban) {
        return "HellBan";
      } else {
        return "Ban";
      }
    }

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 3) {
        c.getPlayer().dropMessage(5, "[Syntax] !" + getCommand() + " <IGN> <Reason>");
        return 0;
      }
      StringBuilder sb = new StringBuilder(c.getPlayer().getName());
      sb.append(" banned ").append(splitted[1]).append(": ").append(StringUtil.joinStringFrom(splitted, 2));
      MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      if (target != null) {
        if (c.getPlayer().getGMLevel() > target.getGMLevel() || c.getPlayer().isAdmin()) {
          sb.append(" (IP: ").append(target.getClient().getSessionIPAddress()).append(")");
          if (target.ban(sb.toString(), c.getPlayer().isAdmin(), false, hellban)) {
            c.getPlayer().dropMessage(6, "[" + getCommand() + "] Successfully banned " + splitted[1] + ".");
            return 1;
          } else {
            c.getPlayer().dropMessage(6, "[" + getCommand() + "] Failed to ban.");
            return 0;
          }
        } else {
          c.getPlayer().dropMessage(6, "[" + getCommand() + "] May not ban GMs...");
          return 1;
        }
      } else {
        if (MapleCharacter.ban(splitted[1], sb.toString(), false,
            c.getPlayer().isAdmin() ? 250 : c.getPlayer().getGMLevel(), splitted[0].equals("!hellban"))) {
          c.getPlayer().dropMessage(6,
              "[" + getCommand() + "] Successfully offline banned " + splitted[1] + ".");
          return 1;
        } else {
          c.getPlayer().dropMessage(6, "[" + getCommand() + "] Failed to ban " + splitted[1]);
          return 0;
        }
      }
    }
  }

  public static class UnHellBan extends UnBan {

    public UnHellBan() {
      hellban = true;
    }
  }

  public static class UnBan extends CommandExecute {

    protected boolean hellban = false;

    private String getCommand() {
      if (hellban) {
        return "UnHellBan";
      } else {
        return "UnBan";
      }
    }

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(6, "[Syntax] !" + getCommand() + " <IGN>");
        return 0;
      }
      byte ret;
      if (hellban) {
        ret = MapleClient.unHellban(splitted[1]);
      } else {
        ret = MapleClient.unban(splitted[1]);
      }
      if (ret == -2) {
        c.getPlayer().dropMessage(6, "[" + getCommand() + "] SQL error.");
        return 0;
      } else if (ret == -1) {
        c.getPlayer().dropMessage(6, "[" + getCommand() + "] The character does not exist.");
        return 0;
      } else {
        c.getPlayer().dropMessage(6, "[" + getCommand() + "] Successfully unbanned!");

      }
      byte ret_ = MapleClient.unbanIPMacs(splitted[1]);
      if (ret_ == -2) {
        c.getPlayer().dropMessage(6, "[UnbanIP] SQL error.");
      } else if (ret_ == -1) {
        c.getPlayer().dropMessage(6, "[UnbanIP] The character does not exist.");
      } else if (ret_ == 0) {
        c.getPlayer().dropMessage(6, "[UnbanIP] No IP or Mac with that character exists!");
      } else if (ret_ == 1) {
        c.getPlayer().dropMessage(6, "[UnbanIP] IP/Mac -- one of them was found and unbanned.");
      } else if (ret_ == 2) {
        c.getPlayer().dropMessage(6, "[UnbanIP] Both IP and Macs were unbanned.");
      }
      return ret_ > 0 ? 1 : 0;
    }
  }

  public static class UnbanIP extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(6, "[Syntax] !unbanip <IGN>");
        return 0;
      }
      byte ret = MapleClient.unbanIPMacs(splitted[1]);
      if (ret == -2) {
        c.getPlayer().dropMessage(6, "[UnbanIP] SQL error.");
      } else if (ret == -1) {
        c.getPlayer().dropMessage(6, "[UnbanIP] The character does not exist.");
      } else if (ret == 0) {
        c.getPlayer().dropMessage(6, "[UnbanIP] No IP or Mac with that character exists!");
      } else if (ret == 1) {
        c.getPlayer().dropMessage(6, "[UnbanIP] IP/Mac -- one of them was found and unbanned.");
      } else if (ret == 2) {
        c.getPlayer().dropMessage(6, "[UnbanIP] Both IP and Macs were unbanned.");
      }
      if (ret > 0) {
        return 1;
      }
      return 0;
    }
  }

  public static class TempBan extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      final int reason = Integer.parseInt(splitted[2]);
      final int numDay = Integer.parseInt(splitted[3]);

      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, numDay);
      final DateFormat df = DateFormat.getInstance();

      if (victim == null) {
        c.getPlayer().dropMessage(6, "Unable to find character");
        return 0;
      }
      victim.tempban("Temp banned by : " + c.getPlayer().getName() + "", cal, reason, true);
      c.getPlayer().dropMessage(6, "The character " + splitted[1] + " has been successfully tempbanned till "
          + df.format(cal.getTime()));
      return 1;
    }
  }

  public static class DC extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      int level = 0;
      MapleCharacter victim;
      if (splitted[1].charAt(0) == '-') {
        level = StringUtil.countCharacters(splitted[1], 'f');
        victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[2]);
      } else {
        victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      }
      if (level < 2 && victim != null) {
        victim.getClient().getSession().close();
        if (level >= 1) {
          victim.getClient().disconnect(true, false);
        }
        return 1;
      } else {
        c.getPlayer().dropMessage(6, "Please use dc -f instead, or the victim does not exist.");
        return 0;
      }
    }
  }

  public static class Kill extends CommandExecute {

    public int execute(MapleClient c, String[] splitted) {
      MapleCharacter player = c.getPlayer();
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(6, "Syntax: !kill <list player names>");
        return 0;
      }
      MapleCharacter victim = null;
      for (int i = 1; i < splitted.length; i++) {
        try {
          victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[i]);
        } catch (Exception e) {
          c.getPlayer().dropMessage(6, "Player " + splitted[i] + " not found.");
        }
        if (player.allowedToTarget(victim)) {
          victim.getStat().setHp((short) 0);
          victim.getStat().setMp((short) 0);
          victim.updateSingleStat(MapleStat.HP, 0);
          victim.updateSingleStat(MapleStat.MP, 0);
        }
      }
      return 1;
    }
  }

  public static class Skill extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 1) {
        c.getPlayer().dropMessage(5, "!skill <id> <level> <masterLevel>");
        return 0;
      }
      ISkill skill = SkillFactory.getSkill(Integer.parseInt(splitted[1]));
      if (skill.getId() >= 22000000 && (GameConstants.isEvan((skill.getId() / 10000))
          || GameConstants.isResist((skill.getId() / 10000)))) {
        c.getPlayer().dropMessage(5, "Please change your job to an Evan instead.");
        return 0;
      }
      byte level = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);
      byte masterlevel = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1);

      if (level > skill.getMaxLevel()) {
        level = skill.getMaxLevel();
      }
      c.getPlayer().changeSkillLevel(skill, level, masterlevel);
      return 1;
    }
  }

  public static class Fame extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleCharacter player = c.getPlayer();
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(6, "Syntax: !fame <player> <amount>");
        return 0;
      }
      MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      short fame = 0;
      try {
        fame = Short.parseShort(splitted[2]);
      } catch (NumberFormatException nfe) {
        c.getPlayer().dropMessage(6, "Invalid Number... baka.");
        return 0;
      }
      if (victim != null && player.allowedToTarget(victim)) {
        victim.addFame(fame);
        victim.updateSingleStat(MapleStat.FAME, victim.getFame());
      }
      return 1;
    }
  }

  public static class HealHere extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleCharacter player = c.getPlayer();
      for (MapleCharacter mch : player.getMap().getCharacters()) {
        if (mch != null) {
          c.getPlayer().getStat().setHp(c.getPlayer().getStat().getMaxHp());
          c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getStat().getMaxHp());
          c.getPlayer().getStat().setMp(c.getPlayer().getStat().getMaxMp());
          c.getPlayer().updateSingleStat(MapleStat.MP, c.getPlayer().getStat().getMaxMp());
        }
      }
      return 1;
    }
  }

  public static class Invincible extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleCharacter player = c.getPlayer();
      if (player.isInvincible()) {
        player.setInvincible(false);
        player.dropMessage(6, "Invincibility deactivated.");
      } else {
        player.setInvincible(true);
        player.dropMessage(6, "Invincibility activated.");
      }
      return 1;
    }
  }

  public static class GiveSkill extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      ISkill skill = SkillFactory.getSkill(Integer.parseInt(splitted[2]));
      byte level = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1);
      byte masterlevel = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 4, 1);

      if (level > skill.getMaxLevel()) {
        level = skill.getMaxLevel();
      }
      victim.changeSkillLevel(skill, level, masterlevel);
      return 1;
    }
  }

  public static class Job extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().changeJob(Integer.parseInt(splitted[1]));
      return 1;
    }
  }

  public static class WhereAmI extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().dropMessage(5, "You are on map " + c.getPlayer().getMap().getId() + " on x: " + c.getPlayer().getPosition().x + " y: " + c.getPlayer().getPosition().y);
      return 1;
    }
  }

  public static class Shop extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleShopFactory shop = MapleShopFactory.getInstance();
      int shopId = Integer.parseInt(splitted[1]);
      if (shop.getShop(shopId) != null) {
        shop.getShop(shopId).sendShop(c);
      }
      return 1;
    }
  }

  public static class GainMeso extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length > 1) {
        String zero = splitted[1];
        if (zero != null && !zero.isEmpty()) {
          c.getPlayer().gainMeso(-c.getPlayer().getMeso(), true);
          return 1;
        }
      }
      c.getPlayer().gainMeso(Integer.MAX_VALUE - c.getPlayer().getMeso(), true);
      return 1;
    }
  }

  public static class GainCash extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(5, "Need amount.");
        return 0;
      }
      c.getPlayer().modifyCSPoints(1, Integer.parseInt(splitted[1]), true);
      return 1;
    }
  }

  public static class GainMP extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(5, "Need amount.");
        return 0;
      }
      c.getPlayer().modifyCSPoints(2, Integer.parseInt(splitted[1]), true);
      return 1;
    }
  }

  public static class GainP extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(5, "Need amount.");
        return 0;
      }
      c.getPlayer().setPoints(c.getPlayer().getPoints() + Integer.parseInt(splitted[1]));
      return 1;
    }
  }


  public static class LevelUp extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (c.getPlayer().getLevel() < 200) {
        c.getPlayer().gainExp(500000000, true, false, true);
      }
      return 1;
    }
  }

  public static class ClearInv extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      java.util.Map<Pair<Short, Short>, MapleInventoryType> eqs = new ArrayMap<Pair<Short, Short>, MapleInventoryType>();
      if (splitted[1].equals("all")) {
        for (MapleInventoryType type : MapleInventoryType.values()) {
          for (IItem item : c.getPlayer().getInventory(type)) {
            eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), type);
          }
        }
      } else if (splitted[1].equals("eqp")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED)) {
          eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()),
              MapleInventoryType.EQUIPPED);
        }
      } else if (splitted[1].equals("eq")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP)) {
          eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIP);
        }
      } else if (splitted[1].equals("u")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.USE)) {
          eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.USE);
        }
      } else if (splitted[1].equals("s")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.SETUP)) {
          eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.SETUP);
        }
      } else if (splitted[1].equals("e")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
          eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.ETC);
        }
      } else if (splitted[1].equals("c")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.CASH)) {
          eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.CASH);
        }
      } else {
        c.getPlayer().dropMessage(6, "[all/eqp/eq/u/s/e/c]");
      }
      for (Entry<Pair<Short, Short>, MapleInventoryType> eq : eqs.entrySet()) {
        MapleInventoryManipulator.removeFromSlot(c, eq.getValue(), eq.getKey().left, eq.getKey().right, false,
            false);
      }
      return 1;
    }
  }

  public static class UnlockInv extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      java.util.Map<IItem, MapleInventoryType> eqs = new ArrayMap<IItem, MapleInventoryType>();
      boolean add = false;
      if (splitted.length < 2 || splitted[1].equals("all")) {
        for (MapleInventoryType type : MapleInventoryType.values()) {
          for (IItem item : c.getPlayer().getInventory(type)) {
            if (ItemFlag.LOCK.check(item.getFlag())) {
              item.setFlag((byte) (item.getFlag() - ItemFlag.LOCK.getValue()));
              add = true;
              // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
              // type.getType()));
            }
            if (ItemFlag.UNTRADEABLE.check(item.getFlag())) {
              item.setFlag((byte) (item.getFlag() - ItemFlag.UNTRADEABLE.getValue()));
              add = true;
              // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
              // type.getType()));
            }
            if (add) {
              eqs.put(item, type);
            }
            add = false;
          }
        }
      } else if (splitted[1].equals("eqp")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED)) {
          if (ItemFlag.LOCK.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.LOCK.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (ItemFlag.UNTRADEABLE.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.UNTRADEABLE.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (add) {
            eqs.put(item, MapleInventoryType.EQUIP);
          }
          add = false;
        }
      } else if (splitted[1].equals("eq")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP)) {
          if (ItemFlag.LOCK.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.LOCK.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (ItemFlag.UNTRADEABLE.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.UNTRADEABLE.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (add) {
            eqs.put(item, MapleInventoryType.EQUIP);
          }
          add = false;
        }
      } else if (splitted[1].equals("u")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.USE)) {
          if (ItemFlag.LOCK.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.LOCK.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (ItemFlag.UNTRADEABLE.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.UNTRADEABLE.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (add) {
            eqs.put(item, MapleInventoryType.USE);
          }
          add = false;
        }
      } else if (splitted[1].equals("s")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.SETUP)) {
          if (ItemFlag.LOCK.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.LOCK.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (ItemFlag.UNTRADEABLE.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.UNTRADEABLE.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (add) {
            eqs.put(item, MapleInventoryType.SETUP);
          }
          add = false;
        }
      } else if (splitted[1].equals("e")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
          if (ItemFlag.LOCK.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.LOCK.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (ItemFlag.UNTRADEABLE.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.UNTRADEABLE.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (add) {
            eqs.put(item, MapleInventoryType.ETC);
          }
          add = false;
        }
      } else if (splitted[1].equals("c")) {
        for (IItem item : c.getPlayer().getInventory(MapleInventoryType.CASH)) {
          if (ItemFlag.LOCK.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.LOCK.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (ItemFlag.UNTRADEABLE.check(item.getFlag())) {
            item.setFlag((byte) (item.getFlag() - ItemFlag.UNTRADEABLE.getValue()));
            add = true;
            // c.getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
            // type.getType()));
          }
          if (add) {
            eqs.put(item, MapleInventoryType.CASH);
          }
          add = false;
        }
      } else {
        c.getPlayer().dropMessage(6, "[all/eqp/eq/u/s/e/c]");
      }

      for (Entry<IItem, MapleInventoryType> eq : eqs.entrySet()) {
        c.getPlayer().forceReAddItem_NoUpdate(eq.getKey().copy(), eq.getValue());
      }
      return 1;
    }
  }

  public static class Item extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final int itemId = Integer.parseInt(splitted[1]);
      final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);


      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (GameConstants.isPet(itemId)) {
        c.getPlayer().dropMessage(5, "Please purchase a pet from the cash shop instead.");
      } else if (!ii.itemExists(itemId)) {
        c.getPlayer().dropMessage(5, itemId + " does not exist");
      } else {
        IItem item;
        byte flag = 0;
        flag |= ItemFlag.SPIKES.getValue();

        if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
          item = ii.randomizeStats((Equip) ii.getEquipById(itemId));
          item.setFlag(flag);
        } else {
          item = new client.inventory.Item(itemId, (byte) 0, quantity, (byte) 0);
          item.setFlag(flag);
        }
        item.setOwner(c.getPlayer().getName());


        MapleInventoryManipulator.addbyItem(c, item);
      }
      return 1;
    }
  }

  public static class Drop extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final int itemId = Integer.parseInt(splitted[1]);
      final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (GameConstants.isPet(itemId)) {
        c.getPlayer().dropMessage(5, "Please purchase a pet from the cash shop instead.");
      } else if (!ii.itemExists(itemId)) {
        c.getPlayer().dropMessage(5, itemId + " does not exist");
      } else {
        IItem toDrop;
        if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
          toDrop = ii.randomizeStats((Equip) ii.getEquipById(itemId));
        } else {
          toDrop = new client.inventory.Item(itemId, (byte) 0, (short) quantity, (byte) 0);
        }

        c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(),
            true, true);
      }
      return 1;
    }
  }

  public static class Level extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().setLevel(Short.parseShort(splitted[1]));
      c.getPlayer().levelUp(true);
      if (c.getPlayer().getExp() < 0) {
        c.getPlayer().gainExp(-c.getPlayer().getExp(), false, false, true);
      }
      return 1;
    }
  }

  public static class Online extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().dropMessage(6, "Total amount of players connected to server:");
      c.getPlayer().dropMessage(6, "" + World.getConnected() + "");
      c.getPlayer().dropMessage(6, "Characters connected to channel " + c.getChannel() + ":");
      c.getPlayer().dropMessage(6, c.getChannelServer().getPlayerStorage().getOnlinePlayers(true));
      return 0;
    }
  }

  public static class Say extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length > 1) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(c.getPlayer().getName());
        sb.append("] ");
        sb.append(StringUtil.joinStringFrom(splitted, 1));
        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, sb.toString()));
      } else {
        c.getPlayer().dropMessage(6, "Syntax: !say <message>");
        return 0;
      }
      return 1;
    }
  }

  public static class Letter extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 3) {
        c.getPlayer().dropMessage(6, "syntax: !letter <color (green/red)> <word>");
        return 0;
      }
      int start, nstart;
      if (splitted[1].equalsIgnoreCase("green")) {
        start = 3991026;
        nstart = 3990019;
      } else if (splitted[1].equalsIgnoreCase("red")) {
        start = 3991000;
        nstart = 3990009;
      } else {
        c.getPlayer().dropMessage(6, "Unknown color!");
        return 0;
      }
      String splitString = StringUtil.joinStringFrom(splitted, 2);
      List<Integer> chars = new ArrayList<Integer>();
      splitString = splitString.toUpperCase();
      // System.out.println(splitString);
      for (int i = 0; i < splitString.length(); i++) {
        char chr = splitString.charAt(i);
        if (chr == ' ') {
          chars.add(-1);
        } else if ((int) (chr) >= (int) 'A' && (int) (chr) <= (int) 'Z') {
          chars.add((int) (chr));
        } else if ((int) (chr) >= (int) '0' && (int) (chr) <= (int) ('9')) {
          chars.add((int) (chr) + 200);
        }
      }
      final int w = 32;
      int dStart = c.getPlayer().getPosition().x - (splitString.length() / 2 * w);
      for (Integer i : chars) {
        if (i == -1) {
          dStart += w;
        } else if (i < 200) {
          int val = start + i - (int) ('A');
          client.inventory.Item item = new client.inventory.Item(val, (byte) 0, (short) 1);
          c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item,
              new Point(dStart, c.getPlayer().getPosition().y), false, false);
          dStart += w;
        } else if (i >= 200 && i <= 300) {
          int val = nstart + i - (int) ('0') - 200;
          client.inventory.Item item = new client.inventory.Item(val, (byte) 0, (short) 1);
          c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item,
              new Point(dStart, c.getPlayer().getPosition().y), false, false);
          dStart += w;
        }
      }
      return 1;
    }
  }

  public static class ItemCheck extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 3 || splitted[1] == null || splitted[1].equals("") || splitted[2] == null
          || splitted[2].equals("")) {
        c.getPlayer().dropMessage(6, "!itemcheck <playername> <itemid>");
        return 0;
      } else {
        int item = Integer.parseInt(splitted[2]);
        MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
        int itemamount = chr.getItemQuantity(item, true);
        if (itemamount > 0) {
          c.getPlayer().dropMessage(6, chr.getName() + " has " + itemamount + " (" + item + ").");
        } else {
          c.getPlayer().dropMessage(6, chr.getName() + " doesn't have (" + item + ")");
        }
      }
      return 1;
    }
  }

  public static class Song extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(splitted[1]));
      return 1;
    }
  }

  public static class StartAutoEvent extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final EventManager em = c.getChannelServer().getEventSM().getEventManager("AutomatedEvent");
      if (em != null) {
        em.scheduleRandomEvent();
      }
      return 1;
    }
  }

  public static class SetEvent extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleEvent.onStartEvent(c.getPlayer());
      return 1;
    }
  }

  public static class CheckPoint extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(6, "Need playername.");
        return 0;
      }
      MapleCharacter chrs = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      if (chrs == null) {
        c.getPlayer().dropMessage(6, "Make sure they are in the correct channel");
      } else {
        c.getPlayer().dropMessage(6, chrs.getName() + " has " + chrs.getPoints() + " points.");
      }
      return 1;
    }
  }

  public static class GivePoint extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 3) {
        c.getPlayer().dropMessage(6, "Need playername and amount.");
        return 0;
      }
      MapleCharacter chrs = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      if (chrs == null) {
        c.getPlayer().dropMessage(6, "Make sure they are in the correct channel");
      } else {
        chrs.setPoints(chrs.getPoints() + Integer.parseInt(splitted[2]));
        c.getPlayer().dropMessage(6,
            splitted[1] + " has " + chrs.getPoints() + " points, after giving " + splitted[2] + ".");
      }
      return 1;
    }
  }



  public static class StartEvent extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (c.getChannelServer().getEvent() == c.getPlayer().getMapId()) {
        MapleEvent.setEvent(c.getChannelServer(), false);
        c.getPlayer().dropMessage(5, "Started the event and closed off");
        return 1;
      } else {
        c.getPlayer().dropMessage(5,
            "!scheduleevent must've been done first, and you must be in the event map.");
        return 0;
      }
    }
  }

  public static class ScheduleEvent extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final MapleEventType type = MapleEventType.getByString(splitted[1]);
      if (type == null) {
        final StringBuilder sb = new StringBuilder("Wrong syntax: ");
        for (MapleEventType t : MapleEventType.values()) {
          sb.append(t.command).append(",");
        }
        c.getPlayer().dropMessage(5, sb.toString().substring(0, sb.toString().length() - 1));
      }
      final String msg = MapleEvent.scheduleEvent(type, c.getChannelServer());
      if (msg.length() > 0) {
        c.getPlayer().dropMessage(5, msg);
        return 0;
      }
      return 1;
    }
  }

  public static class RemoveItem extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 3) {
        c.getPlayer().dropMessage(6, "Need <name> <itemid>");
        return 0;
      }
      MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      if (chr == null) {
        c.getPlayer().dropMessage(6, "This player does not exist");
        return 0;
      }
      chr.removeAll(Integer.parseInt(splitted[2]));
      c.getPlayer().dropMessage(6, "All items with the ID " + splitted[2]
          + " has been removed from the inventory of " + splitted[1] + ".");
      return 1;

    }
  }

  public static class LockItem extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 3) {
        c.getPlayer().dropMessage(6, "Need <name> <itemid>");
        return 0;
      }
      MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      if (chr == null) {
        c.getPlayer().dropMessage(6, "This player does not exist");
        return 0;
      }
      int itemid = Integer.parseInt(splitted[2]);
      MapleInventoryType type = GameConstants.getInventoryType(itemid);
      for (IItem item : chr.getInventory(type).listById(itemid)) {
        item.setFlag((byte) (item.getFlag() | ItemFlag.LOCK.getValue()));
        chr.getClient().getSession().write(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
      }
      if (type == MapleInventoryType.EQUIP) {
        type = MapleInventoryType.EQUIPPED;
        for (IItem item : chr.getInventory(type).listById(itemid)) {
          item.setFlag((byte) (item.getFlag() | ItemFlag.LOCK.getValue()));
          // chr.getClient().getSession().write(MaplePacketCreator.updateSpecialItemUse(item,
          // type.getType()));
        }
      }
      c.getPlayer().dropMessage(6, "All items with the ID " + splitted[2]
          + " has been locked from the inventory of " + splitted[1] + ".");
      return 1;
    }
  }

  public static class KillMap extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (MapleCharacter map : c.getPlayer().getMap().getCharactersThreadsafe()) {
        if (map != null && !map.isGM()) {
          map.getStat().setHp((short) 0);
          map.getStat().setMp((short) 0);
          map.updateSingleStat(MapleStat.HP, 0);
          map.updateSingleStat(MapleStat.MP, 0);
        }
      }
      return 1;
    }
  }

  public static class SpeakMega extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      World.Broadcast
          .broadcastSmega(MaplePacketCreator.serverNotice(3,
              victim == null ? c.getChannel() : victim.getClient().getChannel(), victim == null
                  ? splitted[1] : victim.getName() + " : " + StringUtil.joinStringFrom(splitted, 2),
              true));
      return 1;
    }
  }

  public static class Speak extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      if (victim == null) {
        c.getPlayer().dropMessage(5, "unable to find '" + splitted[1]);
        return 0;
      } else {
        victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(),
            StringUtil.joinStringFrom(splitted, 2), victim.isGM(), 0));
      }
      return 1;
    }
  }

  public static class SpeakMap extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (MapleCharacter victim : c.getPlayer().getMap().getCharactersThreadsafe()) {
        if (victim.getId() != c.getPlayer().getId()) {
          victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(),
              StringUtil.joinStringFrom(splitted, 1), victim.isGM(), 0));
        }
      }
      return 1;
    }
  }

  public static class SpeakChn extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (MapleCharacter victim : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
        if (victim.getId() != c.getPlayer().getId()) {
          victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(),
              StringUtil.joinStringFrom(splitted, 1), victim.isGM(), 0));
        }
      }
      return 1;
    }
  }

  public static class SpeakWorld extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (ChannelServer cserv : ChannelServer.getAllInstances()) {
        for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
          if (victim.getId() != c.getPlayer().getId()) {
            victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(),
                StringUtil.joinStringFrom(splitted, 1), victim.isGM(), 0));
          }
        }
      }
      return 1;
    }
  }

  public static class Disease extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 3) {
        c.getPlayer().dropMessage(6,
            "!disease <type> [charname] <level> where type = SEAL/DARKNESS/WEAKEN/STUN/CURSE/POISON/SLOW/SEDUCE/REVERSE/ZOMBIFY/POTION/SHADOW/BLIND/FREEZE");
        return 0;
      }
      int type = 0;
      MapleDisease dis = null;
      if (splitted[1].equalsIgnoreCase("SEAL")) {
        type = 120;
      } else if (splitted[1].equalsIgnoreCase("DARKNESS")) {
        type = 121;
      } else if (splitted[1].equalsIgnoreCase("WEAKEN")) {
        type = 122;
      } else if (splitted[1].equalsIgnoreCase("STUN")) {
        type = 123;
      } else if (splitted[1].equalsIgnoreCase("CURSE")) {
        type = 124;
      } else if (splitted[1].equalsIgnoreCase("POISON")) {
        type = 125;
      } else if (splitted[1].equalsIgnoreCase("SLOW")) {
        type = 126;
      } else if (splitted[1].equalsIgnoreCase("SEDUCE")) {
        type = 128;
      } else if (splitted[1].equalsIgnoreCase("REVERSE")) {
        type = 132;
      } else if (splitted[1].equalsIgnoreCase("ZOMBIFY")) {
        type = 133;
      } else if (splitted[1].equalsIgnoreCase("POTION")) {
        type = 134;
      } else if (splitted[1].equalsIgnoreCase("SHADOW")) {
        type = 135;
      } else if (splitted[1].equalsIgnoreCase("BLIND")) {
        type = 136;
      } else if (splitted[1].equalsIgnoreCase("FREEZE")) {
        type = 137;
      } else {
        c.getPlayer().dropMessage(6,
            "!disease <type> [charname] <level> where type = SEAL/DARKNESS/WEAKEN/STUN/CURSE/POISON/SLOW/SEDUCE/REVERSE/ZOMBIFY/POTION/SHADOW/BLIND/FREEZE");
        return 0;
      }
      dis = MapleDisease.getBySkill(type);
      if (splitted.length == 4) {
        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[2]);
        if (victim == null) {
          c.getPlayer().dropMessage(5, "Not found.");
          return 0;
        }
        victim.setChair(0);
        victim.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
        victim.getMap().broadcastMessage(victim, MaplePacketCreator.showChair(c.getPlayer().getId(), 0), false);
        victim.giveDebuff(dis,
            MobSkillFactory.getMobSkill(type, CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1)));
      } else {
        for (MapleCharacter victim : c.getPlayer().getMap().getCharactersThreadsafe()) {
          victim.setChair(0);
          victim.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
          victim.getMap().broadcastMessage(victim, MaplePacketCreator.showChair(c.getPlayer().getId(), 0),
              false);
          victim.giveDebuff(dis,
              MobSkillFactory.getMobSkill(type, CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1)));
        }
      }
      return 1;
    }
  }

  public static class SQL extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      try {
        PreparedStatement ps = DatabaseConnection.getConnection()
            .prepareStatement(StringUtil.joinStringFrom(splitted, 1));
        ps.executeUpdate();
        ps.close();
      } catch (SQLException e) {
        c.getPlayer().dropMessage(6, "An error occurred : " + e.getMessage());
      }
      return 1;
    }
  }

  public static class StripEveryone extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      ChannelServer cs = c.getChannelServer();
      for (MapleCharacter mchr : cs.getPlayerStorage().getAllCharacters()) {
        if (mchr.isGM()) {
          continue;
        }
        MapleInventory equipped = mchr.getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = mchr.getInventory(MapleInventoryType.EQUIP);
        List<Byte> ids = new ArrayList<Byte>();
        for (IItem item : equipped.list()) {
          ids.add((byte) item.getPosition());
        }
        for (byte id : ids) {
          MapleInventoryManipulator.unequip(mchr.getClient(), id, equip.getNextFreeSlot());
        }
      }
      return 1;
    }
  }

  public static class SendAllNote extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {

      if (splitted.length >= 1) {
        String text = StringUtil.joinStringFrom(splitted, 1);
        for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
          c.getPlayer().sendNote(mch.getName(), text);
        }
      } else {
        c.getPlayer().dropMessage(6, "Use it like this, !sendallnote <text>");
        return 0;
      }
      return 1;
    }
  }

  public static class MesoEveryone extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (ChannelServer cserv : ChannelServer.getAllInstances()) {
        for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
          mch.gainMeso(Integer.parseInt(splitted[1]), true);
        }
      }
      return 1;
    }
  }

  public static class PermWeather extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (c.getPlayer().getMap().getPermanentWeather() > 0) {
        c.getPlayer().getMap().setPermanentWeather(0);
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeMapEffect());
        c.getPlayer().dropMessage(5, "Map weather has been disabled.");
      } else {
        final int weather = CommandProcessorUtil.getOptionalIntArg(splitted, 1, 5120000);
        if (!MapleItemInformationProvider.getInstance().itemExists(weather) || weather / 10000 != 512) {
          c.getPlayer().dropMessage(5, "Invalid ID.");
        } else {
          c.getPlayer().getMap().setPermanentWeather(weather);
          c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.startMapEffect("", weather, false));
          c.getPlayer().dropMessage(5, "Map weather has been enabled.");
        }
      }
      return 1;
    }
  }

  public static class CharInfo extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final StringBuilder builder = new StringBuilder();
      final MapleCharacter other = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      if (other == null) {
        builder.append("...does not exist");
        c.getPlayer().dropMessage(6, builder.toString());
        return 0;
      }
      /*
       * if (other.getClient().getLastPing() <= 0) {
       * other.getClient().sendPing(); }
       */
      builder.append(MapleClient.getLogMessage(other, ""));
      builder.append(" at ").append(other.getPosition().x);
      builder.append(" /").append(other.getPosition().y);

      builder.append(" || HP : ");
      builder.append(other.getStat().getHp());
      builder.append(" /");
      builder.append(other.getStat().getCurrentMaxHp());

      builder.append(" || MP : ");
      builder.append(other.getStat().getMp());
      builder.append(" /");
      builder.append(other.getStat().getCurrentMaxMp());

      builder.append(" || WATK : ");
      builder.append(other.getStat().getTotalWatk());
      builder.append(" || MATK : ");
      builder.append(other.getStat().getTotalMagic());
      builder.append(" || MAXDAMAGE : ");
      builder.append(other.getStat().getCurrentMaxBaseDamage());
      builder.append(" || DAMAGE% : ");
      builder.append(other.getStat().dam_r);
      builder.append(" || BOSSDAMAGE% : ");
      builder.append(other.getStat().bossdam_r);

      builder.append(" || STR : ");
      builder.append(other.getStat().getStr());
      builder.append(" || DEX : ");
      builder.append(other.getStat().getDex());
      builder.append(" || INT : ");
      builder.append(other.getStat().getInt());
      builder.append(" || LUK : ");
      builder.append(other.getStat().getLuk());

      builder.append(" || Total STR : ");
      builder.append(other.getStat().getTotalStr());
      builder.append(" || Total DEX : ");
      builder.append(other.getStat().getTotalDex());
      builder.append(" || Total INT : ");
      builder.append(other.getStat().getTotalInt());
      builder.append(" || Total LUK : ");
      builder.append(other.getStat().getTotalLuk());

      builder.append(" || EXP : ");
      builder.append(other.getExp());

      builder.append(" || hasParty : ");
      builder.append(other.getParty() != null);

      builder.append(" || hasTrade: ");
      builder.append(other.getTrade() != null);
      /*
       * builder.append(" || Latency: ");
       * builder.append(other.getClient().getLatency()); builder.append(
       * " || PING: "); builder.append(other.getClient().getLastPing());
       */
      builder.append(" || PONG: ");
      builder.append(other.getClient().getLastPong());
      builder.append(" || remoteAddress: ");

      other.getClient().DebugMessage(builder);

      c.getPlayer().dropMessage(6, builder.toString());
      return 1;
    }
  }

  public static class WhosThere extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      StringBuilder builder = new StringBuilder("Players on Map: ");
      for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
        if (builder.length() > 150) { // wild guess :o
          builder.setLength(builder.length() - 2);
          c.getPlayer().dropMessage(6, builder.toString());
          builder = new StringBuilder();
        }
        builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
        builder.append(", ");
      }
      builder.setLength(builder.length() - 2);
      c.getPlayer().dropMessage(6, builder.toString());
      return 1;
    }
  }

  public static class Cheaters extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      List<CheaterData> cheaters = World.getCheaters();
      if (cheaters.isEmpty()) {
        c.getPlayer().dropMessage(6, "There are no cheaters at the moment.");
        return 0;
      }
      for (int x = cheaters.size() - 1; x >= 0; x--) {
        CheaterData cheater = cheaters.get(x);
        c.getPlayer().dropMessage(6, cheater.getInfo());
      }
      return 1;
    }
  }

  public static class Reports extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      List<CheaterData> cheaters = World.getReports();
      if (cheaters.isEmpty()) {
        c.getPlayer().dropMessage(6, "There are no reports at the moment.");
        return 0;
      }
      for (int x = cheaters.size() - 1; x >= 0; x--) {
        CheaterData cheater = cheaters.get(x);
        c.getPlayer().dropMessage(6, cheater.getInfo());
      }
      return 1;
    }
  }

  public static class ClearReport extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 3) {
        StringBuilder ret = new StringBuilder("report [ign] [all/");
        for (ReportType type : ReportType.values()) {
          ret.append(type.theId).append('/');
        }
        ret.setLength(ret.length() - 1);
        c.getPlayer().dropMessage(6, ret.append(']').toString());
        return 0;
      }
      MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      if (victim == null) {
        c.getPlayer().dropMessage(5, "Does not exist");
        return 0;
      }
      ReportType type = ReportType.getByString(splitted[2]);
      if (type != null) {
        victim.clearReports(type);
      } else {
        victim.clearReports();
      }
      c.getPlayer().dropMessage(5, "Done.");
      return 1;
    }
  }

  public static class Connected extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      java.util.Map<Integer, Integer> connected = World.getConnected();
      StringBuilder conStr = new StringBuilder("Connected Clients: ");
      boolean first = true;
      for (int i : connected.keySet()) {
        if (!first) {
          conStr.append(", ");
        } else {
          first = false;
        }
        if (i == 0) {
          conStr.append("Total: ");
          conStr.append(connected.get(i));
        } else {
          conStr.append("Channel");
          conStr.append(i);
          conStr.append(": ");
          conStr.append(connected.get(i));
        }
      }
      c.getPlayer().dropMessage(6, conStr.toString());
      return 1;
    }
  }

  public static class ResetQuest extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleQuest.getInstance(Integer.parseInt(splitted[1])).forfeit(c.getPlayer());
      return 1;
    }


  }

  public static class StartQuest extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleQuest.getInstance(Integer.parseInt(splitted[1])).start(c.getPlayer(), Integer.parseInt(splitted[2]));
      return 1;
    }
  }

  public static class CompleteQuest extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleQuest.getInstance(Integer.parseInt(splitted[1])).complete(c.getPlayer(), Integer.parseInt(splitted[2]),
          Integer.parseInt(splitted[3]));
      return 1;
    }
  }

  public static class FStartQuest extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleQuest.getInstance(Integer.parseInt(splitted[1])).forceStart(c.getPlayer(),
          Integer.parseInt(splitted[2]), splitted.length >= 4 ? splitted[3] : null);
      return 1;
    }
  }

  public static class FCompleteQuest extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleQuest.getInstance(Integer.parseInt(splitted[1])).forceComplete(c.getPlayer(),
          Integer.parseInt(splitted[2]));
      return 1;
    }
  }

  public static class FStartOther extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleQuest.getInstance(Integer.parseInt(splitted[2])).forceStart(
          c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]),
          Integer.parseInt(splitted[3]), splitted.length >= 4 ? splitted[4] : null);
      return 1;
    }
  }

  public static class FCompleteOther extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleQuest.getInstance(Integer.parseInt(splitted[2])).forceComplete(
          c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]),
          Integer.parseInt(splitted[3]));
      return 1;
    }
  }

  public static class NearestPortal extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MaplePortal portal = c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition());
      c.getPlayer().dropMessage(6,
          portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());

      return 1;
    }
  }

  public static class SpawnDebug extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().dropMessage(6, c.getPlayer().getMap().spawnDebug());
      return 1;
    }
  }

  public static class Threads extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      Thread[] threads = new Thread[Thread.activeCount()];
      Thread.enumerate(threads);
      String filter = "";
      if (splitted.length > 1) {
        filter = splitted[1];
      }
      for (int i = 0; i < threads.length; i++) {
        String tstring = threads[i].toString();
        if (tstring.toLowerCase().indexOf(filter.toLowerCase()) > -1) {
          c.getPlayer().dropMessage(6, i + ": " + tstring);
        }
      }
      return 1;
    }
  }

  public static class ShowTrace extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        throw new IllegalArgumentException();
      }
      Thread[] threads = new Thread[Thread.activeCount()];
      Thread.enumerate(threads);
      Thread t = threads[Integer.parseInt(splitted[1])];
      c.getPlayer().dropMessage(6, t.toString() + ":");
      for (StackTraceElement elem : t.getStackTrace()) {
        c.getPlayer().dropMessage(6, elem.toString());
      }
      return 1;
    }
  }

  public static class FakeRelog extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleCharacter player = c.getPlayer();
      c.getSession().write(MaplePacketCreator.getCharInfo(player));
      player.sendSkills();
      player.getMap().removePlayer(player);
      player.getMap().addPlayer(player);
      return 1;
    }
  }

  public static class ToggleOffense extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      try {
        CheatingOffense co = CheatingOffense.valueOf(splitted[1]);
        co.setEnabled(!co.isEnabled());
      } catch (IllegalArgumentException iae) {
        c.getPlayer().dropMessage(6, "Offense " + splitted[1] + " not found");
      }
      return 1;
    }
  }

  public static class TDrops extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().toggleDrops();
      return 1;
    }
  }

  public static class TMegaphone extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      World.toggleMegaphoneMuteState();
      c.getPlayer().dropMessage(6,
          "Megaphone state : " + (c.getChannelServer().getMegaphoneMuteState() ? "Enabled" : "Disabled"));
      return 1;
    }
  }

  public static class SReactor extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleReactorStats reactorSt = MapleReactorFactory.getReactor(Integer.parseInt(splitted[1]));
      MapleReactor reactor = new MapleReactor(reactorSt, Integer.parseInt(splitted[1]));
      reactor.setDelay(-1);
      reactor.setPosition(c.getPlayer().getPosition());
      c.getPlayer().getMap().spawnReactor(reactor);
      return 1;
    }
  }

  public static class HReactor extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().getReactorByOid(Integer.parseInt(splitted[1])).hitReactor(c);
      return 1;
    }
  }

  public static class DReactor extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(),
          Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
      if (splitted[1].equals("all")) {
        for (MapleMapObject reactorL : reactors) {
          MapleReactor reactor2l = (MapleReactor) reactorL;
          c.getPlayer().getMap().destroyReactor(reactor2l.getObjectId());
        }
      } else {
        c.getPlayer().getMap().destroyReactor(Integer.parseInt(splitted[1]));
      }
      return 1;
    }
  }

  public static class ResetReactor extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().resetReactors();
      return 1;
    }
  }

  public static class SetReactor extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().setReactorState(Byte.parseByte(splitted[1]));
      return 1;
    }
  }

  public static class cleardrops extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().dropMessage(5, "Cleared " + c.getPlayer().getMap().getNumItems() + " drops");
      c.getPlayer().getMap().removeDrops();
      return 1;
    }
  }

  public static class ExpRate extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length > 1) {
        final int rate = Integer.parseInt(splitted[1]);
        if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
          for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.setExpRate(rate);
          }
        } else {
          c.getChannelServer().setExpRate(rate);
        }
        c.getPlayer().dropMessage(6, "Exprate has been changed to " + rate + "x");
      } else {
        c.getPlayer().dropMessage(6, "Syntax: !exprate <number> [all]");
      }
      return 1;
    }
  }

  public static class DropRate extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length > 1) {
        final int rate = Integer.parseInt(splitted[1]);
        if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
          for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.setDropRate(rate);
          }
        } else {
          c.getChannelServer().setDropRate(rate);
        }
        c.getPlayer().dropMessage(6, "Drop Rate has been changed to " + rate + "x");
      } else {
        c.getPlayer().dropMessage(6, "Syntax: !droprate <number> [all]");
      }
      return 1;
    }
  }

  public static class MesoRate extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length > 1) {
        final int rate = Integer.parseInt(splitted[1]);
        if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
          for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.setMesoRate(rate);
          }
        } else {
          c.getChannelServer().setMesoRate(rate);
        }
        c.getPlayer().dropMessage(6, "Meso Rate has been changed to " + rate + "x");
      } else {
        c.getPlayer().dropMessage(6, "Syntax: !mesorate <number> [all]");
      }
      return 1;
    }
  }

  public static class CashRate extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length > 1) {
        final int rate = Integer.parseInt(splitted[1]);
        if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
          for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.setCashRate(rate);
          }
        } else {
          c.getChannelServer().setCashRate(rate);
        }
        c.getPlayer().dropMessage(6, "Cash Rate has been changed to " + rate + "x");
      } else {
        c.getPlayer().dropMessage(6, "Syntax: !cashrate <number> [all]");
      }
      return 1;
    }
  }

  public static class ListSquads extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (Entry<String, MapleSquad> squads : c.getChannelServer().getAllSquads().entrySet()) {
        c.getPlayer().dropMessage(5,
            "TYPE: " + squads.getKey() + ", Leader: " + squads.getValue().getLeader().getName()
                + ", status: " + squads.getValue().getStatus() + ", numMembers: "
                + squads.getValue().getSquadSize() + ", numBanned: "
                + squads.getValue().getBannedMemberSize());
      }
      return 1;
    }
  }

  public static class ClearSquads extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final Collection<MapleSquad> squadz = new ArrayList<MapleSquad>(
          c.getChannelServer().getAllSquads().values());
      for (MapleSquad squads : squadz) {
        squads.clear();
      }
      return 1;
    }
  }

  public static class SetInstanceProperty extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      EventManager em = c.getChannelServer().getEventSM().getEventManager(splitted[1]);
      if (em == null || em.getInstances().size() <= 0) {
        c.getPlayer().dropMessage(5, "none");
      } else {
        em.setProperty(splitted[2], splitted[3]);
        for (EventInstanceManager eim : em.getInstances()) {
          eim.setProperty(splitted[2], splitted[3]);
        }
      }
      return 1;
    }
  }

  public static class ListInstanceProperty extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      EventManager em = c.getChannelServer().getEventSM().getEventManager(splitted[1]);
      if (em == null || em.getInstances().size() <= 0) {
        c.getPlayer().dropMessage(5, "none");
      } else {
        for (EventInstanceManager eim : em.getInstances()) {
          c.getPlayer().dropMessage(5, "Event " + eim.getName() + ", eventManager: " + em.getName()
              + " iprops: " + eim.getProperty(splitted[2]) + ", eprops: " + em.getProperty(splitted[2]));
        }
      }
      return 1;
    }
  }

  public static class ListInstances extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      EventManager em = c.getChannelServer().getEventSM().getEventManager(StringUtil.joinStringFrom(splitted, 1));
      if (em == null || em.getInstances().size() <= 0) {
        c.getPlayer().dropMessage(5, "none");
      } else {
        for (EventInstanceManager eim : em.getInstances()) {
          c.getPlayer().dropMessage(5, "Event " + eim.getName() + ", charSize: " + eim.getPlayers().size()
              + ", dcedSize: " + eim.getDisconnected().size() + ", mobSize: " + eim.getMobs().size()
              + ", eventManager: " + em.getName() + ", timeLeft: " + eim.getTimeLeft() + ", iprops: "
              + eim.getProperties().toString() + ", eprops: " + em.getProperties().toString());
        }
      }
      return 1;
    }
  }

  public static class LeaveInstance extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (c.getPlayer().getEventInstance() == null) {
        c.getPlayer().dropMessage(5, "You are not in one");
      } else {
        c.getPlayer().getEventInstance().unregisterPlayer(c.getPlayer());
      }
      return 1;
    }
  }

  public static class StartInstance extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (c.getPlayer().getEventInstance() != null) {
        c.getPlayer().dropMessage(5, "You are in one");
      } else if (splitted.length > 2) {
        EventManager em = c.getChannelServer().getEventSM().getEventManager(splitted[1]);
        if (em == null || em.getInstance(splitted[2]) == null) {
          c.getPlayer().dropMessage(5, "Not exist");
        } else {
          em.getInstance(splitted[2]).registerPlayer(c.getPlayer());
        }
      } else {
        c.getPlayer().dropMessage(5, "!startinstance [eventmanager] [eventinstance]");
      }
      return 1;

    }
  }

  public static class eventinstance extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (c.getPlayer().getEventInstance() == null) {
        c.getPlayer().dropMessage(5, "none");
      } else {
        EventInstanceManager eim = c.getPlayer().getEventInstance();
        c.getPlayer().dropMessage(5,
            "Event " + eim.getName() + ", charSize: " + eim.getPlayers().size() + ", dcedSize: "
                + eim.getDisconnected().size() + ", mobSize: " + eim.getMobs().size()
                + ", eventManager: " + eim.getEventManager().getName() + ", timeLeft: "
                + eim.getTimeLeft() + ", iprops: " + eim.getProperties().toString() + ", eprops: "
                + eim.getEventManager().getProperties().toString());
      }
      return 1;
    }
  }

  public static class Uptime extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().dropMessage(6, "Server has been up for "
          + StringUtil.getReadableMillis(ChannelServer.serverStartTime, System.currentTimeMillis()));
      return 1;
    }
  }

  public static class DCAll extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      int range = -1;
      if (splitted[1].equals("m")) {
        range = 0;
      } else if (splitted[1].equals("c")) {
        range = 1;
      } else if (splitted[1].equals("w")) {
        range = 2;
      }
      if (range == -1) {
        range = 1;
      }
      if (range == 0) {
        c.getPlayer().getMap().disconnectAll();
      } else if (range == 1) {
        c.getChannelServer().getPlayerStorage().disconnectAll(true);
      } else if (range == 2) {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
          cserv.getPlayerStorage().disconnectAll(true);
        }
      }
      return 1;
    }
  }

  public static class GoTo extends CommandExecute {

    private static final HashMap<String, Integer> gotomaps = new HashMap<String, Integer>();

    static {
      gotomaps.put("gmmap", 180000000);
      gotomaps.put("southperry", 2000000);
      gotomaps.put("amherst", 1010000);
      gotomaps.put("henesys", 100000000);
      gotomaps.put("ellinia", 101000000);
      gotomaps.put("perion", 102000000);
      gotomaps.put("kerning", 103000000);
      gotomaps.put("lithharbour", 104000000);
      gotomaps.put("sleepywood", 105040300);
      gotomaps.put("florina", 110000000);
      gotomaps.put("orbis", 200000000);
      gotomaps.put("happyville", 209000000);
      gotomaps.put("elnath", 211000000);
      gotomaps.put("ludibrium", 220000000);
      gotomaps.put("aquaroad", 230000000);
      gotomaps.put("leafre", 240000000);
      gotomaps.put("mulung", 250000000);
      gotomaps.put("herbtown", 251000000);
      gotomaps.put("omegasector", 221000000);
      gotomaps.put("koreanfolktown", 222000000);
      gotomaps.put("newleafcity", 600000000);
      gotomaps.put("sharenian", 990000000);
      gotomaps.put("pianus", 230040420);
      gotomaps.put("horntail", 240060200);
      gotomaps.put("chorntail", 240060201);
      gotomaps.put("mushmom", 100000005);
      gotomaps.put("griffey", 240020101);
      gotomaps.put("manon", 240020401);
      gotomaps.put("zakum", 280030000);
      gotomaps.put("czakum", 280030001);
      gotomaps.put("papulatus", 220080001);
      gotomaps.put("showatown", 801000000);
      gotomaps.put("zipangu", 800000000);
      gotomaps.put("ariant", 260000100);
      gotomaps.put("nautilus", 120000000);
      gotomaps.put("boatquay", 541000000);
      gotomaps.put("malaysia", 550000000);
      gotomaps.put("taiwan", 740000000);
      gotomaps.put("thailand", 500000000);
      gotomaps.put("erev", 130000000);
      gotomaps.put("ellinforest", 300000000);
      gotomaps.put("kampung", 551000000);
      gotomaps.put("singapore", 540000000);
      gotomaps.put("amoria", 680000000);
      gotomaps.put("timetemple", 270000000);
      gotomaps.put("pinkbean", 270050100);
      gotomaps.put("peachblossom", 700000000);
      gotomaps.put("fm", 910000000);
      gotomaps.put("freemarket", 910000000);
      gotomaps.put("oxquiz", 109020001);
      gotomaps.put("ola", 109030101);
      gotomaps.put("fitness", 109040000);
      gotomaps.put("snowball", 109060000);
      gotomaps.put("cashmap", 741010200);
      gotomaps.put("golden", 950100000);
      gotomaps.put("phantom", 610010000);
      gotomaps.put("cwk", 610030000);
      gotomaps.put("rien", 140000000);
    }

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(6, "Syntax: !goto <mapname>");
      } else {
        if (gotomaps.containsKey(splitted[1])) {
          MapleMap target = c.getChannelServer().getMapFactory().getMap(gotomaps.get(splitted[1]));
          MaplePortal targetPortal = target.getPortal(0);
          c.getPlayer().changeMap(target, targetPortal);
        } else {
          if (splitted[1].equals("locations")) {
            c.getPlayer().dropMessage(6, "Use !goto <location>. Locations are as follows:");
            StringBuilder sb = new StringBuilder();
            for (String s : gotomaps.keySet()) {
              sb.append(s).append(", ");
            }
            c.getPlayer().dropMessage(6, sb.substring(0, sb.length() - 2));
          } else {
            c.getPlayer().dropMessage(6,
                "Invalid command syntax - Use !goto <location>. For a list of locations, use !goto locations.");
          }
        }
      }
      return 1;
    }
  }

  public static class KillAll extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      double range = Double.POSITIVE_INFINITY;

      if (splitted.length > 1) {
        int irange = Integer.parseInt(splitted[1]);
        if (splitted.length <= 2) {
          range = irange * irange;
        } else {
          map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
        }
      }
      MapleMonster mob;
      for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range,
          Arrays.asList(MapleMapObjectType.MONSTER))) {
        mob = (MapleMonster) monstermo;
        map.killMonster(mob, c.getPlayer(), true, false, (byte) 1);
      }
      return 1;
    }
  }

  public static class ResetMobs extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().killAllMonsters(false);
      return 1;
    }
  }

  public static class KillMonster extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      double range = Double.POSITIVE_INFINITY;
      MapleMonster mob;
      for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range,
          Arrays.asList(MapleMapObjectType.MONSTER))) {
        mob = (MapleMonster) monstermo;
        if (mob.getId() == Integer.parseInt(splitted[1])) {
          mob.damage(c.getPlayer(), mob.getHp(), false);
        }
      }
      return 1;
    }
  }

  public static class KillMonsterByOID extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      int targetId = Integer.parseInt(splitted[1]);
      MapleMonster monster = map.getMonsterByOid(targetId);
      if (monster != null) {
        map.killMonster(monster, c.getPlayer(), false, false, (byte) 1);
      }
      return 1;
    }
  }

  public static class HitMonsterByOID extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      int targetId = Integer.parseInt(splitted[1]);
      int damage = Integer.parseInt(splitted[2]);
      MapleMonster monster = map.getMonsterByOid(targetId);
      if (monster != null) {
        map.broadcastMessage(MobPacket.damageMonster(targetId, damage));
        monster.damage(c.getPlayer(), damage, false);
      }
      return 1;
    }
  }

  public static class HitAll extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      double range = Double.POSITIVE_INFINITY;
      if (splitted.length > 1) {
        int irange = Integer.parseInt(splitted[1]);
        if (splitted.length <= 2) {
          range = irange * irange;
        } else {
          map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
        }
      }
      int damage = Integer.parseInt(splitted[1]);
      MapleMonster mob;
      for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range,
          Arrays.asList(MapleMapObjectType.MONSTER))) {
        mob = (MapleMonster) monstermo;
        map.broadcastMessage(MobPacket.damageMonster(mob.getObjectId(), damage));
        mob.damage(c.getPlayer(), damage, false);
      }
      return 1;
    }
  }

  public static class HitMonster extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      double range = Double.POSITIVE_INFINITY;
      int damage = Integer.parseInt(splitted[1]);
      MapleMonster mob;
      for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range,
          Arrays.asList(MapleMapObjectType.MONSTER))) {
        mob = (MapleMonster) monstermo;
        if (mob.getId() == Integer.parseInt(splitted[2])) {
          map.broadcastMessage(MobPacket.damageMonster(mob.getObjectId(), damage));
          mob.damage(c.getPlayer(), damage, false);
        }
      }
      return 1;
    }
  }

  public static class KillAllDrops extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      double range = Double.POSITIVE_INFINITY;

      if (splitted.length > 1) {
        // && !splitted[0].equals("!killmonster") &&
        // !splitted[0].equals("!hitmonster") &&
        // !splitted[0].equals("!hitmonsterbyoid") &&
        // !splitted[0].equals("!killmonsterbyoid")) {
        int irange = Integer.parseInt(splitted[1]);
        if (splitted.length <= 2) {
          range = irange * irange;
        } else {
          map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
        }
      }
      MapleMonster mob;
      for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range,
          Arrays.asList(MapleMapObjectType.MONSTER))) {
        mob = (MapleMonster) monstermo;
        map.killMonster(mob, c.getPlayer(), true, false, (byte) 1);
      }
      return 1;
    }
  }

  public static class KillAllNoSpawn extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      map.killAllMonsters(false);
      return 1;
    }
  }

  public static class MonsterDebug extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMap map = c.getPlayer().getMap();
      double range = Double.POSITIVE_INFINITY;

      if (splitted.length > 1) {
        // && !splitted[0].equals("!killmonster") &&
        // !splitted[0].equals("!hitmonster") &&
        // !splitted[0].equals("!hitmonsterbyoid") &&
        // !splitted[0].equals("!killmonsterbyoid")) {
        int irange = Integer.parseInt(splitted[1]);
        if (splitted.length <= 2) {
          range = irange * irange;
        } else {
          map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
        }
      }
      MapleMonster mob;
      for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range,
          Arrays.asList(MapleMapObjectType.MONSTER))) {
        mob = (MapleMonster) monstermo;
        c.getPlayer().dropMessage(6, "Monster " + mob.toString());
      }
      return 1;
    }
  }

  public static class NPC extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      int npcId = Integer.parseInt(splitted[1]);
      MapleNPC npc = MapleLifeFactory.getNPC(npcId);
      if (npc != null && !npc.getName().equals("MISSINGNO")) {
        npc.setPosition(c.getPlayer().getPosition());
        npc.setCy(c.getPlayer().getPosition().y);
        npc.setRx0(c.getPlayer().getPosition().x);
        npc.setRx1(c.getPlayer().getPosition().x);
        npc.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
        npc.setCustom(true);
        c.getPlayer().getMap().addMapObject(npc);
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
      } else {
        c.getPlayer().dropMessage(6, "You have entered an invalid Npc-Id");
        return 0;
      }
      return 1;
    }
  }

  public static class RemoveNPCs extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().resetNPCs();
      return 1;
    }
  }

  public static class LookNPC extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllNPCsThreadsafe()) {
        MapleNPC reactor2l = (MapleNPC) reactor1l;
        c.getPlayer().dropMessage(5, "NPC: oID: " + reactor2l.getObjectId() + " npcID: " + reactor2l.getId()
            + " Position: " + reactor2l.getPosition().toString() + " Name: " + reactor2l.getName());
      }
      return 1;
    }
  }

  public static class LookReactor extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllReactorsThreadsafe()) {
        MapleReactor reactor2l = (MapleReactor) reactor1l;
        c.getPlayer().dropMessage(5,
            "Reactor: oID: " + reactor2l.getObjectId() + " reactorID: " + reactor2l.getReactorId()
                + " Position: " + reactor2l.getPosition().toString() + " State: " + reactor2l.getState()
                + " Name: " + reactor2l.getName());
      }
      return 1;
    }
  }

  public static class LookPortals extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (MaplePortal portal : c.getPlayer().getMap().getPortals()) {
        c.getPlayer().dropMessage(5,
            "Portal: ID: " + portal.getId() + " script: " + portal.getScriptName() + " name: "
                + portal.getName() + " pos: " + portal.getPosition().x + "," + portal.getPosition().y
                + " target: " + portal.getTargetMapId() + " / " + portal.getTarget());
      }
      return 1;
    }
  }

  public static class MakePNPC extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      try {
        c.getPlayer().dropMessage(6, "Making playerNPC...");
        MapleCharacter chhr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
        if (chhr == null) {
          c.getPlayer().dropMessage(6, splitted[1] + " is not online");
          return 0;
        }
        PlayerNPC npc = new PlayerNPC(chhr, Integer.parseInt(splitted[2]), c.getPlayer().getMap(),
            c.getPlayer());
        npc.addToServer();
        c.getPlayer().dropMessage(6, "Done");
      } catch (Exception e) {
        c.getPlayer().dropMessage(6, "NPC failed... : " + e.getMessage());
        e.printStackTrace();
      }
      return 1;
    }
  }

  public static class MakeOfflineP extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      try {
        c.getPlayer().dropMessage(6, "Making playerNPC...");
        MapleClient cs = new MapleClient(null, null, new MockIOSession());
        MapleCharacter chhr = MapleCharacter.loadCharFromDB(MapleCharacterUtil.getIdByName(splitted[1]), cs,
            false);
        if (chhr == null) {
          c.getPlayer().dropMessage(6, splitted[1] + " does not exist");
          return 0;
        }
        PlayerNPC npc = new PlayerNPC(chhr, Integer.parseInt(splitted[2]), c.getPlayer().getMap(),
            c.getPlayer());
        npc.addToServer();
        c.getPlayer().dropMessage(6, "Done");
      } catch (Exception e) {
        c.getPlayer().dropMessage(6, "NPC failed... : " + e.getMessage());
        e.printStackTrace();
      }
      return 1;
    }
  }

  public static class DestroyPNPC extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      try {
        c.getPlayer().dropMessage(6, "Destroying playerNPC...");
        final MapleNPC npc = c.getPlayer().getMap().getNPCByOid(Integer.parseInt(splitted[1]));
        if (npc instanceof PlayerNPC) {
          ((PlayerNPC) npc).destroy(true);
          c.getPlayer().dropMessage(6, "Done");
        } else {
          c.getPlayer().dropMessage(6, "!destroypnpc [objectid]");
        }
      } catch (Exception e) {
        c.getPlayer().dropMessage(6, "NPC failed... : " + e.getMessage());
        e.printStackTrace();
      }
      return 1;
    }
  }

  public static class MyNPCPos extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      Point pos = c.getPlayer().getPosition();
      c.getPlayer().dropMessage(6, "X: " + pos.x + " | Y: " + pos.y + " | RX0: " + (pos.x) + " | RX1: " + (pos.x)
          + " | FH: " + c.getPlayer().getFH());
      return 1;
    }
  }

  public static class Notice extends CommandExecute {

    private static int getNoticeType(String typestring) {
      if (typestring.equals("n")) {
        return 0;
      } else if (typestring.equals("p")) {
        return 1;
      } else if (typestring.equals("l")) {
        return 2;
      } else if (typestring.equals("nv")) {
        return 5;
      } else if (typestring.equals("v")) {
        return 5;
      } else if (typestring.equals("b")) {
        return 6;
      }
      return -1;
    }

    @Override
    public int execute(MapleClient c, String[] splitted) {
      int joinmod = 1;
      int range = -1;
      if (splitted[1].equals("m")) {
        range = 0;
      } else if (splitted[1].equals("c")) {
        range = 1;
      } else if (splitted[1].equals("w")) {
        range = 2;
      }

      int tfrom = 2;
      if (range == -1) {
        range = 2;
        tfrom = 1;
      }
      int type = getNoticeType(splitted[tfrom]);
      if (type == -1) {
        type = 0;
        joinmod = 0;
      }
      StringBuilder sb = new StringBuilder();
      if (splitted[tfrom].equals("nv")) {
        sb.append("[Notice]");
      } else {
        sb.append("");
      }
      joinmod += tfrom;
      sb.append(StringUtil.joinStringFrom(splitted, joinmod));

      byte[] packet = MaplePacketCreator.serverNotice(type, sb.toString());
      if (range == 0) {
        c.getPlayer().getMap().broadcastMessage(packet);
      } else if (range == 1) {
        ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
      } else if (range == 2) {
        World.Broadcast.broadcastMessage(packet);
      }
      return 1;
    }
  }

  public static class Yellow extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      int range = -1;
      if (splitted[1].equals("m")) {
        range = 0;
      } else if (splitted[1].equals("c")) {
        range = 1;
      } else if (splitted[1].equals("w")) {
        range = 2;
      }
      if (range == -1) {
        range = 2;
      }
      byte[] packet = MaplePacketCreator
          .yellowChat((splitted[0].equals("!y") ? ("[" + c.getPlayer().getName() + "] ") : "")
              + StringUtil.joinStringFrom(splitted, 2));
      if (range == 0) {
        c.getPlayer().getMap().broadcastMessage(packet);
      } else if (range == 1) {
        ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
      } else if (range == 2) {
        World.Broadcast.broadcastMessage(packet);
      }
      return 1;
    }
  }

  public static class Y extends Yellow {
  }

  public static class ReloadOps extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      // SendPacketOpcode.reloadValues();
      RecvPacketOpcode.reloadValues();
      return 1;
    }
  }

  public static class ReloadDrops extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleMonsterInformationProvider.getInstance().clearDrops();
      ReactorScriptManager.getInstance().clearDrops();
      return 1;
    }
  }

  public static class ReloadPortal extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      PortalScriptManager.getInstance().clearScripts();
      return 1;
    }
  }

  public static class ReloadShops extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleShopFactory.getInstance().clear();
      return 1;
    }
  }

  public static class ReloadEvents extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (ChannelServer instance : ChannelServer.getAllInstances()) {
        instance.reloadEvents();
      }
      return 1;
    }
  }

  public static class ReloadQuests extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleQuest.clearQuests();
      return 1;
    }
  }

  public static class Find extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length == 1) {
        c.getPlayer().dropMessage(6, splitted[0] + ": <NPC> <MOB> <ITEM> <MAP> <SKILL>");
      } else if (splitted.length == 2) {
        c.getPlayer().dropMessage(6, "Provide something to search.");
      } else {
        String type = splitted[1];
        String search = StringUtil.joinStringFrom(splitted, 2);
        MapleData data = null;
        MapleDataProvider dataProvider = MapleDataProviderFactory
            .getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/" + "String"));
        c.getPlayer().dropMessage(6, "<<Type: " + type + " | Search: " + search + ">>");

        if (type.equalsIgnoreCase("NPC")) {
          List<String> retNpcs = new ArrayList<String>();
          data = dataProvider.getData("Npc.img");
          List<Pair<Integer, String>> npcPairList = new LinkedList<Pair<Integer, String>>();
          for (MapleData npcIdData : data.getChildren()) {
            npcPairList.add(new Pair<Integer, String>(Integer.parseInt(npcIdData.getName()),
                MapleDataTool.getString(npcIdData.getChildByPath("name"), "NO-NAME")));
          }
          for (Pair<Integer, String> npcPair : npcPairList) {
            if (npcPair.getRight().toLowerCase().contains(search.toLowerCase())) {
              retNpcs.add(npcPair.getLeft() + " - " + npcPair.getRight());
            }
          }
          if (retNpcs != null && retNpcs.size() > 0) {
            for (String singleRetNpc : retNpcs) {
              c.getPlayer().dropMessage(6, singleRetNpc);
            }
          } else {
            c.getPlayer().dropMessage(6, "No NPC's Found");
          }

        } else if (type.equalsIgnoreCase("MAP")) {
          List<String> retMaps = new ArrayList<String>();
          data = dataProvider.getData("Map.img");
          List<Pair<Integer, String>> mapPairList = new LinkedList<Pair<Integer, String>>();
          for (MapleData mapAreaData : data.getChildren()) {
            for (MapleData mapIdData : mapAreaData.getChildren()) {
              mapPairList
                  .add(new Pair<Integer, String>(Integer.parseInt(mapIdData.getName()),
                      MapleDataTool.getString(mapIdData.getChildByPath("streetName"),
                          "NO-NAME") + " - "
                          + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "NO-NAME")));
            }
          }
          for (Pair<Integer, String> mapPair : mapPairList) {
            if (mapPair.getRight().toLowerCase().contains(search.toLowerCase())) {
              retMaps.add(mapPair.getLeft() + " - " + mapPair.getRight());
            }
          }
          if (retMaps != null && retMaps.size() > 0) {
            for (String singleRetMap : retMaps) {
              c.getPlayer().dropMessage(6, singleRetMap);
            }
          } else {
            c.getPlayer().dropMessage(6, "No Maps Found");
          }
        } else if (type.equalsIgnoreCase("MOB")) {
          List<String> retMobs = new ArrayList<String>();
          data = dataProvider.getData("Mob.img");
          List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();
          for (MapleData mobIdData : data.getChildren()) {
            mobPairList.add(new Pair<Integer, String>(Integer.parseInt(mobIdData.getName()),
                MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
          }
          for (Pair<Integer, String> mobPair : mobPairList) {
            if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
              retMobs.add(mobPair.getLeft() + " - " + mobPair.getRight());
            }
          }
          if (retMobs != null && retMobs.size() > 0) {
            for (String singleRetMob : retMobs) {
              c.getPlayer().dropMessage(6, singleRetMob);
            }
          } else {
            c.getPlayer().dropMessage(6, "No Mob's Found");
          }

        } else if (type.equalsIgnoreCase("ITEM")) {
          List<String> retItems = new ArrayList<String>();
          for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
            if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
              retItems.add(itemPair.getLeft() + " - " + itemPair.getRight());
            }
          }
          if (retItems != null && retItems.size() > 0) {
            for (String singleRetItem : retItems) {
              c.getPlayer().dropMessage(6, singleRetItem);
              try {
                File file = new File("command.txt");
                if (!file.exists()) {
                  file.createNewFile();
                }
                String finalStr = singleRetItem + System.lineSeparator();
                Files.write(file.toPath(), finalStr.getBytes(), StandardOpenOption.APPEND);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          } else {
            c.getPlayer().dropMessage(6, "No Item's Found");
          }

        } else if (type.equalsIgnoreCase("SKILL")) {
          List<String> retSkills = new ArrayList<String>();
          data = dataProvider.getData("Skill.img");
          List<Pair<Integer, String>> skillPairList = new LinkedList<Pair<Integer, String>>();
          for (MapleData skillIdData : data.getChildren()) {
            skillPairList.add(new Pair<Integer, String>(Integer.parseInt(skillIdData.getName()),
                MapleDataTool.getString(skillIdData.getChildByPath("name"), "NO-NAME")));
          }
          for (Pair<Integer, String> skillPair : skillPairList) {
            if (skillPair.getRight().toLowerCase().contains(search.toLowerCase())) {
              retSkills.add(skillPair.getLeft() + " - " + skillPair.getRight());
            }
          }
          if (retSkills != null && retSkills.size() > 0) {
            for (String singleRetSkill : retSkills) {
              c.getPlayer().dropMessage(6, singleRetSkill);
            }
          } else {
            c.getPlayer().dropMessage(6, "No Skills Found");
          }
        } else if (type.equalsIgnoreCase("QUEST")) {
          MapleDataProvider questProvider = MapleDataProviderFactory
              .getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/" + "Quest"));
          data = questProvider.getData("QuestInfo.img");
          for (MapleData node : data.getChildren()) {
            String name = String.valueOf(node.getChildByPath("name").getData());
            if (name.contains(search)) {
              c.getPlayer().dropMessage(6, node.getName() + " - " + name);
            }
          }
        } else {
          c.getPlayer().dropMessage(6, "Sorry, that search call is unavailable");
        }
      }
      return 1;
    }
  }

  public static class ID extends Find {
  }

  public static class LookUp extends Find {
  }

  public static class Search extends Find {
  }

  public static class ServerMessage extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
      String outputMessage = StringUtil.joinStringFrom(splitted, 1);
      for (ChannelServer cserv : cservs) {
        cserv.setServerMessage(outputMessage);
      }
      return 1;
    }
  }

  public static class ShutdownTime extends AdminCommand.Shutdown {

    private static ScheduledFuture<?> ts = null;
    private int minutesLeft = 0;

    @Override
    public int execute(MapleClient c, String[] splitted) {
      this.minutesLeft = Integer.parseInt(splitted[1]);
      c.getPlayer().dropMessage(6, "Shutting down... in " + this.minutesLeft + " minutes");
      if (ts == null && (t == null || !t.isAlive())) {
        t = new Thread(ShutdownServer.getInstance());
        ts = Timer.EventTimer.getInstance().register(new Runnable() {

          public void run() {
            if (AdminCommand.ShutdownTime.this.minutesLeft == 0) {
              ShutdownServer.getInstance().shutdown();
              AdminCommand.Shutdown.t.start();
              AdminCommand.ShutdownTime.ts.cancel(false);
              return;
            }
            World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0,
                "The server will shutdown in " + AdminCommand.ShutdownTime.this.minutesLeft
                    + " minutes. Please log off safely."));
            AdminCommand.ShutdownTime.this.minutesLeft--;
          }
        }, 60000L);
      } else {
        c.getPlayer().dropMessage(6,
            "A shutdown thread is already in progress or shutdown has not been done. Please wait.");
      }
      return 1;
    }
  }

  public static class Shutdown extends CommandExecute {

    public static Thread t = null;

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().dropMessage(6, "Shutting down...");
      if (t == null || !t.isAlive()) {
        t = new Thread(ShutdownServer.getInstance());
        ShutdownServer.getInstance().shutdown();
        t.start();
      } else {
        c.getPlayer().dropMessage(6, "A shutdown thread is already in progress. Please wait.");
      }
      return 1;
    }
  }

  public static class ShutdownMerchant extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      for (ChannelServer cserv : ChannelServer.getAllInstances()) {
        cserv.closeAllMerchant();
      }
      return 1;

    }
  }

  public static class Spawn extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final int mid = Integer.parseInt(splitted[1]);
      final int num = Math.min(CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1), 500);

      Long hp = CommandProcessorUtil.getNamedLongArg(splitted, 1, "hp");
      Integer exp = CommandProcessorUtil.getNamedIntArg(splitted, 1, "exp");
      Double php = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "php");
      Double pexp = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "pexp");

      MapleMonster onemob;
      try {
        onemob = MapleLifeFactory.getMonster(mid);
      } catch (RuntimeException e) {
        c.getPlayer().dropMessage(5, "Error: " + e.getMessage());
        return 0;
      }

      long newhp = 0;
      int newexp = 0;
      if (hp != null) {
        newhp = hp.longValue();
      } else if (php != null) {
        newhp = (long) (onemob.getMobMaxHp() * (php.doubleValue() / 100));
      } else {
        newhp = onemob.getMobMaxHp();
      }
      if (exp != null) {
        newexp = exp.intValue();
      } else if (pexp != null) {
        newexp = (int) (onemob.getMobExp() * (pexp.doubleValue() / 100));
      } else {
        newexp = onemob.getMobExp();
      }
      if (newhp < 1) {
        newhp = 1;
      }

      final OverrideMonsterStats overrideStats = new OverrideMonsterStats(newhp, onemob.getMobMaxMp(), newexp,
          false);
      for (int i = 0; i < num; i++) {
        MapleMonster mob = MapleLifeFactory.getMonster(mid);
        mob.setHp(newhp);
        mob.setOverrideStats(overrideStats);
        c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
      }
      return 1;
    }
  }

  public static class Test2 extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getSession().write(PlayerShopPacket.Merchant_Buy_Error(Byte.parseByte(splitted[1])));
      return 1;

    }
  }

  public static class Clock extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().broadcastMessage(
          MaplePacketCreator.getClock(CommandProcessorUtil.getOptionalIntArg(splitted, 1, 60)));
      return 1;
    }
  }

  public static class Packet extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length > 1) {
        c.getSession().write(MaplePacketCreator.getPacketFromHexString(StringUtil.joinStringFrom(splitted, 1)));
      } else {
        c.getPlayer().dropMessage(6, "Please enter packet data!");
      }
      return 1;
    }
  }

  public static class PacketToServer extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length > 1) {
        c.getChannelServer().getServerHandler().messageReceived(c.getSession(),
            (Object) MaplePacketCreator.getPacketFromHexString(StringUtil.joinStringFrom(splitted, 1)));
      } else {
        c.getPlayer().dropMessage(6, "Please enter packet data!");
      }
      return 1;
    }
  }

  public static class Warp extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      // smart player selection
      MapleCharacter victim = null;// =
      // c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      List<String> possibility = new LinkedList<>();
      // HashMap<Integer, String> possibility = new HashMap<Integer,
      // String>();
      // int key = 0;

      StringBuilder sb = new StringBuilder();
      for (ChannelServer ch : ChannelServer.getAllInstances()) {
        for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
          if (chr.getName().toLowerCase().contains(splitted[1].toLowerCase()) && victim == null) {
            victim = chr;
            possibility.add(chr.getName());
          } else if (chr.getName().contains(splitted[1]) && victim != null) {
            // key++;
            possibility.add(chr.getName());
          }
        }
      }
      if (possibility.size() > 1) {
        sb.append("There were more than 1 player found, do !warp : ").append(possibility);
        c.getPlayer().dcolormsg(5, sb.toString());
        // end of smart player selection
      } else {
        if (victim != null) {
          if (splitted.length == 2) {
            c.getPlayer().changeMap(victim.getMap(),
                victim.getMap().findClosestSpawnpoint(victim.getPosition()));
          } else {
            MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory()
                .getMap(Integer.parseInt(splitted[2]));
            victim.changeMap(target, target.getPortal(0));
          }
        } else {
          try {
            victim = c.getPlayer();
            int ch = World.Find.findChannel(splitted[1]);
            if (ch < 0) {
              MapleMap target = c.getChannelServer().getMapFactory()
                  .getMap(Integer.parseInt(splitted[1]));
              c.getPlayer().changeMap(target, target.getPortal(0));
            } else {
              victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
              c.getPlayer().dropMessage(6, "Cross changing channel. Please wait.");
              if (victim.getMapId() != c.getPlayer().getMapId()) {
                final MapleMap mapp = c.getChannelServer().getMapFactory().getMap(victim.getMapId());
                c.getPlayer().changeMap(mapp, mapp.getPortal(0));
              }
              c.getPlayer().changeChannel(ch);
            }
          } catch (Exception e) {
            c.getPlayer().dropMessage(6, "Something went wrong " + e.getMessage());
            return 0;
          }
        }
      }
      return 0;
    }
  }

  public static class warpChHere extends CommandExecute {
    @Override
    public int execute(MapleClient c, String[] splitted) {
      try {
        for (MapleCharacter chr : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
          chr.changeMap(c.getPlayer().getMap(), c.getPlayer().getPosition());
          chr.dcolormsg(5, "You have been warped to the event");
        }
        c.getPlayer().dcolormsg(5, "Every player in your channel have been warped here");
      } catch (Exception e) {
        System.out.println("Something went wrong: " + e);
      }
      return 0;
    }
  }

  public static class WarpMapTo extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      try {
        final MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
        final MapleMap from = c.getPlayer().getMap();
        for (MapleCharacter chr : from.getCharactersThreadsafe()) {
          chr.changeMap(target, target.getPortal(0));
        }
      } catch (Exception e) {
        c.getPlayer().dropMessage(5, "Error: " + e.getMessage());
        return 0; // assume drunk GM
      }
      return 1;
    }
  }

  public static class WarpHere extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
      if (victim != null) {
        victim.changeMap(c.getPlayer().getMap(),
            c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));
      } else {
        int ch = World.Find.findChannel(splitted[1]);
        if (ch < 0) {
          c.getPlayer().dropMessage(5, "Not found.");
          return 0;
        }
        victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
        c.getPlayer().dropMessage(5, "Victim is cross changing channel.");
        victim.dropMessage(5, "Cross changing channel.");
        if (victim.getMapId() != c.getPlayer().getMapId()) {
          final MapleMap mapp = victim.getClient().getChannelServer().getMapFactory()
              .getMap(c.getPlayer().getMapId());
          victim.changeMap(mapp, mapp.getPortal(0));
        }
        victim.changeChannel(c.getChannel());
      }
      return 1;
    }
  }

  public static class LOLCastle extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length != 2) {
        c.getPlayer().dropMessage(6, "Syntax: !lolcastle level (level = 1-5)");
        return 0;
      }
      MapleMap target = c.getChannelServer().getEventSM().getEventManager("lolcastle")
          .getInstance("lolcastle" + splitted[1]).getMapFactory().getMap(990000300, false, false);
      c.getPlayer().changeMap(target, target.getPortal(0));

      return 1;
    }
  }

  public static class Map extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      try {
        MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
        MaplePortal targetPortal = null;
        if (splitted.length > 2) {
          try {
            targetPortal = target.getPortal(Integer.parseInt(splitted[2]));
          } catch (IndexOutOfBoundsException e) {
            // noop, assume the gm didn't know how many portals
            // there are
            c.getPlayer().dropMessage(5, "Invalid portal selected.");
          } catch (NumberFormatException a) {
            // noop, assume that the gm is drunk
          }
        }
        if (targetPortal == null) {
          targetPortal = target.getPortal(0);
        }
        c.getPlayer().changeMap(target, targetPortal);
      } catch (Exception e) {
        c.getPlayer().dropMessage(5, "Error: " + e.getMessage());
        return 0;
      }
      return 1;
    }
  }

  public static class StartProfiling extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      CPUSampler sampler = CPUSampler.getInstance();
      sampler.addIncluded("client");
      sampler.addIncluded("constants"); // or should we do
      // Packages.constants etc.?
      sampler.addIncluded("database");
      sampler.addIncluded("handling");
      sampler.addIncluded("provider");
      sampler.addIncluded("scripting");
      sampler.addIncluded("server");
      sampler.addIncluded("tools");
      sampler.start();
      return 1;
    }
  }

  public static class StopProfiling extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      CPUSampler sampler = CPUSampler.getInstance();
      try {
        String filename = "odinprofile.txt";
        if (splitted.length > 1) {
          filename = splitted[1];
        }
        File file = new File(filename);
        if (file.exists()) {
          c.getPlayer().dropMessage(6, "The entered filename already exists, choose a different one");
          return 0;
        }
        sampler.stop();
        FileWriter fw = new FileWriter(file);
        sampler.save(fw, 1, 10);
        fw.close();
      } catch (IOException e) {
        System.err.println("Error saving profile" + e);
      }
      sampler.reset();
      return 1;
    }
  }

  public static class ReloadMap extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final int mapId = Integer.parseInt(splitted[1]);
      for (ChannelServer cserv : ChannelServer.getAllInstances()) {
        if (cserv.getMapFactory().isMapLoaded(mapId)
            && cserv.getMapFactory().getMap(mapId).getCharactersSize() > 0) {
          c.getPlayer().dropMessage(5, "There exists characters on channel " + cserv.getChannel());
          return 0;
        }
      }
      for (ChannelServer cserv : ChannelServer.getAllInstances()) {
        if (cserv.getMapFactory().isMapLoaded(mapId)) {
          cserv.getMapFactory().removeMap(mapId);
        }
      }
      return 1;
    }
  }

  public static class Respawn extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().respawn(true);
      return 1;
    }
  }

  public static class ResetMap extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      c.getPlayer().getMap().resetFully();
      return 1;
    }
  }

  public static class startHotTime extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final long now = System.currentTimeMillis();
      if (!World.startHotTime()) {
        c.getPlayer().dropMessage(5, "The Hot Time Event was already started and have "
            + World.getHotTimeLeft(now) + " seconds left.");
        return 0;
      }
      c.getPlayer().dropMessage(5,
          "The Hot Time Event was started and will end in " + (World.HOT_TIME_INTERVAL / 1000) + " seconds.");
      return 1;
    }
  }

  public static class endHotTime extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final long now = System.currentTimeMillis();
      if (!World.isHotTimeStarted(now)) {
        c.getPlayer().dropMessage(5, "The Hot Time Event is currently inactive.");
        return 0;
      }
      World.endHotTime();
      c.getPlayer().dropMessage(5, "The Hot Time Event has been stopped.");
      return 1;
    }
  }

  public static class hotTimeStatus extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final long now = System.currentTimeMillis();
      if (!World.isHotTimeStarted(now)) {
        c.getPlayer().dropMessage(5, "The Hot Time Event is currently inactive.");
        return 0;
      }
      c.getPlayer().dropMessage(5,
          "The Hot Time Event was already started and have " + World.getHotTimeLeft(now) + " seconds left.");
      return 1;
    }
  }

  public static class hotTimeList extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      final String list = World.getHotTimeList();
      c.getPlayer().dropMessage(6, "Characters obtained items :");
      c.getPlayer().dropMessage(6, list == null ? "<None>" : list);
      return 1;
    }
  }

  public static class PNPC extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 1) {
        c.getPlayer().dropMessage(6, "!pnpc <npcid>");
        return 0;
      }
      int npcId = Integer.parseInt(splitted[1]);
      MapleNPC npc = MapleLifeFactory.getNPC(npcId);
      if (npc != null && !npc.getName().equals("MISSINGNO")) {
        final int xpos = c.getPlayer().getPosition().x;
        final int ypos = c.getPlayer().getPosition().y;
        final int fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
        npc.setPosition(c.getPlayer().getPosition());
        npc.setCy(ypos);
        npc.setRx0(xpos);
        npc.setRx1(xpos);
        npc.setFh(fh);
        npc.setCustom(true);
        try {
          Connection con = DatabaseConnection.getConnection();
          try (PreparedStatement ps = con.prepareStatement(
              "INSERT INTO wz_customlife (dataid, f, hide, fh, cy, rx0, rx1, type, x, y, mid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, npcId);
            ps.setInt(2, 0); // 1 = right , 0 = left
            ps.setInt(3, 0); // 1 = hide, 0 = show
            ps.setInt(4, fh);
            ps.setInt(5, ypos);
            ps.setInt(6, xpos);
            ps.setInt(7, xpos);
            ps.setString(8, "n");
            ps.setInt(9, xpos);
            ps.setInt(10, ypos);
            ps.setInt(11, c.getPlayer().getMapId());
            ps.executeUpdate();
          }
        } catch (SQLException e) {
          c.getPlayer().dropMessage(6, "Failed to save NPC to the database");
        }
        c.getPlayer().getMap().addMapObject(npc);
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
        c.getPlayer().dropMessage(6,
            "Please do not reload this map or else the NPC will disappear till the next restart.");
      } else {
        c.getPlayer().dropMessage(6, "You have entered an invalid Npc-Id");
        return 0;
      }
      return 1;
    }
  }

  public static class PMOB extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {
      if (splitted.length < 2) {
        c.getPlayer().dropMessage(6, "!pmob <mobid> <mobTime>");
        return 0;
      }
      int mobid = Integer.parseInt(splitted[1]);
      int mobTime = Integer.parseInt(splitted[2]);
      MapleMonster npc;
      try {
        npc = MapleLifeFactory.getMonster(mobid);
      } catch (RuntimeException e) {
        c.getPlayer().dropMessage(5, "Error: " + e.getMessage());
        return 0;
      }
      if (npc != null) {
        final int xpos = c.getPlayer().getPosition().x;
        final int ypos = c.getPlayer().getPosition().y;
        final int fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
        npc.setPosition(c.getPlayer().getPosition());
        npc.setCy(ypos);
        npc.setRx0(xpos);
        npc.setRx1(xpos);
        npc.setFh(fh);
        try {
          Connection con = DatabaseConnection.getConnection();
          try (PreparedStatement ps = con.prepareStatement(
              "INSERT INTO wz_customlife (dataid, f, hide, fh, cy, rx0, rx1, type, x, y, mid, mobtime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, mobid);
            ps.setInt(2, 0); // 1 = right , 0 = left
            ps.setInt(3, 0); // 1 = hide, 0 = show
            ps.setInt(4, fh);
            ps.setInt(5, ypos);
            ps.setInt(6, xpos);
            ps.setInt(7, xpos);
            ps.setString(8, "m");
            ps.setInt(9, xpos);
            ps.setInt(10, ypos);
            ps.setInt(11, c.getPlayer().getMapId());
            ps.setInt(12, mobTime);
            ps.executeUpdate();
          }
        } catch (SQLException e) {
          c.getPlayer().dropMessage(6, "Failed to save NPC to the database");
        }
        c.getPlayer().getMap().addMonsterSpawn(npc, mobTime, (byte) -1, null);
        c.getPlayer().dropMessage(6,
            "Please do not reload this map or else the MOB will disappear till the next restart.");
      } else {
        c.getPlayer().dropMessage(6, "You have entered an invalid Mob-Id");
        return 0;
      }
      return 1;
    }
  }

  public static class ReloadCustomLife extends CommandExecute {

    @Override
    public int execute(final MapleClient c, String[] splitted) {
      int size = MapleMapFactory.loadCustomLife();
      if (size == -1) {
        c.getPlayer().dropMessage(6, "Failed to reload custom life.");
        return 0;
      }
      c.getPlayer().dropMessage(6, "Successfully reloaded npcs/mob in " + size + " maps.");
      return 1;
    }
  }


  public abstract static class TestTimer extends CommandExecute {

    protected Timer toTest = null;

    @Override
    public int execute(final MapleClient c, String[] splitted) {
      final int sec = Integer.parseInt(splitted[1]);
      c.getPlayer().dropMessage(5, "Message will pop up in " + sec + " seconds.");
      final long oldMillis = System.currentTimeMillis();
      toTest.schedule(new Runnable() {

        public void run() {
          c.getPlayer().dropMessage(5,
              "Message has popped up in " + ((System.currentTimeMillis() - oldMillis) / 1000)
                  + " seconds, expected was " + sec + " seconds");
        }
      }, sec * 1000);
      return 1;
    }
  }

  public static class TestEventTimer extends TestTimer {

    public TestEventTimer() {
      toTest = EventTimer.getInstance();
    }
  }

  public static class TestCloneTimer extends TestTimer {

    public TestCloneTimer() {
      toTest = CloneTimer.getInstance();
    }
  }

  public static class TestEtcTimer extends TestTimer {

    public TestEtcTimer() {
      toTest = EtcTimer.getInstance();
    }
  }

  public static class TestMobTimer extends TestTimer {

    public TestMobTimer() {
      toTest = MobTimer.getInstance();
    }
  }

  public static class TestMapTimer extends TestTimer {

    public TestMapTimer() {
      toTest = MapTimer.getInstance();
    }
  }

  public static class TestWorldTimer extends TestTimer {

    public TestWorldTimer() {
      toTest = WorldTimer.getInstance();
    }
  }

  public static class TestBuffTimer extends TestTimer {

    public TestBuffTimer() {
      toTest = BuffTimer.getInstance();
    }
  }

  public static class AcidTrip extends CommandExecute {

    @Override
    public int execute(final MapleClient c, String[] splitted) {
      if (splitted.length < 3) {
        c.getPlayer().dropMessage(5, "ERROR. Please use !acidTrip <amount> <itemId> <delay[seconds]>");
        return 0;
      }
      final int amount = Integer.parseInt(splitted[1]);
      if (amount <= 0) {
        c.getPlayer().dropMessage(5, "ERROR. Please use !acidTrip <amount> <itemId> <delay[seconds]>");
        return 0;
      }
      final int itemId = Integer.parseInt(splitted[2]);
      final int delay = Integer.parseInt(splitted[3]);
      final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (GameConstants.isPet(itemId)) {
        c.getPlayer().dropMessage(5, "Please purchase a pet from the cash shop instead.");
      } else if (!ii.itemExists(itemId)) {
        c.getPlayer().dropMessage(5, itemId + " does not exist");
      } else {
        for (int i = 0; i < amount; i++) {
          EtcTimer.getInstance().schedule(new Runnable() {
            public void run() {
              if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
                return;
              }
              IItem toDrop;
              if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                toDrop = ii.randomizeStats((Equip) ii.getEquipById(itemId));
              } else {
                toDrop = new client.inventory.Item(itemId, (byte) 0, (short) 1, (byte) 0);
              }
              c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop,
                  c.getPlayer().getPosition(), true, true);
            }
          }, delay * i + 500);
        }
      }
      return 1;
    }
  }

  public static class printarray extends CommandExecute { // Syntax:
    // !printoutarray
    // <low range> <high
    // range>
    @Override
    public int execute(MapleClient c, String[] splitted) {
      StringBuilder s = new StringBuilder("[");
      if (splitted.length < 2) {
        c.getSession().write(MaplePacketCreator.getGameMessage(6,
            "Correct Syntax: !printoutarray <low range> <high range>"));
      } else if (splitted.length == 2) {
        for (int i = Integer.parseInt(splitted[1]); i < Integer.parseInt(splitted[2]); i++) {
          if (i < 1000000) {
            break;
          } else {
            if (i == Integer.parseInt(splitted[2])) {
              s.append(i).append("];");
            } else {
              s.append(i).append(",").append(" ");
            }
          }
        }
        System.out.print(s.toString()); // or change this to print
        // wherever you want
      }
      return 0;
    }
  }


  public static class lifeoverride extends CommandExecute {

    @Override
    public int execute(MapleClient c, String[] splitted) {

      return 1;
    }
  }

  public static class clearlife extends CommandExecute {
    @Override
    public int execute(MapleClient c, String[] splitted) {
      BufferedReader br = null;
      BufferedWriter bw = null;
      boolean isLife = false; // is this content inside of life?
      boolean clearing = false; // should we delete lines?
      boolean toggle = false;

      File f = new File("wz/Map.wz/Map/Map" + String.valueOf(c.getPlayer().getMapId()).charAt(0) + "/"
          + c.getPlayer().getMapId() + ".img.xml");
      System.out.println("Map loaded! Ready for clearing life image diretory");
      // file reading
      try {
        br = new BufferedReader(new FileReader(f)); // 0.1
        System.out.println("executing 0.1");
        bw = new BufferedWriter(new FileWriter(f)); // 0.2 //new
        // File(f.getAbsolutePath())));
        System.out.println("executing 0.2");
        br.readLine();
        while (br.readLine() != null) {
          System.out.println("reading lines");
          while (clearing) { // clear the content of life

          }
          if (br.readLine().contains("<imgdir name=\"life\">")) { // 1
            System.out.println("executing 1");
            isLife = true;
          }
          if (br.readLine().contains("<imgdir name=") && isLife == true) { // 2
            System.out.println("executing 2");
            clearing = true;
            toggle = true;
          }
          if (br.readLine().contains("</imgdir>")) { // 3
            System.out.println("executing 3");
            if (toggle == true) { // 4
              System.out.println("executing 4");
              toggle = false;
            }
            if (toggle == false) { // 5
              System.out.println("executing 5");
              c.getPlayer().dropMessage(6, "Life cleaned for map " + c.getPlayer().getMapId()
                  + ". Please reload map to see changes.");
              clearing = false;
              bw.flush();
              bw.close();
              br.close();
            }
          }
        }
        // error handling
        return 1;
      } catch (FileNotFoundException fnf) {
        System.err.println("File could not be located in your wz directory \n" + fnf);
      } catch (IOException i) {
        System.err.println(i);
      } finally {
        try {
          br.close();
          bw.close();
        } catch (IOException io) {
          System.err.println(io);
        }
      }
      return 1;
    }
  }

  public void removeLineFromFile(String file, String lineToRemove) {

    try {

      File inFile = new File(file);

      if (!inFile.isFile()) {
        System.out.println("Parameter is not an existing file");
        return;
      }

      // Construct the new file that will later be renamed to the original
      // filename.
      File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

      BufferedReader br = new BufferedReader(new FileReader(file));
      PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

      String line = null;

      // Read from the original file and write to the new
      // unless content matches data to be removed.
      while ((line = br.readLine()) != null) {

        if (!line.trim().equals(lineToRemove)) {

          pw.println(line);
          pw.flush();
        }
      }
      pw.close();
      br.close();

      // Delete the original file
      if (!inFile.delete()) {
        System.out.println("Could not delete file");
        return;
      }

      // Rename the new file to the filename the original file had.
      if (!tempFile.renameTo(inFile)) {
        System.out.println("Could not rename file");
      }

    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static boolean MapChecks(final MapleClient c, final MapleCharacter other) {
    for (int i : GameConstants.blockedMaps) {
      if (other.getMapId() == i) {
        c.getPlayer().dropMessage(5, "You may not use this command here.");
        return false;
      }
    }
    if (other.getLevel() < 10) {
      c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
      return false;
    }
    if (other.getMap().getSquadByMap() != null || other.getEventInstance() != null
        || other.getMap().getEMByMap() != null || MapConstants.isStorylineMap(other.getMapId())) {
      c.getPlayer().dropMessage(5, "You may not use this command here.");
      return false;
    }
    if ((other.getMapId() >= 680000210 && other.getMapId() <= 680000502)
        || (other.getMapId() / 1000 == 980000 && other.getMapId() != 980000000)
        || (other.getMapId() / 100 == 1030008) || (other.getMapId() / 100 == 922010)
        || (other.getMapId() / 10 == 13003000) || (other.getMapId() >= 990000000)) {
      c.getPlayer().dropMessage(5, "You may not use this command here.");
      return false;
    }
    return true;
  }


  public static class finddrop extends CommandExecute {

    private static final String getPaddedLine(String text) {
      StringBuilder builder = new StringBuilder();
      int len = (75 - text.length()) / 2;
      for (int i = 0; i <= len; i++) {
        builder.append("=");
        if (i == len) {
          String firstHalf = builder.toString();
          return firstHalf + text + firstHalf;
        }
      }
      return text;
    }

    @Override
    public int execute(MapleClient c, String[] splitted) {

      final List<MapleMonster> ids = c.getPlayer()
          .getMap()
          .getAllMonster();


      if (ids == null || ids.size() < 0) {
        return 0;
      }
      final MapleDropProvider provider = new DropDataProvider();
      List<Integer> visited = new ArrayList<>();

      ids.stream()
          .forEach((o) -> {
            if (!visited.contains(o.getId())) {
              List<MapleDropData> data = provider.search(o);
              StringBuilder builder = new StringBuilder();
              builder.append(getPaddedLine(o.getStats().getName()));
              for (int i = 0; i < data.size(); i++) {
                MapleDropData currentDataElement = data.get(i);
                builder.append(currentDataElement.getName());
                if (!(i == data.size() - 1)) {
                  builder.append(", ");
                }

              }
              c.getPlayer().dropMessage(6, builder.toString());
              visited.add(o.getId());
            }
          });

      return 0;
    }

  }
}
