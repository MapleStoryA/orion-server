package handling.channel.handler;

import client.MapleClient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import tools.MaplePacketCreator;
import tools.packet.PlayerShopPacket;

@Slf4j
public class OwlWarpHandler extends AbstractMaplePacketHandler {

    public static final int OWL_ID = 2; // don't change. 0 = owner ID, 1 = store
    // ID, 2 = object ID

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getSession().write(MaplePacketCreator.enableActions());
        if (c.getPlayer().getMapId() >= 910000000
                && c.getPlayer().getMapId() <= 910000022
                && c.getPlayer().getPlayerShop() == null) {
            final int id = packet.readInt();
            final int map = packet.readInt();
            if (map >= 910000001 && map <= 910000022) {
                final MapleMap mapp = c.getChannelServer().getMapFactory().getMap(map);
                c.getPlayer().changeMap(mapp, mapp.getPortal(0));
                HiredMerchant merchant = null;
                List<MapleMapObject> objects;
                switch (0) {
                    case 0:
                        objects = mapp.getAllHiredMerchantsThreadsafe();
                        for (MapleMapObject ob : objects) {
                            if (ob instanceof final IMaplePlayerShop ips) {
                                if (ips instanceof final HiredMerchant merch) {
                                    if (merch.getOwnerId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    case 1:
                        objects = mapp.getAllHiredMerchantsThreadsafe();
                        for (MapleMapObject ob : objects) {
                            if (ob instanceof final IMaplePlayerShop ips) {
                                if (ips instanceof final HiredMerchant merch) {
                                    if (merch.getStoreId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        final MapleMapObject ob = mapp.getMapObject(id, MapleMapObjectType.HIRED_MERCHANT);
                        if (ob instanceof final IMaplePlayerShop ips) {
                            if (ips instanceof HiredMerchant) {
                                merchant = (HiredMerchant) ips;
                            }
                        }
                        break;
                }
                if (merchant != null) {
                    if (merchant.isOwner(c.getPlayer())) {
                        merchant.setOpen(false);
                        merchant.removeAllVisitors((byte) 16, (byte) 0);
                        c.getPlayer().setPlayerShop(merchant);
                        c.getSession().write(PlayerShopPacket.getHiredMerch(c.getPlayer(), merchant, false));
                    } else {
                        if (!merchant.isOpen() || !merchant.isAvailable()) {
                            c.getPlayer().dropMessage(1, "This shop is in maintenance, please come by later.");
                        } else {
                            if (merchant.getFreeSlot() == -1) {
                                c.getPlayer()
                                        .dropMessage(
                                                1,
                                                "This shop has reached it's maximum capacity,"
                                                        + " please come by later.");
                            } else if (merchant.isInBlackList(c.getPlayer().getName())) {
                                c.getPlayer().dropMessage(1, "You have been banned from this store.");
                            } else {
                                c.getPlayer().setPlayerShop(merchant);
                                merchant.addVisitor(c.getPlayer());
                                c.getSession().write(PlayerShopPacket.getHiredMerch(c.getPlayer(), merchant, false));
                            }
                        }
                    }
                } else {
                    c.getPlayer().dropMessage(1, "This shop is in maintenance, please come by later.");
                }
            }
        }
    }
}
