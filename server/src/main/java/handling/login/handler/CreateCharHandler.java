package handling.login.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.inventory.*;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginInformationProvider;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class CreateCharHandler extends AbstractMaplePacketHandler {

  private static int[] IDs = {1302000, 1312004, 1322005, 1442079, 1302132, // weapons
      1040002, 1040006, 1040010, 1041002, 1041006, 1041010, 1041011, 1042167, 1060138, 1061160, // bottom
      1060002, 1060006, 1061002, 1061008, 1062115, 1042180, // top
      1072001, 1072005, 1072037, 1072038, 1072383, 1072418, // shoes
      30000, 30010, 30020, 30030, 31000, 31040, 31050, // hair
      20000, 20001, 20002, 21000, 21001, 21002, 21201, 20401, 20402, 21700, 20100 // face
  };

  private static boolean isLegal(int toCompare) {
    for (int i = 0; i < IDs.length; i++) {
      if (IDs[i] == toCompare) {
        return true;
      }
    }
    return false;
  }


  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final String name = slea.readMapleAsciiString();
    final int JobType = slea.readInt(); // 1 = Adventurer, 0 = Cygnus, 2 =
    // Aran, 3 = evan
    final short db = slea.readShort(); // whether dual blade = 1 or
    // adventurer = 0
    final int face = slea.readInt();
    final int hair = slea.readInt();
    final int hairColor = slea.readInt();
    final byte skinColor = (byte) slea.readInt();
    final int top = slea.readInt();
    final int bottom = slea.readInt();
    final int shoes = slea.readInt();
    final int weapon = slea.readInt();
    final byte gender = c.getGender();

    if (JobType < 0 || JobType > 3 || (db == 1 && JobType != 1)) {
      return;
    }

    int[] items = new int[] {weapon, top, bottom, shoes, hair, face};
    for (int i = 0; i < items.length; i++) {
      if (!isLegal(items[i])) {
        return;
      }
    }

    MapleCharacter newchar = MapleCharacter.getDefault(c, JobType);
    newchar.setWorld((byte) c.getWorld());
    newchar.setFace(face);
    newchar.setHair(hair + hairColor);
    newchar.setGender(slea.readByte());
    newchar.setName(name);
    newchar.setSkinColor(skinColor);

    final MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
    final MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();

    final int[] equips = {top, bottom, shoes, weapon, 1003028}; // hat,glove,top,shoes,
    final byte[] pos = {-5, -6, -7, -11, -1};// -6 bottom
    Equip item;
    for (int i = 0; i <= 4; i++) {
      if (JobType != 3 && i == 4) {
        continue;
      }
      item = (Equip) li.getEquipById(equips[i]); // Visitor Helmet
      item.setStr((short) 0);
      item.setDex((short) 0);
      item.setInt((short) 0);
      item.setLuk((short) 0);
      item.setWatk((short) 0);
      item.setMatk((short) 0);
      item.setUpgradeSlots((byte) 7);
      item.setPosition(pos[i]);
      equip.addFromDB(item);
    }
    IItem wea = li.getEquipById(weapon);
    wea.setPosition((byte) -11);
    equip.addFromDB(wea);

    newchar.setQuestAdd(MapleQuest.getInstance(1200), (byte) 2, null);
    newchar.setQuestAdd(MapleQuest.getInstance(1201), (byte) 2, null);
    newchar.setQuestAdd(MapleQuest.getInstance(1202), (byte) 2, null);
    newchar.setQuestAdd(MapleQuest.getInstance(1203), (byte) 2, null);
    newchar.setQuestAdd(MapleQuest.getInstance(1204), (byte) 2, null);
    newchar.setQuestAdd(MapleQuest.getInstance(1205), (byte) 2, null);
    newchar.setQuestAdd(MapleQuest.getInstance(1206), (byte) 2, null);
    newchar.setQuestAdd(MapleQuest.getInstance(1300), (byte) 2, null);
    newchar.setQuestAdd(MapleQuest.getInstance(1301), (byte) 2, null);
    newchar.setQuestAdd(MapleQuest.getInstance(1302), (byte) 2, null);

    switch (JobType) {
      case 0: // Cygnus
        newchar.setQuestAdd(MapleQuest.getInstance(20022), (byte) 1, "1");
        newchar.setQuestAdd(MapleQuest.getInstance(20010), (byte) 1, null);
        newchar.setQuestAdd(MapleQuest.getInstance(20000), (byte) 1, null);
        newchar.setQuestAdd(MapleQuest.getInstance(20015), (byte) 1, null);
        newchar.setQuestAdd(MapleQuest.getInstance(20020), (byte) 1, null);
        final Item it = new Item(4161047, (byte) 0, (short) 1, (byte) 0);
        newchar.getInventory(MapleInventoryType.ETC).addItem(it);
        break;
      case 1: // Adventurer
        final Item it2 = new Item(4161001, (byte) 0, (short) 1, (byte) 0);
        newchar.getInventory(MapleInventoryType.ETC).addItem(it2);
        break;
      case 2: // Aran
        final Item it3 = new Item(4161048, (byte) 0, (short) 1, (byte) 0);
        newchar.getInventory(MapleInventoryType.ETC).addItem(it3);
        break;
      case 3: // Evan
        final Item it4 = new Item(4161052, (byte) 0, (short) 1, (byte) 0);
        newchar.getInventory(MapleInventoryType.ETC).addItem(it4);
        break;
    }

    if (MapleCharacterUtil.canCreateChar(name) && !LoginInformationProvider.getInstance().isForbiddenName(name)) {
      MapleCharacter.saveNewCharToDB(newchar, JobType, JobType == 1 && db > 0);
      c.getSession().write(LoginPacket.addNewCharEntry(newchar, true));
      c.createdChar(newchar.getId());
    } else {
      c.getSession().write(LoginPacket.addNewCharEntry(newchar, false));
    }

  }

}
