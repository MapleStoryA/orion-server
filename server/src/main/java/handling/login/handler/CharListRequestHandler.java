package handling.login.handler;

import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import database.CharacterData;
import database.CharacterListResult;
import database.LoginService;
import handling.AbstractMaplePacketHandler;
import handling.SendPacketOpcode;
import lombok.extern.slf4j.Slf4j;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CharListRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        final int server = slea.readByte();
        final int channel = slea.readByte() + 1;

        c.setWorld(server);
        c.setChannel(channel);

        // I inlined the char list here because loadCharacterList loads only the necessary
        CharacterListResult list = LoginService.loadCharacterList(c.getAccountData().getId(), 0);
        if (list.getCharacters() != null) {
            c.getSession().write(getCharList(list.getCharacters(), c.getCharacterSlots()));
        } else {
            c.getSession().close();
        }

    }

    private static byte[] getCharList(final List<CharacterData> chars, int charslots) {
        final var mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
        mplew.write(0);
        mplew.write(chars.size()); // 1

        for (final CharacterData chr : chars) {
            boolean isGM = chr.getJob() == 900 || chr.getJob() == 910;
            addCharEntry(mplew, chr, !isGM && chr.getLevel() >= 10);
        }
        mplew.write(2); // second pw request
        mplew.writeLong(charslots);

        return mplew.getPacket();
    }

    private static void addCharEntry(final MaplePacketLittleEndianWriter mplew, final CharacterData chr, boolean ranking) {
        addCharStats(mplew, chr);
        addCharLook(mplew, chr, true);
        mplew.write(0);
        mplew.write(ranking ? 1 : 0);
        if (ranking) {
            mplew.writeInt(chr.getRank());
            mplew.writeInt(chr.getRankMove());
            mplew.writeInt(chr.getJobRank());
            mplew.writeInt(chr.getJobRankMove());
        }
    }

    private static void addCharStats(final MaplePacketLittleEndianWriter mplew, final CharacterData chr) {
        mplew.writeInt(chr.getId()); // character id
        mplew.writeAsciiString(chr.getName(), 13);
        mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
        mplew.write(chr.getSkinColor()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.writeInt(chr.getHair()); // hair
        for (int i = 0; i < 3; i++) {
            mplew.writeLong(0);
        }
        mplew.write(chr.getLevel()); // level
        mplew.writeShort(chr.getJob()); // job
        chr.connectData(mplew);
        mplew.writeShort(Math.min(199, chr.getAp())); // Avoid Popup
        if (chr.getJob() == 2001 || chr.isEvan()) {
            mplew.write(0);
        } else {
            mplew.writeShort(chr.getSp()); // remaining sp
        }
        mplew.writeInt(chr.getExp()); // exp
        mplew.writeShort(chr.getFame()); // fame
        mplew.writeInt(0); // Gachapon exp
        mplew.writeInt(chr.getMap()); // current map id
        mplew.write(chr.getSpawnPoint()); // spawnpoint
        mplew.writeInt(0);
        mplew.writeShort(chr.getSubCategory()); //1 here = db
    }

    private static void addCharLook(final MaplePacketLittleEndianWriter mplew, final CharacterData chr, final boolean mega) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair());

        final Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        final Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);

        for (final IItem item : equip.list()) {
            if (item.isNotVisible()) { //not visible
                continue;
            }
            byte pos = (byte) (item.getPosition() * -1);

            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getItemId());
            } else if ((pos > 100 || pos == -128) && pos != 111) {
                pos = (byte) (pos == -128 ? 28 : pos - 100);
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }
        for (final Map.Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }

        mplew.write(0xFF);

        // end of visible itens
        // masked itens
        for (final Map.Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());

        }
        mplew.write(0xFF); // ending markers


        final IItem cWeapon = equip.getItem((byte) -111);
        mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(0);
        }
    }

}
