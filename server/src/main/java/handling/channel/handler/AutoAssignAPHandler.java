package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.PlayerStats;
import handling.packet.AbstractMaplePacketHandler;
import java.util.ArrayList;
import java.util.List;
import tools.MaplePacketCreator;
import tools.collection.Pair;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class AutoAssignAPHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        chr.updateTick(packet.readInt());
        packet.skip(4);
        if (packet.available() < 16) {
            log.info("AutoAssignAP : \n" + packet.toString(true));
            final String msg = "AutoAssignAP : \n" + packet.toString(true);
            log.info("Log_Packet_Except.rtf" + " : " + msg);
            return;
        }
        final int PrimaryStat = packet.readInt();
        final int amount = packet.readInt();
        final int SecondaryStat = packet.readInt();
        final int amount2 = packet.readInt();
        if (amount < 0 || amount2 < 0) {
            return;
        }

        final PlayerStats playerst = chr.getStat();

        List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
        c.getSession()
                .write(MaplePacketCreator.updatePlayerStats(
                        statupdate, true, chr.getJob().getId()));

        if (chr.getRemainingAp() == amount + amount2) {
            switch (PrimaryStat) {
                case 64: // Str
                    if (playerst.getStr() + amount > 999) {
                        return;
                    }
                    playerst.setStr((short) (playerst.getStr() + amount));
                    statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, (int) playerst.getStr()));
                    break;
                case 128: // Dex
                    if (playerst.getDex() + amount > 999) {
                        return;
                    }
                    playerst.setDex((short) (playerst.getDex() + amount));
                    statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, (int) playerst.getDex()));
                    break;
                case 256: // Int
                    if (playerst.getInt() + amount > 999) {
                        return;
                    }
                    playerst.setInt((short) (playerst.getInt() + amount));
                    statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, (int) playerst.getInt()));
                    break;
                case 512: // Luk
                    if (playerst.getLuk() + amount > 999) {
                        return;
                    }
                    playerst.setLuk((short) (playerst.getLuk() + amount));
                    statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, (int) playerst.getLuk()));
                    break;
                default:
                    c.getSession()
                            .write(MaplePacketCreator.updatePlayerStats(
                                    MaplePacketCreator.EMPTY_STATUPDATE,
                                    true,
                                    chr.getJob().getId()));
                    return;
            }
            switch (SecondaryStat) {
                case 64: // Str
                    if (playerst.getStr() + amount2 > 999) {
                        return;
                    }
                    playerst.setStr((short) (playerst.getStr() + amount2));
                    statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, (int) playerst.getStr()));
                    break;
                case 128: // Dex
                    if (playerst.getDex() + amount2 > 999) {
                        return;
                    }
                    playerst.setDex((short) (playerst.getDex() + amount2));
                    statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, (int) playerst.getDex()));
                    break;
                case 256: // Int
                    if (playerst.getInt() + amount2 > 999) {
                        return;
                    }
                    playerst.setInt((short) (playerst.getInt() + amount2));
                    statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, (int) playerst.getInt()));
                    break;
                case 512: // Luk
                    if (playerst.getLuk() + amount2 > 999) {
                        return;
                    }
                    playerst.setLuk((short) (playerst.getLuk() + amount2));
                    statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, (int) playerst.getLuk()));
                    break;
                default:
                    c.getSession()
                            .write(MaplePacketCreator.updatePlayerStats(
                                    MaplePacketCreator.EMPTY_STATUPDATE,
                                    true,
                                    chr.getJob().getId()));
                    return;
            }
            chr.setRemainingAp((short) (chr.getRemainingAp() - (amount + amount2)));
            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, chr.getRemainingAp()));
            c.getSession()
                    .write(MaplePacketCreator.updatePlayerStats(
                            statupdate, true, chr.getJob().getId()));
        }
    }
}
