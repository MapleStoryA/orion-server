package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import handling.AbstractMaplePacketHandler;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class SpawnPetHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        chr.updateTick(packet.readInt());
        byte position = packet.readByte();
        boolean isLead = position > 0;

        final IItem item = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(position);
        if (item == null || item.getItemId() > 5000100 || item.getItemId() < 5000000) {
            return;
        }
        final MaplePet pet = item.getPet();
        if (pet.getSummoned()) {
            chr.unequipPet(pet, true, false);
        } else {
            chr.spawnPet(position, isLead);
        }
    }
}
