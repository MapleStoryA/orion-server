package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.CInPacket;
import tools.packet.PetPacket;

import java.util.Comparator;
import java.util.Optional;

@Slf4j
public class PetFoodHandler extends AbstractMaplePacketHandler {

    private static final int MAX_FULLNESS = 100;
    private static final int MAX_CLOSENESS = 30000;

    @Override
    public void handlePacket(CInPacket packet, MapleClient client) {
        MapleCharacter player = client.getPlayer();
        if (player == null) {
            log.error("Player not found in PetFoodHandler");
            return;
        }

        Optional<MaplePet> petOptional = findLeastFullPet(player);
        if (!petOptional.isPresent()) {
            client.getSession().write(MaplePacketCreator.enableActions());
            return;
        }

        MaplePet pet = petOptional.get();
        packet.skip(6); // Skipping bytes
        final int itemId = packet.readInt();
        updatePetFullnessAndCloseness(client, pet, itemId);
        MapleInventoryManipulator.removeById(client, MapleInventoryType.USE, itemId, 1, true, false);
        client.getSession().write(MaplePacketCreator.enableActions());
    }

    private Optional<MaplePet> findLeastFullPet(MapleCharacter player) {
        return player.getPets().stream()
                .filter(MaplePet::getSummoned)
                .min(Comparator.comparingInt(MaplePet::getFullness));
    }

    private void updatePetFullnessAndCloseness(MapleClient client, MaplePet pet, int itemId) {
        boolean gainCloseness = true;
        if (pet.getFullness() < MAX_FULLNESS) {
            increaseFullness(pet);
            increaseClosenessIfPossible(client, pet, gainCloseness);
        } else {
            decreaseClosenessIfPossible(client, pet, gainCloseness);
        }
        updatePetStatus(client, pet);
    }

    private void increaseFullness(MaplePet pet) {
        int newFullness = Math.min(pet.getFullness() + 100, MAX_FULLNESS);
        pet.setFullness(newFullness);
    }

    private void increaseClosenessIfPossible(MapleClient client, MaplePet pet, boolean gainCloseness) {
        if (gainCloseness && pet.getCloseness() < MAX_CLOSENESS) {
            int newCloseness = Math.min(pet.getCloseness() + 1, MAX_CLOSENESS);
            pet.setCloseness(newCloseness);
            checkAndHandlePetLevelUp(client, pet);
        }
    }

    private void decreaseClosenessIfPossible(MapleClient client, MaplePet pet, boolean gainCloseness) {
        if (gainCloseness) {
            int newCloseness = Math.max(pet.getCloseness() - 1, 0);
            pet.setCloseness(newCloseness);
            checkAndHandlePetLevelDown(client, pet);
        }
    }

    private void updatePetStatus(MapleClient client, MaplePet pet) {
        MapleCharacter player = client.getPlayer();
        byte index = player.getPetIndex(pet);
        client.getSession().write(PetPacket.updatePet(pet, player.getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition())));
        player.getMap().broadcastMessage(player, PetPacket.commandResponse(player.getId(), (byte) 1, index, pet.getFullness() >= 100, true), true);
    }

    private void checkAndHandlePetLevelUp(MapleClient client, MaplePet pet) {
        byte index = client.getPlayer().getPetIndex(pet);
        if (pet.getCloseness() >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
            pet.setLevel(pet.getLevel() + 1);
            client.getSession().write(PetPacket.showOwnPetLevelUp(index));
            client.getPlayer().getMap().broadcastMessage(PetPacket.showPetLevelUp(client.getPlayer(), index));
        }
    }

    private void checkAndHandlePetLevelDown(MapleClient client, MaplePet pet) {
        if (pet.getCloseness() < GameConstants.getClosenessNeededForLevel(pet.getLevel())) {
            pet.setLevel(pet.getLevel() - 1);
        }
    }
}
