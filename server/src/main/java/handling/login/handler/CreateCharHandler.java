package handling.login.handler;

import client.MapleCharacter;
import client.MapleCharacterHelper;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import handling.login.LoginInformationProvider;
import handling.packet.AbstractMaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.data.input.InPacket;
import tools.packet.LoginPacket;

@Slf4j
public class CreateCharHandler extends AbstractMaplePacketHandler {

    private static final int[] LEGAL_CHAR_CREATION_IDS = {
        1302000,
        1312004,
        1322005,
        1442079,
        1302132, // weapons
        1040002,
        1040006,
        1040010,
        1041002,
        1041006,
        1041010,
        1041011,
        1042167,
        1060138,
        1061160, // bottom
        1060002,
        1060006,
        1061002,
        1061008,
        1062115,
        1042180, // top
        1072001,
        1072005,
        1072037,
        1072038,
        1072383,
        1072418, // shoes
        30000,
        30010,
        30020,
        30030,
        31000,
        31040,
        31050, // hair
        20000,
        20001,
        20002,
        21000,
        21001,
        21002,
        21201,
        20401,
        20402,
        21700,
        20100 // face
    };
    public static final int Adventurer = 1;
    public static final int Aran = 2;
    public static final int Evan = 3;
    public static final int Cygnus = 0;

    private static boolean isLegal(int toCompare) {
        for (int i = 0; i < LEGAL_CHAR_CREATION_IDS.length; i++) {
            if (LEGAL_CHAR_CREATION_IDS[i] == toCompare) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final String name = packet.readMapleAsciiString();
        final int jobType = packet.readInt();
        final short dualBladeTypeValue = packet.readShort();
        final int face = packet.readInt();
        final int hair = packet.readInt();
        final int hairColor = packet.readInt();
        final byte skinColor = (byte) packet.readInt();
        final int top = packet.readInt();
        final int bottom = packet.readInt();
        final int shoes = packet.readInt();
        final int weapon = packet.readInt();
        final byte gender = c.getAccountData().getGender();

        if (jobType < Cygnus || jobType > Evan || (dualBladeTypeValue == 1 && jobType != Adventurer)) {
            return;
        }

        int[] items = new int[] {weapon, top, bottom, shoes, hair, face};
        for (int i = 0; i < items.length; i++) {
            if (!isLegal(items[i])) {
                return;
            }
        }

        MapleCharacter player = MapleCharacter.getDefault(c, jobType);
        player.setWorld((byte) c.getWorld());
        player.setFace(face);
        player.setHair(hair + hairColor);
        player.setGender(packet.readByte());
        player.setName(name);
        player.setSkinColor(skinColor);

        final MapleInventory equip = player.getInventory(MapleInventoryType.EQUIPPED);
        final MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();

        final int[] equips = {top, bottom, shoes, weapon, 1003028}; // hat,glove,top,shoes,
        final byte[] pos = {-5, -6, -7, -11, -1}; // -6 bottom
        Equip item;
        for (int i = 0; i <= 4; i++) {
            if (jobType != 3 && i == 4) {
                continue;
            }
            item = (Equip) li.getEquipById(equips[i]);
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

        player.setQuestAdd(MapleQuest.getInstance(1200), 2, null);
        player.setQuestAdd(MapleQuest.getInstance(1201), 2, null);
        player.setQuestAdd(MapleQuest.getInstance(1202), 2, null);
        player.setQuestAdd(MapleQuest.getInstance(1203), 2, null);
        player.setQuestAdd(MapleQuest.getInstance(1204), 2, null);
        player.setQuestAdd(MapleQuest.getInstance(1205), 2, null);
        player.setQuestAdd(MapleQuest.getInstance(1206), 2, null);
        player.setQuestAdd(MapleQuest.getInstance(1300), 2, null);
        player.setQuestAdd(MapleQuest.getInstance(1301), 2, null);
        player.setQuestAdd(MapleQuest.getInstance(1302), 2, null);

        switch (jobType) {
            case Cygnus:
                player.setQuestAdd(MapleQuest.getInstance(20022), 1, "1");
                player.setQuestAdd(MapleQuest.getInstance(20010), 1, null);
                player.setQuestAdd(MapleQuest.getInstance(20000), 1, null);
                player.setQuestAdd(MapleQuest.getInstance(20015), 1, null);
                player.setQuestAdd(MapleQuest.getInstance(20020), 1, null);
                final Item itemCygnus = new Item(4161047, (byte) 0, (short) 1, (byte) 0);
                player.getInventory(MapleInventoryType.ETC).addItem(itemCygnus);
                break;
            case Adventurer:
                final Item itemAdventurer = new Item(4161001, (byte) 0, (short) 1, (byte) 0);
                player.getInventory(MapleInventoryType.ETC).addItem(itemAdventurer);
                break;
            case Aran:
                final Item itemAran = new Item(4161048, (byte) 0, (short) 1, (byte) 0);
                player.getInventory(MapleInventoryType.ETC).addItem(itemAran);
                break;
            case Evan:
                final Item itemEvan = new Item(4161052, (byte) 0, (short) 1, (byte) 0);
                player.getInventory(MapleInventoryType.ETC).addItem(itemEvan);
                break;
        }

        boolean canCreateCharacter = MapleCharacterHelper.canCreateChar(name);
        boolean isNameAllowed = !LoginInformationProvider.getInstance().isForbiddenName(name);
        boolean isDualBladeCreation = jobType == 1 && dualBladeTypeValue > 0;

        if (canCreateCharacter && isNameAllowed) {
            MapleCharacter.saveNewCharToDB(player, jobType, isDualBladeCreation);
            c.getSession().write(LoginPacket.addNewCharEntry(player, true));
        } else {
            c.getSession().write(LoginPacket.addNewCharEntry(player, false));
        }
    }
}
