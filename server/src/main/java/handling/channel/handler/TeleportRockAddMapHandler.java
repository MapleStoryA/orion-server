package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.maps.FieldLimitType;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

import java.util.ArrayList;
import java.util.List;

@lombok.extern.slf4j.Slf4j
public class TeleportRockAddMapHandler extends AbstractMaplePacketHandler {
    private static final List<Integer> blockedMaps;
    public static final int DELETE = 0;
    public static final int ADD = 1;

    static {
        blockedMaps = new ArrayList<>();
        blockedMaps.add(190000000);
        blockedMaps.add(190000001);
        blockedMaps.add(190000002);
        blockedMaps.add(191000000);
        blockedMaps.add(191000001);
        blockedMaps.add(192000000);
        blockedMaps.add(192000001);
        blockedMaps.add(193000000);
        blockedMaps.add(195000000);
        blockedMaps.add(195010000);
        blockedMaps.add(195020000);
        blockedMaps.add(195030000);
        blockedMaps.add(196000000);
        blockedMaps.add(196010000);
        blockedMaps.add(197000000);
        blockedMaps.add(197010000);
        blockedMaps.add(199000000);
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final byte addrem = slea.readByte();
        final byte vip = slea.readByte();

        if (vip == 1) {
            if (addrem == DELETE) {
                chr.getVipTeleportRock().deleteMap(slea.readInt());
            } else if (addrem == ADD) {
                if ((!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) && !blockedMaps.contains(chr.getMapId())) {
                    chr.getVipTeleportRock().addMap(chr.getMapId());
                } else {
                    chr.dropMessage(1, "You may not add this map.");
                }
            }
        } else {
            if (addrem == 0) {
                chr.getRegTeleportRock().deleteMap(slea.readInt());
            } else if (addrem == 1) {
                if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
                    chr.getRegTeleportRock().addMap(chr.getMapId());
                } else {
                    chr.dropMessage(1, "You may not add this map.");
                }
            }
        }
        c.getSession().write(MTSCSPacket.getTeleportRockRefresh(chr, vip == 1, addrem == 3));

    }

}
