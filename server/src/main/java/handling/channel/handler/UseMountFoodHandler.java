package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import constants.GameConstants;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;

@Slf4j
public class UseMountFoodHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        c.getPlayer().updateTick(packet.readInt());
        final byte slot = (byte) packet.readShort();
        final int itemid = packet.readInt(); // 2260000 usually
        final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        final MapleMount mount = chr.getMount();

        if ((itemid / 10000 == 226)
                && toUse != null
                && toUse.getQuantity() > 0
                && toUse.getItemId() == itemid
                && mount != null) {
            final int fatigue = mount.getFatigue();

            boolean levelup = false;
            mount.setFatigue((byte) -30);

            if (fatigue > 0) {
                mount.increaseExp();
                final int level = mount.getLevel();
                if (mount.getExp() >= GameConstants.getMountExpNeededForLevel(level + 1) && level < 31) {
                    mount.setLevel((byte) (level + 1));
                    levelup = true;
                }
            }
            chr.getMap().broadcastMessage(MaplePacketCreator.updateMount(chr, levelup));
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}
