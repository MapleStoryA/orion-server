package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStorage;
import tools.MaplePacketCreator;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class StorageHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final byte mode = packet.readByte();
        if (chr == null) {
            return;
        }
        final MapleStorage storage = chr.getStorage();

        switch (mode) {
            case 4:
                { // Take Out
                    final byte type = packet.readByte();
                    final byte slot =
                            storage.getSlot(MapleInventoryType.getByType(type), packet.readByte());
                    final IItem item = storage.takeOut(slot);

                    if (item != null) {
                        if (!MapleInventoryManipulator.checkSpace(
                                c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                            storage.store(item);
                            chr.dropMessage(1, "Your inventory is full");
                        } else {
                            MapleInventoryManipulator.addFromDrop(c, item, false);
                        }
                        storage.sendTakenOut(c, GameConstants.getInventoryType(item.getItemId()));
                    } else {
                        // AutobanManager.getInstance().autoban(c, "Trying to take out
                        // item from storage which does not exist.");
                        return;
                    }
                    break;
                }
            case 5:
                { // Store
                    final byte slot = (byte) packet.readShort();
                    final int itemId = packet.readInt();
                    short quantity = packet.readShort();
                    final MapleItemInformationProvider ii =
                            MapleItemInformationProvider.getInstance();
                    if (quantity < 1) {
                        // AutobanManager.getInstance().autoban(c, "Trying to store " +
                        // quantity + " of " + itemId);
                        return;
                    }
                    if (storage.isFull()) {
                        c.getSession().write(MaplePacketCreator.getStorageFull());
                        return;
                    }

                    if (chr.getMeso() < 100) {
                        chr.dropMessage(1, "You don't have enough mesos to store the item");
                    } else {
                        MapleInventoryType type = GameConstants.getInventoryType(itemId);
                        IItem item = chr.getInventory(type).getItem(slot).copy();

                        if (GameConstants.isPet(item.getItemId())) {
                            c.getSession().write(MaplePacketCreator.enableActions());
                            return;
                        }
                        final byte flag = item.getFlag();
                        if (item.getItemId() == itemId
                                && (item.getQuantity() >= quantity
                                        || GameConstants.isThrowingStar(itemId)
                                        || GameConstants.isBullet(itemId))) {
                            if (ii.isDropRestricted(item.getItemId())) {
                                if (ItemFlag.KARMA_EQ.check(flag)) {
                                    item.setFlag((byte) (flag - ItemFlag.KARMA_EQ.getValue()));
                                } else if (ItemFlag.KARMA_USE.check(flag)) {
                                    item.setFlag((byte) (flag - ItemFlag.KARMA_USE.getValue()));
                                } else {
                                    c.getSession().write(MaplePacketCreator.enableActions());
                                    return;
                                }
                            }
                            if (GameConstants.isThrowingStar(itemId)
                                    || GameConstants.isBullet(itemId)) {
                                quantity = item.getQuantity();
                            }
                            chr.gainMeso(-100, false, true, false);
                            MapleInventoryManipulator.removeFromSlot(
                                    c, type, slot, quantity, false);
                            item.setQuantity(quantity);
                            storage.store(item);
                        } else {
                            AutobanManager.getInstance()
                                    .addPoints(
                                            c,
                                            1000,
                                            0,
                                            "Trying to store non-matching itemid ("
                                                    + itemId
                                                    + "/"
                                                    + item.getItemId()
                                                    + ") or quantity not in posession ("
                                                    + quantity
                                                    + "/"
                                                    + item.getQuantity()
                                                    + ")");
                            return;
                        }
                    }
                    storage.sendStored(c, GameConstants.getInventoryType(itemId));
                    break;
                }
            case 7:
                {
                    int meso = packet.readInt();
                    final int storageMesos = storage.getMeso();
                    final int playerMesos = chr.getMeso();

                    if ((meso > 0 && storageMesos >= meso) || (meso < 0 && playerMesos >= -meso)) {
                        if (meso < 0 && (storageMesos - meso) < 0) { // storing with
                            // overflow
                            meso = -(Integer.MAX_VALUE - storageMesos);
                            if ((-meso) > playerMesos) { // should never happen just a
                                // failsafe
                                return;
                            }
                        } else if (meso > 0 && (playerMesos + meso) < 0) { // taking out
                            // with
                            // overflow
                            meso = (Integer.MAX_VALUE - playerMesos);
                            if ((meso) > storageMesos) { // should never happen just a
                                // failsafe
                                return;
                            }
                        }
                        storage.setMeso(storageMesos - meso);
                        chr.gainMeso(meso, false, true, false);
                    } else {
                        AutobanManager.getInstance()
                                .addPoints(
                                        c,
                                        1000,
                                        0,
                                        "Trying to store or take out unavailable amount of mesos ("
                                                + meso
                                                + "/"
                                                + storage.getMeso()
                                                + "/"
                                                + c.getPlayer().getMeso()
                                                + ")");
                        return;
                    }
                    storage.sendMeso(c);
                    break;
                }
            case 6:
                storage.sendStored(c, MapleInventoryType.EQUIP);
            case 8:
                {
                    storage.close();
                    chr.setConversation(0);
                    break;
                }
            default:
                log.info("Unhandled Storage mode : " + mode);
                break;
        }
    }
}
