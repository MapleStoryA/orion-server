package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.PlayerStats;
import handling.AbstractMaplePacketHandler;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.ArrayList;
import java.util.List;

public class AutoAssignAPHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    chr.updateTick(slea.readInt());
    slea.skip(4);
    if (slea.available() < 16) {
      System.out.println("AutoAssignAP : \n" + slea.toString(true));
      FileoutputUtil.log(FileoutputUtil.PacketEx_Log, "AutoAssignAP : \n" + slea.toString(true));
      return;
    }
    final int PrimaryStat = slea.readInt();
    final int amount = slea.readInt();
    final int SecondaryStat = slea.readInt();
    final int amount2 = slea.readInt();
    if (amount < 0 || amount2 < 0) {
      return;
    }

    final PlayerStats playerst = chr.getStat();

    List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
    c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));

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
          c.getSession().write(
              MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr.getJob()));
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
          c.getSession().write(
              MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr.getJob()));
          return;
      }
      chr.setRemainingAp((short) (chr.getRemainingAp() - (amount + amount2)));
      statupdate.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, (int) chr.getRemainingAp()));
      c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
    }

  }

}
