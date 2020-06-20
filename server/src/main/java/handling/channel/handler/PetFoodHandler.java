package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PetPacket;

public class PetFoodHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    int previousFullness = 100;
    MapleCharacter chr = c.getPlayer();
    MaplePet pet = null;
    if (chr == null) {
      return;
    }
    for (final MaplePet pets : chr.getPets()) {
      if (pets.getSummoned()) {
        if (pets.getFullness() < previousFullness) {
          previousFullness = pets.getFullness();
          pet = pets;
        }
      }
    }
    if (pet == null) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }

    slea.skip(6);
    final int itemId = slea.readInt();

    boolean gainCloseness = true;

    if (pet.getFullness() < 100) {
      int newFullness = pet.getFullness() + 100;
      if (newFullness > 100) {
        newFullness = 100;
      }
      pet.setFullness(newFullness);
      final byte index = chr.getPetIndex(pet);

      if (gainCloseness && pet.getCloseness() < 30000) {
        int newCloseness = pet.getCloseness() + 1;
        if (newCloseness > 30000) {
          newCloseness = 30000;
        }
        pet.setCloseness(newCloseness);
        if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
          pet.setLevel(pet.getLevel() + 1);

          c.getSession().write(PetPacket.showOwnPetLevelUp(index));
          chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr, index));
        }
      }
      c.getSession().write(PetPacket.updatePet(pet,
          chr.getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition())));
      chr.getMap().broadcastMessage(c.getPlayer(),
          PetPacket.commandResponse(chr.getId(), (byte) 1, index, true, true), true);
    } else {
      if (gainCloseness) {
        int newCloseness = pet.getCloseness() - 1;
        if (newCloseness < 0) {
          newCloseness = 0;
        }
        pet.setCloseness(newCloseness);
        if (newCloseness < GameConstants.getClosenessNeededForLevel(pet.getLevel())) {
          pet.setLevel(pet.getLevel() - 1);
        }
      }
      c.getSession().write(PetPacket.updatePet(pet,
          chr.getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition())));
      chr.getMap().broadcastMessage(chr,
          PetPacket.commandResponse(chr.getId(), (byte) 1, chr.getPetIndex(pet), false, true), true);
    }
    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, false);
    c.getSession().write(MaplePacketCreator.enableActions());

  }

}
