package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetCommand;
import client.inventory.PetDataFactory;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PetPacket;

public class PetCommandHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final byte petIndex = chr.getPetIndex((int) slea.readLong());
    if (petIndex == -1) {
      return;
    }
    MaplePet pet = chr.getPet(petIndex);
    if (pet == null) {
      return;
    }
    slea.skip(1);
    byte command = slea.readByte();
    if (pet.getPetItemId() == 5000042 && command == 1) {
      command = 9; // kino bug (sit replace with poop)
    }
    final PetCommand petCommand = PetDataFactory.getPetCommand(pet.getPetItemId(), (int) command);
    if (petCommand == null) {
      return;
    }

    boolean success = false;
    if (Randomizer.nextInt(99) <= petCommand.getProbability()) {
      success = true;
      if (pet.getCloseness() < 30000) {
        int newCloseness = pet.getCloseness() + petCommand.getIncrease();
        if (newCloseness > 30000) {
          newCloseness = 30000;
        }
        pet.setCloseness(newCloseness);
        if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
          pet.setLevel(pet.getLevel() + 1);
          c.getSession().write(PetPacket.showOwnPetLevelUp(petIndex));
          chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr, petIndex));
        }
        c.getSession().write(PetPacket.updatePet(pet,
            chr.getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition())));
      }
    }
    chr.getMap().broadcastMessage(chr, PetPacket.commandResponse(chr.getId(), command, petIndex, success, false),
        true);

  }

}
