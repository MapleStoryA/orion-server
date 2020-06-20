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

import client.*;
import client.inventory.IEquip;
import client.inventory.IEquip.ScrollResult;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.world.party.MaplePartyCharacter;
import server.*;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;

import java.util.List;
import java.util.Map;

public class InventoryHandlerUtils {


  public static final boolean UseUpgradeScroll(final byte slot, final byte dst, final byte ws, final MapleClient c,
                                               final MapleCharacter chr, final byte type) {
    return UseUpgradeScroll(slot, dst, ws, c, chr, 0, type);
  }

  public static final boolean UseUpgradeScroll(final byte slot, final byte dst, final byte ws, final MapleClient c,
                                               final MapleCharacter chr, final int vegas, final byte type) {
    boolean whiteScroll = false; // white scroll being used?
    boolean legendarySpirit = false; // legendary spirit skill
    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

    if ((ws & 2) == 2) {
      whiteScroll = true;
    }

    IEquip toScroll;
    if (dst < 0) {
      toScroll = (IEquip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
    } else { // legendary spirit
      legendarySpirit = true;
      toScroll = (IEquip) chr.getInventory(MapleInventoryType.EQUIP).getItem(dst);
    }
    if (toScroll == null) {
      return false;
    }
    final byte oldLevel = toScroll.getLevel();
    final byte oldEnhance = toScroll.getEnhance();
    final byte oldState = toScroll.getState();
    final byte oldFlag = toScroll.getFlag();
    final byte oldSlots = toScroll.getUpgradeSlots();

    IItem scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
    if (scroll == null) {
      c.getSession().write(MaplePacketCreator.getInventoryFull());
      return false;
    }
    if (!GameConstants.isSpecialScroll(scroll.getItemId()) && !GameConstants.isCleanSlate(scroll.getItemId())
        && !GameConstants.isEquipScroll(scroll.getItemId())
        && !GameConstants.isPotentialScroll(scroll.getItemId())) {
      if (toScroll.getUpgradeSlots() < 1) {
        c.getSession().write(MaplePacketCreator.getInventoryFull());
        return false;
      }
    } else if (GameConstants.isEquipScroll(scroll.getItemId())) {
      if (toScroll.getUpgradeSlots() >= 1 || toScroll.getEnhance() >= 100 || vegas > 0
          || ii.isCash(toScroll.getItemId())) {
        c.getSession().write(MaplePacketCreator.getInventoryFull());
        return false;
      }
    } else if (GameConstants.isPotentialScroll(scroll.getItemId())) {
      if (toScroll.getState() >= 1 || (toScroll.getLevel() == 0 && toScroll.getUpgradeSlots() == 0) || vegas > 0
          || ii.isCash(toScroll.getItemId())) {
        c.getSession().write(MaplePacketCreator.getInventoryFull());
        return false;
      }
    }
    if (!GameConstants.canScroll(toScroll.getItemId()) && !GameConstants.isChaosScroll(toScroll.getItemId())) {
      c.getSession().write(MaplePacketCreator.getInventoryFull());
      return false;
    }
    if ((GameConstants.isCleanSlate(scroll.getItemId()) || GameConstants.isTablet(scroll.getItemId())
        || GameConstants.isChaosScroll(scroll.getItemId())) && (vegas > 0 || ii.isCash(toScroll.getItemId()))) {
      c.getSession().write(MaplePacketCreator.getInventoryFull());
      return false;
    }
    if (GameConstants.isTablet(scroll.getItemId()) && toScroll.getDurability() < 0) { // not
      // a
      // durability
      // item
      c.getSession().write(MaplePacketCreator.getInventoryFull());
      return false;
    } else if (!GameConstants.isTablet(scroll.getItemId()) && toScroll.getDurability() >= 0) {
      c.getSession().write(MaplePacketCreator.getInventoryFull());
      return false;
    }

    IItem wscroll = null;

    // Anti cheat and validation
    List<Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
    if (scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
      c.getSession().write(MaplePacketCreator.getInventoryFull());
      return false;
    }

    if (whiteScroll) {
      wscroll = chr.getInventory(MapleInventoryType.USE).findById(2340000);
      if (wscroll == null) {
        whiteScroll = false;
      }
    }
    if (scroll.getItemId() == 2049115 && toScroll.getItemId() != 1003068) {
      // ravana
      return false;
    }
    if (GameConstants.isTablet(scroll.getItemId())) {
      switch (scroll.getItemId() % 1000 / 100) {
        case 0: // 1h
          if (GameConstants.isTwoHanded(toScroll.getItemId()) || !GameConstants.isWeapon(toScroll.getItemId())) {
            return false;
          }
          break;
        case 1: // 2h
          if (!GameConstants.isTwoHanded(toScroll.getItemId()) || !GameConstants.isWeapon(toScroll.getItemId())) {
            return false;
          }
          break;
        case 2: // armor
          if (GameConstants.isAccessory(toScroll.getItemId()) || GameConstants.isWeapon(toScroll.getItemId())) {
            return false;
          }
          break;
        case 3: // accessory
          if (!GameConstants.isAccessory(toScroll.getItemId()) || GameConstants.isWeapon(toScroll.getItemId())) {
            return false;
          }
          break;
      }
    } else if (!GameConstants.isAccessoryScroll(scroll.getItemId())
        && !GameConstants.isChaosScroll(scroll.getItemId()) && !GameConstants.isCleanSlate(scroll.getItemId())
        && !GameConstants.isEquipScroll(scroll.getItemId())
        && !GameConstants.isPotentialScroll(scroll.getItemId())) {

      if (!(ii.canScroll(scroll.getItemId(),
          toScroll.getItemId())) &&
          !(ii.isArmorScroll(scroll.getItemId(),
              toScroll.getItemId()))) {
        c.enableActions();
        return false;
      }

    }
    if (GameConstants.isAccessoryScroll(scroll.getItemId()) && !GameConstants.isAccessory(toScroll.getItemId())) {
      return false;
    }
    if (scroll.getQuantity() <= 0) {
      return false;
    }

    if (legendarySpirit && vegas == 0) {
      if (chr.getSkillLevel(SkillFactory.getSkill(1003)) <= 0
          && chr.getSkillLevel(SkillFactory.getSkill(10001003)) <= 0
          && chr.getSkillLevel(SkillFactory.getSkill(20001003)) <= 0
          && chr.getSkillLevel(SkillFactory.getSkill(20011003)) <= 0
          && chr.getSkillLevel(SkillFactory.getSkill(30001003)) <= 0) {
        AutobanManager.getInstance().addPoints(c, 50, 120000,
            "Using the Skill 'Legendary Spirit' without having it.");
        return false;
      }
    }

    // Scroll Success/ Failure/ Curse
    final IEquip scrolled = (IEquip) ii.scrollEquipWithId(toScroll, scroll, whiteScroll, chr, vegas);
    ScrollResult scrollSuccess;
    if (scrolled == null) {
      scrollSuccess = IEquip.ScrollResult.CURSE;
    } else if (scrolled.getLevel() > oldLevel || scrolled.getEnhance() > oldEnhance
        || scrolled.getState() > oldState || scrolled.getFlag() > oldFlag) {
      scrollSuccess = IEquip.ScrollResult.SUCCESS;
    } else if ((GameConstants.isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() > oldSlots)) {
      scrollSuccess = IEquip.ScrollResult.SUCCESS;
    } else {
      scrollSuccess = IEquip.ScrollResult.FAIL;
    }

    // Update
    if (vegas == 0) {
      chr.getInventory(MapleInventoryType.USE).removeItem(scroll.getPosition(), (short) 1, false);
    }
    if (whiteScroll) {
      MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, wscroll.getPosition(), (short) 1, false,
          false);
    }
    if (vegas > 0) {
      MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, scroll.getPosition(), (short) 1, false,
          false);
    }
    if (scrollSuccess == IEquip.ScrollResult.CURSE) {
      c.getSession().write(MaplePacketCreator.scrolledItem(scroll, toScroll, true, false));
      if (dst < 0) {
        chr.getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
      } else {
        chr.getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
      }
    } else if (vegas == 0) {
      c.getSession().write(MaplePacketCreator.scrolledItem(scroll, scrolled, false, false));
    }

