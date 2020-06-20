package handling.channel.handler;

import client.*;
import client.inventory.*;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import handling.channel.ChannelServer;
import handling.channel.handler.utils.InventoryHandlerUtils;
import handling.world.World;
import handling.world.party.MaplePartyCharacter;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.Randomizer;
import server.Timer.MapTimer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.*;
import server.quest.MapleQuest;
import server.shops.HiredMerchant;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;
import tools.packet.PetPacket;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class UseCashItemHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, final MapleClient c) {
    c.getPlayer().updateTick(slea.readInt());
    final byte slot = (byte) slea.readShort();
    final int itemId = slea.readInt();
    if (!c.getPlayer().haveItem(itemId)) { // Using item without having one
      c.enableActions();
      return;
    }
    final IItem toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot);
    if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }

    boolean used = false, cc = false;

    switch (itemId) {
      case 5043001: // NPC Teleport Rock
      case 5043000: { // NPC Teleport Rock
        final short questid = slea.readShort();
        final int npcid = slea.readInt();
        final MapleQuest quest = MapleQuest.getInstance(questid);

        if (c.getPlayer().getQuest(quest).getStatus() == 1 && quest.canComplete(c.getPlayer(), npcid)) {
          final int mapId = MapleLifeFactory.getNPCLocation(npcid);
          if (mapId != -1) {
            final MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
            if (map.containsNPC(npcid) && !FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())
                && !FieldLimitType.VipRock.check(map.getFieldLimit())
                && c.getPlayer().getEventInstance() == null) {
              c.getPlayer().changeMap(map, map.getPortal(0));
            }
            used = true;
          } else {
            c.getPlayer().dropMessage(1, "Unknown error has occurred.");
          }
        }
        break;
      }
      case 2320000: // The Teleport Rock
      case 5041000: // VIP Teleport Rock
      case 5040000: // The Teleport Rock
      case 5040001: { // Teleport Coke
        if (slea.readByte() == 0) { // Rocktype
          final MapleMap target = c.getChannelServer().getMapFactory().getMap(slea.readInt());
          if ((itemId == 5041000 && c.getPlayer().isRockMap(target.getId()))
              || (itemId != 5041000 && c.getPlayer().isRegRockMap(target.getId()))) {
            if (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())
                && !FieldLimitType.VipRock.check(target.getFieldLimit())
                && c.getPlayer().getEventInstance() == null) { // Makes
              // sure
              // this
              // map
              // doesn't
              // have
              // a
              // forced
              // return
              // map
              c.getPlayer().changeMap(target, target.getPortal(0));
              used = true;
            }
          }
        } else {
          final MapleCharacter victim = c.getChannelServer().getPlayerStorage()
              .getCharacterByName(slea.readMapleAsciiString());
          if (victim != null && !victim.isGM() && c.getPlayer().getEventInstance() == null
              && victim.getEventInstance() == null) {
            if (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit()) && !FieldLimitType.VipRock
                .check(c.getChannelServer().getMapFactory().getMap(victim.getMapId()).getFieldLimit())) {
              if (itemId == 5041000
                  || (victim.getMapId() / 100000000) == (c.getPlayer().getMapId() / 100000000)) { // Viprock
                // or
                // same
                // continent
                c.getPlayer().changeMap(victim.getMap(),
                    victim.getMap().findClosestSpawnpoint(victim.getPosition()));
                used = true;
              }
            }
          }
        }
        break;
      }
      case 5050000: { // AP Reset
        List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
        final int apto = slea.readInt();
        final int apfrom = slea.readInt();

        if (apto == apfrom) {
          break; // Hack
        }
        final int job = c.getPlayer().getJob();
        final PlayerStats playerst = c.getPlayer().getStat();
        used = true;

        switch (apto) { // AP to
          case 64: // str
            if (playerst.getStr() >= 999) {
              used = false;
            }
            break;
          case 128: // dex
            if (playerst.getDex() >= 999) {
              used = false;
            }
            break;
          case 256: // int
            if (playerst.getInt() >= 999) {
              used = false;
            }
            break;
          case 512: // luk
            if (playerst.getLuk() >= 999) {
              used = false;
            }
            break;
          case 2048: // hp
            if (playerst.getMaxHp() >= 30000) {
              used = false;
            }
            break;
          case 8192: // mp
            if (playerst.getMaxMp() >= 30000) {
              used = false;
            }
            break;
        }
        switch (apfrom) { // AP to
          case 64: // str
            if (playerst.getStr() <= 4 || (c.getPlayer().getJob() % 1000 / 100 == 1 && playerst.getStr() <= 35)) {
              used = false;
            }
            break;
          case 128: // dex
            if (playerst.getDex() <= 4 || (c.getPlayer().getJob() % 1000 / 100 == 3 && playerst.getStr() <= 25)
                || (c.getPlayer().getJob() % 1000 / 100 == 4 && playerst.getLuk() <= 25)
                || (c.getPlayer().getJob() % 1000 / 100 == 5 && playerst.getStr() <= 20)) {
              used = false;
            }
            break;
          case 256: // int
            if (playerst.getInt() <= 4 || (c.getPlayer().getJob() % 1000 / 100 == 2 && playerst.getInt() <= 20)) {
              used = false;
            }
            break;
          case 512: // luk
            if (playerst.getLuk() <= 4) {
              used = false;
            }
            break;
          case 2048: // hp
            if (/*
             * playerst.getMaxMp() < ((c.getPlayer().getLevel() * 14) +
             * 134) ||
             */c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getHpApUsed() >= 10000) {
              used = false;
            }
            break;
          case 8192: // mp
            if (/*
             * playerst.getMaxMp() < ((c.getPlayer().getLevel() * 14) +
             * 134) ||
             */c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getHpApUsed() >= 10000) {
              used = false;
            }
            break;
        }
        if (used) {
          switch (apto) { // AP to
            case 64: { // str
              final int toSet = playerst.getStr() + 1;
              playerst.setStr((short) toSet);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, toSet));
              break;
            }
            case 128: { // dex
              final int toSet = playerst.getDex() + 1;
              playerst.setDex((short) toSet);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, toSet));
              break;
            }
            case 256: { // int
              final int toSet = playerst.getInt() + 1;
              playerst.setInt((short) toSet);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, toSet));
              break;
            }
            case 512: { // luk
              final int toSet = playerst.getLuk() + 1;
              playerst.setLuk((short) toSet);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, toSet));
              break;
            }
            case 2048: // hp
              int maxhp = playerst.getMaxHp();

              if (job == 0) { // Beginner
                maxhp += Randomizer.rand(8, 12);
              } else if ((job >= 100 && job <= 132) || (job >= 3200 && job <= 3212)) { // Warrior
                ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                maxhp += Randomizer.rand(20, 25);
                if (improvingMaxHPLevel >= 1) {
                  maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                }
              } else if ((job >= 200 && job <= 232) || (GameConstants.isEvan(job))) { // Magician
                maxhp += Randomizer.rand(10, 20);
              } else if ((job >= 300 && job <= 322) || (job >= 400 && job <= 434) || (job >= 1300 && job <= 1312)
                  || (job >= 1400 && job <= 1412) || (job >= 3300 && job <= 3312)) { // Bowman
                maxhp += Randomizer.rand(16, 20);
              } else if ((job >= 500 && job <= 522) || (job >= 3500 && job <= 3512)) { // Pirate
                ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                maxhp += Randomizer.rand(18, 22);
                if (improvingMaxHPLevel >= 1) {
                  maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                }
              } else if (job >= 1500 && job <= 1512) { // Pirate
                ISkill improvingMaxHP = SkillFactory.getSkill(15100000);
                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                maxhp += Randomizer.rand(18, 22);
                if (improvingMaxHPLevel >= 1) {
                  maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                }
              } else if (job >= 1100 && job <= 1112) { // Soul Master
                ISkill improvingMaxHP = SkillFactory.getSkill(11000000);
                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                maxhp += Randomizer.rand(36, 42);
                if (improvingMaxHPLevel >= 1) {
                  maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                }
              } else if (job >= 1200 && job <= 1212) { // Flame Wizard
                maxhp += Randomizer.rand(15, 21);
              } else if (job >= 2000 && job <= 2112) { // Aran
                maxhp += Randomizer.rand(40, 50);
              } else { // GameMaster
                maxhp += Randomizer.rand(50, 100);
              }
              maxhp = (short) Math.min(30000, Math.abs(maxhp));
              c.getPlayer().setHpApUsed((short) (c.getPlayer().getHpApUsed() + 1));
              playerst.setMaxHp(maxhp);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, (int) maxhp));
              break;

            case 8192: // mp
              int maxmp = playerst.getMaxMp();

              if (job == 0) { // Beginner
                maxmp += Randomizer.rand(6, 8);
              } else if (job >= 100 && job <= 132) { // Warrior
                maxmp += Randomizer.rand(5, 7);
              } else if ((job >= 200 && job <= 232) || (GameConstants.isEvan(job)) || (job >= 3200 && job <= 3212)) { // Magician
                ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
                int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
                maxmp += Randomizer.rand(18, 20);
                if (improvingMaxMPLevel >= 1) {
                  maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getY() * 2;
                }
              } else if ((job >= 300 && job <= 322) || (job >= 400 && job <= 434) || (job >= 500 && job <= 522)
                  || (job >= 3200 && job <= 3212) || (job >= 3500 && job <= 3512)
                  || (job >= 1300 && job <= 1312) || (job >= 1400 && job <= 1412)
                  || (job >= 1500 && job <= 1512)) { // Bowman
                maxmp += Randomizer.rand(10, 12);
              } else if (job >= 1100 && job <= 1112) { // Soul Master
                maxmp += Randomizer.rand(6, 9);
              } else if (job >= 1200 && job <= 1212) { // Flame Wizard
                ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
                int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
                maxmp += Randomizer.rand(18, 20);
                if (improvingMaxMPLevel >= 1) {
                  maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getY() * 2;
                }
              } else if (job >= 2000 && job <= 2112) { // Aran
                maxmp += Randomizer.rand(6, 9);
              } else { // GameMaster
                maxmp += Randomizer.rand(50, 100);
              }
              maxmp = (short) Math.min(30000, Math.abs(maxmp));
              c.getPlayer().setHpApUsed((short) (c.getPlayer().getHpApUsed() + 1));
              playerst.setMaxMp(maxmp);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, (int) maxmp));
              break;
          }
          switch (apfrom) { // AP from
            case 64: { // str
              final int toSet = playerst.getStr() - 1;
              playerst.setStr((short) toSet);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, toSet));
              break;
            }
            case 128: { // dex
              final int toSet = playerst.getDex() - 1;
              playerst.setDex((short) toSet);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, toSet));
              break;
            }
            case 256: { // int
              final int toSet = playerst.getInt() - 1;
              playerst.setInt((short) toSet);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, toSet));
              break;
            }
            case 512: { // luk
              final int toSet = playerst.getLuk() - 1;
              playerst.setLuk((short) toSet);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, toSet));
              break;
            }
            case 2048: // HP
              int maxhp = playerst.getMaxHp();
              if (job == 0) { // Beginner
                maxhp -= 12;
              } else if (job >= 100 && job <= 132) { // Warrior
                ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                maxhp -= 24;
                if (improvingMaxHPLevel >= 1) {
                  maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                }
              } else if (job >= 200 && job <= 232) { // Magician
                maxhp -= 10;
              } else if ((job >= 300 && job <= 322) || (job >= 400 && job <= 434) || (job >= 1300 && job <= 1312)
                  || (job >= 1400 && job <= 1412) || (job >= 3300 && job <= 3312)
                  || (job >= 3500 && job <= 3512)) { // Bowman, Thief
                maxhp -= 15;
              } else if (job >= 500 && job <= 522) { // Pirate
                ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                maxhp -= 15;
                if (improvingMaxHPLevel > 0) {
                  maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                }
              } else if (job >= 1500 && job <= 1512) { // Pirate
                ISkill improvingMaxHP = SkillFactory.getSkill(15100000);
                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                maxhp -= 15;
                if (improvingMaxHPLevel > 0) {
                  maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                }
              } else if (job >= 1100 && job <= 1112) { // Soul Master
                ISkill improvingMaxHP = SkillFactory.getSkill(11000000);
                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                maxhp -= 27;
                if (improvingMaxHPLevel >= 1) {
                  maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                }
              } else if (job >= 1200 && job <= 1212) { // Flame Wizard
                maxhp -= 12;
              } else if ((job >= 2000 && job <= 2112) || (job >= 3200 && job <= 3212)) { // Aran
                maxhp -= 40;
              } else { // GameMaster
                maxhp -= 20;
              }
              c.getPlayer().setHpApUsed((short) (c.getPlayer().getHpApUsed() - 1));
              playerst.setHp(maxhp);
              playerst.setMaxHp(maxhp);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, (int) maxhp));
              break;
            case 8192: // MP
              int maxmp = playerst.getMaxMp();
              if (job == 0) { // Beginner
                maxmp -= 8;
              } else if (job >= 100 && job <= 132) { // Warrior
                maxmp -= 4;
              } else if (job >= 200 && job <= 232) { // Magician
                ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
                int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
                maxmp -= 20;
                if (improvingMaxMPLevel >= 1) {
                  maxmp -= improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
                }
              } else if ((job >= 500 && job <= 522) || (job >= 300 && job <= 322) || (job >= 400 && job <= 434)
                  || (job >= 1300 && job <= 1312) || (job >= 1400 && job <= 1412)
                  || (job >= 1500 && job <= 1512) || (job >= 3300 && job <= 3312)
                  || (job >= 3500 && job <= 3512)) { // Pirate,
                // Bowman. Thief
                maxmp -= 10;
              } else if (job >= 1100 && job <= 1112) { // Soul Master
                maxmp -= 6;
              } else if (job >= 1200 && job <= 1212) { // Flame Wizard
                ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
                int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
                maxmp -= 25;
                if (improvingMaxMPLevel >= 1) {
                  maxmp -= improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
                }
              } else if (job >= 2000 && job <= 2112) { // Aran
                maxmp -= 5;
              } else { // GameMaster
                maxmp -= 20;
              }
              c.getPlayer().setHpApUsed((short) (c.getPlayer().getHpApUsed() - 1));
              playerst.setMp(maxmp);
              playerst.setMaxMp(maxmp);
              statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, (int) maxmp));
              break;
          }
          c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, c.getPlayer().getJob()));
        }
        break;
      }
      case 5050001: // SP Reset (1st job)
      case 5050002: // SP Reset (2nd job)
      case 5050003: // SP Reset (3rd job)
      case 5050004: // SP Reset (4th job)
      case 5050005: // evan sp resets
      case 5050006:
      case 5050007:
      case 5050008:
      case 5050009: {
        if (itemId >= 5050005 && !GameConstants.isEvan(c.getPlayer().getJob())) {
          break;
        } // well i dont really care other than this o.o
        if (itemId < 5050005 && GameConstants.isEvan(c.getPlayer().getJob())) {
          break;
        } // well i dont really care other than this o.o
        int skill1 = slea.readInt();
        int skill2 = slea.readInt();
        for (int i : GameConstants.blockedSkills) {
          if (skill1 == i) {
            c.getPlayer().dropMessage(1, "You may not add this skill.");
            return;
          }
        }

        ISkill skillSPTo = SkillFactory.getSkill(skill1);
        ISkill skillSPFrom = SkillFactory.getSkill(skill2);

        if (skillSPTo.isBeginnerSkill() || skillSPFrom.isBeginnerSkill()) {
          break;
        }
        if (GameConstants.getSkillBookForSkill(skill1) != GameConstants.getSkillBookForSkill(skill2)) { // resistance
          // evan
          break;
        }
        if (GameConstants.getJobNumber(skill1 / 10000) > GameConstants.getJobNumber(skill2 / 10000)) { // putting
          // 3rd
          // job
          // skillpoints
          // into
          // 4th
          // job
          // for
          // example
          break;
        }
        if ((c.getPlayer().getSkillLevel(skillSPTo) + 1 <= skillSPTo.getMaxLevel())
            && c.getPlayer().getSkillLevel(skillSPFrom) > 0
            && skillSPTo.canBeLearnedBy(c.getPlayer().getJob())) {
          if (skillSPTo.isFourthJob()
              && (c.getPlayer().getSkillLevel(skillSPTo) + 1 > c.getPlayer().getMasterLevel(skillSPTo))) {
            break;
          }
          if (itemId >= 5050005) {
            if (GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2
                && GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2 + 1) {
              break;
            }
          }
          c.getPlayer().changeSkillLevel(skillSPFrom, (byte) (c.getPlayer().getSkillLevel(skillSPFrom) - 1),
              c.getPlayer().getMasterLevel(skillSPFrom));
          c.getPlayer().changeSkillLevel(skillSPTo, (byte) (c.getPlayer().getSkillLevel(skillSPTo) + 1),
              c.getPlayer().getMasterLevel(skillSPTo));
          used = true;
        }
        break;
      }
      case 5060000: { // Item Tag
        final IItem item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readByte());

        if (item != null && item.getOwner().equals("")) {
          boolean change = true;
          for (String z : GameConstants.RESERVED) {
            if (c.getPlayer().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
              change = false;
            }
          }
          if (change) {
            item.setOwner(c.getPlayer().getName());
            c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
            used = true;
          }
        }
        break;
      }
      case 5062000: { // miracle cube
        final IItem item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
        if (item != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
          final Equip eq = (Equip) item;
          if (eq.getState() >= 5) {
            eq.renewPotential();
            c.getSession().write(MaplePacketCreator.scrolledItem(toUse, item, false, true));
            c.getPlayer().getMap()
                .broadcastMessage(MaplePacketCreator.getMiracleCubeEffect(c.getPlayer().getId(), true));
            c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
            MapleInventoryManipulator.addById(c, 2430112, (short) 1,
                "Cubed on " + FileoutputUtil.CurrentReadable_Date());
            used = true;
          } else {
            c.getPlayer().dropMessage(5, "This item's Potential cannot be reset.");
          }
        } else {
          c.getPlayer().getMap()
              .broadcastMessage(MaplePacketCreator.getMiracleCubeEffect(c.getPlayer().getId(), false));
        }
        break;
      }
      case 5520001: // p.karma
      case 5520000: { // Karma
        final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
        final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());

        if (item != null && !ItemFlag.KARMA_EQ.check(item.getFlag()) && !ItemFlag.KARMA_USE.check(item.getFlag())) {
          if ((itemId == 5520000 && MapleItemInformationProvider.getInstance().isKarmaEnabled(item.getItemId()))
              || (itemId == 5520001
              && MapleItemInformationProvider.getInstance().isPKarmaEnabled(item.getItemId()))) {
            byte flag = item.getFlag();
            if (type == MapleInventoryType.EQUIP) {
              flag |= ItemFlag.KARMA_EQ.getValue();
            } else {
              flag |= ItemFlag.KARMA_USE.getValue();
            }
            item.setFlag(flag);

            c.getPlayer().forceReAddItem_Flag(item, type);
            used = true;
          }
        }
        break;
      }
      case 5570000: { // Vicious Hammer
        slea.readInt(); // Inventory type, Hammered eq is always EQ.
        final Equip item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP)
            .getItem((byte) slea.readInt());
        // another int here, D3 49 DC 00
        if (item != null) {
          if (GameConstants.canHammer(item.getItemId())
              && MapleItemInformationProvider.getInstance().getSlots(item.getItemId()) > 0
              && item.getViciousHammer() <= 2) {
            item.setViciousHammer((byte) (item.getViciousHammer() + 1));
            item.setUpgradeSlots((byte) (item.getUpgradeSlots() + 1));
            c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIP);
            c.getSession().write(MTSCSPacket.ViciousHammer(true, (byte) item.getViciousHammer()));
            used = true;
          } else {
            c.getPlayer().dropMessage(5, "You may not use it on this item.");
            cc = true;
          }
        }
        break;
      }
      case 5610001:
      case 5610000: { // Vega 30
        slea.readInt(); // Inventory type, always eq
        final byte dst = (byte) slea.readInt();
        slea.readInt(); // Inventory type, always use
        final byte src = (byte) slea.readInt();
        used = InventoryHandlerUtils.UseUpgradeScroll(src, dst, (byte) 0, c, c.getPlayer(), itemId, (byte) 0);
        c.getSession().write(MTSCSPacket.VegasScroll(67));//67 is working ON

        MapTimer.getInstance().schedule(new Runnable() {

          @Override
          public void run() {
            IItem item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
            c.getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.EQUIP, item, false));
            c.getSession().write(MaplePacketCreator.scrolledItem(item, item, false, false));
            c.getSession().write(MTSCSPacket.VegasScroll(70));
            c.enableActions();

          }
        }, 2000);

        used = true;
        break;
      }
      case 5060001: { // Sealing Lock
        final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
        final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
        // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
        if (item != null && item.getExpiration() == -1) {
          byte flag = item.getFlag();
          flag |= ItemFlag.LOCK.getValue();
          item.setFlag(flag);

          c.getPlayer().forceReAddItem_Flag(item, type);
          used = true;
        }
        break;
      }
      case 5061000: { // Sealing Lock 7 days
        final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
        final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
        // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
        if (item != null && item.getExpiration() == -1) {
          byte flag = item.getFlag();
          flag |= ItemFlag.LOCK.getValue();
          item.setFlag(flag);
          long expiration = new Date().getTime() + (long) (7 * 1000L * 60 * 60 * 24);
          item.setExpiration(expiration);
          c.getPlayer().forceReAddItem_Flag(item, type);
          used = true;
        }
        break;
      }
      case 5061001: { // Sealing Lock 30 days
        final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
        final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
        // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
        if (item != null && item.getExpiration() == -1) {
          byte flag = item.getFlag();
          flag |= ItemFlag.LOCK.getValue();
          item.setFlag(flag);
          long expiration = new Date().getTime() + (long) (30 * 1000L * 60 * 60 * 24);
          item.setExpiration(expiration);

          c.getPlayer().forceReAddItem_Flag(item, type);
          used = true;
        }
        break;
      }
      case 5061002: { // Sealing Lock 90 days
        final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
        final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
        // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
        if (item != null && item.getExpiration() == -1) {
          byte flag = item.getFlag();
          flag |= ItemFlag.LOCK.getValue();
          item.setFlag(flag);

          item.setExpiration(System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000));

          c.getPlayer().forceReAddItem_Flag(item, type);
          used = true;
        }
        break;
      }
      case 5061003: { // Sealing Lock year
        final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
        final IItem item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
        // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
        if (item != null && item.getExpiration() == -1) {
          byte flag = item.getFlag();
          flag |= ItemFlag.LOCK.getValue();
          item.setFlag(flag);

          long expiration = new Date().getTime() + (long) (365 * 1000L * 60 * 60 * 24);
          item.setExpiration(expiration);

          c.getPlayer().forceReAddItem_Flag(item, type);
          used = true;
        }
        break;
      }
      case 5060003: {// peanut
        IItem item = c.getPlayer().getInventory(MapleInventoryType.ETC).findById(4170023);
        if (item == null || item.getQuantity() <= 0) { // hacking{
          return;
        }
        if (InventoryHandlerUtils.getIncubatedItems(c)) {
          MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, item.getPosition(), (short) 1,
              false);
          used = true;
        }
      }
      break;
      case 5070000: { // Megaphone
        if (c.getPlayer().getLevel() < 10) {
          c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
          break;
        }
        if (!c.getChannelServer().getMegaphoneMuteState()) {
          final String message = slea.readMapleAsciiString();

          if (message.length() > 65) {
            break;
          }
          final StringBuilder sb = new StringBuilder();
          InventoryHandlerUtils.addMedalString(c.getPlayer(), sb);
          sb.append(c.getPlayer().getName());
          sb.append(" : ");
          sb.append(message);

          c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(2, sb.toString()));
          used = true;
        } else {
          c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
        }
        break;
      }
      case 5071000: { // Megaphone
        if (c.getPlayer().getLevel() < 10) {
          c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
          break;
        }
        if (!c.getChannelServer().getMegaphoneMuteState()) {
          final String message = slea.readMapleAsciiString();

          if (message.length() > 65) {
            break;
          }
          final StringBuilder sb = new StringBuilder();
          InventoryHandlerUtils.addMedalString(c.getPlayer(), sb);
          sb.append(c.getPlayer().getName());
          sb.append(" : ");
          sb.append(message);

          c.getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(2, sb.toString()));
          used = true;
        } else {
          c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
        }
        break;
      }
      case 5077000: { // 3 line Megaphone
        if (c.getPlayer().getLevel() < 10) {
          c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
          break;
        }
        if (!c.getChannelServer().getMegaphoneMuteState()) {
          final byte numLines = slea.readByte();
          if (numLines > 3) {
            return;
          }
          final List<String> messages = new LinkedList<String>();
          String message;
          for (int i = 0; i < numLines; i++) {
            message = slea.readMapleAsciiString();
            if (message.length() > 65) {
              break;
            }
            messages.add(c.getPlayer().getName() + " : " + message);
          }
          final boolean ear = slea.readByte() > 0;

          World.Broadcast.broadcastSmega(MaplePacketCreator.tripleSmega(messages, ear, c.getChannel()));
          used = true;
        } else {
          c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
        }
        break;
      }
      case 5074000: { // Skull Megaphone
        if (c.getPlayer().getLevel() < 10) {
          c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
          break;
        }
        if (!c.getChannelServer().getMegaphoneMuteState()) {
          final String message = slea.readMapleAsciiString();

          if (message.length() > 65) {
            break;
          }
          final StringBuilder sb = new StringBuilder();
          InventoryHandlerUtils.addMedalString(c.getPlayer(), sb);
          sb.append(c.getPlayer().getName());
          sb.append(" : ");
          sb.append(message);

          final boolean ear = slea.readByte() != 0;

          World.Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(20, c.getChannel(), sb.toString(), ear));
          used = true;
        } else {
          c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
        }
        break;
      }
      case 5072000: { // Super Megaphone
        if (c.getPlayer().getLevel() < 10) {
          c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
          break;
        }
        if (!c.getChannelServer().getMegaphoneMuteState()) {
          final String message = slea.readMapleAsciiString();

          if (message.length() > 65) {
            break;
          }
          final StringBuilder sb = new StringBuilder();
          InventoryHandlerUtils.addMedalString(c.getPlayer(), sb);
          sb.append(c.getPlayer().getName());
          sb.append(" : ");
          sb.append(message);

          final boolean ear = slea.readByte() != 0;

          World.Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(3, c.getChannel(), sb.toString(), ear));
          used = true;
        } else {
          c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
        }
        break;
      }
      case 5076000: { // Item Megaphone
        if (c.getPlayer().getLevel() < 10) {
          c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
          break;
        }
        if (!c.getChannelServer().getMegaphoneMuteState()) {
          final String message = slea.readMapleAsciiString();

          if (message.length() > 65) {
            break;
          }
          final StringBuilder sb = new StringBuilder();
          InventoryHandlerUtils.addMedalString(c.getPlayer(), sb);
          sb.append(c.getPlayer().getName());
          sb.append(" : ");
          sb.append(message);

          final boolean ear = slea.readByte() > 0;

          IItem item = null;
          if (slea.readByte() == 1) { // item
            byte invType = (byte) slea.readInt();
            int pos = slea.readInt();
            if (pos <= 0) {
              invType = -1;
            }
            item = c.getPlayer().getInventory(MapleInventoryType.getByType(invType)).getItem((byte) pos);
          }
          World.Broadcast
              .broadcastSmega(MaplePacketCreator.itemMegaphone(sb.toString(), ear, c.getChannel(), item));
          used = true;
        } else {
          c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
        }
        break;
      }
      case 5075000: // MapleTV Messenger
      case 5075001: // MapleTV Star Messenger
      case 5075002: { // MapleTV Heart Messenger
        c.getPlayer().dropMessage(5, "There are no MapleTVs to broadcast the message to.");
        break;
      }
      case 5075003:
      case 5075004:
      case 5075005: {
        if (c.getPlayer().getLevel() < 10) {
          c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
          break;
        }
        int tvType = itemId % 10;
        if (tvType == 3) {
          slea.readByte(); // who knows
        }
        boolean ear = tvType != 1 && tvType != 2 && slea.readByte() > 1; // for
        // tvType
        // 1/2,
        // there
        // is
        // no
        // byte.
        MapleCharacter victim = tvType == 1 || tvType == 4 ? null
            : c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString()); // for
        // tvType
        // 4,
        // there
        // is
        // no
        // string.
        if (tvType == 0 || tvType == 3) { // doesn't allow two
          victim = null;
        } else if (victim == null) {
          c.getPlayer().dropMessage(1, "That character is not in the channel.");
          break;
        }
        String message = slea.readMapleAsciiString();
        World.Broadcast.broadcastSmega(
            MaplePacketCreator.serverNotice(3, c.getChannel(), c.getPlayer().getName() + " : " + message, ear));
        used = true;
        break;
      }
      case 5090100: // Wedding Invitation Card
      case 5090000: { // Note
        final String sendTo = slea.readMapleAsciiString();
        final String msg = slea.readMapleAsciiString();
        c.getPlayer().sendNote(sendTo, msg);
        used = true;
        break;
      }
      case 5100000: { // Congratulatory Song
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange("Jukebox/Congratulation"));
        used = true;
        break;
      }
      case 5170000: { // Pet name change
        MaplePet pet = c.getPlayer().getPet(0);
        if (pet == null) {
          return;
        }
        int slo = 0;

        String nName = slea.readMapleAsciiString();
        for (String z : GameConstants.RESERVED) {
          if (pet.getName().indexOf(z) != -1 || nName.indexOf(z) != -1) {
            break;
          }
        }
        if (MapleCharacterUtil.canChangePetName(nName)) {
          pet.setName(nName);
          c.getSession().write(PetPacket.updatePet(pet, c.getPlayer().getInventory(MapleInventoryType.CASH)
              .getItem((byte) pet.getInventoryPosition())));
          c.getSession().write(MaplePacketCreator.enableActions());
          c.getPlayer().getMap().broadcastMessage(MTSCSPacket.changePetName(c.getPlayer(), nName, slo));
          used = true;
        }
        break;
      }
      case 5240000:
      case 5240001:
      case 5240002:
      case 5240003:
      case 5240004:
      case 5240005:
      case 5240006:
      case 5240007:
      case 5240008:
      case 5240009:
      case 5240010:
      case 5240011:
      case 5240012:
      case 5240013:
      case 5240014:
      case 5240015:
      case 5240016:
      case 5240017:
      case 5240018:
      case 5240019:
      case 5240020:
      case 5240021:
      case 5240022:
      case 5240023:
      case 5240024:
      case 5240025:
      case 5240026:
      case 5240027:
      case 5240028: { // Pet food
        MaplePet pet = c.getPlayer().getPet(0);

        if (pet == null) {
          break;
        }
        if (!pet.canConsume(itemId)) {
          pet = c.getPlayer().getPet(1);
          if (pet != null) {
            if (!pet.canConsume(itemId)) {
              pet = c.getPlayer().getPet(2);
              if (pet != null) {
                if (!pet.canConsume(itemId)) {
                  break;
                }
              } else {
                break;
              }
            }
          } else {
            break;
          }
        }
        final byte petindex = c.getPlayer().getPetIndex(pet);
        pet.setFullness(100);
        if (pet.getCloseness() < 30000) {
          if (pet.getCloseness() + 100 > 30000) {
            pet.setCloseness(30000);
          } else {
            pet.setCloseness(pet.getCloseness() + 100);
          }
          if (pet.getCloseness() >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
            pet.setLevel(pet.getLevel() + 1);
            c.getSession().write(PetPacket.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
            c.getPlayer().getMap().broadcastMessage(PetPacket.showPetLevelUp(c.getPlayer(), petindex));
          }
        }
        c.getSession().write(PetPacket.updatePet(pet,
            c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition())));
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(),
            PetPacket.commandResponse(c.getPlayer().getId(), (byte) 1, petindex, true, true), true);
        used = true;
        break;
      }
      case 5230000: {// owl of minerva
        final int itemSearch = slea.readInt();
        final List<HiredMerchant> hms = c.getChannelServer().searchMerchant(itemSearch);
        if (hms.size() > 0) {
          c.getSession().write(MaplePacketCreator.getOwlSearched(itemSearch, hms));
          used = true;
        } else {
          c.getPlayer().dropMessage(1, "Unable to find the item.");
        }
        break;
      }
      case 5281001: // idk, but probably
      case 5280001: // Gas Skill
      case 5281000: { // Passed gas
        Rectangle bounds = new Rectangle((int) c.getPlayer().getPosition().getX(),
            (int) c.getPlayer().getPosition().getY(), 1, 1);
        MapleMist mist = new MapleMist(bounds, c.getPlayer());
        c.getPlayer().getMap().spawnMist(mist, 10000, true);
        c.getPlayer().getMap().broadcastMessage(
            MaplePacketCreator.getChatText(c.getPlayer().getId(), "Oh no, I farted!", false, 1));
        c.getSession().write(MaplePacketCreator.enableActions());
        used = true;
        break;
      }
      case 5370000: { // Chalkboard
        c.getPlayer().setChalkboard(slea.readMapleAsciiString());
        break;
      }
      case 5370001: { // BlackBoard
        if (c.getPlayer().getMapId() / 1000000 == 910) {
          c.getPlayer().setChalkboard(slea.readMapleAsciiString());
        }
        break;
      }
      case 5390000: // Diablo Messenger
      case 5390001: // Cloud 9 Messenger
      case 5390002: // Loveholic Messenger
      case 5390003: // New Year Megassenger 1
      case 5390004: // New Year Megassenger 2
      case 5390005: // Cute Tiger Messenger
      case 5390006: // Tiger Roar's Messenger
      case 5390007: // ?
      case 5390008: { // soccer Messenger
        if (c.getPlayer().getLevel() < 10) {
          c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
          break;
        }
        if (!c.getChannelServer().getMegaphoneMuteState()) {
          final List<String> lines = new LinkedList<String>();
          for (int i = 0; i < 4; i++) {
            lines.add(slea.readMapleAsciiString());
          }
          // if (text.length() > 55) {
          // break;
          // }
          final boolean ear = slea.readByte() != 0;
          World.Broadcast.broadcastSmega(
              MaplePacketCreator.getAvatarMega(c.getPlayer(), c.getChannel(), itemId, lines, ear));
          used = true;
        } else {
          c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
        }
        break;
      }
      case 5450000: { // Mu Mu the Travelling Merchant
        MapleShopFactory.getInstance().getShop(11100).sendShop(c);
        used = true;
        break;
      }
      case 5300000: // Fungus scroll
      case 5300001: // Olinker delight
      case 5300002: { // Zeta Nightmare delight
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        ii.getItemEffect(itemId).applyTo(c.getPlayer());
        used = true;
        break;
      }
      default:
        if (itemId / 10000 == 512) {
          final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
          final String msg = ii.getMsg(itemId).replaceFirst("%s", c.getPlayer().getName()).replaceFirst("%s",
              slea.readMapleAsciiString());
          c.getPlayer().getMap().startMapEffect(msg, itemId);

          final int buff = ii.getStateChangeItem(itemId);
          if (buff != 0) {
            for (MapleCharacter mChar : c.getPlayer().getMap().getCharactersThreadsafe()) {
              ii.getItemEffect(buff).applyTo(mChar);
            }
          }
          used = true;
        } else if (itemId / 10000 == 510) {
          c.getPlayer().getMap().startJukebox(c.getPlayer().getName(), itemId);
          used = true;
        } else if (itemId / 10000 == 520) {
          final int mesars = MapleItemInformationProvider.getInstance().getMeso(itemId);
          boolean isMapleCoin = false;
          int totalMaplePoints = 0;
          switch (itemId) {
            case 5200009: // 1 million MaplePoints
              totalMaplePoints = 1000000;
              isMapleCoin = true;
              break;
            case 5200010: // 10kNx
              totalMaplePoints = 10000;
              isMapleCoin = true;
              break;
            default:

          }
          if (isMapleCoin) {
            c.getPlayer().removeItem(itemId, -1);
            c.getPlayer().modifyCSPoints(2, totalMaplePoints, false);
            c.getPlayer().dropMessage(1, "You've gained " + totalMaplePoints + " maple points");
            World.Broadcast
                .broadcastMessage(MaplePacketCreator.serverNotice(6, itemId, "A lucky person just used a {"
                    + MapleItemInformationProvider.getInstance().getName(itemId) + "}"));
            c.enableActions();
            return;
          }
          if (mesars > 0 && c.getPlayer().getMeso() < (Integer.MAX_VALUE - mesars)) {
            used = true;
            if (Math.random() > 0.1) {
              final int gainmes = Randomizer.nextInt(mesars);
              c.getPlayer().gainMeso(gainmes, false);
              c.getSession().write(MTSCSPacket.sendMesobagSuccess(gainmes));
            } else {
              c.getSession().write(MTSCSPacket.sendMesobagFailed());
            }
          }
        } else if (itemId / 10000 == 562) {
          InventoryHandlerUtils.UseSkillBook(slot, itemId, c, c.getPlayer()); // this should
          // handle
          // removing
        } else if (itemId / 10000 == 553) {
          InventoryHandlerUtils.UseRewardItem(slot, itemId, c, c.getPlayer());// this too
        } else {
          System.out.println("Unhandled CS item : " + itemId);
          System.out.println(slea.toString(true));
        }
        break;
    }

    if (used) {
      MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (short) 1, true, false);
    }
    c.getSession().write(MaplePacketCreator.enableActions());
    if (cc) {
      if (!c.getPlayer().isAlive() || c.getPlayer().getEventInstance() != null
          || FieldLimitType.ChannelSwitch.check(c.getPlayer().getMap().getFieldLimit())) {
        c.getPlayer().dropMessage(1, "Auto change channel failed.");
        return;
      }
      c.getPlayer().dropMessage(5, "Auto changing channels. Please wait.");
      c.getPlayer().changeChannel(c.getChannel() == ChannelServer.getChannelCount() ? 1 : (c.getChannel() + 1));
    }
  }

  public static void PickupItemAtSpot(final MapleClient c, final MapleCharacter chr, final MapleMapObject ob) {
    if (ob == null) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    final MapleMapItem mapitem = (MapleMapItem) ob;
    final Lock lock = mapitem.getLock();
    lock.lock();
    try {
      if (mapitem.isPickedUp() || !mapitem.canLoot(c)) {
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      if (mapitem.getOwner() != chr.getId() && ((!mapitem.isPlayerDrop() && mapitem.getDropType() == 0)
          || (mapitem.isPlayerDrop() && chr.getMap().getEverlast()))) {
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      if (!mapitem.isPlayerDrop() && mapitem.getDropType() == 1 && mapitem.getOwner() != chr.getId()
          && (chr.getParty() == null || chr.getParty().getMemberById(mapitem.getOwner()) == null)) {
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      if (mapitem.isPlayerDrop() && mapitem.getOwner() != chr.getId()) { // cannot
        // vacuum
        // player
        // drops
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      if (mapitem.getMeso() > 0) {
        if (chr.getParty() != null && mapitem.getOwner() != chr.getId()) {
          final List<MapleCharacter> toGive = new LinkedList<MapleCharacter>();
          for (MaplePartyCharacter z : chr.getParty().getMembers()) {
            MapleCharacter m = chr.getMap().getCharacterById(z.getId());
            if (m != null) {
              toGive.add(m);
            }
          }
          for (final MapleCharacter m : toGive) {
            m.gainMeso(
                mapitem.getMeso() / toGive.size()
                    + (m.getStat().hasPartyBonus ? (int) (mapitem.getMeso() / 20.0) : 0),
                true, true);
          }
        } else {
          chr.gainMeso(mapitem.getMeso(), true, true);
        }
        InventoryHandlerUtils.removeItem(chr, mapitem, ob);
      } else {
        if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItem().getItemId())) {
          c.getSession().write(MaplePacketCreator.enableActions());
          c.getPlayer().dropMessage(5, "This item cannot be picked up.");
        } else if (InventoryHandlerUtils.useItem(c, mapitem.getItemId())) {
          InventoryHandlerUtils.removeItem(c.getPlayer(), mapitem, ob);
        } else if (MapleInventoryManipulator.checkSpace(c, mapitem.getItem().getItemId(),
            mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
          if (mapitem.getItem().getQuantity() >= 50
              && GameConstants.isUpgradeScroll(mapitem.getItem().getItemId())) {
            FileoutputUtil.logUsers(chr.getName(), "Player picked up " + mapitem.getItem().getQuantity()
                + " of " + mapitem.getItem().getItemId());
          }
          if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true,
              mapitem.getDropper() instanceof MapleMonster)) {
            InventoryHandlerUtils.removeItem(chr, mapitem, ob);
          }
        } else {
          c.getSession().write(MaplePacketCreator.getInventoryFull());
          c.getSession().write(MaplePacketCreator.getShowInventoryFull());
          c.getSession().write(MaplePacketCreator.enableActions());
        }
      }
    } finally {
      lock.unlock();
    }

  }

}
