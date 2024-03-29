package handling.channel.handler;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.StructPotentialItem;
import tools.MaplePacketCreator;
import tools.helper.Randomizer;

@Slf4j
public class UseMagnifyGlassHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getPlayer().updateTick(packet.readInt());
        byte magnifyId = (byte) packet.readShort();
        byte position = (byte) packet.readShort();
        final IItem magnify = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(magnifyId);
        IItem toReveal = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(position);

        if (toReveal == null) {
            toReveal = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(position);
        }
        if (magnify == null || toReveal == null) {
            c.getSession().write(MaplePacketCreator.getInventoryFull());
            return;
        }
        final Equip eqq = (Equip) toReveal;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int reqLevel = ii.getReqLevel(eqq.getItemId()) / 10;
        if (eqq.getState() == 1
                && (magnify.getItemId() == 2460003
                        || (magnify.getItemId() == 2460002 && reqLevel <= 12)
                        || (magnify.getItemId() == 2460001 && reqLevel <= 7)
                        || (magnify.getItemId() == 2460000 && reqLevel <= 3))) {
            final List<List<StructPotentialItem>> pots =
                    new LinkedList<>(ii.getAllPotentialInfo().values());
            int new_state = Math.abs(eqq.getPotential1());
            if (new_state > 7 || new_state < 5) { // luls
                new_state = 5;
            }

            final int lines = (eqq.getPotential2() != 0 ? 3 : 2);

            while (eqq.getState() != new_state) {
                // 31001 = haste, 31002 = door, 31003 = se, 31004 = hb
                for (int i = 0; i < lines; i++) { // 2 or 3 line
                    boolean rewarded = false;
                    while (!rewarded) {
                        StructPotentialItem pot =
                                pots.get(Randomizer.nextInt(pots.size())).get(reqLevel);
                        if (pot != null
                                && pot.getReqLevel() / 10 <= reqLevel
                                && GameConstants.optionTypeFits(pot.getOptionType(), eqq.getItemId())
                                && GameConstants.potentialIDFits(pot.getPotentialID(), new_state, i)) { // optionType
                            // have to research optionType before making this
                            // truely sea-like
                            if (i == 0) {
                                eqq.setPotential1(pot.getPotentialID());
                            } else if (i == 1) {
                                eqq.setPotential2(pot.getPotentialID());
                            } else if (i == 2) {
                                eqq.setPotential3(pot.getPotentialID());
                            }
                            rewarded = true;
                        }
                    }
                }
            }
            c.getSession().write(MaplePacketCreator.scrolledItem(magnify, toReveal, false, true));
            c.getPlayer()
                    .getMap()
                    .broadcastMessage(
                            MaplePacketCreator.getMagnifyingEffect(c.getPlayer().getId(), eqq.getPosition()));
            MapleInventoryManipulator.removeFromSlot(
                    c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
        } else {
            c.getSession().write(MaplePacketCreator.getInventoryFull());
        }
    }
}
