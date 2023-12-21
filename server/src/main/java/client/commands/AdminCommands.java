package client.commands;

import client.MapleCharacter;
import client.MapleCharacterHelper;
import client.MapleClient;
import client.MapleDisease;
import client.MapleStat;
import client.anticheat.CheatingOffense;
import client.anticheat.ReportType;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import client.skill.ISkill;
import client.skill.SkillFactory;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import handling.world.helper.BroadcastHelper;
import handling.world.helper.CheaterData;
import handling.world.helper.FindCommand;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataTool;
import provider.drop.DropDataProvider;
import provider.drop.MapleDropData;
import provider.drop.MapleDropProvider;
import scripting.EventInstanceManager;
import scripting.EventManager;
import scripting.PortalScriptManager;
import scripting.ReactorScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleShopFactory;
import server.MapleSquad;
import server.ShutdownServer;
import server.Timer;
import server.Timer.BuffTimer;
import server.Timer.CloneTimer;
import server.Timer.EtcTimer;
import server.Timer.EventTimer;
import server.Timer.MapTimer;
import server.Timer.MobTimer;
import server.Timer.WorldTimer;
import server.config.ServerConfig;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.MobSkillFactory;
import server.life.OverrideMonsterStats;
import server.life.PlayerNPC;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import server.maps.MapleReactorStats;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.collection.ArrayMap;
import tools.collection.Pair;
import tools.helper.Api;
import tools.helper.StringUtil;
import tools.packet.MobPacket;

@Api
@Slf4j
public class AdminCommands {

