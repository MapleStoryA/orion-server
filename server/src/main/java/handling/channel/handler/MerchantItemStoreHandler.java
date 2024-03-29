package handling.channel.handler;

import client.MapleClient;
import client.inventory.IItem;
import handling.channel.handler.utils.HiredMerchantHandlerUtils;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.shops.MerchItemPackage;
import tools.packet.PlayerShopPacket;

@Slf4j
public class MerchantItemStoreHandler extends AbstractMaplePacketHandler {

    private static final int MERCH_FULL_MESOS = 31;
    private static final int MERCH_FULL_INVENTORY = 34;
    private static final int MERCH_RETRIEVED_ITEM_SUCCESS = 30;

    public static boolean takeOutMerchantItems(MapleClient c) {
        final MerchItemPackage pack = HiredMerchantHandlerUtils.loadItemFrom_Database(
                c.getPlayer().getId(), c.getPlayer().getAccountID());
        if (pack == null) {
            c.getPlayer().dropMessage(1, "An unknown error occured.");
            return false;
        }
        if (c.getChannelServer().isShutdown()) {
            c.getPlayer().dropMessage(1, "The world is going to shut down.");
            c.getPlayer().setConversation(0);
            return false;
        }

        long currentMesos = c.getPlayer().getMeso() + pack.getMesos();
        if (currentMesos < 0 || currentMesos > Integer.MAX_VALUE) {
            c.getSession().write(PlayerShopPacket.merchItem_Message((byte) MERCH_FULL_MESOS));
            return false;
        }
        if (!HiredMerchantHandlerUtils.check(c.getPlayer(), pack)) {
            c.getSession().write(PlayerShopPacket.merchItem_Message((byte) MERCH_FULL_INVENTORY));
            return false;
        }
        if (HiredMerchantHandlerUtils.deletePackage(
                c.getPlayer().getId(), c.getPlayer().getAccountID(), pack.getPackageid())) {
            c.getPlayer().gainMeso(pack.getMesos(), false);
            for (IItem item : pack.getItems()) {
                MapleInventoryManipulator.addFromDrop(c, item, false);
            }
            c.getSession().write(PlayerShopPacket.merchItem_Message((byte) MERCH_RETRIEVED_ITEM_SUCCESS));
            return true;
        } else {
            c.getPlayer().dropMessage(1, "An unknown error occured.");
            return false;
        }
    }

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        if (c.getPlayer() == null) {
            return;
        }
        final byte operation = packet.readByte();

        switch (operation) {
            case 20: {
                c.getPlayer().dropMessage(1, "An unknown error occured.");
                break;
            }
            case 25: { // Request take out iteme
                if (c.getPlayer().getConversation() != 3) {
                    return;
                }
                c.getSession().write(PlayerShopPacket.merchItemStore((byte) 0x24));
                break;
            }
            case 26: { // Take out item
                takeOutMerchantItems(c);
                break;
            }
            case 28: { // Exit
                c.enableActions();
                break;
            }
        }
    }
}
