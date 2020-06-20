package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerStats;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class HealOverTimeHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null) {
      return;
    }
    chr.updateTick(slea.readInt());
    if (slea.available() >= 8) {
      slea.skip(4);
    }
    int healHP = slea.readShort();
    int healMP = slea.readShort();

    final PlayerStats stats = chr.getStat();

    if (stats.getHp() <= 0) {
      return;
    }

    if (healHP != 0) {// && chr.canHP(now + 1000)) {
      if (healHP > stats.getHealHP()) {
        healHP = (int) stats.getHealHP();
      }
      chr.addHP(healHP);
    }
    if (healMP != 0) {// && chr.canMP(now + 1000)) {
      if (healMP > stats.getHealMP()) {
        healMP = (int) stats.getHealMP();
      }
      chr.addMP(healMP);
    }

  }

}