    @Api(description = "Hides the character with the GM skill.")
    public static class Hide implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            SkillFactory.getSkill(9001004).getEffect(1).applyTo(c.getPlayer());
            return 0;
        }
    }

    @Api(description = "Sets the character to 1 HP.")
    public static class LowHP implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getStat().setHp((short) 1);
            c.getPlayer().getStat().setMp((short) 1);
            c.getPlayer().updateSingleStat(MapleStat.HP, 1);
            c.getPlayer().updateSingleStat(MapleStat.MP, 1);
            return 0;
        }
    }

    @Api(description = "Max the character HP and MP")
    public static class Heal implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getStat().setHp(c.getPlayer().getStat().getCurrentMaxHp());
            c.getPlayer().getStat().setMp(c.getPlayer().getStat().getCurrentMaxMp());
            c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getStat().getCurrentMaxHp());
            c.getPlayer().updateSingleStat(MapleStat.MP, c.getPlayer().getStat().getCurrentMaxMp());
            return 0;
        }
    }

    @Api(description = "Force disconnect a player")
    public static class DC implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            int level = 0;
            MapleCharacter victim;
            if (args[1].charAt(0) == '-') {
                level = StringUtil.countCharacters(args[1], 'f');
                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[2]);
            } else {
                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
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

    @Api
    public static class Kill implements Command {

        public int execute(MapleClient c, String[] args) {
            MapleCharacter player = c.getPlayer();
            if (args.length < 2) {
                c.getPlayer().dropMessage(6, "Syntax: !kill <list player names>");
                return 0;
            }
            MapleCharacter victim = null;
            for (int i = 1; i < args.length; i++) {
                try {
                    victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[i]);
                } catch (Exception e) {
                    c.getPlayer().dropMessage(6, "Player " + args[i] + " not found.");
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

    @Api
    public static class Skill implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 1) {
                c.getPlayer().dropMessage(5, "!skill <id> <level> <masterLevel>");
                return 0;
            }
            ISkill skill = SkillFactory.getSkill(Integer.parseInt(args[1]));
            if (skill.getId() >= 22000000
                    && (GameConstants.isEvan((skill.getId() / 10000))
                            || GameConstants.isResist((skill.getId() / 10000)))) {
                c.getPlayer().dropMessage(5, "Please change your job to an Evan instead.");
                return 0;
            }
            byte level = (byte) CommandProcessorUtil.getOptionalIntArg(args, 2, 1);
            byte masterlevel = (byte) CommandProcessorUtil.getOptionalIntArg(args, 3, 1);

            if (level > skill.getMaxLevel()) {
                level = skill.getMaxLevel();
            }
            c.getPlayer().changeSkillLevel(skill, level, masterlevel);
            return 1;
        }
    }

    @Api
    public static class Fame implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleCharacter player = c.getPlayer();
            if (args.length < 2) {
                c.getPlayer().dropMessage(6, "Syntax: !fame <player> <amount>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
            short fame = 0;
            try {
                fame = Short.parseShort(args[2]);
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

    @Api
    public static class HealHere implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleCharacter player = c.getPlayer();
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                if (mch != null) {
                    c.getPlayer().getStat().setHp(c.getPlayer().getStat().getMaxHp());
                    c.getPlayer()
                            .updateSingleStat(
                                    MapleStat.HP, c.getPlayer().getStat().getMaxHp());
                    c.getPlayer().getStat().setMp(c.getPlayer().getStat().getMaxMp());
                    c.getPlayer()
                            .updateSingleStat(
                                    MapleStat.MP, c.getPlayer().getStat().getMaxMp());
                }
            }
            return 1;
        }
    }

    @Api
    public static class GiveSkill implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
            ISkill skill = SkillFactory.getSkill(Integer.parseInt(args[2]));
            byte level = (byte) CommandProcessorUtil.getOptionalIntArg(args, 3, 1);
            byte masterlevel = (byte) CommandProcessorUtil.getOptionalIntArg(args, 4, 1);

            if (level > skill.getMaxLevel()) {
                level = skill.getMaxLevel();
            }
            victim.changeSkillLevel(skill, level, masterlevel);
            return 1;
        }
    }

    @Api
    public static class Job implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().changeJob(Integer.parseInt(args[1]));
            return 1;
        }
    }

    @Api
    public static class WhereAmI implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer()
                    .dropMessage(
                            5,
                            "You are on map " + c.getPlayer().getMap().getId() + " on x: "
                                    + c.getPlayer().getPosition().x + " y: "
                                    + c.getPlayer().getPosition().y);
            return 1;
        }
    }

    @Api
    public static class Shop implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleShopFactory shop = MapleShopFactory.getInstance();
            int shopId = Integer.parseInt(args[1]);
            if (shop.getShop(shopId) != null) {
                shop.getShop(shopId).sendShop(c);
            }
            return 1;
        }
    }

    @Api
    public static class GainMeso implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length > 1) {
                String zero = args[1];
                if (zero != null && !zero.isEmpty()) {
                    c.getPlayer().gainMeso(-c.getPlayer().getMeso(), true);
                    return 1;
                }
            }
            c.getPlayer().gainMeso(Integer.MAX_VALUE - c.getPlayer().getMeso(), true);
            return 1;
        }
    }

    @Api
    public static class GainCash implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 2) {
                c.getPlayer().dropMessage(5, "Need amount.");
                return 0;
            }
            c.getPlayer().modifyCSPoints(1, Integer.parseInt(args[1]), true);
            return 1;
        }
    }

    @Api
    public static class GainMP implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 2) {
                c.getPlayer().dropMessage(5, "Need amount.");
                return 0;
            }
            c.getPlayer().modifyCSPoints(2, Integer.parseInt(args[1]), true);
            return 1;
        }
    }

    @Api
    public static class LevelUp implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (c.getPlayer().getLevel() < 200) {
                c.getPlayer().gainExp(500000000, true, false, true);
            }
            return 1;
        }
    }

    @Api
    public static class ClearInv implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            java.util.Map<Pair<Short, Short>, MapleInventoryType> eqs = new ArrayMap<>();
            if (args[1].equals("all")) {
                for (MapleInventoryType type : MapleInventoryType.values()) {
                    for (IItem item : c.getPlayer().getInventory(type)) {
                        eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), type);
                    }
                }
            } else if (args[1].equals("eqp")) {
                for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED)) {
                    eqs.put(
                            new Pair<Short, Short>(item.getPosition(), item.getQuantity()),
                            MapleInventoryType.EQUIPPED);
                }
            } else if (args[1].equals("eq")) {
                for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP)) {
                    eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIP);
                }
            } else if (args[1].equals("u")) {
                for (IItem item : c.getPlayer().getInventory(MapleInventoryType.USE)) {
                    eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.USE);
                }
            } else if (args[1].equals("s")) {
                for (IItem item : c.getPlayer().getInventory(MapleInventoryType.SETUP)) {
                    eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.SETUP);
                }
            } else if (args[1].equals("e")) {
                for (IItem item : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
                    eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.ETC);
                }
            } else if (args[1].equals("c")) {
                for (IItem item : c.getPlayer().getInventory(MapleInventoryType.CASH)) {
                    eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.CASH);
                }
            } else {
                c.getPlayer().dropMessage(6, "[all/eqp/eq/u/s/e/c]");
            }
            for (Entry<Pair<Short, Short>, MapleInventoryType> eq : eqs.entrySet()) {
                MapleInventoryManipulator.removeFromSlot(
                        c, eq.getValue(), eq.getKey().left, eq.getKey().right, false, false);
            }
            return 1;
        }
    }

    @Api
    public static class UnlockInv implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            java.util.Map<IItem, MapleInventoryType> eqs = new ArrayMap<IItem, MapleInventoryType>();
            boolean add = false;
            if (args.length < 2 || args[1].equals("all")) {
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
            } else if (args[1].equals("eqp")) {
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
            } else if (args[1].equals("eq")) {
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
            } else if (args[1].equals("u")) {
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
            } else if (args[1].equals("s")) {
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
            } else if (args[1].equals("e")) {
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
            } else if (args[1].equals("c")) {
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

    @Api
    public static class Item implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            final int itemId = Integer.parseInt(args[1]);
            final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(args, 2, 1);

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

    @Api
    public static class Drop implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            final int itemId = Integer.parseInt(args[1]);
            final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(args, 2, 1);
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
                    toDrop = new client.inventory.Item(itemId, (byte) 0, quantity, (byte) 0);
                }

                c.getPlayer()
                        .getMap()
                        .spawnItemDrop(
                                c.getPlayer(),
                                c.getPlayer(),
                                toDrop,
                                c.getPlayer().getPosition(),
                                true,
                                true);
            }
            return 1;
        }
    }

    @Api
    public static class Level implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().setLevel(Short.parseShort(args[1]));
            c.getPlayer().levelUp(true);
            if (c.getPlayer().getExp() < 0) {
                c.getPlayer().gainExp(-c.getPlayer().getExp(), false, false, true);
            }
            return 1;
        }
    }

    @Api
    public static class Online implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().dropMessage(6, "Total amount of players connected to server:");
            c.getPlayer().dropMessage(6, "" + WorldServer.getInstance().getConnected() + "");
            c.getPlayer().dropMessage(6, "Characters connected to channel " + c.getChannel() + ":");
            c.getPlayer().dropMessage(6, c.getChannelServer().getPlayerStorage().getOnlinePlayers(true));
            return 0;
        }
    }

    @Api
    public static class Say implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length > 1) {
                String sb = "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(args, 1);
                BroadcastHelper.broadcastMessage(MaplePacketCreator.serverNotice(6, sb));
            } else {
                c.getPlayer().dropMessage(6, "Syntax: !say <message>");
                return 0;
            }
            return 1;
        }
    }

    @Api
    public static class Letter implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 3) {
                c.getPlayer().dropMessage(6, "syntax: !letter <color (green/red)> <word>");
                return 0;
            }
            int start, nstart;
            if (args[1].equalsIgnoreCase("green")) {
                start = 3991026;
                nstart = 3990019;
            } else if (args[1].equalsIgnoreCase("red")) {
                start = 3991000;
                nstart = 3990009;
            } else {
                c.getPlayer().dropMessage(6, "Unknown color!");
                return 0;
            }
            String splitString = StringUtil.joinStringFrom(args, 2);
            List<Integer> chars = new ArrayList<Integer>();
            splitString = splitString.toUpperCase();
            // log.info(splitString);
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
                    c.getPlayer()
                            .getMap()
                            .spawnItemDrop(
                                    c.getPlayer(),
                                    c.getPlayer(),
                                    item,
                                    new Point(dStart, c.getPlayer().getPosition().y),
                                    false,
                                    false);
                    dStart += w;
                } else if (i >= 200 && i <= 300) {
                    int val = nstart + i - (int) ('0') - 200;
                    client.inventory.Item item = new client.inventory.Item(val, (byte) 0, (short) 1);
                    c.getPlayer()
                            .getMap()
                            .spawnItemDrop(
                                    c.getPlayer(),
                                    c.getPlayer(),
                                    item,
                                    new Point(dStart, c.getPlayer().getPosition().y),
                                    false,
                                    false);
                    dStart += w;
                }
            }
            return 1;
        }
    }

    @Api
    public static class ItemCheck implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 3 || args[1] == null || args[1].equals("") || args[2] == null || args[2].equals("")) {
                c.getPlayer().dropMessage(6, "!itemcheck <playername> <itemid>");
                return 0;
            } else {
                int item = Integer.parseInt(args[2]);
                MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
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

    @Api
    public static class Song implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(args[1]));
            return 1;
        }
    }

    @Api
    public static class SetEvent implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleEvent.onStartEvent(c.getPlayer());
            return 1;
        }
    }

    @Api
    public static class StartEvent implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (c.getChannelServer().getEvent() == c.getPlayer().getMapId()) {
                MapleEvent.setEvent(c.getChannelServer(), false);
                c.getPlayer().dropMessage(5, "Started the event and closed off");
                return 1;
            } else {
                c.getPlayer()
                        .dropMessage(5, "!scheduleevent must've been done first, and you must be in the event map.");
                return 0;
            }
        }
    }

    @Api
    public static class ScheduleEvent implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            final MapleEventType type = MapleEventType.getByString(args[1]);
            if (type == null) {
                final StringBuilder sb = new StringBuilder("Wrong syntax: ");
                for (MapleEventType t : MapleEventType.values()) {
                    sb.append(t.getCommand()).append(",");
                }
                c.getPlayer().dropMessage(5, sb.substring(0, sb.toString().length() - 1));
            }
            final String msg = MapleEvent.scheduleEvent(type, c.getChannelServer());
            if (msg.length() > 0) {
                c.getPlayer().dropMessage(5, msg);
                return 0;
            }
            return 1;
        }
    }

    @Api
    public static class RemoveItem implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 3) {
                c.getPlayer().dropMessage(6, "Need <name> <itemid>");
                return 0;
            }
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
            if (chr == null) {
                c.getPlayer().dropMessage(6, "This player does not exist");
                return 0;
            }
            chr.removeAll(Integer.parseInt(args[2]));
            c.getPlayer()
                    .dropMessage(
                            6,
                            "All items with the ID " + args[2] + " has been removed from the inventory of " + args[1]
                                    + ".");
            return 1;
        }
    }

    @Api
    public static class LockItem implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 3) {
                c.getPlayer().dropMessage(6, "Need <name> <itemid>");
                return 0;
            }
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
            if (chr == null) {
                c.getPlayer().dropMessage(6, "This player does not exist");
                return 0;
            }
            int itemid = Integer.parseInt(args[2]);
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
            c.getPlayer()
                    .dropMessage(
                            6,
                            "All items with the ID " + args[2] + " has been locked from the inventory of " + args[1]
                                    + ".");
            return 1;
        }
    }

    @Api
    public static class KillMap implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            for (MapleCharacter map : c.getPlayer().getMap().getCharactersThreadsafe()) {
                if (map != null && !map.isGameMaster()) {
                    map.getStat().setHp((short) 0);
                    map.getStat().setMp((short) 0);
                    map.updateSingleStat(MapleStat.HP, 0);
                    map.updateSingleStat(MapleStat.MP, 0);
                }
            }
            return 1;
        }
    }

    @Api
    public static class Disease implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 3) {
                c.getPlayer()
                        .dropMessage(
                                6,
                                "!disease <type> [charname] <level> where type = SEAL/DARKNESS/WEAKEN/STUN/CURSE/POISON/SLOW/SEDUCE/REVERSE/ZOMBIFY/POTION/SHADOW/BLIND/FREEZE");
                return 0;
            }
            int type = 0;
            MapleDisease dis = null;
            if (args[1].equalsIgnoreCase("SEAL")) {
                type = 120;
            } else if (args[1].equalsIgnoreCase("DARKNESS")) {
                type = 121;
            } else if (args[1].equalsIgnoreCase("WEAKEN")) {
                type = 122;
            } else if (args[1].equalsIgnoreCase("STUN")) {
                type = 123;
            } else if (args[1].equalsIgnoreCase("CURSE")) {
                type = 124;
            } else if (args[1].equalsIgnoreCase("POISON")) {
                type = 125;
            } else if (args[1].equalsIgnoreCase("SLOW")) {
                type = 126;
            } else if (args[1].equalsIgnoreCase("SEDUCE")) {
                type = 128;
            } else if (args[1].equalsIgnoreCase("REVERSE")) {
                type = 132;
            } else if (args[1].equalsIgnoreCase("ZOMBIFY")) {
                type = 133;
            } else if (args[1].equalsIgnoreCase("POTION")) {
                type = 134;
            } else if (args[1].equalsIgnoreCase("SHADOW")) {
                type = 135;
            } else if (args[1].equalsIgnoreCase("BLIND")) {
                type = 136;
            } else if (args[1].equalsIgnoreCase("FREEZE")) {
                type = 137;
            } else {
                c.getPlayer()
                        .dropMessage(
                                6,
                                "!disease <type> [charname] <level> where type = SEAL/DARKNESS/WEAKEN/STUN/CURSE/POISON/SLOW/SEDUCE/REVERSE/ZOMBIFY/POTION/SHADOW/BLIND/FREEZE");
                return 0;
            }
            dis = MapleDisease.getBySkill(type);
            if (args.length == 4) {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[2]);
                if (victim == null) {
                    c.getPlayer().dropMessage(5, "Not found.");
                    return 0;
                }
                victim.setChair(0);
                victim.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
                victim.getMap()
                        .broadcastMessage(
                                victim,
                                MaplePacketCreator.showChair(c.getPlayer().getId(), 0),
                                false);
                victim.giveDebuff(
                        dis, MobSkillFactory.getMobSkill(type, CommandProcessorUtil.getOptionalIntArg(args, 3, 1)));
            } else {
                for (MapleCharacter victim : c.getPlayer().getMap().getCharactersThreadsafe()) {
                    victim.setChair(0);
                    victim.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
                    victim.getMap()
                            .broadcastMessage(
                                    victim,
                                    MaplePacketCreator.showChair(c.getPlayer().getId(), 0),
                                    false);
                    victim.giveDebuff(
                            dis, MobSkillFactory.getMobSkill(type, CommandProcessorUtil.getOptionalIntArg(args, 2, 1)));
                }
            }
            return 1;
        }
    }

    @Api
    public static class SendAllNote implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {

            if (args.length >= 1) {
                String text = StringUtil.joinStringFrom(args, 1);
                for (MapleCharacter mch :
                        c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                    c.getPlayer().sendNote(mch.getName(), text);
                }
            } else {
                c.getPlayer().dropMessage(6, "Use it like this, !sendallnote <text>");
                return 0;
            }
            return 1;
        }
    }

    @Api
    public static class PermWeather implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (c.getPlayer().getMap().getPermanentWeather() > 0) {
                c.getPlayer().getMap().setPermanentWeather(0);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeMapEffect());
                c.getPlayer().dropMessage(5, "Map weather has been disabled.");
            } else {
                final int weather = CommandProcessorUtil.getOptionalIntArg(args, 1, 5120000);
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

    @Api
    public static class WhosThere implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            StringBuilder builder = new StringBuilder("Players on Map: ");
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                if (builder.length() > 150) { // wild guess :o
                    builder.setLength(builder.length() - 2);
                    c.getPlayer().dropMessage(6, builder.toString());
                    builder = new StringBuilder();
                }
                builder.append(MapleCharacterHelper.makeMapleReadable(chr.getName()));
                builder.append(", ");
            }
            builder.setLength(builder.length() - 2);
            c.getPlayer().dropMessage(6, builder.toString());
            return 1;
        }
    }

    @Api
    public static class Cheaters implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            List<CheaterData> cheaters = WorldServer.getInstance().getCheaters();
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

    @Api
    public static class Reports implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            List<CheaterData> cheaters = WorldServer.getInstance().getReports();
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

    @Api
    public static class ClearReport implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 3) {
                StringBuilder ret = new StringBuilder("report [ign] [all/");
                for (ReportType type : ReportType.values()) {
                    ret.append(type.theId).append('/');
                }
                ret.setLength(ret.length() - 1);
                c.getPlayer().dropMessage(6, ret.append(']').toString());
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
            if (victim == null) {
                c.getPlayer().dropMessage(5, "Does not exist");
                return 0;
            }
            ReportType type = ReportType.getByString(args[2]);
            if (type != null) {
                victim.clearReports(type);
            } else {
                victim.clearReports();
            }
            c.getPlayer().dropMessage(5, "Done.");
            return 1;
        }
    }

    @Api
    public static class Connected implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            java.util.Map<Integer, Integer> connected =
                    WorldServer.getInstance().getConnected();
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

    @Api
    public static class ResetQuest implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleQuest.getInstance(Integer.parseInt(args[1])).forfeit(c.getPlayer());
            return 1;
        }
    }

    @Api
    public static class StartQuest implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleQuest.getInstance(Integer.parseInt(args[1])).start(c.getPlayer(), Integer.parseInt(args[2]));
            return 1;
        }
    }

    @Api
    public static class CompleteQuest implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleQuest.getInstance(Integer.parseInt(args[1]))
                    .complete(c.getPlayer(), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            return 1;
        }
    }

    @Api
    public static class FStartQuest implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleQuest.getInstance(Integer.parseInt(args[1]))
                    .forceStart(c.getPlayer(), Integer.parseInt(args[2]), args.length >= 4 ? args[3] : null);
            return 1;
        }
    }

    @Api
    public static class FCompleteQuest implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleQuest.getInstance(Integer.parseInt(args[1])).forceComplete(c.getPlayer(), Integer.parseInt(args[2]));
            return 1;
        }
    }

    @Api
    public static class FStartOther implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleQuest.getInstance(Integer.parseInt(args[2]))
                    .forceStart(
                            c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]),
                            Integer.parseInt(args[3]),
                            args.length >= 4 ? args[4] : null);
            return 1;
        }
    }

    @Api
    public static class FCompleteOther implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleQuest.getInstance(Integer.parseInt(args[2]))
                    .forceComplete(
                            c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]),
                            Integer.parseInt(args[3]));
            return 1;
        }
    }

    @Api
    public static class NearestPortal implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MaplePortal portal =
                    c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition());
            c.getPlayer()
                    .dropMessage(6, portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());

            return 1;
        }
    }

    @Api
    public static class SpawnDebug implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().dropMessage(6, c.getPlayer().getMap().spawnDebug());
            return 1;
        }
    }

    @Api
    public static class Threads implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            String filter = "";
            if (args.length > 1) {
                filter = args[1];
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

    @Api
    public static class ShowTrace implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 2) {
                throw new IllegalArgumentException();
            }
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            Thread t = threads[Integer.parseInt(args[1])];
            c.getPlayer().dropMessage(6, t.toString() + ":");
            for (StackTraceElement elem : t.getStackTrace()) {
                c.getPlayer().dropMessage(6, elem.toString());
            }
            return 1;
        }
    }

    @Api
    public static class FakeRelog implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleCharacter player = c.getPlayer();
            c.getSession().write(MaplePacketCreator.getCharInfo(player));
            player.sendSkills();
            player.getMap().removePlayer(player);
            player.getMap().addPlayer(player);
            return 1;
        }
    }

    @Api
    public static class ToggleOffense implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            try {
                CheatingOffense co = CheatingOffense.valueOf(args[1]);
                co.setEnabled(!co.isEnabled());
            } catch (IllegalArgumentException iae) {
                c.getPlayer().dropMessage(6, "Offense " + args[1] + " not found");
            }
            return 1;
        }
    }

    @Api
    public static class TDrops implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getMap().toggleDrops();
            return 1;
        }
    }

    @Api
    public static class TMegaphone implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            WorldServer.getInstance().toggleMegaphoneMuteState();
            c.getPlayer()
                    .dropMessage(
                            6,
                            "Megaphone state : "
                                    + (c.getChannelServer().getMegaphoneMuteState() ? "Enabled" : "Disabled"));
            return 1;
        }
    }

    @Api
    public static class SReactor implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleReactorStats reactorSt = MapleReactorFactory.getReactor(Integer.parseInt(args[1]));
            MapleReactor reactor = new MapleReactor(reactorSt, Integer.parseInt(args[1]));
            reactor.setDelay(-1);
            reactor.setPosition(c.getPlayer().getPosition());
            c.getPlayer().getMap().spawnReactor(reactor);
            return 1;
        }
    }

    @Api
    public static class HReactor implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getMap().getReactorByOid(Integer.parseInt(args[1])).hitReactor(c);
            return 1;
        }
    }

    @Api
    public static class DReactor implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            List<MapleMapObject> reactors = map.getMapObjectsInRange(
                    c.getPlayer().getPosition(),
                    Double.POSITIVE_INFINITY,
                    Collections.singletonList(MapleMapObjectType.REACTOR));
            if (args[1].equals("all")) {
                for (MapleMapObject reactorL : reactors) {
                    MapleReactor reactor2l = (MapleReactor) reactorL;
                    c.getPlayer().getMap().destroyReactor(reactor2l.getObjectId());
                }
            } else {
                c.getPlayer().getMap().destroyReactor(Integer.parseInt(args[1]));
            }
            return 1;
        }
    }

    @Api
    public static class ResetReactor implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getMap().resetReactors();
            return 1;
        }
    }

    @Api
    public static class SetReactor implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getMap().setReactorState(Byte.parseByte(args[1]));
            return 1;
        }
    }

    @Api
    public static class cleardrops implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().dropMessage(5, "Cleared " + c.getPlayer().getMap().getNumItems() + " drops");
            c.getPlayer().getMap().removeDrops();
            return 1;
        }
    }

    @Api
    public static class ExpRate implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length > 1) {
                final int rate = Integer.parseInt(args[1]);
                if (args.length > 2 && args[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : WorldServer.getInstance().getAllChannels()) {
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

    @Api
    public static class DropRate implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length > 1) {
                final int rate = Integer.parseInt(args[1]);
                if (args.length > 2 && args[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : WorldServer.getInstance().getAllChannels()) {
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

    @Api
    public static class MesoRate implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length > 1) {
                final int rate = Integer.parseInt(args[1]);
                if (args.length > 2 && args[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : WorldServer.getInstance().getAllChannels()) {
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

    @Api
    public static class CashRate implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length > 1) {
                final int rate = Integer.parseInt(args[1]);
                if (args.length > 2 && args[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : WorldServer.getInstance().getAllChannels()) {
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

    @Api
    public static class ListSquads implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            for (Entry<String, MapleSquad> squads :
                    c.getChannelServer().getAllSquads().entrySet()) {
                c.getPlayer()
                        .dropMessage(
                                5,
                                "TYPE: " + squads.getKey() + ", Leader: "
                                        + squads.getValue().getLeader().getName()
                                        + ", status: " + squads.getValue().getStatus() + ", numMembers: "
                                        + squads.getValue().getSquadSize() + ", numBanned: "
                                        + squads.getValue().getBannedMemberSize());
            }
            return 1;
        }
    }

    @Api
    public static class ClearSquads implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            final Collection<MapleSquad> squadz = new ArrayList<MapleSquad>(
                    c.getChannelServer().getAllSquads().values());
            for (MapleSquad squads : squadz) {
                squads.clear();
            }
            return 1;
        }
    }

    @Api
    public static class SetInstanceProperty implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            EventManager em = c.getChannelServer().getEventSM().getEventManager(args[1]);
            if (em == null || em.getInstances().size() <= 0) {
                c.getPlayer().dropMessage(5, "none");
            } else {
                em.setProperty(args[2], args[3]);
                for (EventInstanceManager eim : em.getInstances()) {
                    eim.setProperty(args[2], args[3]);
                }
            }
            return 1;
        }
    }

    @Api
    public static class ListInstanceProperty implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            EventManager em = c.getChannelServer().getEventSM().getEventManager(args[1]);
            if (em == null || em.getInstances().size() <= 0) {
                c.getPlayer().dropMessage(5, "none");
            } else {
                for (EventInstanceManager eim : em.getInstances()) {
                    c.getPlayer()
                            .dropMessage(
                                    5,
                                    "Event " + eim.getName() + ", eventManager: " + em.getName() + " iprops: "
                                            + eim.getProperty(args[2]) + ", eprops: "
                                            + em.getProperty(args[2]));
                }
            }
            return 1;
        }
    }

    @Api
    public static class ListInstances implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            EventManager em = c.getChannelServer().getEventSM().getEventManager(StringUtil.joinStringFrom(args, 1));
            if (em == null || em.getInstances().size() <= 0) {
                c.getPlayer().dropMessage(5, "none");
            } else {
                for (EventInstanceManager eim : em.getInstances()) {
                    c.getPlayer()
                            .dropMessage(
                                    5,
                                    "Event " + eim.getName() + ", charSize: "
                                            + eim.getPlayers().size()
                                            + ", dcedSize: "
                                            + eim.getDisconnected().size() + ", mobSize: "
                                            + eim.getMobs().size()
                                            + ", eventManager: " + em.getName() + ", timeLeft: " + eim.getTimeLeft()
                                            + ", iprops: "
                                            + eim.getProperties().toString() + ", eprops: "
                                            + em.getProperties().toString());
                }
            }
            return 1;
        }
    }

    @Api
    public static class LeaveInstance implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (c.getPlayer().getEventInstance() == null) {
                c.getPlayer().dropMessage(5, "You are not in one");
            } else {
                c.getPlayer().getEventInstance().unregisterPlayer(c.getPlayer());
            }
            return 1;
        }
    }

    @Api
    public static class StartInstance implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (c.getPlayer().getEventInstance() != null) {
                c.getPlayer().dropMessage(5, "You are in one");
            } else if (args.length > 2) {
                EventManager em = c.getChannelServer().getEventSM().getEventManager(args[1]);
                if (em == null || em.getInstance(args[2]) == null) {
                    c.getPlayer().dropMessage(5, "Not exist");
                } else {
                    em.getInstance(args[2]).registerPlayer(c.getPlayer());
                }
            } else {
                c.getPlayer().dropMessage(5, "!startinstance [eventmanager] [eventinstance]");
            }
            return 1;
        }
    }

    @Api
    public static class eventinstance implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (c.getPlayer().getEventInstance() == null) {
                c.getPlayer().dropMessage(5, "none");
            } else {
                EventInstanceManager eim = c.getPlayer().getEventInstance();
                c.getPlayer()
                        .dropMessage(
                                5,
                                "Event " + eim.getName() + ", charSize: "
                                        + eim.getPlayers().size() + ", dcedSize: "
                                        + eim.getDisconnected().size() + ", mobSize: "
                                        + eim.getMobs().size()
                                        + ", eventManager: "
                                        + eim.getEventManager().getName() + ", timeLeft: "
                                        + eim.getTimeLeft() + ", iprops: "
                                        + eim.getProperties().toString() + ", eprops: "
                                        + eim.getEventManager().getProperties().toString());
            }
            return 1;
        }
    }

    @Api
    public static class Uptime implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer()
                    .dropMessage(
                            6,
                            "Server has been up for "
                                    + StringUtil.getReadableMillis(
                                            WorldServer.getInstance().getServerStartTime(),
                                            System.currentTimeMillis()));
            return 1;
        }
    }

    @Api
    public static class DCAll implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            int range = -1;
            if (args[1].equals("m")) {
                range = 0;
            } else if (args[1].equals("c")) {
                range = 1;
            } else if (args[1].equals("w")) {
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
                for (ChannelServer cserv : WorldServer.getInstance().getAllChannels()) {
                    cserv.getPlayerStorage().disconnectAll(true);
                }
            }
            return 1;
        }
    }

    @Api
    public static class GoTo implements Command {

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
        public int execute(MapleClient c, String[] args) {
            if (args.length < 2) {
                c.getPlayer().dropMessage(6, "Syntax: !goto <mapname>");
            } else {
                if (gotomaps.containsKey(args[1])) {
                    MapleMap target = c.getChannelServer().getMapFactory().getMap(gotomaps.get(args[1]));
                    MaplePortal targetPortal = target.getPortal(0);
                    c.getPlayer().changeMap(target, targetPortal);
                } else {
                    if (args[1].equals("locations")) {
                        c.getPlayer().dropMessage(6, "Use !goto <location>. Locations are as follows:");
                        StringBuilder sb = new StringBuilder();
                        for (String s : gotomaps.keySet()) {
                            sb.append(s).append(", ");
                        }
                        c.getPlayer().dropMessage(6, sb.substring(0, sb.length() - 2));
                    } else {
                        c.getPlayer()
                                .dropMessage(
                                        6,
                                        "Invalid command syntax - Use !goto <location>. For a list of locations, use !goto locations.");
                    }
                }
            }
            return 1;
        }
    }

    @Api
    public static class KillAll implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;

            if (args.length > 1) {
                int irange = Integer.parseInt(args[1]);
                if (args.length <= 2) {
                    range = irange * irange;
                } else {
                    map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(args[2]));
                }
            }
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(
                    c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                map.killMonster(mob, c.getPlayer(), true, false, (byte) 1);
            }
            return 1;
        }
    }

    @Api
    public static class ResetMobs implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getMap().killAllMonsters(false);
            return 1;
        }
    }

    @Api
    public static class KillMonster implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(
                    c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (mob.getId() == Integer.parseInt(args[1])) {
                    mob.damage(c.getPlayer(), mob.getHp(), false);
                }
            }
            return 1;
        }
    }

    @Api
    public static class KillMonsterByOID implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            int targetId = Integer.parseInt(args[1]);
            MapleMonster monster = map.getMonsterByOid(targetId);
            if (monster != null) {
                map.killMonster(monster, c.getPlayer(), false, false, (byte) 1);
            }
            return 1;
        }
    }

    @Api
    public static class HitMonsterByOID implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            int targetId = Integer.parseInt(args[1]);
            int damage = Integer.parseInt(args[2]);
            MapleMonster monster = map.getMonsterByOid(targetId);
            if (monster != null) {
                map.broadcastMessage(MobPacket.damageMonster(targetId, damage));
                monster.damage(c.getPlayer(), damage, false);
            }
            return 1;
        }
    }

    @Api
    public static class HitAll implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;
            if (args.length > 1) {
                int irange = Integer.parseInt(args[1]);
                if (args.length <= 2) {
                    range = irange * irange;
                } else {
                    map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(args[2]));
                }
            }
            int damage = Integer.parseInt(args[1]);
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(
                    c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                map.broadcastMessage(MobPacket.damageMonster(mob.getObjectId(), damage));
                mob.damage(c.getPlayer(), damage, false);
            }
            return 1;
        }
    }

    @Api
    public static class HitMonster implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;
            int damage = Integer.parseInt(args[1]);
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(
                    c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (mob.getId() == Integer.parseInt(args[2])) {
                    map.broadcastMessage(MobPacket.damageMonster(mob.getObjectId(), damage));
                    mob.damage(c.getPlayer(), damage, false);
                }
            }
            return 1;
        }
    }

    @Api
    public static class KillAllDrops implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;

            if (args.length > 1) {
                // && !splitted[0].equals("!killmonster") &&
                // !splitted[0].equals("!hitmonster") &&
                // !splitted[0].equals("!hitmonsterbyoid") &&
                // !splitted[0].equals("!killmonsterbyoid")) {
                int irange = Integer.parseInt(args[1]);
                if (args.length <= 2) {
                    range = irange * irange;
                } else {
                    map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(args[2]));
                }
            }
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(
                    c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                map.killMonster(mob, c.getPlayer(), true, false, (byte) 1);
            }
            return 1;
        }
    }

    @Api
    public static class KillAllNoSpawn implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            map.killAllMonsters(false);
            return 1;
        }
    }

    @Api
    public static class MonsterDebug implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;

            if (args.length > 1) {
                // && !splitted[0].equals("!killmonster") &&
                // !splitted[0].equals("!hitmonster") &&
                // !splitted[0].equals("!hitmonsterbyoid") &&
                // !splitted[0].equals("!killmonsterbyoid")) {
                int irange = Integer.parseInt(args[1]);
                if (args.length <= 2) {
                    range = irange * irange;
                } else {
                    map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(args[2]));
                }
            }
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(
                    c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                c.getPlayer().dropMessage(6, "Monster " + mob.toString());
            }
            return 1;
        }
    }

    @Api
    public static class NPC implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            int npcId = Integer.parseInt(args[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(c.getPlayer().getPosition().y);
                npc.setRx0(c.getPlayer().getPosition().x);
                npc.setRx1(c.getPlayer().getPosition().x);
                npc.setFh(c.getPlayer()
                        .getMap()
                        .getFootholds()
                        .findBelow(c.getPlayer().getPosition())
                        .getId());
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

    @Api
    public static class RemoveNPCs implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getMap().resetNPCs();
            return 1;
        }
    }

    @Api
    public static class LookNPC implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllNPCsThreadsafe()) {
                MapleNPC reactor2l = (MapleNPC) reactor1l;
                c.getPlayer()
                        .dropMessage(
                                5,
                                "NPC: oID: " + reactor2l.getObjectId() + " npcID: " + reactor2l.getId() + " Position: "
                                        + reactor2l.getPosition().toString() + " Name: " + reactor2l.getName());
            }
            return 1;
        }
    }

    @Api
    public static class LookReactor implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllReactorsThreadsafe()) {
                MapleReactor reactor2l = (MapleReactor) reactor1l;
                c.getPlayer()
                        .dropMessage(
                                5,
                                "Reactor: oID: " + reactor2l.getObjectId() + " reactorID: " + reactor2l.getReactorId()
                                        + " Position: "
                                        + reactor2l.getPosition().toString() + " State: " + reactor2l.getState()
                                        + " Name: " + reactor2l.getName());
            }
            return 1;
        }
    }

    @Api
    public static class LookPortals implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            for (MaplePortal portal : c.getPlayer().getMap().getPortals()) {
                c.getPlayer()
                        .dropMessage(
                                5,
                                "Portal: ID: " + portal.getId() + " script: " + portal.getScriptName() + " name: "
                                        + portal.getName() + " pos: " + portal.getPosition().x + ","
                                        + portal.getPosition().y
                                        + " target: " + portal.getTargetMapId() + " / " + portal.getTarget());
            }
            return 1;
        }
    }

    @Api
    public static class MakePNPC implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            try {
                c.getPlayer().dropMessage(6, "Making playerNPC...");
                MapleCharacter chhr = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
                if (chhr == null) {
                    c.getPlayer().dropMessage(6, args[1] + " is not online");
                    return 0;
                }
                PlayerNPC npc = new PlayerNPC(
                        chhr, Integer.parseInt(args[2]), c.getPlayer().getMap(), c.getPlayer());
                npc.addToServer();
                c.getPlayer().dropMessage(6, "Done");
            } catch (Exception e) {
                c.getPlayer().dropMessage(6, "NPC failed... : " + e.getMessage());
                e.printStackTrace();
            }
            return 1;
        }
    }

    @Api
    public static class DestroyPNPC implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            try {
                c.getPlayer().dropMessage(6, "Destroying playerNPC...");
                final MapleNPC npc = c.getPlayer().getMap().getNPCByOid(Integer.parseInt(args[1]));
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

    @Api
    public static class MyNPCPos implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            Point pos = c.getPlayer().getPosition();
            c.getPlayer()
                    .dropMessage(
                            6,
                            "X: " + pos.x + " | Y: " + pos.y + " | RX0: " + (pos.x) + " | RX1: " + (pos.x) + " | FH: "
                                    + c.getPlayer().getFH());
            return 1;
        }
    }

    @Api
    public static class Notice implements Command {

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
        public int execute(MapleClient c, String[] args) {
            int joinmod = 1;
            int range = -1;
            if (args[1].equals("m")) {
                range = 0;
            } else if (args[1].equals("c")) {
                range = 1;
            } else if (args[1].equals("w")) {
                range = 2;
            }

            int tfrom = 2;
            if (range == -1) {
                range = 2;
                tfrom = 1;
            }
            int type = getNoticeType(args[tfrom]);
            if (type == -1) {
                type = 0;
                joinmod = 0;
            }
            StringBuilder sb = new StringBuilder();
            if (args[tfrom].equals("nv")) {
                sb.append("[Notice]");
            } else {
            }
            joinmod += tfrom;
            sb.append(StringUtil.joinStringFrom(args, joinmod));

            byte[] packet = MaplePacketCreator.serverNotice(type, sb.toString());
            if (range == 0) {
                c.getPlayer().getMap().broadcastMessage(packet);
            } else if (range == 1) {
                WorldServer.getInstance().getChannel(c.getChannel()).broadcastPacket(packet);
            } else if (range == 2) {
                BroadcastHelper.broadcastMessage(packet);
            }
            return 1;
        }
    }

    @Api
    public static class Yellow implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            int range = -1;
            if (args[1].equals("m")) {
                range = 0;
            } else if (args[1].equals("c")) {
                range = 1;
            } else if (args[1].equals("w")) {
                range = 2;
            }
            if (range == -1) {
                range = 2;
            }
            byte[] packet = MaplePacketCreator.yellowChat(
                    (args[0].equals("!y") ? ("[" + c.getPlayer().getName() + "] ") : "")
                            + StringUtil.joinStringFrom(args, 2));
            if (range == 0) {
                c.getPlayer().getMap().broadcastMessage(packet);
            } else if (range == 1) {
                WorldServer.getInstance().getChannel(c.getChannel()).broadcastPacket(packet);
            } else if (range == 2) {
                BroadcastHelper.broadcastMessage(packet);
            }
            return 1;
        }
    }

    @Api
    public static class Y extends Yellow {}

    @Api
    public static class ReloadDrops implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleMonsterInformationProvider.getInstance().clearDrops();
            ReactorScriptManager.getInstance().clearDrops();
            return 1;
        }
    }

    @Api
    public static class ReloadPortal implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            PortalScriptManager.getInstance().clearScripts();
            return 1;
        }
    }

    @Api
    public static class ReloadShops implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleShopFactory.getInstance().clear();
            return 1;
        }
    }

    @Api
    public static class ReloadEvents implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            for (ChannelServer instance : WorldServer.getInstance().getAllChannels()) {
                instance.reloadEvents();
            }
            return 1;
        }
    }

    @Api
    public static class ReloadQuests implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleQuest.clearQuests();
            return 1;
        }
    }

    @Api
    public static class Find implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length == 1) {
                c.getPlayer().dropMessage(6, args[0] + ": <NPC> <MOB> <ITEM> <MAP> <SKILL>");
            } else if (args.length == 2) {
                c.getPlayer().dropMessage(6, "Provide something to search.");
            } else {
                String type = args[1];
                String search = StringUtil.joinStringFrom(args, 2);
                MapleData data = null;
                MapleDataProvider dataProvider = ServerConfig.serverConfig().getDataProvider("wz/String");
                c.getPlayer().dropMessage(6, "<<Type: " + type + " | Search: " + search + ">>");

                if (type.equalsIgnoreCase("NPC")) {
                    List<String> retNpcs = new ArrayList<String>();
                    data = dataProvider.getData("Npc.img");
                    List<Pair<Integer, String>> npcPairList = new LinkedList<Pair<Integer, String>>();
                    for (MapleData npcIdData : data.getChildren()) {
                        npcPairList.add(new Pair<Integer, String>(
                                Integer.parseInt(npcIdData.getName()),
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
                            mapPairList.add(new Pair<Integer, String>(
                                    Integer.parseInt(mapIdData.getName()),
                                    MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "NO-NAME") + " - "
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
                        mobPairList.add(new Pair<Integer, String>(
                                Integer.parseInt(mobIdData.getName()),
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
                    for (Pair<Integer, String> itemPair :
                            MapleItemInformationProvider.getInstance().getAllItems()) {
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
                        skillPairList.add(new Pair<Integer, String>(
                                Integer.parseInt(skillIdData.getName()),
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
                    MapleDataProvider questProvider =
                            ServerConfig.serverConfig().getDataProvider("wz/Quest");
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

    @Api
    public static class ID extends Find {}

    @Api
    public static class LookUp extends Find {}

    @Api
    public static class Search extends Find {}

    @Api
    public static class ServerMessage implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            Collection<ChannelServer> cservs = WorldServer.getInstance().getAllChannels();
            String outputMessage = StringUtil.joinStringFrom(args, 1);
            for (ChannelServer cserv : cservs) {
                cserv.setServerMessage(outputMessage);
            }
            return 1;
        }
    }

    @Api
    public static class ShutdownTime extends Shutdown {

        private static ScheduledFuture<?> ts = null;
        private int minutesLeft = 0;

        @Override
        public int execute(MapleClient c, String[] args) {
            this.minutesLeft = Integer.parseInt(args[1]);
            c.getPlayer().dropMessage(6, "Shutting down... in " + this.minutesLeft + " minutes");
            if (ts == null && (t == null || !t.isAlive())) {
                t = new Thread(ShutdownServer.getInstance());
                ts = Timer.EventTimer.getInstance()
                        .register(
                                new Runnable() {

                                    public void run() {
                                        if (ShutdownTime.this.minutesLeft == 0) {
                                            ShutdownServer.getInstance().shutdown();
                                            Shutdown.t.start();
                                            ShutdownTime.ts.cancel(false);
                                            return;
                                        }
                                        BroadcastHelper.broadcastMessage(MaplePacketCreator.serverNotice(
                                                0,
                                                "The server will shutdown in " + ShutdownTime.this.minutesLeft
                                                        + " minutes. Please log off safely."));
                                        ShutdownTime.this.minutesLeft--;
                                    }
                                },
                                60000L);
            } else {
                c.getPlayer()
                        .dropMessage(
                                6,
                                "A shutdown thread is already in progress or shutdown has not been done. Please wait.");
            }
            return 1;
        }
    }

    @Api
    public static class Shutdown implements Command {

        public static Thread t = null;

        @Override
        public int execute(MapleClient c, String[] args) {
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

    @Api
    public static class ShutdownMerchant implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            for (ChannelServer cserv : WorldServer.getInstance().getAllChannels()) {
                cserv.closeAllMerchant();
            }
            return 1;
        }
    }

    @Api
    public static class Spawn implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            final int mid = Integer.parseInt(args[1]);
            final int num = Math.min(CommandProcessorUtil.getOptionalIntArg(args, 2, 1), 500);

            Long hp = CommandProcessorUtil.getNamedLongArg(args, 1, "hp");
            Integer exp = CommandProcessorUtil.getNamedIntArg(args, 1, "exp");
            Double php = CommandProcessorUtil.getNamedDoubleArg(args, 1, "php");
            Double pexp = CommandProcessorUtil.getNamedDoubleArg(args, 1, "pexp");

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

            final OverrideMonsterStats overrideStats =
                    new OverrideMonsterStats(newhp, onemob.getMobMaxMp(), newexp, false);
            for (int i = 0; i < num; i++) {
                MapleMonster mob = MapleLifeFactory.getMonster(mid);
                mob.setHp(newhp);
                mob.setOverrideStats(overrideStats);
                c.getPlayer()
                        .getMap()
                        .spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
            }
            return 1;
        }
    }

    @Api
    public static class Clock implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer()
                    .getMap()
                    .broadcastMessage(MaplePacketCreator.getClock(CommandProcessorUtil.getOptionalIntArg(args, 1, 60)));
            return 1;
        }
    }

    @Api
    public static class Warp implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            // smart player selection
            MapleCharacter victim = null; // =
            // c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            List<String> possibility = new LinkedList<>();
            // HashMap<Integer, String> possibility = new HashMap<Integer,
            // String>();
            // int key = 0;

            StringBuilder sb = new StringBuilder();
            for (ChannelServer ch : WorldServer.getInstance().getAllChannels()) {
                for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
                    if (chr.getName().toLowerCase().contains(args[1].toLowerCase()) && victim == null) {
                        victim = chr;
                        possibility.add(chr.getName());
                    } else if (chr.getName().contains(args[1]) && victim != null) {
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
                    if (args.length == 2) {
                        c.getPlayer()
                                .changeMap(
                                        victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
                    } else {
                        MapleMap target = WorldServer.getInstance()
                                .getChannel(c.getChannel())
                                .getMapFactory()
                                .getMap(Integer.parseInt(args[2]));
                        victim.changeMap(target, target.getPortal(0));
                    }
                } else {
                    try {
                        victim = c.getPlayer();
                        int ch = FindCommand.findChannel(args[1]);
                        if (ch < 0) {
                            MapleMap target =
                                    c.getChannelServer().getMapFactory().getMap(Integer.parseInt(args[1]));
                            c.getPlayer().changeMap(target, target.getPortal(0));
                        } else {
                            victim = WorldServer.getInstance()
                                    .getChannel(ch)
                                    .getPlayerStorage()
                                    .getCharacterByName(args[1]);
                            c.getPlayer().dropMessage(6, "Cross changing channel. Please wait.");
                            if (victim.getMapId() != c.getPlayer().getMapId()) {
                                final MapleMap mapp =
                                        c.getChannelServer().getMapFactory().getMap(victim.getMapId());
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

    @Api
    public static class warpChHere implements Command {
        @Override
        public int execute(MapleClient c, String[] args) {
            try {
                for (MapleCharacter chr :
                        c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                    chr.changeMap(c.getPlayer().getMap(), c.getPlayer().getPosition());
                    chr.dcolormsg(5, "You have been warped to the event");
                }
                c.getPlayer().dcolormsg(5, "Every player in your channel have been warped here");
            } catch (Exception e) {
                log.info("Something went wrong: " + e);
            }
            return 0;
        }
    }

    @Api
    public static class WarpMapTo implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            try {
                final MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(args[1]));
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

    @Api
    public static class WarpHere implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[1]);
            if (victim != null) {
                victim.changeMap(
                        c.getPlayer().getMap(),
                        c.getPlayer()
                                .getMap()
                                .findClosestSpawnpoint(c.getPlayer().getPosition()));
            } else {
                int ch = FindCommand.findChannel(args[1]);
                if (ch < 0) {
                    c.getPlayer().dropMessage(5, "Not found.");
                    return 0;
                }
                victim = WorldServer.getInstance()
                        .getChannel(ch)
                        .getPlayerStorage()
                        .getCharacterByName(args[1]);
                c.getPlayer().dropMessage(5, "Victim is cross changing channel.");
                victim.dropMessage(5, "Cross changing channel.");
                if (victim.getMapId() != c.getPlayer().getMapId()) {
                    final MapleMap mapp = victim.getClient()
                            .getChannelServer()
                            .getMapFactory()
                            .getMap(c.getPlayer().getMapId());
                    victim.changeMap(mapp, mapp.getPortal(0));
                }
                victim.changeChannel(c.getChannel());
            }
            return 1;
        }
    }

    @Api
    public static class Map implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            try {
                MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(args[1]));
                MaplePortal targetPortal = null;
                if (args.length > 2) {
                    try {
                        targetPortal = target.getPortal(Integer.parseInt(args[2]));
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

    @Api
    public static class ReloadMap implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            final int mapId = Integer.parseInt(args[1]);
            for (ChannelServer cserv : WorldServer.getInstance().getAllChannels()) {
                if (cserv.getMapFactory().isMapLoaded(mapId)
                        && cserv.getMapFactory().getMap(mapId).getCharactersSize() > 0) {
                    c.getPlayer().dropMessage(5, "There exists characters on channel " + cserv.getChannel());
                    return 0;
                }
            }
            for (ChannelServer cserv : WorldServer.getInstance().getAllChannels()) {
                if (cserv.getMapFactory().isMapLoaded(mapId)) {
                    cserv.getMapFactory().removeMap(mapId);
                }
            }
            return 1;
        }
    }

    @Api
    public static class Respawn implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getMap().respawn(true);
            return 1;
        }
    }

    @Api
    public static class ResetMap implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            c.getPlayer().getMap().resetFully();
            return 1;
        }
    }

    @Api
    public static class PNPC implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 1) {
                c.getPlayer().dropMessage(6, "!pnpc <npcid>");
                return 0;
            }
            int npcId = Integer.parseInt(args[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                final int xpos = c.getPlayer().getPosition().x;
                final int ypos = c.getPlayer().getPosition().y;
                final int fh = c.getPlayer()
                        .getMap()
                        .getFootholds()
                        .findBelow(c.getPlayer().getPosition())
                        .getId();
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(ypos);
                npc.setRx0(xpos);
                npc.setRx1(xpos);
                npc.setFh(fh);
                npc.setCustom(true);
                try (var con = DatabaseConnection.getConnection()) {
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
                c.getPlayer()
                        .dropMessage(
                                6,
                                "Please do not reload this map or else the NPC will disappear till the next restart.");
            } else {
                c.getPlayer().dropMessage(6, "You have entered an invalid Npc-Id");
                return 0;
            }
            return 1;
        }
    }

    @Api
    public static class PMOB implements Command {

        @Override
        public int execute(MapleClient c, String[] args) {
            if (args.length < 2) {
                c.getPlayer().dropMessage(6, "!pmob <mobid> <mobTime>");
                return 0;
            }
            int mobid = Integer.parseInt(args[1]);
            int mobTime = Integer.parseInt(args[2]);
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
                final int fh = c.getPlayer()
                        .getMap()
                        .getFootholds()
                        .findBelow(c.getPlayer().getPosition())
                        .getId();
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(ypos);
                npc.setRx0(xpos);
                npc.setRx1(xpos);
                npc.setFh(fh);
                try (var con = DatabaseConnection.getConnection()) {
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
                c.getPlayer()
                        .dropMessage(
                                6,
                                "Please do not reload this map or else the MOB will disappear till the next restart.");
            } else {
                c.getPlayer().dropMessage(6, "You have entered an invalid Mob-Id");
                return 0;
            }
            return 1;
        }
    }

    @Api
    public static class ReloadCustomLife implements Command {

        @Override
        public int execute(final MapleClient c, String[] args) {
            int size = MapleMapFactory.loadCustomLife();
            if (size == -1) {
                c.getPlayer().dropMessage(6, "Failed to reload custom life.");
                return 0;
            }
            c.getPlayer().dropMessage(6, "Successfully reloaded npcs/mob in " + size + " maps.");
            return 1;
        }
    }

    public abstract static class TestTimer implements Command {

        protected Timer toTest = null;

        @Override
        public int execute(final MapleClient c, String[] args) {
            final int sec = Integer.parseInt(args[1]);
            c.getPlayer().dropMessage(5, "Message will pop up in " + sec + " seconds.");
            final long oldMillis = System.currentTimeMillis();
            toTest.schedule(
                    new Runnable() {

                        public void run() {
                            c.getPlayer()
                                    .dropMessage(
                                            5,
                                            "Message has popped up in "
                                                    + ((System.currentTimeMillis() - oldMillis) / 1000)
                                                    + " seconds, expected was " + sec + " seconds");
                        }
                    },
                    sec * 1000L);
            return 1;
        }
    }

    @Api
    public static class TestEventTimer extends TestTimer {

        public TestEventTimer() {
            toTest = EventTimer.getInstance();
        }
    }

    @Api
    public static class TestCloneTimer extends TestTimer {

        public TestCloneTimer() {
            toTest = CloneTimer.getInstance();
        }
    }

    @Api
    public static class TestEtcTimer extends TestTimer {

        public TestEtcTimer() {
            toTest = EtcTimer.getInstance();
        }
    }

    @Api
    public static class TestMobTimer extends TestTimer {

        public TestMobTimer() {
            toTest = MobTimer.getInstance();
        }
    }

    @Api
    public static class TestMapTimer extends TestTimer {

        public TestMapTimer() {
            toTest = MapTimer.getInstance();
        }
    }

    @Api
    public static class TestWorldTimer extends TestTimer {

        public TestWorldTimer() {
            toTest = WorldTimer.getInstance();
        }
    }

    @Api
    public static class TestBuffTimer extends TestTimer {

        public TestBuffTimer() {
            toTest = BuffTimer.getInstance();
        }
    }

    @Api
    public static class finddrop implements Command {

        private static String getPaddedLine(String text) {
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
        public int execute(MapleClient c, String[] args) {

            final List<MapleMonster> ids = c.getPlayer().getMap().getAllMonster();

            if (ids == null || ids.size() < 0) {
                return 0;
            }
            final MapleDropProvider provider = new DropDataProvider();
            List<Integer> visited = new ArrayList<>();

            ids.stream().forEach((o) -> {
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

    @Api(description = "Gives the player the gm equips")
    public static class GMEquip implements Command {
        @Override
        public int execute(MapleClient c, String[] args) {
            addItem(c, 1002140); // Invincible Hat
            addItem(c, 1042003); // Plain Suit
            addItem(c, 1062007); // Plain Suit pants
            addItem(c, 1322013); // Secret Agent case
            return 1;
        }

        private static void addItem(MapleClient c, int itemId) {
            if (c.getPlayer()
                    .getInventory(MapleInventoryType.EQUIP)
                    .listById(itemId)
                    .isEmpty()) {
                MapleInventoryManipulator.addById(
                        c, itemId, (short) 1, c.getPlayer().getName());
            }
        }
    }

    class CommandProcessorUtil {

        public static int getOptionalIntArg(String[] splitted, int position, int def) {
            if (splitted.length > position) {
                try {
                    return Integer.parseInt(splitted[position]);
                } catch (NumberFormatException nfe) {
                    return def;
                }
            }
            return def;
        }

        public static String getNamedArg(String[] splitted, int startpos, String name) {
            for (int i = startpos; i < splitted.length; i++) {
                if (splitted[i].equalsIgnoreCase(name) && i + 1 < splitted.length) {
                    return splitted[i + 1];
                }
            }
            return null;
        }

        public static Long getNamedLongArg(String[] splitted, int startpos, String name) {
            String arg = getNamedArg(splitted, startpos, name);
            if (arg != null) {
                try {
                    return Long.parseLong(arg);
                } catch (NumberFormatException nfe) {
                    // swallow - we don't really care
                }
            }
            return null;
        }

        public static Integer getNamedIntArg(String[] splitted, int startpos, String name) {
            String arg = getNamedArg(splitted, startpos, name);
            if (arg != null) {
                try {
                    return Integer.parseInt(arg);
                } catch (NumberFormatException nfe) {
                    // swallow - we don't really care
                }
            }
            return null;
        }

        public static Double getNamedDoubleArg(String[] splitted, int startpos, String name) {
            String arg = getNamedArg(splitted, startpos, name);
            if (arg != null) {
                try {
                    return Double.parseDouble(arg);
                } catch (NumberFormatException nfe) {
                    // swallow - we don't really care
                }
            }
            return null;
        }
    }
}