    if (type == 0) {
      chr.getMap().broadcastMessage(chr,
          MaplePacketCreator.getNormalScrollEffect(chr.getId(), scrollSuccess, legendarySpirit, whiteScroll),
          vegas == 0);
    } else if (type == 1) {
      chr.getMap().broadcastMessage(chr,
          MaplePacketCreator.getPotentialScrollEffect(true, chr.getId(), scrollSuccess, legendarySpirit),
          vegas == 0);
    } else if (type == 2) {
      chr.getMap().broadcastMessage(chr,
          MaplePacketCreator.getPotentialScrollEffect(false, chr.getId(), scrollSuccess, legendarySpirit),
          vegas == 0);
    }
    // equipped item was scrolled and changed
    if (dst < 0 && (scrollSuccess == IEquip.ScrollResult.SUCCESS || scrollSuccess == IEquip.ScrollResult.CURSE)
        && vegas == 0) {
      chr.equipChanged();
    }
    return true;
  }

  public static final boolean UseSkillBook(final byte slot, final int itemId, final MapleClient c,
                                           final MapleCharacter chr) {
    // we don't need sbs but..not sure.
    final IItem toUse = chr.getInventory(GameConstants.getInventoryType(itemId)).getItem(slot);

    if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
      return false;
    }
    final Map<String, Integer> skilldata = MapleItemInformationProvider.getInstance()
        .getSkillStats(toUse.getItemId());
    if (skilldata == null) { // Hacking or used an unknown item
      return false;
    }
    boolean canuse = false, success = false;
    int skill = 0, maxlevel = 0;

    final int SuccessRate = skilldata.get("success");
    final int ReqSkillLevel = skilldata.get("reqSkillLevel");
    final int MasterLevel = skilldata.get("masterLevel");

    byte i = 0;
    Integer CurrentLoopedSkillId;
    while (true) {
      CurrentLoopedSkillId = skilldata.get("skillid" + i);
      i++;
      if (CurrentLoopedSkillId == null) {
        break; // End of data
      }
      final ISkill CurrSkillData = SkillFactory.getSkill(CurrentLoopedSkillId);
      if (CurrSkillData != null && CurrSkillData.canBeLearnedBy(chr.getJob())
          && chr.getSkillLevel(CurrSkillData) >= ReqSkillLevel
          && chr.getMasterLevel(CurrSkillData) < MasterLevel) {
        canuse = true;
        if (Randomizer.nextInt(100) <= SuccessRate && SuccessRate != 0) {
          success = true;
          byte level = chr.getSkillLevel(CurrSkillData);
          if (JobUtils.isDualbladeCashMasteryItem(toUse)) {
            level += 1;
          }
          chr.changeSkillLevel(CurrSkillData, level, (byte) MasterLevel);
        } else {
          success = false;
        }
        MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(itemId), slot, (short) 1,
            false);
        break;
      }
    }
    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.useSkillBook(chr, skill, maxlevel, canuse, success));
    c.getSession().write(MaplePacketCreator.enableActions());
    c.getPlayer().sendSkills();
    return canuse;
  }


  public static final boolean useItem(final MapleClient c, final int id) {
    if (GameConstants.isUse(id)) { // TO prevent caching of everything,
      // waste of mem
      final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      final byte consumeval = ii.isConsumeOnPickup(id);

      if (consumeval > 0) {
        if (consumeval == 2) {
          if (c.getPlayer().getParty() != null) {
            for (final MaplePartyCharacter pc : c.getPlayer().getParty().getMembers()) {
              final MapleCharacter chr = c.getPlayer().getMap().getCharacterById(pc.getId());
              if (chr != null) {
                ii.getItemEffect(id).applyTo(chr);
              }
            }
          } else {
            ii.getItemEffect(id).applyTo(c.getPlayer());
          }
        } else {
          ii.getItemEffect(id).applyTo(c.getPlayer());
        }
        c.getSession().write(MaplePacketCreator.getShowItemGain(id, (byte) 1));
        return true;
      }
    }
    return false;
  }

  public static final void removeItem_Pet(final MapleCharacter chr, final MapleMapItem mapitem, int pet) {
    mapitem.setPickedUp(true);
    chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), pet),
        mapitem.getPosition());
    chr.getMap().removeMapObject(mapitem);
    if (mapitem.isRandDrop()) {
      chr.getMap().spawnRandDrop();
    }
  }

  public static final void removeItem(final MapleCharacter chr, final MapleMapItem mapitem, final MapleMapObject ob) {
    mapitem.setPickedUp(true);
    chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()),
        mapitem.getPosition());
    chr.getMap().removeMapObject(ob);
    if (mapitem.isRandDrop()) {
      chr.getMap().spawnRandDrop();
    }
  }

  public static final void addMedalString(final MapleCharacter c, final StringBuilder sb) {
    final IItem medal = c.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -46);
    if (medal != null) { // Medal
      sb.append("<");
      sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
      sb.append("> ");
    }
  }

  public static final boolean getIncubatedItems(MapleClient c) {
    if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < 2
        || c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 2
        || c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < 2) {
      c.getPlayer().dropMessage(5, "Please make room in your inventory.");
      return false;
    }
    final int[] ids = {2430091, 2430092, 2430093, 2430101, 2430102, // mounts
        2340000, // rares
        1152000, 1152001, 1152004, 1152005, 1152006, 1152007, 1152008, // toenail
        // only
        // comes
        // when
        // db
        // is
        // out.
        1000040, 1102246, 1082276, 1050169, 1051210, 1072447, 1442106, // blizzard
        3010019, // chairs
        1001060, 1002391, 1102004, 1050039, 1102040, 1102041, 1102042, 1102043, // equips
        1082145, 1082146, 1082147, 1082148, 1082149, 1082150, // wg
        2043704, 2040904, 2040409, 2040307, 2041030, 2040015, 2040109, 2041035, 2041036, 2040009, 2040511,
        2040408, 2043804, 2044105, 2044903, 2044804, 2043009, 2043305, 2040610, 2040716, 2041037, 2043005,
        2041032, 2040305, // scrolls
        2040211, 2040212, 1022097, // dragon glasses
        2049000, 2049001, 2049002, 2049003, // clean slate
        1012058, 1012059, 1012060, 1012061, // pinocchio nose msea only.
        1332100, 1382058, 1402073, 1432066, 1442090, 1452058, 1462076, 1472069, 1482051, 1492024, 1342009, // durability
        // weapons
        // level
        // 105
        2049400, 2049401, 2049301};
    // out of 1000
    final int[] chances = {100, 100, 100, 100, 100, 1, 10, 10, 10, 10, 10, 10, 10, 5, 5, 5, 5, 5, 5, 5, 2, 10, 10,
        10, 10, 10, 10, 10, 10, 5, 5, 5, 5, 5, 5, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
        10, 10, 10, 10, 10, 10, 10, 10, 10, 5, 5, 10, 10, 10, 10, 10, 5, 5, 5, 5, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 1, 2, 1, 2};
    int z = Randomizer.nextInt(ids.length);
    while (chances[z] < Randomizer.nextInt(1000)) {
      z = Randomizer.nextInt(ids.length);
    }
    int z_2 = Randomizer.nextInt(ids.length);
    while (z_2 == z || chances[z_2] < Randomizer.nextInt(1000)) {
      z_2 = Randomizer.nextInt(ids.length);
    }
    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    c.getSession().write(MaplePacketCreator.getPeanutResult(ids[z], (short) 1, ids[z_2], (short) 1));
    MapleInventoryManipulator.addById(c, ids[z], (short) 1,
        ii.getName(ids[z]) + " on " + FileoutputUtil.CurrentReadable_Date());
    MapleInventoryManipulator.addById(c, ids[z_2], (short) 1,
        ii.getName(ids[z_2]) + " on " + FileoutputUtil.CurrentReadable_Date());
    return true;
  }

  public static void UseRewardItem(byte slot, int itemId, MapleClient c, MapleCharacter player) {
    MapleCharacter chr = c.getPlayer();
    final IItem toUse = c.getPlayer().getInventory(GameConstants.getInventoryType(itemId)).getItem(slot);
    c.getSession().write(MaplePacketCreator.enableActions());
    if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
      if (chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1
          && chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1
          && chr.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1
          && chr.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Pair<Integer, List<StructRewardItem>> rewards = ii.getRewardItem(itemId);
        // hot time event, 2022336 < custom (Secret Box)
        if (rewards != null && rewards.getLeft() > 0) {
          boolean rewarded = false;
          while (!rewarded) {
            for (StructRewardItem reward : rewards.getRight()) {
              if (reward.prob > 0 && Randomizer.nextInt(rewards.getLeft()) < reward.prob) { // Total
                // prob
                if (GameConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP) {
                  final IItem item = ii.getEquipById(reward.itemid);
                  if (reward.period > 0) {
                    item.setExpiration(System.currentTimeMillis() + (reward.period * 60 * 60 * 10));
                  }
                  MapleInventoryManipulator.addbyItem(c, item);
                } else {
                  MapleInventoryManipulator.addById(c, reward.itemid, reward.quantity,
                      "Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                }
                MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemId), itemId,
                    1, false, false);

                c.getSession().write(
                    MaplePacketCreator.showRewardItemAnimation(reward.itemid, reward.effect));
                chr.getMap().broadcastMessage(chr, MaplePacketCreator
                    .showRewardItemAnimation(reward.itemid, reward.effect, chr.getId()), false);
                c.getSession()
                    .write(MaplePacketCreator.getShowItemGain(reward.itemid, (short) 1, true));
                rewarded = true;
              }
            }
          }
        } else {
          chr.dropMessage(6, "Unknown error.");
        }
      } else {
        chr.dropMessage(6, "Insufficient inventory slot.");
      }
    }

  }


}
