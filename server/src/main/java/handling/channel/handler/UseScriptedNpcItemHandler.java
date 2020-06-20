package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.inventory.IItem;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import scripting.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.maps.MapleMap;
import server.maps.SavedLocationType;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseScriptedNpcItemHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    c.getPlayer().updateTick(slea.readInt());
    final byte slot = (byte) slea.readShort();
    final int itemId = slea.readInt();
    final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
    long expiration_days = 0;
    int mountid = 0;

    if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
      switch (toUse.getItemId()) {
        case 2430007: { // Blank Compass
          final MapleInventory inventory = chr.getInventory(MapleInventoryType.SETUP);
          MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);

          if (inventory.countById(3994102) >= 20 // Compass Letter "North"
              && inventory.countById(3994103) >= 20 // Compass Letter
              // "South"
              && inventory.countById(3994104) >= 20 // Compass Letter
              // "East"
              && inventory.countById(3994105) >= 20) { // Compass
            // Letter
            // "West"
            MapleInventoryManipulator.addById(c, 2430008, (short) 1,
                "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date()); // Gold
            // Compass
            MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994102, 20, false, false);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994103, 20, false, false);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994104, 20, false, false);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994105, 20, false, false);
          } else {
            MapleInventoryManipulator.addById(c, 2430007, (short) 1,
                "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date()); // Blank
            // Compass
          }
          NPCScriptManager.getInstance().start(c, 2084001);
          break;
        }
        case 2430008: { // Gold Compass
          chr.saveLocation(SavedLocationType.RICHIE);
          MapleMap map;
          boolean warped = false;

          for (int i = 390001000; i <= 390001004; i++) {
            map = c.getChannelServer().getMapFactory().getMap(i);

            if (map.getCharactersSize() == 0) {
              chr.changeMap(map, map.getPortal(0));
              warped = true;
              break;
            }
          }
          if (warped) { // Removal of gold compass
            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, 2430008, 1, false, false);
          } else { // Or mabe some other message.
            c.getPlayer().dropMessage(5, "All maps are currently in use, please try again later.");
          }
          break;
        }
        case 2430112: // miracle cube
          if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
            if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 25) {
              if (MapleInventoryManipulator.checkSpace(c, 2049400, 1, "") && MapleInventoryManipulator
                  .removeById(c, MapleInventoryType.USE, 2430112, 25, true, false)) {
                MapleInventoryManipulator.addById(c, 2049400, (short) 1,
                    "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
              } else {
                c.getPlayer().dropMessage(5, "Please make some space.");
              }
            } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 10) {
              if (MapleInventoryManipulator.checkSpace(c, 2049400, 1, "") && MapleInventoryManipulator
                  .removeById(c, MapleInventoryType.USE, 2430112, 10, true, false)) {
                MapleInventoryManipulator.addById(c, 2049401, (short) 1,
                    "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
              } else {
                c.getPlayer().dropMessage(5, "Please make some space.");
              }
            } else {
              c.getPlayer().dropMessage(5,
                  "There needs to be 10 Fragments for a Potential Scroll, 25 for Advanced Potential Scroll.");
            }
          } else {
            c.getPlayer().dropMessage(5, "Please make some space.");
          }
          break;
        case 2430036: // croco 1 day
          mountid = 1027;
          expiration_days = 1;
          break;
        case 2430037: // black scooter 1 day
          mountid = 1028;
          expiration_days = 1;
          break;
        case 2430038: // pink scooter 1 day
          mountid = 1029;
          expiration_days = 1;
          break;
        case 2430039: // clouds 1 day
          mountid = 1030;
          expiration_days = 1;
          break;
        case 2430040: // balrog 1 day
          mountid = 1031;
          expiration_days = 1;
          break;
        case 2430053: // croco 30 day
          mountid = 1027;
          expiration_days = 1;
          break;
        case 2430054: // black scooter 30 day
          mountid = 1028;
          expiration_days = 30;
          break;
        case 2430055: // pink scooter 30 day
          mountid = 1029;
          expiration_days = 30;
          break;
        case 2430056: // mist rog 30 day
          mountid = 1035;
          expiration_days = 30;
          break;
        // race kart 30 day? unknown 2430057
        case 2430072: // ZD tiger 7 day
          mountid = 1034;
          expiration_days = 7;
          break;
        case 2430073: // lion 15 day
          mountid = 1036;
          expiration_days = 15;
          break;
        case 2430074: // unicorn 15 day
          mountid = 1037;
          expiration_days = 15;
          break;
        case 2430075: // low rider 15 day
          mountid = 1038;
          expiration_days = 15;
          break;
        case 2430076: // red truck 15 day
          mountid = 1039;
          expiration_days = 15;
          break;
        case 2430077: // gargoyle 15 day
          mountid = 1040;
          expiration_days = 15;
          break;
        case 2430080: // shinjo 20 day
          mountid = 1042;
          expiration_days = 20;
          break;
        case 2430082: // orange mush 7 day
          mountid = 1044;
          expiration_days = 7;
          break;
        case 2430091: // nightmare 10 day
          mountid = 1049;
          expiration_days = 10;
          break;
        case 2430092: // yeti 10 day
          mountid = 1050;
          expiration_days = 10;
          break;
        case 2430093: // ostrich 10 day
          mountid = 1051;
          expiration_days = 10;
          break;
        case 2430101: // pink bear 10 day
          mountid = 1052;
          expiration_days = 10;
          break;
        case 2430102: // transformation robo 10 day
          mountid = 1053;
          expiration_days = 10;
          break;
        case 2430103: // chicken 30 day
          mountid = 1054;
          expiration_days = 30;
          break;
        case 2430117: // lion 1 year
          mountid = 1036;
          expiration_days = 365;
          break;
        case 2430118: // red truck 1 year
          mountid = 1039;
          expiration_days = 365;
          break;
        case 2430119: // gargoyle 1 year
          mountid = 1040;
          expiration_days = 365;
          break;
        case 2430120: // unicorn 1 year
          mountid = 1037;
          expiration_days = 365;
          break;
        case 2430136: // owl 30 day
          mountid = 1069;
          expiration_days = 30;
          break;
        case 2430137: // owl 1 year
          mountid = 1069;
          expiration_days = 365;
          break;
        case 2430201: // giant bunny 60 day
          mountid = 1096;
          expiration_days = 60;
          break;
        case 2430228: // tiny bunny 60 day
          mountid = 1101;
          expiration_days = 60;
          break;
        case 2430229: // bunny rickshaw 60 day
          mountid = 1102;
          expiration_days = 60;
          break;
        case 2430050: // barlog coupon
          mountid = 1031;
          expiration_days = 360;
          break;
        case 2430014:
          c.getPlayer().dropMessage(5, "The barrier was removed and the forces released.");
          if (c.getPlayer().getMapId() == 106020300) {
            c.getPlayer().set("KILLER_SPORE", "1");
            c.getPlayer().removeItem(2430014, -1);
          }
          break;
      }
    }
    if (mountid > 0) {
      mountid += (GameConstants.isAran(c.getPlayer().getJob()) ? 20000000
          : (GameConstants.isEvan(c.getPlayer().getJob()) ? 20010000
          : (GameConstants.isKOC(c.getPlayer().getJob()) ? 10000000
          : (GameConstants.isResist(c.getPlayer().getJob()) ? 30000000 : 0))));
      if (c.getPlayer().getSkillLevel(mountid) > 0) {
        c.getPlayer().dropMessage(5, "You already have this skill.");
      } else if (expiration_days > 0) {
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
        c.getPlayer().changeSkillLevel(SkillFactory.getSkill(mountid), (byte) 1, (byte) 1,
            System.currentTimeMillis() + (long) (expiration_days * 24 * 60 * 60 * 1000));
        c.getPlayer().dropMessage(5, "The skill has been attained.");
      }
    }
    c.getSession().write(MaplePacketCreator.enableActions());

  }

}
