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

package tools.packet;

import client.MapleCharacter;
import client.MapleStat;
import client.inventory.IItem;
import client.inventory.MaplePet;
import handling.SendPacketOpcode;
import server.movement.MovePath;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.awt.*;

public class PetPacket {

  public final static byte[] ITEM_MAGIC = new byte[] {(byte) 0x80, 5};

  public static final byte[] updatePet(final MaplePet pet, final IItem item) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
    mplew.write(0);
    mplew.write(2);
    mplew.write(3);
    mplew.write(5);
    mplew.writeShort(pet.getInventoryPosition());
    mplew.write(0);
    mplew.write(5);
    mplew.writeShort(pet.getInventoryPosition());
    mplew.write(3);
    mplew.writeInt(pet.getPetItemId());
    mplew.write(1);
    mplew.writeLong(pet.getUniqueId());
    PacketHelper.addPetItemInfo(mplew, item, pet);
    return mplew.getPacket();
  }

  public static final byte[] removePet(final MapleCharacter chr, final int slot) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
    mplew.writeInt(chr.getId());
    mplew.write(slot);
    mplew.writeShort(0);


    return mplew.getPacket();
  }

  public static void addPetInfo(final MaplePacketLittleEndianWriter mplew, MaplePet pet, boolean showpet) {
    mplew.write(1);
    if (showpet) {
      mplew.write(0);
    }

    mplew.writeInt(pet.getPetItemId());
    mplew.writeMapleAsciiString(pet.getName());
    mplew.writeLong(pet.getUniqueId());
    mplew.writeShort(pet.getPos().x);
    mplew.writeShort(pet.getPos().y - 20);
    mplew.write(pet.getStance());
    mplew.writeInt(pet.getFh());
  }

  public static final byte[] showPet(final MapleCharacter chr, final MaplePet pet, final boolean remove, final boolean hunger) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
    mplew.writeInt(chr.getId());
    mplew.write(chr.getPetIndex(pet));
    Point position = pet.getPos() == null ? chr.getPosition() : pet.getPos();
    if (remove) {
      mplew.write(0);
      mplew.write(hunger ? 1 : 0);
    } else {
      mplew.write(1);
      mplew.write(1); //1?
      mplew.writeInt(pet.getPetItemId());
      mplew.writeMapleAsciiString(pet.getName());
      mplew.writeLong(pet.getUniqueId());
      mplew.writeShort(position.x);
      mplew.writeShort(position.y - 20);
      mplew.write(pet.getStance());
      mplew.writeInt(pet.getFh());
    }
    return mplew.getPacket();
  }

  public static final byte[] removePet(final int cid, final int index) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
    mplew.writeInt(cid);
    mplew.write(index);
    mplew.writeShort(0);
    return mplew.getPacket();
  }

  public static byte[] movePet(int cid, byte slot, MovePath moves){
    final MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
    packet.writeShort(SendPacketOpcode.MOVE_PET.getValue());
    packet.writeInt(cid);
    packet.write(slot);
    moves.encode(packet);
    return packet.getPacket();
  }

  public static final byte[] petChat(final int cid, final int un, final String text, final byte slot) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PET_CHAT.getValue());
    mplew.writeInt(cid);
    mplew.write(slot);
    mplew.writeShort(un);
    mplew.writeMapleAsciiString(text);
    mplew.write(0); //hasQuoteRing

    return mplew.getPacket();
  }

  public static final byte[] commandResponse(final int cid, final byte command, final byte slot, final boolean success, final boolean food) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PET_COMMAND.getValue());
    mplew.writeInt(cid);
    mplew.write(slot);
    mplew.write(command == 1 ? 1 : 0);
    mplew.write(command);
    if (command == 1) {
      mplew.write(0);

    } else {
      mplew.writeShort(success ? 1 : 0);
    }
    return mplew.getPacket();
  }

  public static final byte[] showOwnPetLevelUp(final byte index) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
    mplew.write(4);
    mplew.write(0);
    mplew.write(index); // Pet Index

    return mplew.getPacket();
  }

  public static final byte[] showPetLevelUp(final MapleCharacter chr, final byte index) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
    mplew.writeInt(chr.getId());
    mplew.write(4);
    mplew.write(0);
    mplew.write(index);

    return mplew.getPacket();
  }

  public static final byte[] emptyStatUpdate() {
    return MaplePacketCreator.enableActions();
  }

  public static final byte[] petStatUpdate_Empty() {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
    mplew.write(0);
    mplew.writeInt(MapleStat.PET.getValue());
    mplew.writeZeroBytes(25);
    return mplew.getPacket();
  }

  public static final byte[] petStatUpdate(final MapleCharacter chr) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
    mplew.write(0);
    mplew.writeInt(MapleStat.PET.getValue());

    byte count = 0;
    for (final MaplePet pet : chr.getPets()) {
      if (pet.getSummoned()) {
        mplew.writeLong(pet.getUniqueId());
        count++;
      }
    }
    while (count < 3) {
      mplew.writeZeroBytes(8);
      count++;
    }
    mplew.write(0);

    return mplew.getPacket();
  }

  public static final byte[] loadExceptionList(final int cid, final int petId, final String data) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PET_EXCEPTION_LIST.getValue());
    mplew.writeInt(cid);
    mplew.write(0); // lets make it 0, 0 = only boss pet
    mplew.writeLong(petId);
    final String[] ii = data.split(",");
    if (data.isEmpty()) {
      mplew.write(0);
      return mplew.getPacket();
    }
    mplew.write(ii.length > 10 ? 10 : ii.length);
    int i = 0;
    for (final String ids : ii) {
      i++;
      if (i > 10) {
        break;
      }
      mplew.writeInt(Integer.parseInt(ids));
    }

    return mplew.getPacket();
  }

  public static byte[] petAutoHP(int itemId) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PET_AUTO_HP.getValue());
    mplew.writeInt(itemId);

    return mplew.getPacket();
  }

  public static byte[] petAutoMP(int itemId) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PET_AUTO_MP.getValue());
    mplew.writeInt(itemId);

    return mplew.getPacket();
  }


}
