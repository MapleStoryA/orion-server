package tools.packet;

import client.MapleCharacter;
import client.MapleStat;
import client.inventory.IItem;
import client.inventory.MaplePet;
import handling.SendPacketOpcode;
import java.awt.*;
import server.movement.MovePath;
import tools.MaplePacketCreator;
import tools.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class PetPacket {

    public static final byte[] ITEM_MAGIC = new byte[] {(byte) 0x80, 5};

    public static final byte[] updatePet(final MaplePet pet, final IItem item) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(0);
        packet.write(2);
        packet.write(3);
        packet.write(5);
        packet.writeShort(pet.getInventoryPosition());
        packet.write(0);
        packet.write(5);
        packet.writeShort(pet.getInventoryPosition());
        packet.write(3);
        packet.writeInt(pet.getPetItemId());
        packet.write(1);
        packet.writeLong(pet.getUniqueId());
        PacketHelper.addPetItemInfo(packet, item, pet);
        return packet.getPacket();
    }

    public static final byte[] removePet(final MapleCharacter chr, final int slot) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
        packet.writeInt(chr.getId());
        packet.write(slot);
        packet.writeShort(0);

        return packet.getPacket();
    }

    public static void addPetInfo(final OutPacket packet, MaplePet pet, boolean showpet) {
        packet.write(1);
        if (showpet) {
            packet.write(0);
        }

        packet.writeInt(pet.getPetItemId());
        packet.writeMapleAsciiString(pet.getName());
        packet.writeLong(pet.getUniqueId());
        packet.writeShort(pet.getPos().x);
        packet.writeShort(pet.getPos().y - 20);
        packet.write(pet.getStance());
        packet.writeInt(pet.getFh());
    }

    public static final byte[] showPet(
            final MapleCharacter chr,
            final MaplePet pet,
            final boolean remove,
            final boolean hunger) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
        packet.writeInt(chr.getId());
        packet.write(chr.getPetIndex(pet));
        Point position = pet.getPos() == null ? chr.getPosition() : pet.getPos();
        if (remove) {
            packet.write(0);
            packet.write(hunger ? 1 : 0);
        } else {
            packet.write(1);
            packet.write(1); // 1?
            packet.writeInt(pet.getPetItemId());
            packet.writeMapleAsciiString(pet.getName());
            packet.writeLong(pet.getUniqueId());
            packet.writeShort(position.x);
            packet.writeShort(position.y - 20);
            packet.write(pet.getStance());
            packet.writeInt(pet.getFh());
        }
        return packet.getPacket();
    }

    public static final byte[] removePet(final int cid, final int index) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
        packet.writeInt(cid);
        packet.write(index);
        packet.writeShort(0);
        return packet.getPacket();
    }

    public static byte[] movePet(int cid, byte slot, MovePath moves) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.MOVE_PET.getValue());
        packet.writeInt(cid);
        packet.write(slot);
        moves.encode(packet);
        return packet.getPacket();
    }

    public static final byte[] petChat(
            final int cid, final int un, final String text, final byte slot) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PET_CHAT.getValue());
        packet.writeInt(cid);
        packet.write(slot);
        packet.writeShort(un);
        packet.writeMapleAsciiString(text);
        packet.write(0); // hasQuoteRing

        return packet.getPacket();
    }

    public static final byte[] commandResponse(
            final int cid,
            final byte command,
            final byte slot,
            final boolean success,
            final boolean food) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PET_COMMAND.getValue());
        packet.writeInt(cid);
        packet.write(slot);
        packet.write(command == 1 ? 1 : 0);
        packet.write(command);
        if (command == 1) {
            packet.write(0);

        } else {
            packet.writeShort(success ? 1 : 0);
        }
        return packet.getPacket();
    }

    public static final byte[] showOwnPetLevelUp(final byte index) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(4);
        packet.write(0);
        packet.write(index); // Pet Index

        return packet.getPacket();
    }

    public static final byte[] showPetLevelUp(final MapleCharacter chr, final byte index) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(chr.getId());
        packet.write(4);
        packet.write(0);
        packet.write(index);

        return packet.getPacket();
    }

    public static final byte[] emptyStatUpdate() {
        return MaplePacketCreator.enableActions();
    }

    public static final byte[] petStatUpdate_Empty() {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(0);
        packet.writeInt(MapleStat.PET.getValue());
        packet.writeZeroBytes(25);
        return packet.getPacket();
    }

    public static final byte[] petStatUpdate(final MapleCharacter chr) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(0);
        packet.writeInt(MapleStat.PET.getValue());

        byte count = 0;
        for (final MaplePet pet : chr.getPets()) {
            if (pet.getSummoned()) {
                packet.writeLong(pet.getUniqueId());
                count++;
            }
        }
        while (count < 3) {
            packet.writeZeroBytes(8);
            count++;
        }
        packet.write(0);

        return packet.getPacket();
    }

    public static final byte[] loadExceptionList(
            final int cid, final int petId, final String data) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PET_EXCEPTION_LIST.getValue());
        packet.writeInt(cid);
        packet.write(0); // lets make it 0, 0 = only boss pet
        packet.writeLong(petId);
        final String[] ii = data.split(",");
        if (data.isEmpty()) {
            packet.write(0);
            return packet.getPacket();
        }
        packet.write(ii.length > 10 ? 10 : ii.length);
        int i = 0;
        for (final String ids : ii) {
            i++;
            if (i > 10) {
                break;
            }
            packet.writeInt(Integer.parseInt(ids));
        }

        return packet.getPacket();
    }

    public static byte[] petAutoHP(int itemId) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PET_AUTO_HP.getValue());
        packet.writeInt(itemId);

        return packet.getPacket();
    }

    public static byte[] petAutoMP(int itemId) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PET_AUTO_MP.getValue());
        packet.writeInt(itemId);

        return packet.getPacket();
    }
}
